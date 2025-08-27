
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TIMEncodeTest {
    private static final Logger logger = LogManager.getLogger(TIMEncodeTest.class);
    Encoder encoder;
    TravelerInformation mockTimData;
    @Before
    public void setUp() {
         encoder = new Encoder(); 

        }

    @Test
    public void TIM_encode_test() {
    ByteArrayObject result = encoder.encode(mockTimData);
    Assert.assertNotNull("Byte array should not be null", result.getMessage());    
    byte[] expected = new byte[] { (byte)0xDE, (byte)0xAD, (byte)0xBE, (byte)0xEF };
    Assert.assertArrayEquals("Encoded TIM message doesn't match expected output", expected, result.getMessage());

    }
}
