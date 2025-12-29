package com.bscllc.taxis.app;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;

/**
 * Main Quarkus application for indexing taxi trip data into OpenSearch.
 */
@QuarkusMain
public class TaxiDataIndexerApplication implements QuarkusApplication {
    
    private static final Logger LOG = LoggerFactory.getLogger(TaxiDataIndexerApplication.class);
    
    @Inject
    FileProcessingService fileProcessingService;
    
    @Override
    public int run(String... args) {
        LOG.info("Taxi Data Indexer Application started");
        LOG.info("Monitoring directory: " + fileProcessingService.getMonitor().getDirectory());
        LOG.info("Scan period: " + fileProcessingService.getMonitor().getScanPeriodMillis() + " ms");
        
        // Keep the application running
        Quarkus.waitForExit();
        return 0;
    }
    
    public static void main(String... args) {
        Quarkus.run(TaxiDataIndexerApplication.class, args);
    }
}

