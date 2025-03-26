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
package gov.usdot.cv.asn1decoder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ASN1DecoderTest {
    private static final Logger logger = LogManager.getLogger(ASN1DecoderTest.class);
    Decoder decoder;
    ByteArrayObject mockMsg;

    @Before
    public void setup() {


        mockMsg = mock(ByteArrayObject.class);

        decoder = new Decoder();
        when(mockMsg.getType()).thenReturn("MAP");
        when(mockMsg.getMessage()).thenReturn(new byte[]{1});

    }

    @Test
    public void ASN1DecoderTest() {
        long start = System.currentTimeMillis();
        logger.debug("mockMsg Type: " + mockMsg.getType());
        System.out.print("mockMsg");
        System.out.printf("Type: %s", mockMsg.getType());

        String res = decoder.decode(mockMsg);
        long end = System.currentTimeMillis();

        System.out.printf("res: %s", mockMsg.getType());

        String expected = "Decoded Message";


        Assert.assertEquals(expected, res);
    }
}
