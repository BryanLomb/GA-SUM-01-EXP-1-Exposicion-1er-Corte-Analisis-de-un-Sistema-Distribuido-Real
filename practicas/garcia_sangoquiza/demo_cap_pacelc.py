#!/usr/bin/env python3
"""Modelo local de CAP/PACELC: ventas y reservas CLAB, no backend productivo."""

import argparse
import datetime
import getpass
import hashlib
import json
import multiprocessing
import os
from pathlib import Path
import platform
import queue
import socket
import time


def overlaps(first, second):
    """Intervalos semiabiertos [inicio, fin), por laboratorio."""
    for slot in (first, second):
        if slot["inicio"] >= slot["fin"]:
            raise ValueError("El inicio debe ser anterior al fin")
    return (
        first["laboratorio"] == second["laboratorio"]
        and first["inicio"] < second["fin"]
        and second["inicio"] < first["fin"]
    )


def reserve(confirmed, request, can_coordinate=True):
    """Decisión serial del coordinador del MODELO; no una transacción SQL."""
    if not can_coordinate:
        return "SIN_CONFIRMAR"
    if any(overlaps(old, request) for old in confirmed):
        return "RECHAZADA_POR_CRUCE"
    # Validación y registro juntos en este único coordinador.
    # No sería seguro copiar esta función sin protección a varios escritores.
    overlaps(request, request)
    confirmed.append(dict(request))
    return "CONFIRMADA"


def _cpu_work(milliseconds):
    start = time.process_time_ns()
    value = 1
    while time.process_time_ns() - start < milliseconds * 1_000_000:
        for _ in range(512):
            value = (value * 1664525 + 1013904223) & 0xFFFFFFFF
    return value


def _worker(node, case, work_ms, ready, start, results):
    try:
        # Cada proceso recibe una copia inicial independiente, no datos CLAB.
        local_stock = 1
        local_reservations = []
        request = {
            "docente": "Docente " + node,
            "laboratorio": "LAB-01",
            "inicio": 9 * 60,
            "fin": 10 * 60,
        }
        ready.put(node)
        if not start.wait(timeout=20):
            raise RuntimeError("No se recibió la señal de inicio")
        wall_start = time.perf_counter_ns()
        cpu_start = time.process_time_ns()
        checksum = _cpu_work(work_ms)
        if case == "general":
            available = local_stock > 0
            if available:
                local_stock -= 1
        else:
            available = not any(overlaps(x, request) for x in local_reservations)
        cpu_end = time.process_time_ns()
        wall_end = time.perf_counter_ns()
        results.put({
            "nodo": node,
            "pid": os.getpid(),
            "ve_disponible": available,
            "solicitud": request if case == "clab" else None,
            "stock_local_final": local_stock if case == "general" else None,
            "inicio_ns": wall_start,
            "fin_ns": wall_end,
            "cpu_ns": cpu_end - cpu_start,
            "checksum": checksum,
        })
    except Exception as error:
        results.put({"nodo": node, "error": str(error)})


def run_workers(case, work_ms):
    """Dos procesos con inicio coordinado; medir no presupone paralelismo."""
    context = multiprocessing.get_context("spawn")
    ready = context.Queue()
    results = context.Queue()
    start = context.Event()
    processes = [
        context.Process(
            target=_worker,
            args=(node, case, work_ms, ready, start, results),
        )
        for node in ("A", "B")
    ]
    started = []
    try:
        for process in processes:
            process.start()
            started.append(process)
        for _ in started:
            ready.get(timeout=25)
        start.set()
        votes = [results.get(timeout=30) for _ in started]
        for process in started:
            process.join(timeout=10)
            if process.exitcode != 0:
                raise RuntimeError("Un trabajador no terminó correctamente")
        if any("error" in vote for vote in votes):
            raise RuntimeError(str(votes))
        return sorted(votes, key=lambda vote: vote["nodo"])
    except queue.Empty as error:
        raise RuntimeError("Tiempo de espera agotado para un trabajador") from error
    finally:
        start.set()
        for process in started:
            if process.is_alive():
                process.terminate()
                process.join(timeout=5)
        ready.close()
        results.close()


def execution_summary(votes):
    span = max(x["fin_ns"] for x in votes) - min(x["inicio_ns"] for x in votes)
    cpu = sum(x["cpu_ns"] for x in votes)
    overlap = min(x["fin_ns"] for x in votes) > max(x["inicio_ns"] for x in votes)
    ratio = cpu / span if span else 0
    return {
        "pids": [x["pid"] for x in votes],
        "intervalos_solapados": overlap,
        "ventana_ms": span / 1_000_000,
        "cpu_acumulada_ms": cpu / 1_000_000,
        "cpu_sobre_ventana": ratio,
        "evidencia_mas_de_una_cpu_logica": ratio > 1.05,
        "umbral_conservador": 1.05,
    }


def general_model(votes):
    ap_sales = sum(bool(vote["ve_disponible"]) for vote in votes)
    # Partición MODELADA: A alcanza al único decisor; B no puede consultarlo.
    # Los canales de control de Python siguen activos: no se corta la red real.
    stock = 1
    coordinated = {}
    for vote in sorted(votes, key=lambda item: item["nodo"]):
        if vote["nodo"] == "B":
            coordinated["B"] = "SIN_CONFIRMAR"
        elif stock:
            stock -= 1
            coordinated[vote["nodo"]] = "CONFIRMADA"
        else:
            coordinated[vote["nodo"]] = "RECHAZADA_SIN_STOCK"
    return {
        "stock_inicial_global": 1,
        "ventas_locales_sin_coordinar": ap_sales,
        "sobreventa": ap_sales > 1,
        "politica_coordinada": coordinated,
        "stock_final_coordinado": stock,
    }


def read_model(state, delay_ms=0):
    start = time.perf_counter_ns()
    if delay_ms:
        time.sleep(delay_ms / 1000)
    result = dict(state)
    result["elapsed_ms"] = (time.perf_counter_ns() - start) / 1_000_000
    result["retardo_introducido_ms"] = delay_ms
    return result


def clab_model(votes, delay_ms):
    confirmed = []
    decisions = {
        vote["nodo"]: reserve(confirmed, vote["solicitud"])
        for vote in sorted(votes, key=lambda item: item["nodo"])
    }
    with_partition = []
    partition_decisions = {
        vote["nodo"]: reserve(
            with_partition, vote["solicitud"], vote["nodo"] != "B"
        )
        for vote in sorted(votes, key=lambda item: item["nodo"])
    }
    # Sin partición: hay un commit nuevo y una réplica todavía no actualizada.
    primary = {"version": 1, "disponible": False}
    replica = {"version": 0, "disponible": True}
    local_read = read_model(replica)
    coordinated_read = read_model(primary, delay_ms)
    replica = dict(primary)  # Paso explícito del modelo, no replicación real.
    refreshed_read = read_model(replica)
    return {
        "lecturas_iniciales_libre": sum(x["ve_disponible"] for x in votes),
        "sin_particion": decisions,
        "reservas_confirmadas": confirmed,
        "con_particion_modelada": partition_decisions,
        "replica_atrasada": local_read,
        "lectura_autoridad": coordinated_read,
        "replica_actualizada": refreshed_read,
    }


def print_execution(summary):
    print("Procesos:", ", ".join(str(pid) for pid in summary["pids"]))
    print("Intervalos solapados:", "SI" if summary["intervalos_solapados"] else "NO")
    print(
        "CPU acumulada / ventana:",
        f"{summary['cpu_acumulada_ms']:.1f} / {summary['ventana_ms']:.1f} ms",
        f"(razón {summary['cpu_sobre_ventana']:.2f})",
    )
    if summary["evidencia_mas_de_una_cpu_logica"]:
        print("Medición compatible con uso simultáneo de más de una CPU lógica.")
    else:
        print("Esta medición NO acredita uso simultáneo de más de una CPU lógica.")


def print_case(case, item):
    print("\n===", "GENERAL: ÚLTIMA UNIDAD" if case == "general" else "PFC CLAB: LAB-01, 09:00-10:00", "===")
    print_execution(item["ejecucion"])
    model = item["modelo"]
    if case == "general":
        print("Partición MODELADA: dos copias aisladas, una unidad inicial.")
        print("Sin coordinar: ventas =", model["ventas_locales_sin_coordinar"], "| sobreventa =", model["sobreventa"])
        print("Coordinando: A =", model["politica_coordinada"]["A"], "| B =", model["politica_coordinada"]["B"])
        print("Lección CAP: no confirmar sin autoridad protege el estado y limita A.")
    else:
        print("Dos docentes leen LIBRE antes de intentar reservar.")
        print("Confirmación central: A =", model["sin_particion"]["A"], "| B =", model["sin_particion"]["B"])
        print("Reservas finales para ese laboratorio/intervalo:", len(model["reservas_confirmadas"]))
        print("Partición MODELADA: B =", model["con_particion_modelada"]["B"])
        print("PACELC sin partición: réplica atrasada frente a consulta coordinada")
        for label, key in [("Copia local", "replica_atrasada"), ("Autoridad", "lectura_autoridad"), ("Copia al día", "replica_actualizada")]:
            reading = model[key]
            status = "LIBRE" if reading["disponible"] else "OCUPADO"
            print(f"  {label:12} v{reading['version']} {status:7} | {reading['elapsed_ms']:.3f} ms | retardo impuesto {reading['retardo_introducido_ms']} ms")
        print("Lección: consultar una copia NO confirma una reserva.")
    print("Alcance: modelo local; no es ejecución del backend ni de PostgreSQL.")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--caso", choices=["general", "clab", "todos"], default="todos")
    parser.add_argument("--trabajo-ms", type=int, default=150, help="CPU artificial por proceso (0-2000 ms)")
    parser.add_argument("--retardo-ms", type=int, default=80, help="Espera artificial de consulta coordinada (0-2000 ms)")
    parser.add_argument("--salida", type=Path, help="JSON de esta ejecución; no sobrescribe archivos")
    args = parser.parse_args()
    if not (0 <= args.trabajo_ms <= 2000 and 0 <= args.retardo_ms <= 2000):
        parser.error("Los tiempos deben estar entre 0 y 2000 ms")
    if args.salida and args.salida.exists():
        parser.error("La salida ya existe; usa otro nombre para conservar el registro")
    metadata = {
        "fecha": datetime.datetime.now().astimezone().isoformat(),
        "usuario": getpass.getuser(),
        "equipo": socket.gethostname(),
        "python": platform.python_version(),
        "plataforma": platform.platform(),
        "codigo_sha256": hashlib.sha256(Path(__file__).read_bytes()).hexdigest(),
        "trabajo_cpu_artificial_ms": args.trabajo_ms,
        "retardo_consulta_artificial_ms": args.retardo_ms,
    }
    report = {
        "tipo": "modelo_didactico_local",
        "datos": "sinteticos; no proceden de CLAB",
        "metadatos": metadata,
        "casos": {},
    }
    print("MODELO DIDÁCTICO CAP/PACELC | DATOS SINTÉTICOS")
    print(metadata["fecha"], "|", metadata["usuario"], "@", metadata["equipo"])
    print("Python", metadata["python"], "| CPU artificial:", args.trabajo_ms, "ms/proceso")
    for case in (["general", "clab"] if args.caso == "todos" else [args.caso]):
        votes = run_workers(case, args.trabajo_ms)
        item = {
            "trabajadores": votes,
            "ejecucion": execution_summary(votes),
            "modelo": general_model(votes) if case == "general" else clab_model(votes, args.retardo_ms),
        }
        report["casos"][case] = item
        print_case(case, item)
    if args.salida:
        args.salida.parent.mkdir(parents=True, exist_ok=True)
        with args.salida.open("x", encoding="utf-8") as output:
            json.dump(report, output, ensure_ascii=False, indent=2)
            output.write("\n")
        print("\nRegistro guardado:", args.salida)


if __name__ == "__main__":
    main()
