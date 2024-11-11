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

public class LaneDataAttribute {
    private int choice;
    private int pathEndPointAngle;
    private int laneCrownPointCenter;
    private int laneCrownPointRight;
    private int laneCrownPointLeft;
    private int laneAngle;
    private SpeedLimitList speedLimits;

    //enum choices
    public static final int PATH_END_POINT_ANGLE = 1;
    public static final int LANE_CROWN_POINT_CENTER = 2;
    public static final int LANE_CROWN_POINT_RIGHT = 3;
    public static final int LANE_CROWN_POINT_LEFT = 4;
    public static final int LANE_ANGLE = 5;
    public static final int SPEED_LIMITS = 6;

    public LaneDataAttribute() {
    }

    public LaneDataAttribute(int choice, int pathEndPointAngle, int laneCrownPointCenter, int laneCrownPointRight, int laneCrownPointLeft, int laneAngle, SpeedLimitList speedLimits) {
        this.choice = choice;
        this.pathEndPointAngle = pathEndPointAngle;
        this.laneCrownPointCenter = laneCrownPointCenter;
        this.laneCrownPointRight = laneCrownPointRight;
        this.laneCrownPointLeft = laneCrownPointLeft;
        this.laneAngle = laneAngle;
        this.speedLimits = speedLimits;
    }

    public int getChoice() {
        return choice;
    }

    public void setChoice(int choice) {
        this.choice = choice;
    }

    public int getPathEndPointAngle() {
        return pathEndPointAngle;
    }

    public void setPathEndPointAngle(int pathEndPointAngle) {
        this.pathEndPointAngle = pathEndPointAngle;
    }

    public int getLaneCrownPointCenter() {
        return laneCrownPointCenter;
    }

    public void setLaneCrownPointCenter(int laneCrownPointCenter) {
        this.laneCrownPointCenter = laneCrownPointCenter;
    }

    public int getLaneCrownPointRight() {
        return laneCrownPointRight;
    }

    public void setLaneCrownPointRight(int laneCrownPointRight) {
        this.laneCrownPointRight = laneCrownPointRight;
    }

    public int getLaneCrownPointLeft() {
        return laneCrownPointLeft;
    }

    public void setLaneCrownPointLeft(int laneCrownPointLeft) {
        this.laneCrownPointLeft = laneCrownPointLeft;
    }

    public int getLaneAngle() {
        return laneAngle;
    }

    public void setLaneAngle(int laneAngle) {
        this.laneAngle = laneAngle;
    }

    public SpeedLimitList getSpeedLimits() {
        return speedLimits;
    }

    public void setSpeedLimits(SpeedLimitList speedLimits) {
        this.speedLimits = speedLimits;
    }
}
