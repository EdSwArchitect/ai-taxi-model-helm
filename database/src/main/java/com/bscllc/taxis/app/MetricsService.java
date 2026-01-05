package com.bscllc.taxis.app;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing application metrics.
 */
@ApplicationScoped
public class MetricsService {
    
    private static final Logger LOG = LoggerFactory.getLogger(MetricsService.class);
    
    private final MeterRegistry meterRegistry;
    private final Counter filesProcessed;
    private final Counter filesErrored;
    private final Counter recordsInserted;
    private final Counter tablesCreated;
    private final Counter filesProcessedGreen;
    private final Counter filesProcessedYellow;
    private final Counter greenRecordsInserted;
    private final Counter yellowRecordsInserted;
    
    @Inject
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Use Counter.builder() for explicit counter creation
        this.filesProcessed = Counter.builder("taxis.files.processed")
                .description("Total number of files processed successfully")
                .tag("type", "database")
                .register(meterRegistry);
        
        this.filesErrored = Counter.builder("taxis.files.errored")
                .description("Total number of files that failed to process")
                .tag("type", "database")
                .register(meterRegistry);
        
        this.recordsInserted = Counter.builder("taxis.records.inserted")
                .description("Total number of records inserted into the database")
                .tag("type", "database")
                .register(meterRegistry);
        
        this.tablesCreated = Counter.builder("taxis.tables.created")
                .description("Total number of database tables created")
                .register(meterRegistry);
        
        // Create schema-specific counters using separate metric names (no tags)
        this.filesProcessedGreen = Counter.builder("taxis.green.files.processed")
                .description("Number of green schema files processed successfully")
                .tag("type", "database")
                .register(meterRegistry);
        this.filesProcessedYellow = Counter.builder("taxis.yellow.files.processed")
                .description("Number of yellow schema files processed successfully")
                .tag("type", "database")
                .register(meterRegistry);
        this.greenRecordsInserted = Counter.builder("taxis.green.records.inserted")
                .description("Number of green schema records inserted into the database")
                .tag("type", "database")
                .register(meterRegistry);
        this.yellowRecordsInserted = Counter.builder("taxis.yellow.records.inserted")
                .description("Number of yellow schema records inserted into the database")
                .tag("type", "database")
                .register(meterRegistry);
    }
    
    @PostConstruct
    void init() {
        // Ensure all counters are registered and visible in Prometheus
        // Counters are registered when created, but this ensures the service is initialized
        LOG.info("MetricsService initialized. All counters registered:");
        LOG.info("  - taxis.files.processed (with type=database)");
        LOG.info("  - taxis.green.files.processed (with type=database)");
        LOG.info("  - taxis.yellow.files.processed (with type=database)");
        LOG.info("  - taxis.files.errored (with type=database)");
        LOG.info("  - taxis.records.inserted (with type=database)");
        LOG.info("  - taxis.green.records.inserted (with type=database)");
        LOG.info("  - taxis.yellow.records.inserted (with type=database)");
        LOG.info("  - taxis.tables.created");
        
        // Verify schema-specific counters are properly registered
        LOG.info("Verifying schema-specific counters:");
        LOG.info("  - greenRecordsInserted counter ID: {}", greenRecordsInserted.getId());
        LOG.info("  - greenRecordsInserted counter name: {}", greenRecordsInserted.getId().getName());
        LOG.info("  - yellowRecordsInserted counter ID: {}", yellowRecordsInserted.getId());
        LOG.info("  - yellowRecordsInserted counter name: {}", yellowRecordsInserted.getId().getName());
        LOG.info("  - Green counter initial value: {}", greenRecordsInserted.count());
        LOG.info("  - Yellow counter initial value: {}", yellowRecordsInserted.count());
    }
    
    public void incrementFilesProcessed() {
        filesProcessed.increment();
    }
    
    public void incrementFilesErrored() {
        filesErrored.increment();
    }
    
    public void incrementRecordsInserted(long count) {
        recordsInserted.increment(count);
    }
    
    public void incrementTablesCreated() {
        tablesCreated.increment();
    }
    
    public void incrementFilesProcessedGreen() {
        filesProcessedGreen.increment();
        LOG.debug("Incremented green files counter (current value: {})", filesProcessedGreen.count());
    }
    
    public void incrementFilesProcessedYellow() {
        filesProcessedYellow.increment();
        LOG.debug("Incremented yellow files counter (current value: {})", filesProcessedYellow.count());
    }
    
    public void incrementRecordsInsertedGreen(long count) {
        greenRecordsInserted.increment(count);
        LOG.info("Incremented green records counter by {} (current value: {})", 
            count, greenRecordsInserted.count());
    }
    
    public void incrementRecordsInsertedYellow(long count) {
        yellowRecordsInserted.increment(count);
        LOG.info("Incremented yellow records counter by {} (current value: {})", 
            count, yellowRecordsInserted.count());
    }
}

