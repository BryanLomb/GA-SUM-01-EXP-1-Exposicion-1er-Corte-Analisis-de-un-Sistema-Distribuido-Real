# Bitácora del equipo

## Sesión 1: 18 de noviembre de 2026

- Integrantes presentes: Bryan Humberto Lombeida Escaleras.

- Actividades realizadas:
  - Creación del repositorio y del commit inicial con la estructura base del proyecto: `diapositivas_general.tex`, `notas_general.tex`,`referencias.bib`, las carpetas `diapositivas/`, `notas/`, `figuras/` y `evidencias/`, y los archivos de cada integrante.
  - Reorganización del equipo en el orden Giraldo Cagua, Herrera Barco, Garcia Sangoquiza y Lombeida Escaleras: se actualizaron los autores, el orden de inclusión de archivos y la numeración de las diapositivas.
  - Corrección del nombre del archivo principal, que contenía un carácter invisible al inicio.
  - Diseño e implementación de la identidad visual en `identidad.sty`: paleta de colores, estilo de título sin número, viñetas circulares y pie con texto institucional, fuentes y número de diapositiva.
  - Generación de `diapositivas_general.pdf` y verificación de la compilación.

- Problema encontrado: el orden de los integrantes en los archivos iniciales no coincidía con el orden definido, el archivo principal tenía un carácter invisible en su nombre y no existía una identidad visual unificada. Además, la primera versión de la plantilla del pie utilizó una opción no soportada por la versión instalada de Beamer.

- Solución: se renombró el archivo principal, se reordenaron autores, inclusiones y títulos, se creó `identidad.sty` ajustando la plantilla del pie y se generó el PDF verificando las 13 páginas resultantes.

- Tiempo dedicado: 1 hora y media.

## Sesión 2: 19 de noviembre de 2026

- Integrantes presentes: Bryan Humberto Lombeida Escaleras.

- Actividades realizadas:
  - Verificación de los metadatos y de la pertinencia de las fuentes bibliográficas: se confirmaron los tres DOI iniciales (Assiri y Sheneamer, 2025; Mushtaq et al., 2024; Asch et al., 2025) y se seleccionó una cuarta fuente (Chaurasia, Verma y Verma, 2024) para respaldar la detección mediante heartbeats y timeouts.
  - Registro de las cuatro referencias en `referencias.bib` con autores, título, revista, volumen, número, páginas, año y DOI verificados.
  - Ajuste de `identidad.sty` para que el texto de fuentes del pie sea configurable mediante `\fuentespie`, conservando `[PENDIENTE]` como valor por defecto.
  - Redacción de la diapositiva 10, «Tolerancia a fallos y tipos de fallos», con poco texto: definición en una frase, cadena fallo → error → falla y los cuatro tipos de fallos (nodos, comunicación, procesos y recursos).
  - Redacción del guion extenso de la diapositiva 10 en `notas/lombeida_escaleras.tex`.
  - Compilación de `diapositivas_general.pdf` y `notas_general.pdf` con la secuencia `pdflatex → biber → pdflatex → pdflatex`; verificación de las citas [1] y [2], del pie de la diapositiva 10 y de las 13 páginas del PDF.

- Problema encontrado: la redefinición de `\fuentespie` dentro del cuerpo del frame no llegaba a la plantilla del pie, por lo que la diapositiva 10 seguía mostrando `Fuentes: [PENDIENTE]`. Además, el título de la sección de notas desbordaba el margen.

- Solución: se movió la redefinición de `\fuentespie` antes de `\begin{frame}` y se restauró el valor por defecto después de `\end{frame}`; así la diapositiva 10 muestra `Fuentes: [1], [2]` y las demás conservan `[PENDIENTE]`. Se acortó el título de la sección de notas y la distribución de tiempos se trasladó al texto introductorio.

- Tiempo dedicado: 2 horas y media.

## Sesión 3: 19 de noviembre de 2026

- Integrantes presentes: Bryan Humberto Lombeida Escaleras.

- Actividades realizadas:
  - Redacción de la diapositiva 11, «Detección y recuperación ante fallos», con dos bloques de frases breves: detección mediante heartbeats, timeouts y watchdogs; recuperación mediante replicación, reasignación y checkpoint/reinicio.
  - Incorporación de las citas correspondientes y configuración de las fuentes del pie de la diapositiva 11.
  - Redacción del guion y del material de apoyo para preguntas en `notas/lombeida_escaleras.tex`.
  - Revisión del vínculo entre la tolerancia a fallos y el tema principal de computación paralela.
  - Ampliación de las notas de las diapositivas 10 y 11 para explicar cómo el fallo de un trabajador puede impedir completar el resultado conjunto y cómo recuperar sus tareas, con un ejemplo didáctico de procesamiento de cuatro bloques de datos.
  - Aclaración de que el respaldo Groq → validador Java de SGRA ilustra continuidad del servicio, pero no constituye por sí solo evidencia de ejecución paralela.
  - Compilación de ambos PDFs con la secuencia `pdflatex → biber → pdflatex → pdflatex` y verificación del contenido y del pie de la diapositiva 11. Después de ampliar las explicaciones, se actualizó únicamente `notas_general.pdf`, conservando intacto el PDF de las diapositivas.
  - Generación de los auxiliares de compilación en `/tmp/opencode/`, fuera del repositorio.

- Problema encontrado: el contenido inicial explicaba la tolerancia a fallos en sistemas distribuidos de forma general, pero no hacía explícita su relación con la ejecución paralela; además, el caso de respaldo de SGRA podía confundirse con un ejemplo de paralelismo.

- Solución: se mantuvo el texto breve de las diapositivas y se desarrolló la relación con la computación paralela en las notas: trabajadores simultáneos, dependencia del resultado conjunto, detección de fallos y recuperación del trabajo. Se distinguió expresamente el respaldo local de SGRA de la replicación y de la ejecución paralela.

- Tiempo dedicado: 1 hora y media.

## Sesión 4: 19 de noviembre de 2026

- Integrantes presentes: Diego Giraldo Cagua.
- Actividades realizadas:
  - Configuración del entorno en Windows PowerShell con Java 21, Python 3.12.7 y PySpark 4.3.0. Se verificó la instalación ejecutando una prueba local con dos particiones.
  - Desarrollo de una práctica de Apache Spark aplicada al PFC de revisión técnica vehicular (RTV). Se usaron cuatro identificadores ficticios de vehículos para mostrar cómo Spark divide los datos con `parallelize()`, define el procesamiento con `map()` y recupera los resultados con `collect()`.
  - Ejecución de la práctica en modo local (`local[*]`) con dos y cuatro particiones. En Spark UI se revisaron los trabajos, las etapas y las tareas. Se registraron 2/2 y 4/4 tareas completadas en las etapas observadas, así como intervalos de ejecución superpuestos en la línea de tiempo.
  - Registro de dos mediciones por configuración. El tiempo promedio fue de 1,8195 s con dos particiones y de 3,4206 s con cuatro. Se consideró el costo de coordinar más tareas como una posible explicación del aumento de tiempo en esta prueba.
  - Documentación del código, los comandos, los resultados y las capturas en `evidencias/`. Se prepararon las notas de apoyo para explicar la práctica y se registró el uso de ChatGPT como apoyo durante la configuración, la interpretación de los resultados y la documentación.
- Problema encontrado: PowerShell no reconocía inicialmente los comandos `java` y `python`. Además, la sesión de Spark se cerraba al terminar el programa, lo que impedía revisar las tareas en Spark UI.
- Solución: se habilitó Java, se ejecutó Python con `py -3.12` y se configuró `PYSPARK_PYTHON` para que PySpark usara el intérprete correcto. Se dejó el programa esperando una entrada al final para mantener Spark UI disponible. La línea de tiempo se usó como evidencia de concurrencia en un solo equipo.
- Tiempo dedicado: 2 horas.

## Sesión 5: 20 de noviembre de 2026

- Integrantes presentes: Bryan Humberto Lombeida Escaleras.

- Actividades realizadas:
  - Revisión de los pasajes de Assiri y Sheneamer, Mushtaq et al. y Asch et al. para orientar las capturas de lectura: definición de tolerancia a fallos, tipos de fallos, relación fault/error/failure, recuperación y watchdogs.
  - Identificación de las páginas y los fragmentos que respaldan las diapositivas 10 y 11.
  - Redacción de la diapositiva 12, «SGRA: lecciones para la PFC y conclusiones», con poco texto: escenario Groq Cloud → validador Java local, causa raíz por determinar, recomendaciones y conclusión vinculada con la recuperación del trabajo paralelo.
  - Redacción del guion de aproximadamente un minuto y del material de apoyo para preguntas de la diapositiva 12 en `notas/lombeida_escaleras.tex`.
  - Distinción entre el escenario de respaldo descrito en el documento interno de SGRA, las recomendaciones para la PFC y los resultados que requerirían una prueba controlada.
  - Compilación de `diapositivas_general.pdf` y `notas_general.pdf` mediante `pdflatex → biber → pdflatex → pdflatex`; verificación del texto y de las fuentes del pie de la diapositiva 12.
  - Generación de los auxiliares de compilación en `/tmp/opencode/`, fuera del repositorio.

- Problema encontrado: el documento interno describe el respaldo de SGRA, pero no demuestra un incidente ocurrido, su causa raíz ni resultados de una prueba. Presentarlo como un hecho observado podía confundir continuidad del servicio con evidencia de ejecución paralela.

- Solución: se presentó el caso como escenario previsto y se indicó que la causa raíz está por determinar. Las notas distinguen el síntoma de las posibles causas, aclaran que el validador Java no es una réplica de Groq ni demuestra paralelismo y conectan la conclusión con la recuperación de tareas en computación paralela distribuida. La prueba práctica y sus evidencias quedaron para una etapa posterior.

- Tiempo dedicado: 1 hora y media.

## Sesión 6: 21 de noviembre de 2026

- Integrantes presentes: Bryan Humberto Lombeida Escaleras.

- Actividades realizadas:
  - Implementación de una práctica en Java para comprobar cuatro franjas de un refuerzo académico, usando uno y cuatro trabajadores con la misma agenda de prueba.
  - Ejecución de diez comprobaciones automáticas y pruebas de recuperación: reintento exitoso de una franja libre (F3), reintento exitoso de una franja ocupada (F2) y fallo persistente de F3.
  - Verificación de que el frontend y el backend de SGRA se comunican en el entorno local mediante una consulta HTTP sin sesión.
  - Documentación de los comandos, resultados, alcance y evidencias en el README de `evidencias/Bryan Humberto Lombeida Escaleras/Practica/`.
  - Actualización de la diapositiva 12 y su guion para explicar la aplicación al flujo estudiante–docente. Compilación de `diapositivas_general.pdf` y `notas_general.pdf`, con los auxiliares fuera del repositorio.

- Problema encontrado: una comprobación sin resultado podía confundirse con una franja libre. Además, era necesario distinguir el cálculo paralelo de la práctica de la programación de refuerzos que realiza SGRA.

- Solución: se reintenta únicamente la comprobación fallida y se conservan las demás. Si el segundo intento falla, la franja queda sin comprobar. El material de exposición separa el cálculo local en Java de la aceptación de la solicitud en SGRA.

- Tiempo dedicado: 1 hora y media.

## Sesión 7: 21 de noviembre de 2026

- Integrantes presentes: GARCIA SANGOQUIZA.

- Actividades realizadas:
  - Publicación de la comparación de primario PostgreSQL y réplicas asíncronas para CLAB, con sus referencias, notas y PDF actualizados, conservando los avances del equipo.
  - Incorporación de un modelo Python de CAP y PACELC con dos procesos, doce pruebas automatizadas e instrucciones compatibles con fish. El código usa datos sintéticos y no modifica el backend CLAB.
  - Ejecución personal en Arch Linux con Python 3.14.8 de dos casos: venta de la última unidad y reserva de LAB-01 de 09:00 a 10:00. Se conservaron las capturas originales y los JSON de las ejecuciones iniciadas a las 15:56:55 y 15:59:42, respectivamente, en la zona horaria UTC−05:00.
  - Registro de los resultados: dos ventas locales para una unidad sin coordinación; una sola reserva CLAB confirmada y la otra rechazada por cruce; solicitud B sin confirmar al no poder coordinar; copia atrasada libre frente a autoridad ocupada.
  - Publicación de las evidencias en `evidencias/garcia_sangoquiza/` y del código en `practicas/garcia_sangoquiza/`. Ambos JSON contienen el SHA-256 de la misma versión del programa.
  - Integración de las dos capturas como páginas de respaldo con enlaces desde CAP y PACELC y botones de regreso, sin renumerar las doce diapositivas principales. Se publicaron los PDF regenerados y se actualizaron las notas con los resultados personales.

- Problema encontrado: consultar una copia que indica disponibilidad no garantiza que una operación pueda confirmarse sin conflicto. Además, la concurrencia de procesos, el trabajo CPU artificial y la espera introducida para PACELC podían confundirse con rendimiento del backend o con una partición de red real.

- Solución: se separaron consulta y confirmación en el modelo; el coordinador confirma una sola reserva y deja sin confirmar las solicitudes que no pueden coordinar. Las capturas, las notas y la documentación identifican expresamente la partición modelada, los datos sintéticos y los 80 ms de espera impuesta.

- Alcance de los resultados: la CPU acumulada superó la ventana de ejecución en ambos casos, lo que aporta evidencia compatible con uso simultáneo de más de una CPU lógica para el trabajo artificial. No se atribuye aceleración ni latencia medida a CLAB, no se cortó la red y no se implementaron reservas o replicación en su backend.

- Apoyo de IA: la preparación del modelo, sus pruebas y los materiales de explicación recibió asistencia de IA. Las capturas y los JSON corresponden a la ejecución personal registrada.

## Sesión 8: 21 de noviembre de 2026

- Integrantes presentes: Bryan Humberto Lombeida Escaleras.
- Actividades realizadas:
  - Revisión de la consigna de GA-SUM-02 / EXP-1 y del límite de 2 a 4 páginas para las notas.
  - Resumen del guion de las diapositivas 10–12, conservando las citas, los mecanismos de tolerancia a fallos y el alcance de la práctica aplicada a SGRA.
  - Incorporación de mi contribución a la tabla de aportes y de su declaración de uso de IA. Las contribuciones de los demás integrantes se dejaron para su confirmación.
  - Regeneración de `notas_general.pdf` con `pdflatex → biber → pdflatex → pdflatex`. El documento pasó de 6 a 5 páginas, sin cambios en las diapositivas ni en las secciones de los compañeros.
- Problema encontrado: el guion grupal superaba el límite de páginas y faltaba registrar el aporte y el uso de IA.
- Solución: se condensó su sección y se documentaron ambos aportes en las notas generales. El ajuste restante de extensión corresponde a la revisión del documento grupal.
- Tiempo dedicado: 1 hora.


## Sesión 9: 21 de noviembre de 2026

- Integrantes presentes: Aldair Herrera Barco.

- Actividades realizadas:
  - Análisis del código del SSDC para identificar un uso real de computación paralela: la notificación de resultados del comité mediante `enviarCorreosDecision` y `enviarResultadoSeleccion`, ambos marcados con `@Async`.
  - Implementación y ejecución de una práctica en Java (`NotificacionComite.java`) que compara el envío de correos con 1 hilo y con 8 hilos para 16 y 32 postulantes ficticios, incluido un correo que falla a propósito.
  - Documentación de la práctica en `evidencias/Aldair Herrera Barco/` (`comandos.md`, `experimento.md`, `resultados.md` y siete capturas).
  - Elaboración del diagrama `figuras/notificacion_ssdc.tex` en TikZ y actualización de la diapositiva «Espera y avance de tareas» con la tabla de resultados.
  - Incorporación de documentación oficial (Oracle, Angular y BitTorrent) para respaldar sockets, transparencia de ubicación y el modelo híbrido; retiro de la referencia de 2016.
  - Actualización del guion en `notas/herrera_barco.tex` con las citas y la aplicación al SSDC.

- Problema encontrado: el SSDC no puede ejecutarse fuera del equipo de desarrollo sin sus credenciales de base de datos y correo, y un envío real de correos haría variar los tiempos. Además, la aceleración obtenida podía confundirse con uso de CPU.

- Solución: se reprodujo el flujo en un programa independiente con un tiempo de correo simulado. En las notas y en `resultados.md` se aclara que los hilos superponen esperas de comunicación y que la práctica no mide la velocidad real del SSDC.

- Tiempo dedicado: 3 horas.
