# OpenSearch Configuration

This directory contains OpenSearch configuration files.

## opensearch.yml

Configuration file for OpenSearch when running in non-TLS mode. This file:
- Disables the security plugin (`plugins.security.disabled: true`)
- Configures OpenSearch for single-node deployment
- Allows plain HTTP access (no TLS required)

## Usage

This configuration file is automatically mounted into the OpenSearch container when using `docker-compose.yml` (non-TLS mode).

For TLS mode, you would need a different configuration that enables security and TLS settings.

