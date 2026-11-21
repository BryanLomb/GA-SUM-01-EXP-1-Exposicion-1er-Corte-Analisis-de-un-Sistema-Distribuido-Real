#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")"

mkdir -p build
javac --release 11 -encoding UTF-8 -d build src/*.java

case "${1:-comparar}" in
  probar)
    if [[ $# -ne 1 ]]; then
      echo "Uso: bash ejecutar.sh probar" >&2
      exit 2
    fi
    java -cp build Pruebas
    ;;
  *)
    if [[ $# -eq 0 ]]; then set -- comparar; fi
    java -cp build PracticaHorarios "$@"
    ;;
esac
