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
    private final Counter recordsInserted;
    private final Counter tablesCreated;
    
    @Inject
    public MetricsService(MeterRegistry meterRegistry) {
        this.filesProcessed = Counter.builder("taxis.files.processed")
                .description("Total number of files processed successfully")
                .register(meterRegistry);
        
        this.filesErrored = Counter.builder("taxis.files.errored")
                .description("Total number of files that failed to process")
                .register(meterRegistry);
        
        this.recordsInserted = Counter.builder("taxis.records.inserted")
                .description("Total number of records inserted into the database")
                .tag("type", "database")
                .register(meterRegistry);
        
        this.tablesCreated = Counter.builder("taxis.tables.created")
                .description("Total number of database tables created")
                .register(meterRegistry);
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
}

