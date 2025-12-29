# Docker Compose Setup with Optional TLS

This project includes a Docker Compose setup with optional TLS encryption support.

## Quick Start

### Without TLS (HTTP only - Default)
```bash
./start-without-tls.sh
# OR
docker-compose up -d
```

### With TLS (HTTPS)
```bash
./start-with-tls.sh
# OR
docker-compose -f docker-compose.yml -f docker-compose.tls.yml up -d
```

## Services

- **PostgreSQL 16 with PostGIS 3.4**: Spatial database (port 5432)
- **OpenSearch 2.11.0**: Search engine (ports 9200, 9600)
- **OpenSearch Dashboards 2.11.0**: Search UI (port 5601)
- **Kafka (Apache)**: Message broker with KRaft (ports 9092, 9094)
- **etcd 3.5.10**: Distributed key-value store (ports 2379, 2380)
- **VictoriaMetrics v1.100.0**: Time series database (port 8428)
- **Prometheus 2.48.0**: Metrics collection and scraping (HTTP: 9090, HTTPS: 9091)
- **Grafana 10.2.2**: Visualization and dashboards (HTTP: 3000, HTTPS: 3443)
- **GeoServer (kartoza)**: Geospatial server (HTTP: 8080, HTTPS: 8443)

## TLS Configuration

### Prerequisites

1. Generate certificates:
   ```bash
   ./generate-tls-cert.sh
   ```

   This creates:
   - `certs/localhost.key` - Private key
   - `certs/localhost.crt` - Certificate
   - `certs/localhost.p12` - PKCS12 keystore
   - `certs/localhost.jks` - JKS keystore (if Java is installed)
   - `certs/localhost-truststore.jks` - Truststore (if Java is installed)

### Enable TLS

**Option 1: Using helper script (recommended)**
```bash
./start-with-tls.sh
```

**Option 2: Using docker-compose directly**
```bash
# Create .env file
cp .env.example .env
# Edit .env and set TLS_ENABLED=true

# Start with TLS override
docker-compose -f docker-compose.yml -f docker-compose.tls.yml up -d
```

**Option 3: Environment variables**
```bash
export TLS_ENABLED=true
export CERT_DIR=./certs
export KEYSTORE_PASSWORD=changeit
docker-compose -f docker-compose.yml -f docker-compose.tls.yml up -d
```

### Disable TLS

**Option 1: Using helper script**
```bash
./start-without-tls.sh
```

**Option 2: Using docker-compose directly**
```bash
docker-compose up -d
```

## Access URLs

### Without TLS (HTTP)
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000
- VictoriaMetrics: http://localhost:8428
- GeoServer: http://localhost:8080/geoserver
- OpenSearch: http://localhost:9200
- OpenSearch Dashboards: http://localhost:5601
- PostgreSQL: localhost:5432
- Kafka: localhost:9092 (internal), localhost:9094 (external)
- etcd: localhost:2379

### With TLS (HTTPS)
- Prometheus: https://localhost:9091
- Grafana: https://localhost:3443
- GeoServer: https://localhost:8443/geoserver

**Note**: You'll need to accept the self-signed certificate in your browser.

### Default Credentials
- **Grafana**: admin / admin
- **GeoServer**: admin / geoserver
- **OpenSearch**: admin / admin
- **PostgreSQL**: postgres / postgres

## Metrics Architecture

The Docker stack includes a complete metrics collection and visualization pipeline:

1. **Quarkus Applications** expose metrics at `/metrics` endpoint (Micrometer format)
2. **Prometheus** scrapes metrics from applications and writes to VictoriaMetrics via `remote_write`
3. **VictoriaMetrics** stores time series data with 12-month retention
4. **Grafana** queries VictoriaMetrics and displays pre-configured dashboards

### VictoriaMetrics

VictoriaMetrics is configured as the primary time series database:
- **Port**: 8428
- **Retention**: 12 months
- **Storage**: Persistent volume (`victoriametrics_data`)
- **API**: Prometheus-compatible (can be used as Prometheus replacement)
- **Health Check**: http://localhost:8428/health

### Prometheus Configuration

Prometheus is configured to:
- Scrape metrics from Quarkus applications (taxi-data-processor, taxi-data-indexer)
- Write all metrics to VictoriaMetrics via `remote_write`
- Short retention (2 hours) since VictoriaMetrics handles long-term storage
- Configuration: `prometheus/prometheus.yml`

### Grafana Dashboards

Grafana is pre-configured with:
- **VictoriaMetrics datasource** (default)
- **Taxi Data Processing Metrics dashboard** (auto-provisioned)
- Dashboard location: `grafana/provisioning/dashboards/taxi-metrics.json`

## OpenSearch Indices

The indexing application automatically creates two separate indices:

- **`green-tripdata`**: Stores green taxi trip data
  - Auto-created when first green trip data file is processed
  - Settings: 1 shard, 0 replicas (development)
  
- **`yellow-tripdata`**: Stores yellow taxi trip data
  - Auto-created when first yellow trip data file is processed
  - Settings: 1 shard, 0 replicas (development)

View indices via OpenSearch Dashboards: http://localhost:5601 → Management → Index Management

## Configuration Files

- `docker-compose.yml` - Base configuration (HTTP by default)
- `docker-compose.tls.yml` - TLS override configuration
- `.env` - Environment variables (create from `.env.example`)
- `prometheus/web.yml` - Prometheus TLS configuration
- `prometheus/prometheus.yml` - Prometheus scrape and remote_write configuration
- `grafana/provisioning/datasources/victoriametrics.yml` - Grafana datasource configuration
- `grafana/provisioning/dashboards/taxi-metrics.json` - Pre-configured dashboard

## Environment Variables

Create a `.env` file or set these environment variables:

```bash
TLS_ENABLED=false              # Set to 'true' to enable TLS
CERT_DIR=./certs               # Certificate directory
KEYSTORE_PASSWORD=changeit     # Keystore password

# Optional: Custom ports
PROMETHEUS_TLS_PORT=9091
GRAFANA_TLS_PORT=3443
GEOSERVER_TLS_PORT=8443
```

## Service Dependencies

Services start in the following order:
1. **VictoriaMetrics** - Time series database (no dependencies)
2. **PostgreSQL** - Database (no dependencies)
3. **OpenSearch** - Search engine (no dependencies)
4. **Prometheus** - Depends on VictoriaMetrics
5. **Grafana** - Depends on VictoriaMetrics and Prometheus
6. **OpenSearch Dashboards** - Depends on OpenSearch
7. **Kafka** - Message broker (no dependencies)
8. **etcd** - Key-value store (no dependencies)
9. **GeoServer** - Geospatial server (no dependencies)

## Volumes

Persistent data is stored in Docker volumes:
- `postgres_data` - PostgreSQL database files
- `opensearch_data` - OpenSearch indices and data
- `opensearch-dashboards_data` - OpenSearch Dashboards configuration
- `kafka_data` - Kafka logs and data
- `etcd_data` - etcd data
- `victoriametrics_data` - VictoriaMetrics time series data
- `prometheus_data` - Prometheus data (short-term, 2 hours)
- `grafana_data` - Grafana dashboards and configuration
- `geoserver_data` - GeoServer data directory

To remove all volumes (⚠️ **WARNING**: This deletes all data):
```bash
docker-compose down -v
```

## Troubleshooting

### Certificates not found
If you see certificate errors, run:
```bash
./generate-tls-cert.sh
```

### Port conflicts
If ports are already in use, modify the port mappings in `docker-compose.yml` or set custom ports in `.env`.

### Browser certificate warnings
Self-signed certificates will show warnings in browsers. This is expected for development. Click "Advanced" and "Proceed" to continue.

### VictoriaMetrics not accessible
Check if VictoriaMetrics is healthy:
```bash
curl http://localhost:8428/health
```

### Prometheus not scraping metrics
1. Check Prometheus targets: http://localhost:9090/targets
2. Verify applications are running and exposing metrics at `/metrics`
3. Check Prometheus logs: `docker-compose logs prometheus`

### Grafana dashboard not showing data
1. Verify VictoriaMetrics datasource is configured: http://localhost:3000/datasources
2. Check if metrics are being written to VictoriaMetrics
3. Verify time range in dashboard (default: last 6 hours)
4. Check Grafana logs: `docker-compose logs grafana`

### OpenSearch indices not created
1. Verify indexing application is running and processing files
2. Check OpenSearch logs: `docker-compose logs opensearch`
3. Verify files are being placed in the input directory
4. Check application logs for indexing errors

### Check service status
```bash
# List all services and their status
docker-compose ps

# View logs for a specific service
docker-compose logs [service-name]

# Follow logs in real-time
docker-compose logs -f [service-name]

# Check service health
docker-compose ps | grep -E "Up|Exit"
```

## Stopping Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

