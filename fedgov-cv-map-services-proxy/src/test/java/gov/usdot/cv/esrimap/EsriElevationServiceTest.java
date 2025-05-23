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
package gov.usdot.cv.esrimap;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import gov.usdot.cv.esrimap.models.EsriElevationResponse;
import gov.usdot.cv.esrimap.services.EsriElevationService;
import okhttp3.OkHttpClient;

@SpringBootTest
public class EsriElevationServiceTest {
    @Autowired
    EsriElevationService service;

    @MockBean
    private OkHttpClient client;

    @Test
    void composeFullURLReturn() {
        String url = service.composeFullURL("30", "-40", "fake-key");
        String expectedUrl = "https://elevation-api.arcgis.com/arcgis/rest/services/elevation-service/v1/elevation/at-point?relativeTo=ellipsoid&lat=30&lon=-40&token=fake-key";
        assertEquals(expectedUrl, url);
    }

    @Test
    void getElevationShouldReturnResponse() {
        EsriElevationResponse data = service.getElevation("30", "-40", "fake-key");
        assertEquals(null, data);
    }
    
    @Test 
    void parseElevationResponse() {
        String response = "{\"result\":{\"point\":{\"x\":-83.050622, \"y\":42.337106, \"z\":149.0, \"spatialReference\":{\"wkid\":4326}}}}";
        EsriElevationResponse data = service.parseElevationResponse(response);
        assertEquals(149.0, data.getZ());
    }

    @Test 
    void parseElevationNullResponse(){
        String response = null;
        EsriElevationResponse data = service.parseElevationResponse(response);
        assertEquals(null, data);
    }

    @Test 
    void parseElevationInvalidResponse(){
        String response = "{\"result\": 0}";
        EsriElevationResponse data = service.parseElevationResponse(response);
        assertEquals(null, data);
    }

    @Test 
    void parseElevationInvalidResponseParsing(){
        String response = "{\"result\":{\"invalidJson\"}}";
        EsriElevationResponse data = service.parseElevationResponse(response);
        assertEquals(null, data);
    }

}
