package gov.usdot.cv.googlemap;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import gov.usdot.cv.googlemap.models.GoogleElevationResponse;
import gov.usdot.cv.googlemap.services.GoogleElevationsService;
import okhttp3.OkHttpClient;

@SpringBootTest
public class GoogleElevationsServiceTest {
    @Autowired
    GoogleElevationsService service;

    @MockBean
    private OkHttpClient client;
    
    @Test
    void composeFullURLReturn(){
        String url = service.composeFullURL("30", "-40", "fake-key");
        String expectedUrl = "https://maps.googleapis.com/maps/api/elevation/json?locations=30,-40&key=fake-key";
        assertEquals(expectedUrl, url);
    }
    
    @Test
    void getElevationShouldReturnResponse(){
        GoogleElevationResponse data = service.getElevation("30", "-40", "fake-key");
        assertEquals(null, data);
    }

    @Test
    void parseElevationResponse(){
        String response ="{\"results\":[{\"elevation\":1608.637939453125,\"location\":{\"lat\":39.7391536,\"lng\":-104.9847034},\"resolution\":4.771975994110107}],\"status\":\"OK\"}";
        GoogleElevationResponse data =  service.parseElevationResponse(response);
        assertEquals(1608.637939453125, data.getElevation());
    }

    @Test
    void parseElevationNullResponse(){
        String response =null;
        GoogleElevationResponse data =  service.parseElevationResponse(response);
        assertEquals(null, data);
    }

    @Test
    void parseElevationInvalidResponse(){
        String response = "{\"results\": 0}";
        GoogleElevationResponse data =  service.parseElevationResponse(response);
        assertEquals(null, data);
    }

    @Test
    void parseElevationInvalidResponseParsing(){
        String response = "{\"results\":[{\"invalidJson\"}]";
        GoogleElevationResponse data =  service.parseElevationResponse(response);
        assertEquals(null, data);
    }
}
