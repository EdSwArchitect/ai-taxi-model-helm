package com.bscllc.taxis.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

/**
 * Configuration properties for OpenSearch connection.
 */
@ConfigMapping(prefix = "taxis.opensearch")
public interface OpenSearchConfig {
    
    String host();
    
    int port();
    
    String username();
    
    String password();
    
    @WithName("use-tls")
    boolean useTls();
    
    @WithName("cert-path")
    String certPath();
}

