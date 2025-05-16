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

import gov.usdot.cv.mapencoder.Position3D;


public class ReferencePointInfo {
    private Position3D location;
    private DDate timeOfCalculation;

    public ReferencePointInfo() {
        this.location = null;
        this.timeOfCalculation = null;
    }

    public ReferencePointInfo(Position3D location, DDate timeOfCalculation) {
        this.location = location;
        this.timeOfCalculation = timeOfCalculation;
    }
    
    public Position3D getLocation() {
        return location;
    }

    public void setLocation(Position3D location) {
        this.location = location;
    }

    public DDate getTimeOfCalculation() {
        return timeOfCalculation;
    }

    public void setTimeOfCalculation(DDate timeOfCalculation) {
        this.timeOfCalculation = timeOfCalculation;
    }

    @Override
    public String toString() {
        return "ReferencePointInfo [location= " + location + ", timeOfCalculation = " + timeOfCalculation +  " ]";
    }


    
}