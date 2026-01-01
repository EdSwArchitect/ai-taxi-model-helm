# Helm Charts for Taxi Infrastructure

This directory contains Helm charts for deploying the complete taxi data infrastructure stack to Kubernetes.

## Chart Structure

### Parent Charts
- **taxi-platform/** - **Top-level parent chart** that deploys everything (infrastructure + applications)
- **infrastructure/** - Parent chart that deploys only infrastructure services (without applications)

### Application Charts
- **database/** - Taxi Data Processor (Database) Quarkus application
- **indexing/** - Taxi Data Indexer (OpenSearch) Quarkus application

### Infrastructure Charts
- **postgres/** - PostgreSQL with PostGIS extension
- **opensearch/** - OpenSearch search and analytics engine
- **opensearch-dashboards/** - OpenSearch Dashboards UI
- **kafka/** - Apache Kafka message broker
- **etcd/** - etcd distributed key-value store
- **victoriametrics/** - VictoriaMetrics time series database
- **prometheus/** - Prometheus metrics collection (with TLS support)
- **grafana/** - Grafana visualization (with TLS support)
- **geoserver/** - GeoServer geospatial data server (with TLS support)

## TLS Support

The following charts support TLS configuration:
- **prometheus** - TLS enabled via `tls.enabled: true`
- **grafana** - TLS enabled via `tls.enabled: true`
- **geoserver** - TLS enabled via `tls.enabled: true`

## Quick Start

### Deploy Complete Platform (Recommended)

```bash
# Non-TLS
helm install taxi-platform ./helm/taxi-platform

# With TLS
helm install taxi-platform ./helm/taxi-platform \
  --set global.tls.enabled=true
```

### Deploy Only Infrastructure (Without Applications)

```bash
# Non-TLS
helm install taxi-infrastructure ./helm/infrastructure

# With TLS
helm install taxi-infrastructure ./helm/infrastructure \
  --set global.tls.enabled=true
```

### Deploy Individual Services

```bash
# PostgreSQL
helm install postgres ./helm/postgres

# OpenSearch
helm install opensearch ./helm/opensearch

# Prometheus with TLS
helm install prometheus ./helm/prometheus --set tls.enabled=true

# Grafana with TLS
helm install grafana ./helm/grafana --set tls.enabled=true
```

## Configuration

Each chart has its own `values.yaml` file with configurable parameters. Key configuration areas:

- **Replica counts** - Default is 1 for all services
- **Resource limits** - CPU and memory constraints
- **Persistence** - Storage configuration for data volumes
- **TLS** - Certificate and keystore configuration
- **Service types** - ClusterIP, NodePort, or LoadBalancer
- **Ingress** - Optional ingress configuration

## TLS Certificate Management

For TLS-enabled services, certificates should be provided via:
1. Kubernetes Secrets (manually created)
2. cert-manager (automatic certificate management)
3. External secret management systems

Example secret creation:
```bash
kubectl create secret tls prometheus-tls \
  --cert=./certs/localhost.crt \
  --key=./certs/localhost.key
```

## Dependencies

The infrastructure chart manages dependencies between services:
- Prometheus depends on VictoriaMetrics
- Grafana depends on VictoriaMetrics and Prometheus
- OpenSearch Dashboards depends on OpenSearch

## Notes

- All charts default to 1 replica for development
- Persistent volumes are optional (use emptyDir if storageClass not specified)
- Health checks are enabled by default
- Service accounts are created automatically
- Platform-specific images (linux/amd64) are used where needed

## Chart Status

✅ Complete and tested:
- database
- indexing
- postgres
- opensearch
- prometheus (with TLS)
- grafana (with TLS)

🔄 In progress:
- opensearch-dashboards
- kafka
- etcd
- victoriametrics
- geoserver (with TLS)

