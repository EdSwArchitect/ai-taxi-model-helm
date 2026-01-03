package com.bscllc.taxis.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;
import io.smallrye.config.WithDefault;
import java.util.Optional;

/**
 * Configuration properties for OpenSearch connection.
 */
@ConfigMapping(prefix = "taxis.opensearch")
public interface OpenSearchConfig {
    
    @WithDefault("localhost")
    String host();
    
    @WithDefault("9200")
    int port();
    
    @WithDefault("admin")
    String username();
    
    @WithDefault("admin")
    String password();
    
    @WithName("use-tls")
    @WithDefault("false")
    boolean useTls();
    
    @WithName("cert-path")
    Optional<String> certPath();
}

