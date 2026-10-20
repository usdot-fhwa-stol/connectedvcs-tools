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
package gov.usdot.cv.gnis.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.usdot.cv.gnis.models.GnisFeature;
import gov.usdot.cv.gnis.services.GnisLookupService;

@RestController
@RequestMapping("/gnis/api")
public class GnisController {
    @Autowired
    GnisLookupService gnisLookupService;

    @GetMapping("/lookup/{latitude}/{longitude}")
    public ResponseEntity<List<GnisFeature>> getGnisFeatures(@PathVariable double latitude, @PathVariable double longitude) {
        if (latitude < -90.0 || latitude > 90.0 || longitude < -180.0 || longitude > 180.0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<GnisFeature> response = gnisLookupService.getGnisFeatures(latitude, longitude);
        if (response == null || response.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }
}
