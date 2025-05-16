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

package gov.usdot.cv.rgaencoder;

public class GeometryContainer {
    private int geometryContainerID;
    private ApproachGeometryLayer approachGeometryLayer;
    private MotorVehicleLaneGeometryLayer motorVehicleLaneGeometryLayer;
    private BicycleLaneGeometryLayer bicycleLaneGeometryLayer;
    private CrosswalkLaneGeometryLayer crosswalkLaneGeometryLayer;

    // Constants to represent the choices
    public static final int APPROACH_GEOMETRY_LAYER_ID = 1;
    public static final int MOTOR_VEHICLE_LANE_GEOMETRY_LAYER_ID = 2;
    public static final int BICYCLE_LANE_GEOMETRY_LAYER_ID = 3;
    public static final int CROSSWALK_LANE_GEOMETRY_LAYER_ID = 4;

    public GeometryContainer() {
        this.geometryContainerID = 0;
        this.approachGeometryLayer = null;
        this.motorVehicleLaneGeometryLayer = null;
        this.bicycleLaneGeometryLayer = null;
        this.crosswalkLaneGeometryLayer = null;
    }

    public GeometryContainer(int geometryContainerID,
            ApproachGeometryLayer approachGeometryLayer,
            MotorVehicleLaneGeometryLayer motorVehicleLaneGeometryLayer,
            BicycleLaneGeometryLayer bicycleLaneGeometryLayer,
            CrosswalkLaneGeometryLayer crosswalkLaneGeometryLayer) {
        this.geometryContainerID = geometryContainerID;
        this.approachGeometryLayer = approachGeometryLayer;
        this.motorVehicleLaneGeometryLayer = motorVehicleLaneGeometryLayer;
        this.bicycleLaneGeometryLayer = bicycleLaneGeometryLayer;
        this.crosswalkLaneGeometryLayer = crosswalkLaneGeometryLayer;
    }

    public int getGeometryContainerID() {
        return geometryContainerID;
    }

    public void setGeometryContainerID(int geometryContainerID) {
        this.geometryContainerID = geometryContainerID;
    }

    public ApproachGeometryLayer getApproachGeometryLayer() {
        return approachGeometryLayer;
    }

    public void setApproachGeometryLayer(ApproachGeometryLayer approachGeometryLayer) {
        this.approachGeometryLayer = approachGeometryLayer;
    }

    public MotorVehicleLaneGeometryLayer getMotorVehicleLaneGeometryLayer() {
        return motorVehicleLaneGeometryLayer;
    }

    public void setMotorVehicleLaneGeometryLayer(MotorVehicleLaneGeometryLayer motorVehicleLaneGeometryLayer) {
        this.motorVehicleLaneGeometryLayer = motorVehicleLaneGeometryLayer;
    }

    public BicycleLaneGeometryLayer getBicycleLaneGeometryLayer() {
        return bicycleLaneGeometryLayer;
    }

    public void setBicycleLaneGeometryLayer(BicycleLaneGeometryLayer bicycleLaneGeometryLayer) {
        this.bicycleLaneGeometryLayer = bicycleLaneGeometryLayer;
    }

    public CrosswalkLaneGeometryLayer getCrosswalkLaneGeometryLayer() {
        return crosswalkLaneGeometryLayer;
    }

    public void setCrosswalkLaneGeometryLayer(CrosswalkLaneGeometryLayer crosswalkLaneGeometryLayer) {
        this.crosswalkLaneGeometryLayer = crosswalkLaneGeometryLayer;
    }

    @Override
    public String toString() {
        return "GeometryContainer [geometryContainerID=" + geometryContainerID +
                ", approachGeometryLayer=" + (approachGeometryLayer != null ? approachGeometryLayer.toString() : "null")
                +
                ", motorVehicleLaneGeometryLayer="
                + (motorVehicleLaneGeometryLayer != null ? motorVehicleLaneGeometryLayer.toString() : "null") +
                ", bicycleLaneGeometryLayer="
                + (bicycleLaneGeometryLayer != null ? bicycleLaneGeometryLayer.toString() : "null") +
                ", crosswalkLaneGeometryLayer="
                + (crosswalkLaneGeometryLayer != null ? crosswalkLaneGeometryLayer.toString() : "null") +
                "]";
    }
}
