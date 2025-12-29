package com.bscllc.taxis.service;

import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;

import com.bscllc.taxis.model.GreenTripdata;
import com.bscllc.taxis.model.YellowTripdata;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.IndexOperation;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * Service class for connecting to OpenSearch with optional TLS support.
 */
public class IndexingService {
    
    private final OpenSearchClient client;
    private final String host;
    private final int port;
    private final boolean useTls;
    private final String username;
    private final String password;
    
    /**
     * Builder class for creating IndexingService instances.
     */
    public static class Builder {
        private String host = "localhost";
        private int port = 9200;
        private boolean useTls = false;
        private String username;
        private String password;
        private String certPath;
        private boolean trustAllCerts = false;
        
        /**
         * Sets the OpenSearch host.
         *
         * @param host hostname or IP address
         * @return this builder
         */
        public Builder host(String host) {
            this.host = host;
            return this;
        }
        
        /**
         * Sets the OpenSearch port.
         *
         * @param port port number
         * @return this builder
         */
        public Builder port(int port) {
            this.port = port;
            return this;
        }
        
        /**
         * Enables or disables TLS.
         *
         * @param useTls true to use TLS, false for plain HTTP
         * @return this builder
         */
        public Builder useTls(boolean useTls) {
            this.useTls = useTls;
            return this;
        }
        
        /**
         * Sets credentials for authentication.
         *
         * @param username username
         * @param password password
         * @return this builder
         */
        public Builder credentials(String username, String password) {
            this.username = username;
            this.password = password;
            return this;
        }
        
        /**
         * Sets the path to the certificate file for TLS.
         *
         * @param certPath path to certificate file
         * @return this builder
         */
        public Builder certPath(String certPath) {
            this.certPath = certPath;
            return this;
        }
        
        /**
         * Sets whether to trust all certificates (for self-signed certs).
         *
         * @param trustAll true to trust all certificates
         * @return this builder
         */
        public Builder trustAllCerts(boolean trustAll) {
            this.trustAllCerts = trustAll;
            return this;
        }
        
        /**
         * Builds the IndexingService instance.
         *
         * @return configured IndexingService
         * @throws Exception if client creation fails
         */
        public IndexingService build() throws Exception {
            return new IndexingService(this);
        }
    }
    
    /**
     * Creates a new Builder instance.
     *
     * @return new Builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    private IndexingService(Builder builder) throws Exception {
        this.host = builder.host;
        this.port = builder.port;
        this.useTls = builder.useTls;
        this.username = builder.username;
        this.password = builder.password;
        this.client = createClient(builder);
    }
    
    /**
     * Creates the OpenSearch client with optional TLS support.
     */
    private OpenSearchClient createClient(Builder builder) throws Exception {
        String scheme = builder.useTls ? "https" : "http";
        HttpHost httpHost = new HttpHost(builder.host, builder.port, scheme);
        
        RestClientBuilder restClientBuilder = RestClient.builder(httpHost);
        
        // Configure authentication if credentials are provided
        if (builder.username != null && builder.password != null) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(builder.username, builder.password)
            );
            
            restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                
                // Configure TLS if enabled
                if (builder.useTls) {
                    configureTls(httpClientBuilder, builder);
                }
                
                return httpClientBuilder;
            });
        } else if (builder.useTls) {
            // Configure TLS even without authentication
            restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
                configureTls(httpClientBuilder, builder);
                return httpClientBuilder;
            });
        }
        
        RestClient restClient = restClientBuilder.build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        
        return new OpenSearchClient(transport);
    }
    
    /**
     * Configures TLS/SSL for the HTTP client.
     */
    private void configureTls(org.apache.http.impl.nio.client.HttpAsyncClientBuilder httpClientBuilder, Builder builder) {
        try {
            if (builder.trustAllCerts) {
                // Trust all certificates (useful for self-signed certs)
                SSLContext sslContext = SSLContext.getInstance("TLS");
                TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }
                        
                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }
                        
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
                };
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                httpClientBuilder.setSSLContext(sslContext);
                httpClientBuilder.setSSLHostnameVerifier((hostname, session) -> true);
            } else if (builder.certPath != null) {
                // Use provided certificate
                Path certFile = Paths.get(builder.certPath);
                if (Files.exists(certFile)) {
                    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    trustStore.load(null, null);
                    
                    // Load certificate
                    try (InputStream certStream = Files.newInputStream(certFile)) {
                        java.security.cert.Certificate cert = 
                            java.security.cert.CertificateFactory.getInstance("X.509")
                                .generateCertificate(certStream);
                        trustStore.setCertificateEntry("opensearch", cert);
                    }
                    
                    // Create SSL context with truststore
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    javax.net.ssl.TrustManagerFactory trustManagerFactory =
                        javax.net.ssl.TrustManagerFactory.getInstance(
                            javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm());
                    trustManagerFactory.init(trustStore);
                    sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
                    
                    httpClientBuilder.setSSLContext(sslContext);
                } else {
                    throw new RuntimeException("Certificate file not found: " + builder.certPath);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure TLS", e);
        }
    }
    
    /**
     * Gets the OpenSearch client.
     *
     * @return OpenSearch client
     */
    public OpenSearchClient getClient() {
        return client;
    }
    
    /**
     * Gets the host.
     *
     * @return host
     */
    public String getHost() {
        return host;
    }
    
    /**
     * Gets the port.
     *
     * @return port
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Checks if TLS is enabled.
     *
     * @return true if TLS is enabled
     */
    public boolean isUseTls() {
        return useTls;
    }
    
    /**
     * Closes the client connection.
     *
     * @throws Exception if closing fails
     */
    public void close() throws Exception {
        if (client != null) {
            client._transport().close();
        }
    }
    
    /**
     * Checks if the OpenSearch cluster is healthy.
     *
     * @return true if cluster is healthy
     */
    public boolean isHealthy() {
        try {
            client.cluster().health();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Indexes a batch of green tripdata records to OpenSearch.
     *
     * @param trips list of green tripdata records to index
     * @throws Exception if indexing fails
     */
    public void indexGreenTrips(List<GreenTripdata> trips) throws Exception {
        if (trips == null || trips.isEmpty()) {
            return;
        }
        
        String indexName = "green-tripdata";
        ensureIndexExists(indexName);
        
        List<BulkOperation> bulkOperations = new ArrayList<>();
        
        for (GreenTripdata trip : trips) {
            Map<String, Object> doc = convertGreenTripdataToMap(trip);
            
            IndexOperation.Builder<Map<String, Object>> indexOp = new IndexOperation.Builder<Map<String, Object>>()
                .index(indexName)
                .document(doc)
                .id(UUID.randomUUID().toString());
            
            bulkOperations.add(new BulkOperation.Builder()
                .index(indexOp.build())
                .build());
        }
        
        BulkRequest bulkRequest = new BulkRequest.Builder()
            .operations(bulkOperations)
            .build();
        
        client.bulk(bulkRequest);
    }
    
    /**
     * Indexes a batch of yellow tripdata records to OpenSearch.
     *
     * @param trips list of yellow tripdata records to index
     * @throws Exception if indexing fails
     */
    public void indexYellowTrips(List<YellowTripdata> trips) throws Exception {
        if (trips == null || trips.isEmpty()) {
            return;
        }
        
        String indexName = "yellow-tripdata";
        ensureIndexExists(indexName);
        
        List<BulkOperation> bulkOperations = new ArrayList<>();
        
        for (YellowTripdata trip : trips) {
            Map<String, Object> doc = convertYellowTripdataToMap(trip);
            
            IndexOperation.Builder<Map<String, Object>> indexOp = new IndexOperation.Builder<Map<String, Object>>()
                .index(indexName)
                .document(doc)
                .id(UUID.randomUUID().toString());
            
            bulkOperations.add(new BulkOperation.Builder()
                .index(indexOp.build())
                .build());
        }
        
        BulkRequest bulkRequest = new BulkRequest.Builder()
            .operations(bulkOperations)
            .build();
        
        client.bulk(bulkRequest);
    }
    
    /**
     * Ensures that an index exists in OpenSearch with proper mappings.
     *
     * @param indexName name of the index
     * @throws Exception if index creation fails
     */
    private void ensureIndexExists(String indexName) throws Exception {
        try {
            boolean exists = client.indices().exists(e -> e.index(indexName)).value();
            if (!exists) {
                if (indexName.equals("green-tripdata")) {
                    createGreenTripdataIndex(indexName);
                } else if (indexName.equals("yellow-tripdata")) {
                    createYellowTripdataIndex(indexName);
                } else {
                    // Default index creation for unknown types
                    client.indices().create(c -> c.index(indexName));
                }
            }
        } catch (Exception e) {
            // Index might already exist, ignore
        }
    }
    
    /**
     * Creates the green tripdata index.
     * OpenSearch will auto-detect field types from the first document indexed.
     *
     * @param indexName name of the index
     * @throws Exception if index creation fails
     */
    private void createGreenTripdataIndex(String indexName) throws Exception {
        // Create index with basic settings
        // Field mappings will be auto-detected from the first document
        client.indices().create(c -> c
            .index(indexName)
            .settings(s -> s
                .numberOfShards("1")
                .numberOfReplicas("0")
            )
        );
    }
    
    /**
     * Creates the yellow tripdata index.
     * OpenSearch will auto-detect field types from the first document indexed.
     *
     * @param indexName name of the index
     * @throws Exception if index creation fails
     */
    private void createYellowTripdataIndex(String indexName) throws Exception {
        // Create index with basic settings
        // Field mappings will be auto-detected from the first document
        client.indices().create(c -> c
            .index(indexName)
            .settings(s -> s
                .numberOfShards("1")
                .numberOfReplicas("0")
            )
        );
    }
    
    /**
     * Converts a GreenTripdata object to a Map for indexing.
     *
     * @param trip green tripdata object
     * @return map representation
     */
    private Map<String, Object> convertGreenTripdataToMap(GreenTripdata trip) {
        Map<String, Object> doc = new HashMap<>();
        
        if (trip.getVendorId() != null) doc.put("vendorId", trip.getVendorId());
        if (trip.getLpepPickupDatetime() != null) doc.put("lpepPickupDatetime", trip.getLpepPickupDatetime().toString());
        if (trip.getLpepDropoffDatetime() != null) doc.put("lpepDropoffDatetime", trip.getLpepDropoffDatetime().toString());
        if (trip.getStoreAndFwdFlag() != null) doc.put("storeAndFwdFlag", trip.getStoreAndFwdFlag());
        if (trip.getRatecodeId() != null) doc.put("ratecodeId", trip.getRatecodeId());
        if (trip.getPuLocationId() != null) doc.put("puLocationId", trip.getPuLocationId());
        if (trip.getDoLocationId() != null) doc.put("doLocationId", trip.getDoLocationId());
        if (trip.getPassengerCount() != null) doc.put("passengerCount", trip.getPassengerCount());
        if (trip.getTripDistance() != null) doc.put("tripDistance", trip.getTripDistance());
        if (trip.getFareAmount() != null) doc.put("fareAmount", trip.getFareAmount());
        if (trip.getExtra() != null) doc.put("extra", trip.getExtra());
        if (trip.getMtaTax() != null) doc.put("mtaTax", trip.getMtaTax());
        if (trip.getTipAmount() != null) doc.put("tipAmount", trip.getTipAmount());
        if (trip.getTollsAmount() != null) doc.put("tollsAmount", trip.getTollsAmount());
        if (trip.getEhailFee() != null) doc.put("ehailFee", trip.getEhailFee());
        if (trip.getImprovementSurcharge() != null) doc.put("improvementSurcharge", trip.getImprovementSurcharge());
        if (trip.getTotalAmount() != null) doc.put("totalAmount", trip.getTotalAmount());
        if (trip.getPaymentType() != null) doc.put("paymentType", trip.getPaymentType());
        if (trip.getTripType() != null) doc.put("tripType", trip.getTripType());
        if (trip.getCongestionSurcharge() != null) doc.put("congestionSurcharge", trip.getCongestionSurcharge());
        
        return doc;
    }
    
    /**
     * Converts a YellowTripdata object to a Map for indexing.
     *
     * @param trip yellow tripdata object
     * @return map representation
     */
    private Map<String, Object> convertYellowTripdataToMap(YellowTripdata trip) {
        Map<String, Object> doc = new HashMap<>();
        
        if (trip.getVendorId() != null) doc.put("vendorId", trip.getVendorId());
        if (trip.getTpepPickupDatetime() != null) doc.put("tpepPickupDatetime", trip.getTpepPickupDatetime().toString());
        if (trip.getTpepDropoffDatetime() != null) doc.put("tpepDropoffDatetime", trip.getTpepDropoffDatetime().toString());
        if (trip.getPassengerCount() != null) doc.put("passengerCount", trip.getPassengerCount());
        if (trip.getTripDistance() != null) doc.put("tripDistance", trip.getTripDistance());
        if (trip.getRatecodeId() != null) doc.put("ratecodeId", trip.getRatecodeId());
        if (trip.getStoreAndFwdFlag() != null) doc.put("storeAndFwdFlag", trip.getStoreAndFwdFlag());
        if (trip.getPuLocationId() != null) doc.put("puLocationId", trip.getPuLocationId());
        if (trip.getDoLocationId() != null) doc.put("doLocationId", trip.getDoLocationId());
        if (trip.getPaymentType() != null) doc.put("paymentType", trip.getPaymentType());
        if (trip.getFareAmount() != null) doc.put("fareAmount", trip.getFareAmount());
        if (trip.getExtra() != null) doc.put("extra", trip.getExtra());
        if (trip.getMtaTax() != null) doc.put("mtaTax", trip.getMtaTax());
        if (trip.getTipAmount() != null) doc.put("tipAmount", trip.getTipAmount());
        if (trip.getTollsAmount() != null) doc.put("tollsAmount", trip.getTollsAmount());
        if (trip.getImprovementSurcharge() != null) doc.put("improvementSurcharge", trip.getImprovementSurcharge());
        if (trip.getTotalAmount() != null) doc.put("totalAmount", trip.getTotalAmount());
        if (trip.getCongestionSurcharge() != null) doc.put("congestionSurcharge", trip.getCongestionSurcharge());
        
        return doc;
    }
}

