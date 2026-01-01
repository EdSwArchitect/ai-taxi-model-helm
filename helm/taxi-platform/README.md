# Taxi Platform Helm Chart

This is the **parent chart** that deploys the complete taxi data platform, including all infrastructure services and application services.

## Chart Hierarchy

```
taxi-platform/          # Top-level parent (everything)
├── infrastructure/     # Infrastructure services only
│   ├── postgres
│   ├── opensearch
│   ├── prometheus
│   └── ...
└── applications/       # Application services
    ├── database
    └── indexing
```

## Prerequisites

- Kubernetes 1.19+
- Helm 3.0+
- Persistent storage for databases and data volumes
- Container registry access for application images

## Installation

### Deploy Complete Platform (Non-TLS)

```bash
helm install taxi-platform ./helm/taxi-platform
```

### Deploy Complete Platform (TLS)

```bash
helm install taxi-platform ./helm/taxi-platform \
  --set global.tls.enabled=true
```

### Deploy Only Infrastructure

```bash
helm install taxi-platform ./helm/taxi-platform \
  --set database.enabled=false \
  --set indexing.enabled=false
```

### Deploy Only Applications (requires existing infrastructure)

```bash
helm install taxi-platform ./helm/taxi-platform \
  --set postgres.enabled=false \
  --set opensearch.enabled=false \
  --set prometheus.enabled=false \
  --set grafana.enabled=false \
  # ... disable other infrastructure services
```

### Custom Configuration

```bash
helm install taxi-platform ./helm/taxi-platform \
  -f my-values.yaml
```

## Components

### Application Services
- **database** - Taxi Data Processor (PostgreSQL)
- **indexing** - Taxi Data Indexer (OpenSearch)

### Infrastructure Services
- **postgres** - PostgreSQL with PostGIS
- **opensearch** - OpenSearch search engine
- **opensearch-dashboards** - OpenSearch Dashboards UI
- **kafka** - Apache Kafka message broker
- **etcd** - etcd distributed key-value store
- **victoriametrics** - VictoriaMetrics time series database
- **prometheus** - Prometheus metrics collection
- **grafana** - Grafana visualization
- **geoserver** - GeoServer geospatial data server

## Configuration

### Global TLS Configuration

Enable TLS across all services:

```yaml
global:
  tls:
    enabled: true
    certDir: "./certs"
    keystorePassword: "changeit"
```

### Service-Specific Configuration

Override individual service configurations:

```yaml
database:
  enabled: true
  replicaCount: 2
  config:
    database:
      host: postgres
      port: 5432

prometheus:
  enabled: true
  tls:
    enabled: true
```

### Application Dependencies

The applications automatically connect to infrastructure services using Kubernetes service names:
- `database` → `postgres:5432`
- `indexing` → `opensearch:9200`
- `prometheus` → `victoriametrics:8428`
- `grafana` → `victoriametrics:8428` and `prometheus:9090`

## Deployment Scenarios

### 1. Complete Stack (Recommended for Development)

```bash
helm install taxi-platform ./helm/taxi-platform
```

### 2. Production with TLS

```bash
helm install taxi-platform ./helm/taxi-platform \
  --set global.tls.enabled=true \
  --set database.replicaCount=3 \
  --set indexing.replicaCount=3
```

### 3. Infrastructure Only

Use the `infrastructure` chart instead:

```bash
helm install taxi-infrastructure ./helm/infrastructure
```

### 4. Applications Only (Infrastructure Already Deployed)

```bash
helm install taxi-apps ./helm/taxi-platform \
  --set postgres.enabled=false \
  --set opensearch.enabled=false \
  --set kafka.enabled=false \
  --set etcd.enabled=false \
  --set victoriametrics.enabled=false \
  --set prometheus.enabled=false \
  --set grafana.enabled=false \
  --set geoserver.enabled=false \
  --set opensearch-dashboards.enabled=false
```

## Upgrading

```bash
helm upgrade taxi-platform ./helm/taxi-platform
```

## Uninstallation

```bash
helm uninstall taxi-platform
```

**Warning**: This will remove all services. Make sure you have backups of persistent data.

## Troubleshooting

### Check All Services

```bash
kubectl get pods -l app.kubernetes.io/managed-by=Helm
```

### Check Specific Service

```bash
kubectl get pods -l app.kubernetes.io/name=postgres
```

### View Logs

```bash
kubectl logs -l app.kubernetes.io/name=database
```

### Check Service Dependencies

```bash
kubectl get svc
```

## Notes

- Services are deployed in dependency order
- Health checks ensure services start in the correct sequence
- Persistent volumes are created automatically if storageClass is specified
- TLS certificates should be provided via Kubernetes secrets or cert-manager
- Default replica count is 1 for all services (development mode)

