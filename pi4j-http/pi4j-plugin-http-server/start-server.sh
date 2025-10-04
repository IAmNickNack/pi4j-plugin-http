#!/usr/bin/env bash

SERVER_PORT=${1:-8080}
JAR_FILE="$(find . -name pi4j-plugin-http-server.jar)"

 java \
  --sun-misc-unsafe-memory-access=allow \
  --enable-native-access=ALL-UNNAMED \
  -Dserver.port="${SERVER_PORT}" \
  -jar "${JAR_FILE}"
