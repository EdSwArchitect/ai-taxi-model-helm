package com.bscllc.taxis.util;

import com.bscllc.taxis.model.GreenTripdata;
import com.bscllc.taxis.model.YellowTripdata;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.SimpleGroup;
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.io.MessageColumnIO;
import org.apache.parquet.io.RecordReader;
import org.apache.parquet.io.api.GroupConverter;
import org.apache.parquet.io.api.RecordMaterializer;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.Type;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for NYC taxi trip data parquet files.
 * Supports both green and yellow tripdata formats.
 */
public class TripDataParser {

    private static final String GREEN_SCHEMA_INDICATOR = "lpep_pickup_datetime";
    private static final String YELLOW_SCHEMA_INDICATOR = "tpep_pickup_datetime";
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Checks if the given file is a valid green tripdata parquet file.
     *
     * @param filePath path to the file to check
     * @return true if the file is a valid green tripdata parquet file, false otherwise
     */
    public static boolean isGreenTripdataFile(String filePath) {
        try {
            return isGreenTripdataFile(new File(filePath));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the given file is a valid green tripdata parquet file.
     *
     * @param file the file to check
     * @return true if the file is a valid green tripdata parquet file, false otherwise
     */
    public static boolean isGreenTripdataFile(File file) {
        try {
            if (!file.exists() || !file.isFile()) {
                return false;
            }
            
            MessageType schema = readParquetSchema(file);
            if (schema == null) {
                return false;
            }
            
            return hasField(schema, GREEN_SCHEMA_INDICATOR) && 
                   hasField(schema, "ehail_fee") && 
                   hasField(schema, "trip_type");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the given file is a valid yellow tripdata parquet file.
     *
     * @param filePath path to the file to check
     * @return true if the file is a valid yellow tripdata parquet file, false otherwise
     */
    public static boolean isYellowTripdataFile(String filePath) {
        try {
            return isYellowTripdataFile(new File(filePath));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the given file is a valid yellow tripdata parquet file.
     *
     * @param file the file to check
     * @return true if the file is a valid yellow tripdata parquet file, false otherwise
     */
    public static boolean isYellowTripdataFile(File file) {
        try {
            if (!file.exists() || !file.isFile()) {
                return false;
            }
            
            MessageType schema = readParquetSchema(file);
            if (schema == null) {
                return false;
            }
            
            return hasField(schema, YELLOW_SCHEMA_INDICATOR) && 
                   !hasField(schema, "ehail_fee") && 
                   !hasField(schema, "trip_type");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Parses a green tripdata parquet file and returns a list of GreenTripdata objects.
     *
     * @param filePath path to the parquet file
     * @return list of GreenTripdata objects
     * @throws TripDataParserException if the file cannot be parsed or doesn't match the green schema
     */
    public static List<GreenTripdata> parseGreenTripdata(String filePath) throws TripDataParserException {
        return parseGreenTripdata(new File(filePath));
    }

    /**
     * Parses a green tripdata parquet file and returns a list of GreenTripdata objects.
     *
     * @param file the parquet file
     * @return list of GreenTripdata objects
     * @throws TripDataParserException if the file cannot be parsed or doesn't match the green schema
     */
    public static List<GreenTripdata> parseGreenTripdata(File file) throws TripDataParserException {
        validateFile(file);
        
        if (!isGreenTripdataFile(file)) {
            throw new TripDataParserException(
                "File does not match green tripdata schema: " + file.getAbsolutePath());
        }
        
        try {
            return readGreenTripdata(file);
        } catch (IOException e) {
            throw new TripDataParserException("Error reading green tripdata file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Parses a yellow tripdata parquet file and returns a list of YellowTripdata objects.
     *
     * @param filePath path to the parquet file
     * @return list of YellowTripdata objects
     * @throws TripDataParserException if the file cannot be parsed or doesn't match the yellow schema
     */
    public static List<YellowTripdata> parseYellowTripdata(String filePath) throws TripDataParserException {
        return parseYellowTripdata(new File(filePath));
    }

    /**
     * Parses a yellow tripdata parquet file and returns a list of YellowTripdata objects.
     *
     * @param file the parquet file
     * @return list of YellowTripdata objects
     * @throws TripDataParserException if the file cannot be parsed or doesn't match the yellow schema
     */
    public static List<YellowTripdata> parseYellowTripdata(File file) throws TripDataParserException {
        validateFile(file);
        
        if (!isYellowTripdataFile(file)) {
            throw new TripDataParserException(
                "File does not match yellow tripdata schema: " + file.getAbsolutePath());
        }
        
        try {
            return readYellowTripdata(file);
        } catch (IOException e) {
            throw new TripDataParserException("Error reading yellow tripdata file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Validates that the file exists and is a valid parquet file.
     */
    private static void validateFile(File file) throws TripDataParserException {
        if (file == null) {
            throw new TripDataParserException("File cannot be null");
        }
        
        if (!file.exists()) {
            throw new TripDataParserException("File does not exist: " + file.getAbsolutePath());
        }
        
        if (!file.isFile()) {
            throw new TripDataParserException("Path is not a file: " + file.getAbsolutePath());
        }
        
        // Try to read the schema to verify it's a parquet file
        try {
            MessageType schema = readParquetSchema(file);
            if (schema == null) {
                throw new TripDataParserException("File is not a valid parquet file: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            throw new TripDataParserException("File is not a valid parquet file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Reads the schema from a parquet file.
     */
    private static MessageType readParquetSchema(File file) throws IOException {
        Configuration conf = new Configuration();
        Path path = new Path(file.toURI());
        HadoopInputFile inputFile = HadoopInputFile.fromPath(path, conf);
        
        try (ParquetFileReader reader = ParquetFileReader.open(inputFile)) {
            return reader.getFileMetaData().getSchema();
        }
    }

    /**
     * Reads green tripdata from a parquet file.
     */
    private static List<GreenTripdata> readGreenTripdata(File file) throws IOException {
        List<GreenTripdata> tripDataList = new ArrayList<>();
        Configuration conf = new Configuration();
        Path path = new Path(file.toURI());
        HadoopInputFile inputFile = HadoopInputFile.fromPath(path, conf);
        
        try (ParquetFileReader reader = ParquetFileReader.open(inputFile)) {
            MessageType schema = reader.getFileMetaData().getSchema();
            MessageColumnIO columnIO = new ColumnIOFactory().getColumnIO(schema);
            
            PageReadStore pages;
            while ((pages = reader.readNextRowGroup()) != null) {
                final long rows = pages.getRowCount();
                RecordMaterializer<Group> recordMaterializer = createGroupRecordMaterializer(schema);
                RecordReader<Group> recordReader = columnIO.getRecordReader(pages, recordMaterializer);
                
                for (int i = 0; i < rows; i++) {
                    SimpleGroup group = (SimpleGroup) recordReader.read();
                    GreenTripdata tripData = convertToGreenTripdata(group);
                    tripDataList.add(tripData);
                }
            }
        }
        
        return tripDataList;
    }

    /**
     * Reads yellow tripdata from a parquet file.
     */
    private static List<YellowTripdata> readYellowTripdata(File file) throws IOException {
        List<YellowTripdata> tripDataList = new ArrayList<>();
        Configuration conf = new Configuration();
        Path path = new Path(file.toURI());
        HadoopInputFile inputFile = HadoopInputFile.fromPath(path, conf);
        
        try (ParquetFileReader reader = ParquetFileReader.open(inputFile)) {
            MessageType schema = reader.getFileMetaData().getSchema();
            MessageColumnIO columnIO = new ColumnIOFactory().getColumnIO(schema);
            
            PageReadStore pages;
            while ((pages = reader.readNextRowGroup()) != null) {
                final long rows = pages.getRowCount();
                RecordMaterializer<Group> recordMaterializer = createGroupRecordMaterializer(schema);
                RecordReader<Group> recordReader = columnIO.getRecordReader(pages, recordMaterializer);
                
                for (int i = 0; i < rows; i++) {
                    SimpleGroup group = (SimpleGroup) recordReader.read();
                    YellowTripdata tripData = convertToYellowTripdata(group);
                    tripDataList.add(tripData);
                }
            }
        }
        
        return tripDataList;
    }

    /**
     * Converts a Parquet Group to a GreenTripdata object.
     */
    private static GreenTripdata convertToGreenTripdata(SimpleGroup group) {
        GreenTripdata tripData = new GreenTripdata();
        
        tripData.setVendorId(getIntegerValue(group, "VendorID"));
        tripData.setLpepPickupDatetime(getDateTimeValue(group, "lpep_pickup_datetime"));
        tripData.setLpepDropoffDatetime(getDateTimeValue(group, "lpep_dropoff_datetime"));
        tripData.setStoreAndFwdFlag(getStringValue(group, "store_and_fwd_flag"));
        tripData.setRatecodeId(getIntegerValue(group, "RatecodeID"));
        tripData.setPuLocationId(getIntegerValue(group, "PULocationID"));
        tripData.setDoLocationId(getIntegerValue(group, "DOLocationID"));
        tripData.setPassengerCount(getIntegerValue(group, "passenger_count"));
        tripData.setTripDistance(getDoubleValue(group, "trip_distance"));
        tripData.setFareAmount(getDoubleValue(group, "fare_amount"));
        tripData.setExtra(getDoubleValue(group, "extra"));
        tripData.setMtaTax(getDoubleValue(group, "mta_tax"));
        tripData.setTipAmount(getDoubleValue(group, "tip_amount"));
        tripData.setTollsAmount(getDoubleValue(group, "tolls_amount"));
        tripData.setEhailFee(getDoubleValue(group, "ehail_fee"));
        tripData.setImprovementSurcharge(getDoubleValue(group, "improvement_surcharge"));
        tripData.setTotalAmount(getDoubleValue(group, "total_amount"));
        tripData.setPaymentType(getIntegerValue(group, "payment_type"));
        tripData.setTripType(getIntegerValue(group, "trip_type"));
        tripData.setCongestionSurcharge(getDoubleValue(group, "congestion_surcharge"));
        
        return tripData;
    }

    /**
     * Converts a Parquet Group to a YellowTripdata object.
     */
    private static YellowTripdata convertToYellowTripdata(SimpleGroup group) {
        YellowTripdata tripData = new YellowTripdata();
        
        tripData.setVendorId(getIntegerValue(group, "VendorID"));
        tripData.setTpepPickupDatetime(getDateTimeValue(group, "tpep_pickup_datetime"));
        tripData.setTpepDropoffDatetime(getDateTimeValue(group, "tpep_dropoff_datetime"));
        tripData.setPassengerCount(getIntegerValue(group, "passenger_count"));
        tripData.setTripDistance(getDoubleValue(group, "trip_distance"));
        tripData.setRatecodeId(getIntegerValue(group, "RatecodeID"));
        tripData.setStoreAndFwdFlag(getStringValue(group, "store_and_fwd_flag"));
        tripData.setPuLocationId(getIntegerValue(group, "PULocationID"));
        tripData.setDoLocationId(getIntegerValue(group, "DOLocationID"));
        tripData.setPaymentType(getIntegerValue(group, "payment_type"));
        tripData.setFareAmount(getDoubleValue(group, "fare_amount"));
        tripData.setExtra(getDoubleValue(group, "extra"));
        tripData.setMtaTax(getDoubleValue(group, "mta_tax"));
        tripData.setTipAmount(getDoubleValue(group, "tip_amount"));
        tripData.setTollsAmount(getDoubleValue(group, "tolls_amount"));
        tripData.setImprovementSurcharge(getDoubleValue(group, "improvement_surcharge"));
        tripData.setTotalAmount(getDoubleValue(group, "total_amount"));
        tripData.setCongestionSurcharge(getDoubleValue(group, "congestion_surcharge"));
        
        return tripData;
    }

    /**
     * Helper method to get an Integer value from a Parquet Group.
     */
    private static Integer getIntegerValue(SimpleGroup group, String fieldName) {
        try {
            if (group.getFieldRepetitionCount(fieldName) == 0) {
                return null;
            }
            return group.getInteger(fieldName, 0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Helper method to get a Double value from a Parquet Group.
     */
    private static Double getDoubleValue(SimpleGroup group, String fieldName) {
        try {
            if (group.getFieldRepetitionCount(fieldName) == 0) {
                return null;
            }
            return group.getDouble(fieldName, 0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Helper method to get a String value from a Parquet Group.
     */
    private static String getStringValue(SimpleGroup group, String fieldName) {
        try {
            if (group.getFieldRepetitionCount(fieldName) == 0) {
                return null;
            }
            return group.getString(fieldName, 0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Helper method to check if a schema contains a field with the given name.
     * Performs case-insensitive comparison to handle variations in field name casing.
     */
    private static boolean hasField(MessageType schema, String fieldName) {
        if (schema == null || fieldName == null) {
            return false;
        }
        String lowerFieldName = fieldName.toLowerCase();
        for (Type field : schema.getFields()) {
            if (field.getName().toLowerCase().equals(lowerFieldName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a RecordMaterializer for Group objects using GroupRecordConverter.
     * GroupRecordConverter already extends RecordMaterializer<Group> and has a constructor
     * that takes MessageType, so we can use it directly.
     */
    private static RecordMaterializer<Group> createGroupRecordMaterializer(MessageType schema) {
        return new GroupRecordConverter(schema);
    }
    
    /**
     * Helper method to get a LocalDateTime value from a Parquet Group.
     */
    private static LocalDateTime getDateTimeValue(SimpleGroup group, String fieldName) {
        try {
            if (group.getFieldRepetitionCount(fieldName) == 0) {
                return null;
            }
            String dateTimeStr = group.getString(fieldName, 0);
            if (dateTimeStr == null || dateTimeStr.isEmpty()) {
                return null;
            }
            // Handle different datetime formats
            if (dateTimeStr.contains("T")) {
                return LocalDateTime.parse(dateTimeStr.replace("T", " ").substring(0, 19), DATE_TIME_FORMATTER);
            }
            return LocalDateTime.parse(dateTimeStr.substring(0, 19), DATE_TIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}

