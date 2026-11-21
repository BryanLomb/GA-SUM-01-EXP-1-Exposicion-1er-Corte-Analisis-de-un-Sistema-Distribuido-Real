# Práctica: comprobación paralela de horarios para un refuerzo en SGRA

**Autor:** Bryan Humberto Lombeida Escaleras.

## Qué se prueba y por qué

El estudiante solicita un refuerzo individual y el docente elige una franja para programar una sesión virtual. La práctica aplica computación paralela a esa elección: un programa Java comprueba cuatro franjas independientes contra la misma copia de los horarios de ambos actores. El programa trabaja con CSV locales, no modifica SGRA y no intervienen otros roles.

Se compara **un trabajador contra cuatro** con las mismas entradas para que la distribución del trabajo sea lo único que cambia entre ambas ejecuciones. Después se simula un fallo en una comprobación: solo esa tarea vuelve a la cola y los resultados de las demás se conservan. Una franja cuyo segundo intento falla queda **sin comprobar**, no libre.

## Cómo reproducirla

Desde la raíz del repositorio, con Bash y un JDK 11 o superior:

```bash
cd practica-Demostracion-explicada-Bryan-Lombeida
java -version
javac -version
bash ejecutar.sh probar
bash ejecutar.sh comparar datos/franjas.csv datos/agenda.csv F3 1000000
bash ejecutar.sh comparar datos/franjas.csv datos/agenda.csv F2 1000000
bash ejecutar.sh fallo-persistente
```

1. **`probar`** verifica las reglas de cruce, el número de trabajadores y el reintento selectivo. Las franjas contiguas no se consideran superpuestas.
2. **Comparación con `F3`**: revisa A (secuencial), B (paralela) y C (paralela con recuperación). Los resultados coinciden; en C solo `F3` tiene un segundo intento y queda libre.
3. **Comparación con `F2`**: repite la prueba sobre una franja ocupada. El segundo intento detecta el cruce del docente. Recuperar la comprobación no significa declarar libre la franja.
4. **`fallo-persistente`**: ambos intentos de `F3` fallan. `F3` queda `SIN_COMPROBAR` y `F4` se convierte en la primera libre verificada.

El argumento `1000000` repite el cálculo de cruce, sin pausas artificiales, para poder observar el solapamiento. No cambia los resultados ni agrega solicitudes. En la captura de `F3`, el bloque paralelo registra **243,332 ms de CPU acumulada en una ventana de 67,680 ms**, evidencia de uso simultáneo de más de un núcleo lógico durante parte de esa ejecución. No es una medición de la velocidad de SGRA.

## Evidencias

| Archivo | Qué demuestra |
|---|---|
| [Comprobación de dependencias](Comprobacion-dependencias.png) | Java y `javac` disponibles; diez comprobaciones automáticas superadas. |
| [Reintento exitoso de F3](Demostracion-reintento-exitoso-F3.png) | Comparación secuencial/paralela y recuperación de una comprobación cuyo resultado es `LIBRE`. |
| [Reintento exitoso de F2](Demostracion-reintento-exitoso-F2.png) | Solo se recupera la comprobación de `F2`; el resultado sigue siendo `CRUCE_DOCENTE`. |
| [Reintento fallido](Demostracion-reintento-fallido.png) | El fallo persistente no se confunde con disponibilidad; se conserva el resultado libre de `F4`. |

## Aplicación al flujo de SGRA

1. En un entorno de prueba, el estudiante envía una solicitud individual y el docente la localiza en estado pendiente.
2. El docente toma las franjas seleccionables y los eventos ocupados de ambos actores. Se ejecuta la búsqueda con una copia de esos datos: cada candidata debe cubrir toda la duración del refuerzo.
3. El docente vuelve a comprobar que la franja elegida sigue disponible y acepta la solicitud en modalidad virtual **una sola vez**. El reintento se hace sobre el cálculo local, no sobre la aceptación.

La causa del fallo de prueba es conocida: se introdujo deliberadamente una señal de error en la comprobación de una franja. No representa una caída de SGRA o de Groq. Tampoco demuestra que el backend compruebe cruces en paralelo ni que impida reservas simultáneas.
