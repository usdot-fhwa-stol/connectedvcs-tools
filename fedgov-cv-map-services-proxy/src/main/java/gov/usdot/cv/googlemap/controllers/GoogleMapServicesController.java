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
package gov.usdot.cv.googlemap.controllers;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.usdot.cv.googlemap.models.GoogleElevationResponse;
import gov.usdot.cv.googlemap.models.GoogleMapProperties;
import gov.usdot.cv.googlemap.models.GooglePlaceSuggestions;
import gov.usdot.cv.googlemap.models.GoogleSearchTextResponse;
import gov.usdot.cv.googlemap.services.GoogleElevationsService;
import gov.usdot.cv.googlemap.services.GooglePlacesService;

@RestController
@CrossOrigin(origins = "${allowed.origins}")
@RequestMapping("/googlemap/api")
public class GoogleMapServicesController {
    @Autowired
    GoogleElevationsService elevationsService;

    @Autowired
    GooglePlacesService placesService;

    @Autowired
    GoogleMapProperties googleMapProp;

    private Logger logger = LogManager.getLogger(GoogleMapServicesController.class);

    @GetMapping("/elevation/{latitude}/{longitude}")
    public ResponseEntity<GoogleElevationResponse> getElevation(@PathVariable String latitude, @PathVariable String longitude){
        GoogleElevationResponse response = elevationsService.getElevation(latitude, longitude, googleMapProp.get_api_key());
        if(response==null){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @PostMapping("/places/autocomplete")
    public ResponseEntity<?> autoCompletePlaces(@RequestBody Map<String, String> requestMap){
        String searchPlace = requestMap.get("inputText");
        if(searchPlace==null){
            String errString = "Bad request. 'inputText' field is required in request body";
            logger.error(errString);
            return new ResponseEntity<>(errString,HttpStatus.BAD_REQUEST);
        }
        GooglePlaceSuggestions response = placesService.requestAutoComplete(searchPlace, googleMapProp.get_api_key());
        if(response==null){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @PostMapping("/places/searchText")
    public ResponseEntity<?> searchPlaces(@RequestBody Map<String, String> requestMap){
        String searchPlace = requestMap.get("inputText");
        if(searchPlace==null){
            String errString = "Bad request. 'inputText' field is required in request body";
            logger.error(errString);
            return new ResponseEntity<>(errString,HttpStatus.BAD_REQUEST);
        }
        GoogleSearchTextResponse response = placesService.requestSearchText(searchPlace, googleMapProp.get_api_key());
        if(response==null){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }
}
