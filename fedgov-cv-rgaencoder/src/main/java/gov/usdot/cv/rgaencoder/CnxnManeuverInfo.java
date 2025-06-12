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

public class CnxnManeuverInfo {
    private WayCnxnManeuvers allowedManeuvers;
    private WayCnxnManeuverControlType maneuverControlType;
    private RGATimeRestrictions timeRestrictions; //Optional

    public CnxnManeuverInfo() {
        this.allowedManeuvers = null;
        this.maneuverControlType = null;
        this.timeRestrictions = null;
    }

    public CnxnManeuverInfo(WayCnxnManeuvers allowedManeuvers, WayCnxnManeuverControlType maneuverControlType,
            RGATimeRestrictions timeRestrictions) {
        this.allowedManeuvers = allowedManeuvers;
        this.maneuverControlType = maneuverControlType;
        this.timeRestrictions = timeRestrictions;
    }

    public WayCnxnManeuvers getAllowedManeuvers() {
        return allowedManeuvers;
    }

    public void setAllowedManeuvers(WayCnxnManeuvers allowedManeuvers) {
        this.allowedManeuvers = allowedManeuvers;
    }

    public WayCnxnManeuverControlType getManeuverControlType() {
        return maneuverControlType;
    }

    public void setManeuverControlType(WayCnxnManeuverControlType maneuverControlType) {
        this.maneuverControlType = maneuverControlType;
    }

    public RGATimeRestrictions getTimeRestrictions() {
        return timeRestrictions;
    }

    public void setTimeRestrictions(RGATimeRestrictions timeRestrictions) {
        this.timeRestrictions = timeRestrictions;
    }

    @Override
    public String toString() {
        return "CnxnManeuverInfo [allowedManeuvers=" + allowedManeuvers + ", maneuverControlType=" + maneuverControlType
                + ", timeRestrictions=" + timeRestrictions + "]";
    }
}
