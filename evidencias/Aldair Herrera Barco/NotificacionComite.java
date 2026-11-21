import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Practica de Aldair Herrera Barco (GA-SUM-01 / EXP-1).
 *
 * Escenario SSDC: cuando el comite registra su decision, ComiteFinalController
 * llama a enviarCorreosDecision(idSolicitud), que es @Async y recorre a los
 * postulantes llamando a enviarResultadoSeleccion(...), tambien @Async. Cada
 * correo se ejecuta en un hilo del grupo de Spring (8 hilos por defecto).
 *
 * El programa compara el envio con 1 hilo (secuencial) y con 8 hilos
 * (paralelo, como en el SSDC). El correo se SIMULA con una espera fija; no se
 * conecta a SMTP ni al SSDC. Un correo falla a proposito para observar que los
 * demas continuan.
 *
 * Ejecucion (JDK 11 o superior):
 *   java NotificacionComite.java              (16 postulantes, 500 ms por correo)
 *   java NotificacionComite.java 32 500       (32 postulantes, 500 ms por correo)
 */
public class NotificacionComite {

    private static final int POSTULANTE_CON_FALLO = 5;

    public static void main(String[] args) throws Exception {
        int postulantes = args.length > 0 ? Integer.parseInt(args[0]) : 16;
        long msCorreo = args.length > 1 ? Long.parseLong(args[1]) : 500;

        System.out.println("=== SSDC: notificacion de resultados del comite (simulada) ===");
        System.out.printf("Postulantes: %d | Correo simulado: %d ms | Nucleos logicos: %d | Falla a proposito: postulante %d%n%n",
                postulantes, msCorreo, Runtime.getRuntime().availableProcessors(), POSTULANTE_CON_FALLO);

        long t1 = notificar(postulantes, msCorreo, 1);
        long t8 = notificar(postulantes, msCorreo, 8);

        System.out.println("=== Resumen ===");
        System.out.printf("1 hilo  : %d ms%n", t1);
        System.out.printf("8 hilos : %d ms%n", t8);
        System.out.printf("Aceleracion: %.2fx%n", (double) t1 / t8);
    }

    private static long notificar(int postulantes, long msCorreo, int hilos) throws Exception {
        System.out.printf("--- %d hilo(s) ---%n", hilos);
        AtomicInteger numero = new AtomicInteger(1);
        ExecutorService grupo = Executors.newFixedThreadPool(hilos, r -> {
            Thread t = new Thread(r);
            t.setName("task-" + numero.getAndIncrement());
            return t;
        });

        long inicio = System.currentTimeMillis();
        List<Future<?>> envios = new ArrayList<>();
        for (int i = 1; i <= postulantes; i++) {
            int id = i;
            envios.add(grupo.submit(() -> enviarResultado(id, msCorreo, inicio)));
        }
        
        System.out.printf("[%5d ms] [main  ] comite: respuesta 200 OK (los correos siguen en otros hilos)%n",
                System.currentTimeMillis() - inicio);

        int enviados = 0, fallidos = 0;
        for (Future<?> f : envios) {
            try {
                f.get();
                enviados++;
            } catch (Exception e) {
                fallidos++;
            }
        }
        long total = System.currentTimeMillis() - inicio;
        grupo.shutdown();
        System.out.printf("Enviados: %d | Fallidos: %d | Tiempo total: %d ms%n%n", enviados, fallidos, total);
        return total;
    }

    private static void enviarResultado(int id, long msCorreo, long inicio) {
        String hilo = Thread.currentThread().getName();
        try {
            Thread.sleep(msCorreo);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long t = System.currentTimeMillis() - inicio;
        if (id == POSTULANTE_CON_FALLO) {
            System.out.printf("[%5d ms] [%-6s] postulante %2d: ERROR, correo no entregado%n", t, hilo, id);
            throw new IllegalStateException("SMTP no disponible para el postulante " + id);
        }
        System.out.printf("[%5d ms] [%-6s] postulante %2d: correo entregado%n", t, hilo, id);
    }
}