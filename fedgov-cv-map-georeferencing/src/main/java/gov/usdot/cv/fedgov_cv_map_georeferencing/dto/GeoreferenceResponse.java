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
package gov.usdot.cv.fedgov_cv_map_georeferencing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeoreferenceResponse {
    private boolean success;
    private String message;
    private GeoreferenceDetails details;

    @Data
    public static class GeoreferenceDetails {
        // Define fields for georeference details as needed
        private String originalImageName;
        private long imageSize;
        private long processedImageSize;
        private String processedImageUrl;
        private int gcpCount;
        private Extent extent;
        private String extentProjection;
        private String coordinateSystem;
        private String processingTimestamp;
        private String status;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Extent {
            private double minLongitude;
            private double maxLongitude;
            private double minLatitude;
            private double maxLatitude;
        }
    }
}
