#!/bin/bash

# Script to generate a self-signed TLS certificate for localhost
# Usage: ./generate-tls-cert.sh [key-size] [validity-days] [keystore-password]

set -e

# Configuration
KEY_SIZE=${1:-2048}
VALIDITY_DAYS=${2:-365}
KEYSTORE_PASSWORD=${3:-changeit}
CERT_DIR="certs"
KEY_FILE="${CERT_DIR}/localhost.key"
CERT_FILE="${CERT_DIR}/localhost.crt"
PEM_FILE="${CERT_DIR}/localhost.pem"
CSR_FILE="${CERT_DIR}/localhost.csr"
P12_FILE="${CERT_DIR}/localhost.p12"
JKS_FILE="${CERT_DIR}/localhost.jks"
TRUSTSTORE_FILE="${CERT_DIR}/localhost-truststore.jks"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Generating self-signed TLS certificate for localhost...${NC}"

# Create certs directory if it doesn't exist
mkdir -p "${CERT_DIR}"

# Generate private key
echo -e "${YELLOW}Generating private key (${KEY_SIZE} bits)...${NC}"
openssl genrsa -out "${KEY_FILE}" ${KEY_SIZE}

# Set secure permissions on private key
chmod 600 "${KEY_FILE}"

# Create certificate configuration file
CONFIG_FILE="${CERT_DIR}/cert.conf"
cat > "${CONFIG_FILE}" <<EOF
[req]
default_bits = ${KEY_SIZE}
prompt = no
default_md = sha256
distinguished_name = dn
req_extensions = v3_req

[dn]
CN = localhost
O = Local Development
OU = IT Department
L = Local
ST = Local
C = US

[v3_req]
basicConstraints = CA:FALSE
keyUsage = nonRepudiation, digitalSignature, keyEncipherment
subjectAltName = @alt_names

[alt_names]
DNS.1 = localhost
DNS.2 = *.localhost
DNS.3 = localhost.localdomain
IP.1 = 127.0.0.1
IP.2 = ::1
EOF

# Generate self-signed certificate
echo -e "${YELLOW}Generating self-signed certificate (valid for ${VALIDITY_DAYS} days)...${NC}"
openssl req -new -x509 -key "${KEY_FILE}" -out "${CERT_FILE}" \
    -days ${VALIDITY_DAYS} -config "${CONFIG_FILE}" \
    -extensions v3_req

# Create PEM file (combined key and certificate)
echo -e "${YELLOW}Creating PEM file (key + certificate)...${NC}"
cat "${KEY_FILE}" "${CERT_FILE}" > "${PEM_FILE}"
chmod 600 "${PEM_FILE}"

# Generate CSR (optional, for reference)
echo -e "${YELLOW}Generating Certificate Signing Request (CSR)...${NC}"
openssl req -new -key "${KEY_FILE}" -out "${CSR_FILE}" -config "${CONFIG_FILE}"

# Generate PKCS12 keystore
echo -e "${YELLOW}Generating PKCS12 keystore...${NC}"
openssl pkcs12 -export \
    -in "${CERT_FILE}" \
    -inkey "${KEY_FILE}" \
    -out "${P12_FILE}" \
    -name "localhost" \
    -password "pass:${KEYSTORE_PASSWORD}" \
    -noiter -nomaciter

chmod 600 "${P12_FILE}"

# Generate JKS keystore from PKCS12
echo -e "${YELLOW}Generating JKS keystore...${NC}"
if command -v keytool &> /dev/null; then
    keytool -importkeystore \
        -srckeystore "${P12_FILE}" \
        -srcstoretype PKCS12 \
        -srcstorepass "${KEYSTORE_PASSWORD}" \
        -destkeystore "${JKS_FILE}" \
        -deststoretype JKS \
        -deststorepass "${KEYSTORE_PASSWORD}" \
        -noprompt
    
    chmod 600 "${JKS_FILE}"
    
    # Generate truststore (contains only the certificate, no private key)
    echo -e "${YELLOW}Generating JKS truststore...${NC}"
    keytool -import \
        -alias "localhost" \
        -file "${CERT_FILE}" \
        -keystore "${TRUSTSTORE_FILE}" \
        -storetype JKS \
        -storepass "${KEYSTORE_PASSWORD}" \
        -noprompt
    
    chmod 600 "${TRUSTSTORE_FILE}"
else
    echo -e "${YELLOW}Warning: keytool not found. Skipping JKS keystore generation.${NC}"
    echo -e "${YELLOW}Install Java JDK to generate JKS keystores.${NC}"
fi

# Display certificate information
echo -e "\n${GREEN}Certificate generated successfully!${NC}\n"
echo -e "${YELLOW}Certificate Details:${NC}"
openssl x509 -in "${CERT_FILE}" -text -noout | grep -A 2 "Subject:\|Issuer:\|Validity\|Subject Alternative Name"

echo -e "\n${GREEN}Files created:${NC}"
echo "  - Private Key:     ${KEY_FILE}"
echo "  - Certificate:      ${CERT_FILE}"
echo "  - PEM (combined):   ${PEM_FILE}"
echo "  - CSR:             ${CSR_FILE}"
echo "  - Config:          ${CONFIG_FILE}"
if [ -f "${P12_FILE}" ]; then
    echo "  - PKCS12 KeyStore: ${P12_FILE}"
fi
if [ -f "${JKS_FILE}" ]; then
    echo "  - JKS KeyStore:    ${JKS_FILE}"
fi
if [ -f "${TRUSTSTORE_FILE}" ]; then
    echo "  - JKS TrustStore:  ${TRUSTSTORE_FILE}"
fi

echo -e "\n${YELLOW}Keystore Information:${NC}"
echo "  - Password:        ${KEYSTORE_PASSWORD}"
echo "  - Alias:           localhost"

echo -e "\n${YELLOW}Java Usage Examples:${NC}"
if [ -f "${JKS_FILE}" ]; then
    echo "  - Use JKS in Spring Boot (application.properties):"
    echo "    server.ssl.key-store=${JKS_FILE}"
    echo "    server.ssl.key-store-password=${KEYSTORE_PASSWORD}"
    echo "    server.ssl.key-store-type=JKS"
    echo "    server.ssl.key-alias=localhost"
    echo ""
    echo "  - Use JKS in Tomcat (server.xml):"
    echo "    <Connector port=\"8443\" protocol=\"org.apache.coyote.http11.Http11NioProtocol\""
    echo "               keystoreFile=\"\${catalina.base}/conf/${JKS_FILE}\""
    echo "               keystorePass=\"${KEYSTORE_PASSWORD}\""
    echo "               keyAlias=\"localhost\" />"
    echo ""
fi
if [ -f "${P12_FILE}" ]; then
    echo "  - Use PKCS12 in Spring Boot (application.properties):"
    echo "    server.ssl.key-store=${P12_FILE}"
    echo "    server.ssl.key-store-password=${KEYSTORE_PASSWORD}"
    echo "    server.ssl.key-store-type=PKCS12"
    echo "    server.ssl.key-alias=localhost"
    echo ""
fi
if [ -f "${TRUSTSTORE_FILE}" ]; then
    echo "  - Use TrustStore (for client connections):"
    echo "    System.setProperty(\"javax.net.ssl.trustStore\", \"${TRUSTSTORE_FILE}\");"
    echo "    System.setProperty(\"javax.net.ssl.trustStorePassword\", \"${KEYSTORE_PASSWORD}\");"
    echo ""
fi

echo -e "${YELLOW}Other Usage Examples:${NC}"
echo "  - Verify cert:     openssl x509 -in ${CERT_FILE} -text -noout"
echo "  - Test connection: openssl s_client -connect localhost:8443 -CAfile ${CERT_FILE}"
if [ -f "${JKS_FILE}" ]; then
    echo "  - List keystore:   keytool -list -v -keystore ${JKS_FILE} -storepass ${KEYSTORE_PASSWORD}"
fi

echo -e "\n${GREEN}Done!${NC}"

