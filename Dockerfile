FROM gradle:7.4.2-jdk8 AS gradle-build
ARG USE_SSL
RUN ls -la && pwd
FROM maven:3.8.5-jdk-8-slim AS mvn-build
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

FROM jetty:9.4.46-jre8-slim
ARG USE_SSL

# Switch to root for installations and configurations
USER root

# Install GDAL for georeferencing service
RUN apt-get update && \
    apt-get install -y gdal-bin libgdal28  \
    && apt-get autoremove -y \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && rm -rf /tmp/* \
    && rm -rf /var/tmp/*

# Create third_party_lib directory and set permissions early
RUN mkdir -p /var/lib/jetty/webapps/third_party_lib && \
    chown jetty:jetty /var/lib/jetty/webapps

# Install the generated WAR files with chown to jetty user
COPY --from=mvn-build --chown=jetty:jetty /root/fedgov-cv-ISDcreator-webapp/target/isd.war /var/lib/jetty/webapps
COPY --from=mvn-build --chown=jetty:jetty /root/fedgov-cv-TIMcreator-webapp/target/tim.war /var/lib/jetty/webapps
COPY --from=mvn-build --chown=jetty:jetty /root/fedgov-cv-message-validator-webapp/target/validator.war /var/lib/jetty/webapps
COPY --from=mvn-build --chown=jetty:jetty /root/private-resources.war /var/lib/jetty/webapps
COPY --from=mvn-build --chown=jetty:jetty /root/root.war /var/lib/jetty/webapps
COPY --from=mvn-build --chown=jetty:jetty /root/fedgov-cv-map-services-proxy/target/*.war /var/lib/jetty/webapps/msp.war
COPY --from=mvn-build --chown=jetty:jetty /root/fedgov-cv-map-georeferencing/target/*.war /var/lib/jetty/webapps/georef.war

# Copy the shared libraries with chown to jetty user
COPY --from=mvn-build --chown=jetty:jetty /root/fedgov-cv-lib-asn1c/third_party_lib/libasn1c.so /var/lib/jetty/webapps/third_party_lib
COPY --from=mvn-build --chown=jetty:jetty /root/fedgov-cv-lib-asn1c/third_party_lib/libasn1c_decoder.so /var/lib/jetty/webapps/third_party_lib
COPY --from=mvn-build --chown=jetty:jetty /root/fedgov-cv-lib-asn1c/third_party_lib/libasn1c_timdecoder.so /var/lib/jetty/webapps/third_party_lib
COPY --from=mvn-build --chown=jetty:jetty /root/fedgov-cv-lib-asn1c/third_party_lib/libasn1c_timencoder.so /var/lib/jetty/webapps/third_party_lib
COPY --from=mvn-build --chown=jetty:jetty /root/fedgov-cv-lib-asn1c/third_party_lib/libasn1c_x64.so /var/lib/jetty/webapps/third_party_lib
COPY --from=mvn-build --chown=jetty:jetty /root/fedgov-cv-lib-asn1c/third_party_lib/libasn1c_x86.so /var/lib/jetty/webapps/third_party_lib
COPY --from=mvn-build --chown=jetty:jetty /root/fedgov-cv-lib-asn1c/third_party_lib/libasn1c_rga.so /var/lib/jetty/webapps/third_party_lib

# Set library path env and update ldconfig
ENV LD_LIBRARY_PATH=/var/lib/jetty/webapps/third_party_lib
RUN ldconfig

# Pre-create and chown Jetty directories for config/SSL to allow non-root operations
RUN mkdir -p /var/lib/jetty/etc /var/lib/jetty/start.d && \
    chown -R jetty:jetty /var/lib/jetty/etc /var/lib/jetty/start.d

# Switch to non-root jetty user early for remaining file creations
USER jetty

# Configure Jetty
WORKDIR /var/lib/jetty
RUN echo 'log4j2.version=2.23.1' >> start.d/logging-log4j2.ini && \
    java -jar "$JETTY_HOME"/start.jar --create-files

# Prepare files for SSL with chown
COPY --chown=jetty:jetty keystore* /tmp/
COPY --chown=jetty:jetty ssl.ini /tmp/

# Conditionally add SSL or non-SSL based on the USE_SSL environment variable
RUN if [ "$USE_SSL" = "true" ]; then \
        if [ -f /tmp/ssl.ini ]; then \
            java -jar "$JETTY_HOME"/start.jar --add-to-start=https; \
            cp /tmp/keystore* /var/lib/jetty/etc/; \
            cp /tmp/ssl.ini /var/lib/jetty/start.d/; \
        else \
            echo "SSL is enabled, but keystore or ssl.ini files are missing."; \
            exit 1; \
        fi; \
    else \
        java -jar "$JETTY_HOME"/start.jar --add-to-start=http; \
    fi