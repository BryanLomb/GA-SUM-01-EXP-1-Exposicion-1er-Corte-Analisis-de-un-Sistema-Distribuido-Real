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
