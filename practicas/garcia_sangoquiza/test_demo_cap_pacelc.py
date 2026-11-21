"""Pruebas del modelo; no validan el backend CLAB ni PostgreSQL."""

import unittest

from demo_cap_pacelc import (
    clab_model,
    execution_summary,
    general_model,
    overlaps,
    read_model,
    reserve,
    run_workers,
)


def slot(start=540, end=600, lab="LAB-01", teacher="A"):
    return {"inicio": start, "fin": end, "laboratorio": lab, "docente": teacher}


def votes():
    # Datos unitarios explícitos; no son mediciones experimentales.
    return [
        {"nodo": node, "ve_disponible": True, "solicitud": slot(teacher=node)}
        for node in ("A", "B")
    ]


class ReservationTests(unittest.TestCase):
    def test_overlap_same_laboratory(self):
        self.assertTrue(overlaps(slot(), slot(570, 630)))

    def test_adjacent_intervals_do_not_overlap(self):
        self.assertFalse(overlaps(slot(), slot(600, 660)))

    def test_different_laboratories_do_not_overlap(self):
        self.assertFalse(overlaps(slot(), slot(lab="LAB-02")))

    def test_invalid_interval_rejected(self):
        with self.assertRaises(ValueError):
            overlaps(slot(600, 540), slot())

    def test_two_requests_only_one_confirmed(self):
        state = []
        self.assertEqual(reserve(state, slot()), "CONFIRMADA")
        self.assertEqual(reserve(state, slot(teacher="B")), "RECHAZADA_POR_CRUCE")
        self.assertEqual(len(state), 1)

    def test_unknown_coordination_is_not_confirmation(self):
        state = []
        self.assertEqual(reserve(state, slot(), False), "SIN_CONFIRMAR")
        self.assertEqual(state, [])

    def test_reservation_copies_request(self):
        state = []
        request = slot()
        reserve(state, request)
        request["laboratorio"] = "MODIFICADO"
        self.assertEqual(state[0]["laboratorio"], "LAB-01")


class ModelTests(unittest.TestCase):
    def test_general_model_exposes_overselling(self):
        model = general_model(votes())
        self.assertEqual(model["ventas_locales_sin_coordinar"], 2)
        self.assertTrue(model["sobreventa"])
        self.assertEqual(model["politica_coordinada"]["B"], "SIN_CONFIRMAR")
        self.assertEqual(model["stock_final_coordinado"], 0)

    def test_clab_invariant_and_replica_refresh(self):
        model = clab_model(votes(), 0)
        self.assertEqual(model["lecturas_iniciales_libre"], 2)
        self.assertEqual(len(model["reservas_confirmadas"]), 1)
        self.assertEqual(model["sin_particion"]["B"], "RECHAZADA_POR_CRUCE")
        self.assertEqual(model["con_particion_modelada"]["B"], "SIN_CONFIRMAR")
        self.assertTrue(model["replica_atrasada"]["disponible"])
        self.assertFalse(model["lectura_autoridad"]["disponible"])
        self.assertFalse(model["replica_actualizada"]["disponible"])

    def test_read_returns_independent_state_and_nonnegative_time(self):
        state = {"version": 0, "disponible": True}
        result = read_model(state)
        self.assertGreaterEqual(result["elapsed_ms"], 0)
        result["version"] = 10
        self.assertEqual(state["version"], 0)

    def test_cpu_measurement_does_not_assume_parallelism(self):
        samples = [
            {"pid": 1, "inicio_ns": 0, "fin_ns": 100, "cpu_ns": 40},
            {"pid": 2, "inicio_ns": 20, "fin_ns": 120, "cpu_ns": 40},
        ]
        result = execution_summary(samples)
        self.assertTrue(result["intervalos_solapados"])
        self.assertFalse(result["evidencia_mas_de_una_cpu_logica"])

    def test_two_real_worker_processes(self):
        result = run_workers("clab", 0)
        self.assertEqual([x["nodo"] for x in result], ["A", "B"])
        self.assertNotEqual(result[0]["pid"], result[1]["pid"])
        self.assertTrue(all(x["ve_disponible"] for x in result))
        self.assertTrue(all(x["fin_ns"] >= x["inicio_ns"] for x in result))


if __name__ == "__main__":
    unittest.main()
