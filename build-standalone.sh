#!/bin/bash
# Construye el frontend de Angular y lo copia dentro del backend, para que
# quede TODO (API + frontend) sirviéndose desde un solo puerto (8080).
#
# Úsalo antes de salir a capturar con la tablet vía túnel remoto
# (ngrok/Cloudflare): solo necesitas exponer el puerto del backend, no dos
# puertos distintos.
#
# Uso:
#   ./build-standalone.sh
#
# Después:
#   1. Corre el backend normal (desde IntelliJ o "mvn spring-boot:run").
#   2. Abre un túnel al puerto 8080 (ej. "ngrok http 8080" o
#      "cloudflared tunnel --url http://localhost:8080").
#   3. Entra desde la tablet a la URL que te dé el túnel + "/Los_Lopez/"
#      (ej. https://xxxx.ngrok-free.app/Los_Lopez/).

set -e

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FRONTEND_DIR="$ROOT_DIR/frontEnd"
BACKEND_STATIC_DIR="$ROOT_DIR/backEnd/src/main/resources/static"

echo "1/3 - Instalando dependencias del frontend (si hace falta)..."
cd "$FRONTEND_DIR"
if [ ! -d "node_modules" ]; then
  npm install
fi

echo "2/3 - Construyendo Angular (producción)..."
npx ng build

echo "3/3 - Copiando el build al backend..."
rm -rf "$BACKEND_STATIC_DIR"
mkdir -p "$BACKEND_STATIC_DIR"
cp -R "$FRONTEND_DIR/dist/front-los-lopez/." "$BACKEND_STATIC_DIR/"

echo ""
echo "Listo. Reconstruye y reinicia el backend para que tome el nuevo build."
echo "Después, abre el túnel al puerto 8080 y entra desde la tablet a <url-del-tunel>/Los_Lopez/"
