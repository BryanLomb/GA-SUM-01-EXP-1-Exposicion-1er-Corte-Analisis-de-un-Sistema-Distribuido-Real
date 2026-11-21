import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

public final class Pruebas {
    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
        System.out.println("OK: " + mensaje);
    }

    private static Map<String, Long> intentos(PracticaHorarios.Ejecucion ejecucion) {
        return ejecucion.intentos.stream().collect(Collectors.groupingBy(i -> i.id, Collectors.counting()));
    }

    public static void main(String[] args) throws Exception {
        var datos = PracticaHorarios.cargar(Path.of("datos/franjas.csv"), Path.of("datos/agenda.csv"));
        var secuencial = PracticaHorarios.ejecutar(datos, 1, null, false, 1);
        var paralela = PracticaHorarios.ejecutar(datos, 4, null, false, 1);
        var recuperada = PracticaHorarios.ejecutar(datos, 4, "F3", false, 1);
        var persistente = PracticaHorarios.ejecutar(datos, 4, "F3", true, 1);
        var esperado = Map.of("F1", PracticaHorarios.Estado.CRUCE_ESTUDIANTE,
                              "F2", PracticaHorarios.Estado.CRUCE_DOCENTE,
                              "F3", PracticaHorarios.Estado.LIBRE,
                              "F4", PracticaHorarios.Estado.LIBRE);
        verificar(secuencial.resultados.equals(esperado), "La referencia coincide con los cuatro casos esperados");
        verificar(paralela.resultados.equals(esperado), "La ejecución paralela conserva los resultados");
        verificar(secuencial.intentos.stream().map(i -> i.trabajador).distinct().count() == 1
                && paralela.intentos.stream().map(i -> i.trabajador).distinct().count() == 4,
                "La ejecución usa uno y cuatro trabajadores, respectivamente");
        verificar(recuperada.resultados.equals(esperado), "La recuperación devuelve el resultado correcto de F3");
        var esperadosConFallo = Map.of("F1", 1L, "F2", 1L, "F3", 2L, "F4", 1L);
        verificar(intentos(recuperada).equals(esperadosConFallo)
                && intentos(persistente).equals(esperadosConFallo),
                "Solo se reintenta F3, una vez y sin repetir las demás");
        verificar(persistente.resultados.get("F3") == PracticaHorarios.Estado.SIN_COMPROBAR
                && persistente.resultados.get("F4") == PracticaHorarios.Estado.LIBRE,
                "Un fallo persistente no convierte F3 en libre ni afecta F4");
        var conCruceRecuperada = PracticaHorarios.ejecutar(datos, 4, "F1", false, 1);
        verificar(conCruceRecuperada.resultados.get("F1") == PracticaHorarios.Estado.CRUCE_ESTUDIANTE,
                "Recuperar una comprobación no convierte una franja ocupada en libre");
        intervaloPruebas();
    }

    private static void intervaloPruebas() {
        var anterior = new PracticaHorarios.Intervalo("2027-01-19T09:30", "2027-01-19T10:00");
        var siguiente = new PracticaHorarios.Intervalo("2027-01-19T10:00", "2027-01-19T10:30");
        var solapado = new PracticaHorarios.Intervalo("2027-01-19T09:59", "2027-01-19T10:01");
        verificar(!anterior.cruza(siguiente) && !siguiente.cruza(anterior),
                "Las franjas contiguas no se solapan");
        verificar(anterior.cruza(solapado) && siguiente.cruza(solapado),
                "Un evento que cruza el límite se detecta en ambas franjas");
        try {
            new PracticaHorarios.Intervalo("2027-01-19T10:00", "2027-01-19T10:00");
            throw new AssertionError("Aceptó una franja de duración cero");
        } catch (IllegalArgumentException esperado) {
            verificar(true, "Rechaza franjas sin duración");
        }
    }
}
