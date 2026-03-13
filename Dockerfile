FROM maven:3.8.5-jdk-8-slim AS dev
ARG USE_SSL
RUN apt-get update && apt-get install -y gettext-base cmake gcc && \
    echo "deb [trusted=yes] http://s3.amazonaws.com/stol-apt-repository develop focal" \
        > /etc/apt/sources.list.d/stol-apt-repository.list && \
    apt-get update && \
    apt-get install -y stol-j2735-2024-1 && \
    apt-get clean

FROM dev AS mvn-build
COPY . /root
WORKDIR /root/fedgov-cv-lib-asn1c
RUN ./build_jni.sh --clean --skip-deps
WORKDIR /root
RUN ./build.sh

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

# # Run the Maven build
# COPY ./build.sh /root
# WORKDIR /root
# RUN ./build.sh

FROM jetty:9.4.46-jre8-slim
ARG USE_SSL

# Switch to root for installations and configurations
USER root

# Install GDAL for georeferencing service
RUN apt-get update && \
    apt-get install -y gdal-bin libgdal28 cmake curl ca-certificates gnupg\
    && apt-get autoremove -y \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && rm -rf /tmp/* \
    && rm -rf /var/tmp/*

RUN echo "deb [trusted=yes] http://s3.amazonaws.com/stol-apt-repository develop focal" \
    > /etc/apt/sources.list.d/stol-apt-repository.list && \
    apt-get update && \
    apt-get install -y stol-j2735-2024-1 && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Create third_party_lib directory and set permissions early
RUN mkdir -p /var/lib/jetty/webapps/third_party_lib && \
    chown root:jetty /var/lib/jetty/webapps && \
    chmod 755 /var/lib/jetty/webapps

# Install the generated WAR files with chown to jetty user
COPY --from=mvn-build --chown=root:jetty --chmod=755 /root/fedgov-cv-ISDcreator-webapp/target/isd.war /var/lib/jetty/webapps
COPY --from=mvn-build --chown=root:jetty --chmod=755 /root/fedgov-cv-TIMcreator-webapp/target/tim.war /var/lib/jetty/webapps
COPY --from=mvn-build --chown=root:jetty --chmod=755 /root/fedgov-cv-message-validator-webapp/target/validator.war /var/lib/jetty/webapps
COPY --from=mvn-build --chown=root:jetty --chmod=755 /root/private-resources.war /var/lib/jetty/webapps
COPY --from=mvn-build --chown=root:jetty --chmod=755 /root/root.war /var/lib/jetty/webapps
COPY --from=mvn-build --chown=root:jetty --chmod=755 /root/fedgov-cv-map-services-proxy/target/*.war /var/lib/jetty/webapps/msp.war
COPY --from=mvn-build --chown=root:jetty --chmod=755 /root/fedgov-cv-map-georeferencing/target/*.war /var/lib/jetty/webapps/georef.war

# Copy the shared libraries with chown to jetty user
COPY --from=mvn-build --chown=root:jetty  --chmod=755  /root/fedgov-cv-lib-asn1c/third_party_lib/libasn1c_jni.so /var/lib/jetty/webapps/third_party_lib

COPY --from=mvn-build --chown=root:jetty --chmod=755 /root/fedgov-cv-lib-asn1c/third_party_lib/libasn1c_jni.so /var/lib/jetty/webapps/third_party_lib/libasn1c.so
COPY --from=mvn-build --chown=root:jetty --chmod=755 /root/fedgov-cv-lib-asn1c/third_party_lib/libasn1c_jni.so /var/lib/jetty/webapps/third_party_lib/libasn1c_decoder.so
COPY --from=mvn-build --chown=root:jetty --chmod=755 /root/fedgov-cv-lib-asn1c/third_party_lib/libasn1c_jni.so /var/lib/jetty/webapps/third_party_lib/libasn1c_timdecoder.so
COPY --from=mvn-build --chown=root:jetty --chmod=755 /root/fedgov-cv-lib-asn1c/third_party_lib/libasn1c_jni.so /var/lib/jetty/webapps/third_party_lib/libasn1c_timencoder.so
COPY --from=mvn-build --chown=root:jetty --chmod=755 /root/fedgov-cv-lib-asn1c/third_party_lib/libasn1c_jni.so /var/lib/jetty/webapps/third_party_lib/libasn1c_rga.so

# Set library path env and update ldconfig
ENV LD_LIBRARY_PATH=/var/lib/jetty/webapps/third_party_lib:/opt/carma/lib
RUN ldconfig

# Prepare Jetty base and restrict write access to config
WORKDIR /var/lib/jetty
RUN mkdir -p /var/lib/jetty/etc /var/lib/jetty/start.d /var/lib/jetty/logs /var/lib/jetty/tmp /var/lib/jetty/work && \
    # Ensure config dirs are owned by root and not writable by jetty
    chown -R root:root /var/lib/jetty/etc /var/lib/jetty/start.d && \
    chmod -R 755 /var/lib/jetty/etc /var/lib/jetty/start.d && \
    # Allow runtime writes only to these dirs
    chown -R jetty:jetty /var/lib/jetty/logs /var/lib/jetty/tmp /var/lib/jetty/work && \
    chmod -R 750 /var/lib/jetty/logs /var/lib/jetty/tmp /var/lib/jetty/work

# Prepare files for SSL (restrict write; allow jetty read of keystore only)
COPY --chown=root:jetty --chmod=640 keystore* /tmp/
COPY --chown=root:root  --chmod=644 ssl.ini /tmp/

# Create Jetty base structure and modules as root, then lock down
RUN echo 'log4j2.version=2.23.1' >> /var/lib/jetty/start.d/logging-log4j2.ini && \
    java -jar "$JETTY_HOME"/start.jar --create-files

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
    fi && \
    # After creating config, ensure config remains read-only and runtime dirs writable
    chown -R root:root /var/lib/jetty/etc /var/lib/jetty/start.d && \
    chmod -R 755 /var/lib/jetty/etc /var/lib/jetty/start.d && \
    chown -R jetty:jetty /var/lib/jetty/logs /var/lib/jetty/tmp /var/lib/jetty/work && \
    chmod -R 750 /var/lib/jetty/logs /var/lib/jetty/tmp /var/lib/jetty/work

# Drop privileges for runtime
USER jetty
