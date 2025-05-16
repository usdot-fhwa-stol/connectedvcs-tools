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
import java.util.List;
import java.util.ArrayList;

public class RGAData {
    private BaseLayer baseLayer;
    private List<GeometryContainer> geometryContainers;
    
    public RGAData()
    {
        this.baseLayer = null;
        this.geometryContainers = new ArrayList<>();
    }
    
    public RGAData(BaseLayer baseLayer, List<GeometryContainer> geometryContainers) {
        this.baseLayer = baseLayer;
        this.geometryContainers = geometryContainers;
    }

    public BaseLayer getBaseLayer() {
        return baseLayer;
    }

    public void setBaseLayer(BaseLayer baseLayer){
        this.baseLayer = baseLayer;
    }

    public List<GeometryContainer> getGeometryContainers() {
        return geometryContainers;
    }

    public void setGeometryContainers(List<GeometryContainer> geometryContainers) {
        this.geometryContainers = geometryContainers;
    }

    public void addGeometryContainer(GeometryContainer geometryContainer) {
        if (geometryContainer != null) {
            this.geometryContainers.add(geometryContainer);
        }
    }

    @Override
    public String toString() {
        return "RGAData [baseLayer" + baseLayer + ", geometryContainers=" + 
               (geometryContainers.isEmpty() ? "[]" : geometryContainers.toString()) + "]";
    }
}
