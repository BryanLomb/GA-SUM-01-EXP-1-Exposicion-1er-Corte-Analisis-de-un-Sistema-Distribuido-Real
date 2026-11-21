# Computación paralela aplicada a los PFC

**Enlace al video:** [PENDIENTE: enlace al video]

## Descripción del repositorio

Este repositorio contiene los materiales de la exposición del primer corte del
curso de Aplicaciones Distribuidas. Incluye las diapositivas de la
presentación, las notas o guion de sustentación, la bibliografía y la bitácora
de trabajo del equipo.

La exposición aborda computación paralela mediante ejemplos generales y
aplicaciones a los proyectos de fin de curso (PFC) de los integrantes. Las
secciones distinguen los ejemplos didácticos, las propuestas de diseño y las
implementaciones o ejecuciones documentadas.

El material está en desarrollo. Los marcadores `[PENDIENTE: …]` deben
sustituirse por contenido verificado antes de la entrega.

### Estructura

| Ruta | Contenido |
|---|---|
| `diapositivas_general.tex` | Archivo principal de la presentación en Beamer (16:9). Carga `identidad.sty` e incluye los archivos de `diapositivas/`. |
| `notas_general.tex` | Archivo principal del guion de sustentación en `article`. Incluye los archivos de `notas/`. |
| `diapositivas/` | Un archivo `.tex` por integrante con sus diapositivas. |
| `notas/` | Un archivo `.tex` por integrante con su guion. |
| `identidad.sty` | Identidad visual: paleta de colores, estilo de títulos y pie de diapositiva. |
| `referencias.bib` | Bibliografía en formato BibTeX con las fuentes citadas y de consulta. |
| `figuras/` | Imágenes utilizadas por las diapositivas. |
| `evidencias/` | Evidencias del trabajo del equipo. |
| `BITACORA.md` | Registro de las sesiones de trabajo. |
| `diapositivas_general.pdf` | PDF ya generado de la presentación. |
| `notas_general.pdf` | PDF compilado de las notas. |

## Compilación

### Compilador y dependencias

- **Compilador:** `pdflatex` (TeX Live).
- **Motor bibliográfico:** `biber`.
- **Archivos principales:** `diapositivas_general.tex` y `notas_general.tex`.
- **Paquetes requeridos:** `beamer`, `babel` (opción `spanish`), `graphicx`,
  `geometry`, `csquotes`, `booktabs`, `tikz` con la biblioteca `arrows.meta`,
  `hyperref`, `biblatex` con `backend=biber` y `style=ieee`.
- **Archivo local:** `identidad.sty` debe estar en la raíz del repositorio.

### Orden de comandos

La secuencia es `pdflatex → biber → pdflatex → pdflatex`. Se ejecuta desde la
raíz del repositorio.

Para las diapositivas:

```bash
pdflatex diapositivas_general.tex
biber diapositivas_general
pdflatex diapositivas_general.tex
pdflatex diapositivas_general.tex
```

Para las notas:

```bash
pdflatex notas_general.tex
biber notas_general
pdflatex notas_general.tex
pdflatex notas_general.tex
```

## Cómo hacer los PDFs

1. Ubicarse en la raíz del repositorio.
2. Ejecutar la secuencia de comandos anterior para el documento deseado.
3. Los resultados son `diapositivas_general.pdf` y `notas_general.pdf`.

Si se desea evitar que los archivos auxiliares `.aux`, `.bbl`, `.log`, `.nav` y
`.out` queden en el proyecto, puede compilarse en otro directorio:

```bash
mkdir -p /tmp/opencode/build
pdflatex -output-directory=/tmp/opencode/build diapositivas_general.tex
biber --output-directory=/tmp/opencode/build diapositivas_general
pdflatex -output-directory=/tmp/opencode/build diapositivas_general.tex
pdflatex -output-directory=/tmp/opencode/build diapositivas_general.tex
cp /tmp/opencode/build/diapositivas_general.pdf .
```

La bibliografía se genera con `biber` a partir de `referencias.bib`. Si se
agregan o modifican fuentes, debe repetirse la secuencia completa para
actualizar las citas y la lista final.
