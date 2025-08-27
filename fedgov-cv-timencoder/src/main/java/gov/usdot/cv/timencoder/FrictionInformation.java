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
package gov.usdot.cv.timencoder;
public class FrictionInformation {

    private DescriptionOfRoadSurface roadSurfaceDescription;
    private RoadSurfaceCondition dryOrWet;     // OPTIONAL
    private RoadRoughness roadRoughness;       // OPTIONAL

    public FrictionInformation() {
    }

    public FrictionInformation(DescriptionOfRoadSurface roadSurfaceDescription) {
        this.roadSurfaceDescription = roadSurfaceDescription;
    }

    public FrictionInformation(DescriptionOfRoadSurface roadSurfaceDescription,
                               RoadSurfaceCondition dryOrWet,
                               RoadRoughness roadRoughness) {
        this.roadSurfaceDescription = roadSurfaceDescription;
        this.dryOrWet = dryOrWet;
        this.roadRoughness = roadRoughness;
    }

    public DescriptionOfRoadSurface getRoadSurfaceDescription() {
        return roadSurfaceDescription;
    }

    public void setRoadSurfaceDescription(DescriptionOfRoadSurface roadSurfaceDescription) {
        this.roadSurfaceDescription = roadSurfaceDescription;
    }

    public RoadSurfaceCondition getDryOrWet() {
        return dryOrWet;
    }

    public void setDryOrWet(RoadSurfaceCondition dryOrWet) {
        this.dryOrWet = dryOrWet;
    }

    public RoadRoughness getRoadRoughness() {
        return roadRoughness;
    }

    public void setRoadRoughness(RoadRoughness roadRoughness) {
        this.roadRoughness = roadRoughness;
    }

    @Override
    public String toString() {
        return "FrictionInformation{" +
                "roadSurfaceDescription=" + roadSurfaceDescription +
                ", dryOrWet=" + dryOrWet +
                ", roadRoughness=" + roadRoughness +
                '}';
    }
}
