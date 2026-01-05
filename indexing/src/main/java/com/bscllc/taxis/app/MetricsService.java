package com.bscllc.taxis.app;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Service for managing application metrics.
 */
@ApplicationScoped
public class MetricsService {
    
    private final Counter filesProcessed;
    private final Counter filesErrored;
    private final Counter recordsIndexed;
    private final Counter filesProcessedGreen;
    private final Counter filesProcessedYellow;
    private final Counter greenRecordsIndexed;
    private final Counter yellowRecordsIndexed;
    
    @Inject
    public MetricsService(MeterRegistry meterRegistry) {
        this.filesProcessed = Counter.builder("taxis.files.processed")
                .description("Total number of files processed successfully")
                .tag("type", "opensearch")
                .register(meterRegistry);
        
        this.filesErrored = Counter.builder("taxis.files.errored")
                .description("Total number of files that failed to process")
                .tag("type", "opensearch")
                .register(meterRegistry);
        
        this.recordsIndexed = Counter.builder("taxis.records.indexed")
                .description("Total number of records indexed into OpenSearch")
                .tag("type", "opensearch")
                .register(meterRegistry);
        
        this.filesProcessedGreen = Counter.builder("taxis.green.files.processed")
                .description("Number of green schema files processed successfully")
                .tag("type", "opensearch")
                .register(meterRegistry);
        
        this.filesProcessedYellow = Counter.builder("taxis.yellow.files.processed")
                .description("Number of yellow schema files processed successfully")
                .tag("type", "opensearch")
                .register(meterRegistry);
        
        // Create schema-specific counters using separate metric names (no tags)
        this.greenRecordsIndexed = Counter.builder("taxis.green.records.indexed")
                .description("Number of green schema records indexed into OpenSearch")
                .tag("type", "opensearch")
                .register(meterRegistry);
        
        this.yellowRecordsIndexed = Counter.builder("taxis.yellow.records.indexed")
                .description("Number of yellow schema records indexed into OpenSearch")
                .tag("type", "opensearch")
                .register(meterRegistry);
    }
    
    public void incrementFilesProcessed() {
        filesProcessed.increment();
    }
    
    public void incrementFilesErrored() {
        filesErrored.increment();
    }
    
    public void incrementRecordsIndexed(long count) {
        recordsIndexed.increment(count);
    }
    
    public void incrementFilesProcessedGreen() {
        filesProcessedGreen.increment();
    }
    
    public void incrementFilesProcessedYellow() {
        filesProcessedYellow.increment();
    }
    
    public void incrementRecordsIndexedGreen(long count) {
        greenRecordsIndexed.increment(count);
    }
    
    public void incrementRecordsIndexedYellow(long count) {
        yellowRecordsIndexed.increment(count);
    }
}

