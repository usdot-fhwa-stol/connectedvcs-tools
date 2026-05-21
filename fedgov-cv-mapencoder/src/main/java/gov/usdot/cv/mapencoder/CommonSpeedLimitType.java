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

public class CommonSpeedLimitType {
    private byte speedLimitType;

    // Constants
    public static final byte UNKNOWN = 0;
    public static final byte MAXSPEEDINSCHOOLZONE = 1;
    public static final byte MAXSPEEDINSCHOOLZONEWHENCHILDRENAREPRESENT = 2;
    public static final byte MAXSPEEDINCONSTRUCTIONZONE = 3;
    public static final byte VEHICLEMINSPEED = 4;
    public static final byte VEHICLEMAXSPEED = 5;
    public static final byte VEHICLENIGHTMAXSPEED = 6;
    public static final byte TRUCKMINSPEED = 7;
    public static final byte TRUCKMAXSPEED = 8;
    public static final byte TRUCKNIGHTMAXSPEED = 9;
    public static final byte VEHICLESWITHTRAILERSMINSPEED = 10;
    public static final byte VEHICLESWITHTRAILERSMAXSPEED = 11;
    public static final byte VEHICLESWITHTRAILERSNIGHTMAXSPEED = 12;

    // Constructors
    public CommonSpeedLimitType() {
    }

    public CommonSpeedLimitType(byte speedLimitType) {
        this.speedLimitType = speedLimitType;
    }

    // Getter and Setter
    public byte getSpeedLimitType() {
        return speedLimitType;
    }

    public void setSpeedLimitType(byte speedLimitType) {
        this.speedLimitType = speedLimitType;
    }

    @Override
    public String toString() {
        return "SpeedLimitType{" +
                "speedLimitType=" + speedLimitType +
                '}';
    }

}
