# Taxi Data Processor (Database) Helm Chart

This Helm chart deploys the Taxi Data Processor Quarkus application, which processes NYC taxi trip data from Parquet files and stores them in PostgreSQL.

## Prerequisites

- Kubernetes 1.19+
- Helm 3.0+
- PostgreSQL database accessible from the cluster
- Persistent storage (optional, for data directories)

## Installation

### Basic Installation

```bash
helm install taxi-data-processor ./helm/database
```

### Installation with Custom Values

```bash
helm install taxi-data-processor ./helm/database -f my-values.yaml
```

### Installation with Overrides

```bash
helm install taxi-data-processor ./helm/database \
  --set config.database.host=postgres.example.com \
  --set config.database.port=5432 \
  --set secrets.database.password=mysecretpassword
```

## Configuration

The following table lists the configurable parameters and their default values:

| Parameter | Description | Default |
|-----------|-------------|---------|
| `replicaCount` | Number of replicas | `1` |
| `image.repository` | Container image repository | `taxi-data-processor` |
| `image.tag` | Container image tag | `1.0.0-SNAPSHOT` |
| `image.pullPolicy` | Image pull policy | `IfNotPresent` |
| `service.type` | Kubernetes service type | `ClusterIP` |
| `service.port` | Service port | `8080` |
| `config.processing.inputDirectory` | Input directory for Parquet files | `/data/input` |
| `config.processing.outputDirectory` | Output directory for processed files | `/data/output` |
| `config.processing.errorDirectory` | Error directory for failed files | `/data/error` |
| `config.processing.monitorPeriodMs` | Monitor period in milliseconds | `5000` |
| `config.database.host` | PostgreSQL host | `postgres` |
| `config.database.port` | PostgreSQL port | `5432` |
| `config.database.database` | Database name | `taxidb` |
| `config.database.username` | Database username | `postgres` |
| `config.database.useTls` | Enable TLS for database connection | `false` |
| `config.database.schemaType` | Schema type (GREEN or YELLOW) | `GREEN` |
| `config.database.createTableIfNotExists` | Create table if it doesn't exist | `true` |
| `secrets.database.password` | Database password | `postgres` |
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

The database password is stored in a Kubernetes Secret. For production, consider using:

- External Secrets Operator
- Sealed Secrets
- HashiCorp Vault
- Cloud provider secret management

To use an existing secret:

```yaml
secrets:
  database:
    password: ""  # Leave empty and create secret manually
```

Then create the secret manually:

```bash
kubectl create secret generic taxi-data-processor-secrets \
  --from-literal=database-password=mysecretpassword
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
kubectl scale deployment taxi-data-processor --replicas=3
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
helm uninstall taxi-data-processor
```

## Troubleshooting

### Check Pod Status

```bash
kubectl get pods -l app.kubernetes.io/name=taxi-data-processor
```

### View Logs

```bash
kubectl logs -l app.kubernetes.io/name=taxi-data-processor
```

### Check Configuration

```bash
kubectl describe deployment taxi-data-processor
```

### Access Metrics

```bash
kubectl port-forward svc/taxi-data-processor 8080:8080
curl http://localhost:8080/metrics
```

