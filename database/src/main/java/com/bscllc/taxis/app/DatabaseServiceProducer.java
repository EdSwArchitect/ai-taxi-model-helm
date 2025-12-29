package com.bscllc.taxis.app;

import com.bscllc.taxis.config.DatabaseConfig;
import com.bscllc.taxis.service.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.sql.SQLException;

/**
 * Producer for DatabaseService bean.
 */
@ApplicationScoped
public class DatabaseServiceProducer {
    
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseServiceProducer.class);
    
    @Inject
    DatabaseConfig databaseConfig;
    
    @Inject
    MetricsService metricsService;
    
    @Produces
    @Singleton
    public DatabaseService produceDatabaseService() {
        try {
            LOG.info("Creating DatabaseService - Host: " + databaseConfig.host() + 
                    ", Port: " + databaseConfig.port() + 
                    ", Database: " + databaseConfig.database() + 
                    ", Schema Type: " + databaseConfig.schemaType() +
                    ", TLS: " + databaseConfig.useTls());
            
            DatabaseService.Builder builder = DatabaseService.builder()
                    .host(databaseConfig.host())
                    .port(databaseConfig.port())
                    .database(databaseConfig.database())
                    .credentials(databaseConfig.username(), databaseConfig.password())
                    .useTls(databaseConfig.useTls())
                    .schemaType(databaseConfig.schemaTypeEnum())
                    .createTableIfNotExists(databaseConfig.createTableIfNotExists());
            
            if (databaseConfig.useTls() && databaseConfig.certPath() != null && 
                !databaseConfig.certPath().isEmpty()) {
                builder.certPath(databaseConfig.certPath());
            }
            
            DatabaseService service = builder.build();
            LOG.info("DatabaseService created successfully. Table: " + service.getTableName());
            
            // Track table creation if table was actually created
            if (service.wasTableCreated()) {
                metricsService.incrementTablesCreated();
                LOG.info("Table created: " + service.getTableName());
            } else if (databaseConfig.createTableIfNotExists()) {
                LOG.info("Table already exists: " + service.getTableName());
            }
            
            return service;
        } catch (SQLException e) {
            LOG.error("Failed to create DatabaseService", e);
            throw new RuntimeException("Failed to create DatabaseService", e);
        }
    }
}

