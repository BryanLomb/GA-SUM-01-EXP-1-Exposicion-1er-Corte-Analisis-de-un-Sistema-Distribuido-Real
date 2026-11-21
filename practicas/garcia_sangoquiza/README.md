# Modelo local de CAP y PACELC aplicado a CLAB

## Propósito y alcance

La demostración usa dos procesos Python y datos sintéticos. Desarrolla dos
ejemplos: venta de la última unidad y reserva del mismo laboratorio por dos
docentes. En el segundo también contrasta una réplica atrasada con una lectura
coordinada para explicar el compromiso entre consistencia y latencia.

Es un **modelo didáctico**, no una implementación distribuida de CLAB. No utiliza
su backend, Spring Boot ni PostgreSQL. La partición se representa impidiendo
que B confirme con el decisor; los canales de control de Python continúan
funcionando. No se corta la red real ni se demuestra experimentalmente el
teorema CAP.

## Requisitos

- Python 3.10 o posterior, con biblioteca estándar y posibilidad de crear procesos.
- Sin dependencias de `pip`, permisos de administrador ni conexión a Internet.
- Comandos compatibles con fish; ejecutar desde la raíz del repositorio.

## Pruebas

```fish
python3 -m unittest discover -s practicas/garcia_sangoquiza -p 'test_*.py' -v
```

Las pruebas comprueban reglas del modelo, intervalos solapados, comportamiento
ante falta de coordinación, actualización de una copia y creación de dos
procesos. No validan el sistema CLAB completo ni una transacción SQL.

## Ejecución

```fish
python3 practicas/garcia_sangoquiza/demo_cap_pacelc.py --caso general --salida evidencias/garcia_sangoquiza/general_01.json
python3 practicas/garcia_sangoquiza/demo_cap_pacelc.py --caso clab --salida evidencias/garcia_sangoquiza/clab_01.json
```

También se puede usar `--caso todos`. Los archivos de salida se crean solo al
usar `--salida`; un archivo existente no se sobrescribe. Para repetir una
ejecución conservando el historial, usar otro nombre, por ejemplo `general_02.json`.

El encabezado registra fecha, usuario, equipo y versión de Python; el JSON añade
el hash del código. Los PID y tiempos corresponden a esa ejecución. Las capturas
y los JSON de una sesión personal deben corresponder a la misma ejecución.

## Interpretación esperada, no resultados medidos

### Ejemplo general

Dos copias reciben stock inicial de una unidad. Al vender independientemente,
ambas pueden aceptar y se produce sobreventa en el modelo. La política
coordinada permite confirmar A y deja B sin confirmar cuando no alcanza al
decisor. Esa falta de confirmación protege la regla global y limita la
disponibilidad de la operación.

Las políticas se comparan sobre los mismos datos iniciales; no son dos fases de
una sola venta real. El orden A/B del coordinador es deliberado y no depende de
qué proceso termine primero.

### Ejemplo CLAB

Ambos procesos consultan una copia inicialmente vacía y consideran libre
LAB-01 de 09:00 a 10:00. Un único coordinador valida y registra las solicitudes:
confirma una y rechaza la otra por cruce. Los intervalos son semiabiertos
`[inicio, fin)`: una reserva que termina a las 10:00 no cruza otra que comienza
a las 10:00. Laboratorios diferentes no compiten por la misma franja.

El coordinador es una función serial del modelo. No representa una transacción
PostgreSQL ya implementada, y copiarla a varios escritores sin protección no
garantizaría atomicidad. La ausencia de confirmación de B durante la partición
se muestra como un escenario alternativo, no como otro intento productivo.

Para PACELC no hay partición: el primario ya tiene una reserva (versión 1), pero
una copia conserva versión 0 y muestra libre. La consulta coordinada obtiene
ocupado; después se actualiza explícitamente la copia. La lectura atrasada
nunca se usa para autorizar una segunda reserva.

## Tiempos, trabajo artificial y paralelismo

- `--trabajo-ms` introduce trabajo CPU artificial por proceso; valor inicial: 150 ms.
- `--retardo-ms` introduce una espera artificial en la lectura coordinada;
  valor inicial: 80 ms. **No es latencia real de red ni una medición de CLAB.**
- `elapsed_ms` es el tiempo observado de esa llamada local, incluida la espera
  configurada. No debe presentarse como un benchmark de replicación.
- Los procesos esperan una señal común. Se registran sus intervalos y tiempo
  CPU. Intervalos superpuestos demuestran concurrencia, no uso de núcleos por sí solos.
- Si la CPU acumulada supera en más de un 5 % la ventana que contiene ambos
  trabajos, se informa de evidencia compatible con uso simultáneo de más de
  una CPU lógica. El margen es conservador; no identifica núcleos físicos.
- Si no se cumple, la salida lo dice. No se debe cambiar o seleccionar la
  medición para fingir paralelismo. La cuota de CPU, la carga y la planificación
  del equipo pueden afectar al resultado.

No se compara aceleración secuencial/paralela: el experimento ilustra
coordinación y estado compartido, no promete que usar procesos sea más rápido.

## Limitaciones

No hay consenso distribuido, elección de líder, replicación por red,
recuperación persistente, transacciones de base de datos ni tolerancia a la
caída del coordinador. La política de protección supone un solo decisor. Las
fuentes teóricas CAP/PACELC y PostgreSQL están en `referencias.bib` y las notas
de la exposición; no convierten estas simplificaciones en funciones de CLAB.
