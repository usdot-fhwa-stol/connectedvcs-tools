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
        byte[] bsm = {0,18,-127,-99,56,2,48,0,-80,20,40,9,78,-37,-101,-45,57,102,-121,-86,25,106,2,-36,8,62,-32,56,-128,8,-112,0,0,1,65,-72,-70,-96,79,-113,-12,2,126,79,-11,83,-29,-11,16,20,20,55,-29,-31,40,80,-85,-73,-23,-100,127,120,10,10,13,113,-51,-63,-65,-8,-40,57,82,-72,3,-29,-121,1,-57,53,100,-128,68,-128,0,0,6,0,114,-72,-123,3,-99,10,40,0,-17,113,64,-25,10,46,3,-103,-98,2,-126,-123,3,-104,102,40,28,-61,48,-57,46,-38,-103,-57,-65,0,-96,-96,-34,61,-122,15,-1,-31,14,-86,-82,1,-61,-112,112,58,-87,-43,-110,0,-64,-112,10,4,-128,112,33,0,51,32,0,0,0,84,92,-57,-58,4,-5,8,108,83,-99,103,92,95,-96,19,-20,72,8,-120,0,0,0,10,-114,-120,69,-64,-97,97,12,-38,-91,44,-11,76,54,2,125,-119,0,32,-120,5,4,64,56,32,-128,42,-112,0,0,0,43,114,33,-46,0,-97,97,-64,88,-1,-29,125,-15,-39,9,1,-103,0,0,0,1,92,60,-113,20,4,-5,13,-3,71,-45,92,0,14,-12,4,-5,18,0,65,-112,6,12,-128,112,97,0,119,32,0,0,2,-59,-88,-117,122,29,-53,-53,-61,105,53,88,101,54,116,12,66,-50,-127,123,-8,-56,13,71,-98,22,27,-110,2,-81,-16,-30,81,-34,-96,2,-126,-126,-94,48,48,15,127,-64,42,-1,91,18,4,66,0,0,0,16,31,-125,6,-61,-93,51,72,108,30,50,12,111,-44,9,125,89,32,32,41,-108,55,-31,57,74,77,-8,114,-98,-95,-88,-4,2,2,-104,80,-106,51,126,40,61,-123,55,-30,88,22,-85,8,10,106,55,-30,-51,-8,-61,126,20,-33,-119,78,91,4,32,40,20,-75,-87,70,27,-76,40,40,20,-61,-80,82,14,-49,-108,32,40,32,-5,-124,-128,16,-124,1,-124,32,20,32,4,44,9,42,-122,72,-122,-9,13,1,1,5,0};


        when(mockMsg.getMessage()).thenReturn(bsm);

    }

    @Test
    public void ASN1DecoderTest() {
        long start = System.currentTimeMillis();
        logger.debug("mockMsg Type: " + mockMsg.getType());
        System.out.print("mockMsg");
        System.out.printf("Type: %s\n", mockMsg.getType());

        String res = decoder.decode(mockMsg);
        long end = System.currentTimeMillis();

        System.out.printf("res: %s", mockMsg.getType());

        String expected = "Decoded Message";


        Assert.assertEquals(expected, res);
    }
}
