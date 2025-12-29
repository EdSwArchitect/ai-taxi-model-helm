package com.bscllc.taxis.util;

/**
 * Exception thrown when parsing trip data files fails.
 * This can occur when:
 * - The file is not a valid parquet file
 * - The file does not match the expected green or yellow tripdata schema
 */
public class TripDataParserException extends Exception {

    public TripDataParserException(String message) {
        super(message);
    }

    public TripDataParserException(String message, Throwable cause) {
        super(message, cause);
    }
}

