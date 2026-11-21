# Ejecuciones personales: modelos CAP y PACELC

Los PNG son capturas originales de terminal y los JSON son los registros de las
mismas ejecuciones del 21 de noviembre de 2026, zona horaria UTC−05:00. La
terminal y los registros identifican usuario `roger`, equipo `omarchy` y Python
3.14.8. Se conservaron los archivos originales sin modificar sus resultados.

| Caso | Captura | Registro | Hora de inicio | PID |
|---|---|---|---|---|
| General: última unidad | `01_cap_general.png` | `general_01.json` | 15:56:55 | 45396, 45397 |
| CLAB: LAB-01, 09:00–10:00 | `02_clab_cap_pacelc.png` | `clab_01.json` | 15:59:42 | 47335, 47336 |

Código: `practicas/garcia_sangoquiza/demo_cap_pacelc.py`.
SHA-256 registrado en ambos JSON:

```text
2f3fc488a6f136b6405f7e11df5e2f459618bf8f65accf1334e76155fe96829d
```

## Resultados y alcance

- General: dos ventas locales para una unidad cuando no hay coordinación. Con
  la política coordinada, A confirma y B queda sin confirmar durante la
  partición modelada.
- CLAB: ambas consultas consideran libre la misma franja; el coordinador
  confirma una reserva y rechaza la otra por cruce. Si B no puede coordinar,
  su solicitud no se confirma.
- Sin partición, la copia atrasada versión 0 indica libre y la autoridad
  versión 1 indica ocupado. La lectura coordinada tarda 80,120736 ms, incluidos
  80 ms de espera artificial. No es una medida de latencia del backend.
- CPU acumulada/ventana: general 300,121243/150,649349 ms; CLAB
  300,064212/150,333385 ms. La medición es compatible con uso simultáneo de más
  de una CPU lógica para el trabajo artificial; no acredita uso de núcleos
  físicos concretos ni aceleración de CLAB.

La demostración usa datos sintéticos y dos procesos locales. No ejecuta el
backend, PostgreSQL ni una replicación por red. La pérdida de coordinación es
un estado del modelo y no un corte real de red. El programa no prueba
experimentalmente el teorema CAP ni implementa consenso distribuido.

La reproducción y los límites están documentados en
`practicas/garcia_sangoquiza/README.md`. Los tiempos y PID de una nueva ejecución
pueden variar; no deben sustituirse en estos registros históricos.
