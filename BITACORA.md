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
