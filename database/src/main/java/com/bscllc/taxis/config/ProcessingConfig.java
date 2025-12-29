package com.bscllc.taxis.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

/**
 * Configuration properties for the taxi data processing application.
 */
@ConfigMapping(prefix = "taxis.processing")
public interface ProcessingConfig {
    
    /**
     * Input directory to monitor for parquet files.
     */
    @WithName("input-directory")
    String inputDirectory();
    
    /**
     * Output directory for successfully processed files.
     */
    @WithName("output-directory")
    String outputDirectory();
    
    /**
     * Error directory for files that couldn't be processed.
     */
    @WithName("error-directory")
    String errorDirectory();
    
    /**
     * Time period in milliseconds to monitor the input directory.
     */
    @WithName("monitor-period-ms")
    long monitorPeriodMs();
}

