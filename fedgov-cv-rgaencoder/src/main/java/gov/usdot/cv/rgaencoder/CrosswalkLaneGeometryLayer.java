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

import java.util.ArrayList;
import java.util.List;

public class CrosswalkLaneGeometryLayer {
    private List<IndvCrosswalkLaneGeometryInfo> laneGeomLaneSet;

    public CrosswalkLaneGeometryLayer() {
        this.laneGeomLaneSet = new ArrayList<>();
    }

    public CrosswalkLaneGeometryLayer(List<IndvCrosswalkLaneGeometryInfo> laneGeomLaneSet) {
        this.laneGeomLaneSet = laneGeomLaneSet;
    }

    public List<IndvCrosswalkLaneGeometryInfo> getLaneGeomLaneSet() {
        return laneGeomLaneSet;
    }

    public void setLaneGeomLaneSet(List<IndvCrosswalkLaneGeometryInfo> laneGeomLaneSet) {
        this.laneGeomLaneSet = laneGeomLaneSet;
    }

    public void addIndvCrosswalkLaneGeometryInfo(IndvCrosswalkLaneGeometryInfo indvCrosswalkLaneGeometryInfo) {
        if (indvCrosswalkLaneGeometryInfo != null) {
            this.laneGeomLaneSet.add(indvCrosswalkLaneGeometryInfo);
        }
    }

    @Override
    public String toString() {
        return "CrosswalkLaneGeometryLayer{" +
                "laneGeomLaneSet=" + laneGeomLaneSet +
                '}';
    }
}
