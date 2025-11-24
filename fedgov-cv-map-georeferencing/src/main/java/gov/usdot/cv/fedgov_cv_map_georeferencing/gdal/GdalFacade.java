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
package gov.usdot.cv.fedgov_cv_map_georeferencing.gdal;

import gov.usdot.cv.fedgov_cv_map_georeferencing.dto.GCP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GDAL Facade - Provides a clean interface for all GDAL command-line utility operations.
 * 
 * This facade encapsulates all interactions with GDAL utilities including:
 * - gdal_translate: For VRT creation and format conversion
 * - gdalwarp: For coordinate transformation and reprojection
 * - gdalinfo: For extracting metadata and extent information
 */
@Component
public class GdalFacade {
    private static final Logger logger = LoggerFactory.getLogger(GdalFacade.class);
    
    private static final String GDAL_TRANSLATE = "gdal_translate";
    private static final String GDAL_WARP = "gdalwarp";
    private static final String GDAL_INFO = "gdalinfo";
    
    private final boolean gdalAvailable;
    
    public GdalFacade() {
        this.gdalAvailable = checkGdalAvailability();
    }
    
    /**
     * Check if GDAL utilities are available on the system
     */
    private boolean checkGdalAvailability() {
        try {
            ProcessBuilder pb = new ProcessBuilder(GDAL_INFO, "--version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("GDAL command-line utilities detected and available");
                return true;
            } else {
                logger.warn("GDAL utilities not responding correctly");
                return false;
            }
        } catch (Exception e) {
            logger.warn("GDAL command-line utilities not available: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if GDAL is available
     */
    public boolean isGdalAvailable() {
        return gdalAvailable;
    }
    
    /**
     * Create a VRT (Virtual Raster files) file with Ground Control Points using gdal_translate
     * 
     * Supports all GDAL-readable raster formats including:
     * - Common formats: PNG and JPEG
     * - Specialized formats: BMP, GIF, NetCDF, HDF, etc.
     * 
     * @param inputImagePath Path to input image (any GDAL-supported raster format)
     * @param outputVrtPath Path where VRT file will be created
     * @param gcps List of Ground Control Points
     * @param sourceSrs Source spatial reference system (e.g., "EPSG:4326")
     * @throws GdalException if the operation fails
     */
    public void createVrtWithGcps(Path inputImagePath, Path outputVrtPath, List<GCP> gcps, String sourceSrs) throws GdalException {
        logger.info("Creating VRT with {} GCPs: {} -> {}", gcps.size(), inputImagePath.getFileName(), outputVrtPath.getFileName());
        
        List<String> command = new ArrayList<>();
        command.add(GDAL_TRANSLATE);
        
        // Add all GCPs
        for (GCP gcp : gcps) {
            command.add("-gcp");
            command.add(String.valueOf(gcp.imageX()));
            command.add(String.valueOf(gcp.imageY()));
            command.add(String.valueOf(gcp.longitude()));
            command.add(String.valueOf(gcp.latitude()));
        }
        
        // Set source spatial reference system
        command.add("-a_srs");
        command.add(sourceSrs);
        
        // Input and output paths
        command.add(inputImagePath.toString());
        command.add(outputVrtPath.toString());
        
        executeGdalCommand(command, "VRT creation with GCPs");
    }
    
    /**
     * Transform image to target coordinate system using gdalwarp
     * 
     * @param inputPath Path to input file (VRT or image)
     * @param outputPath Path for output file
     * @param targetSrs Target spatial reference system (e.g., "EPSG:3857")
     * @param resamplingMethod Resampling method ("bilinear", "cubic", "nearest")
     * @param outputFormat Output format ("PNG")
     * @throws GdalException if the operation fails
     */
    public void warpImage(Path inputPath, Path outputPath, String targetSrs, 
                         String resamplingMethod, String outputFormat) throws GdalException {
        logger.info("Warping image {} -> {} (SRS: {}, Format: {})", 
                   inputPath.getFileName(), outputPath.getFileName(), targetSrs, outputFormat);
        
        List<String> command = new ArrayList<>();
        command.add(GDAL_WARP);
        command.add("-t_srs");
        command.add(targetSrs);
        
        if (resamplingMethod != null) {
            command.add("-r");
            command.add(resamplingMethod);
        }
        
        if (outputFormat != null) {
            command.add("-of");
            command.add(outputFormat);
        }
        
        command.add(inputPath.toString());
        command.add(outputPath.toString());
        
        executeGdalCommand(command, "Image warping to " + targetSrs);
    }
    
    /**
     * Convert image format using gdal_translate
     * 
     * @param inputPath Path to input file
     * @param outputPath Path for output file
     * @param outputFormat Target format ("PNG", "JPEG", "GTiff", etc.)
     * @param options Additional options (e.g., "-scale" for auto-scaling)
     * @throws GdalException if the operation fails
     */
    public void translateImage(Path inputPath, Path outputPath, String outputFormat, String... options) throws GdalException {
        logger.info("Translating image {} -> {} (Format: {})", 
                   inputPath.getFileName(), outputPath.getFileName(), outputFormat);
        
        List<String> command = new ArrayList<>();
        command.add(GDAL_TRANSLATE);
        command.add("-of");
        command.add(outputFormat);
        
        // Add additional options
        for (String option : options) {
            command.add(option);
        }
        
        command.add(inputPath.toString());
        command.add(outputPath.toString());
        
        executeGdalCommand(command, "Image format translation to " + outputFormat);
    }
    
    /**
     * Get detailed information about an image using gdalinfo
     * 
     * @param imagePath Path to image file
     * @param jsonOutput Whether to request JSON format output
     * @param includeChecksum Whether to include checksum information
     * @return Map containing image information
     * @throws GdalException if the operation fails
     */
    public Map<String, Object> getImageInfo(Path imagePath, boolean jsonOutput, boolean includeChecksum) throws GdalException {
        logger.debug("Getting image info for: {}", imagePath.getFileName());
        
        List<String> command = new ArrayList<>();
        command.add(GDAL_INFO);
        
        if (jsonOutput) {
            command.add("-json");
        }
        
        if (includeChecksum) {
            command.add("-checksum");
        }
        
        command.add(imagePath.toString());
        
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();
            
            // Capture output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\\n");
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                String error = readProcessError(process);
                if (jsonOutput) {
                    // Fallback: try without JSON format
                    logger.warn("JSON gdalinfo failed, trying text format for: {}", imagePath.getFileName());
                    return getImageInfo(imagePath, false, includeChecksum);
                } else {
                    throw new GdalException("gdalinfo failed with exit code " + exitCode + ": " + error);
                }
            }
            
            Map<String, Object> info = new HashMap<>();
            String outputStr = output.toString();
            
            if (jsonOutput) {
                info.put("gdalinfo_json", outputStr.trim());
            } else {
                info.put("gdalinfo_text", outputStr.trim());
            }
            
            logger.debug("Successfully retrieved image info for: {}", imagePath.getFileName());
            return info;
            
        } catch (Exception e) {
            throw new GdalException("Failed to get image info: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get comprehensive image information (both JSON and text formats)
     */
    public Map<String, Object> getComprehensiveImageInfo(Path imagePath) throws GdalException {
        Map<String, Object> info = new HashMap<>();
        
        try {
            // Try to get JSON format first
            Map<String, Object> jsonInfo = getImageInfo(imagePath, true, false);
            info.putAll(jsonInfo);
        } catch (GdalException e) {
            logger.debug("JSON format failed, will try text format");
        }
        
        try {
            // Get text format with checksum
            Map<String, Object> textInfo = getImageInfo(imagePath, false, true);
            info.putAll(textInfo);
        } catch (GdalException e) {
            logger.warn("Failed to get text format info for: {}", imagePath.getFileName());
        }
        
        return info;
    }
    
    /**
     * Execute a GDAL command with proper error handling and logging
     */
    private void executeGdalCommand(List<String> command, String operationDescription) throws GdalException {
        if (!gdalAvailable) {
            throw new GdalException("GDAL utilities not available");
        }
        
        logger.debug("Executing GDAL command: {}", String.join(" ", command));
        
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();
            
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                String error = readProcessError(process);
                String commandStr = String.join(" ", command);
                throw new GdalException(operationDescription + " failed with exit code " + exitCode + 
                                      ". Command: " + commandStr + ". Error: " + error);
            }
            
            logger.debug("Successfully completed: {}", operationDescription);
            
        } catch (Exception e) {
            if (e instanceof GdalException) {
                throw new GdalException("Failed to execute " + operationDescription + ": " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Read error output from a process
     */
    private String readProcessError(Process process) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            StringBuilder error = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                error.append(line).append("\\n");
            }
            return error.toString();
        } catch (IOException e) {
            return "Could not read error output: " + e.getMessage();
        }
    }
    
    /**
     * Get GDAL version information
     */
    public String getGdalVersion() throws GdalException {
        if (!gdalAvailable) {
            throw new GdalException("GDAL not available");
        }
        
        try {
            ProcessBuilder pb = new ProcessBuilder(GDAL_INFO, "--version");
            Process process = pb.start();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String version = reader.readLine();
                process.waitFor();
                return version != null ? version : "Unknown version";
            }
        } catch (Exception e) {
            throw new GdalException("Failed to get GDAL version: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validate that all required GDAL utilities are available
     */
    public void validateGdalUtilities() throws GdalException {
        if (!gdalAvailable) {
            throw new GdalException("GDAL utilities not available");
        }
        
        String[] utilities = {GDAL_TRANSLATE, GDAL_WARP, GDAL_INFO};
        
        for (String utility : utilities) {
            try {
                ProcessBuilder pb = new ProcessBuilder(utility, "--help");
                Process process = pb.start();
                int exitCode = process.waitFor();
                
                if (exitCode != 0 && exitCode != 1) { // exit code 1 is normal for --help
                    throw new GdalException("GDAL utility not working: " + utility);
                }
                
            } catch (Exception e) {
                throw new GdalException("GDAL utility not available: " + utility + " - " + e.getMessage(), e);
            }
        }
        
        logger.info("All GDAL utilities validated successfully");
    }
}