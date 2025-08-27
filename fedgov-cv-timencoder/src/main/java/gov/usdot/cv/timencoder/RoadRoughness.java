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
public class RoadRoughness {

    private CommonMeanVariation meanVerticalVariation;          // required
    private VariationStdDev verticalVariationStdDev;            // optional
    private CommonMeanVariation meanHorizontalVariation;        // optional
    private VariationStdDev horizontalVariationStdDev;          // optional

    public RoadRoughness() {
    }

    public RoadRoughness(CommonMeanVariation meanVerticalVariation,
                         VariationStdDev verticalVariationStdDev,
                         CommonMeanVariation meanHorizontalVariation,
                         VariationStdDev horizontalVariationStdDev) {
        this.meanVerticalVariation = meanVerticalVariation;
        this.verticalVariationStdDev = verticalVariationStdDev;
        this.meanHorizontalVariation = meanHorizontalVariation;
        this.horizontalVariationStdDev = horizontalVariationStdDev;
    }

    public CommonMeanVariation getMeanVerticalVariation() {
        return meanVerticalVariation;
    }

    public void setMeanVerticalVariation(CommonMeanVariation meanVerticalVariation) {
        this.meanVerticalVariation = meanVerticalVariation;
    }

    public VariationStdDev getVerticalVariationStdDev() {
        return verticalVariationStdDev;
    }

    public void setVerticalVariationStdDev(VariationStdDev verticalVariationStdDev) {
        this.verticalVariationStdDev = verticalVariationStdDev;
    }

    public CommonMeanVariation getMeanHorizontalVariation() {
        return meanHorizontalVariation;
    }

    public void setMeanHorizontalVariation(CommonMeanVariation meanHorizontalVariation) {
        this.meanHorizontalVariation = meanHorizontalVariation;
    }

    public VariationStdDev getHorizontalVariationStdDev() {
        return horizontalVariationStdDev;
    }

    public void setHorizontalVariationStdDev(VariationStdDev horizontalVariationStdDev) {
        this.horizontalVariationStdDev = horizontalVariationStdDev;
    }

    @Override
    public String toString() {
        return "RoadRoughness{" +
                "meanVerticalVariation=" + meanVerticalVariation +
                ", verticalVariationStdDev=" + verticalVariationStdDev +
                ", meanHorizontalVariation=" + meanHorizontalVariation +
                ", horizontalVariationStdDev=" + horizontalVariationStdDev +
                '}';
    }
}
