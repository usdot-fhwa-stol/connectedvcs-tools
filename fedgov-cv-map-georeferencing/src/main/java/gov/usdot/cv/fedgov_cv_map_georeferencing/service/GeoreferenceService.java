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

import gov.usdot.cv.fedgov_cv_map_georeferencing.dto.GCP;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class GeoreferenceService {
    
    private static boolean gdalAvailable = false;
    
    static {
        // Check if GDAL utilities are available
        try {
            ProcessBuilder pb = new ProcessBuilder("gdalinfo", "--version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                gdalAvailable = true;
                System.out.println("GDAL command-line utilities detected and available");
            }
        } catch (Exception e) {
            System.out.println("GDAL command-line utilities not available: " + e.getMessage());
            gdalAvailable = false;
        }
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
        
        if (gcps == null || gcps.size() < 6) {
            throw new IllegalArgumentException("At least 6 ground control points are required for high precision georeferencing. Provided: " + (gcps != null ? gcps.size() : 0));
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
        
        // Use GDAL command-line utilities for processing
        if (gdalAvailable) {
            try {
                return processWithGDALCommandLine(image, gcps);
            } catch (Exception e) {
                System.err.println("GDAL processing failed, falling back to mock response: " + e.getMessage());
                return createMockResponse(image, gcps);
            }
        } else {
            System.out.println("GDAL not available, returning mock response");
            return createMockResponse(image, gcps);
        }
    }
    
    /**
     * Process using GDAL command-line utilities (gdal_translate and gdalwarp)
     */
    private Map<String, Object> processWithGDALCommandLine(MultipartFile image, List<GCP> gcps) throws Exception {
        Path tempDir = Files.createTempDirectory("georeferencing_");
        
        try {
            // Save uploaded image to temporary file
            String originalFilename = image.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".tif";
            
            Path inputImagePath = tempDir.resolve("input_image" + fileExtension);
            Files.write(inputImagePath, image.getBytes());
            
            // Step 1: Use gdal_translate to create VRT with GCPs and explicitly set SRS to EPSG:4326
            Path vrtPath = tempDir.resolve("temp_georef.vrt");
            
            ProcessBuilder translateBuilder = new ProcessBuilder();
            translateBuilder.command().add("gdal_translate");
            
            // Add GCPs to the command
            for (GCP gcp : gcps) {
                translateBuilder.command().add("-gcp");
                translateBuilder.command().add(String.valueOf(gcp.imageX()));
                translateBuilder.command().add(String.valueOf(gcp.imageY()));
                translateBuilder.command().add(String.valueOf(gcp.longitude()));
                translateBuilder.command().add(String.valueOf(gcp.latitude()));
            }
            
            // Explicitly set spatial reference system to EPSG:4326
            translateBuilder.command().add("-a_srs");
            translateBuilder.command().add("EPSG:4326");
            translateBuilder.command().add(inputImagePath.toString());
            translateBuilder.command().add(vrtPath.toString());
            
            Process translateProcess = translateBuilder.start();
            int translateExitCode = translateProcess.waitFor();
            
            if (translateExitCode != 0) {
                String error = readProcessError(translateProcess);
                throw new RuntimeException("gdal_translate VRT creation failed with exit code " + translateExitCode + ": " + error);
            }
            
            // Step 2: Use gdalwarp to transform VRT to Web Mercator (EPSG:3857) for web display
            Path georeferencedTiffPath = tempDir.resolve("georef.tif");
            
            ProcessBuilder warpBuilder = new ProcessBuilder(
                "gdalwarp",
                "-t_srs", "EPSG:3857", // Target spatial reference system (Web Mercator for web maps)
                "-r", "bilinear", // Resampling method
                "-of", "GTiff", // Output format (temporary GeoTIFF)
                vrtPath.toString(),
                georeferencedTiffPath.toString()
            );
            
            Process warpProcess = warpBuilder.start();
            int warpExitCode = warpProcess.waitFor();
            
            if (warpExitCode != 0) {
                String error = readProcessError(warpProcess);
                throw new RuntimeException("gdalwarp failed with exit code " + warpExitCode + ": " + error);
            }
            
            // Step 3: Convert GeoTIFF to PNG for browser display
            Path finalPngPath = tempDir.resolve("georef_final.png");
            
            ProcessBuilder pngBuilder = new ProcessBuilder(
                "gdal_translate",
                "-of", "PNG", // Output as PNG
                "-scale", // Auto-scale pixel values
                georeferencedTiffPath.toString(),
                finalPngPath.toString()
            );
            
            Process pngProcess = pngBuilder.start();
            int pngExitCode = pngProcess.waitFor();
            
            if (pngExitCode != 0) {
                String error = readProcessError(pngProcess);
                System.err.println("gdal_translate to PNG failed, using original GeoTIFF: " + error);
                // Fall back to using the GeoTIFF if PNG conversion fails
                finalPngPath = georeferencedTiffPath;
            }
            
            // Read the processed image bytes
            byte[] processedImageBytes = Files.readAllBytes(finalPngPath);
            
            // Get image info using gdalinfo on the GeoTIFF (which has the spatial reference)
            Map<String, Object> imageInfo = getImageInfo(georeferencedTiffPath);
            
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
            result.put("status", "processed_gdal_cli_vrt");
            result.put("message", "Image successfully georeferenced using GDAL VRT and Web Mercator projection with " + gcps.size() + " ground control points");
            return result;
            
        } finally {
            // Clean up temporary directory
            deleteDirectoryRecursively(tempDir);
        }
    }
    
    /**
     * Get image information using gdalinfo
     */
    private Map<String, Object> getImageInfo(Path imagePath) throws Exception {
        ProcessBuilder infoBuilder = new ProcessBuilder(
            "gdalinfo",
            "-json",
            imagePath.toString()
        );
        
        Process infoProcess = infoBuilder.start();
        
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(infoProcess.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        int exitCode = infoProcess.waitFor();
        if (exitCode != 0) {
            // If JSON output fails, return basic info
            Map<String, Object> basicInfo = new HashMap<>();
            basicInfo.put("format", "GeoTIFF");
            basicInfo.put("projection", "EPSG:4326");
            return basicInfo;
        }
        
        // Parse JSON output (simplified - you might want to use a proper JSON parser)
        Map<String, Object> info = new HashMap<>();
        String jsonOutput = output.toString();
        info.put("gdalinfo_json", jsonOutput.trim());
        
        // Also try to get simple extent using gdalinfo text output
        try {
            ProcessBuilder extentBuilder = new ProcessBuilder(
                "gdalinfo", 
                "-checksum",
                imagePath.toString()
            );
            Process extentProcess = extentBuilder.start();
            
            StringBuilder extentOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(extentProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    extentOutput.append(line).append("\n");
                }
            }
            
            extentProcess.waitFor();
            info.put("gdalinfo_text", extentOutput.toString());
            
        } catch (Exception e) {
            System.err.println("Failed to get extent info: " + e.getMessage());
        }
        
        return info;
    }
    
    /**
     * Read error output from a process
     */
    private String readProcessError(Process process) throws IOException {
        StringBuilder error = new StringBuilder();
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = errorReader.readLine()) != null) {
                error.append(line).append("\n");
            }
        }
        return error.toString();
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
                System.out.println("GDAL JSON output (first 500 chars): " + gdalJson.substring(0, Math.min(500, gdalJson.length())));
                
                ObjectMapper mapper = new ObjectMapper();
                JsonNode infoNode = mapper.readTree(gdalJson);
                System.out.println(infoNode.toPrettyString());
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
                    System.out.println("Processing wgs84Extent GeoJSON coordinates");
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
                    System.out.println("Extracted WGS84 extent from wgs84Extent: " + extent);
                    return extent;
                    
                } else if (extentNode.has("upperLeft") && extentNode.has("lowerRight")) {
                    // cornerCoordinates object (Web Mercator coordinates)
                    System.out.println("Processing cornerCoordinates object");
                    double ulx = extentNode.path("upperLeft").get(0).asDouble();
                    double uly = extentNode.path("upperLeft").get(1).asDouble();
                    double lrx = extentNode.path("lowerRight").get(0).asDouble();
                    double lry = extentNode.path("lowerRight").get(1).asDouble();
                    
                    minX = Math.min(ulx, lrx);
                    minY = Math.min(uly, lry);
                    maxX = Math.max(ulx, lrx);
                    maxY = Math.max(uly, lry);
                    foundCoords = true;
                    
                    System.out.println("Raw cornerCoordinates bounds: minX=" + minX + ", maxX=" + maxX + ", minY=" + minY + ", maxY=" + maxY);
                    
                    // Convert Web Mercator to WGS84
                    double minLon = webMercatorXToLongitude(minX);
                    double maxLon = webMercatorXToLongitude(maxX);
                    double minLat = webMercatorYToLatitude(minY);
                    double maxLat = webMercatorYToLatitude(maxY);
                    
                    extent.put("minLongitude", minLon);
                    extent.put("maxLongitude", maxLon);
                    extent.put("minLatitude", minLat);
                    extent.put("maxLatitude", maxLat);
                    System.out.println("Extracted Web Mercator extent and converted to WGS84: " + extent);
                    return extent;
                }
                
                if (!foundCoords) {
                    System.out.println("No valid extent coordinates found in GDAL JSON");
                }
            } else {
                System.out.println("No GDAL JSON output available");
            }
        } catch (Exception e) {
            System.err.println("Failed to extract extent from GDAL info: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Fallback to GCP-based extent in WGS84
        System.out.println("Using GCP-based extent in WGS84 as fallback");
        return createExtentMetadata(gcps);
    }

    /**
     * Convert Web Mercator X coordinate to longitude
     */
    private double webMercatorXToLongitude(double x) {
        return x / 20037508.34 * 180.0;
    }

    /**
     * Convert Web Mercator Y coordinate to latitude
     */
    private double webMercatorYToLatitude(double y) {
        return Math.toDegrees(Math.atan(Math.sinh(y / 20037508.34 * Math.PI)));
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
                            System.err.println("Failed to delete: " + path + " - " + e.getMessage());
                        }
                    });
            }
        } catch (Exception e) {
            System.err.println("Failed to clean up directory: " + directory + " - " + e.getMessage());
        }
    }
}