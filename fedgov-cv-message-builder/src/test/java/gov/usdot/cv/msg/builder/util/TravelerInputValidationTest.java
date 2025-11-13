/*
 * Copyright (C) 2025 LEIDOS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gov.usdot.cv.msg.builder.util;

import org.junit.Test;
import gov.usdot.cv.msg.builder.input.TravelerInputData;

public class TravelerInputValidationTest {

    private TravelerInputData createValidTravelerInput() throws Exception {
        TravelerInputData data = JSONMapper.jsonFileToPojo(
                "src/test/resources/sample_tim_withroadfriction.json",
                TravelerInputData.class);
        data.validate();
        return data;
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_invalidRegionType_throws() throws Exception {
        TravelerInputData data = createValidTravelerInput();
        data.regions[0].regionType = "badregiontype";
        data.validate();
    }

    @Test
    public void validate_validRegionTypes_pass() throws Exception {
        TravelerInputData data = createValidTravelerInput();
        String[] validValues = { "lane", "region", "circle" };
        for (String type : validValues) {
            data.regions[0].regionType = type;
            data.validate();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_invalidExtent_throws() throws Exception {
        TravelerInputData data = createValidTravelerInput();
        data.regions[0].extent = 25;
        data.validate();
    }

    @Test
    public void validate_validExtent_passes() throws Exception {
        TravelerInputData data = createValidTravelerInput();
        data.regions[0].extent = 5;
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_invalidLaneNodeLat_throws() throws Exception {
        TravelerInputData data = createValidTravelerInput();
        data.regions[0].laneNodes[0].nodeLat = 0.0;
        data.validate();
    }

    @Test
    public void validate_validLaneNodeLat_passes() throws Exception {
        TravelerInputData data = createValidTravelerInput();
        data.regions[0].laneNodes[0].nodeLat = 42.123456;
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_invalidMutcd_throwsWithMessage() throws Exception {
        TravelerInputData data = createValidTravelerInput();
        data.anchorPoint.mutcd = 99;
        data.validate();
    }

    @Test
    public void validate_validMutcd_passes() throws Exception {
        TravelerInputData data = createValidTravelerInput();
        data.anchorPoint.mutcd = 2;
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_startTimeEmpty_throws() throws Exception {
        TravelerInputData data = createValidTravelerInput();
        data.anchorPoint.startTime = "";
        data.validate();
    }

    @Test
    public void validate_validStartTime_passes() throws Exception {
        TravelerInputData data = createValidTravelerInput();
        data.anchorPoint.startTime = "05/20/2015 10:56 AM";
        data.anchorPoint.endTime = "05/21/2015 10:56 AM";

        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_startTimeAfterEndTime_throws() throws Exception {
        TravelerInputData data = createValidTravelerInput();
        data.anchorPoint.endTime = "";
        data.validate();
    }

        @Test
    public void validate_validEndTime_passes() throws Exception {
        TravelerInputData data = createValidTravelerInput();
         data.anchorPoint.startTime = "05/20/2015 10:56 AM";
        data.anchorPoint.endTime = "05/21/2015 10:56 AM";
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_invalidInfoType_throws() throws Exception {
        TravelerInputData data = createValidTravelerInput();
        data.anchorPoint.infoType = 99;
        data.validate();
    }

    @Test
    public void validate_validInfoType_passes() throws Exception {
        TravelerInputData data = createValidTravelerInput();
        data.anchorPoint.infoType = 2;
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_invalidVerifiedMapLat_throws() throws Exception {
        TravelerInputData data = createValidTravelerInput();
        data.verifiedPoint.verifiedMapLat = 0.0;
        data.validate();
    }

    @Test
    public void validate_validVerifiedMapLat_passes() throws Exception {
        TravelerInputData data = createValidTravelerInput();
        data.verifiedPoint.verifiedMapLat = 42.123456;
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_invalidVerifiedSurveyedLon_throws() throws Exception {
        TravelerInputData data = createValidTravelerInput();
        data.verifiedPoint.verifiedSurveyedLon = 200.0;
        data.validate();
    }

    @Test
    public void validate_validVerifiedSurveyedLon_passes() throws Exception {
        TravelerInputData data = createValidTravelerInput();
        data.verifiedPoint.verifiedSurveyedLon = -70.0;
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_invalidTimeToLive_throws() throws Exception {
        TravelerInputData data = createValidTravelerInput();
        if (data.deposit == null) {
            data.deposit = new TravelerInputData.Deposit();
        }
        data.deposit.timeToLive = 99;
        data.validate();
    }

    @Test
    public void validate_validTimeToLive_passes() throws Exception {
        TravelerInputData data = createValidTravelerInput();
        if (data.deposit == null) {
            data.deposit = new TravelerInputData.Deposit();
        }
        data.deposit.timeToLive = 3;
        data.validate();
    }
}
