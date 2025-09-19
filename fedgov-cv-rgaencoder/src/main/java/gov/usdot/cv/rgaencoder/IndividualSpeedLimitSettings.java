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

public class IndividualSpeedLimitSettings {
    private long speedLimit;
    private SpeedLimitTypeRGA speedLimitType;
    private SpeedLimitVehicleType vehicleTypes;
    private RGATimeRestrictions timeRestrictions;

    public IndividualSpeedLimitSettings() {
    }

    public IndividualSpeedLimitSettings(long speedLimit, SpeedLimitTypeRGA speedLimitType,
            SpeedLimitVehicleType vehicleTypes, RGATimeRestrictions timeRestrictions) {
        this.speedLimit = speedLimit;
        this.speedLimitType = speedLimitType;
        this.vehicleTypes = vehicleTypes;
        this.timeRestrictions = timeRestrictions;
    }

    public long getSpeedLimit() {
        return speedLimit;
    }

    public void setSpeedLimit(long speedLimit) {
        this.speedLimit = speedLimit;
    }

    public SpeedLimitTypeRGA getSpeedLimitType() {
        return speedLimitType;
    }

    public void setSpeedLimitType(SpeedLimitTypeRGA speedLimitType) {
        this.speedLimitType = speedLimitType;
    }

    public SpeedLimitVehicleType getVehicleTypes() {
        return vehicleTypes;
    }

    public void setVehicleTypes(SpeedLimitVehicleType vehicleTypes) {
        this.vehicleTypes = vehicleTypes;
    }

    public RGATimeRestrictions getTimeRestrictions() {
        return timeRestrictions;
    }

    public void setTimeRestrictions(RGATimeRestrictions timeRestrictions) {
        this.timeRestrictions = timeRestrictions;
    }

    @Override
    public String toString() {
        return "IndividualSpeedLimitSettings [speedLimit=" + speedLimit + ", speedLimitType=" + speedLimitType
                + ", vehicleTypes=" + vehicleTypes + ", timeRestrictions=" + timeRestrictions + "]";
    }
}
