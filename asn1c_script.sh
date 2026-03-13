# Set up path
LD_LIBRARY_PATH="/workspaces/connectedvcs-tools/fedgov-cv-lib-asn1c/lib"
export LD_LIBRARY_PATH

# Maven run parent 
cd /workspaces/connectedvcs-tools/fedgov-cv-parent/
mvn clean install

# Maven run asn1c 
cd /workspaces/connectedvcs-tools/fedgov-cv-lib-asn1c/
mvn clean install

# Maven run asn1c decoder
cd /workspaces/connectedvcs-tools/fedgov-cv-asn1decoder/
mvn clean install

# Maven run unit test
cd /workspaces/connectedvcs-tools/fedgov-cv-mapencoder/
mvn clean install

# Maven run unit test
cd /workspaces/connectedvcs-tools/fedgov-cv-rgaencoder/
mvn clean install

cd /workspaces/connectedvcs-tools/fedgov-cv-timencoder/
mvn clean install

cd /workspaces/connectedvcs-tools/fedgov-cv-message-builder/
mvn clean install

# Maven run webapp validator
cd /workspaces/connectedvcs-tools/fedgov-cv-message-validator-webapp/
mvn clean install
