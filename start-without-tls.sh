#!/bin/bash

# Script to start docker-compose without TLS (HTTP only)

set -e

ENV_FILE=".env"

echo "Starting services without TLS (HTTP only)..."

# Update .env to disable TLS
if [ -f "${ENV_FILE}" ]; then
    if grep -q "TLS_ENABLED=true" "${ENV_FILE}" 2>/dev/null; then
        sed -i.bak 's/TLS_ENABLED=true/TLS_ENABLED=false/' "${ENV_FILE}"
        echo "Updated .env to disable TLS"
    fi
fi

# Start services without TLS override
echo "Starting docker-compose without TLS..."
docker-compose up -d

echo ""
echo "Services started without TLS (HTTP only)!"
echo ""
echo "Access URLs (HTTP):"
echo "  - Prometheus:  http://localhost:9090"
echo "  - Grafana:     http://localhost:3000"
echo "  - GeoServer:   http://localhost:8080/geoserver"

