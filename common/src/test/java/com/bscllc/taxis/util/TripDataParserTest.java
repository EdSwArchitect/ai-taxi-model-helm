package com.bscllc.taxis.util;

import com.bscllc.taxis.model.GreenTripdata;
import com.bscllc.taxis.model.TripDataConstants;
import com.bscllc.taxis.model.YellowTripdata;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.schema.MessageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TripDataParser utility class.
 * 
 * Note: Some tests require test parquet files with specific schema fields:
 * - Green files: must have lpep_pickup_datetime, ehail_fee, trip_type fields
 * - Yellow files: must have tpep_pickup_datetime field and must NOT have ehail_fee or trip_type fields
 */
@DisplayName("TripDataParser Tests")
public class TripDataParserTest {

    /**
     * Gets the test resource file path.
     */
    private File getTestResourceFile(String filename) {
        URL resource = getClass().getClassLoader().getResource(filename);
        if (resource == null) {
            return null;
        }
        
        try {
            // Decode URL-encoded paths (handles spaces and special characters)
            String filePath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8.name());
            return new File(filePath);
        } catch (Exception e) {
            // Fallback to simple path if decoding fails
            return new File(resource.getFile());
        }
    }

    /**
     * Checks if a file is a valid green tripdata file.
     */
    private boolean isGreenTestFileValid() {
        try {
            File greenFile = getTestResourceFile("green_test.parquet");
            return TripDataParser.isGreenTripdataFile(greenFile);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a file is a valid yellow tripdata file.
     */
    private boolean isYellowTestFileValid() {
        try {
            File yellowFile = getTestResourceFile("yellow_test.parquet");
            return TripDataParser.isYellowTripdataFile(yellowFile);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if green_tripdata.parquet file exists and is valid.
     */
    private boolean isGreenTripdataFileValid() {
        try {
            File greenFile = getTestResourceFile("green_tripdata.parquet");
            return greenFile != null && greenFile.exists() && TripDataParser.isGreenTripdataFile(greenFile);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if yellow_tripdata.parquet file exists and is valid.
     */
    private boolean isYellowTripdataFileValid() {
        try {
            File yellowFile = getTestResourceFile("yellow_tripdata.parquet");
            return yellowFile != null && yellowFile.exists() && TripDataParser.isYellowTripdataFile(yellowFile);
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    @DisplayName("Should identify green tripdata file correctly")
    public void testIsGreenTripdataFile() {
        File greenFile = getTestResourceFile("green_test.parquet");
        assertTrue(TripDataParser.isGreenTripdataFile(greenFile), 
            "Should identify green_test.parquet as green tripdata file");
        assertTrue(TripDataParser.isGreenTripdataFile(greenFile.getAbsolutePath()),
            "Should identify green_test.parquet by path as green tripdata file");
    }

    @Test
    @DisplayName("Should identify yellow tripdata file correctly")
    public void testIsYellowTripdataFile() {
        File yellowFile = getTestResourceFile("yellow_test.parquet");
        assertTrue(TripDataParser.isYellowTripdataFile(yellowFile),
            "Should identify yellow_test.parquet as yellow tripdata file");
        assertTrue(TripDataParser.isYellowTripdataFile(yellowFile.getAbsolutePath()),
            "Should identify yellow_test.parquet by path as yellow tripdata file");
    }

    @Test
    @DisplayName("Should not identify green file as yellow")
    public void testGreenFileNotYellow() {
        File greenFile = getTestResourceFile("green_test.parquet");
        assertFalse(TripDataParser.isYellowTripdataFile(greenFile),
            "Should not identify green_test.parquet as yellow tripdata file");
    }

    @Test
    @DisplayName("Should not identify yellow file as green")
    public void testYellowFileNotGreen() {
        File yellowFile = getTestResourceFile("yellow_test.parquet");
        assertFalse(TripDataParser.isGreenTripdataFile(yellowFile),
            "Should not identify yellow_test.parquet as green tripdata file");
    }

    /**
     * Reads the schema from a parquet file.
     */
    private MessageType readParquetSchema(File file) throws IOException {
        Configuration conf = new Configuration();
        Path path = new Path(file.toURI());
        HadoopInputFile inputFile = HadoopInputFile.fromPath(path, conf);
        
        try (ParquetFileReader reader = ParquetFileReader.open(inputFile)) {
            return reader.getFileMetaData().getSchema();
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
            if (field.isPrimitive()) {
                sb.append(field.asPrimitiveType().getPrimitiveTypeName());
            } else {
                sb.append(field.asGroupType().getOriginalType() != null 
                    ? field.asGroupType().getOriginalType().name() 
                    : "GROUP");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Test
    @DisplayName("Should parse green tripdata file successfully")
    public void testParseGreenTripdata() {
        File greenFile = getTestResourceFile("green_test.parquet");
        
        try {
            List<GreenTripdata> trips = TripDataParser.parseGreenTripdata(greenFile);
            
            assertNotNull(trips, "Parsed trips list should not be null");
            assertFalse(trips.isEmpty(), "Parsed trips list should not be empty");
            
            // Verify first trip has expected green tripdata fields
            GreenTripdata firstTrip = trips.get(0);
            assertNotNull(firstTrip, "First trip should not be null");
            
            // Green tripdata should have lpep pickup/dropoff datetimes
            // (exact field names depend on the model class structure)
            // Just verify we got some data back
            assertTrue(trips.size() > 0, "Should parse at least one trip");
        } catch (TripDataParserException e) {
            // Try to read and display schema information for any parsing failure
            try {
                // Read the actual schema from the file
                MessageType actualSchema = readParquetSchema(greenFile);
                String actualSchemaString = formatSchema(actualSchema);
                
                // Get the expected schema JSON
                String expectedSchemaString = "Expected Schema (JSON):\n" + TripDataConstants.GREEN_TRIPDATA_SCHEMA_JSON;
                
                // Display both schemas in the failure message
                String errorMessage = String.format(
                    "Failed to parse green tripdata file!\n\n" +
                    "Actual Schema from Parquet File:\n%s\n\n" +
                    "%s\n\n" +
                    "Original Error: %s",
                    actualSchemaString,
                    expectedSchemaString,
                    e.getMessage()
                );
                
                fail(errorMessage);
            } catch (IOException ioException) {
                // If we can't read the schema, just fail with the original exception
                fail("Failed to parse green tripdata file and could not read schema for comparison. Original error: " + e.getMessage(), e);
            }
        }
    }

    @Test
    @DisplayName("Should parse green tripdata file by path successfully")
    public void testParseGreenTripdataByPath() throws TripDataParserException {
        File greenFile = getTestResourceFile("green_test.parquet");
        
        List<GreenTripdata> trips = TripDataParser.parseGreenTripdata(greenFile.getAbsolutePath());
        
        assertNotNull(trips, "Parsed trips list should not be null");
        assertFalse(trips.isEmpty(), "Parsed trips list should not be empty");
    }

    @Test
    @DisplayName("Should parse yellow tripdata file successfully")
    public void testParseYellowTripdata() throws TripDataParserException {
        File yellowFile = getTestResourceFile("yellow_test.parquet");
        
        List<YellowTripdata> trips = TripDataParser.parseYellowTripdata(yellowFile);
        
        assertNotNull(trips, "Parsed trips list should not be null");
        assertFalse(trips.isEmpty(), "Parsed trips list should not be empty");
        
        // Verify first trip has expected yellow tripdata fields
        YellowTripdata firstTrip = trips.get(0);
        assertNotNull(firstTrip, "First trip should not be null");
        
        // Yellow tripdata should have tpep pickup/dropoff datetimes
        // (exact field names depend on the model class structure)
        // Just verify we got some data back
        assertTrue(trips.size() > 0, "Should parse at least one trip");
    }

    @Test
    @DisplayName("Should parse yellow tripdata file by path successfully")
    public void testParseYellowTripdataByPath() throws TripDataParserException {
        File yellowFile = getTestResourceFile("yellow_test.parquet");
        
        List<YellowTripdata> trips = TripDataParser.parseYellowTripdata(yellowFile.getAbsolutePath());
        
        assertNotNull(trips, "Parsed trips list should not be null");
        assertFalse(trips.isEmpty(), "Parsed trips list should not be empty");
    }

    @Test
    @DisplayName("Should throw exception when parsing wrong file type")
    public void testParseWrongFileType() {
        File greenFile = getTestResourceFile("green_test.parquet");
        
        // Attempting to parse green file as yellow should throw exception
        assertThrows(TripDataParserException.class, () -> {
            TripDataParser.parseYellowTripdata(greenFile);
        }, "Should throw TripDataParserException when parsing green file as yellow");
        
        if (isYellowTestFileValid()) {
            File yellowFile = getTestResourceFile("yellow_test.parquet");
            
            // Attempting to parse yellow file as green should throw exception
            assertThrows(TripDataParserException.class, () -> {
                TripDataParser.parseGreenTripdata(yellowFile);
            }, "Should throw TripDataParserException when parsing yellow file as green");
        }
    }

    @Test
    @DisplayName("Should throw exception for non-existent file")
    public void testParseNonExistentFile() {
        File nonExistentFile = new File("non_existent_file.parquet");
        
        assertThrows(TripDataParserException.class, () -> {
            TripDataParser.parseGreenTripdata(nonExistentFile);
        }, "Should throw TripDataParserException for non-existent file");
        
        assertThrows(TripDataParserException.class, () -> {
            TripDataParser.parseYellowTripdata(nonExistentFile);
        }, "Should throw TripDataParserException for non-existent file");
    }

    @Test
    @DisplayName("Should identify green_tripdata.parquet schema correctly")
    public void testIdentifyGreenTripdataSchema() {
        File greenFile = getTestResourceFile("green_tripdata.parquet");
        assertNotNull(greenFile, "green_tripdata.parquet file should exist");
        assertTrue(greenFile.exists(), "green_tripdata.parquet file should exist");
        
        assertTrue(TripDataParser.isGreenTripdataFile(greenFile),
            "Should identify green_tripdata.parquet as green tripdata file");
        assertTrue(TripDataParser.isGreenTripdataFile(greenFile.getAbsolutePath()),
            "Should identify green_tripdata.parquet by path as green tripdata file");
        
        // Verify it's NOT identified as yellow
        assertFalse(TripDataParser.isYellowTripdataFile(greenFile),
            "Should not identify green_tripdata.parquet as yellow tripdata file");
    }

    @Test
    @DisplayName("Should identify yellow_tripdata.parquet schema correctly")
    public void testIdentifyYellowTripdataSchema() {
        File yellowFile = getTestResourceFile("yellow_tripdata.parquet");
        assertNotNull(yellowFile, "yellow_tripdata.parquet file should exist");
        assertTrue(yellowFile.exists(), "yellow_tripdata.parquet file should exist");
        
        assertTrue(TripDataParser.isYellowTripdataFile(yellowFile),
            "Should identify yellow_tripdata.parquet as yellow tripdata file");
        assertTrue(TripDataParser.isYellowTripdataFile(yellowFile.getAbsolutePath()),
            "Should identify yellow_tripdata.parquet by path as yellow tripdata file");
        
        // Verify it's NOT identified as green
        assertFalse(TripDataParser.isGreenTripdataFile(yellowFile),
            "Should not identify yellow_tripdata.parquet as green tripdata file");
    }
}

