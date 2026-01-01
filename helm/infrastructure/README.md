# Taxi Infrastructure Helm Chart

This is a **parent chart** that deploys only the infrastructure services (databases, message brokers, monitoring, etc.) without the application services.

For deploying the complete platform (infrastructure + applications), use the `taxi-platform` chart instead.

## Prerequisites

- Kubernetes 1.19+
- Helm 3.0+
- Persistent storage for databases and data volumes

## Installation

### Non-TLS Mode (Default)

```bash
helm install taxi-infrastructure ./helm/infrastructure
```

### TLS Mode

```bash
helm install taxi-infrastructure ./helm/infrastructure \
  --set global.tls.enabled=true
```

### Custom Configuration

```bash
helm install taxi-infrastructure ./helm/infrastructure \
  -f my-values.yaml
```

## Components

This chart deploys the following services:

1. **PostgreSQL with PostGIS** - Database for taxi trip data
2. **OpenSearch** - Search and analytics engine
3. **OpenSearch Dashboards** - Visualization for OpenSearch
4. **Kafka** - Message broker
5. **etcd** - Distributed key-value store
6. **VictoriaMetrics** - Time series database
7. **Prometheus** - Metrics collection and monitoring
8. **Grafana** - Metrics visualization and dashboards
9. **GeoServer** - Geospatial data server

## Configuration

See individual service chart READMEs for detailed configuration options:
- `helm/postgres/README.md`
- `helm/opensearch/README.md`
- `helm/prometheus/README.md`
- `helm/grafana/README.md`
- etc.

## TLS Configuration

To enable TLS across all services:

```yaml
global:
  tls:
    enabled: true
    certDir: "./certs"
    keystorePassword: "changeit"
```

Individual services can override TLS settings in their respective values files.

## Uninstallation

```bash
helm uninstall taxi-infrastructure
```

