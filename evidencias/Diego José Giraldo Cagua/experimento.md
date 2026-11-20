from pyspark.sql import SparkSession
import time

# 1. Crear la sesión de Spark
spark = SparkSession.builder \
    .master("local[*]") \
    .appName("SistemaRTV") \
    .getOrCreate()

print("Interfaz web de Spark:", spark.sparkContext.uiWebUrl)

# 2. Obtener el contexto de Spark
sc = spark.sparkContext
print("Cores disponibles:", sc.defaultParallelism)

# 3. Simular los vehículos que ingresaron al RTV
vehiculos = [101, 102, 103, 104]

# 4. Distribuir los datos en dos particiones
datos = sc.parallelize(vehiculos, 2)
print("\nINFORMACION DE PARTICIONES")
print("Cantidad de particiones:", datos.getNumPartitions())

# Mostrar cómo se distribuyeron los vehículos
distribucion = datos.mapPartitionsWithIndex(
    lambda indice, elementos: [(indice, list(elementos))]
).collect()

print("\nDISTRIBUCION DE PARTICIONES")

for indice, elementos in distribucion:
    print(f"Particion {indice}: {elementos}")

# 5. Procesar cada vehículo
resultados = datos.map(
    lambda id_vehiculo: f"Vehiculo {id_vehiculo}: procesado"
)

# Medir el tiempo de procesamiento
inicio = time.time()

# 6. Mostrar los resultados
print("RESULTADOS DEL PROCESAMIENTO RTV")

for resultado in resultados.collect():
    print(resultado)

fin = time.time()

print("Tiempo de procesamiento:", round(fin - inicio, 4), "segundos")

# 7. Finalizar Sparks
#spark.stop()

input("Presiona ENTER para finalizar Spark...")
