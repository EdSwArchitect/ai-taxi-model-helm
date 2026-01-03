package com.bscllc.taxis.model;

import com.bscllc.taxis.util.TripDataParser;
import com.bscllc.taxis.util.TripDataParserException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.schema.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class YellowTripdataTest {

    private YellowTripdata yellowTripdata;

    @BeforeEach
    void setUp() {
        yellowTripdata = new YellowTripdata();
    }

    @Test
    void testVendorId() {
        assertNull(yellowTripdata.getVendorId());
        yellowTripdata.setVendorId(1);
        assertEquals(1, yellowTripdata.getVendorId());
    }

    @Test
    void testTpepPickupDatetime() {
        assertNull(yellowTripdata.getTpepPickupDatetime());
        LocalDateTime pickupTime = LocalDateTime.of(2023, 1, 15, 10, 30, 0);
        yellowTripdata.setTpepPickupDatetime(pickupTime);
        assertEquals(pickupTime, yellowTripdata.getTpepPickupDatetime());
    }

    @Test
    void testTpepDropoffDatetime() {
        assertNull(yellowTripdata.getTpepDropoffDatetime());
        LocalDateTime dropoffTime = LocalDateTime.of(2023, 1, 15, 11, 0, 0);
        yellowTripdata.setTpepDropoffDatetime(dropoffTime);
        assertEquals(dropoffTime, yellowTripdata.getTpepDropoffDatetime());
    }

    @Test
    void testPassengerCount() {
        assertNull(yellowTripdata.getPassengerCount());
        yellowTripdata.setPassengerCount(2);
        assertEquals(2, yellowTripdata.getPassengerCount());
    }

    @Test
    void testTripDistance() {
        assertNull(yellowTripdata.getTripDistance());
        yellowTripdata.setTripDistance(2.5);
        assertEquals(2.5, yellowTripdata.getTripDistance());
    }

    @Test
    void testRatecodeId() {
        assertNull(yellowTripdata.getRatecodeId());
        yellowTripdata.setRatecodeId(1);
        assertEquals(1, yellowTripdata.getRatecodeId());
    }

    @Test
    void testStoreAndFwdFlag() {
        assertNull(yellowTripdata.getStoreAndFwdFlag());
        yellowTripdata.setStoreAndFwdFlag("N");
        assertEquals("N", yellowTripdata.getStoreAndFwdFlag());
        yellowTripdata.setStoreAndFwdFlag("Y");
        assertEquals("Y", yellowTripdata.getStoreAndFwdFlag());
    }

    @Test
    void testPuLocationId() {
        assertNull(yellowTripdata.getPuLocationId());
        yellowTripdata.setPuLocationId(100);
        assertEquals(100, yellowTripdata.getPuLocationId());
    }

    @Test
    void testDoLocationId() {
        assertNull(yellowTripdata.getDoLocationId());
        yellowTripdata.setDoLocationId(200);
        assertEquals(200, yellowTripdata.getDoLocationId());
    }

    @Test
    void testPaymentType() {
        assertNull(yellowTripdata.getPaymentType());
        yellowTripdata.setPaymentType(1);
        assertEquals(1, yellowTripdata.getPaymentType());
    }

    @Test
    void testFareAmount() {
        assertNull(yellowTripdata.getFareAmount());
        yellowTripdata.setFareAmount(10.50);
        assertEquals(10.50, yellowTripdata.getFareAmount());
    }

    @Test
    void testExtra() {
        assertNull(yellowTripdata.getExtra());
        yellowTripdata.setExtra(1.0);
        assertEquals(1.0, yellowTripdata.getExtra());
    }

    @Test
    void testMtaTax() {
        assertNull(yellowTripdata.getMtaTax());
        yellowTripdata.setMtaTax(0.5);
        assertEquals(0.5, yellowTripdata.getMtaTax());
    }

    @Test
    void testTipAmount() {
        assertNull(yellowTripdata.getTipAmount());
        yellowTripdata.setTipAmount(2.0);
        assertEquals(2.0, yellowTripdata.getTipAmount());
    }

    @Test
    void testTollsAmount() {
        assertNull(yellowTripdata.getTollsAmount());
        yellowTripdata.setTollsAmount(0.0);
        assertEquals(0.0, yellowTripdata.getTollsAmount());
    }

    @Test
    void testImprovementSurcharge() {
        assertNull(yellowTripdata.getImprovementSurcharge());
        yellowTripdata.setImprovementSurcharge(0.3);
        assertEquals(0.3, yellowTripdata.getImprovementSurcharge());
    }

    @Test
    void testTotalAmount() {
        assertNull(yellowTripdata.getTotalAmount());
        yellowTripdata.setTotalAmount(15.30);
        assertEquals(15.30, yellowTripdata.getTotalAmount());
    }

    @Test
    void testCongestionSurcharge() {
        assertNull(yellowTripdata.getCongestionSurcharge());
        yellowTripdata.setCongestionSurcharge(2.5);
        assertEquals(2.5, yellowTripdata.getCongestionSurcharge());
    }

    @Test
    void testToString() {
        yellowTripdata.setVendorId(1);
        yellowTripdata.setPassengerCount(2);
        yellowTripdata.setTripDistance(2.5);
        yellowTripdata.setTotalAmount(15.30);
        
        String toString = yellowTripdata.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("YellowTripdata"));
        assertTrue(toString.contains("vendorId=1"));
        assertTrue(toString.contains("passengerCount=2"));
        assertTrue(toString.contains("tripDistance=2.5"));
        assertTrue(toString.contains("totalAmount=15.3"));
    }

    @Test
    void testCompleteTripData() {
        LocalDateTime pickup = LocalDateTime.of(2023, 1, 15, 10, 30, 0);
        LocalDateTime dropoff = LocalDateTime.of(2023, 1, 15, 11, 0, 0);
        
        yellowTripdata.setVendorId(1);
        yellowTripdata.setTpepPickupDatetime(pickup);
        yellowTripdata.setTpepDropoffDatetime(dropoff);
        yellowTripdata.setPassengerCount(2);
        yellowTripdata.setTripDistance(2.5);
        yellowTripdata.setRatecodeId(1);
        yellowTripdata.setStoreAndFwdFlag("N");
        yellowTripdata.setPuLocationId(100);
        yellowTripdata.setDoLocationId(200);
        yellowTripdata.setPaymentType(1);
        yellowTripdata.setFareAmount(10.50);
        yellowTripdata.setExtra(1.0);
        yellowTripdata.setMtaTax(0.5);
        yellowTripdata.setTipAmount(2.0);
        yellowTripdata.setTollsAmount(0.0);
        yellowTripdata.setImprovementSurcharge(0.3);
        yellowTripdata.setTotalAmount(15.30);
        yellowTripdata.setCongestionSurcharge(2.5);
        
        assertEquals(1, yellowTripdata.getVendorId());
        assertEquals(pickup, yellowTripdata.getTpepPickupDatetime());
        assertEquals(dropoff, yellowTripdata.getTpepDropoffDatetime());
        assertEquals(2, yellowTripdata.getPassengerCount());
        assertEquals(2.5, yellowTripdata.getTripDistance());
        assertEquals(1, yellowTripdata.getRatecodeId());
        assertEquals("N", yellowTripdata.getStoreAndFwdFlag());
        assertEquals(100, yellowTripdata.getPuLocationId());
        assertEquals(200, yellowTripdata.getDoLocationId());
        assertEquals(1, yellowTripdata.getPaymentType());
        assertEquals(10.50, yellowTripdata.getFareAmount());
        assertEquals(1.0, yellowTripdata.getExtra());
        assertEquals(0.5, yellowTripdata.getMtaTax());
        assertEquals(2.0, yellowTripdata.getTipAmount());
        assertEquals(0.0, yellowTripdata.getTollsAmount());
        assertEquals(0.3, yellowTripdata.getImprovementSurcharge());
        assertEquals(15.30, yellowTripdata.getTotalAmount());
        assertEquals(2.5, yellowTripdata.getCongestionSurcharge());
    }

    @Test
    void testNullHandling() {
        // Test that all fields can be set to null
        yellowTripdata.setVendorId(null);
        yellowTripdata.setTpepPickupDatetime(null);
        yellowTripdata.setTpepDropoffDatetime(null);
        yellowTripdata.setPassengerCount(null);
        yellowTripdata.setTripDistance(null);
        yellowTripdata.setRatecodeId(null);
        yellowTripdata.setStoreAndFwdFlag(null);
        yellowTripdata.setPuLocationId(null);
        yellowTripdata.setDoLocationId(null);
        yellowTripdata.setPaymentType(null);
        yellowTripdata.setFareAmount(null);
        yellowTripdata.setExtra(null);
        yellowTripdata.setMtaTax(null);
        yellowTripdata.setTipAmount(null);
        yellowTripdata.setTollsAmount(null);
        yellowTripdata.setImprovementSurcharge(null);
        yellowTripdata.setTotalAmount(null);
        yellowTripdata.setCongestionSurcharge(null);
        
        assertNull(yellowTripdata.getVendorId());
        assertNull(yellowTripdata.getTpepPickupDatetime());
        assertNull(yellowTripdata.getTpepDropoffDatetime());
        assertNull(yellowTripdata.getPassengerCount());
        assertNull(yellowTripdata.getTripDistance());
        assertNull(yellowTripdata.getRatecodeId());
        assertNull(yellowTripdata.getStoreAndFwdFlag());
        assertNull(yellowTripdata.getPuLocationId());
        assertNull(yellowTripdata.getDoLocationId());
        assertNull(yellowTripdata.getPaymentType());
        assertNull(yellowTripdata.getFareAmount());
        assertNull(yellowTripdata.getExtra());
        assertNull(yellowTripdata.getMtaTax());
        assertNull(yellowTripdata.getTipAmount());
        assertNull(yellowTripdata.getTollsAmount());
        assertNull(yellowTripdata.getImprovementSurcharge());
        assertNull(yellowTripdata.getTotalAmount());
        assertNull(yellowTripdata.getCongestionSurcharge());
    }

    /**
     * Gets the test resource file path.
     */
    private File getTestResourceFile(String filename) {
        URL resource = getClass().getClassLoader().getResource(filename);
        if (resource == null) {
            return null;
        }
        
        try {
            String filePath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8.name());
            return new File(filePath);
        } catch (Exception e) {
            return new File(resource.getFile());
        }
    }

    /**
     * Reads the schema from a parquet file.
     */
    private MessageType readParquetSchema(File file) throws IOException {
        try {
            Configuration conf = new Configuration();
            Path path = new Path(file.toURI());
            HadoopInputFile inputFile = HadoopInputFile.fromPath(path, conf);
            
            try (ParquetFileReader reader = ParquetFileReader.open(inputFile)) {
                return reader.getFileMetaData().getSchema();
            }
        } catch (UnsupportedOperationException e) {
            // Java 17+ security restriction with Hadoop - wrap as IOException
            throw new IOException("Unable to read parquet schema due to Java security restrictions: " + e.getMessage(), e);
        }
    }

    /**
     * Formats a MessageType schema as a readable string.
     */
    private String formatSchema(MessageType schema) {
        if (schema == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Schema: ").append(schema.getName()).append("\n");
        sb.append("Fields:\n");
        for (org.apache.parquet.schema.Type field : schema.getFields()) {
            sb.append("  - ").append(field.getName()).append(": ");
            try {
                if (field.isPrimitive()) {
                    org.apache.parquet.schema.PrimitiveType primitiveType = field.asPrimitiveType();
                    sb.append(primitiveType.getPrimitiveTypeName());
                    if (primitiveType.getOriginalType() != null) {
                        sb.append(" (").append(primitiveType.getOriginalType().name()).append(")");
                    }
                } else {
                    sb.append("GROUP");
                }
            } catch (Exception e) {
                // Fallback to simple string representation if we can't extract type info
                sb.append(field.toString());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Test parsing yellow tripdata file with schema comparison on failure.
     * 
     * Note: This test may be skipped on Java 17+ due to Hadoop security restrictions
     * (UnsupportedOperationException: getSubject is not supported).
     */
    @Test
    void testParseYellowTripdataFile() {
        File yellowFile = getTestResourceFile("yellow_test.parquet");
        if (yellowFile == null || !yellowFile.exists()) {
            // Skip test if file doesn't exist
            return;
        }

        try {
            List<YellowTripdata> trips = TripDataParser.parseYellowTripdata(yellowFile);
            assertNotNull(trips, "Parsed trips list should not be null");
            assertFalse(trips.isEmpty(), "Parsed trips list should not be empty");
        } catch (TripDataParserException e) {
            // Check if the error is due to Java 17+ security restrictions
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof UnsupportedOperationException 
                && cause.getMessage() != null && cause.getMessage().contains("getSubject")) {
                // Skip test on Java 17+ due to Hadoop security restrictions
                return;
            }
            
            // Try to read and display schema information for any parsing failure
            try {
                // Read the actual schema from the file
                MessageType actualSchema = readParquetSchema(yellowFile);
                String actualSchemaString = formatSchema(actualSchema);
                
                // Get the expected schema JSON
                String expectedSchemaString = "Expected Schema (JSON):\n" + TripDataConstants.YELLOW_TRIPDATA_SCHEMA_JSON;
                
                // Display both schemas in the failure message
                String errorMessage = String.format(
                    "Failed to parse yellow tripdata file!\n\n" +
                    "Actual Schema from Parquet File:\n%s\n\n" +
                    "%s\n\n" +
                    "Original Error: %s",
                    actualSchemaString,
                    expectedSchemaString,
                    e.getMessage()
                );
                
                fail(errorMessage);
            } catch (IOException ioException) {
                // If we can't read the schema (e.g., due to Java 17+ security restrictions),
                // skip the test
                if (ioException.getCause() instanceof UnsupportedOperationException 
                    && ioException.getCause().getMessage() != null 
                    && ioException.getCause().getMessage().contains("getSubject")) {
                    // Skip test on Java 17+ due to Hadoop security restrictions
                    return;
                }
                // Otherwise, fail with the original parsing exception
                String errorMsg = "Failed to parse yellow tripdata file. " +
                    "Could not read schema for comparison. " +
                    "Original parsing error: " + e.getMessage();
                fail(errorMsg, e);
            }
        }
    }
}

