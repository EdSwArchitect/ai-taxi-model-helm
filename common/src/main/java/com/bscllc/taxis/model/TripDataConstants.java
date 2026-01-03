package com.bscllc.taxis.model;

/**
 * Constants for taxi trip data schemas.
 * Contains JSON string representations of Parquet schemas for green and yellow trip data.
 */
public class TripDataConstants {

    /**
     * JSON string representation of the green trip data Parquet schema.
     * This schema is based on the NYC green taxi tripdata parquet file format.
     */
    public static final String GREEN_TRIPDATA_SCHEMA_JSON = """
        {
          "type": "record",
          "name": "green_tripdata",
          "fields": [
            {"name": "VendorID", "type": ["null", "int"], "default": null},
            {"name": "lpep_pickup_datetime", "type": ["null", {"type": "long", "logicalType": "timestamp-millis"}], "default": null},
            {"name": "lpep_dropoff_datetime", "type": ["null", {"type": "long", "logicalType": "timestamp-millis"}], "default": null},
            {"name": "store_and_fwd_flag", "type": ["null", "string"], "default": null},
            {"name": "RatecodeID", "type": ["null", "int"], "default": null},
            {"name": "PULocationID", "type": ["null", "int"], "default": null},
            {"name": "DOLocationID", "type": ["null", "int"], "default": null},
            {"name": "passenger_count", "type": ["null", "int"], "default": null},
            {"name": "trip_distance", "type": ["null", "double"], "default": null},
            {"name": "fare_amount", "type": ["null", "double"], "default": null},
            {"name": "extra", "type": ["null", "double"], "default": null},
            {"name": "mta_tax", "type": ["null", "double"], "default": null},
            {"name": "tip_amount", "type": ["null", "double"], "default": null},
            {"name": "tolls_amount", "type": ["null", "double"], "default": null},
            {"name": "ehail_fee", "type": ["null", "double"], "default": null},
            {"name": "improvement_surcharge", "type": ["null", "double"], "default": null},
            {"name": "total_amount", "type": ["null", "double"], "default": null},
            {"name": "payment_type", "type": ["null", "int"], "default": null},
            {"name": "trip_type", "type": ["null", "int"], "default": null},
            {"name": "congestion_surcharge", "type": ["null", "double"], "default": null}
          ]
        }
        """;

    /**
     * JSON string representation of the yellow trip data Parquet schema.
     * This schema is based on the NYC yellow taxi tripdata parquet file format.
     */
    public static final String YELLOW_TRIPDATA_SCHEMA_JSON = """
        {
          "type": "record",
          "name": "yellow_tripdata",
          "fields": [
            {"name": "VendorID", "type": ["null", "int"], "default": null},
            {"name": "tpep_pickup_datetime", "type": ["null", {"type": "long", "logicalType": "timestamp-millis"}], "default": null},
            {"name": "tpep_dropoff_datetime", "type": ["null", {"type": "long", "logicalType": "timestamp-millis"}], "default": null},
            {"name": "passenger_count", "type": ["null", "int"], "default": null},
            {"name": "trip_distance", "type": ["null", "double"], "default": null},
            {"name": "RatecodeID", "type": ["null", "int"], "default": null},
            {"name": "store_and_fwd_flag", "type": ["null", "string"], "default": null},
            {"name": "PULocationID", "type": ["null", "int"], "default": null},
            {"name": "DOLocationID", "type": ["null", "int"], "default": null},
            {"name": "payment_type", "type": ["null", "int"], "default": null},
            {"name": "fare_amount", "type": ["null", "double"], "default": null},
            {"name": "extra", "type": ["null", "double"], "default": null},
            {"name": "mta_tax", "type": ["null", "double"], "default": null},
            {"name": "tip_amount", "type": ["null", "double"], "default": null},
            {"name": "tolls_amount", "type": ["null", "double"], "default": null},
            {"name": "improvement_surcharge", "type": ["null", "double"], "default": null},
            {"name": "total_amount", "type": ["null", "double"], "default": null},
            {"name": "congestion_surcharge", "type": ["null", "double"], "default": null}
          ]
        }
        """;

    /**
     * Private constructor to prevent instantiation.
     */
    private TripDataConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }
}

