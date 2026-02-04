/*
 * Copyright (C) 2025 LEIDOS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package gov.usdot.cv.asn1decoder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.xml.bind.DatatypeConverter;

import gov.usdot.cv.libasn1decoder.DecodedResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ASN1DecoderTest {

    private static final Logger logger = LogManager.getLogger(ASN1DecoderTest.class);

    private Decoder decoder;

    private ByteArrayObject bsmMsg1;
    private ByteArrayObject bsmMsg2;
    private ByteArrayObject spatMsg1;
    private ByteArrayObject spatMsg2;
    private ByteArrayObject mapMsg1;
    private ByteArrayObject mapMsg2;
    private ByteArrayObject psmMsg1;
    private ByteArrayObject psmMsg2;
    private ByteArrayObject travelerInfoMsg1;
    private ByteArrayObject travelerInfoMsg2;
    private ByteArrayObject onlyTIM;
    private ByteArrayObject emptyMsg;
    private ByteArrayObject onlybsmMsg;
    private ByteArrayObject onlypsmMsg;
    private ByteArrayObject onlymapMsg;
    private ByteArrayObject onlyspatMsg;

    

    @Before
    public void setup() {

        decoder = new Decoder();

        // BSM
        bsmMsg1 = mock(ByteArrayObject.class);
        when(bsmMsg1.getType()).thenReturn("BasicSafetyMessage");
        when(bsmMsg1.getMessage()).thenReturn(
                DatatypeConverter.parseHexBinary(
                        "001425067c0eb5842562e66e8a2b9ea6c96408b97fffffff900027d9637d07d0007fff8000640fa0"
                )
        );

        bsmMsg2 = mock(ByteArrayObject.class);
        when(bsmMsg2.getType()).thenReturn("BasicSafetyMessage");
        when(bsmMsg2.getMessage()).thenReturn(
                DatatypeConverter.parseHexBinary(
                        "00145F45A6EEC002ADC4266E9C501EA6E42588CC0404000020A96DCC197966D600780405404F89D000E0C0A101653FFE100000E410A4AC1241000073810BCBC0EF0FEE08A010EFB3E83EFE00D3C11331BB96EFDC11D81182737EACFE417F07ED7510"
                )
        );

        // SPAT
        spatMsg1 = mock(ByteArrayObject.class);
        when(spatMsg1.getType()).thenReturn("SPaT");
        when(spatMsg1.getMessage()).thenReturn(
                DatatypeConverter.parseHexBinary(
                        "00131900100b5a81000021a6100007047f8000001400140014780000"
                )
        );

        spatMsg2 = mock(ByteArrayObject.class);
        when(spatMsg2.getType()).thenReturn("SPaT");
        when(spatMsg2.getMessage()).thenReturn(
                DatatypeConverter.parseHexBinary(
                        "00136400382E4EEE997973CB8FA69DFB800020402015528407742C00410C0753800408683AAE3AAE01604301D4E00182180EA7001010D0755C755C03008603A9C00504301D4E003021A0EAB8EAB806810C0753800E08603A9C00804341D571D5700E02180EA700"
                )
        );

        // MAP
        mapMsg1 = mock(ByteArrayObject.class);
        when(mapMsg1.getType()).thenReturn("MapData");
        when(mapMsg1.getMessage()).thenReturn(
                DatatypeConverter.parseHexBinary(
                        "0012815338033020204bda0d4cdcf8143d4dc48811860224164802280008002297d4bc80a0a0a9825825923a90b2f2e418986f41b7006480602403812020084015480010004521d9f001414160c7c42a1879858619502a42a060e927100662000400105be6bf41c8aded5816ebc050507dcb860ec57aead5079e02828900890001000417223a50728b750f9c6ea9e8ae480a0a0f68746ad447c002828900a0880704404020803b9000200062b68d5305d1f9269a725027d8352f72867d6c82403340004000c53f5b761abbb7d35d3c0813ec1a3baac16bfc048050240301202008402208001000310fe55f849acd608d8ace136b440000dfe4808880008002086365c0017d1612eb34026067404895390907bd848050440302201c100024000200000090026180a0a0f2852600140001000000169fc1585bd1da000b00008000000a3bb2f439459a80060000400000046d55c416c67f40"
                )
        );

        mapMsg2 = mock(ByteArrayObject.class);
        when(mapMsg2.getType()).thenReturn("MapData");
        when(mapMsg2.getMessage()).thenReturn(
                DatatypeConverter.parseHexBinary(
                        "0012829138023000201428094edb9bd3396687aa196a02dc0e200224000800518e59a274276dcf1b98e59a262e76dd28a18e59a25a676dd61f38e59a249276dd91b814146396688df9db7735a6396689479db77db4e39668a5a9db784aa050518e59a306a76de24318e59a3a1a76de34198e59a456676de3e718e59a4e8e76de3f418e59a561676de3cd920112000200186396685c89db7393c6396685eb9db744fa6396685eb9db7518463966860d9db75c76e396686309db76dc6050518e59a194a76ddd5598e59a19d276ddf3998e59a1bfa76de0c4b8e59a204a76de21c81414639668a5b9db78d6a639668cc79db790cc6396690029db793646396692909db793966396695cb9db792fe48030240281201c08400cc8001000171cb340bd4edb981f027d8c72ccf3a93b6e5551c72ccdeb53b6e46c009f624044400040005c72cd04bd3b6e566809f631cb33d4ecedb932971cb33840cedb8fc0027d890020880504403820802a900020002e396685909db727f204fb18e59a16ae76dc66218e59a156e76dc28292033200020002e396688d09db7286a04fb18e59a22d676dc66c38e59a22d676dc296013ec48010640183201c18401dc8001000b31cb34706cedb99c931cb348c7cedb9ca731cb349c6cedb9f0e31cb34a2b4edba14331cb34a53cedba37931cb34a3f4edba57331cb34a09cedba71331cb349764edba92131cb348b3cedbab0771cb347a0cedbad6402828c72cd1af33b6ebcd8c72cd186f3b6ec3d4c72cd15653b6ecd98902210001000831cb3461bcedb9beb31cb347a7cedb9e1631cb348924edba01031cb348d54edba26d31cb348c7cedba47b31cb348924edba63931cb347f84edba8a031cb34706cedbab1b71cb345eccedbadbd02828c72cd15153b6ebdc8900210803084028400"
                )
        );

        // PSM
        psmMsg1 = mock(ByteArrayObject.class);
        when(psmMsg1.getType()).thenReturn("PersonalSafetyMessage");
        when(psmMsg1.getMessage()).thenReturn(
                DatatypeConverter.parseHexBinary(
                        "00201c000002a5158048d159e14cdd338f3d4da420101effffffff00000000"
                )
        );

        psmMsg2 = mock(ByteArrayObject.class);
        when(psmMsg2.getType()).thenReturn("PersonalSafetyMessage");
        when(psmMsg2.getMessage()).thenReturn(
                DatatypeConverter.parseHexBinary(
                        "00201c000002a5157c48d159e14cdd338f3d4da420101effffffff00000000"
                )
        );

        // TIM
        travelerInfoMsg1 = mock(ByteArrayObject.class);
        when(travelerInfoMsg1.getType()).thenReturn("TravelerInformationMessage");
        when(travelerInfoMsg1.getMessage()).thenReturn(
                DatatypeConverter.parseHexBinary(
                        "001f4f20100000000000f87ac3f0803299ba81d27a9caece2474002e2fd2f134b68030006e5337503a4f5395d9c48e80b74005c004a3d3e0a028296dac37a813ecbe01cae009f65bc7b0e204fb000403e6a0"
                )
        );

        travelerInfoMsg2 = mock(ByteArrayObject.class);
        when(travelerInfoMsg2.getType()).thenReturn("TravelerInformationMessage");
        when(travelerInfoMsg2.getMessage()).thenReturn(
                DatatypeConverter.parseHexBinary(
                        "001f482010000000000114d3bc20807299b9efbc7a9b8e02260400060fd2eea2b0e008006e53373df78f5371c044c080b74000c004a51fc12021c04da26084c7e5da414307450000002838"
                )
        );
        onlyTIM = mock(ByteArrayObject.class);
        when(onlyTIM.getType()).thenReturn("TravelerInformationMessage");
        when(onlyTIM.getMessage()).thenReturn(
                DatatypeConverter.parseHexBinary(
                        "20100000000001c2defa8180b29dc1fafc73929a323749c00e6fd400fe1f4020007e53b83f5f8e72534646e900b72e00700298e7252be677080c898e7253da67707d4218e7253f227707cf438e7254b167707a7b813ec639c954df9dc1e300639c957bd9dc1da2e639c9594f9dc1d55800100f9b80849800e004004400900980"
                )
        );
        
        onlybsmMsg = mock(ByteArrayObject.class);
        when(onlybsmMsg.getType()).thenReturn("BasicSafetyMessage");
        when(onlybsmMsg.getMessage()).thenReturn(
                DatatypeConverter.parseHexBinary(
                        "45A6EEC002ADC4266E9C501EA6E42588CC0404000020A96DCC197966D600780405404F89D000E0C0A101653FFE100000E410A4AC1241000073810BCBC0EF0FEE08A010EFB3E83EFE00D3C11331BB96EFDC11D81182737EACFE417F07ED7510"
                )
        );
        onlypsmMsg = mock(ByteArrayObject.class);
        when(onlypsmMsg.getType()).thenReturn("PersonalSafetyMessage");
        when(onlypsmMsg.getMessage()).thenReturn(
                DatatypeConverter.parseHexBinary(
                        "000002a5158048d159e14cdd338f3d4da420101effffffff00000000"
                )
        );

        onlyspatMsg = mock(ByteArrayObject.class);
        when(onlyspatMsg.getType()).thenReturn("SPaT");
        when(onlyspatMsg.getMessage()).thenReturn(
                DatatypeConverter.parseHexBinary(
                        "00100b5a81000021a6100007047f8000001400140014780000"
                )
        );

        onlymapMsg = mock(ByteArrayObject.class);
        when(onlymapMsg.getType()).thenReturn("MapData");
        when(onlymapMsg.getMessage()).thenReturn(
                DatatypeConverter.parseHexBinary(
                        "38033020204bda0d4cdcf8143d4dc48811860224164802280008002297d4bc80a0a0a9825825923a90b2f2e418986f41b7006480602403812020084015480010004521d9f001414160c7c42a1879858619502a42a060e927100662000400105be6bf41c8aded5816ebc050507dcb860ec57aead5079e02828900890001000417223a50728b750f9c6ea9e8ae480a0a0f68746ad447c002828900a0880704404020803b9000200062b68d5305d1f9269a725027d8352f72867d6c82403340004000c53f5b761abbb7d35d3c0813ec1a3baac16bfc048050240301202008402208001000310fe55f849acd608d8ace136b440000dfe4808880008002086365c0017d1612eb34026067404895390907bd848050440302201c100024000200000090026180a0a0f2852600140001000000169fc1585bd1da000b00008000000a3bb2f439459a80060000400000046d55c416c67f40"
                )
        );

        // Empty Message
        emptyMsg = mock(ByteArrayObject.class);
        when(emptyMsg.getMessage()).thenReturn(new byte[]{}); 
    }

    @Test
    public void testDecodeBsm() {
        DecodedResult r1 = decoder.decode(bsmMsg1,"MessageFrame");
        Assert.assertTrue(r1.success);
        Assert.assertFalse(r1.decodedMessage.isEmpty());
        Assert.assertEquals(
            "Expected decoded message type to be 'BasicSafetyMessage'",
            "BasicSafetyMessage",
            r1.messageType
        );

        DecodedResult r2 = decoder.decode(bsmMsg2,"MessageFrame");
        Assert.assertTrue(r2.success);
        Assert.assertFalse(r2.decodedMessage.isEmpty());
        Assert.assertEquals(
            "Expected decoded message type to be 'BasicSafetyMessage'",
            "BasicSafetyMessage",
            r2.messageType
        );
    }

    @Test
    public void testDecodePsm() {
        DecodedResult r1 = decoder.decode(psmMsg1,"MessageFrame");
        Assert.assertTrue(r1.success);
        Assert.assertFalse(r1.decodedMessage.isEmpty());
         Assert.assertEquals(
            "Expected decoded message type to be 'PersonalSafetyMessage'",
            "PersonalSafetyMessage",
            r1.messageType
        );

        DecodedResult r2 = decoder.decode(psmMsg2,"MessageFrame");
        Assert.assertTrue(r2.success);
        Assert.assertFalse(r2.decodedMessage.isEmpty());
         Assert.assertEquals(
            "Expected decoded message type to be 'PersonalSafetyMessage'",
            "PersonalSafetyMessage",
            r2.messageType
        );
    }

    @Test
    public void testDecodeSpat() {
        DecodedResult r1 = decoder.decode(spatMsg1,"MessageFrame");
        Assert.assertTrue(r1.success);
        Assert.assertFalse(r1.decodedMessage.isEmpty());
        Assert.assertEquals(
            "Expected decoded message type to be 'SPAT'",
            "SPaT",
            r1.messageType
        );

        DecodedResult r2 = decoder.decode(spatMsg2,"MessageFrame");
        Assert.assertTrue(r2.success);
        Assert.assertFalse(r2.decodedMessage.isEmpty());
        Assert.assertEquals(
            "Expected decoded message type to be 'SPAT'",
            "SPaT",
            r2.messageType
        );
    }

    @Test
    public void testDecodeMapData() {
        DecodedResult r1 = decoder.decode(mapMsg1,"MessageFrame");
        Assert.assertTrue(r1.success);
        Assert.assertFalse(r1.decodedMessage.isEmpty());
        Assert.assertEquals(
            "Expected decoded message type to be 'MapData'",
            "MapData",
            r1.messageType
        );

        DecodedResult r2 = decoder.decode(mapMsg2,"MessageFrame");
        Assert.assertTrue(r2.success);
        Assert.assertFalse(r2.decodedMessage.isEmpty());
        Assert.assertEquals(
            "Expected decoded message type to be 'MapData'",
            "MapData",
            r2.messageType
        );
    }

    @Test
    public void testDecodeTravelerInformation() {
        DecodedResult r1 = decoder.decode(travelerInfoMsg1,"MessageFrame");
        Assert.assertTrue(r1.success);
        Assert.assertFalse(r1.decodedMessage.isEmpty());
         Assert.assertEquals(
            "Expected decoded message type to be 'TravelerInformationMessage'",
            "TravelerInformationMessage",
            r1.messageType
        );

        DecodedResult r2 = decoder.decode(travelerInfoMsg2,"MessageFrame");
        Assert.assertTrue(r2.success);
        Assert.assertFalse(r2.decodedMessage.isEmpty());
         Assert.assertEquals(
            "Expected decoded message type to be 'TravelerInformationMessage'",
            "TravelerInformationMessage",
            r2.messageType
        );
    }
    @Test
    public void ASN1DecoderTestEmpty() {
        DecodedResult decodedMessage = decoder.decode(emptyMsg,"empty");
        Assert.assertFalse("Decoding result should be False", decodedMessage.success);
    }
    @Test
    public void testDecodeOnlyTIM() {       
        
                DecodedResult r = decoder.decode( onlyTIM, "TIM");
                Assert.assertTrue(r.success);
               Assert.assertFalse(r.decodedMessage.isEmpty());
                 Assert.assertEquals(
                        "Expected decoded message type to be 'TravelerInformationMessage'",
                        "TravelerInformationMessage",
                        r.messageType
                );
                System.out.println("Decoded TIM Message: " + r.decodedMessage);
        }

      @Test
      public void testDecodeOnlyBSM() {       
        
                DecodedResult r = decoder.decode( onlybsmMsg, "BSM");
                Assert.assertTrue(r.success);
               Assert.assertFalse(r.decodedMessage.isEmpty());
                 Assert.assertEquals(
                        "Expected decoded message type to be 'BasicSafetyMessage'",
                        "BasicSafetyMessage",
                        r.messageType
                );
               
        }
        @Test
        public void testDecodeOnlyPSM() {       
        
                DecodedResult r = decoder.decode( onlypsmMsg, "PSM");
                Assert.assertTrue(r.success);
               Assert.assertFalse(r.decodedMessage.isEmpty());
                 Assert.assertEquals(
                        "Expected decoded message type to be 'PersonalSafetyMessage'",
                        "PersonalSafetyMessage",
                        r.messageType
                );    
        }
        @Test  
        public void testDecodeOnlySPAT() {       
        
                DecodedResult r = decoder.decode( onlyspatMsg, "SPAT");
                Assert.assertTrue(r.success);
               Assert.assertFalse(r.decodedMessage.isEmpty());
                 Assert.assertEquals(
                        "Expected decoded message type to be 'SPaT'",
                        "SPaT",
                        r.messageType
                );    
        }
        @Test  
        public void testDecodeOnlyMAP() {    
        
                DecodedResult r = decoder.decode( onlymapMsg, "MAP");
                Assert.assertTrue(r.success);
               Assert.assertFalse(r.decodedMessage.isEmpty());
                 Assert.assertEquals(
                        "Expected decoded message type to be 'MapData'",
                        "MapData",
                        r.messageType
                );    
        }  

}
