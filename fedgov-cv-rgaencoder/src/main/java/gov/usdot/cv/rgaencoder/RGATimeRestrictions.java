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

public class RGATimeRestrictions {
    private int choice;
    private TimeWindowItemControlInfo fixedTimeWindowItemCtrl;
    private OtherDSItemControlInfo otherDataSetItemCtrl;

    // Constants to represent the choices
    public static final int TIME_WINDOW_ITEM_CONTROL = 1;
    public static final int OTHER_DATA_SET_ITEM_CONTROL = 2;

    public RGATimeRestrictions() {
        this.choice = -1;
        this.fixedTimeWindowItemCtrl = null;
        this.otherDataSetItemCtrl = null;
    }

    public RGATimeRestrictions(int choice, TimeWindowItemControlInfo fixedTimeWindowItemCtrl, OtherDSItemControlInfo otherDataSetItemCtrl) {
        this.choice = choice;
        this.fixedTimeWindowItemCtrl = fixedTimeWindowItemCtrl;
        this.otherDataSetItemCtrl = otherDataSetItemCtrl;
    }

    public int getChoice() {
        return choice;
    }
    
    public void setChoice(int choice) {
        this.choice = choice;
    }

    public TimeWindowItemControlInfo getFixedTimeWindowCtrl() {
        return fixedTimeWindowItemCtrl;
    }

    public void setFixedTimeWindowCtrl(TimeWindowItemControlInfo fixedTimeWindowItemCtrl) {
        this.fixedTimeWindowItemCtrl = fixedTimeWindowItemCtrl;
    }

    public OtherDSItemControlInfo getOtherDataSetItemCtrl() {
        return otherDataSetItemCtrl;
    }

    public void setOtherDataSetItemCtrl(OtherDSItemControlInfo otherDataSetItemCtrl) {
        this.otherDataSetItemCtrl = otherDataSetItemCtrl;
    }

    @Override
    public String toString() {
        return "RGATimeRestrictions [fixedTimeWindowItemCtrl=" + fixedTimeWindowItemCtrl + " , otherDataSetItemCtrl= " + otherDataSetItemCtrl + "]";
    }




    
}