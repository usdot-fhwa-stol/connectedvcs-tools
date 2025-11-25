# Fedgov CV Map Georeferencing Service

A high-precision georeferencing REST API service built with Spring Boot 3.2 that provides image georeferencing capabilities using GDAL command-line utilities. This service acts as a bridge between web applications and GDAL, enabling accurate spatial transformation of images using Ground Control Points (GCPs).

## Table of Contents

- [Fedgov CV Map Georeferencing Service](#fedgov-cv-map-georeferencing-service)
  - [Table of Contents](#table-of-contents)
  - [Overview](#overview)
  - [Features](#features)
  - [Requirements](#requirements)
    - [Runtime Requirements](#runtime-requirements)
    - [Development Requirements](#development-requirements)
  - [Installation](#installation)
    - [Local Development Setup](#local-development-setup)
  - [Docker Deployment](#docker-deployment)
    - [Quick Start with Docker](#quick-start-with-docker)
  - [API Documentation](#api-documentation)
    - [Interactive Documentation](#interactive-documentation)
    - [Endpoints](#endpoints)
      - [POST /georeference](#post-georeference)
  - [Usage Examples](#usage-examples)
    - [cURL Example](#curl-example)
  - [Configuration](#configuration)
    - [Application Properties](#application-properties)
  - [Development](#development)
    - [Building from Source](#building-from-source)

## Overview

The Fedgov CV Map Georeferencing Service provides a REST API for georeferencing images with high precision using a minimum of 6 Ground Control Points (GCPs). The service leverages GDAL's powerful geospatial capabilities through a modern Spring Boot interface, supporting various coordinate systems and providing Web Mercator projection for web mapping applications.

## Features

- **High-Precision Georeferencing**: Minimum 6 GCPs for enhanced accuracy
- **GDAL Integration**: Utilizes GDAL command-line utilities for robust geospatial processing
- **Coordinate System Support**: 
  - Input: WGS84 (EPSG:4326)
  - Processing: Web Mercator (EPSG:3857)
  - Output: Configurable extent coordinates
- **VRT-Based Processing**: Three-stage pipeline (VRT → Transform → PNG)
- **RESTful API**: OpenAPI 3.0 documented endpoints
- **Docker Support**: Containerized deployment with optimized GDAL installation

- **Error Handling**: Detailed validation and error reporting
- **Binary Image Support**: Efficient byte-array image handling

## Requirements

### Runtime Requirements
- **Java**: OpenJDK 17 or higher
- **GDAL**: Version 3.0+ with command-line utilities
  - `gdal_translate`
  - `gdalwarp` 
  - `gdalinfo`
- **Memory**: 512MB minimum, 1GB recommended
- **Storage**: Temporary directory access for processing

### Development Requirements
- **Java JDK**: 17+
- **Maven**: 3.6+
- **GDAL**: Development libraries and CLI tools

## Installation

### Local Development Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/usdot-fhwa-stol/connectedvcs-tools.git
   cd connectedvcs-tools/fedgov-cv-map-georeferencing
   ```

2. **Install GDAL (Ubuntu/Debian)**
   ```bash
   sudo apt-get update
   sudo apt-get install gdal-bin libgdal-dev
   ```

3. **Verify GDAL Installation**
   ```bash
   gdalinfo --version
   # Expected output: GDAL 3.x.x, released yyyy/mm/dd
   ```

4. **Build and Run**
   ```bash
   ./mvnw clean package
   java -jar target/fedgov-cv-map-georeferencing-0.0.1-SNAPSHOT.jar
   ```

## Docker Deployment

### Quick Start with Docker

```bash
# Build the Docker image
docker build -t fedgov-cv-georeferencing .

# Run the container
docker run -p 8080:8080 fedgov-cv-georeferencing
```

## API Documentation

### Interactive Documentation

Once the service is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Endpoints

#### POST /georeference

Georeference an image using Ground Control Points.

**Request:**
- **Content-Type**: `multipart/form-data`
- **Parameters**:
  - `image` (file): Image file (PNG, JPEG, TIFF)
  - `gcps` (JSON string): Array of at least 6 Ground Control Points

**GCP Format:**
```json
[
  {
    "pointId": "GCP1",
    "imageX": 100.0,
    "imageY": 200.0,
    "longitude": -77.123456,
    "latitude": 38.654321
  },
  {
    "pointId": "GCP2",
    "imageX": 300.0,
    "imageY": 200.0,
    "longitude": -77.120000,
    "latitude": 38.654321
  }
  // ... minimum 6 GCPs total
]
```

**Response:**
```json
{
  "success": true,
  "message": "Image successfully georeferenced using GDAL VRT and Web Mercator projection with 6 ground control points",
  "details": {
    "originalImageName": "sample.jpg",
    "imageSize": 2048576,
    "processedImageSize": 1876543,
    "gcpCount": 6,
    "processedImageBytes": "base64-encoded-image-data",
    "extent": {
      "minLongitude": -77.126789,
      "maxLongitude": -77.120123,
      "minLatitude": 38.651234,
      "maxLatitude": 38.657890
    },
    "extentProjection": "EPSG:4326",
    "coordinateSystem": "EPSG:3857",
    "processingTimestamp": "2025-11-24T10:30:45",
    "status": "processed_gdal_cli_vrt"
  }
}
```

**Error Responses:**

*400 Bad Request - Insufficient GCPs:*
```json
{
  "success": false,
  "message": "At least 6 Ground Control Points are required for high precision georeferencing, but received 4",
  "error": "INVALID_GCP_COUNT"
}
```

*400 Bad Request - Invalid JSON:*
```json
{
  "success": false,
  "message": "Invalid GCP format: Unexpected character...",
  "error": "JSON_PARSE_ERROR"
}
```

## Usage Examples

### cURL Example

```bash
curl -X POST "http://localhost:8080/georeference" \
  -H "Content-Type: multipart/form-data" \
  -F "image=@/path/to/your/image.jpg" \
  -F 'gcps=[
    {"pointId":"GCP1","imageX":100,"imageY":200,"longitude":-77.036,"latitude":38.895},
    {"pointId":"GCP2","imageX":300,"imageY":200,"longitude":-77.030,"latitude":38.895},
    {"pointId":"GCP3","imageX":100,"imageY":400,"longitude":-77.036,"latitude":38.890},
    {"pointId":"GCP4","imageX":300,"imageY":400,"longitude":-77.030,"latitude":38.890},
    {"pointId":"GCP5","imageX":200,"imageY":300,"longitude":-77.033,"latitude":38.892},
    {"pointId":"GCP6","imageX":250,"imageY":350,"longitude":-77.032,"latitude":38.891}
  ]'
```

## Configuration

### Application Properties

```yaml
# application.yaml
spring:
  application:
    name: fedgov-cv-map-georeferencing
  jackson:
    serialization:
      FAIL_ON_EMPTY_BEANS: false
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 60MB

server:
  port: 8080
  
# Optional: Custom configurations
georeferencing:
  temp-dir: /tmp/georef
  max-gcps: 50
  timeout-seconds: 300
```
## Development
### Building from Source

```bash
# Clone repository
git clone https://github.com/usdot-fhwa-stol/connectedvcs-tools.git
cd connectedvcs-tools/fedgov-cv-map-georeferencing

# Run tests
./mvnw test

# Package application
./mvnw clean package

# Run locally
java -jar target/fedgov-cv-map-georeferencing-0.0.1-SNAPSHOT.jar
```
