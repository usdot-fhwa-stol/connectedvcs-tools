package gov.usdot.cv.msg.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gov.usdot.cv.timencoder.*;
import gov.usdot.cv.msg.builder.message.SemiMessage;
import gov.usdot.cv.msg.builder.message.TravelerInformationMessage;
import gov.usdot.cv.msg.builder.util.J2735Helper;
import gov.usdot.cv.msg.builder.util.OffsetEncoding;
import gov.usdot.cv.msg.builder.util.OffsetEncoding.OffsetEncodingSize;
import org.apache.commons.io.FileUtils;



import java.io.File;
import java.io.IOException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import java.io.File;
import java.io.IOException;


public class TravelerInformationBuilderTest {
	
	@Test
	public void testBuildTravelerInformation() throws IOException {
		testBuildTravelerInformation("tim2.json");	// lane
		testBuildTravelerInformation("tim8.json");	// region
		// testBuildTravelerInformation("tim_Circle.json");  // circle
		// testBuildTravelerInformation("tim4.json");	// lane, new ITIS code
	
	}
	
	public void testBuildTravelerInformation(String timName) throws IOException {
		TravelerInformationBuilder timBuilder = new TravelerInformationBuilder();
		String json = FileUtils.readFileToString(new File("src/test/resources/" + timName));
		TravelerInformationMessage timMessage = (TravelerInformationMessage)timBuilder.build(json);
		
		

    }


}