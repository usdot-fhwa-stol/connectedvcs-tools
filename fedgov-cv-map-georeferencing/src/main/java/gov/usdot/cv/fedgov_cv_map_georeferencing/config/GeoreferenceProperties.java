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
package gov.usdot.cv.fedgov_cv_map_georeferencing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Configuration properties for georeferencing functionality.
 * Maps values from application.yaml under the 'georeference' prefix.
 */
@Data
@Component
@ConfigurationProperties(prefix = "georeference")
public class GeoreferenceProperties {

    private Image image = new Image();

    /**
     * Image-related configuration properties.
     */
    @Data
    public static class Image {
        private List<String> supportedFormats = new ArrayList<>(); // Will be populated from application.yaml
        private String maxSize = "50MB";

        /**
         * Get supported formats as a Set for efficient lookup.
         */
        public Set<String> getSupportedFormatsAsSet() {
            return Set.copyOf(supportedFormats);
        }
    }

    private Gcp gcp = new Gcp();

    /**
     * GCP-related configuration properties.
     */
    @Data
    public static class Gcp {
        private int minCount = 6;
        private int maxCount = 10;
    }
}