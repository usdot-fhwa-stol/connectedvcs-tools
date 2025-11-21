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

import jakarta.validation.constraints.NotNull;

public record GCP(
        //A unique identifier for the ground control point
        @NotNull String pointId,
        //The X coordinate (in pixels) of the point in the image
        @NotNull Integer imageX,
        @NotNull Integer imageY,
        //The geographic coordinates of the point
        @NotNull Double longitude,
        @NotNull Double latitude
){
  
}
