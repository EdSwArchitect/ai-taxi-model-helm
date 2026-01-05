package com.bscllc.taxis.app;

import com.bscllc.taxis.config.DatabaseConfig;
import com.bscllc.taxis.config.OpenSearchConfig;
import com.bscllc.taxis.service.DatabaseService;
import com.bscllc.taxis.service.IndexingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.sql.SQLException;

/**
 * Producers for DatabaseService and IndexingService beans.
 */
@ApplicationScoped
public class ServiceProducers {
    
    private static final Logger LOG = LoggerFactory.getLogger(ServiceProducers.class);
    
    @Inject
    DatabaseConfig databaseConfig;
    
    @Inject
    OpenSearchConfig openSearchConfig;
    
    @Inject
    CombinedMetricsService metricsService;
    
    @Produces
    @Singleton
    public DatabaseService produceDatabaseService() {
        try {
            LOG.info("Creating DatabaseService - Host: " + databaseConfig.host() + 
                    ", Port: " + databaseConfig.port() + 
                    ", Database: " + databaseConfig.database() + 
                    ", Schema Types: GREEN and YELLOW (both supported)" +
                    ", TLS: " + databaseConfig.useTls());
            
            DatabaseService.Builder builder = DatabaseService.builder()
                    .host(databaseConfig.host())
                    .port(databaseConfig.port())
                    .database(databaseConfig.database())
                    .credentials(databaseConfig.username(), databaseConfig.password())
                    .useTls(databaseConfig.useTls())
                    .schemaType(databaseConfig.schemaTypeEnum())
                    .createTableIfNotExists(databaseConfig.createTableIfNotExists());
            
            if (databaseConfig.useTls() && databaseConfig.certPath().isPresent() && 
                !databaseConfig.certPath().get().isEmpty()) {
                builder.certPath(databaseConfig.certPath().get());
            }
            
            DatabaseService service = builder.build();
            metricsService.incrementTablesCreated();
            LOG.info("DatabaseService created successfully");
            return service;
        } catch (SQLException e) {
            LOG.error("Failed to create DatabaseService", e);
            throw new RuntimeException("Failed to create DatabaseService", e);
        }
    }
    
    @Produces
    @Singleton
    public IndexingService produceIndexingService() {
        try {
            LOG.info("Creating IndexingService - Host: " + openSearchConfig.host() + 
                    ", Port: " + openSearchConfig.port() + 
                    ", TLS: " + openSearchConfig.useTls());
            
            IndexingService.Builder builder = IndexingService.builder()
                    .host(openSearchConfig.host())
                    .port(openSearchConfig.port())
                    .useTls(openSearchConfig.useTls());
            
            if (openSearchConfig.username() != null && openSearchConfig.password() != null) {
                builder.credentials(openSearchConfig.username(), openSearchConfig.password());
            }
            
            if (openSearchConfig.useTls() && openSearchConfig.certPath().isPresent() && 
                !openSearchConfig.certPath().get().isEmpty()) {
                builder.certPath(openSearchConfig.certPath().get());
            } else if (openSearchConfig.useTls()) {
                builder.trustAllCerts(true);
            }
            
            IndexingService service = builder.build();
            LOG.info("IndexingService created successfully");
            return service;
        } catch (Exception e) {
            LOG.error("Failed to create IndexingService", e);
            throw new RuntimeException("Failed to create IndexingService", e);
        }
    }
}

