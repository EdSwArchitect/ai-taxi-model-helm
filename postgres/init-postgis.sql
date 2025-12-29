-- Initialize PostGIS extension in the taxidb database
-- This script runs automatically when the database is first created

-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;
CREATE EXTENSION IF NOT EXISTS postgis_raster;

-- Optional: Enable additional PostGIS extensions
-- CREATE EXTENSION IF NOT EXISTS postgis_sfcgal;

-- Verify PostGIS installation
SELECT PostGIS_version();

