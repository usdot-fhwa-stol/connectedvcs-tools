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

package gov.usdot.cv.msg.builder;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.xml.bind.DatatypeConverter;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.usdot.cv.msg.builder.util.TIMValidator;
import gov.usdot.cv.msg.builder.util.SemiValidatorException;

public class TIMValidatorTest {

    private static final Logger logger = LogManager.getLogger(TIMValidatorTest.class);

    @Test
    public void TIMValidatorDecodeTest() throws SemiValidatorException {

        TIMValidator validator = new TIMValidator();

        // TIM payload in HEX (replace with a valid TIM hex if needed)
        byte[] bytes = DatatypeConverter.parseHexBinary(
            "001f5f201000000000002026731180b29dc20c8473928d7a374800002fd2fcd2c00030007e53b841908e7251af46e900b74000000118e7251f2e77082f3b8e725459a7707d7c813ec639c954489dc1e182639c959a99dc1cfe800100f9a808110020"
        );

        // Decode TIM
        String decodedMessage = validator.validateTIM(bytes);

        System.out.println("Decoded TIM Message:\n" + decodedMessage);

        // Basic sanity checks
        assertTrue("Decoded message should not be empty",
                decodedMessage != null && !decodedMessage.isEmpty());

        // Ensure ASN.1 structure exists
        assertTrue("Decoded message should contain MessageFrame",
                decodedMessage.contains("MessageFrame"));

        // Extract messageId from ASN.1 text
        Pattern pattern = Pattern.compile("messageId:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(decodedMessage);

        int messageId = -1;
        if (matcher.find()) {
            messageId = Integer.parseInt(matcher.group(1));
        }

        // TIM Message ID = 31
        Assert.assertEquals("TIM messageId should be 31", 31, messageId);
    }

    @Test
    public void TIMValidatorEmptyBytesTest() {

        TIMValidator validator = new TIMValidator();
        byte[] bytes = new byte[] {};

        try {
            validator.validateTIM(bytes);
            fail("Expected SemiValidatorException to be thrown");
        } catch (SemiValidatorException e) {
            assertTrue(true);
        }
    }
}
