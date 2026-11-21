# Experimento: notificación paralela de resultados del comité

## Escenario real en el SSDC

Cuando el comité registra su decisión sobre una solicitud docente, `ComiteFinalController` llama a `ComiteFinalService.enviarCorreosDecision(idSolicitud)`. Ese método está marcado con `@Async`, así que el controlador responde al comité sin esperar los correos.

Dentro de ese método se recorre la lista de postulantes y, para cada uno, se llama a `EmailService.enviarResultadoSeleccion(...)`, que también es `@Async`. Spring ejecuta cada correo en un hilo de su grupo de tareas. El SSDC habilita esta ejecución con `@EnableAsync` en `BackendApplication` y no configura otro grupo de hilos, por lo que se usa el de Spring Boot, con 8 hilos por defecto.

## Programa de la práctica

`NotificacionComite.java` reproduce ese flujo fuera del SSDC:

1. Crea un grupo de hilos con nombres `task-1`, `task-2`, etc., como los hilos `@Async` de Spring.
2. Entrega un envío por postulante al grupo y responde al comité de inmediato.
3. Cada envío simula el tiempo del servidor de correo con una espera fija.
4. El postulante 5 falla a propósito para observar qué pasa con los demás.
5. Repite el proceso con 1 hilo (secuencial) y con 8 hilos (como en el SSDC) y muestra la aceleración.

El programa no se conecta al SSDC, a la base de datos ni a un servidor SMTP. Los postulantes son ficticios.

## Fragmento principal

```java
ExecutorService grupo = Executors.newFixedThreadPool(hilos, ...);
for (int i = 1; i <= postulantes; i++) {
    int id = i;
    envios.add(grupo.submit(() -> enviarResultado(id, msCorreo, inicio)));
}
// el comité ya recibió su respuesta; los correos siguen en otros hilos
```

## Correspondencia con el SSDC

| SSDC                                     | Práctica                                                        |
| ---------------------------------------- | --------------------------------------------------------------- |
| `enviarCorreosDecision` con `@Async`     | El comité recibe su respuesta antes de que terminen los correos |
| `enviarResultadoSeleccion` con `@Async`  | Cada correo se entrega al grupo de hilos con `submit`           |
| Grupo de tareas de Spring Boot (8 hilos) | `Executors.newFixedThreadPool(8)`                               |
| Envío por SMTP                           | Espera simulada de 500 ms                                       |
