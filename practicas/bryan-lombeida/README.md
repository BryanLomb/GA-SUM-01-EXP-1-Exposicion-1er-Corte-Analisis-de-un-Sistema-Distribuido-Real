# Práctica de Bryan Lombeida — Programar un refuerzo sin cruces

**Actores:** estudiante y docente. El estudiante solicita un refuerzo individual; el docente comprueba horarios y acepta la solicitud para programar una sesión virtual. No intervienen otros roles, espacios físicos ni Groq.

La práctica Java es **de solo lectura sobre archivos locales**. Hace la búsqueda secuencial y paralela, y recupera una comprobación ante un fallo simulado. La aceptación se hace después, una sola vez, en **SGRA de prueba**. El programa no se conecta a SGRA ni consulta agendas automáticamente.

## 1. Preparar la terminal

Desde la raíz del repositorio:

```bash
cd practica-Demostracion-explicada-Bryan-Lombeida
java -version
javac -version
```

Necesitas Bash y un **JDK 11 o superior**. Si funciona `java` pero no existe `javac`, falta el JDK en tu `PATH`. La práctica no requiere Maven, Docker, base de datos, internet ni credenciales. El backend de SGRA, si decides levantarlo aparte, tiene sus propios requisitos y usa Java 21.

## 2. Ejecutar y comprobar la práctica

```bash
bash ejecutar.sh probar
bash ejecutar.sh comparar
bash ejecutar.sh fallo-persistente
```

1. `probar` compila y ejecuta las pruebas automáticas. Deben aparecer diez líneas `OK`. Se comprueban los cruces, las franjas contiguas, el número de trabajadores, el reintento selectivo y el fallo persistente.
2. `comparar` ejecuta tres escenarios con los mismos archivos: **A** con un trabajador, **B** con cuatro trabajadores y **C** con cuatro trabajadores y un fallo simulado en el primer intento de `F3`. En C aparecen cinco intentos en total: solo `F3` tiene un segundo intento. El resumen confirma que las tres ejecuciones llegaron al mismo resultado por franja.
3. `fallo-persistente` hace fallar ambos intentos de `F3`: queda `SIN_COMPROBAR`, nunca `LIBRE`. La primera franja libre verificada pasa a ser `F4`. Esto es una comprobación de seguridad del programa, no un fallo real de SGRA.

Con los CSV incluidos, los resultados de A, B y C son:

| Franja | Horario local | Resultado |
|---|---|---|
| `F1` | 09:00–09:30 | `CRUCE_ESTUDIANTE` |
| `F2` | 09:30–10:00 | `CRUCE_DOCENTE` |
| `F3` | 10:00–10:30 | `LIBRE` (primera franja libre verificada) |
| `F4` | 10:30–11:00 | `LIBRE` |

Un evento que termina a las 10:00 no se cruza con otro que empieza a las 10:00. El reintento vuelve a poner solo la comprobación fallida en la cola; puede tomarla el mismo hilo u otro. No se simula la caída de un proceso ni se reintenta una escritura.

### Mostrar que hubo cómputo en paralelo

Con dos eventos en la agenda, cada comprobación puede terminar antes de que empiece la siguiente, incluso usando cuatro trabajadores. Para la exposición, amplifica el trabajo real de comparación sin añadir pausas:

```bash
bash ejecutar.sh comparar datos/franjas.csv datos/agenda.csv F3 1000000
```

El último argumento repite el mismo cálculo un millón de veces dentro de cada intento que llega a comprobar la franja. No crea más solicitudes ni cambia el resultado. En la tabla, compara `Inicio(ms)` y `Fin(ms)` de los trabajadores de B. Si en esa ejecución la **CPU acumulada** de los hilos supera claramente la duración de la **ventana de tareas**, se consumió más de un núcleo lógico a la vez en parte de la ventana. Si no ocurre, reporta solo lo que muestran los registros. No uses esta prueba como benchmark de aceleración.

## 3. Probar el flujo real de SGRA

Para este paso necesitas una **instancia de prueba ya operativa**, una cuenta de estudiante inscrita y una cuenta de docente asignada a la misma asignatura. Usa datos ficticios y no uses producción. Si no tienes una instancia con esos datos, puedes completar los pasos de terminal, pero la programación real queda pendiente.

1. **Como estudiante**, abre la opción de solicitar un refuerzo. Elige una asignatura con docente asignado, escribe un motivo de prueba y envía una solicitud individual. Anota su identificador si la interfaz lo muestra, o un motivo único para reconocerla.
2. **Como docente**, localiza la misma solicitud en estado pendiente. Antes de aceptarla, apunta cuatro franjas futuras que SGRA permita seleccionar, y los eventos conocidos de ambos actores. Procura tener un cruce para cada actor y al menos dos franjas libres. Si no puedes crear esos cruces con cuentas de prueba, usa la salida de los CSV incluidos para mostrar los cruces y documenta aparte qué agenda se verificó en la instancia.
3. **Adapta los datos si es necesario.** Desde la carpeta de la práctica, copia los ejemplos y edita las copias:

   ```bash
   cp datos/franjas.csv datos/franjas-sgra.csv
   cp datos/agenda.csv datos/agenda-sgra.csv
   nvim datos/franjas-sgra.csv
   nvim datos/agenda-sgra.csv
   bash ejecutar.sh comparar datos/franjas-sgra.csv datos/agenda-sgra.csv F3
   ```

   Conserva los encabezados. En `franjas-sgra.csv` pon una fila por candidata con `id,inicio,fin`; en `agenda-sgra.csv` pon una fila por evento ocupado con `actor,inicio,fin`. Usa solamente `docente` o `estudiante` para el actor y fechas en formato `AAAA-MM-DDTHH:MM`, con la misma hora local. Cada franja debe cubrir **toda la duración** que vas a registrar al aceptar, no solo la hora de inicio. Si cambias los IDs, sustituye `F3` en el comando por el ID de la franja cuyo fallo quieres simular. Usa `probar` con los archivos originales sin modificar.
4. **Revisa que el horario siga libre y acepta una sola vez.** En SGRA selecciona la fecha y franja que acabas de verificar, modalidad virtual y la duración correspondiente. No selecciones espacio físico. Confirma la aceptación una vez. Si la interfaz falla o no confirma, revisa el estado antes de volver a pulsar: la búsqueda local no garantiza que las agendas sigan iguales ni evita reservas concurrentes.
5. **Verifica el resultado.** Recarga la vista del docente y comprueba que la solicitud ya no esté pendiente y que la sesión tenga la fecha, franja y modalidad correctas. Si la interfaz del estudiante muestra el estado, compruébalo también. No confundas la solicitud aceptada con una sesión ya impartida.

La operación del backend es `POST /api/student/requests` para enviar la solicitud y `PUT /api/teacher/requests/{requestId}/accept` para aceptarla y programarla. Se usa la interfaz para no pedir tokens, IDs de catálogos ni otros datos de configuración. La búsqueda local **no** implica que la API controle cruces automáticamente ni que proteja contra reservas simultáneas.

## 4. Guardar evidencias

Desde la carpeta de la práctica:

```bash
set -o pipefail
destino="../evidencias/Bryan Humberto Lombeida Escaleras/Practica"
mkdir -p "$destino"
bash ejecutar.sh probar | tee "$destino/01-pruebas.txt"
bash ejecutar.sh comparar datos/franjas.csv datos/agenda.csv F3 1000000 | tee "$destino/02-comparacion.txt"
bash ejecutar.sh fallo-persistente | tee "$destino/03-fallo-persistente.txt"
```

**Verificado localmente:** las diez comprobaciones pasaron con Java 11; A, B y C coincidieron, solo se reintentó `F3` y el fallo persistente dejó `F3` sin comprobar. Se observó cómputo en paralelo al amplificar la carga. Consulta los [resultados y evidencias](../evidencias/Bryan%20Humberto%20Lombeida%20Escaleras/Practica/README.md).
