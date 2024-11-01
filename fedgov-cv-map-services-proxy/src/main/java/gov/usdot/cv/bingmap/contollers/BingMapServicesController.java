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
import gov.usdot.cv.bingmap.services.BingImageryServices;


@RestController
@RequestMapping("/bingmap/api")
public class BingMapServicesController {
    @Autowired
    private BingImageryServices imageryService;

    @GetMapping("/imagery/metadata/{latitude}/{longitude}/{zoomLevel}/{session_key}")
    ResponseEntity<BingImageryMetadata> getImageryMetadata( @PathVariable String latitude, @PathVariable String longitude, @PathVariable String zoomLevel , @PathVariable String session_key){
        BingImageryMetadata mdata= imageryService.getImageryMetadata(latitude, longitude, zoomLevel,session_key);
        if(mdata==null){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return  new ResponseEntity<>(mdata,headers,HttpStatus.OK);
    }
}