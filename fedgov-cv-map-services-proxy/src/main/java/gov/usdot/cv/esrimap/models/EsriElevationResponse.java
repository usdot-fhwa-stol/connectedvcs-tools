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
package gov.usdot.cv.esrimap.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EsriElevationResponse {
    private double x;
    private double y;
    private double z;
    private EsriSpatialReference spatialReference;

    public EsriElevationResponse() {
    }

    public EsriElevationResponse(double x, double y, double z, EsriSpatialReference spatialReference) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.spatialReference = spatialReference;
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public EsriSpatialReference getSpatialReference() {
        return this.spatialReference;
    }

    public void setSpatialReference(EsriSpatialReference spatialReference) {
        this.spatialReference = spatialReference;
    }

    @Override
    public String toString() {
        return "{" +
            " x='" + getX() + "'" +
            ", y='" + getY() + "'" +
            ", z='" + getZ() + "'" +
            ", spatialReference='" + getSpatialReference() + "'" +
            "}";
    }
}
