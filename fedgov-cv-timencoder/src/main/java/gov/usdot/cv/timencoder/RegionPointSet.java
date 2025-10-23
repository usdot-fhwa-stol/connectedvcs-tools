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
import gov.usdot.cv.mapencoder.Position3D;

public class RegionPointSet {
    private Position3D anchor;
    private Zoom scale;
    private RegionList nodeList;

    public RegionPointSet(Position3D anchor, Zoom scale, RegionList nodeList) {
        this.anchor = anchor;
        this.scale = scale;
        this.nodeList = nodeList;
    }

    public Position3D getAnchor() {
        return anchor;
    }

    public Zoom getScale() {
        return scale;
    }

    public RegionList getRegionList() {
        return nodeList;
    }

    public void setAnchor(Position3D anchor) {
        this.anchor = anchor;
    }

    public void setScale(Zoom scale) {
        this.scale = scale;
    }

    public void setNodeList(RegionList nodeList) {
        this.nodeList = nodeList;
    }

    @Override
    public String toString() {
        return "RegionPointSet {" +
                 "anchor =" + anchor +
                 ", scale =" + scale +
                 ", nodeList=" + nodeList +
                 '}';
    }
    
}
