/*
 * Copyright (C) 2024 LEIDOS.
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
package gov.usdot.cv.googlemap.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleElevationResponse {
    private double elevation;
    private GoogleLocation location;
    private double resolution;

    public GoogleElevationResponse() {
    }


    public GoogleElevationResponse(double elevation, GoogleLocation location, double resolution) {
        this.elevation = elevation;
        this.location = location;
        this.resolution = resolution;
    }
    public double getElevation() {
        return this.elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public GoogleLocation getLocation() {
        return this.location;
    }

    public void setLocation(GoogleLocation location) {
        this.location = location;
    }

    public double getResolution() {
        return this.resolution;
    }

    public void setResolution(double resolution) {
        this.resolution = resolution;
    }

    @Override
    public String toString() {
        return "{" +
            " elevation='" + getElevation() + "'" +
            ", location='" + getLocation() + "'" +
            ", resolution='" + getResolution() + "'" +
            "}";
    }

}
