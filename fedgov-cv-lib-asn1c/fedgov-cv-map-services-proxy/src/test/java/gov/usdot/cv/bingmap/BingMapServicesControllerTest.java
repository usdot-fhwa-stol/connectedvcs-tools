package gov.usdot.cv.bingmap;

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

import gov.usdot.cv.bingmap.contollers.BingMapServicesController;
import gov.usdot.cv.bingmap.models.BingImageryMetadata;
import gov.usdot.cv.bingmap.models.BingMapProperties;
import gov.usdot.cv.bingmap.services.BingImageryServices;

@WebMvcTest(BingMapServicesController.class)
public class BingMapServicesControllerTest {
    @MockBean
    private BingImageryServices imageryService;

    @MockBean
    private BingMapProperties property;

    @Autowired
    private MockMvc mockMvc;
    
    @BeforeEach
    void setUp(){
        when(property.get_api_key()).thenReturn("fake-api-key");
    }
    
    @Test
    void getImageryMetadataShouldReturnMessageFromService(){
        BingImageryMetadata mockedResponse = new BingImageryMetadata("url","com",200,100,"04 Oct 2022 GMT","04 Sep 2022 GMT",20,20);
        when(imageryService.getImageryMetadata("38", "-47","20","fake-api-key")).thenReturn(mockedResponse);
        try {
            this.mockMvc.perform(get("/bingmap/api/imagery/metadata/38/-47/20"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string("{\"imageUrl\":\"url\",\"imageUrlSubdomains\":\"com\",\"imageWidth\":200,\"imageHeight\":100,\"vintageStart\":\"04 Oct 2022 GMT\",\"vintageEnd\":\"04 Sep 2022 GMT\",\"zoomMin\":20,\"zoomMax\":20}"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    void getImageryMetadataShouldReturnNoContent(){
        when(imageryService.getImageryMetadata("38", "-47","20","fake-api-key")).thenReturn(null);
        try {
            this.mockMvc.perform(get("/bingmap/api/imagery/metadata/38/-47/20"))
            .andExpect(status().is2xxSuccessful());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
