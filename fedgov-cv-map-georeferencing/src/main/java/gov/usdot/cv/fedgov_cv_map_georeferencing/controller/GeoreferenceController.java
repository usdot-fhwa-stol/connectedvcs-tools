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
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/georeference")
@Tag(name = "Georeferencing", description = "API for georeferencing images using Ground Control Points")
public class GeoreferenceController {
    private final GeoreferenceService georeferenceService;

    public GeoreferenceController(GeoreferenceService georeferenceService) {
        this.georeferenceService = georeferenceService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Georeference an image",
        description = "Upload an image file and provide 4 Ground Control Points (GCPs) to georeference the image. Returns the processed image and extent metadata."
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
            description = "Bad request - invalid input data or validation errors",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error during processing",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    public ResponseEntity<GeoreferenceResponse> georeference(
        @Parameter(
            description = "Image file to be georeferenced (PNG, JPEG, etc.)",
            required = true,
            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
        )
        @RequestPart("image") MultipartFile image,
        
        @Parameter(
            description = "List of exactly 4 Ground Control Points with pixel and geographic coordinates",
            required = true
        )
        @RequestPart(value = "gcps", required = true) @Valid @Size(min = 4, max = 4, message = "Exactly 4 Ground Control Points are required") List<GCP> gcps
    ) throws Exception {
        var result = georeferenceService.process(image, gcps);
        return ResponseEntity.ok(new GeoreferenceResponse(true, "Processed", result));
    }

}