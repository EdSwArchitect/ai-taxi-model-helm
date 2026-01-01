# Taxi Data Indexer (OpenSearch) Helm Chart

This Helm chart deploys the Taxi Data Indexer Quarkus application, which processes NYC taxi trip data from Parquet files and indexes them into OpenSearch.

## Prerequisites

- Kubernetes 1.19+
- Helm 3.0+
- OpenSearch cluster accessible from the cluster
- Persistent storage (optional, for data directories)

## Installation

### Basic Installation

```bash
helm install taxi-data-indexer ./helm/indexing
```

### Installation with Custom Values

```bash
helm install taxi-data-indexer ./helm/indexing -f my-values.yaml
```

### Installation with Overrides

```bash
helm install taxi-data-indexer ./helm/indexing \
  --set config.opensearch.host=opensearch.example.com \
  --set config.opensearch.port=9200 \
  --set secrets.opensearch.password=mysecretpassword
```

## Configuration

The following table lists the configurable parameters and their default values:

| Parameter | Description | Default |
|-----------|-------------|---------|
| `replicaCount` | Number of replicas | `1` |
| `image.repository` | Container image repository | `taxi-data-indexer` |
| `image.tag` | Container image tag | `1.0.0-SNAPSHOT` |
| `image.pullPolicy` | Image pull policy | `IfNotPresent` |
| `service.type` | Kubernetes service type | `ClusterIP` |
| `service.port` | Service port | `8081` |
| `config.processing.inputDirectory` | Input directory for Parquet files | `/data/input` |
| `config.processing.outputDirectory` | Output directory for processed files | `/data/output` |
| `config.processing.errorDirectory` | Error directory for failed files | `/data/error` |
| `config.processing.monitorPeriodMs` | Monitor period in milliseconds | `5000` |
| `config.indexing.rateLimitPerSecond` | Rate limit for indexing (docs/sec) | `100` |
| `config.indexing.batchSize` | Batch size for indexing operations | `50` |
| `config.opensearch.host` | OpenSearch host | `opensearch` |
| `config.opensearch.port` | OpenSearch port | `9200` |
| `config.opensearch.username` | OpenSearch username | `admin` |
| `config.opensearch.useTls` | Enable TLS for OpenSearch connection | `false` |
| `secrets.opensearch.password` | OpenSearch password | `admin` |
| `persistence.enabled` | Enable persistent volumes | `true` |
| `persistence.storageClass` | Storage class for PVCs | `""` (uses default) |
| `persistence.size` | Size of each PVC | `10Gi` |
| `resources.limits.cpu` | CPU limit | `1000m` |
| `resources.limits.memory` | Memory limit | `1Gi` |
| `resources.requests.cpu` | CPU request | `500m` |
| `resources.requests.memory` | Memory request | `512Mi` |

## Persistent Volumes

By default, the chart creates persistent volume claims for input, output, and error directories. If `persistence.storageClass` is not specified, it will use `emptyDir` volumes instead.

To use persistent storage:

```yaml
persistence:
  enabled: true
  storageClass: "standard"
  size: 10Gi
```

## Secrets Management

The OpenSearch password is stored in a Kubernetes Secret. For production, consider using:

- External Secrets Operator
- Sealed Secrets
- HashiCorp Vault
- Cloud provider secret management

To use an existing secret:

```yaml
secrets:
  opensearch:
    password: ""  # Leave empty and create secret manually
```

Then create the secret manually:

```bash
kubectl create secret generic taxi-data-indexer-secrets \
  --from-literal=opensearch-password=mysecretpassword
```

## Rate Limiting

The application implements rate limiting to control the indexing throughput. Adjust the rate limit based on your OpenSearch cluster capacity:

```yaml
config:
  indexing:
    rateLimitPerSecond: 200  # Increase for higher throughput
    batchSize: 100            # Increase for larger batches
```

## Metrics

The application exposes Prometheus metrics at `/metrics`. The service is configured to expose this endpoint for scraping by Prometheus.

## Health Checks

The chart includes liveness and readiness probes that check the Quarkus health endpoints:
- Liveness: `/q/health/live`
- Readiness: `/q/health/ready`

Note: These endpoints require the Quarkus SmallRye Health extension to be included in the application.

## Scaling

### Manual Scaling

```bash
kubectl scale deployment taxi-data-indexer --replicas=3
```

### Horizontal Pod Autoscaling

Enable HPA in `values.yaml`:

```yaml
autoscaling:
  enabled: true
  minReplicas: 1
  maxReplicas: 10
  targetCPUUtilizationPercentage: 80
  targetMemoryUtilizationPercentage: 80
```

## Uninstallation

```bash
helm uninstall taxi-data-indexer
```

## Troubleshooting

### Check Pod Status

```bash
kubectl get pods -l app.kubernetes.io/name=taxi-data-indexer
```

### View Logs

```bash
kubectl logs -l app.kubernetes.io/name=taxi-data-indexer
```

### Check Configuration

```bash
kubectl describe deployment taxi-data-indexer
```

### Access Metrics

```bash
kubectl port-forward svc/taxi-data-indexer 8081:8081
curl http://localhost:8081/metrics
```

### Check OpenSearch Connection

```bash
kubectl exec -it <pod-name> -- env | grep TAXIS_OPENSEARCH
```

