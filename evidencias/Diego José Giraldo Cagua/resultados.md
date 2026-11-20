Resultados de la práctica de Apache Spark aplicada al escenario RTV
1. Objetivo
Realizar una práctica introductoria con Apache Spark y PySpark para
observar cómo se distribuyen datos en particiones, cómo se ejecutan
tareas y cómo cambia el tiempo registrado al variar la cantidad de
particiones.

Se utilizaron cuatro identificadores ficticios de vehículos:
vehiculos = [101, 102, 103, 104]
Los datos son únicamente de prueba y no corresponden a registros reales
de un centro de revisión técnica vehicular.
2. Entorno de ejecución
  Elemento                      Versión o configuración
  Sistema operativo             Windows 11
Java                          Eclipse Temurin JDK 21.0.13 LTS
Python                        3.12.7 (64 bits)
Apache Spark / PySpark        4.3.0
Py4J                          0.10.9.9
Modo de ejecución             local[*]
Cores mostrados en Spark UI   12
Aplicación                    SistemaRTV

La aplicación se ejecutó en modo local. Por tanto, esta práctica no
utilizó un clúster de varias máquinas.
3. Distribución de los vehículos en particiones
Se utilizó sc.parallelize() para crear un RDD a partir de la lista de
vehículos y distribuir sus elementos en particiones.
Prueba A: dos particiones
Código utilizado:
datos = sc.parallelize(vehiculos, 2)
Distribución observada:
Particion 0: [101, 102]
Particion 1: [103, 104]
Cantidad de particiones informada por datos.getNumPartitions():
Cantidad de particiones: 2
En el Stage observado se completaron dos tareas:
2/2 tasks succeeded
Prueba B: cuatro particiones
Código utilizado:
datos = sc.parallelize(vehiculos, 4)
Distribución observada:
Particion 0: [101]
Particion 1: [102]
Particion 2: [103]
Particion 3: [104]
Cantidad de particiones informada por datos.getNumPartitions():
Cantidad de particiones: 4
En el Stage observado se completaron cuatro tareas:
4/4 tasks succeeded
4. Procesamiento de los vehículos
Se aplicó una transformación map() para generar un mensaje por
vehículo:
resultados = datos.map(
    lambda id_vehiculo: f"Vehiculo {id_vehiculo}: procesado"
)
Después se utilizó collect() para ejecutar la acción y recuperar los
resultados en el programa principal.

Salida observada:
Vehiculo 101: procesado
Vehiculo 102: procesado
Vehiculo 103: procesado
Vehiculo 104: procesado
map() es una transformación perezosa: por sí sola no ejecuta el
procesamiento. La acción collect() provoca la ejecución de las
operaciones pendientes.
5. Observaciones en Spark UI
Durante la práctica se revisaron las pestañas Jobs, Stages y Executors.
Jobs y Stages
- Se observaron dos Jobs asociados a las acciones que consultaban la
  distribución de particiones y los resultados del procesamiento.
- Los Jobs observados finalizaron con estado SUCCEEDED.
- En el Stage de la prueba con dos particiones se observaron dos
  tareas completadas.
- En el Stage de la prueba con cuatro particiones se observaron cuatro
  tareas completadas.
Event Timeline
En la prueba con cuatro particiones, las barras de las tareas comenzaron
aproximadamente al mismo tiempo y sus intervalos se superpusieron.

Esto constituye una observación de concurrencia en la ejecución. La
superposición de las barras, por sí sola, no demuestra que cada tarea se
ejecutara simultáneamente en un core físico distinto.
Executors
En modo local, Spark UI mostró un executor identificado como driver,
con 12 cores disponibles. El valor de sc.defaultParallelism también
fue 12 en este entorno.

Este dato describe la configuración de paralelismo predeterminada de
Spark en la ejecución local; no debe interpretarse por sí solo como una
medición independiente del uso físico de cada core.
6. Medición del tiempo
Se utilizó el módulo time de Python para registrar el tiempo alrededor
de la transformación y la acción collect().

Fragmento utilizado:
inicio = time.time()

resultados = datos.map(
    lambda id_vehiculo: f"Vehiculo {id_vehiculo}: procesado"
)

for resultado in resultados.collect():
    print(resultado)

fin = time.time()

print("Tiempo de procesamiento:", round(fin - inicio, 4), "segundos")
Resultados registrados
    Cantidad de Primera ejecución Segunda ejecución          Promedio
    particiones                                     

              2          1.8197 s          1.8192 s         1.81945 s

              4          3.4779 s          3.3633 s         3.42060 s

Promedios redondeados a cuatro decimales:
- Dos particiones: 1.8195 segundos.
- Cuatro particiones: 3.4206 segundos.
En estas mediciones, la configuración de cuatro particiones tardó
aproximadamente un 88 % más que la de dos particiones.
7. Interpretación de los resultados
En esta prueba pequeña, utilizar más particiones no redujo el tiempo
registrado. Una explicación posible es que el conjunto de datos contiene
solamente cuatro elementos y que la planificación y coordinación de más
tareas representa un costo considerable en relación con el trabajo que
debe realizarse.

Este resultado no significa que dos particiones sean siempre mejores que
cuatro, ni que Spark sea más lento al aumentar las particiones. El
rendimiento depende del tamaño de los datos, el tipo de procesamiento,
los recursos disponibles y los costos de coordinación.

Además, las mediciones corresponden a una prueba pequeña en modo local.
No constituyen un benchmark general de Apache Spark. El tiempo medido
rodea la transformación y la acción collect(), pero no incluye la
creación inicial de la sesión Spark ni la acción anterior utilizada para
mostrar la distribución de particiones.
8. Conclusiones
1. Se logró instalar y ejecutar PySpark 4.3.0 utilizando Python 3.12.7
   y Java 21.
2. Se comprobó que parallelize() permite distribuir una colección en
   una cantidad definida de particiones.
3. En el Stage observado, al pasar de dos a cuatro particiones, las
   tareas completadas pasaron de 2/2 a 4/4.
4. Spark UI permitió observar Jobs, Stages, Tasks, el Event Timeline y
   el executor local.
5. Las barras superpuestas del Event Timeline permitieron identificar
   intervalos de ejecución concurrentes, sin afirmar por ello que se
   verificó el uso simultáneo de cores físicos distintos.
6. En esta prueba de cuatro vehículos, dos particiones obtuvieron un
   menor tiempo promedio que cuatro particiones.
7. Se comprobó que aumentar la cantidad de particiones no garantiza una
   mejora del rendimiento.
9. Alcance y limitaciones
Esta práctica fue una simulación didáctica de procesamiento de
identificadores de vehículos. No implementó un sistema real de revisión
técnica vehicular, no utilizó datos productivos y no ejecutó Spark sobre
un clúster distribuido de varias máquinas.

Las etapas de inspección visual, mecánica y de gases pertenecen al flujo
funcional del escenario RTV; no se modelaron aquí como hilos, procesos
ni componentes distribuidos.
