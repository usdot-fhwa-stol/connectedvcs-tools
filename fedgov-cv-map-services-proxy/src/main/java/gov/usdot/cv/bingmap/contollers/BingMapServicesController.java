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
package gov.usdot.cv.bingmap.contollers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.usdot.cv.bingmap.models.BingImageryMetadata;
import gov.usdot.cv.bingmap.models.BingMapProperties;
import gov.usdot.cv.bingmap.services.BingImageryServices;


@RestController
@RequestMapping("/bingmap/api")
public class BingMapServicesController {
    @Autowired
    private BingImageryServices imageryService;

    @Autowired
    private BingMapProperties property;

    @GetMapping("/imagery/metadata/{latitude}/{longitude}/{zoomLevel}")
    ResponseEntity<BingImageryMetadata> getImageryMetadata( @PathVariable String latitude, @PathVariable String longitude, @PathVariable String zoomLevel){
        BingImageryMetadata mdata= imageryService.getImageryMetadata(latitude, longitude, zoomLevel,property.get_api_key());
        if(mdata==null){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return  new ResponseEntity<>(mdata,headers,HttpStatus.OK);
    }
}
