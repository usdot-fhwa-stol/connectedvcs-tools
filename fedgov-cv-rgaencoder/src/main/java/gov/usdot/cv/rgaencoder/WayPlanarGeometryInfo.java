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

public class WayPlanarGeometryInfo {
    private WayWidth wayWidth; // Optional field

    public WayPlanarGeometryInfo() {
        this.wayWidth = null;
    }

    public WayPlanarGeometryInfo(WayWidth wayWidth) {
        this.wayWidth = wayWidth;
    }

    public WayWidth getWayWidth() {
        return wayWidth;
    }

    public void setWayWidth(WayWidth wayWidth) {
        this.wayWidth = wayWidth;
    }

    @Override
    public String toString() {
        return "WayPlanarGeometryInfo [wayWidth=" + (wayWidth != null ? wayWidth.toString() : "null") + "]";
    }
}
