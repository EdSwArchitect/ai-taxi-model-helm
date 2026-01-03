# Troubleshooting Prometheus Metrics

## Common Issues

### 1. Applications Not Running

**Problem**: Prometheus cannot scrape metrics if the Quarkus applications are not running.

**Solution**: 
- Ensure the database application is running on port 8080:
  ```bash
  mvn quarkus:dev -pl database -am
  ```
- Ensure the indexing application is running on port 8081:
  ```bash
  mvn quarkus:dev -pl indexing -am
  ```

### 2. host.docker.internal Not Working (Linux)

**Problem**: On Linux, `host.docker.internal` may not resolve correctly. This is a Docker Desktop feature that works on Mac/Windows but requires extra configuration on Linux.

**Solution Options**:

**Option A: Use host network mode (Linux only)**
Modify `docker-compose.yml` to use host network for Prometheus:
```yaml
prometheus:
  network_mode: "host"
  # Remove networks section
```

Then update `prometheus.yml` targets to use `localhost` instead of `host.docker.internal`:
```yaml
- targets: ['localhost:8080']
- targets: ['localhost:8081']
```

**Option B: Use the host's IP address**
Find your host IP:
```bash
ip addr show docker0 | grep inet
# or
hostname -I | awk '{print $1}'
```

Then update `prometheus.yml`:
```yaml
- targets: ['<your-host-ip>:8080']
- targets: ['<your-host-ip>:8081']
```

**Option C: Add extra_hosts to Prometheus service (Recommended)**
In `docker-compose.yml`, add to Prometheus service:
```yaml
prometheus:
  extra_hosts:
    - "host.docker.internal:host-gateway"
```

### 3. Metrics Endpoint Not Accessible

**Problem**: The metrics endpoint might not be accessible from the Prometheus container.

**Test**:
```bash
# Test from host machine
curl http://localhost:8080/metrics
curl http://localhost:8081/metrics

# Test from Prometheus container
docker exec -it prometheus wget -qO- http://host.docker.internal:8080/metrics
```

### 4. Wrong Metrics Path

**Problem**: Quarkus might expose metrics at a different path.

**Default Quarkus path**: `/q/metrics`
**Configured path**: `/metrics` (set in application.yml)

**Verify**: Check the application logs for the metrics endpoint URL, or try:
```bash
curl http://localhost:8080/q/metrics
curl http://localhost:8080/metrics
```

If metrics are at `/q/metrics`, update `prometheus.yml`:
```yaml
metrics_path: '/q/metrics'
```

### 5. Check Prometheus Targets Status

**Access Prometheus UI**:
1. Open http://localhost:9090/targets
2. Check if targets show "UP" or "DOWN"
3. Click on a target to see error messages

### 6. Check Prometheus Logs

```bash
docker logs prometheus
```

Look for errors like:
- "connection refused"
- "no such host"
- "timeout"

### 7. Firewall Issues

**Problem**: Firewall might be blocking connections.

**Solution**: Ensure ports 8080 and 8081 are accessible from Docker containers.

## Quick Verification Checklist

- [ ] Database application is running on port 8080
- [ ] Indexing application is running on port 8081
- [ ] Metrics endpoint is accessible: `curl http://localhost:8080/metrics`
- [ ] Prometheus container can reach host: `docker exec prometheus ping -c 1 host.docker.internal`
- [ ] Prometheus targets show "UP" status: http://localhost:9090/targets
- [ ] No errors in Prometheus logs: `docker logs prometheus`

## Recommended Configuration for Linux

If you're on Linux and `host.docker.internal` doesn't work, use this configuration:

1. **Update docker-compose.yml** - Add to Prometheus service:
```yaml
prometheus:
  extra_hosts:
    - "host.docker.internal:host-gateway"
```

2. **Restart Prometheus**:
```bash
docker-compose restart prometheus
```

This maps `host.docker.internal` to the host gateway, allowing the container to reach services on the host machine.

