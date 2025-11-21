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
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

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
    public Object process(MultipartFile image, List<GCP> gcps) throws Exception {
        // Validate input parameters
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image file is required and cannot be empty");
        }
        
        if (gcps == null || gcps.size() != 4) {
            throw new IllegalArgumentException("Exactly 4 ground control points are required");
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
    private Object processWithGDALCommandLine(MultipartFile image, List<GCP> gcps) throws Exception {
        Path tempDir = Files.createTempDirectory("georeferencing_");
        
        try {
            // Save uploaded image to temporary file
            String originalFilename = image.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".tif";
            
            Path inputImagePath = tempDir.resolve("input_image" + fileExtension);
            Files.write(inputImagePath, image.getBytes());
            
            Path tempImagePath = tempDir.resolve("temp_georef" + fileExtension);
            Path outputImagePath = tempDir.resolve("output_georef.tif");
            
            // Step 1: Use gdal_translate to add GCPs to the image
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
            
            translateBuilder.command().add(inputImagePath.toString());
            translateBuilder.command().add(tempImagePath.toString());
            
            Process translateProcess = translateBuilder.start();
            int translateExitCode = translateProcess.waitFor();
            
            if (translateExitCode != 0) {
                String error = readProcessError(translateProcess);
                throw new RuntimeException("gdal_translate failed with exit code " + translateExitCode + ": " + error);
            }
            
            // Step 2: Use gdalwarp to actually georeference the image
            ProcessBuilder warpBuilder = new ProcessBuilder(
                "gdalwarp",
                "-t_srs", "EPSG:4326", // Target spatial reference system (WGS84)
                "-r", "bilinear", // Resampling method
                "-of", "GTiff", // Output format
                tempImagePath.toString(),
                outputImagePath.toString()
            );
            
            Process warpProcess = warpBuilder.start();
            int warpExitCode = warpProcess.waitFor();
            
            if (warpExitCode != 0) {
                String error = readProcessError(warpProcess);
                throw new RuntimeException("gdalwarp failed with exit code " + warpExitCode + ": " + error);
            }
            
            // Read the processed image and encode as base64
            byte[] processedImageBytes = Files.readAllBytes(outputImagePath);
            String processedImageBase64 = Base64.getEncoder().encodeToString(processedImageBytes);
            
            // Get image info using gdalinfo
            Map<String, Object> imageInfo = getImageInfo(outputImagePath);
            
            // Create result
            Map<String, Object> result = new HashMap<>();
            result.put("originalImageName", originalFilename);
            result.put("imageSize", image.getSize());
            result.put("processedImageSize", processedImageBytes.length);
            result.put("gcpCount", gcps.size());
            result.put("processedImageBase64", processedImageBase64);
            result.put("extent", createExtentMetadata(gcps));
            result.put("imageInfo", imageInfo);
            result.put("coordinateSystem", "EPSG:4326");
            result.put("processingTimestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            result.put("status", "processed_gdal_cli");
            result.put("message", "Image successfully georeferenced using GDAL command-line utilities with " + gcps.size() + " ground control points");
            
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
        info.put("format", "GeoTIFF");
        info.put("projection", "EPSG:4326");
        
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
    private Object createMockResponse(MultipartFile image, List<GCP> gcps) {
        Map<String, Object> result = new HashMap<>();
        result.put("originalImageName", image.getOriginalFilename());
        result.put("imageSize", image.getSize());
        result.put("gcpCount", gcps.size());
        result.put("processedImageBase64", ""); // Empty - no actual processing
        result.put("extent", createExtentMetadata(gcps));
        result.put("status", "mock_processed");
        result.put("message", "Mock response - GDAL not available. Image processing simulated using " + gcps.size() + " ground control points");
        
        return result;
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