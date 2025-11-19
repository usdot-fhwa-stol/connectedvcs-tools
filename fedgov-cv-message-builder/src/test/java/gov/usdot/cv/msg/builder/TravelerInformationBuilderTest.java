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
import gov.usdot.cv.msg.builder.exception.MessageEncodeException;
import gov.usdot.cv.msg.builder.message.TravelerInformationMessage;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import org.junit.Test;

public class TravelerInformationBuilderTest {

	@Test
	public void testBuildTravelerInformation() throws IOException {
		testBuildTravelerInformation("sample_tim.json","0011006099ba1ebc7a9ba564003eca3162d008006e133743d78f5374ac80b70007c00445d3451824dd8f4c927147bcc929a3fec00100f9a808110020"); 
		testBuildTravelerInformation("sample_timplusframe.json","001f3c0011006099ba1ebc7a9ba564003eca3162d008006e133743d78f5374ac80b70007c00445d3451824dd8f4c927147bcc929a3fec00100f9a808110020");
		testBuildTravelerInformation("tim_Circle.json","001102629dc2385273926240217000006fb102d030406a53b8470a4e724c48042e00000b00000816e0d693a401ad2747ff5640420000430040880100");
		testBuildTravelerInformation("tim_Circle2.json","001102629dc2385273926240217000006fb102d030406a53b8470a4e724c48042e00000b00000816e0d693a401ad2747fc1f50420000430040880100");
		testBuildTravelerInformation("sample_tim_lane.json","001f46001100a292837b0e4adfe1d21ff43000a642bf4020007e52506f61c95bfc3a43fe80b7218000003895bfc36e4a0debd81450e25700a2992837bc2050a0000007cd8040880100"); 
		testBuildTravelerInformation("sample_tim_poly.json","001f6e0011006299ba56787a9b8926204dfffe7c12bf4028007e53374acf0f537124c40983e87ffff00535e5a5d7000a0a2c4e2cb7495fdd60f90afabd053ed6a4688c3027b0b01f1045ad92da78dc02850b08e6fbd6581ff7deb2c0973ede96005dee44aeb82d17400000171201022004"); 
		testBuildTravelerInformation("sample_tim_tight.json","001f6d001100a299b9ec767a9ba76a204ffffe7c12bf4038007e53373d8ecf5374ed4409c0b77ffff006a80969f40a0a0b8c2f02da16f656e23a9014143c44bd785fe50462cd085d165cc5c8b59e3a02e33610857daf0670e7920cb4d017518cc2e813ec233b897000807cdc04088010"); 
	}

	public void testBuildTravelerInformation(String timName, String expectedString) throws IOException {
		TravelerInformationBuilder timBuilder = new TravelerInformationBuilder();
		String json = FileUtils.readFileToString(new File("src/test/resources/" + timName));
		TravelerInformationMessage timMessage = (TravelerInformationMessage) timBuilder.build(json);
		System.out.println("TIM Message Name: " + timMessage.getMessageName());
		assertEquals(expectedString, timMessage.getHexString());
	}

}