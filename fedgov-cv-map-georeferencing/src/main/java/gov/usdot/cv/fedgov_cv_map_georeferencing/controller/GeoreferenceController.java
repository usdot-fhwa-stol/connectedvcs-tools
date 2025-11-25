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
package gov.usdot.cv.fedgov_cv_map_georeferencing.controller;

import gov.usdot.cv.fedgov_cv_map_georeferencing.dto.GCP;
import gov.usdot.cv.fedgov_cv_map_georeferencing.dto.GeoreferenceResponse;
import gov.usdot.cv.fedgov_cv_map_georeferencing.service.GeoreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/georeference")
@Tag(name = "Georeferencing", description = "API for georeferencing images using Ground Control Points")
public class GeoreferenceController {
    private static final Logger logger = LoggerFactory.getLogger(GeoreferenceController.class);
    
    private final GeoreferenceService georeferenceService;
    private final ObjectMapper objectMapper;
    
    // Store processed images temporarily (in production, use a proper cache or file storage)
    private final Map<String, byte[]> imageCache = new ConcurrentHashMap<>();

    public GeoreferenceController(GeoreferenceService georeferenceService) {
        this.georeferenceService = georeferenceService;
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Georeference an image",
        description = "Upload an image file and provide at least 6 Ground Control Points (GCPs) to georeference the image with high precision. Returns the processed image and extent metadata."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully processed the image",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = GeoreferenceResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - invalid input data, validation errors, or unsupported image format (only PNG and JPEG are supported)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error during processing",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    public ResponseEntity<?> georeference(
        @Parameter(
            description = "Image file to be georeferenced (PNG or JPEG format only)",
            required = true,
            content = @Content(mediaType = "image/*")
        )
        @RequestPart("image") MultipartFile image,
        
        @Parameter(
            description = "JSON string containing at least 6 Ground Control Points with pixel and geographic coordinates for high precision georeferencing",
            required = true
        )
        @RequestPart(value = "gcps", required = true) String gcpsJson
    ) {
        try {
            logger.info("Received georeference request with image: {}", image.getOriginalFilename());            
            logger.debug("GCPs JSON: {}", gcpsJson);
            
            // Parse JSON string to List<GCP>
            List<GCP> gcps;
            try {
                gcps = objectMapper.readValue(gcpsJson, new TypeReference<List<GCP>>() {});
            } catch (Exception e) {
                logger.error("Failed to parse GCPs JSON: {}", e.getMessage());
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Invalid GCP format: " + e.getMessage());
                errorResponse.put("error", "JSON_PARSE_ERROR");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            logger.debug("Parsed GCPs: {}", gcps);
            
            // Process the request
            Map<String, Object> result = georeferenceService.process(image, gcps);
            
            // Return successful response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Processing completed successfully");           
           
            // Store the binary image data and provide URL instead of base64
            Object imageBytes = result.get("processedImageBytes");
            if (imageBytes instanceof byte[] && ((byte[]) imageBytes).length > 0) {
                String imageId = java.util.UUID.randomUUID().toString();
                imageCache.put(imageId, (byte[]) imageBytes);
                
                // Replace bytes with URL
                result.put("processedImageUrl", "/api/georeference/image/" + imageId);
                result.remove("processedImageBytes");
                logger.info("Stored image with ID: {}, size: {} bytes", imageId, ((byte[]) imageBytes).length);
            }
            response.putAll(result);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("error", "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            logger.error("Internal server error: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Internal server error: " + e.getMessage());
            errorResponse.put("error", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/image/{imageId}")
    @Operation(
        summary = "Get processed image",
        description = "Retrieve a processed georeferenced image by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved the image",
            content = @Content(mediaType = MediaType.IMAGE_PNG_VALUE)
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Image not found"
        )
    })
    public ResponseEntity<Resource> getProcessedImage(@PathVariable String imageId) {
        logger.info("Fetching processed image with ID: {}", imageId);
        byte[] imageData = imageCache.get(imageId);
        if (imageData == null) {
            return ResponseEntity.notFound().build();
        }
        
        ByteArrayResource resource = new ByteArrayResource(imageData);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
            .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(imageData.length))
            .header(HttpHeaders.CACHE_CONTROL, "max-age=3600") // Cache for 1 hour
            .body(resource);
    }

}