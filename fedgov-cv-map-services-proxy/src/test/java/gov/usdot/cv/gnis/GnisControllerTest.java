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
package gov.usdot.cv.gnis;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import gov.usdot.cv.gnis.controllers.GnisController;
import gov.usdot.cv.gnis.models.GnisFeature;
import gov.usdot.cv.gnis.services.GnisLookupService;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(GnisController.class)
public class GnisControllerTest {
    @MockBean
    private GnisLookupService gnisLookupService;

    @Autowired
    private MockMvc mockMvc;

    private static final GnisFeature MARYLAND = new GnisFeature(1714934L, "Maryland", "State", "", "MD");

    @Test
    void lookupReturnsFeatures() throws Exception {
        when(gnisLookupService.getGnisFeatures(38.9847, -77.0947)).thenReturn(Arrays.asList(MARYLAND));
        mockMvc.perform(get("/gnis/api/lookup/38.9847/-77.0947"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].gnisId").value(1714934))
            .andExpect(jsonPath("$[0].state").value("MD"));
    }

    @Test
    void lookupWithNoCodesReturnsNoContent() throws Exception {
        when(gnisLookupService.getGnisFeatures(30.0, -40.0)).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/gnis/api/lookup/30/-40")).andExpect(status().isNoContent());
    }

    @Test
    void lookupFailureReturnsBadGateway() throws Exception {
        when(gnisLookupService.getGnisFeatures(38.9847, -77.0947)).thenReturn(null);
        mockMvc.perform(get("/gnis/api/lookup/38.9847/-77.0947")).andExpect(status().isBadGateway());
    }

    @Test
    void lookupRejectsOutOfRangeCoordinates() throws Exception {
        mockMvc.perform(get("/gnis/api/lookup/91/-77")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/gnis/api/lookup/38/181")).andExpect(status().isBadRequest());
        verify(gnisLookupService, never()).getGnisFeatures(anyDouble(), anyDouble());
    }

    @Test
    void searchReturnsFeatures() throws Exception {
        when(gnisLookupService.searchGnisFeatures("Maryland", "MD")).thenReturn(Arrays.asList(MARYLAND));
        mockMvc.perform(get("/gnis/api/search").param("name", " Maryland ").param("state", "MD"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Maryland"));
    }

    @Test
    void searchWithoutStateSearchesEveryState() throws Exception {
        when(gnisLookupService.searchGnisFeatures("Springfield", null)).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/gnis/api/search").param("name", "Springfield").param("state", " "))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
        verify(gnisLookupService).searchGnisFeatures(any(), isNull());
    }

    @Test
    void searchFailureReturnsBadGateway() throws Exception {
        when(gnisLookupService.searchGnisFeatures("Maryland", null)).thenReturn(null);
        mockMvc.perform(get("/gnis/api/search").param("name", "Maryland")).andExpect(status().isBadGateway());
    }

    @Test
    void searchRejectsUnsafeInput() throws Exception {
        mockMvc.perform(get("/gnis/api/search").param("name", "x%' OR 1=1 --")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/gnis/api/search").param("name", "Springfield").param("state", "M'"))
            .andExpect(status().isBadRequest());
        verify(gnisLookupService, never()).searchGnisFeatures(any(), any());
    }
}
