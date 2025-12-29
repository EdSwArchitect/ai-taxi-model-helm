package com.bscllc.taxis.config;

import com.bscllc.taxis.service.DatabaseService;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;
import io.smallrye.config.WithDefault;

/**
 * Configuration properties for database connection.
 */
@ConfigMapping(prefix = "taxis.database")
public interface DatabaseConfig {
    
    @WithDefault("localhost")
    String host();
    
    @WithDefault("5432")
    int port();
    
    @WithDefault("taxidb")
    String database();
    
    @WithDefault("postgres")
    String username();
    
    @WithDefault("postgres")
    String password();
    
    @WithName("use-tls")
    @WithDefault("false")
    boolean useTls();
    
    @WithName("cert-path")
    @WithDefault("")
    String certPath();
    
    @WithName("schema-type")
    @WithDefault("GREEN")
    String schemaType();
    
    @WithName("create-table-if-not-exists")
    @WithDefault("true")
    boolean createTableIfNotExists();
    
    /**
     * Converts schema type string to enum.
     */
    default DatabaseService.SchemaType schemaTypeEnum() {
        return "YELLOW".equalsIgnoreCase(schemaType()) 
            ? DatabaseService.SchemaType.YELLOW 
            : DatabaseService.SchemaType.GREEN;
    }
}

