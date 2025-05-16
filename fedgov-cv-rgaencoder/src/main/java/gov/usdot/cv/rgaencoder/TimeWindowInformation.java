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

public class TimeWindowInformation {
    private DaysOfTheWeek daysOfTheWeek;
    private DDateTime startPeriod;
    private DDateTime endPeriod;
    private GeneralPeriod generalPeriod;

    public TimeWindowInformation() {
        this.daysOfTheWeek = null;
        this.startPeriod = null;
        this.endPeriod = null;
        this.generalPeriod = null;
    }

    public TimeWindowInformation(DaysOfTheWeek daysOfTheWeek, DDateTime startPeriod, DDateTime endPeriod, GeneralPeriod generalPeriod) {
        this.daysOfTheWeek = daysOfTheWeek;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
        this.generalPeriod = generalPeriod;
    }

    public DaysOfTheWeek getDaysOfTheWeek() {
        return daysOfTheWeek;
    }

    public void setDaysOfTheWeek(DaysOfTheWeek daysOfTheWeek) {
        this.daysOfTheWeek = daysOfTheWeek;
    }
    
    public DDateTime getStartPeriod() {
        return startPeriod;
    }

    public void setStartPeriod(DDateTime startPeriod) {
        this.startPeriod = startPeriod;
    }

    public DDateTime getEndPeriod() {
        return endPeriod;
    }

    public void setEndPeriod(DDateTime endPeriod) {
        this.endPeriod = endPeriod;
    }

    public GeneralPeriod getGeneralPeriod() {
        return generalPeriod;
    }
    
    public void setGeneralPeriod(GeneralPeriod generalPeriod) {
        this.generalPeriod = generalPeriod;
    }

    @Override
    public String toString() {
        return "TimeWindowInformation [daysOfTheWeek = " +  daysOfTheWeek + ", startPeriod= " + startPeriod + ", endPeriod= " + ", generalPeriod= " + generalPeriod + "]";
    }
    
}