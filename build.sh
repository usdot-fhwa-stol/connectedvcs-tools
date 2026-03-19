#!/bin/bash
set -e

SKIP_TESTS=true

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --run-tests)
            SKIP_TESTS=false
            shift
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: $0 [--run-tests]"
            exit 1
            ;;
    esac
done

if [[ "$SKIP_TESTS" = "true" ]]; then
    MVN_ARGS="-DskipTests=true"
    echo "Building without tests..."
else
    MVN_ARGS=""
    export LD_LIBRARY_PATH="./fedgov-cv-lib-asn1c/lib"
    echo "Building with tests..."
fi

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$BASE_DIR"

cd fedgov-cv-parent
mvn clean install $MVN_ARGS

cd ../fedgov-cv-lib-asn1c
mvn clean install $MVN_ARGS

cd ../fedgov-cv-asn1decoder
mvn clean install $MVN_ARGS

cd ../fedgov-cv-mapencoder
mvn clean install $MVN_ARGS

cd ../fedgov-cv-rgaencoder
mvn clean install $MVN_ARGS

cd ../fedgov-cv-timencoder
mvn clean install $MVN_ARGS

cd ../fedgov-cv-message-builder
mvn clean install $MVN_ARGS

cd ../fedgov-cv-ISDcreator-webapp
mvn clean install $MVN_ARGS

cd ../fedgov-cv-TIMcreator-webapp
mvn clean install $MVN_ARGS

cd ../fedgov-cv-map-services-proxy
mvn clean install $MVN_ARGS

cd ../fedgov-cv-message-validator-webapp
mvn clean install $MVN_ARGS

cd ../fedgov-cv-map-georeferencing
mvn clean install $MVN_ARGS

jar cvf ../private-resources.war -C ../private-resources .
jar cvf ../root.war -C ../root .

echo "Build complete."