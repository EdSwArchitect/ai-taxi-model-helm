package com.bscllc.taxis.app;

import com.bscllc.taxis.config.ProcessingConfig;
import com.bscllc.taxis.model.GreenTripdata;
import com.bscllc.taxis.model.YellowTripdata;
import com.bscllc.taxis.service.DatabaseService;
import com.bscllc.taxis.util.Monitor;
import com.bscllc.taxis.util.TripDataParser;
import com.bscllc.taxis.util.TripDataParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;

/**
 * Service for processing parquet files from the input directory and storing them in the database.
 */
@ApplicationScoped
public class FileProcessingService {
    
    private static final Logger LOG = LoggerFactory.getLogger(FileProcessingService.class);
    
    @Inject
    ProcessingConfig processingConfig;
    
    @Inject
    DatabaseService databaseService;
    
    @Inject
    MetricsService metricsService;
    
    private Monitor monitor;
    
    @PostConstruct
    void init() {
        try {
            // Create directories if they don't exist
            createDirectories();
            
            // Create and configure the monitor
            monitor = Monitor.builder()
                    .directory(processingConfig.inputDirectory())
                    .filePattern(".*\\.parquet$")
                    .scanPeriod(processingConfig.monitorPeriodMs())
                    .onFileAdded(this::processFile)
                    .build();
            
            // Start monitoring
            monitor.start();
            LOG.info("File processing service started. Monitoring directory: " + processingConfig.inputDirectory());
        } catch (IOException e) {
            LOG.error("Failed to initialize file processing service", e);
            throw new RuntimeException("Failed to initialize file processing service", e);
        }
    }
    
    @PreDestroy
    void cleanup() {
        if (monitor != null && monitor.isRunning()) {
            monitor.stop();
            LOG.info("File processing service stopped");
        }
        if (databaseService != null) {
            try {
                databaseService.close();
            } catch (SQLException e) {
                LOG.error("Error closing database connection", e);
            }
        }
    }
    
    /**
     * Creates the required directories if they don't exist.
     */
    private void createDirectories() throws IOException {
        Path inputDir = Paths.get(processingConfig.inputDirectory());
        Path outputDir = Paths.get(processingConfig.outputDirectory());
        Path errorDir = Paths.get(processingConfig.errorDirectory());
        
        Files.createDirectories(inputDir);
        Files.createDirectories(outputDir);
        Files.createDirectories(errorDir);
        
        LOG.info("Created directories - Input: " + inputDir + ", Output: " + outputDir + ", Error: " + errorDir);
    }
    
    /**
     * Processes a parquet file: parses it and stores the data in the database.
     */
    private void processFile(Path filePath) {
        LOG.info("Processing file: " + filePath);
        
        try {
            // Determine schema type and parse the file
            boolean isGreen = TripDataParser.isGreenTripdataFile(filePath.toFile());
            boolean isYellow = TripDataParser.isYellowTripdataFile(filePath.toFile());
            
            if (!isGreen && !isYellow) {
                LOG.warn("File does not match green or yellow tripdata schema: " + filePath);
                metricsService.incrementFilesErrored();
                moveToErrorDirectory(filePath, "Unknown schema type");
                return;
            }
            
            // Check if database service schema type matches the file type
            DatabaseService.SchemaType dbSchemaType = databaseService.getSchemaType();
            if ((isGreen && dbSchemaType != DatabaseService.SchemaType.GREEN) ||
                (isYellow && dbSchemaType != DatabaseService.SchemaType.YELLOW)) {
                LOG.warn("File schema type (" + (isGreen ? "GREEN" : "YELLOW") + 
                        ") does not match database schema type (" + dbSchemaType + "): " + filePath);
                metricsService.incrementFilesErrored();
                moveToErrorDirectory(filePath, "Schema type mismatch");
                return;
            }
            
            // Parse and store the data
            if (isGreen) {
                processGreenTripdata(filePath);
            } else {
                processYellowTripdata(filePath);
            }
            
            // Move file to output directory on success
            moveToOutputDirectory(filePath);
            metricsService.incrementFilesProcessed();
            LOG.info("Successfully processed file: " + filePath);
            
        } catch (Exception e) {
            LOG.error("Error processing file: " + filePath, e);
            metricsService.incrementFilesErrored();
            try {
                moveToErrorDirectory(filePath, e.getMessage());
            } catch (IOException ioException) {
                LOG.error("Failed to move file to error directory: " + filePath, ioException);
            }
        }
    }
    
    /**
     * Processes green tripdata file.
     */
    private void processGreenTripdata(Path filePath) throws TripDataParserException, SQLException {
        List<GreenTripdata> trips = TripDataParser.parseGreenTripdata(filePath.toString());
        LOG.info("Parsed " + trips.size() + " green trip records from " + filePath);
        
        // Batch insert for better performance
        if (!trips.isEmpty()) {
            databaseService.batchInsertGreen(trips);
            metricsService.incrementRecordsInserted(trips.size());
            LOG.info("Inserted " + trips.size() + " green trip records into database");
        }
    }
    
    /**
     * Processes yellow tripdata file.
     */
    private void processYellowTripdata(Path filePath) throws TripDataParserException, SQLException {
        List<YellowTripdata> trips = TripDataParser.parseYellowTripdata(filePath.toString());
        LOG.info("Parsed " + trips.size() + " yellow trip records from " + filePath);
        
        // Batch insert for better performance
        if (!trips.isEmpty()) {
            databaseService.batchInsertYellow(trips);
            metricsService.incrementRecordsInserted(trips.size());
            LOG.info("Inserted " + trips.size() + " yellow trip records into database");
        }
    }
    
    /**
     * Moves a file to the output directory.
     */
    private void moveToOutputDirectory(Path filePath) throws IOException {
        Path outputDir = Paths.get(processingConfig.outputDirectory());
        Path destination = outputDir.resolve(filePath.getFileName());
        Files.move(filePath, destination, StandardCopyOption.REPLACE_EXISTING);
    }
    
    /**
     * Moves a file to the error directory with an error message suffix.
     */
    private void moveToErrorDirectory(Path filePath, String errorMessage) throws IOException {
        Path errorDir = Paths.get(processingConfig.errorDirectory());
        String fileName = filePath.getFileName().toString();
        // Add timestamp and error suffix to filename
        String errorFileName = fileName.replace(".parquet", "_" + System.currentTimeMillis() + ".parquet");
        Path destination = errorDir.resolve(errorFileName);
        Files.move(filePath, destination, StandardCopyOption.REPLACE_EXISTING);
        LOG.warn("Moved file to error directory: " + destination + " (Error: " + errorMessage + ")");
    }
    
    /**
     * Gets the monitor instance.
     *
     * @return monitor instance
     */
    public Monitor getMonitor() {
        return monitor;
    }
}

