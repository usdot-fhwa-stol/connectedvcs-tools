/*
 * Copyright (C) 2024 LEIDOS.
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
package gov.usdot.cv.esrimap.controllers;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.usdot.cv.esrimap.models.EsriElevationResponse;
import gov.usdot.cv.esrimap.models.EsriMapProperties;
import gov.usdot.cv.esrimap.services.EsriElevationService;

@RestController
@RequestMapping("/esrimap/api")
public class EsriMapServicesController {
    @Autowired
    EsriElevationService elevationsService;

    @Autowired
    EsriMapProperties esriMapProp;

    @GetMapping("/elevation/{latitude}/{longitude}")
    public ResponseEntity<EsriElevationResponse> getElevation(@PathVariable String latitude, @PathVariable String longitude){
        EsriElevationResponse response = elevationsService.getElevation(latitude, longitude, esriMapProp.get_api_key());
        if(response==null){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }
}