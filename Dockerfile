FROM gradle:7.4.2-jdk11 AS gradle-build
RUN ls -la && pwd
FROM maven:3.8.5-jdk-11-slim AS mvn-build
COPY . /root

# Install gettext to use envsubst
RUN apt-get update && \
    apt-get install -y gettext-base && \
    apt-get clean

# Update the web.xml based on SSL selection
RUN if [ "$USE_SSL" = "true" ]; then \
        export SECURITY_CONSTRAINT="<security-constraint><web-resource-collection><web-resource-name>Everything</web-resource-name><url-pattern>/*</url-pattern></web-resource-collection><user-data-constraint><transport-guarantee>CONFIDENTIAL</transport-guarantee></user-data-constraint></security-constraint>"; \
    else \
        export SECURITY_CONSTRAINT=""; \
    fi && \
    envsubst '$SECURITY_CONSTRAINT' < /root/root/WEB-INF/web.xml > /tmp/web.xml.tmp && \
    mv /tmp/web.xml.tmp /root/root/WEB-INF/web.xml && \
    envsubst '$SECURITY_CONSTRAINT' < /root/fedgov-cv-TIMcreator-webapp/src/main/webapp/WEB-INF/web.xml > /tmp/web.xml.tmp && \
    mv /tmp/web.xml.tmp /root/fedgov-cv-TIMcreator-webapp/src/main/webapp/WEB-INF/web.xml && \
    envsubst '$SECURITY_CONSTRAINT' < /root/fedgov-cv-ISDcreator-webapp/src/main/webapp/WEB-INF/web.xml > /tmp/web.xml.tmp && \
    mv /tmp/web.xml.tmp /root/fedgov-cv-ISDcreator-webapp/src/main/webapp/WEB-INF/web.xml

# Run the Maven build
COPY ./build.sh /root
WORKDIR /root
RUN ./build.sh

FROM jetty:9.4.46-jre11-slim
# Install the generated WAR files
COPY --from=mvn-build /root/fedgov-cv-ISDcreator-webapp/target/isd.war /var/lib/jetty/webapps 
COPY --from=mvn-build /root/fedgov-cv-TIMcreator-webapp/target/tim.war /var/lib/jetty/webapps
COPY --from=mvn-build /root/private-resources.war /var/lib/jetty/webapps
COPY --from=mvn-build /root/root.war /var/lib/jetty/webapps

# Create third_party_lib directory and copy the shared libraries to it
RUN mkdir -p /var/lib/jetty/webapps/third_party_lib
COPY --from=mvn-build /root/fedgov-cv-lib-asn1c/third_party_lib/libasn1c.so /var/lib/jetty/webapps/third_party_lib
COPY --from=mvn-build /root/fedgov-cv-lib-asn1c/third_party_lib/libasn1c_x64.so /var/lib/jetty/webapps/third_party_lib
COPY --from=mvn-build /root/fedgov-cv-lib-asn1c/third_party_lib/libasn1c_x86.so /var/lib/jetty/webapps/third_party_lib

# Create library path env
USER root
ENV LD_LIBRARY_PATH=/var/lib/jetty/webapps/third_party_lib
RUN ldconfig

WORKDIR /var/lib/jetty
RUN echo 'log4j2.version=2.23.1' >> start.d/logging-log4j2.ini && \
    java -jar "$JETTY_HOME"/start.jar --create-files

# Conditionally add SSL or non-SSL based on the USE_SSL environment variable
RUN if [ "$USE_SSL" = "true" ]; then \
        java -jar "$JETTY_HOME"/start.jar --add-to-start=https; \
    else \
        java -jar "$JETTY_HOME"/start.jar --add-to-start=http; \
    fi

## If using SSL, change the following two lines to include your keystore files and ssl.ini (sample available in /docs/Sample_ssl.ini):
# COPY keystore* /var/lib/jetty/etc/
# COPY ssl.ini /var/lib/jetty/start.d/
