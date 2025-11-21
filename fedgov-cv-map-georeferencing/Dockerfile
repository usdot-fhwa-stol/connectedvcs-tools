# Use OpenJDK base for Java application
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

FROM eclipse-temurin:17-jdk-jammy AS runtime

# Install GDAL and dependencies
RUN apt-get update && \
    apt-get install -y gdal-bin libgdal-dev python3-gdal && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN gdalinfo --version

# Create app directory
WORKDIR /app
# Copy the JAR file
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]