package gov.usdot.cv.msg.builder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import gov.usdot.cv.msg.builder.message.IntersectionMessage;
public class IntersectionSituationDataBuilderTest {

    // Encoder encoder;
    IntersectionSituationDataBuilder builder;
    IntersectionMessage res;

    @Before
    public void setup() {
        builder = new IntersectionSituationDataBuilder();
    }

    @Test
    public void MapMessageWithFramePlusMapTypeTest() {
        String filePath = "src/test/resources/samplemap.json";
        String expectedHexString = "00124638693020216407a54cdb36463d4d6bee11c202dc00c0022ccc3bb2a0622800000028f0335bb5e4a64bc05050fa872885fa2668404fb2fa292fc028297d2c938814141e8b0b80";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    @Test
    public void MapMessageWithNoMessageTypeTest() {
        String filePath = "src/test/resources/intersection2.json";
        String expectedHexString = "001242380b30602000002d4ee11f6a39c931a613e802dc025002280000010000961419f59cb0690efa8581097ce7280314001000010000961949f544b0638f39c581ba7b9d";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    @Test
    public void MapMessageWithComputedLaneTest() {
        String filePath = "src/test/resources/intersection4_with_computed_lane.json";
        String expectedHexString = "00123f38023020206072094ee104f539c9439e17d302dc023002254802928400b60c71eaac0a146c0d23e0581414d82557b2f028286008480000020840028c746380";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    @Test
    public void MapMessageWithCrossWalksTest() {
        String filePath = "src/test/resources/intersection4_with_crosswalks.json";
        String expectedHexString = "00125038023020206072094ee104f539c9439e17d302dc023002254802928400b60c71eaac0a146c0d23e0581414d82557b2f0282810080002000002d933464f902851b0348f8160505360955ecbc0a0a00008";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    @Test
    public void MapMessageWith20BitNodeOffsetEncodingTest() {
        String filePath = "src/test/resources/intersection_Automatic_20_Bit.json";
        String expectedHexString = "00122738013000204d520545b2a0403b1e49cd102802dc00300225480292840081955b80ca95008d4cf0";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    @Test
    public void MapMessageWith64BitNodeOffsetEncodingTest() {
        String filePath = "src/test/resources/intersection_Automatic_64_Bit.json";
        String expectedHexString = "00123738013000204d520545b2a0403b1e49cd102802dc00300225480292840098ec6f65d22d6e33d98ec6e8f3e2d4396d98ec6fd11e2d1fb1b8";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    @Test
    public void MapMessageWithExplicit32BitNodeOffsetEncodingTest() {
        String filePath = "src/test/resources/intersection_Explicit_32_Bit.json";
        String expectedHexString = "00122b38013000204d520545b2a0403b1e49cd102802dc0030022548029284009596e89220af8823946583bf19bd";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    @Test
    public void MapMessageWithExplicit64BitNodeOffsetEncodingTest() {
        String filePath = "src/test/resources/intersection_Explicit_64_Bit.json";
        String expectedHexString = "00123738013000204d520545b2a0403b1e49cd102802dc00300225480292840098ec6f65d22d6e33d98ec6e8f3e2d4396d98ec6fd11e2d1fb1b8";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    @Test
    public void MapMessageWithTightEncodingNodeOffsetTest() {
        String filePath = "src/test/resources/samplemap2.json";
        String expectedHexString = "00128234380030003007b4014edb6fe539666b0e194c02dc241179445e18d8022ccc3bb2a062280000020003a9f9c4a40a0a5bf90c2c04fb0a01829950fa28804fb0a2c0f0c51fe7589ac019c09f605b05102775143612800000201016c0216661dd95032240000020002552d226805050dfde6e253fd18c813ec28110464c2d2d604fb06279210c8e2e0198e5253374bb013ec0aa87a05a4458ac1a00018096064000c06b0062ccc3bb2a0666400000400002b27d7e40a0a5f1fdfc8050536020b330eeca81a220000008000aadaa806028293dee08814140fba5a0e488aa0fa24407dd19828e800161a8000e08580aa800000400002b5ae7c80a0a5f175ff405050ac26000101560c400080cb00c2ccc3bb2a06cc40000040005aa45f9000a0a140e721860b6e80439fa8237be1134d8c0505029bb44158e060b0b7104a7a5821cc0c10056c07d7352c07740000028001d4d2dc9605050e0239300bfac0007aadc035b710181ad80a55b80436d703231e8ac20800081d61b0000410301104000004000028d9eb940a0a50a2405813ecb0122ccc3bb2a07324000004000028d725200a0a50aac08813ec581548000002000108a55e542769f484ea3780158d002c190000c24b02ed0000008000011312ff08a35fe816070001014d81834cc3bb2a062cb1000000200001139a6c88a05fe8161a4001016301ba40000040006299505e00a0a54000b0813ec27fa39b0c0b22c18165ac50b65d898c042c09f60c3e3ce182e5d80de012063cf14a8431a04fb00f52b408b9ec0";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    @Test
    public void MapMessageWithConnectingLaneTest() {
        String filePath = "src/test/resources/intersection8.json";
        String expectedHexString = "0012643801304030fd40044ee1052939c943fc02dc400701c394a14f8192008a000000005831c79342c1c53cdc0240d0000490085000000002c2ad3cdc161095e318120880002200684000000015e569ef24aef6cf6e2200884000000015e461f86caed5af5ae0";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    @Test
    public void MapMessageWithDeltaWidthTest() {
        String filePath = "src/test/resources/intersection11.json";
        String expectedHexString = "0012817a38013020317c98054ee11bf839c9785f172602dc241179445e18580228000000400235fb2dec38121e6c084beec8243cd80dd7ce704879b018ef7ea090f360215ef7c121e6c00b3e318243c090d4000124021400000014b0058f7d4580f37d972c063beb596026df4a4b01e6f8c65809b7cd12c037be478243100004900c5000000062c0c73e631603cdf5acb0162fad6580b17cfd2c063be9f9602c5f4fcb010af9fa5802c7def04854000112020a01000006582d97db82c079bed696026df764b0162fad6580b17ce72484c000124149000920c40004200a4404000001615c60920b0a890632200c4400000001612aa0d98b0a890632480e680000000160f8e12c0b0dd107ea090b8000324083402000000b06390b72586fe83f50484900020804a100000000d7e72851e04b21afb57081609640805210000000057d1084162beb4c1fa8805a900000000d7c8c800b04b21af2e0f84209640806290000000057c767e102bcb83e108806a90000000057c607c162bcce3e2680";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    @Test
    public void MapMessageWithMapTypeTest() {
        String filePath = "src/test/resources/samplemap3.json";
        String expectedHexString = "38013000204db2054d7591193cb57a27118602dc0e5802280000050003a85317a80a0a18e21ac0b3333a200a0a087c7801110fd043a7018a24585d027d821de2ba802256c09f615824000100ac088000808b0221000000c0007508439d01414330b37384082d4aa13874d028282629e608ab2019466102204fb2856a22c09f619fc62b62b064000103582c0000820c02a9000001000102e319f10eaa9961c1f2881dfda00dd2ed06835f8308c9c147e0a0a6f2d04452b8802210000000e2e928b80ed29da1ce33e438826583c5bcc1bd6668bc773027d81425d00a6f4a1802220000020002050e06a2881918027d80e923006a90703048c01264ec04627600653b08265c80245a32c033400000280025531e6b804fb01e30940cf904066535026e8680e74ec062aea01a55e001caf04cdb520172cc0ac10800101560d000080c300cc40000040003a9f6f3fc0a0a0b71e1015b9c4c076f4202ff3e00b1880013bcc24eab68030b2cb01dd000000c0007535be658141416d7b392cf70181414846fbe027d805470813ef56c04056613153fc26e2850ac0a000201d60880010100";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    @Test
    public void MapMessageWithMapTypeAndExplicitNodeOffsetEncodingType() {
        String filePath = "src/test/resources/samplemap4.json";
        String expectedHexString = "38023000201428094edb9bd3396687aa196a02dc0e200224000000518e59a274276dcf1b98e59a262e76dd28a18e59a25a676dd61f38e59a249276dd91b814146396688df9db7735a6396689479db77db4e39668a5a9db784aa050518e59a306a76de24318e59a3a1a76de34198e59a456676de3e718e59a4e8e76de3f418e59a561676de3cd920112000000186396685c89db7393c6396685eb9db744fa6396685eb9db7518463966860d9db75c76e396686309db76dc6050518e59a194a76ddd5598e59a19d276ddf3998e59a1bfa76de0c4b8e59a204a76de21c81414639668a5b9db78d6a639668cc79db790cc6396690029db793646396692909db793966396695cb9db792fe48030240281201c08400cc8000000171cb340bd4edb981f027d8c72ccf3a93b6e5551c72ccdeb53b6e46c009f624044400000005c72cd04bd3b6e566809f631cb33d4ecedb932971cb33840cedb8fc0027d890020880504403820802a900000002e396685909db727f204fb18e59a16ae76dc66218e59a156e76dc28292033200000002e396688d09db7286a04fb18e59a22d676dc66c38e59a22d676dc296013ec48010640183201c18401dc8000000b31cb34706cedb99c931cb348c7cedb9ca731cb349c6cedb9f0e31cb34a2b4edba14331cb34a53cedba37931cb34a3f4edba57331cb34a09cedba71331cb349764edba92131cb348b3cedbab0771cb347a0cedbad6402828c72cd1af33b6ebcd8c72cd186f3b6ec3d4c72cd15653b6ecd98902210000000831cb3461bcedb9beb31cb347a7cedb9e1631cb348924edba01031cb348d54edba26d31cb348c7cedba47b31cb348924edba63931cb347f84edba8a031cb34706cedbab1b71cb345eccedbadbd02828c72cd15153b6ebdc8900210803084028400";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    @Test
    public void MapMessageWithConnectingLaneManeuversTest() {
        String filePath = "src/test/resources/samplemap5.json";
        String expectedHexString = "0012815e38023000201428094edb9bd3396687aa196a02dc0e200224000000506e2ea813e3fd009f93fd54f8fd4405050df8f84a142aedfa671fde0282835c73706ffe360e54ae00f8e1c071cd59201120000001801cae2140e7428a003bdc5039c28b80e66780a0a140e6198a0730cc31cbb6a671efc02828378f6183fff843aaab8070e41c0eaa75648030240281201c08400cc8000000151731f1813ec21b14e759d717e804fb12022200000002a3a2117027d84336a94b3d530d809f624008220141100e08200aa40000000adc88748027d870163ff8df7c7642406640000000570f23c5013ec37f51f4d70003bd013ec48010640183201c18401dc8000000b16a22de8772f2f0da4d56194d9d0310b3a05efe320351e78586e480abfc389477a800a0a0a88c0c03dff00abfd6c481108000000407e0c1b0e8ccd21b078c831bf5005f56480351f805816ab0a72d829461bb40a0a01d9f292004210061080508";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    @Test
    public void mapMessageWithRegionIDTest() {
        String filePath = "src/test/resources/samplemap6.json"; 
        String expectedHexString = "00128160380230002200081428094edb9bd3396687aa196a02dc0e200224000000506e2ea813e3fd009f93fd54f8fd4405050df8f84a142aedfa671fde0282835c73706ffe360e54ae00f8e1c071cd59201120000001801cae2140e7428a003bdc5039c28b80e66780a0a140e6198a0730cc31cbb6a671efc02828378f6183fff843aaab8070e41c0eaa75648030240281201c08400cc8000000151731f1813ec21b14e759d717e804fb12022200000002a3a2117027d84336a94b3d530d809f624008220141100e08200aa40000000adc88748027d870163ff8df7c7642406640000000570f23c5013ec37f51f4d70003bd013ec48010640183201c18401dc8000000b16a22de8772f2f0da4d56194d9d0310b3a05efe320351e78586e480abfc389477a800a0a0a88c0c03dff00abfd6c481108000000407e0c1b0e8ccd21b078c831bf5005f56480351f805816ab0a72d829461bb40a0a01d9f292004210061080508";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    @Test
    public void mapMessageWithNodeSpeedLimitTest() {
        String filePath = "src/test/resources/samplemap7.json"; 
        String expectedHexString = "0012819038023000301428094edb9bd3396687aa196a02dc083ee03880089000000141b8baa04f8ff4027e4ff553e3f510141437e3e12850abb7e99c7f780a0a0d71cdc1bff8d83952b803e38701c735648044800000060072b885039d0a2800ef7140e70a2e03999e028285039866281cc330c72eda99c7bf00a0a0de3d860fffe10eaaae01c390703aa9d59200c0900a04807021003320000000545cc7c604fb086c539d675c5fa013ec4808880000000a8e8845c09f610cdaa52cf54c36027d890020880504403820802a900000002b7221d2009f61c058ffe37df1d9090199000000015c3c8f1404fb0dfd47d35c000ef404fb12004190060c807061007720000002c5a88b7a1dcbcbc36935586536740c42ce817bf8c80d479e161b9202aff0e251dea0028282a230300f7fc02aff5b120442000000101f8306c3a333486c1e320c6fd4097d592020299437e1394a4df8729ea1a8fc0202985096337e283d8537e25816ab080a6a37e2cdf8c37e14df894e5b04202814b5a9461bb4282814c3b0520ecf94202820fb848010840184201420";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    @Test
    public void rgaBaseLayerMessageWithFramePlusRGATypeTest() {
        String filePath = "src/test/resources/samplerga.json";
        String expectedHexString = "00000a66d9b231ea6b5f708e13f4105024aa19221bdc340404141202060c020a0a0e060201fdfa082d080081a400";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    @Test
    public void rgaBaseLayerMessageWithFramePlusRGATypeTestRelativeRAID() {
        String filePath = "src/test/resources/samplerga2.json";
        String expectedHexString = "002b300000000a66d9b231ea6b5f708e13f3b55428040a19221bdc340404141202060c020a0a0e060201fdf9daa9600065a400";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    @Test
    public void mapMessageWithFullRAIDTest() {
        String filePath = "src/test/resources/samplemap8.json"; 
        String expectedHexString = "0012819d38023000b01428094edb9bd3396687aa196a02dc083ee03880089000000141b8baa04f8ff4027e4ff553e3f510141437e3e12850abb7e99c7f780a0a0d71cdc1bff8d83952b803e38701c735648044800000060072b885039d0a2800ef7140e70a2e03999e028285039866281cc330c72eda99c7bf00a0a0de3d860fffe10eaaae01c390703aa9d59200c0900a04807021003320000000545cc7c604fb086c539d675c5fa013ec4808880000000a8e8845c09f610cdaa52cf54c36027d890020880504403820802a900000002b7221d2009f61c058ffe37df1d9090199000000015c3c8f1404fb0dfd47d35c000ef404fb12004190060c807061007720000002c5a88b7a1dcbcbc36935586536740c42ce817bf8c80d479e161b9202aff0e251dea0028282a230300f7fc02aff5b120442000000101f8306c3a333486c1e320c6fd4097d592020299437e1394a4df8729ea1a8fc0202985096337e283d8537e25816ab080a6a37e2cdf8c37e14df894e5b04202814b5a9461bb4282814c3b0520ecf94202820fb848010840184201420042c092a864886f70d01010500";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    @Test
    public void mapMessageWithRelRAIDTest() {
        String filePath = "src/test/resources/samplemap9.json"; 
        String expectedHexString = "0012819e38023000b01428094edb9bd3396687aa196a02dc083ee03880089000000141b8baa04f8ff4027e4ff553e3f510141437e3e12850abb7e99c7f780a0a0d71cdc1bff8d83952b803e38701c735648044800000060072b885039d0a2800ef7140e70a2e03999e028285039866281cc330c72eda99c7bf00a0a0de3d860fffe10eaaae01c390703aa9d59200c0900a04807021003320000000545cc7c604fb086c539d675c5fa013ec4808880000000a8e8845c09f610cdaa52cf54c36027d890020880504403820802a900000002b7221d2009f61c058ffe37df1d9090199000000015c3c8f1404fb0dfd47d35c000ef404fb12004190060c807061007720000002c5a88b7a1dcbcbc36935586536740c42ce817bf8c80d479e161b9202aff0e251dea0028282a230300f7fc02aff5b120442000000101f8306c3a333486c1e320c6fd4097d592020299437e1394a4df8729ea1a8fc0202985096337e283d8537e25816ab080a6a37e2cdf8c37e14df894e5b04202814b5a9461bb4282814c3b0520ecf94202820fb84801084018420142004310a0102864886f70d01010500";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    @Test
    public void mapMessageNoRAIDTest() {
        String filePath = "src/test/resources/samplemap10.json"; 
        String expectedHexString = "0012819038023000301428094edb9bd3396687aa196a02dc083ee03880089000000141b8baa04f8ff4027e4ff553e3f510141437e3e12850abb7e99c7f780a0a0d71cdc1bff8d83952b803e38701c735648044800000060072b885039d0a2800ef7140e70a2e03999e028285039866281cc330c72eda99c7bf00a0a0de3d860fffe10eaaae01c390703aa9d59200c0900a04807021003320000000545cc7c604fb086c539d675c5fa013ec4808880000000a8e8845c09f610cdaa52cf54c36027d890020880504403820802a900000002b7221d2009f61c058ffe37df1d9090199000000015c3c8f1404fb0dfd47d35c000ef404fb12004190060c807061007720000002c5a88b7a1dcbcbc36935586536740c42ce817bf8c80d479e161b9202aff0e251dea0028282a230300f7fc02aff5b120442000000101f8306c3a333486c1e320c6fd4097d592020299437e1394a4df8729ea1a8fc0202985096337e283d8537e25816ab080a6a37e2cdf8c37e14df894e5b04202814b5a9461bb4282814c3b0520ecf94202820fb848010840184201420";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    @Test
    public void rgaMessageFromUI() {
        String filePath = "src/test/resources/samplerga3.json"; 
        String expectedHexString = "002b290000408a76dcde99cb343d50cb53f4571024aa19221bdc3404041406182c4203fdfa2b88280011a400";
        runIntersectionSituationDataBuilderTests(filePath, expectedHexString);
    }

    public void runIntersectionSituationDataBuilderTests(String filePath, String expectedHexString) {
        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
            res = builder.build(jsonContent);
            assertEquals(expectedHexString, res.getHexString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}