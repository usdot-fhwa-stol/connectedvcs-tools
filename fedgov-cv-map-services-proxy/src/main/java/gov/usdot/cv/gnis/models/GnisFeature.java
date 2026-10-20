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
package gov.usdot.cv.gnis.models;

public class GnisFeature {
    private long gnisId;
    private String name;
    private String type;
    private String county;
    private String state;
    // Distance from the requested point, or null when the feature contains the point
    private Double distanceMiles;

    public GnisFeature() {
    }

    public GnisFeature(long gnisId, String name, String type, String county, String state, Double distanceMiles) {
        this.gnisId = gnisId;
        this.name = name;
        this.type = type;
        this.county = county;
        this.state = state;
        this.distanceMiles = distanceMiles;
    }

    public long getGnisId() {
        return gnisId;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getCounty() {
        return county;
    }

    public String getState() {
        return state;
    }

    public Double getDistanceMiles() {
        return distanceMiles;
    }

    @Override
    public String toString() {
        return "GnisFeature [gnisId=" + gnisId + ", name=" + name + ", type=" + type + ", county=" + county
            + ", state=" + state + ", distanceMiles=" + distanceMiles + "]";
    }
}
