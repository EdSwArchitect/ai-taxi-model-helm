package com.bscllc.taxis.app;

import com.bscllc.taxis.config.IndexingConfig;
import com.bscllc.taxis.config.ProcessingConfig;
import com.bscllc.taxis.model.GreenTripdata;
import com.bscllc.taxis.model.YellowTripdata;
import com.bscllc.taxis.service.DatabaseService;
import com.bscllc.taxis.service.IndexingService;
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
 * Combined service for processing parquet files from the input directory,
 * storing them in the database and indexing them in OpenSearch.
 * Both operations are performed on each file.
 */
@ApplicationScoped
public class CombinedFileProcessingService {
    
    private static final Logger LOG = LoggerFactory.getLogger(CombinedFileProcessingService.class);
    
    @Inject
    ProcessingConfig processingConfig;
    
    @Inject
    IndexingConfig indexingConfig;
    
    @Inject
    DatabaseService databaseService;
    
    @Inject
    IndexingService indexingService;
    
    @Inject
    CombinedMetricsService metricsService;
    
    private Monitor monitor;
    private RateLimiter rateLimiter;
    
    @PostConstruct
    void init() {
        try {
            // Create directories if they don't exist
            createDirectories();
            
            // Initialize rate limiter for indexing operations
            rateLimiter = new RateLimiter(indexingConfig.rateLimitPerSecond());
            LOG.info("Rate limiter initialized: " + indexingConfig.rateLimitPerSecond() + " permits/second");
            
            // Create and configure the monitor
            monitor = Monitor.builder()
                    .directory(processingConfig.inputDirectory())
                    .filePattern(".*\\.parquet$")
                    .scanPeriod(processingConfig.monitorPeriodMs())
                    .onFileAdded(this::processFile)
                    .build();
            
            // Start monitoring
            monitor.start();
            LOG.info("Combined file processing service started. Monitoring directory: " + processingConfig.inputDirectory());
            LOG.info("Batch size: " + indexingConfig.batchSize());
        } catch (IOException e) {
            LOG.error("Failed to initialize combined file processing service", e);
            throw new RuntimeException("Failed to initialize combined file processing service", e);
        }
    }
    
    @PreDestroy
    void cleanup() {
        if (monitor != null && monitor.isRunning()) {
            monitor.stop();
            LOG.info("File monitoring stopped");
        }
        
        if (rateLimiter != null) {
            rateLimiter.shutdown();
        }
        
        if (databaseService != null) {
            try {
                databaseService.close();
            } catch (SQLException e) {
                LOG.error("Error closing database connection", e);
            }
        }
        
        if (indexingService != null) {
            try {
                indexingService.close();
            } catch (Exception e) {
                LOG.error("Error closing indexing service", e);
            }
        }
        
        LOG.info("Combined file processing service stopped");
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
     * Processes a parquet file: parses it, stores in database, and indexes in OpenSearch.
     */
    private void processFile(Path filePath) {
        LOG.info("Processing file: " + filePath);
        
        boolean databaseSuccess = false;
        boolean indexingSuccess = false;
        String errorMessage = null;
        
        try {
            // Determine schema type
            boolean isGreen = TripDataParser.isGreenTripdataFile(filePath.toFile());
            boolean isYellow = TripDataParser.isYellowTripdataFile(filePath.toFile());
            
            if (!isGreen && !isYellow) {
                LOG.warn("File does not match green or yellow tripdata schema: " + filePath);
                metricsService.incrementFilesErroredDatabase();
                metricsService.incrementFilesErroredIndexing();
                moveToErrorDirectory(filePath, "Unknown schema type");
                return;
            }
            
            // Process for database
            try {
                if (isGreen) {
                    processGreenTripdataForDatabase(filePath);
                    metricsService.incrementFilesProcessedGreenDatabase();
                } else {
                    processYellowTripdataForDatabase(filePath);
                    metricsService.incrementFilesProcessedYellowDatabase();
                }
                metricsService.incrementFilesProcessedDatabase();
                databaseSuccess = true;
            } catch (Exception e) {
                LOG.error("Error processing file for database: " + filePath, e);
                metricsService.incrementFilesErroredDatabase();
                errorMessage = "Database error: " + e.getMessage();
            }
            
            // Process for indexing
            try {
                if (isGreen) {
                    processGreenTripdataForIndexing(filePath);
                    metricsService.incrementFilesProcessedGreenIndexing();
                } else {
                    processYellowTripdataForIndexing(filePath);
                    metricsService.incrementFilesProcessedYellowIndexing();
                }
                metricsService.incrementFilesProcessedIndexing();
                indexingSuccess = true;
            } catch (Exception e) {
                LOG.error("Error processing file for indexing: " + filePath, e);
                metricsService.incrementFilesErroredIndexing();
                if (errorMessage == null) {
                    errorMessage = "Indexing error: " + e.getMessage();
                } else {
                    errorMessage += "; Indexing error: " + e.getMessage();
                }
            }
            
            // Move file to output directory only if both operations succeeded
            if (databaseSuccess && indexingSuccess) {
                moveToOutputDirectory(filePath);
                LOG.info("Successfully processed file (database and indexing): " + filePath);
            } else {
                // At least one operation failed, move to error directory
                moveToErrorDirectory(filePath, errorMessage != null ? errorMessage : "Partial failure");
                LOG.warn("File processing partially or completely failed: " + filePath);
            }
            
        } catch (Exception e) {
            LOG.error("Error processing file: " + filePath, e);
            metricsService.incrementFilesErroredDatabase();
            metricsService.incrementFilesErroredIndexing();
            try {
                moveToErrorDirectory(filePath, e.getMessage());
            } catch (IOException ioException) {
                LOG.error("Failed to move file to error directory: " + filePath, ioException);
            }
        }
    }
    
    /**
     * Processes green tripdata file for database.
     */
    private void processGreenTripdataForDatabase(Path filePath) throws TripDataParserException, SQLException {
        List<GreenTripdata> trips = TripDataParser.parseGreenTripdata(filePath.toString());
        LOG.info("Parsed " + trips.size() + " green trip records from " + filePath);
        
        if (!trips.isEmpty()) {
            databaseService.batchInsertGreen(trips);
            metricsService.incrementRecordsInserted(trips.size());
            metricsService.incrementRecordsInsertedGreen(trips.size());
            LOG.info("Inserted " + trips.size() + " green trip records into database");
        }
    }
    
    /**
     * Processes yellow tripdata file for database.
     */
    private void processYellowTripdataForDatabase(Path filePath) throws TripDataParserException, SQLException {
        List<YellowTripdata> trips = TripDataParser.parseYellowTripdata(filePath.toString());
        LOG.info("Parsed " + trips.size() + " yellow trip records from " + filePath);
        
        if (!trips.isEmpty()) {
            databaseService.batchInsertYellow(trips);
            metricsService.incrementRecordsInserted(trips.size());
            metricsService.incrementRecordsInsertedYellow(trips.size());
            LOG.info("Inserted " + trips.size() + " yellow trip records into database");
        }
    }
    
    /**
     * Processes green tripdata file for indexing with rate limiting.
     */
    private void processGreenTripdataForIndexing(Path filePath) throws TripDataParserException, InterruptedException, Exception {
        List<GreenTripdata> trips = TripDataParser.parseGreenTripdata(filePath.toString());
        LOG.info("Parsed " + trips.size() + " green trip records from " + filePath + " for indexing");
        
        if (trips.isEmpty()) {
            return;
        }
        
        // Process in batches with rate limiting
        int batchSize = indexingConfig.batchSize();
        for (int i = 0; i < trips.size(); i += batchSize) {
            int end = Math.min(i + batchSize, trips.size());
            List<GreenTripdata> batch = trips.subList(i, end);
            
            // Acquire permits for this batch (rate limiting)
            rateLimiter.acquire(batch.size());
            
            // Index the batch to OpenSearch
            indexingService.indexGreenTrips(batch);
            metricsService.incrementRecordsIndexed(batch.size());
            metricsService.incrementRecordsIndexedGreen(batch.size());
            LOG.info("Indexed batch of " + batch.size() + " green trip records (total: " + trips.size() + ")");
        }
        
        LOG.info("Indexed " + trips.size() + " green trip records into OpenSearch");
    }
    
    /**
     * Processes yellow tripdata file for indexing with rate limiting.
     */
    private void processYellowTripdataForIndexing(Path filePath) throws TripDataParserException, InterruptedException, Exception {
        List<YellowTripdata> trips = TripDataParser.parseYellowTripdata(filePath.toString());
        LOG.info("Parsed " + trips.size() + " yellow trip records from " + filePath + " for indexing");
        
        if (trips.isEmpty()) {
            return;
        }
        
        // Process in batches with rate limiting
        int batchSize = indexingConfig.batchSize();
        for (int i = 0; i < trips.size(); i += batchSize) {
            int end = Math.min(i + batchSize, trips.size());
            List<YellowTripdata> batch = trips.subList(i, end);
            
            // Acquire permits for this batch (rate limiting)
            rateLimiter.acquire(batch.size());
            
            // Index the batch to OpenSearch
            indexingService.indexYellowTrips(batch);
            metricsService.incrementRecordsIndexed(batch.size());
            metricsService.incrementRecordsIndexedYellow(batch.size());
            LOG.info("Indexed batch of " + batch.size() + " yellow trip records (total: " + trips.size() + ")");
        }
        
        LOG.info("Indexed " + trips.size() + " yellow trip records into OpenSearch");
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

