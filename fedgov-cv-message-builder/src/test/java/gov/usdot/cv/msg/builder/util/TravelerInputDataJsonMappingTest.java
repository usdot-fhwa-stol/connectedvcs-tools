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

package gov.usdot.cv.msg.builder.util;
import gov.usdot.cv.msg.builder.input.TravelerInputData;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TravelerInputDataJsonMappingTest {
    @Test
    public void jsonToPojo_mapsAllFields() throws Exception {

        TravelerInputData data = JSONMapper.jsonFileToPojo("src/test/resources/sample_tim_withroadfriction.json",
                TravelerInputData.class);

        assertNotNull(data);
        assertNotNull(data.anchorPoint);

        // road-surface fields
        assertEquals("ASPHALT_OR_TAR", data.anchorPoint.road_surface);
        assertEquals(1, data.anchorPoint.road_surface_type);
        assertEquals("0", data.anchorPoint.road_condition);
        assertEquals(6L, data.anchorPoint.meanVerticalVariation);
        assertEquals(3L, data.anchorPoint.verticalVariationStdDev);
        assertEquals(10L, data.anchorPoint.meanHorizontalVariation);
        assertEquals(4L, data.anchorPoint.horizontalVariationStdDev);

        // messageType / generateType
        assertEquals("TIM", data.messageType);
        assertEquals(TravelerInputData.GenerateType.TIM, data.getGenerateType());
    }
}
