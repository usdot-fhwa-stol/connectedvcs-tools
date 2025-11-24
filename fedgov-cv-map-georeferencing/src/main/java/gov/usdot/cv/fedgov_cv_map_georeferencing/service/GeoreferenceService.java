/*
 * Copyright (C) 2025 LEIDOS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.usdot.cv.fedgov_cv_map_georeferencing.service;

import gov.usdot.cv.fedgov_cv_map_georeferencing.config.GeoreferenceProperties;
import gov.usdot.cv.fedgov_cv_map_georeferencing.dto.GCP;
import gov.usdot.cv.fedgov_cv_map_georeferencing.gdal.GdalFacade;
import gov.usdot.cv.fedgov_cv_map_georeferencing.gdal.GdalException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class GeoreferenceService {
    private static final Logger logger = LoggerFactory.getLogger(GeoreferenceService.class);
    
    // Web Mercator (EPSG:3857) maximum extent coordinate - half Earth's circumference in meters
    private static final double WEB_MERCATOR_MAX_EXTENT = 20037508.34;
    
    private final GdalFacade gdalFacade;
    private final GeoreferenceProperties georeferenceProperties;
    
    public GeoreferenceService(GdalFacade gdalFacade, GeoreferenceProperties georeferenceProperties) {
        this.gdalFacade = gdalFacade;
        this.georeferenceProperties = georeferenceProperties;
        
        // Validate GDAL availability on service initialization
        try {
            gdalFacade.validateGdalUtilities();
            logger.info("GDAL facade initialized successfully. Version: {}", gdalFacade.getGdalVersion());
        } catch (GdalException e) {
            logger.warn("GDAL validation failed during service initialization: {}", e.getMessage());
        }
        logger.info(this.georeferenceProperties.toString());
    }
    
    /**
     * Processes an image with ground control points to create a georeferenced image.
     * This method uses GDAL command-line utilities (gdal_translate and gdalwarp) for georeferencing.
     * 
     * @param image The input image file to be georeferenced
     * @param gcps List of ground control points containing pixel and geographic coordinates
     * @return A map containing the processing results including the georeferenced image data
     * @throws Exception if processing fails
     */
    public Map<String, Object> process(MultipartFile image, List<GCP> gcps) throws Exception {
        // Validate input parameters
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image file is required and cannot be empty");
        }
        // Validate image requirements
        String originalFilename = image.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("Image filename is required");
        }

        // Validate image format against supported formats from configuration
        String contentType = image.getContentType();
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("Image content type cannot be determined");
        }
        
        if (!validateImageFormat(contentType)) {
            throw new IllegalArgumentException("Unsupported image format: " + contentType + 
            ". Supported formats: " + String.join(", ", georeferenceProperties.getImage().getSupportedFormats()));
        }

        
        //Validate image size against max size from configuration
        String maxSizeStr = georeferenceProperties.getImage().getMaxSize();
        if (maxSizeStr != null && !maxSizeStr.trim().isEmpty()) {
            long maxSizeBytes = parseSizeToBytes(maxSizeStr);
            if (image.getSize() > maxSizeBytes) {
                throw new IllegalArgumentException("Image size exceeds the maximum allowed size of " + maxSizeStr);
            }
        }

        if (gcps == null || gcps.size() < georeferenceProperties.getGcp().getMinCount()) {
            throw new IllegalArgumentException("At least " + georeferenceProperties.getGcp().getMinCount()
                    + " ground control points are required for high precision georeferencing. Provided: "
                    + (gcps != null ? gcps.size() : 0));
        }
        
        if(gcps.size() > georeferenceProperties.getGcp().getMaxCount()) {
            throw new IllegalArgumentException("No more than " + georeferenceProperties.getGcp().getMaxCount()
                    + " ground control points are allowed. Provided: "
                    + gcps.size());
        }
        
        // Validate each GCP
        for (GCP gcp : gcps) {
            if (gcp.pointId() == null || gcp.pointId().trim().isEmpty()) {
                throw new IllegalArgumentException("All ground control points must have valid point IDs");
            }
            if (gcp.imageX() == null || gcp.imageY() == null) {
                throw new IllegalArgumentException("All ground control points must have valid image coordinates");
            }
            if (gcp.longitude() == null || gcp.latitude() == null) {
                throw new IllegalArgumentException("All ground control points must have valid geographic coordinates");
            }
        }
        
        // Use GDAL facade for processing
        if (gdalFacade.isGdalAvailable()) {
            try {
                return processWithGDAL(image, gcps);
            } catch (Exception e) {
                logger.error("GDAL processing failed, falling back to mock response: {}", e.getMessage(), e);
                return createMockResponse(image, gcps);
            }
        } else {
            logger.info("GDAL not available, returning mock response");
            return createMockResponse(image, gcps);
        }
    }
    
    /**
     * Process using GDAL facade
     */
    private Map<String, Object> processWithGDAL(MultipartFile image, List<GCP> gcps) throws Exception {
        logger.info("Processing image {} with GDAL facade using {} GCPs: {}", image.getOriginalFilename(), gcps.size(), gcps);
        Path tempDir = Files.createTempDirectory("georeferencing_");
        
        try {
            // Save uploaded image to temporary file
            String originalFilename = image.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".tif";
            
            Path inputImagePath = tempDir.resolve("input_image" + fileExtension);
            Files.write(inputImagePath, image.getBytes());
            
            // Step 1: Create VRT with GCPs using GDAL facade
            Path vrtPath = tempDir.resolve("temp_georef.vrt");
            gdalFacade.createVrtWithGcps(inputImagePath, vrtPath, gcps, "EPSG:4326");
            
            // Step 2: Transform VRT to Web Mercator (EPSG:3857) for web display using GDAL facade
            Path georeferencedTiffPath = tempDir.resolve("georef.tif");
            gdalFacade.warpImage(vrtPath, georeferencedTiffPath, "EPSG:3857", "bilinear", "GTiff");
            
            // Step 3: Convert GeoTIFF to PNG for browser display using GDAL facade
            Path finalPngPath = tempDir.resolve("georef_final.png");
            gdalFacade.translateImage(georeferencedTiffPath, finalPngPath, "PNG", "-scale");
            
            // Read the processed image bytes
            byte[] processedImageBytes = Files.readAllBytes(finalPngPath);
            
            // Get image info using GDAL facade on the GeoTIFF (which has the spatial reference)
            Map<String, Object> imageInfo = gdalFacade.getComprehensiveImageInfo(georeferencedTiffPath);
            
            // Create result
            Map<String, Object> result = new HashMap<>();
            result.put("originalImageName", originalFilename);
            result.put("imageSize", image.getSize());
            result.put("processedImageSize", processedImageBytes.length);
            result.put("gcpCount", gcps.size());
            result.put("processedImageBytes", processedImageBytes); // Return raw bytes instead of base64
            result.put("extent", extractActualExtentFromGdalInfo(imageInfo, gcps));
            result.put("extentProjection", "EPSG:4326"); // Indicate the projection of extent coordinates
            result.put("imageInfo", imageInfo);
            result.put("coordinateSystem", "EPSG:3857"); // Web Mercator projection
            result.put("processingTimestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            result.put("status", "processed_gdal_facade");
            result.put("message", "Image successfully georeferenced using GDAL facade with " + gcps.size() + " ground control points");
            return result;
            
        } catch (GdalException e) {
            logger.error("GDAL operation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Georeferencing failed: " + e.getMessage(), e);
        } finally {
            // Clean up temporary directory
            deleteDirectoryRecursively(tempDir);
        }
    }
    
    
    /**
     * Fallback method when GDAL is not available - returns mock response
     */
    private Map<String, Object> createMockResponse(MultipartFile image, List<GCP> gcps) {
        Map<String, Object> result = new HashMap<>();
        result.put("originalImageName", image.getOriginalFilename());
        result.put("imageSize", image.getSize());
        result.put("gcpCount", gcps.size());
        result.put("processedImageBytes", new byte[0]); // Empty bytes array - no actual processing
        result.put("extent", createExtentMetadata(gcps));
        result.put("status", "mock_processed");
        result.put("message", "Mock response - GDAL not available. Image processing simulated using " + gcps.size() + " ground control points");
        
        return result;
    }
    
    /**
     * Extract actual extent from GDAL info JSON output
     * Returns extent in WGS84 coordinates (EPSG:4326) by properly parsing JSON
     */
    private Map<String, Double> extractActualExtentFromGdalInfo(Map<String, Object> imageInfo, List<GCP> gcps) {
        Map<String, Double> extent = new HashMap<>();
        
        try {
            String gdalJson = (String) imageInfo.get("gdalinfo_json");
            if (gdalJson != null) {
                
                // Clean the GDAL JSON to handle problematic characters and sections
                String cleanedJson = cleanGdalJson(gdalJson);
                logger.debug(cleanedJson);
                try {
                    ObjectMapper mapper = createTolerantObjectMapper();
                    JsonNode infoNode = mapper.readTree(cleanedJson);
                    logger.debug("Successfully parsed GDAL JSON");
                    
                    // First try to get wgs84Extent coordinates (preferred - already in WGS84)
                    JsonNode extentNode = infoNode.path("wgs84Extent").path("coordinates");
                    if (extentNode.isMissingNode() || !extentNode.isArray() || extentNode.size() == 0) {
                        // Fallback to cornerCoordinates (in Web Mercator, needs conversion)
                        extentNode = infoNode.path("cornerCoordinates");
                    }

                
                double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
                double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
                boolean foundCoords = false;
                
                if (extentNode.isArray() && extentNode.size() > 0 && extentNode.get(0).isArray()) {
                    // GeoJSON Polygon coordinates (from wgs84Extent)
                    logger.debug("Processing wgs84Extent GeoJSON coordinates");
                    for (JsonNode coord : extentNode.get(0)) {
                        double x = coord.get(0).asDouble();
                        double y = coord.get(1).asDouble();
                        minX = Math.min(minX, x);
                        minY = Math.min(minY, y);
                        maxX = Math.max(maxX, x);
                        maxY = Math.max(maxY, y);
                        foundCoords = true;
                    }
                    
                    // These are already WGS84 coordinates from wgs84Extent
                    extent.put("minLongitude", minX);
                    extent.put("maxLongitude", maxX);
                    extent.put("minLatitude", minY);
                    extent.put("maxLatitude", maxY);
                    logger.info("Extracted WGS84 extent from wgs84Extent: {}", extent);
                    return extent;
                    
                } else if (extentNode.has("upperLeft") && extentNode.has("lowerRight")) {
                    // cornerCoordinates object (Web Mercator coordinates)
                    logger.debug("Processing cornerCoordinates object");
                    double ulx = extentNode.path("upperLeft").get(0).asDouble();
                    double uly = extentNode.path("upperLeft").get(1).asDouble();
                    double lrx = extentNode.path("lowerRight").get(0).asDouble();
                    double lry = extentNode.path("lowerRight").get(1).asDouble();
                    
                    minX = Math.min(ulx, lrx);
                    minY = Math.min(uly, lry);
                    maxX = Math.max(ulx, lrx);
                    maxY = Math.max(uly, lry);
                    foundCoords = true;
                    
                    logger.debug("Raw cornerCoordinates bounds: minX={}, maxX={}, minY={}, maxY={}", minX, maxX, minY, maxY);
                    
                    // Convert Web Mercator to WGS84
                    double minLon = webMercatorXToLon(minX);
                    double maxLon = webMercatorXToLon(maxX);
                    double minLat = webMercatorYToLat(minY);
                    double maxLat = webMercatorYToLat(maxY);
                    
                    extent.put("minLongitude", minLon);
                    extent.put("maxLongitude", maxLon);
                    extent.put("minLatitude", minLat);
                    extent.put("maxLatitude", maxLat);
                    logger.info("Extracted Web Mercator extent and converted to WGS84: {}", extent);
                    return extent;
                }
                
                if (!foundCoords) {
                    logger.warn("No valid extent coordinates found in GDAL JSON");
                }
                } catch (JsonProcessingException e) {
                    logger.warn("JSON parsing failed even after cleaning: {}", e.getMessage());
                    // Continue to fallback strategy
                }
            } else {
                logger.warn("No GDAL JSON output available");
            }
        } catch (Exception e) {
            logger.error("Failed to extract extent from GDAL info: {}", e.getMessage(), e);
        }
        
        // Fallback to GCP-based extent in WGS84
        logger.info("Using GCP-based extent in WGS84 as fallback");
        return createExtentMetadata(gcps);
    }

    /**
     * Create a Jackson ObjectMapper configured to be more tolerant of formatting issues
     */
    private ObjectMapper createTolerantObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Ignore unknown properties
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
    
    /**
     * Clean and fix problematic JSON content from GDAL output.
     * This fixes issues with malformed JSON that breaks Jackson parsing.
     */
    private String cleanGdalJson(String gdalJson) {
        try {
            logger.debug("Cleaning GDAL JSON. Original length: {}", gdalJson.length());
            
            String cleaned = gdalJson;
            int originalLength = cleaned.length();
            
            // Step 1: Remove the problematic coordinateSystem section using direct string removal
            if (cleaned.contains("\"coordinateSystem\":")) {
                cleaned = removeCoordinateSystemDirectly(cleaned);
            }
            
            if (cleaned.length() != originalLength) {
                logger.debug("Removed coordinateSystem section. Size reduced from {} to {} chars", 
                    originalLength, cleaned.length());
            }
            
            // Step 2: The main issue is that GDAL might be returning escaped newlines as literal \n
            // Let's convert escaped newlines to actual newlines so Jackson can parse properly
            if (cleaned.contains("\\n")) {
                logger.debug("Found escaped newlines, converting to actual newlines");
                cleaned = cleaned.replace("\\n", "\n");
            }
            if (cleaned.contains("\\r")) {
                logger.debug("Found escaped carriage returns, converting to actual CR");
                cleaned = cleaned.replace("\\r", "\r");
            }
            if (cleaned.contains("\\t")) {
                logger.debug("Found escaped tabs, converting to actual tabs");
                cleaned = cleaned.replace("\\t", "\t");
            }
            
            logger.debug("JSON cleaning completed successfully. Final size: {} chars", cleaned.length());
            return cleaned;
            
        } catch (Exception e) {
            logger.warn("Failed to clean GDAL JSON: {}", e.getMessage());
        }
        
        logger.warn("Could not clean GDAL JSON, returning original");
        return gdalJson; // Return original if cleaning fails
    }
    
    /**
     * Direct removal of coordinateSystem section using simple string operations
     */
    private String removeCoordinateSystemDirectly(String json) {
        try {
            logger.debug("Starting direct coordinateSystem removal");
            
            // Find the coordinateSystem field
            int coordStart = json.indexOf("\"coordinateSystem\":");
            if (coordStart == -1) {
                logger.debug("coordinateSystem field not found");
                return json;
            }
            
            logger.debug("Found coordinateSystem at position: {}", coordStart);
            
            // Find the start position (include leading comma if present)
            int startPos = coordStart;
            while (startPos > 0 && Character.isWhitespace(json.charAt(startPos - 1))) {
                startPos--;
            }
            if (startPos > 0 && json.charAt(startPos - 1) == ',') {
                startPos--; // Include the comma
            }
            
            logger.debug("Field start position: {}", startPos);
            
            // Find the end - look for the next field we know exists
            int nextFieldPos = -1;
            String[] possibleFields = {
                "\"geoTransform\":",
                "\"metadata\":",
                "\"cornerCoordinates\":",
                "\"wgs84Extent\":",
                "\"bands\":"
            };
            
            for (String field : possibleFields) {
                int fieldPos = json.indexOf(field, coordStart);
                if (fieldPos != -1) {
                    if (nextFieldPos == -1 || fieldPos < nextFieldPos) {
                        nextFieldPos = fieldPos;
                        logger.debug("Found direct next field '{}' at position: {}", field, fieldPos);
                    }
                }
            }
            
            if (nextFieldPos != -1) {
                // Look backwards from next field to include the comma
                while (nextFieldPos > 0 && Character.isWhitespace(json.charAt(nextFieldPos - 1))) {
                    nextFieldPos--;
                }
                if (nextFieldPos > 0 && json.charAt(nextFieldPos - 1) == ',') {
                    nextFieldPos--; // Include the comma
                }
                
                logger.debug("Next field {} start position: {}", nextFieldPos);
                
                // Cut out the coordinateSystem section
                String before = json.substring(0, startPos);
                String after = json.substring(nextFieldPos);
                String result = before + after;
                
                // Clean up any double commas
                result = result.replaceAll(",\\s*,", ",");
                result = result.replaceAll("\\{\\s*,", "{");
                result = result.replaceAll(",\\s*}", "}");
                
                logger.debug("Direct CoordinateSystem removal successful. Size: {} -> {} chars", json.length(), result.length());
                logger.debug("Cleaned JSON first 500 chars: {}", result.substring(0, Math.min(500, result.length())));
                
                return result;
            } else {
                logger.warn("Could not find any next field after coordinateSystem. Available fields after position {}:", coordStart);
                // Log what fields we can find after coordinateSystem for debugging
                for (String field : possibleFields) {
                    int pos = json.indexOf(field, coordStart);
                    if (pos != -1) {
                        logger.warn("  {} at position {}", field, pos);
                    }
                }
                return json;
            }
            
        } catch (Exception e) {
            logger.warn("Direct coordinateSystem removal failed: {}", e.getMessage());
            return json;
        }
    }
    
    /**
     * Convert Web Mercator X coordinate to longitude
     */
    private double webMercatorXToLon(double x) {
        return x / WEB_MERCATOR_MAX_EXTENT * 180.0;
    }

    /**
     * Convert Web Mercator Y coordinate to latitude
     */
    private double webMercatorYToLat(double y) {
        return Math.toDegrees(Math.atan(Math.sinh(y / WEB_MERCATOR_MAX_EXTENT * Math.PI)));
    }



    /**
     * Creates extent metadata from ground control points
     */
    private Map<String, Double> createExtentMetadata(List<GCP> gcps) {
        double minLon = gcps.stream().mapToDouble(GCP::longitude).min().orElse(0.0);
        double maxLon = gcps.stream().mapToDouble(GCP::longitude).max().orElse(0.0);
        double minLat = gcps.stream().mapToDouble(GCP::latitude).min().orElse(0.0);
        double maxLat = gcps.stream().mapToDouble(GCP::latitude).max().orElse(0.0);
        
        Map<String, Double> extent = new HashMap<>();
        extent.put("minLongitude", minLon);
        extent.put("maxLongitude", maxLon);
        extent.put("minLatitude", minLat);
        extent.put("maxLatitude", maxLat);
        
        return extent;
    }
    
    /**
     * Validate if the image content type is supported
     */
    private boolean validateImageFormat(String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            return false;
        }
        Set<String> supportedFormats = georeferenceProperties.getImage().getSupportedFormatsAsSet();
        if (supportedFormats == null || supportedFormats.isEmpty()) {
            return true; // If no formats are configured, accept all
        }
        return supportedFormats.contains(contentType.toLowerCase());
    }
    
    /**
     * Parse size string (e.g., "10MB", "5GB") to bytes
     */
    private long parseSizeToBytes(String sizeStr) {
        if (sizeStr == null || sizeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Size string cannot be null or empty");
        }
        
        String upperSize = sizeStr.trim().toUpperCase();
        long multiplier = 1;
        String numericPart = upperSize;
        
        if (upperSize.endsWith("KB")) {
            multiplier = 1024L;
            numericPart = upperSize.substring(0, upperSize.length() - 2);
        } else if (upperSize.endsWith("MB")) {
            multiplier = 1024L * 1024L;
            numericPart = upperSize.substring(0, upperSize.length() - 2);
        } else if (upperSize.endsWith("GB")) {
            multiplier = 1024L * 1024L * 1024L;
            numericPart = upperSize.substring(0, upperSize.length() - 2);
        } else if (upperSize.endsWith("B")) {
            numericPart = upperSize.substring(0, upperSize.length() - 1);
        }
        
        try {
            return Long.parseLong(numericPart.trim()) * multiplier;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid size format: " + sizeStr, e);
        }
    }
    
    /**
     * Recursively delete a directory and all its contents
     */
    private void deleteDirectoryRecursively(Path directory) {
        try {
            if (Files.exists(directory)) {
                Files.walk(directory)
                    .sorted((path1, path2) -> path2.compareTo(path1)) // Delete files before directories
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (Exception e) {
                            // Log but don't fail - cleanup is best effort
                            logger.warn("Failed to delete: {} - {}", path, e.getMessage());
                        }
                    });
            }
        } catch (Exception e) {
            logger.warn("Failed to clean up directory: {} - {}", directory, e.getMessage());
        }
    }
}