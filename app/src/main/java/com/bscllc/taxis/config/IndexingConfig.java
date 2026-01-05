package com.bscllc.taxis.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

/**
 * Configuration properties for indexing operations.
 */
@ConfigMapping(prefix = "taxis.indexing")
public interface IndexingConfig {
    
    /**
     * Maximum number of documents to index per second (rate limit).
     */
    @WithName("rate-limit-per-second")
    int rateLimitPerSecond();
    
    /**
     * Batch size for indexing operations.
     */
    @WithName("batch-size")
    int batchSize();
}

