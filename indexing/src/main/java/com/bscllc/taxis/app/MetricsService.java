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
    
    @Inject
    public MetricsService(MeterRegistry meterRegistry) {
        this.filesProcessed = Counter.builder("taxis.files.processed")
                .description("Total number of files processed successfully")
                .register(meterRegistry);
        
        this.filesErrored = Counter.builder("taxis.files.errored")
                .description("Total number of files that failed to process")
                .register(meterRegistry);
        
        this.recordsIndexed = Counter.builder("taxis.records.indexed")
                .description("Total number of records indexed into OpenSearch")
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
}

