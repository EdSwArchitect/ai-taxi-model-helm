# AI Taxi Model Helm

A multi-module Maven project for processing and analyzing NYC taxi trip data from parquet files. This project provides data models, parsing utilities, and a complete Docker-based infrastructure stack for development and deployment.

## Table of Contents

- [Project Structure](#project-structure)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Building the Project](#building-the-project)
- [Quarkus Applications](#quarkus-applications)
- [Model Classes](#model-classes)
- [Trip Data Parser](#trip-data-parser)
- [Testing](#testing)
- [Logging](#logging)
- [Metrics and Monitoring](#metrics-and-monitoring)
- [Docker Infrastructure](#docker-infrastructure)
- [TLS Certificate Generation](#tls-certificate-generation)
- [Usage Examples](#usage-examples)

## Project Structure

This is a multi-module Maven project with the following modules:

```
ai-taxi-model-helm/
├── common/          # Common utilities and shared code
├── database/         # Database access and persistence layer
├── indexing/        # Indexing and search functionality
├── docker-compose.yml
├── docker-compose.tls.yml
└── generate-tls-cert.sh
```

### Modules

- **common**: Contains model classes (`GreenTripdata`, `YellowTripdata`), utilities (`TripDataParser`, `Monitor`), and shared resources
- **database**: Quarkus application for processing parquet files and storing data in PostgreSQL (depends on `common`)
- **indexing**: Quarkus application for processing parquet files and indexing data in OpenSearch with rate limiting (depends on `common` and `database`)

## Features

- ✅ Multi-module Maven project structure
- ✅ NYC taxi trip data models (Green and Yellow)
- ✅ Parquet file parser with schema validation
- ✅ Quarkus applications for automated file processing
- ✅ PostgreSQL integration with optional TLS
- ✅ OpenSearch integration with optional TLS and rate limiting
- ✅ Separate OpenSearch indices for green and yellow trip data
- ✅ Directory monitoring with configurable scan periods
- ✅ Batch processing with configurable batch sizes
- ✅ Rate limiting for indexing operations
- ✅ Logback logging with file rotation
- ✅ Comprehensive test coverage
- ✅ Docker Compose infrastructure stack
- ✅ Optional TLS/HTTPS support
- ✅ Self-signed certificate generation

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- Docker and Docker Compose
- OpenSSL (for certificate generation)

## Building the Project

### Build all modules:
```bash
mvn clean install
```

### Build specific module:
```bash
mvn clean install -pl common
```

### Run tests:
```bash
mvn test
```

### Run tests for specific module:
```bash
mvn test -pl common
```

## Quarkus Applications

The project includes two Quarkus applications for automated processing of taxi trip data:

### Database Application (`database` module)

A Quarkus application that monitors an input directory for parquet files, parses them, and stores the data in PostgreSQL.

**Features:**
- Automatic directory monitoring
- Schema validation (green vs yellow tripdata)
- Batch database inserts
- Automatic table creation
- File management (move to output/error directories)
- Optional TLS support for PostgreSQL connections

**Run in development mode:**
```bash
cd database
mvn quarkus:dev
```

**Build and run:**
```bash
cd database
mvn clean package
java -jar target/quarkus-app/quarkus-run.jar
```

**Configuration** (`database/src/main/resources/application.yml`):
```yaml
taxis:
  processing:
    input-directory: "./data/input"
    output-directory: "./data/output"
    error-directory: "./data/error"
    monitor-period-ms: 5000
    
  database:
    host: localhost
    port: 5432
    database: taxidb
    username: postgres
    password: postgres
    use-tls: false
    cert-path: ""
    schema-type: GREEN  # or YELLOW
    create-table-if-not-exists: true
```

### Indexing Application (`indexing` module)

A Quarkus application that monitors an input directory for parquet files, parses them, and indexes the data in OpenSearch with rate limiting.

**Features:**
- Automatic directory monitoring
- Schema validation (green vs yellow tripdata)
- **Separate indices based on schema type**:
  - Green trip data → `green-tripdata` index
  - Yellow trip data → `yellow-tripdata` index
- Automatic index creation with optimized settings
- Rate-limited indexing operations
- Configurable batch processing
- File management (move to output/error directories)
- Optional TLS support for OpenSearch connections

**Run in development mode:**
```bash
cd indexing
mvn quarkus:dev
```

**Build and run:**
```bash
cd indexing
mvn clean package
java -jar target/quarkus-app/quarkus-run.jar
```

**Configuration** (`indexing/src/main/resources/application.yml`):
```yaml
taxis:
  processing:
    input-directory: "./data/input"
    output-directory: "./data/output"
    error-directory: "./data/error"
    monitor-period-ms: 5000
    
  indexing:
    rate-limit-per-second: 100  # Maximum documents per second
    batch-size: 50              # Documents per batch
    
  opensearch:
    host: localhost
    port: 9200
    username: admin
    password: admin
    use-tls: false
    cert-path: ""
```

**Rate Limiting:**
- Configurable rate limit (default: 100 documents/second)
- Token bucket algorithm implementation
- Automatic permit refill every second
- Blocks when rate limit is exceeded

**Batch Processing:**
- Configurable batch size (default: 50 documents)
- Processes files in batches to optimize performance
- Rate limiting applies per batch

**OpenSearch Indices:**
- **`green-tripdata`**: Index for green taxi trip data
  - Auto-created with 1 shard, 0 replicas (development settings)
  - Field types auto-detected from first document
  - Contains all green trip data fields (lpepPickupDatetime, lpepDropoffDatetime, etc.)
  
- **`yellow-tripdata`**: Index for yellow taxi trip data
  - Auto-created with 1 shard, 0 replicas (development settings)
  - Field types auto-detected from first document
  - Contains all yellow trip data fields (tpepPickupDatetime, tpepDropoffDatetime, etc.)

The application automatically routes data to the correct index based on the parquet file schema type detected during parsing.

## Model Classes

### GreenTripdata

Model class for NYC green taxi trip data located in `common/src/main/java/com/bscllc/taxis/model/GreenTripdata.java`.

**Key Fields:**
- `lpepPickupDatetime`, `lpepDropoffDatetime` - Trip timestamps
- `puLocationId`, `doLocationId` - Pickup and dropoff location IDs
- `tripDistance`, `fareAmount`, `totalAmount` - Trip metrics
- `ehailFee`, `tripType` - Green taxi specific fields

### YellowTripdata

Model class for NYC yellow taxi trip data located in `common/src/main/java/com/bscllc/taxis/model/YellowTripdata.java`.

**Key Fields:**
- `tpepPickupDatetime`, `tpepDropoffDatetime` - Trip timestamps
- `puLocationId`, `doLocationId` - Pickup and dropoff location IDs
- `tripDistance`, `fareAmount`, `totalAmount` - Trip metrics

## Trip Data Parser

The `TripDataParser` utility class (`com.bscllc.taxis.util.TripDataParser`) provides methods to parse and validate parquet trip data files.

### Features

- Automatic schema detection (green vs yellow tripdata)
- Schema validation
- Parsing to model objects
- Exception handling for invalid files

### Usage

```java
import com.bscllc.taxis.util.TripDataParser;
import com.bscllc.taxis.util.TripDataParserException;
import com.bscllc.taxis.model.GreenTripdata;
import com.bscllc.taxis.model.YellowTripdata;

// Check if file is green tripdata
if (TripDataParser.isGreenTripdataFile("path/to/file.parquet")) {
    List<GreenTripdata> trips = TripDataParser.parseGreenTripdata("path/to/file.parquet");
}

// Check if file is yellow tripdata
if (TripDataParser.isYellowTripdataFile("path/to/file.parquet")) {
    List<YellowTripdata> trips = TripDataParser.parseYellowTripdata("path/to/file.parquet");
}
```

### Methods

- `isGreenTripdataFile(File/String)` - Returns true if file matches green tripdata schema
- `isYellowTripdataFile(File/String)` - Returns true if file matches yellow tripdata schema
- `parseGreenTripdata(File/String)` - Parses green tripdata parquet file
- `parseYellowTripdata(File/String)` - Parses yellow tripdata parquet file

All parsing methods throw `TripDataParserException` if:
- File doesn't exist
- File is not a valid parquet file
- File doesn't match the expected schema

## Testing

The project includes comprehensive JUnit 5 tests for all model classes.

### Run all tests:
```bash
mvn test
```

### Test Coverage

- **GreenTripdataTest**: Tests for all fields, getters/setters, toString, and null handling
- **YellowTripdataTest**: Tests for all fields, getters/setters, toString, and null handling
- **MonitorTest**: Tests for directory monitoring, file detection, and pattern matching

## Logging

The project uses **SLF4J** with **Logback** as the logging implementation.

### Logback Configuration

Both Quarkus applications include Logback configuration files:
- `database/src/main/resources/logback.xml`
- `indexing/src/main/resources/logback.xml`

**Features:**
- Console and file appenders
- Separate error log file
- Time-based rolling with size limits
- Configurable log levels per package
- Max file size: 100MB per file
- Max history: 30 days
- Total size cap: 3GB (main log), 1GB (error log)

**Log File Locations:**
- Default directory: `./logs/`
- Main log: `taxi-data-processor.log` or `taxi-data-indexer.log`
- Error log: `taxi-data-processor-error.log` or `taxi-data-indexer-error.log`

**Customize Log Directory:**
```bash
java -DLOG_DIR=/var/log/taxi -jar database/target/quarkus-app/quarkus-run.jar
```

**Log Levels:**
- Application packages (`com.bscllc.taxis.*`): INFO (DEBUG for app/service/util)
- Third-party libraries: WARN/INFO based on package

## Metrics and Monitoring

The project includes comprehensive metrics collection using **Micrometer** and visualization with **Grafana** and **VictoriaMetrics**.

### Metrics Collected

Both Quarkus applications expose the following metrics via the `/metrics` endpoint:

- **`taxis_files_processed_total`** - Total number of files processed successfully
- **`taxis_files_errored_total`** - Total number of files that failed to process
- **`taxis_records_indexed_total`** - Total number of records indexed into OpenSearch (indexing module)
- **`taxis_records_inserted_total`** - Total number of records inserted into the database (database module)
- **`taxis_tables_created_total`** - Total number of database tables created (database module)

### Architecture

1. **Quarkus Applications** expose metrics at `/metrics` endpoint (Micrometer format)
2. **Prometheus** scrapes metrics from the applications and writes to VictoriaMetrics
3. **VictoriaMetrics** stores time series data (12 months retention)
4. **Grafana** queries VictoriaMetrics and displays dashboards

### VictoriaMetrics

VictoriaMetrics v1.100.0 is configured as the time series database:
- **Port**: 8428
- **Retention**: 12 months
- **Storage**: Persistent volume
- **API**: Prometheus-compatible

### Grafana Dashboard

A pre-configured dashboard is automatically provisioned: **"Taxi Data Processing Metrics"**

**Dashboard Panels:**
- Files Processed Rate (time series)
- Files Errored Rate (stat)
- Records Indexed Rate (time series)
- Records Inserted Rate (time series)
- Tables Created (stat)
- Processing Success Rate (gauge)
- Files Processed vs Errored (cumulative)
- Records Indexed vs Inserted (cumulative)

**Access:**
- URL: `http://localhost:3000` (or configured port)
- Default credentials: `admin` / `admin`
- Dashboard: Navigate to "Dashboards" → "Taxi Data Processing Metrics"

### Configuration

**Prometheus** is configured to scrape metrics from:
- `taxi-data-processor` (database application) - port 8080
- `taxi-data-indexer` (indexing application) - port 8081

**Note**: Update the Prometheus scrape targets in `prometheus/prometheus.yml` if your applications run on different ports or hosts.

### OpenSearch Indices

The indexing application automatically creates and manages two separate indices based on the parquet schema type:

- **`green-tripdata`**: Stores green taxi trip data
  - Created automatically when first green trip data file is processed
  - Index settings: 1 shard, 0 replicas (suitable for development)
  - Field mappings: Auto-detected from first document
  - Contains all green trip data fields (lpepPickupDatetime, lpepDropoffDatetime, ehailFee, tripType, etc.)
  
- **`yellow-tripdata`**: Stores yellow taxi trip data
  - Created automatically when first yellow trip data file is processed
  - Index settings: 1 shard, 0 replicas (suitable for development)
  - Field mappings: Auto-detected from first document
  - Contains all yellow trip data fields (tpepPickupDatetime, tpepDropoffDatetime, etc.)

The application automatically routes data to the correct index based on the parquet file schema type detected during parsing.

**Querying Indices:**

You can query the indices directly via OpenSearch API:

```bash
# Query green trip data
curl -X GET "http://localhost:9200/green-tripdata/_search?pretty" \
  -u admin:admin

# Query yellow trip data
curl -X GET "http://localhost:9200/yellow-tripdata/_search?pretty" \
  -u admin:admin

# Search across both indices
curl -X GET "http://localhost:9200/green-tripdata,yellow-tripdata/_search?pretty" \
  -u admin:admin
```

**Index Management:**

View index information via OpenSearch Dashboards:
- URL: `http://localhost:5601`
- Navigate to: Management → Index Management
- You'll see both `green-tripdata` and `yellow-tripdata` indices

### Metrics Endpoints

- **Database Application**: `http://localhost:8080/metrics`
- **Indexing Application**: `http://localhost:8081/metrics`
- **VictoriaMetrics**: `http://localhost:8428`
- **Prometheus**: `http://localhost:9090`
- **Grafana**: `http://localhost:3000`

## Docker Infrastructure

The project includes a complete Docker Compose setup with the following services:

- **PostgreSQL 16 with PostGIS 3.4** - Spatial database (port 5432)
- **OpenSearch 2.11.0** - Search engine (ports 9200, 9600)
- **OpenSearch Dashboards 2.11.0** - Search UI (port 5601)
- **Kafka 3.6.1** - Message broker with KRaft (ports 9092, 9094)
- **etcd 3.5.10** - Distributed key-value store (ports 2379, 2380)
- **VictoriaMetrics v1.100.0** - Time series database (port 8428)
- **Prometheus 2.48.0** - Metrics collection (HTTP: 9090, HTTPS: 9091)
- **Grafana 10.2.2** - Visualization (HTTP: 3000, HTTPS: 3443)
- **GeoServer 2.23.4** - Geospatial server (HTTP: 8080, HTTPS: 8443)

### Quick Start

**Without TLS (HTTP only):**
```bash
./start-without-tls.sh
# OR
docker-compose up -d
```

**With TLS (HTTPS):**
```bash
./start-with-tls.sh
# OR
docker-compose -f docker-compose.yml -f docker-compose.tls.yml up -d
```

### Access URLs

**Without TLS:**
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000
- GeoServer: http://localhost:8080/geoserver
- OpenSearch: http://localhost:9200
- OpenSearch Dashboards: http://localhost:5601
- VictoriaMetrics: http://localhost:8428

**With TLS:**
- Prometheus: https://localhost:9091
- Grafana: https://localhost:3443
- GeoServer: https://localhost:8443/geoserver

**Default Credentials:**
- Grafana: admin/admin
- GeoServer: admin/geoserver
- OpenSearch: admin/admin

For detailed Docker setup instructions, see [README-DOCKER.md](README-DOCKER.md).

## TLS Certificate Generation

The project includes a script to generate self-signed TLS certificates for localhost.

### Generate Certificates

```bash
./generate-tls-cert.sh
```

### Options

```bash
# Default: 2048-bit key, 365 days validity, password: changeit
./generate-tls-cert.sh

# Custom key size
./generate-tls-cert.sh 4096

# Custom key size and validity
./generate-tls-cert.sh 4096 730

# Custom password
./generate-tls-cert.sh 2048 365 mypassword
```

### Generated Files

All files are created in the `certs/` directory:

- `localhost.key` - Private key
- `localhost.crt` - Certificate
- `localhost.pem` - Combined PEM file
- `localhost.p12` - PKCS12 keystore
- `localhost.jks` - JKS keystore (if Java is installed)
- `localhost-truststore.jks` - Truststore (if Java is installed)

### Java Usage

The script generates Java keystores that can be used in Spring Boot applications:

```properties
# application.properties
server.ssl.key-store=certs/localhost.jks
server.ssl.key-store-password=changeit
server.ssl.key-store-type=JKS
server.ssl.key-alias=localhost
```

## Usage Examples

### Parse Green Tripdata

```java
import com.bscllc.taxis.util.TripDataParser;
import com.bscllc.taxis.model.GreenTripdata;

try {
    List<GreenTripdata> trips = TripDataParser.parseGreenTripdata(
        "common/src/main/resources/green_tripdata.parquet"
    );
    
    for (GreenTripdata trip : trips) {
        System.out.println("Trip: " + trip.getTotalAmount());
    }
} catch (TripDataParserException e) {
    System.err.println("Error parsing file: " + e.getMessage());
}
```

### Parse Yellow Tripdata

```java
import com.bscllc.taxis.util.TripDataParser;
import com.bscllc.taxis.model.YellowTripdata;

try {
    List<YellowTripdata> trips = TripDataParser.parseYellowTripdata(
        "common/src/main/resources/yellow_tripdata.parquet"
    );
    
    for (YellowTripdata trip : trips) {
        System.out.println("Trip: " + trip.getTotalAmount());
    }
} catch (TripDataParserException e) {
    System.err.println("Error parsing file: " + e.getMessage());
}
```

### Check File Type

```java
File tripFile = new File("path/to/tripdata.parquet");

if (TripDataParser.isGreenTripdataFile(tripFile)) {
    System.out.println("This is a green tripdata file");
} else if (TripDataParser.isYellowTripdataFile(tripFile)) {
    System.out.println("This is a yellow tripdata file");
} else {
    System.out.println("Unknown tripdata format");
}
```

## Dependencies

### Common Module

- Apache Parquet (parquet-avro, parquet-column, parquet-hadoop)
- Hadoop Common
- JUnit 5 (for testing)

### Database Module

- Quarkus 3.6.0 (quarkus-arc, quarkus-config-yaml)
- Quarkus Micrometer Registry Prometheus
- PostgreSQL JDBC Driver 42.7.1
- SLF4J API 2.0.9
- Logback Classic 1.4.14

### Indexing Module

- Quarkus 3.6.0 (quarkus-arc, quarkus-config-yaml)
- Quarkus Micrometer Registry Prometheus
- OpenSearch Java Client 2.11.0
- Apache HttpComponents Client 4.5.14
- Jackson Databind 2.15.2
- SLF4J API 2.0.9
- Logback Classic 1.4.14

## Project Information

- **Group ID**: `com.bscllc.taxis`
- **Artifact ID**: `ai-taxi-model-helm`
- **Version**: `1.0.0-SNAPSHOT`
- **Java Version**: 21

## License

[Add your license information here]

## Contributing

[Add contributing guidelines here]

## Support

[Add support information here]
