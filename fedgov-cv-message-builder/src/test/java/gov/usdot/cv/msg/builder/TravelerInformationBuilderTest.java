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
		testBuildTravelerInformation("tim2.json",
				"1102629dc20a84739287f81ffe0000ea840168190000188061a948043062470cd3e1972e441b34edd3d1074de8336fd9b37f758837336031018079f41a3e0081100200"); // lane

	}

	public void testBuildTravelerInformation(String timName, String expectedhexString) throws IOException {
		TravelerInformationBuilder timBuilder = new TravelerInformationBuilder();
		String json = FileUtils.readFileToString(new File("src/test/resources/" + timName));
		TravelerInformationMessage timMessage = (TravelerInformationMessage) timBuilder.build(json);
		assertEquals(timMessage.getHexString(), expectedhexString);

	}

}