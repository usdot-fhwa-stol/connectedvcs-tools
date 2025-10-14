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

package gov.usdot.cv.msg.builder;

import static org.junit.Assert.assertEquals;
import gov.usdot.cv.timencoder.*;
import gov.usdot.cv.msg.builder.message.TravelerInformationMessage;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import org.junit.Test;


public class TravelerInformationBuilderTest {

	@Test
	public void testBuildTravelerInformation() throws IOException {
		testBuildTravelerInformation("sample_tim.json","0011006099ba1ebc7a9ba5640000ca3162d0080000000807cd4040880100"); 
		testBuildTravelerInformation("sample_timplusframe.json","001f1e0011006099ba1ebc7a9ba5640000ca3162d0080000000807cd4040880100");

	}

	public void testBuildTravelerInformation(String timName, String expectedString) throws IOException {
		TravelerInformationBuilder timBuilder = new TravelerInformationBuilder();
		String json = FileUtils.readFileToString(new File("src/test/resources/" + timName));
		TravelerInformationMessage timMessage = (TravelerInformationMessage) timBuilder.build(json);
		System.out.println("TIM Message Name: " + timMessage.getMessageName());
		assertEquals(expectedString, timMessage.getHexString());
		

	}

}