package com.bscllc.taxis.app;

import com.bscllc.taxis.config.OpenSearchConfig;
import com.bscllc.taxis.service.IndexingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Producer for IndexingService bean.
 */
@ApplicationScoped
public class IndexingServiceProducer {
    
    private static final Logger LOG = LoggerFactory.getLogger(IndexingServiceProducer.class);
    
    @Inject
    OpenSearchConfig openSearchConfig;
    
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

