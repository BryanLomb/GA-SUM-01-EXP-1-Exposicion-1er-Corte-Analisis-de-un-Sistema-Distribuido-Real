# Comandos utilizados en la práctica

Práctica de notificación paralela de resultados del comité (SSDC). Los comandos se ejecutaron en PowerShell desde la raíz del repositorio.

## 1. Entrar a la carpeta de la práctica

```powershell
cd "evidencias\Aldair Herrera Barco"
```

## 2. Comprobar la versión de Java

```powershell
java -version
```

Versión utilizada: Java SE 24.0.1. Se necesita JDK 11 o superior, porque el programa se ejecuta directamente desde el archivo fuente sin compilarlo antes.

## 3. Ejecutar la prueba principal

```powershell
java NotificacionComite.java
```

Usa 16 postulantes y 500 ms por correo simulado. Ejecuta primero la versión con 1 hilo y después la de 8 hilos, e imprime el resumen con la aceleración.

## 4. Repetir la medición

Se ejecutó el mismo comando una segunda vez para obtener dos mediciones por configuración y calcular el promedio.

## 5. Prueba con más postulantes

```powershell
java NotificacionComite.java 32 500
```

El primer argumento es la cantidad de postulantes y el segundo, el tiempo simulado por correo en milisegundos. Esta prueba duplica los postulantes para observar cómo cambia el tiempo de cada versión.