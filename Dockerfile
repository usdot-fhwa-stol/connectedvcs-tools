FROM gradle:7.4.2-jdk8 AS gradle-build
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
RUN cd /root/fedgov-cv-parent \
    && mvn install -DskipTests
RUN cd /root/fedgov-cv-lib-asn1c \
    && mvn install -DskipTests
RUN cd /root/fedgov-cv-mapencoder \
    && mvn install -DskipTests
RUN cd /root/fedgov-cv-message-builder \
    && mvn install -DskipTests
RUN cd /root/fedgov-cv-ISDcreator-webapp \
    && mvn install -DskipTests
RUN cd /root/fedgov-cv-TIMcreator-webapp \
    && mvn install -DskipTests
RUN cd /root/fedgov-cv-map-services-proxy \
    && mvn clean install -DskipTests
RUN jar cvf /root/private-resources.war -C /root/private-resources .
RUN jar cvf /root/root.war -C /root/root .


FROM jetty:9.4.46-jre8-slim
# Install the generated WAR files
COPY --from=mvn-build /root/fedgov-cv-ISDcreator-webapp/target/isd.war /var/lib/jetty/webapps
COPY --from=mvn-build /root/fedgov-cv-TIMcreator-webapp/target/tim.war /var/lib/jetty/webapps
COPY --from=mvn-build /root/private-resources.war /var/lib/jetty/webapps
COPY --from=mvn-build /root/root.war /var/lib/jetty/webapps

COPY --from=mvn-build /root/fedgov-cv-map-services-proxy/target/*.war /var/lib/jetty/webapps/msp.war


# Create third_party_lib directory and copy the shared libraries to it
RUN mkdir -p /var/lib/jetty/webapps/third_party_lib
COPY --from=mvn-build /root/fedgov-cv-lib-asn1c/third_party_lib/libasn1c.so /var/lib/jetty/webapps/third_party_lib
COPY --from=mvn-build /root/fedgov-cv-lib-asn1c/third_party_lib/libasn1c_x64.so /var/lib/jetty/webapps/third_party_lib
COPY --from=mvn-build /root/fedgov-cv-lib-asn1c/third_party_lib/libasn1c_x86.so /var/lib/jetty/webapps/third_party_lib
COPY --from=mvn-build /root/fedgov-cv-lib-asn1c/third_party_lib/libasn1c_rga.so /var/lib/jetty/webapps/third_party_lib

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

RUN java -jar $JETTY_HOME/start.jar --add-to-start=https
COPY --from=gradle-build /home/gradle/CARMASensitive/maptool/keystore* /var/lib/jetty/etc/
COPY --from=gradle-build /home/gradle/CARMASensitive/maptool/ssl.ini /var/lib/jetty/start.d/

