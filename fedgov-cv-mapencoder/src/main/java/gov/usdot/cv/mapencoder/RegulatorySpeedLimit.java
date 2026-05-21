/*
 * Copyright (C) 2023 LEIDOS.
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
 
package gov.usdot.cv.mapencoder;

public class RegulatorySpeedLimit {
    private CommonSpeedLimitType type;
    private double speed;

    // Constructors
    public RegulatorySpeedLimit() {
    }

    public RegulatorySpeedLimit(CommonSpeedLimitType type, double speed) {
        this.type = type;
        this.speed = speed;
    }

    // Getter and Setter
    public CommonSpeedLimitType getType() {
        return type;
    }

    public void setType(CommonSpeedLimitType type) {
        this.type = type;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public String toString() {
        return "RegulatorySpeedLimit{" +
                "type=" + type +
                ", speed=" + speed +
                '}';
    }

}
