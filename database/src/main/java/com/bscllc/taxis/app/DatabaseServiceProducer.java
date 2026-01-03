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
            LOG.info("DatabaseService created successfully. Tables: " + 
                    service.getGreenTableName() + ", " + service.getYellowTableName());
            
            // Track table creation if tables were created
            if (service.wasTableCreated()) {
                metricsService.incrementTablesCreated();
                LOG.info("Tables created/verified: " + service.getGreenTableName() + ", " + service.getYellowTableName());
            } else if (databaseConfig.createTableIfNotExists()) {
                LOG.info("Tables already exist: " + service.getGreenTableName() + ", " + service.getYellowTableName());
            }
            
            return service;
        } catch (SQLException e) {
            LOG.error("Failed to create DatabaseService", e);
            throw new RuntimeException("Failed to create DatabaseService", e);
        }
    }
}

