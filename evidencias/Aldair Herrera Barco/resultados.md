# Resultados de la práctica

## 1. Objetivo

Comprobar cómo cambia el tiempo de notificación de los resultados del comité del SSDC al pasar de 1 hilo a 8 hilos, y observar qué ocurre cuando falla uno de los correos.

## 2. Entorno de ejecución

| Elemento | Versión o configuración |
|---|---|
| Sistema operativo | Windows 11 |
| Java | Java SE 24.0.1 (HotSpot 64-Bit Server VM, build 24.0.1+9-30) |
| Núcleos lógicos | 8 |
| Postulantes | 16 (y 32 en la prueba adicional) |
| Tiempo simulado por correo | 500 ms |

## 3. Mediciones

| Hilos | Enviados | Fallidos | Ejecución 1 | Ejecución 2 | Promedio |
|---|---|---|---|---|---|
| 1 | 15 | 1 | 8079 ms | 8049 ms | 8064 ms |
| 8 | 15 | 1 | 1020 ms | 1051 ms | 1035,5 ms |

Aceleración promedio (1 hilo / 8 hilos): 7,79×.

Prueba con 32 postulantes (una ejecución): 1 hilo 16109 ms, 8 hilos 2018 ms, aceleración 7,98×.

## 4. Observaciones

- En todas las ejecuciones, el comité recibió su respuesta entre 2 y 8 ms después de iniciar, porque los correos siguen en otros hilos. Esto corresponde a la comunicación asincrónica.
- Con 1 hilo, todos los correos se enviaron en `task-1`, uno tras otro, cada unos 500 ms. El tiempo total fue cercano a 16 × 500 ms.
- Con 8 hilos, los correos se repartieron entre `task-1` y `task-8` y se entregaron en dos bloques, cerca de los 500 ms y de los 1000 ms. El tiempo total fue cercano a 2 × 500 ms.
- Al duplicar los postulantes a 32, el tiempo con 1 hilo también se duplicó (16109 ms), mientras que con 8 hilos subió a 2018 ms (cuatro bloques de ocho).
- El correo del postulante 5 falló en todas las ejecuciones, pero los demás se entregaron. El comité no se enteró del fallo en su respuesta: el error quedó en el hilo del correo.

## 5. Interpretación

Los hilos pasan casi todo el tiempo esperando la respuesta simulada del servidor de correo. Por eso la aceleración se acerca a 8: lo que se superpone son las esperas de comunicación, no el cálculo. La práctica muestra que varias tareas de comunicación avanzan a la vez; no mide el uso de CPU ni la velocidad real del SSDC.

En el código del SSDC, el bucle de `enviarCorreosDecision` tiene un `try/catch` alrededor de cada envío. Como `enviarResultadoSeleccion` también es `@Async`, el error ocurre en otro hilo y ese `try/catch` no lo atrapa. Es un costo de la asincronía: el fallo queda oculto para quien hizo la solicitud.

## 6. Conclusiones

1. El SSDC ya envía en paralelo los correos de resultados mediante `@Async`.
2. Con 8 hilos, el tiempo promedio de notificación de 16 postulantes bajó de 8064 ms a 1035,5 ms, una aceleración de 7,79×.
3. La comunicación asincrónica permite responder al comité sin esperar los correos.
4. El fallo de un correo no detiene a los demás, pero tampoco llega a la respuesta del comité.

## 7. Alcance y limitaciones

Simulación didáctica con postulantes ficticios y un tiempo de correo fijo. No se ejecutó el SSDC, no se envió ningún correo real y no se usó un clúster. Las mediciones dependen del equipo en que se ejecuta.

## Capturas

| Archivo | Contenido |
|---|---|
| `1.- Version de Java.png` | Salida de `java -version`. |
| `2.- Ejecucion con 1 hilo.png` | Envío secuencial y su tiempo total. |
| `3.- Ejecucion con 8 hilos.png` | Envío con `task-1` a `task-8`, fallo del postulante 5 y resumen. |
| `4.- Segunda ejecucion.png` | Resumen de la segunda medición. |
| `5.- Prueba con 32 postulantes.png` | Resumen con 32 postulantes. |
| `6.- Codigo SSDC enviarCorreosDecision.png` | `@Async` y bucle en `ComiteFinalService`. |
| `7.- Codigo SSDC enviarResultadoSeleccion.png` | `@Async` en `EmailService`. |