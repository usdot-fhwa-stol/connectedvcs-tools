package gov.usdot.cv.bingmap;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import gov.usdot.cv.bingmap.models.BingImageryMetadata;
import gov.usdot.cv.bingmap.services.BingImageryServices;
import okhttp3.OkHttpClient;

@SpringBootTest
public class BingImageryServicesTest {
    @Autowired
    BingImageryServices service;

    @MockBean
    private OkHttpClient client;
    
    @Test
    void composeFullURLReturn(){
        String url = service.composeFullURL("30", "-40", "12", "fake-key");
        String expectedUrl = "https://dev.virtualearth.net/REST/v1/Imagery/Metadata/Aerial/30,-40?uriScheme=https&zl=12&key=fake-key";
        assertEquals(expectedUrl, url);
    }
    
    @Test
    void getImageryMetadataNullResponse(){
        BingImageryMetadata data = service.getImageryMetadata("30", "-40","20", "fake-key");
        assertEquals(null, data);
    }

    @Test
    void parseImageryMetadataResponse(){
        String response = "{\"authenticationResultCode\":\"ValidCredentials\",\"brandLogoUri\":\"https://dev.virtualearth.net/Branding/logo_powered_by.png\",\"copyright\":\"Copyright © 2024 Microsoft and its suppliers. All rights reserved. This API cannot be accessed and the content and any results may not be used, reproduced or transmitted in any manner without express written permission from Microsoft Corporation.\",\"resourceSets\":[{\"estimatedTotal\":1,\"resources\":[{\"__type\":\"ImageryMetadata:http://schemas.microsoft.com/search/local/ws/rest/v1\",\"imageHeight\":256,\"imageUrl\":\"https://ecn.t1.tiles.virtualearth.net/tiles/a030223203133020211.jpeg?g=14731\",\"imageUrlSubdomains\":null,\"imageWidth\":256,\"imageryProviders\":null,\"vintageEnd\":\"04 Oct 2022 GMT\",\"vintageStart\":\"08 Sep 2022 GMT\",\"zoomMax\":18,\"zoomMin\":18}]}],\"statusCode\":200,\"statusDescription\":\"OK\",\"traceId\":\"6483e3ddf049409e2b7204a11f69c140\"}";
        BingImageryMetadata data =  service.parseImageryMetadataResponse(response);
        assertEquals("08 Sep 2022 GMT", data.getVintageStart());
    }

    @Test
    void parseImageryMetadataInvalidResponse(){
        String response = "{\"InvalidresourceSets\":[{\"estimatedTotal\":1,\"resources\":[{\"__type\":\"ImageryMetadata:http://schemas.microsoft.com/search/local/ws/rest/v1\",\"imageHeight\":256,\"imageUrl\":\"https://ecn.t1.tiles.virtualearth.net/tiles/a030223203133020211.jpeg?g=14731\",\"imageUrlSubdomains\":null,\"imageWidth\":256,\"imageryProviders\":null,\"vintageEnd\":\"04 Oct 2022 GMT\",\"vintageStart\":\"08 Sep 2022 GMT\",\"zoomMax\":18,\"zoomMin\":18}]}],\"statusCode\":200,\"statusDescription\":\"OK\",\"traceId\":\"6483e3ddf049409e2b7204a11f69c140\"}";
        BingImageryMetadata data =  service.parseImageryMetadataResponse(response);
        assertEquals(null, data);
    }

    @Test
    void parseImageryMetadataNullResponse(){
        String response =null;
        BingImageryMetadata data =  service.parseImageryMetadataResponse(response);
        assertEquals(null, data);
    }

    @Test
    void parseImageryMetadataInvalidResponseParsing(){
        String response = "{\"resourceSets\":[{\"estimatedTotal\":1,\"resources\":[{\"invalid\"}]";
        BingImageryMetadata data =  service.parseImageryMetadataResponse(response);
        assertEquals(null, data);
    }

    @Test
    void parseImageryMetadataInvalidResponseMapping(){
        String response = "{\"resourceSets\":[{\"estimatedTotal\":1,\"resources\":[{\"invalid\":12}]";
        BingImageryMetadata data =  service.parseImageryMetadataResponse(response);
        assertEquals(null, data);
    }
}
