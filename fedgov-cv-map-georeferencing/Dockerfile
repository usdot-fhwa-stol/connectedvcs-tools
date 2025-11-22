FROM eclipse-temurin:17-jdk-jammy AS build

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests 

# Use smaller JRE runtime instead of JDK
FROM eclipse-temurin:17-jre-jammy AS runtime

# Install only essential GDAL components and clean up in single layer
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    gdal-bin \
    libgdal30 \
    && apt-get autoremove -y \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && rm -rf /tmp/* \
    && rm -rf /var/tmp/*

RUN gdalinfo --version

# Create app directory
WORKDIR /app
# Copy the JAR file
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]