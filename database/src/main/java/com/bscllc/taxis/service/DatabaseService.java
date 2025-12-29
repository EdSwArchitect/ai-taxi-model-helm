package com.bscllc.taxis.service;

import com.bscllc.taxis.model.GreenTripdata;
import com.bscllc.taxis.model.YellowTripdata;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Service class for connecting to PostgreSQL and storing trip data.
 * Supports both TLS and non-TLS connections.
 * Table names are based on the schema type (green or yellow).
 */
public class DatabaseService {
    
    public enum SchemaType {
        GREEN("green_tripdata"),
        YELLOW("yellow_tripdata");
        
        private final String tableName;
        
        SchemaType(String tableName) {
            this.tableName = tableName;
        }
        
        public String getTableName() {
            return tableName;
        }
    }
    
    private final Connection connection;
    private final String host;
    private final int port;
    private final String database;
    private final boolean useTls;
    private final SchemaType schemaType;
    private final boolean tableCreated;
    
    /**
     * Builder class for creating DatabaseService instances.
     */
    public static class Builder {
        private String host = "localhost";
        private int port = 5432;
        private String database = "taxidb";
        private String username = "postgres";
        private String password = "postgres";
        private boolean useTls = false;
        private String certPath;
        private SchemaType schemaType = SchemaType.GREEN;
        private boolean createTableIfNotExists = true;
        
        /**
         * Sets the PostgreSQL host.
         *
         * @param host hostname or IP address
         * @return this builder
         */
        public Builder host(String host) {
            this.host = host;
            return this;
        }
        
        /**
         * Sets the PostgreSQL port.
         *
         * @param port port number
         * @return this builder
         */
        public Builder port(int port) {
            this.port = port;
            return this;
        }
        
        /**
         * Sets the database name.
         *
         * @param database database name
         * @return this builder
         */
        public Builder database(String database) {
            this.database = database;
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
         * Enables or disables TLS.
         *
         * @param useTls true to use TLS, false for plain connection
         * @return this builder
         */
        public Builder useTls(boolean useTls) {
            this.useTls = useTls;
            return this;
        }
        
        /**
         * Sets the path to the SSL certificate file.
         *
         * @param certPath path to certificate file
         * @return this builder
         */
        public Builder certPath(String certPath) {
            this.certPath = certPath;
            return this;
        }
        
        /**
         * Sets the schema type (GREEN or YELLOW).
         *
         * @param schemaType schema type
         * @return this builder
         */
        public Builder schemaType(SchemaType schemaType) {
            this.schemaType = schemaType;
            return this;
        }
        
        /**
         * Sets whether to create table if it doesn't exist.
         *
         * @param createTableIfNotExists true to create table automatically
         * @return this builder
         */
        public Builder createTableIfNotExists(boolean createTableIfNotExists) {
            this.createTableIfNotExists = createTableIfNotExists;
            return this;
        }
        
        /**
         * Builds the DatabaseService instance.
         *
         * @return configured DatabaseService
         * @throws SQLException if connection fails
         */
        public DatabaseService build() throws SQLException {
            return new DatabaseService(this);
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
    
    private DatabaseService(Builder builder) throws SQLException {
        this.host = builder.host;
        this.port = builder.port;
        this.database = builder.database;
        this.useTls = builder.useTls;
        this.schemaType = builder.schemaType;
        this.connection = createConnection(builder);
        
        if (builder.createTableIfNotExists) {
            this.tableCreated = createTableIfNotExists();
        } else {
            this.tableCreated = false;
        }
    }
    
    /**
     * Checks if the table was created during initialization.
     *
     * @return true if table was created, false if it already existed or wasn't created
     */
    public boolean wasTableCreated() {
        return tableCreated;
    }
    
    /**
     * Creates a PostgreSQL connection with optional TLS support.
     */
    private Connection createConnection(Builder builder) throws SQLException {
        String url = buildConnectionUrl(builder);
        Properties props = new Properties();
        props.setProperty("user", builder.username);
        props.setProperty("password", builder.password);
        
        if (builder.useTls) {
            props.setProperty("ssl", "true");
            props.setProperty("sslmode", "prefer");
            
            if (builder.certPath != null && !builder.certPath.isEmpty()) {
                props.setProperty("sslcert", builder.certPath);
                props.setProperty("sslmode", "verify-ca");
            } else {
                // For self-signed certificates, use require mode
                props.setProperty("sslmode", "require");
            }
        }
        
        return DriverManager.getConnection(url, props);
    }
    
    /**
     * Builds the PostgreSQL connection URL.
     */
    private String buildConnectionUrl(Builder builder) {
        StringBuilder url = new StringBuilder("jdbc:postgresql://");
        url.append(builder.host).append(":").append(builder.port);
        url.append("/").append(builder.database);
        return url.toString();
    }
    
    /**
     * Creates the table if it doesn't exist based on the schema type.
     * 
     * @return true if table was created, false if it already existed
     */
    private boolean createTableIfNotExists() throws SQLException {
        String tableName = schemaType.getTableName();
        
        // Check if table already exists
        try (Statement stmt = connection.createStatement()) {
            String checkTableSql = "SELECT EXISTS (" +
                    "SELECT FROM information_schema.tables " +
                    "WHERE table_schema = 'public' " +
                    "AND table_name = '" + tableName + "'" +
                    ")";
            try (ResultSet rs = stmt.executeQuery(checkTableSql)) {
                if (rs.next() && rs.getBoolean(1)) {
                    // Table already exists
                    return false;
                }
            }
        }
        
        // Table doesn't exist, create it
        String createTableSql = getCreateTableSql(schemaType);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSql);
            return true;
        }
    }
    
    /**
     * Gets the CREATE TABLE SQL statement based on schema type.
     */
    private String getCreateTableSql(SchemaType schemaType) {
        String tableName = schemaType.getTableName();
        
        if (schemaType == SchemaType.GREEN) {
            return "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                    "id SERIAL PRIMARY KEY, " +
                    "vendor_id INTEGER, " +
                    "lpep_pickup_datetime TIMESTAMP, " +
                    "lpep_dropoff_datetime TIMESTAMP, " +
                    "store_and_fwd_flag VARCHAR(1), " +
                    "ratecode_id INTEGER, " +
                    "pu_location_id INTEGER, " +
                    "do_location_id INTEGER, " +
                    "passenger_count INTEGER, " +
                    "trip_distance DOUBLE PRECISION, " +
                    "fare_amount DOUBLE PRECISION, " +
                    "extra DOUBLE PRECISION, " +
                    "mta_tax DOUBLE PRECISION, " +
                    "tip_amount DOUBLE PRECISION, " +
                    "tolls_amount DOUBLE PRECISION, " +
                    "ehail_fee DOUBLE PRECISION, " +
                    "improvement_surcharge DOUBLE PRECISION, " +
                    "total_amount DOUBLE PRECISION, " +
                    "payment_type INTEGER, " +
                    "trip_type INTEGER, " +
                    "congestion_surcharge DOUBLE PRECISION" +
                    ")";
        } else {
            return "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                    "id SERIAL PRIMARY KEY, " +
                    "vendor_id INTEGER, " +
                    "tpep_pickup_datetime TIMESTAMP, " +
                    "tpep_dropoff_datetime TIMESTAMP, " +
                    "passenger_count INTEGER, " +
                    "trip_distance DOUBLE PRECISION, " +
                    "ratecode_id INTEGER, " +
                    "store_and_fwd_flag VARCHAR(1), " +
                    "pu_location_id INTEGER, " +
                    "do_location_id INTEGER, " +
                    "payment_type INTEGER, " +
                    "fare_amount DOUBLE PRECISION, " +
                    "extra DOUBLE PRECISION, " +
                    "mta_tax DOUBLE PRECISION, " +
                    "tip_amount DOUBLE PRECISION, " +
                    "tolls_amount DOUBLE PRECISION, " +
                    "improvement_surcharge DOUBLE PRECISION, " +
                    "total_amount DOUBLE PRECISION, " +
                    "congestion_surcharge DOUBLE PRECISION" +
                    ")";
        }
    }
    
    /**
     * Inserts a GreenTripdata record into the database.
     *
     * @param tripData trip data to insert
     * @return generated ID of the inserted record
     * @throws SQLException if insert fails
     */
    public Long insert(GreenTripdata tripData) throws SQLException {
        if (schemaType != SchemaType.GREEN) {
            throw new IllegalStateException("Cannot insert GreenTripdata into " + schemaType.getTableName() + " table");
        }
        
        String sql = "INSERT INTO " + schemaType.getTableName() + " " +
                "(vendor_id, lpep_pickup_datetime, lpep_dropoff_datetime, store_and_fwd_flag, " +
                "ratecode_id, pu_location_id, do_location_id, passenger_count, trip_distance, " +
                "fare_amount, extra, mta_tax, tip_amount, tolls_amount, ehail_fee, " +
                "improvement_surcharge, total_amount, payment_type, trip_type, congestion_surcharge) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setGreenTripdataParameters(pstmt, tripData);
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Inserts a YellowTripdata record into the database.
     *
     * @param tripData trip data to insert
     * @return generated ID of the inserted record
     * @throws SQLException if insert fails
     */
    public Long insert(YellowTripdata tripData) throws SQLException {
        if (schemaType != SchemaType.YELLOW) {
            throw new IllegalStateException("Cannot insert YellowTripdata into " + schemaType.getTableName() + " table");
        }
        
        String sql = "INSERT INTO " + schemaType.getTableName() + " " +
                "(vendor_id, tpep_pickup_datetime, tpep_dropoff_datetime, passenger_count, " +
                "trip_distance, ratecode_id, store_and_fwd_flag, pu_location_id, do_location_id, " +
                "payment_type, fare_amount, extra, mta_tax, tip_amount, tolls_amount, " +
                "improvement_surcharge, total_amount, congestion_surcharge) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setYellowTripdataParameters(pstmt, tripData);
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Batch inserts GreenTripdata records.
     *
     * @param tripDataList list of trip data to insert
     * @return array of update counts
     * @throws SQLException if batch insert fails
     */
    public int[] batchInsertGreen(List<GreenTripdata> tripDataList) throws SQLException {
        if (schemaType != SchemaType.GREEN) {
            throw new IllegalStateException("Cannot insert GreenTripdata into " + schemaType.getTableName() + " table");
        }
        
        String sql = "INSERT INTO " + schemaType.getTableName() + " " +
                "(vendor_id, lpep_pickup_datetime, lpep_dropoff_datetime, store_and_fwd_flag, " +
                "ratecode_id, pu_location_id, do_location_id, passenger_count, trip_distance, " +
                "fare_amount, extra, mta_tax, tip_amount, tolls_amount, ehail_fee, " +
                "improvement_surcharge, total_amount, payment_type, trip_type, congestion_surcharge) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (GreenTripdata tripData : tripDataList) {
                setGreenTripdataParameters(pstmt, tripData);
                pstmt.addBatch();
            }
            return pstmt.executeBatch();
        }
    }
    
    /**
     * Batch inserts YellowTripdata records.
     *
     * @param tripDataList list of trip data to insert
     * @return array of update counts
     * @throws SQLException if batch insert fails
     */
    public int[] batchInsertYellow(List<YellowTripdata> tripDataList) throws SQLException {
        if (schemaType != SchemaType.YELLOW) {
            throw new IllegalStateException("Cannot insert YellowTripdata into " + schemaType.getTableName() + " table");
        }
        
        String sql = "INSERT INTO " + schemaType.getTableName() + " " +
                "(vendor_id, tpep_pickup_datetime, tpep_dropoff_datetime, passenger_count, " +
                "trip_distance, ratecode_id, store_and_fwd_flag, pu_location_id, do_location_id, " +
                "payment_type, fare_amount, extra, mta_tax, tip_amount, tolls_amount, " +
                "improvement_surcharge, total_amount, congestion_surcharge) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (YellowTripdata tripData : tripDataList) {
                setYellowTripdataParameters(pstmt, tripData);
                pstmt.addBatch();
            }
            return pstmt.executeBatch();
        }
    }
    
    /**
     * Sets parameters for GreenTripdata prepared statement.
     */
    private void setGreenTripdataParameters(PreparedStatement pstmt, GreenTripdata tripData) throws SQLException {
        int index = 1;
        pstmt.setObject(index++, tripData.getVendorId());
        pstmt.setObject(index++, tripData.getLpepPickupDatetime() != null ? 
            Timestamp.valueOf(tripData.getLpepPickupDatetime()) : null);
        pstmt.setObject(index++, tripData.getLpepDropoffDatetime() != null ? 
            Timestamp.valueOf(tripData.getLpepDropoffDatetime()) : null);
        pstmt.setString(index++, tripData.getStoreAndFwdFlag());
        pstmt.setObject(index++, tripData.getRatecodeId());
        pstmt.setObject(index++, tripData.getPuLocationId());
        pstmt.setObject(index++, tripData.getDoLocationId());
        pstmt.setObject(index++, tripData.getPassengerCount());
        pstmt.setObject(index++, tripData.getTripDistance());
        pstmt.setObject(index++, tripData.getFareAmount());
        pstmt.setObject(index++, tripData.getExtra());
        pstmt.setObject(index++, tripData.getMtaTax());
        pstmt.setObject(index++, tripData.getTipAmount());
        pstmt.setObject(index++, tripData.getTollsAmount());
        pstmt.setObject(index++, tripData.getEhailFee());
        pstmt.setObject(index++, tripData.getImprovementSurcharge());
        pstmt.setObject(index++, tripData.getTotalAmount());
        pstmt.setObject(index++, tripData.getPaymentType());
        pstmt.setObject(index++, tripData.getTripType());
        pstmt.setObject(index++, tripData.getCongestionSurcharge());
    }
    
    /**
     * Sets parameters for YellowTripdata prepared statement.
     */
    private void setYellowTripdataParameters(PreparedStatement pstmt, YellowTripdata tripData) throws SQLException {
        int index = 1;
        pstmt.setObject(index++, tripData.getVendorId());
        pstmt.setObject(index++, tripData.getTpepPickupDatetime() != null ? 
            Timestamp.valueOf(tripData.getTpepPickupDatetime()) : null);
        pstmt.setObject(index++, tripData.getTpepDropoffDatetime() != null ? 
            Timestamp.valueOf(tripData.getTpepDropoffDatetime()) : null);
        pstmt.setObject(index++, tripData.getPassengerCount());
        pstmt.setObject(index++, tripData.getTripDistance());
        pstmt.setObject(index++, tripData.getRatecodeId());
        pstmt.setString(index++, tripData.getStoreAndFwdFlag());
        pstmt.setObject(index++, tripData.getPuLocationId());
        pstmt.setObject(index++, tripData.getDoLocationId());
        pstmt.setObject(index++, tripData.getPaymentType());
        pstmt.setObject(index++, tripData.getFareAmount());
        pstmt.setObject(index++, tripData.getExtra());
        pstmt.setObject(index++, tripData.getMtaTax());
        pstmt.setObject(index++, tripData.getTipAmount());
        pstmt.setObject(index++, tripData.getTollsAmount());
        pstmt.setObject(index++, tripData.getImprovementSurcharge());
        pstmt.setObject(index++, tripData.getTotalAmount());
        pstmt.setObject(index++, tripData.getCongestionSurcharge());
    }
    
    /**
     * Gets the database connection.
     *
     * @return database connection
     */
    public Connection getConnection() {
        return connection;
    }
    
    /**
     * Gets the schema type.
     *
     * @return schema type
     */
    public SchemaType getSchemaType() {
        return schemaType;
    }
    
    /**
     * Gets the table name.
     *
     * @return table name
     */
    public String getTableName() {
        return schemaType.getTableName();
    }
    
    /**
     * Checks if the connection is valid.
     *
     * @return true if connection is valid
     */
    public boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Closes the database connection.
     *
     * @throws SQLException if closing fails
     */
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}

