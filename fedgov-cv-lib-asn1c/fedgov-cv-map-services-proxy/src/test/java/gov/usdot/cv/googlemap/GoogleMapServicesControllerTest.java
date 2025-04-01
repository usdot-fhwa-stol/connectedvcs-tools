package gov.usdot.cv.googlemap;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import gov.usdot.cv.googlemap.controllers.GoogleMapServicesController;
import gov.usdot.cv.googlemap.models.GoogleElevationResponse;
import gov.usdot.cv.googlemap.models.GoogleLocation;
import gov.usdot.cv.googlemap.models.GoogleMapProperties;
import gov.usdot.cv.googlemap.services.GoogleElevationsService;
import gov.usdot.cv.googlemap.services.GooglePlacesService;

@WebMvcTest(GoogleMapServicesController.class)
public class GoogleMapServicesControllerTest {
    @MockBean
    private GoogleElevationsService elevationService;

    @MockBean
    private GooglePlacesService placeService;

    @MockBean
    private GoogleMapProperties googleMapProp;

    @Autowired
    private MockMvc mockMvc;
    
    @BeforeEach
    void setUp(){
        when(googleMapProp.get_api_key()).thenReturn("fake-api-key");
    }

    @Test
    void getElevationShouldReturnMessageFromService(){
        GoogleElevationResponse mockedResponse = new GoogleElevationResponse(148, new GoogleLocation(38, -47), 10);
        when(elevationService.getElevation("38", "-47","fake-api-key")).thenReturn(mockedResponse);
        try {
            this.mockMvc.perform(get("/googlemap/api/elevation/38/-47"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string("{\"elevation\":148.0,\"location\":{\"lat\":38.0,\"lng\":-47.0},\"resolution\":10.0}"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    void getElevationShouldReturnNoContent(){
        when(elevationService.getElevation("30", "-40","fake-api-key")).thenReturn(null);
        try {
            this.mockMvc.perform(get("/googlemap/api/elevation/30/-40"))
            .andExpect(status().is2xxSuccessful());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
