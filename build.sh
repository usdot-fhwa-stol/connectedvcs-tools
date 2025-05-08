# Build Steps
cd ./fedgov-cv-parent
mvn install -DskipTests=true

cd ../fedgov-cv-lib-asn1c/
mvn install -DskipTests=true

cd ../fedgov-cv-mapencoder/
mvn install -DskipTests=true

cd ../fedgov-cv-rgaencoder/
mvn install -DskipTests=true

cd ../fedgov-cv-message-builder/
mvn install -DskipTests=true

cd ../fedgov-cv-ISDcreator-webapp/
mvn install -DskipTests=true

cd ../fedgov-cv-TIMcreator-webapp/
mvn install -DskipTests=true

cd ../fedgov-cv-map-services-proxy/
mvn install -DskipTests=true

jar cvf ../private-resources.war -C ../private-resources .
jar cvf ../root.war -C ../root .

