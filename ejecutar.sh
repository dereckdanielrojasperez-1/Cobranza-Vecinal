#!/usr/bin/env bash
cd "$(dirname "$0")"
echo "========================================================"
echo "  Iniciando TuCobranza Vecinal (Sistema de Cobranza)"
echo "========================================================"
if [ -f "./mvnw" ]; then
    chmod +x ./mvnw
    ./mvnw spring-boot:run
else
    mvn spring-boot:run
fi
