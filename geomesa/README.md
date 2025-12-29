# GeoMesa Simple Feature Types (SFT)

This directory contains GeoMesa Simple Feature Type (SFT) definitions for NYC taxi trip data.

## Files

- `sfts/green-tripdata.sft.conf` - SFT definition for green taxi trip data
- `sfts/yellow-tripdata.sft.conf` - SFT definition for yellow taxi trip data

## Schema Overview

Both SFTs include:

### Common Attributes
- **vendorId** - Taxi vendor identifier
- **pickup/dropoff datetime** - Trip timestamps (indexed)
- **puLocationId/doLocationId** - Pickup and dropoff location IDs
- **passengerCount** - Number of passengers
- **tripDistance** - Distance traveled
- **fareAmount** - Base fare amount
- **totalAmount** - Total trip cost
- **paymentType** - Payment method
- **pickupGeometry** - Point geometry for pickup location (default geometry, SRID 4326)
- **dropoffGeometry** - Point geometry for dropoff location (SRID 4326)
- **tripLine** - LineString geometry representing the trip path

### Green Tripdata Specific
- **ehailFee** - E-hail fee (green taxi specific)
- **tripType** - Trip type classification
- **lpepPickupDatetime/lpepDropoffDatetime** - Timestamp fields

### Yellow Tripdata Specific
- **tpepPickupDatetime/tpepDropoffDatetime** - Timestamp fields

## Geometry Fields

The SFTs include three geometry fields:

1. **pickupGeometry** (Point) - Default geometry, indexed, SRID 4326 (WGS84)
2. **dropoffGeometry** (Point) - Indexed, SRID 4326 (WGS84)
3. **tripLine** (LineString) - Optional line connecting pickup and dropoff points

## Indexing

- **Date fields** (pickup/dropoff datetime) are indexed for temporal queries
- **Geometry fields** (pickupGeometry, dropoffGeometry) are indexed for spatial queries
- **Numeric fields** (amounts, distances, counts) are indexed for range queries
- **Categorical fields** (vendorId, locationIds, paymentType) are indexed for filtering

## Usage

### Creating the SFT in GeoMesa

```bash
# For green tripdata
geomesa create-schema -c catalog -f green-tripdata -s geomesa/sfts/green-tripdata.sft.conf

# For yellow tripdata
geomesa create-schema -c catalog -f yellow-tripdata -s geomesa/sfts/yellow-tripdata.sft.conf
```

### Using with GeoMesa Java API

```java
import org.locationtech.geomesa.utils.interop.SimpleFeatureTypes;

// Load SFT from config file
SimpleFeatureType sft = SimpleFeatureTypes.createType(
    "green-tripdata",
    SimpleFeatureTypes.Configs.load("geomesa/sfts/green-tripdata.sft.conf")
        .getConfig("green-tripdata")
);
```

### Converting TripData to SimpleFeature

You'll need to:
1. Convert location IDs to actual coordinates (using a location lookup table)
2. Create Point geometries for pickup and dropoff locations
3. Optionally create LineString geometry for the trip path
4. Map all attributes to the SimpleFeature

## Notes

- **SRID 4326** (WGS84) is used for all geometries
- **Default geometry** is set to `pickupGeometry` for spatial indexing
- **Date fields** are configured as the default time field (`geomesa.index.dtg`)
- **Statistics** are enabled for better query performance
- Location IDs (puLocationId, doLocationId) need to be converted to actual coordinates using NYC taxi zone data

## Location ID to Coordinate Mapping

NYC taxi data uses location IDs that correspond to taxi zones. To populate the geometry fields, you'll need:

1. A mapping table/dataset of location IDs to coordinates (centroid of taxi zones)
2. Conversion logic to map location IDs to Point geometries
3. Optionally, actual trip paths if available

Example location lookup:
```java
// Pseudo-code for location conversion
Point pickupPoint = locationIdToPoint(tripData.getPuLocationId());
Point dropoffPoint = locationIdToPoint(tripData.getDoLocationId());
LineString tripPath = createLineString(pickupPoint, dropoffPoint);
```

