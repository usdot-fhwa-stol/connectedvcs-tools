/*

Copyright (C) 2025 LEIDOS.
Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
*/
package gov.usdot.cv.service.rest;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import javax.xml.bind.DatatypeConverter;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.sun.jersey.test.framework.JerseyTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SemiValidatorTest extends JerseyTest {
	
	private static SemiValidator validator;
	    private static final Logger logger = LogManager.getLogger(SemiValidatorTest.class);


	
	public SemiValidatorTest() throws Exception {
		super("gov.usdot.cv.service.rest");
	}

	@Test
	public void ValidatorTest() throws SemiValidatorException {
	
		validator = new SemiValidator();
	
		//MAP payload in Hex and converting it into binary

		byte[] bytes = DatatypeConverter.parseHexBinary("0012829138023000201428094edb9bd3396687aa196a02dc0e200224000800518e59a274276dcf1b98e59a262e76dd28a18e59a25a676dd61f38e59a249276dd91b814146396688df9db7735a6396689479db77db4e39668a5a9db784aa050518e59a306a76de24318e59a3a1a76de34198e59a456676de3e718e59a4e8e76de3f418e59a561676de3cd920112000200186396685c89db7393c6396685eb9db744fa6396685eb9db7518463966860d9db75c76e396686309db76dc6050518e59a194a76ddd5598e59a19d276ddf3998e59a1bfa76de0c4b8e59a204a76de21c81414639668a5b9db78d6a639668cc79db790cc6396690029db793646396692909db793966396695cb9db792fe48030240281201c08400cc8001000171cb340bd4edb981f027d8c72ccf3a93b6e5551c72ccdeb53b6e46c009f624044400040005c72cd04bd3b6e566809f631cb33d4ecedb932971cb33840cedb8fc0027d890020880504403820802a900020002e396685909db727f204fb18e59a16ae76dc66218e59a156e76dc28292033200020002e396688d09db7286a04fb18e59a22d676dc66c38e59a22d676dc296013ec48010640183201c18401dc8001000b31cb34706cedb99c931cb348c7cedb9ca731cb349c6cedb9f0e31cb34a2b4edba14331cb34a53cedba37931cb34a3f4edba57331cb34a09cedba71331cb349764edba92131cb348b3cedbab0771cb347a0cedbad6402828c72cd1af33b6ebcd8c72cd186f3b6ec3d4c72cd15653b6ecd98902210001000831cb3461bcedb9beb31cb347a7cedb9e1631cb348924edba01031cb348d54edba26d31cb348c7cedba47b31cb348924edba63931cb347f84edba8a031cb34706cedbab1b71cb345eccedbadbd02828c72cd15153b6ebdc8900210803084028400");
		//calling SemiValidator to decode

		String decodedMessage=validator.validate(bytes);

		/*Acessing message content returned as a json fromatted String */
		try{
			JSONObject jsonObject = new JSONObject(decodedMessage);
			//Accessing messageframe
            String messageContent = jsonObject.getString("decodedMessage");
			System.out.println(messageContent);

			//Using Java regular expression to find a match of messgae ID
			Pattern pattern = Pattern.compile("messageId:\\s*(\\d+)");
			
			//searching string to find pattern with messageID
        	Matcher matcher = pattern.matcher(messageContent);
			int messageId=0;
        	if (matcher.find()) {
             	messageId = Integer.parseInt(matcher.group(1));
            	
        	} 

		//Comparing if messageID:18 is present in the decoded string Hex value is a MAP with messageID:18
		 Assert.assertEquals("The messageID should match",18,messageId);

		}
		catch (Exception e) {
           // LOGGER.log("Failed to retrieve content from jsonObject", e);
			logger.info("Context",e);
        }	
	}


    @Test
    public void ValidatorTestEmptyBytes() throws SemiValidatorException{
		byte[] bytes= new byte[]{};
		validator = new SemiValidator();

		 try {
			//should throw a SemiValidatorException 
            validator.validate(bytes);

            fail("Expected SemiValidatorException to be thrown");
        } catch (SemiValidatorException e) {

			//Test passes if a SemiValidatorException is thrown
            assertTrue(true);

        }



    }


}
