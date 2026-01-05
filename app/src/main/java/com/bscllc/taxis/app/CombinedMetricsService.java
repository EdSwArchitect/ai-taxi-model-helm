package com.bscllc.taxis.app;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing application metrics for both database and indexing operations.
 */
@ApplicationScoped
public class CombinedMetricsService {
    
    private static final Logger LOG = LoggerFactory.getLogger(CombinedMetricsService.class);
    
    private final MeterRegistry meterRegistry;
    
    // Database metrics
    private final Counter filesProcessedDatabase;
    private final Counter filesErroredDatabase;
    private final Counter recordsInserted;
    private final Counter tablesCreated;
    private final Counter filesProcessedGreenDatabase;
    private final Counter filesProcessedYellowDatabase;
    private final Counter greenRecordsInserted;
    private final Counter yellowRecordsInserted;
    
    // Indexing metrics
    private final Counter filesProcessedIndexing;
    private final Counter filesErroredIndexing;
    private final Counter recordsIndexed;
    private final Counter filesProcessedGreenIndexing;
    private final Counter filesProcessedYellowIndexing;
    private final Counter greenRecordsIndexed;
    private final Counter yellowRecordsIndexed;
    
    @Inject
    public CombinedMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Database metrics
        this.filesProcessedDatabase = Counter.builder("taxis.files.processed")
                .description("Total number of files processed successfully (database)")
                .tag("type", "database")
                .register(meterRegistry);
        
        this.filesErroredDatabase = Counter.builder("taxis.files.errored")
                .description("Total number of files that failed to process (database)")
                .tag("type", "database")
                .register(meterRegistry);
        
        this.recordsInserted = Counter.builder("taxis.records.inserted")
                .description("Total number of records inserted into the database")
                .tag("type", "database")
                .register(meterRegistry);
        
        this.tablesCreated = Counter.builder("taxis.tables.created")
                .description("Total number of database tables created")
                .register(meterRegistry);
        
        this.filesProcessedGreenDatabase = Counter.builder("taxis.green.files.processed")
                .description("Number of green schema files processed successfully (database)")
                .tag("type", "database")
                .register(meterRegistry);
        
        this.filesProcessedYellowDatabase = Counter.builder("taxis.yellow.files.processed")
                .description("Number of yellow schema files processed successfully (database)")
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
        
        // Indexing metrics
        this.filesProcessedIndexing = Counter.builder("taxis.files.processed")
                .description("Total number of files processed successfully (indexing)")
                .tag("type", "opensearch")
                .register(meterRegistry);
        
        this.filesErroredIndexing = Counter.builder("taxis.files.errored")
                .description("Total number of files that failed to process (indexing)")
                .tag("type", "opensearch")
                .register(meterRegistry);
        
        this.recordsIndexed = Counter.builder("taxis.records.indexed")
                .description("Total number of records indexed into OpenSearch")
                .tag("type", "opensearch")
                .register(meterRegistry);
        
        this.filesProcessedGreenIndexing = Counter.builder("taxis.green.files.processed")
                .description("Number of green schema files processed successfully (indexing)")
                .tag("type", "opensearch")
                .register(meterRegistry);
        
        this.filesProcessedYellowIndexing = Counter.builder("taxis.yellow.files.processed")
                .description("Number of yellow schema files processed successfully (indexing)")
                .tag("type", "opensearch")
                .register(meterRegistry);
        
        this.greenRecordsIndexed = Counter.builder("taxis.green.records.indexed")
                .description("Number of green schema records indexed into OpenSearch")
                .tag("type", "opensearch")
                .register(meterRegistry);
        
        this.yellowRecordsIndexed = Counter.builder("taxis.yellow.records.indexed")
                .description("Number of yellow schema records indexed into OpenSearch")
                .tag("type", "opensearch")
                .register(meterRegistry);
    }
    
    @PostConstruct
    void init() {
        LOG.info("CombinedMetricsService initialized. All counters registered for both database and indexing operations.");
    }
    
    // Database metrics
    public void incrementFilesProcessedDatabase() {
        filesProcessedDatabase.increment();
    }
    
    public void incrementFilesErroredDatabase() {
        filesErroredDatabase.increment();
    }
    
    public void incrementRecordsInserted(long count) {
        recordsInserted.increment(count);
    }
    
    public void incrementTablesCreated() {
        tablesCreated.increment();
    }
    
    public void incrementFilesProcessedGreenDatabase() {
        filesProcessedGreenDatabase.increment();
    }
    
    public void incrementFilesProcessedYellowDatabase() {
        filesProcessedYellowDatabase.increment();
    }
    
    public void incrementRecordsInsertedGreen(long count) {
        greenRecordsInserted.increment(count);
    }
    
    public void incrementRecordsInsertedYellow(long count) {
        yellowRecordsInserted.increment(count);
    }
    
    // Indexing metrics
    public void incrementFilesProcessedIndexing() {
        filesProcessedIndexing.increment();
    }
    
    public void incrementFilesErroredIndexing() {
        filesErroredIndexing.increment();
    }
    
    public void incrementRecordsIndexed(long count) {
        recordsIndexed.increment(count);
    }
    
    public void incrementFilesProcessedGreenIndexing() {
        filesProcessedGreenIndexing.increment();
    }
    
    public void incrementFilesProcessedYellowIndexing() {
        filesProcessedYellowIndexing.increment();
    }
    
    public void incrementRecordsIndexedGreen(long count) {
        greenRecordsIndexed.increment(count);
    }
    
    public void incrementRecordsIndexedYellow(long count) {
        yellowRecordsIndexed.increment(count);
    }
}

