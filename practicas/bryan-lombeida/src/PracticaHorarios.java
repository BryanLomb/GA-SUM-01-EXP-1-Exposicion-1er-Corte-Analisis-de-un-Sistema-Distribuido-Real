import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public final class PracticaHorarios {
    private static final ThreadMXBean CPU = ManagementFactory.getThreadMXBean();

    enum Estado { LIBRE, CRUCE_ESTUDIANTE, CRUCE_DOCENTE, CRUCE_AMBOS, SIN_COMPROBAR }

    static final class Intervalo {
        final LocalDateTime inicio;
        final LocalDateTime fin;

        Intervalo(String inicio, String fin) {
            this.inicio = LocalDateTime.parse(inicio);
            this.fin = LocalDateTime.parse(fin);
            if (!this.inicio.isBefore(this.fin)) {
                throw new IllegalArgumentException("El inicio debe preceder al fin");
            }
        }

        boolean cruza(Intervalo otro) {
            return inicio.isBefore(otro.fin) && otro.inicio.isBefore(fin);
        }
    }

    static final class Datos {
        final Map<String, Intervalo> franjas = new LinkedHashMap<>();
        final List<Intervalo> estudiante = new ArrayList<>();
        final List<Intervalo> docente = new ArrayList<>();
    }

    static final class Intento {
        final String id;
        final int numero;
        final Estado estado;
        final String trabajador;
        final long inicio;
        final long fin;
        final long cpu;

        Intento(String id, int numero, Estado estado, long inicio, long fin, long cpu) {
            this.id = id;
            this.numero = numero;
            this.estado = estado;
            this.trabajador = Thread.currentThread().getName();
            this.inicio = inicio;
            this.fin = fin;
            this.cpu = cpu;
        }
    }

    static final class Ejecucion {
        final Map<String, Estado> resultados = new LinkedHashMap<>();
        final List<Intento> intentos = new ArrayList<>();
        final long inicio = System.nanoTime();
    }

    private static List<String[]> csv(Path archivo, String encabezado) throws IOException {
        List<String> lineas = Files.readAllLines(archivo);
        if (lineas.isEmpty() || !lineas.get(0).trim().equals(encabezado)) {
            throw new IllegalArgumentException(archivo + ": encabezado esperado: " + encabezado);
        }
        List<String[]> filas = new ArrayList<>();
        for (int i = 1; i < lineas.size(); i++) {
            if (lineas.get(i).isBlank()) continue;
            String[] campos = lineas.get(i).split(",", -1);
            if (campos.length != 3) {
                throw new IllegalArgumentException(archivo + ":" + (i + 1) + ": se esperan tres columnas");
            }
            for (int j = 0; j < campos.length; j++) campos[j] = campos[j].trim();
            filas.add(campos);
        }
        return filas;
    }

    static Datos cargar(Path franjas, Path agenda) throws IOException {
        Datos datos = new Datos();
        for (String[] f : csv(franjas, "id,inicio,fin")) {
            if (f[0].isEmpty() || datos.franjas.putIfAbsent(f[0], new Intervalo(f[1], f[2])) != null) {
                throw new IllegalArgumentException("ID de franja vacío o repetido: " + f[0]);
            }
        }
        if (datos.franjas.isEmpty()) throw new IllegalArgumentException("No hay franjas candidatas");
        for (String[] f : csv(agenda, "actor,inicio,fin")) {
            Intervalo intervalo = new Intervalo(f[1], f[2]);
            switch (f[0]) {
                case "estudiante": datos.estudiante.add(intervalo); break;
                case "docente": datos.docente.add(intervalo); break;
                default: throw new IllegalArgumentException("Actor inválido: " + f[0]);
            }
        }
        return datos;
    }

    private static boolean ocupado(Intervalo franja, List<Intervalo> agenda) {
        for (Intervalo evento : agenda) {
            if (franja.cruza(evento)) return true;
        }
        return false;
    }

    private static Estado comprobar(Datos datos, Intervalo franja) {
        boolean estudiante = ocupado(franja, datos.estudiante);
        boolean docente = ocupado(franja, datos.docente);
        if (estudiante && docente) return Estado.CRUCE_AMBOS;
        if (estudiante) return Estado.CRUCE_ESTUDIANTE;
        if (docente) return Estado.CRUCE_DOCENTE;
        return Estado.LIBRE;
    }

    private static long cpuActual() {
        return CPU.isCurrentThreadCpuTimeSupported() ? CPU.getCurrentThreadCpuTime() : -1;
    }

    private static void enviar(CompletionService<Intento> cola, Datos datos, String id,
                               int numero, String fallo, boolean persistente, int repeticiones) {
        cola.submit(() -> {
            long inicio = System.nanoTime();
            long cpuInicio = cpuActual();
            Estado estado = Estado.SIN_COMPROBAR;
            if (!id.equals(fallo) || (numero > 1 && !persistente)) {
                Intervalo franja = datos.franjas.get(id);
                for (int i = 0; i < repeticiones; i++) estado = comprobar(datos, franja);
            }
            long cpuFin = cpuActual();
            return new Intento(id, numero, estado, inicio, System.nanoTime(),
                    cpuInicio < 0 || cpuFin < 0 ? -1 : cpuFin - cpuInicio);
        });
    }

    static Ejecucion ejecutar(Datos datos, int trabajadores, String fallo, boolean persistente,
                              int repeticiones) throws InterruptedException, ExecutionException {
        if (fallo != null && !datos.franjas.containsKey(fallo)) {
            throw new IllegalArgumentException("No existe la franja de fallo: " + fallo);
        }
        if (repeticiones < 1) throw new IllegalArgumentException("Las repeticiones deben ser positivas");
        Ejecucion ejecucion = new Ejecucion();
        AtomicInteger ids = new AtomicInteger();
        ExecutorService grupo = Executors.newFixedThreadPool(trabajadores,
                tarea -> new Thread(tarea, "trabajador-" + ids.incrementAndGet()));
        CompletionService<Intento> cola = new ExecutorCompletionService<>(grupo);
        try {
            for (String id : datos.franjas.keySet()) {
                enviar(cola, datos, id, 1, fallo, persistente, repeticiones);
            }
            int pendientes = datos.franjas.size();
            while (pendientes > 0) {
                Intento intento = cola.take().get();
                pendientes--;
                ejecucion.intentos.add(intento);
                if (intento.estado == Estado.SIN_COMPROBAR && intento.numero == 1) {
                    enviar(cola, datos, intento.id, 2, fallo, persistente, repeticiones);
                    pendientes++;
                } else {
                    ejecucion.resultados.put(intento.id, intento.estado);
                }
            }
        } finally {
            grupo.shutdownNow();
        }
        return ejecucion;
    }

    private static void mostrar(String titulo, Datos datos, Ejecucion ejecucion) {
        System.out.println("\n=== " + titulo + " ===");
        System.out.println("Franja Intento Trabajador      Inicio(ms)    Fin(ms)    CPU(ms) Resultado");
        List<Intento> linea = new ArrayList<>(ejecucion.intentos);
        linea.sort((a, b) -> Long.compare(a.inicio, b.inicio));
        for (Intento i : linea) {
            String cpu = i.cpu < 0 ? "n/d" : String.format(Locale.ROOT, "%.3f", i.cpu / 1e6);
            System.out.printf(Locale.ROOT, "%-6s %-7d %-14s %10.3f %10.3f %10s %s%n",
                    i.id, i.numero, i.trabajador, (i.inicio - ejecucion.inicio) / 1e6,
                    (i.fin - ejecucion.inicio) / 1e6, cpu,
                    i.estado == Estado.SIN_COMPROBAR ? "FALLO_SIMULADO" : i.estado);
        }
        long ultimoFin = linea.stream().mapToLong(i -> i.fin).max().orElseThrow();
        long ventana = ultimoFin - linea.get(0).inicio;
        long reintentos = linea.stream().filter(i -> i.numero > 1).count();
        if (linea.stream().allMatch(i -> i.cpu >= 0)) {
            long cpuTotal = linea.stream().mapToLong(i -> i.cpu).sum();
            System.out.printf(Locale.ROOT, "Ventana de tareas: %.3f ms | CPU acumulada: %.3f ms%n",
                    ventana / 1e6, cpuTotal / 1e6);
        }
        System.out.printf("Comprobaciones: %d | Reintentos: %d%n", linea.size(), reintentos);
        String recomendada = null;
        for (String id : datos.franjas.keySet()) {
            Estado estado = ejecucion.resultados.get(id);
            Intervalo franja = datos.franjas.get(id);
            System.out.printf("%s %s a %s -> %s%n", id, franja.inicio, franja.fin, estado);
            if (estado == Estado.LIBRE && (recomendada == null
                    || franja.inicio.isBefore(datos.franjas.get(recomendada).inicio))) {
                recomendada = id;
            }
        }
        System.out.println("Primera franja libre verificada: " + (recomendada == null ? "ninguna" : recomendada));
    }

    public static void main(String[] args) {
        try {
            if (args.length < 1 || args.length > 5) {
                throw new IllegalArgumentException("Número de argumentos inválido");
            }
            String modo = args[0];
            if (!Set.of("comparar", "secuencial", "paralelo", "fallo", "fallo-persistente").contains(modo)) {
                throw new IllegalArgumentException("Modo desconocido: " + modo);
            }
            Datos datos = cargar(Path.of(args.length > 1 ? args[1] : "datos/franjas.csv"),
                    Path.of(args.length > 2 ? args[2] : "datos/agenda.csv"));
            String fallo = args.length > 3 ? args[3] : "F3";
            int repeticiones = args.length > 4 ? Integer.parseInt(args[4]) : 1;
            if (modo.equals("comparar")) {
                Ejecucion a = ejecutar(datos, 1, null, false, repeticiones);
                Ejecucion b = ejecutar(datos, 4, null, false, repeticiones);
                Ejecucion c = ejecutar(datos, 4, fallo, false, repeticiones);
                mostrar("A. Secuencial", datos, a);
                mostrar("B. Paralela", datos, b);
                mostrar("C. Paralela con recuperación", datos, c);
                if (!a.resultados.equals(b.resultados) || !a.resultados.equals(c.resultados)) {
                    throw new IllegalStateException("No coinciden los resultados por franja");
                }
                System.out.println("\nA, B y C coinciden por franja. No se escribió en SGRA.");
            } else {
                boolean simular = modo.startsWith("fallo");
                mostrar(modo, datos, ejecutar(datos, modo.equals("secuencial") ? 1 : 4,
                        simular ? fallo : null, modo.equals("fallo-persistente"), repeticiones));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Práctica interrumpida");
            System.exit(1);
        } catch (IOException | RuntimeException | ExecutionException e) {
            System.err.println("No se pudo completar la práctica: " + e.getMessage());
            System.err.println("Uso: bash ejecutar.sh [comparar|secuencial|paralelo|fallo|fallo-persistente]"
                    + " [franjas.csv] [agenda.csv] [idFallo] [repeticiones]");
            System.exit(1);
        }
    }
}
