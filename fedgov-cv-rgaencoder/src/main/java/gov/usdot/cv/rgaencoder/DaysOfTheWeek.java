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

public class DaysOfTheWeek {
    private short daysOfTheWeekValue;

    // Constants for choice field
    public static final short ALLDAYS = 0;
    public static final short MONDAY = 1;
    public static final short TUESDAY = 2;
    public static final short WEDNESDAY = 3;
    public static final short THURSDAY = 4;
    public static final short FRIDAY = 5;
    public static final short SATURDAY = 6;
    public static final short SUNDAY = 7;

    public DaysOfTheWeek() {
    }

    public DaysOfTheWeek(short daysOfTheWeekValue) {
            this.daysOfTheWeekValue = daysOfTheWeekValue;
        }

    public short getDaysOfTheWeekValue() {
        return daysOfTheWeekValue;
    }

    public void setDaysOfTheWeekValue(short daysOfTheWeekValue) {
        this.daysOfTheWeekValue = daysOfTheWeekValue;
    }

    @Override
    public String toString() {
        return "DaysOfTheWeek{" +
                "daysOfTheWeekValue=" + daysOfTheWeekValue +
                '}';
    }
}