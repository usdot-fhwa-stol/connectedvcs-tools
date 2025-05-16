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

public class BaseLayer {
    //DataSetFormatVersionInfo
    private int majorVer;
    private int minorVer;

    //ReferencePointInfo
    private Position3D location;
    private DDate timeOfCalculation;

    //RoadGeometryRefIDInfo
    private boolean fullRdAuthIDExists;
    private int[] fullRdAuthID;
    private boolean relRdAuthIDExists;
    private int[] relRdAuthID;
    private int[] relativeToRdAuthID;

    //DataSetContentIdentification
    private int contentVer;
    private DDateTime contentDateTime;

    public BaseLayer()
    {
        this.majorVer = 0;
        this.minorVer = 0;
        this.location = null;
        this.timeOfCalculation = null;
        this.fullRdAuthIDExists = false;
        this.fullRdAuthID = null; 
        this.relRdAuthIDExists = false;
        this.relRdAuthID = null;
        this.relativeToRdAuthID = null;
        this.contentVer = 0;
        this.contentDateTime = null;
    }
    
    public BaseLayer(int majorVer, int minorVer, Position3D location, DDate timeOfCalculation, int[] fullRdAuthID, boolean relRdAuthIDExists, int[] relRdAuthID, int[] relativeToRdAuthID, int contentVer, DDateTime contentDateTime) {
        this.majorVer = majorVer;
        this.minorVer = minorVer;
        this.location = location;
        this.timeOfCalculation = timeOfCalculation;
        this.fullRdAuthIDExists = fullRdAuthIDExists;
        this.fullRdAuthID = fullRdAuthID;
        this.relRdAuthIDExists = relRdAuthIDExists;
        this.relRdAuthID = relRdAuthID;
        this.relativeToRdAuthID = relativeToRdAuthID;
        this.contentVer = contentVer;
        this.contentDateTime = contentDateTime;
    }

    public int getMajorVer() {
        return majorVer;
    }

    public int getMinorVer() {
        return minorVer;
    }

    public Position3D getLocation() {
        return location;
    }

    public DDate getTimeOfCalculation() {
        return timeOfCalculation;
    }

    public boolean isFullRdAuthIDExists() {
        return fullRdAuthIDExists;
    }

    public void setFullRdAuthIDExists(boolean fullRdAuthIDExists) {
        this.fullRdAuthIDExists = fullRdAuthIDExists;
    }

    public int[] getFullRdAuthID() {
        return fullRdAuthID;
    }

    public void setFullRdAuthID(int[] fullRdAuthID) {
        this.fullRdAuthID = fullRdAuthID;
    }

    public boolean isRelRdAuthIDExists() {
        return relRdAuthIDExists;
    }

    public void setRelRdAuthIDExists(boolean relRdAuthIDExists) {
        this.relRdAuthIDExists = relRdAuthIDExists;
    }

    public int[] getRelRdAuthID() {
        return relRdAuthID;
    }

    public void setRelRdAuthID(int[] relRdAuthID) {
        this.relRdAuthID = relRdAuthID;
    }
    
    public int[] getRelativeToRdAuthID() {
        return relativeToRdAuthID;
    }

    public void setRelativeToRdAuthID(int[] relativeToRdAuthID) {
        this.relativeToRdAuthID = relativeToRdAuthID;
    }

    public int getContentVer() {
        return contentVer;
    }

    public DDateTime getContentDateTime() {
        return contentDateTime;
    }

    public void setMajorVer(int majorVer) {
        this.majorVer = majorVer;
    }

    public void setMinorVer(int minorVer) {
        this.minorVer = minorVer;
    }

    public void setLocation(Position3D location) {
        this.location = location;
    }

    public void setTimeOfCalculation(DDate timeOfCalculation) {
        this.timeOfCalculation = timeOfCalculation;
    }

    public void setContentVer(int contentVer) {
        this.contentVer = contentVer;
    }

    public void setContentDateTime(DDateTime contentDateTime) {
        this.contentDateTime = contentDateTime;
    }

    @Override
    public String toString() {
        return "RGAData [majorVer=" + majorVer + ", minorVer=" + minorVer + ", location="+ location + ", timeOfCalculation" + timeOfCalculation 
            + ", contentVer=" + contentVer + ", contentDate Time" + contentDateTime + ", fullRdAuthID " + fullRdAuthID + "]";
    }
}
 