package com.bscllc.taxis.app;

import com.bscllc.taxis.config.IndexingConfig;
import com.bscllc.taxis.config.ProcessingConfig;
import com.bscllc.taxis.model.GreenTripdata;
import com.bscllc.taxis.model.YellowTripdata;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Service for processing parquet files from the input directory and indexing them in OpenSearch.
 * Includes rate limiting and batch processing.
 */
@ApplicationScoped
public class FileProcessingService {
    
    private static final Logger LOG = LoggerFactory.getLogger(FileProcessingService.class);
    
    @Inject
    ProcessingConfig processingConfig;
    
    @Inject
    IndexingConfig indexingConfig;
    
    @Inject
    IndexingService indexingService;
    
    @Inject
    MetricsService metricsService;
    
    private Monitor monitor;
    private RateLimiter rateLimiter;
    private BlockingQueue<ProcessingTask> processingQueue;
    private Thread processingThread;
    private volatile boolean running = false;
    
    @PostConstruct
    void init() {
        try {
            // Create directories if they don't exist
            createDirectories();
            
            // Initialize rate limiter
            rateLimiter = new RateLimiter(indexingConfig.rateLimitPerSecond());
            LOG.info("Rate limiter initialized: " + indexingConfig.rateLimitPerSecond() + " permits/second");
            
            // Initialize processing queue
            processingQueue = new LinkedBlockingQueue<>();
            
            // Start processing thread
            running = true;
            processingThread = new Thread(this::processQueue, "FileProcessing-Queue");
            processingThread.setDaemon(false);
            processingThread.start();
            
            // Create and configure the monitor
            monitor = Monitor.builder()
                    .directory(processingConfig.inputDirectory())
                    .filePattern(".*\\.parquet$")
                    .scanPeriod(processingConfig.monitorPeriodMs())
                    .onFileAdded(this::queueFileForProcessing)
                    .build();
            
            // Start monitoring
            monitor.start();
            LOG.info("File processing service started. Monitoring directory: " + processingConfig.inputDirectory());
            LOG.info("Batch size: " + indexingConfig.batchSize());
        } catch (IOException e) {
            LOG.error("Failed to initialize file processing service", e);
            throw new RuntimeException("Failed to initialize file processing service", e);
        }
    }
    
    @PreDestroy
    void cleanup() {
        running = false;
        
        if (monitor != null && monitor.isRunning()) {
            monitor.stop();
            LOG.info("File monitoring stopped");
        }
        
        // Signal processing thread to stop
        if (processingThread != null) {
            processingThread.interrupt();
            try {
                processingThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        if (rateLimiter != null) {
            rateLimiter.shutdown();
        }
        
        if (indexingService != null) {
            try {
                indexingService.close();
            } catch (Exception e) {
                LOG.error("Error closing indexing service", e);
            }
        }
        
        LOG.info("File processing service stopped");
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
     * Queues a file for processing.
     */
    private void queueFileForProcessing(Path filePath) {
        try {
            processingQueue.put(new ProcessingTask(filePath));
            LOG.debug("Queued file for processing: " + filePath);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Interrupted while queueing file: " + filePath, e);
        }
    }
    
    /**
     * Processes the queue of files.
     */
    private void processQueue() {
        LOG.info("File processing queue thread started");
        
        while (running || !processingQueue.isEmpty()) {
            try {
                ProcessingTask task = processingQueue.poll(1, TimeUnit.SECONDS);
                if (task != null) {
                    processFile(task.filePath);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.info("File processing queue thread interrupted");
                break;
            } catch (Exception e) {
                LOG.error("Error in processing queue", e);
            }
        }
        
        LOG.info("File processing queue thread stopped");
    }
    
    /**
     * Processes a parquet file: parses it and indexes the data in OpenSearch.
     */
    private void processFile(Path filePath) {
        LOG.info("Processing file: " + filePath);
        
        try {
            // Determine schema type
            boolean isGreen = TripDataParser.isGreenTripdataFile(filePath.toFile());
            boolean isYellow = TripDataParser.isYellowTripdataFile(filePath.toFile());
            
            if (!isGreen && !isYellow) {
                LOG.warn("File does not match green or yellow tripdata schema: " + filePath);
                metricsService.incrementFilesErrored();
                moveToErrorDirectory(filePath, "Unknown schema type");
                return;
            }
            
            // Parse and index the data
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
     * Processes green tripdata file with rate limiting.
     */
    private void processGreenTripdata(Path filePath) throws TripDataParserException, InterruptedException {
        List<GreenTripdata> trips = TripDataParser.parseGreenTripdata(filePath.toString());
        LOG.info("Parsed " + trips.size() + " green trip records from " + filePath);
        
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
            try {
                indexingService.indexGreenTrips(batch);
                metricsService.incrementRecordsIndexed(batch.size());
                LOG.info("Indexed batch of " + batch.size() + " green trip records (total: " + trips.size() + ")");
            } catch (Exception e) {
                LOG.error("Failed to index batch of green trip records: " + e.getMessage(), e);
                throw new RuntimeException("Failed to index green trip records", e);
            }
        }
        
        LOG.info("Indexed " + trips.size() + " green trip records into OpenSearch");
    }
    
    /**
     * Processes yellow tripdata file with rate limiting.
     */
    private void processYellowTripdata(Path filePath) throws TripDataParserException, InterruptedException {
        List<YellowTripdata> trips = TripDataParser.parseYellowTripdata(filePath.toString());
        LOG.info("Parsed " + trips.size() + " yellow trip records from " + filePath);
        
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
            try {
                indexingService.indexYellowTrips(batch);
                metricsService.incrementRecordsIndexed(batch.size());
                LOG.info("Indexed batch of " + batch.size() + " yellow trip records (total: " + trips.size() + ")");
            } catch (Exception e) {
                LOG.error("Failed to index batch of yellow trip records: " + e.getMessage(), e);
                throw new RuntimeException("Failed to index yellow trip records", e);
            }
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
    
    /**
     * Internal class for queuing processing tasks.
     */
    private static class ProcessingTask {
        final Path filePath;
        
        ProcessingTask(Path filePath) {
            this.filePath = filePath;
        }
    }
}

