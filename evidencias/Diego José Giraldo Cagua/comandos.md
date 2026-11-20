Comandos de PowerShell utilizados para preparar y ejecutar Apache Spark
Este documento reúne los comandos utilizados durante la práctica de
Apache Spark con PySpark en Windows PowerShell.
1. Comprobar las versiones instaladas
java -version
py -3.12 --version
Java utilizado: Eclipse Temurin JDK 21.0.13 LTS.
Python utilizado: 3.12.7 (64 bits).
En este equipo se utilizó py -3.12 para ejecutar Python 3.12 porque
el comando python no estaba disponible directamente en el PATH.

2. Instalar PySpark
py -3.12 -m pip install pyspark
La instalación utilizada fue PySpark 4.3.0, junto con Py4J 0.10.9.9.
3. Configurar el ejecutable de Python para PySpark
En la sesión de PowerShell se estableció esta variable de entorno:
$env:PYSPARK_PYTHON = "C:\Users\diego\AppData\Local\Programs\Python\Python312\python.exe"
Esta configuración indica a PySpark qué ejecutable de Python debe
utilizar. Al estar definida mediante $env:, se aplica a la sesión
actual de PowerShell.
4. Comprobar que PySpark funciona
Se ejecutó una prueba rápida desde PowerShell:
py -3.12 -c "from pyspark.sql import SparkSession; spark=SparkSession.builder.master('local[*]').appName('PruebaRTV').getOrCreate(); print('Version Spark:',spark.version); print('Resultado:',sc.parallelize([1,2,3,4]).map(lambda x:x*2).collect()); spark.stop()"
Si se desea ejecutar exactamente esa prueba, debe definirse sc antes
de utilizarlo. La versión corregida y autocontenida es:
py -3.12 -c "from pyspark.sql import SparkSession; spark=SparkSession.builder.master('local[*]').appName('PruebaRTV').getOrCreate(); sc=spark.sparkContext; print('Version Spark:',spark.version); print('Resultado:',sc.parallelize([1,2,3,4]).map(lambda x:x*2).collect()); spark.stop()"
Resultado obtenido:
Version Spark: 4.3.0
Resultado: [2, 4, 6, 8]
5. Ejecutar el programa de práctica
Desde la carpeta del proyecto C:\Users\diego\practica-spark, se
ejecutó:
py -3.12 prueba_spark.py
El programa utiliza Spark en modo local mediante local[*].
6. Mantener Spark activo para consultar la interfaz web
Para que la interfaz web de Spark permanezca disponible mientras se
revisan los resultados, el programa se dejó esperando una entrada en la
terminal:
input("Presiona ENTER para finalizar Spark...")
Mientras el programa permanece esperando, se puede abrir la interfaz en
el navegador:
http://localhost:4040
Al terminar la revisión, se presiona Enter en la terminal para
finalizar la ejecución.
7. Notas
- py -3.12 -m pip permite utilizar pip a través del lanzador de
  Python, aunque el comando pip no esté disponible directamente.
- La variable Env: Pyspark Python debe configurarse nuevamente si se
  abre una nueva sesión de PowerShell, salvo que se configure de forma
  permanente en Windows.
- Los avisos relacionados con winutils.exe o NativeCodeLoader
  aparecieron durante la práctica, pero no impidieron que Spark
  ejecutara las pruebas.
- La interfaz de Spark en el puerto 4040 está disponible mientras la
  aplicación correspondiente sigue activa.
