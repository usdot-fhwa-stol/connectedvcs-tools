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

public class TimeWindowItemControlInfo {
    private List<TimeWindowInformation> timeWindowSet;

    public TimeWindowItemControlInfo() {
        this.timeWindowSet = new ArrayList<>();
    }

    public TimeWindowItemControlInfo(List<TimeWindowInformation> timeWindowSet) {
        this.timeWindowSet = timeWindowSet;
    }

    public List<TimeWindowInformation> getTimeWindowSet() {
        return timeWindowSet;
    }

    public void setTimeWindowSet(List<TimeWindowInformation> timeWindowSet) {
        this.timeWindowSet = timeWindowSet;
    }

    public void addTimeWindowSet(TimeWindowInformation timeWindowInformation) {
        if (timeWindowInformation != null) {
            this.timeWindowSet.add(timeWindowInformation);
        }
    }

    @Override
    public String toString() {
        return "TimeWindowControlInfo [timeWindowSet="
                + (timeWindowSet != null ? timeWindowSet.toString() : "null") + "]";
    }
}