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

public class GeneralPeriod {
    private int generalPeriodValue;

    // Constants for choice field
    public static final int DAY = 0;
    public static final int NIGHT = 1;

    public GeneralPeriod() {
    }

    public GeneralPeriod(int generalPeriodValue) {
        this.generalPeriodValue = generalPeriodValue;
    }

    public int getGeneralPeriodValue() {
        return generalPeriodValue;
    }

    public void setGeneralPeriodValue(int generalPeriodValue) {
        this.generalPeriodValue = generalPeriodValue;
    }

    @Override
    public String toString() {
        return "GeneralPeriod{" +
                "generalPeriodValue=" + generalPeriodValue +
                '}';
    }
}