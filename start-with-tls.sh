#!/bin/bash

# Script to start docker-compose with TLS enabled
# This script generates certificates if needed and starts services with TLS

set -e

CERT_DIR="./certs"
ENV_FILE=".env"

echo "Starting services with TLS enabled..."

# Check if certificates exist
if [ ! -f "${CERT_DIR}/localhost.crt" ] || [ ! -f "${CERT_DIR}/localhost.key" ]; then
    echo "Certificates not found. Generating new certificates..."
    if [ ! -f "./generate-tls-cert.sh" ]; then
        echo "Error: generate-tls-cert.sh not found!"
        exit 1
    fi
    ./generate-tls-cert.sh
fi

# Check if .env file exists, create from example if not
if [ ! -f "${ENV_FILE}" ]; then
    if [ -f ".env.example" ]; then
        echo "Creating .env file from .env.example..."
        cp .env.example .env
    else
        echo "Creating .env file..."
        cat > "${ENV_FILE}" <<EOF
# TLS Configuration
TLS_ENABLED=true
CERT_DIR=./certs
KEYSTORE_PASSWORD=changeit

# Service TLS Ports
PROMETHEUS_TLS_PORT=9091
GRAFANA_TLS_PORT=3443
GEOSERVER_TLS_PORT=8443
EOF
    fi
fi

# Update .env to enable TLS
if grep -q "TLS_ENABLED=false" "${ENV_FILE}" 2>/dev/null; then
    sed -i.bak 's/TLS_ENABLED=false/TLS_ENABLED=true/' "${ENV_FILE}"
    echo "Updated .env to enable TLS"
fi

# Start services with TLS override
echo "Starting docker-compose with TLS configuration..."
docker-compose -f docker-compose.yml -f docker-compose.tls.yml up -d

echo ""
echo "Services started with TLS enabled!"
echo ""
echo "Access URLs (HTTPS):"
echo "  - Prometheus:  https://localhost:9091"
echo "  - Grafana:     https://localhost:3443"
echo "  - GeoServer:   https://localhost:8443/geoserver"
echo ""
echo "Note: You may need to accept the self-signed certificate in your browser."

