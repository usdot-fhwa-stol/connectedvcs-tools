/**
 * Created by martzth on 3/20/2015.
 * Updated 3/2017 by martzth
 */

/**
 * DEFINE GLOBAL VARIABLES
 */

let proj_name, host;
let message_json_input, message_hex_input, message_text_input;
let message_status_div;


/**
 * Define functions that must bind on load
 */

$(document).ready(function()
{

    proj_name = window.location.pathname.split( '/' )[1];
    host = window.location.host;

    message_json_input = $('#message_json');
    message_hex_input = $('#message_hex');
    message_text_input = $('#message_text');
    message_status_div = $('#message_status');


    /**
     * Purpose: checks for errors in message
     * @params: click event
     * @event: checks for errors and prevents bad send
     */

    $('#message_deposit_modal').on('show.bs.modal', function (e) {
        resetMessageForm()
        if( !errorCheck() ){
            let message = createMessageJSON();
            message_json_input.val( JSON.stringify(message, null, 2) )
        } else {
            $("#message_deposit").prop('disabled', true);
        }
    });

    $('#message_type').on("change", function(){
    	setCookie("isd_message_type", $('#message_type').val(), 365);
        resetMessageForm();
        message_json_input.val(JSON.stringify(createMessageJSON(), null, 2));
    });

    $('#node_offsets').on("change", function(){
        setCookie("isd_node_offsets", $('#node_offsets').val(), 365);
        resetMessageForm();
        message_json_input.val(JSON.stringify(createMessageJSON(), null, 2));
    });

    $('#enable_elevation').on("change", function(){
        setCookie("isd_enable_elevation", $('#enable_elevation').is(":checked"), 365);
        resetMessageForm();
        message_json_input.val(JSON.stringify(createMessageJSON(), null, 2));
    });


    /**
     * Purpose: to allow conversion of message
     * @params: message
     * @event: POSTS to server and returns a hex and text version
     */

    $('#message_deposit').click( function() {
        let message =  JSON.parse(message_json_input.val());
        let type = $("#message_type").val();
        message["message"] = type;
        message = JSON.stringify(message);

        $.ajax({
            type : "POST",
            url : "/" + proj_name + "/builder/messages/intersection",
            contentType: "text/plain",
            data : message,
            success : function(result) {
                console.log( "success: ", result );
                setMessageResult( true, result.hexString, "hex" );

                // TODO: Temporarily changing this until decoder is implemented using ASN1c
                // setMessageResult( true, result.readableString, "text" );
                setMessageResult( true, "This box is not populated in this version of the Tool", "text" );

            },
            error : function(xhr, status, error) {
                console.log( "fail: ", xhr.responseText );
                setMessageResult( false, xhr.responseText, "text" );
            }
        });
    });
});

function setMessageResult( success, message, type ){

    if( success ) {
        message_status_div.removeClass('has-error').addClass('has-success');
    }
    else {
        message_status_div.removeClass('has-success').addClass('has-error');
    }

    if(type == "hex"){
        message_hex_input.val( message );
        $('.message_size').text((message.length/2) + " bytes");
    } else {
        message_text_input.val( message );
    }
}


/**
 * Purpose: reset message form on close
 * @params: click event
 * @event: clears boxes of messages
 */

$('.close').click(function() {
    resetMessageForm();
});

function resetMessageForm() {
    $('#alert_placeholder').html("");
    $("#message_deposit").prop('disabled', false);
    message_json_input.val("");
    message_hex_input.val("");
    message_text_input.val("");
    $('.message_size').text("");
    message_status_div.removeClass('has-error has-success');
}


/**
 * Purpose: create JSON from map elements
 * @params: map layers and elements
 * @event: builds JSON message for deposit
 * Note: each variable is stored in the feature object model
 */

function createMessageJSON()
{

    let isdMessage = {};
    let minuteOfTheYear = moment().diff(moment().startOf('year'), 'minutes');

    //Feature object models
    let stopFeat = stopFeat;
    let laneFeat = lanes.getSource().getFeatures();

    //Building of nested layers
    let approachesArray = { "approach": []};
    let drivingLanesArray = {"drivingLanes":[]};
    let crosswalkLanesArray = {"crosswalkLanes":[]};
    let attributesArray = {"laneAttributes":[]};
    let nodesArray = {"laneNodes":[]};
    let spatsArray = {"spatNodes":[]};

    let approachArray = approachesArray["approach"];
    let drivingLaneArray = drivingLanesArray["drivingLanes"];
    let crosswalkLaneArray = crosswalkLanesArray["crosswalkLanes"];
    let attributeArray = attributesArray["laneAttributes"];
    let nodeArray = nodesArray["laneNodes"];
    let computedLane = "";
    let spatArray = spatsArray["spatNodes"];

    let incompleteApproaches = []
    let verified = {};
    let reference = {};
    let referenceChild = {};

    for(let b=0; b< laneFeat.length; b++){
        laneFeat[b].set('inBox', false);
    }

    for(let i=0; i< stopFeat.length; i++){
        let temp_j = 0;
        let temp_j_c = 0;
        for(let j=0; j< laneFeat.length; j++){

            let inside = stopFeat[i].getGeometry().intersectsCoordinate(lanes.getSource().getFeatures()[j].getGeometry().getFirstCoordinate());
            if (inside && laneFeat[j].get('laneType') != "Crosswalk"){
                //console.log("Stop Box: " + i + " contains lead point of feature " + j);
                laneFeat[j].set('inBox', true);

                if (!lanes.getSource().getFeatures()[j].get('computed')) {
                    for(let m=0; m< laneFeat[j].getGeometry().getCoordinates().length; m++){
                        let coord = laneFeat[j].getGeometry().getCoordinates()[m];
                        let lonlat = ol.proj.toLonLat(coord);

                        let currentSpeedLimits = [];
                        if(laneFeat[j].get('speedLimitType')) {
                            let mapSpeedLimits = laneFeat[j].get('speedLimitType');

                            for (let mapSpeedLimit of mapSpeedLimits) {
                                if (mapSpeedLimit.speedLimitType != "Speed Limit Type") {
                                    currentSpeedLimits.push(mapSpeedLimit)
                                }
                            }
                        }

                        try {
                            nodeArray[m] = {
                                "nodeNumber": m,
                                "nodeLat": lonlat[1],
                                "nodeLong": lonlat[0],
                                "nodeElev": laneFeat[j].get('elevation')[m]?.value,
                                "laneWidthDelta": laneFeat[j].get('laneWidth')[m],
                                "speedLimitType": currentSpeedLimits
                            }
                        } catch (e) {
                            nodeArray[m] = {
                                "nodeNumber": m,
                                "nodeLat": lonlat[1],
                                "nodeLong": lonlat[0],
                                "nodeElev": laneFeat[j].get('elevation')[m]?.value,
                                "laneWidthDelta": laneFeat[j].get('laneWidth')[m],
                                "speedLimitType": currentSpeedLimits
                            }
                            $("#message_deposit").prop('disabled', true);
                            $('#alert_placeholder').append('<div id="approach-alert" class="alert alert-danger alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>'+ "Node elevation empty for node " + m + " in lane " + laneFeat[j].get('laneNumber') + "." +'</span></div>');
                        }                 
                    }
                } else {
                    computedLane = {
                        "computedLaneNumber": laneFeat[j].get('computedLaneNumber'),
                        "computedLaneID": laneFeat[j].get('computedLaneID'),
                        "referenceLaneID": laneFeat[j].get('referenceLaneID'),
                        "offsetX": laneFeat[j].get('offsetX'),
                        "offsetY": laneFeat[j].get('offsetY'),
                        "rotation": laneFeat[j].get('rotation'),
                        "scaleX": laneFeat[j].get('scaleX'),
                        "scaleY": laneFeat[j].get('scaleY')
                    }
                }

                attributeArray = [];

                laneFeat[j].get('lane_attributes').forEach(attr => {
                    attributeArray.push(attr.id);
                });
                console.log(laneFeat[j].getProperties());
                drivingLaneArray[temp_j] = {
                    "laneID": laneFeat[j].get('laneNumber'),
                    "descriptiveName": laneFeat[j].get('descriptiveName'),
                    "laneType": laneFeat[j].get('laneType'),
                    "typeAttributes": laneFeat[j].get('typeAttribute'),
                    "sharedWith": laneFeat[j].get('sharedWith'),
                    "connections": laneFeat[j].get('connections'),
                    "laneManeuvers": attributeArray,
                    "isComputed": laneFeat[j].get('computed')
                };
                if(!laneFeat[j].get('computed')) {
                    drivingLaneArray[temp_j].laneNodes = nodeArray;
                } else {
                    drivingLaneArray[temp_j].computedLane = computedLane;
                }

                //since some lanes are not in the driving lane
                temp_j++;
            } else if(laneFeat[j].get('laneType') == "Crosswalk"){
                //even though not in a "box" it's still allowed to be outside as a crosswalk - still want to be able to catch vehicle lanes outside
                laneFeat[j].set('inBox', true);

                if(!laneFeat[j].get('computed')) {
                    for(let m=0; m< laneFeat[j].getGeometry().getCoordinates().length; m++){
                        let coord = laneFeat[j].getGeometry().getCoordinates()[m];
                        let lonlat = ol.proj.toLonLat(coord);
                        nodeArray[m] = {
                            "nodeNumber": m,
                            "nodeLat": lonlat[1],
                            "nodeLong": lonlat[0],
                            "nodeElev": laneFeat[j].get('elevation')[m]?.value,
                            "laneWidthDelta": laneFeat[j].get('laneWidth')[m]
                        }
                    }
                } else {
                    computedLane = {
                        "referenceLaneID": laneFeat[j].get('referenceLaneID'),
                        "offsetX": laneFeat[j].get('offsetX'),
                        "offsetY": laneFeat[j].get('offsetY'),
                        "rotation": laneFeat[j].get('rotation'),
                        "scaleX": laneFeat[j].get('scaleX'),
                        "scaleY": laneFeat[j].get('scaleY')
                    }
                }

                attributeArray = [];

                laneFeat[j].get('lane_attributes').forEach(attr => {
                    attributeArray.push(attr.id);
                });
                console.log(laneFeat[j].getProperties());
                crosswalkLaneArray[temp_j_c] = {
                    "laneID": laneFeat[j].get('laneNumber'),
                    "descriptiveName": laneFeat[j].get('descriptiveName'),
                    "laneType": laneFeat[j].get('laneType'),
                    "typeAttributes": laneFeat[j].get('typeAttribute'),
                    "sharedWith": laneFeat[j].get('sharedWith'),
                    "connections": laneFeat[j].get('connections'),
                    "laneManeuvers": attributeArray,
                    "isComputed": laneFeat[j].get('computed')
                };
                if(!laneFeat[j].get('computed')) {
                    crosswalkLaneArray[temp_j_c].laneNodes = nodeArray;
                } else {
                    crosswalkLaneArray[temp_j_c].computedLane = computedLane;
                }

                //since some lanes are not in the driving lane
                temp_j_c++;

            }
            nodeArray = [];
            computedLane = "";
        }

        approachArray[i] = {
            "approachType": stopFeat[i].get('approachType'),
            "approachID": stopFeat[i].get('approachID'),
            "descriptiveName": stopFeat[i].get('approachName'),
            "speedLimit": stopFeat[i].get('speedLimit'),
            "drivingLanes": drivingLaneArray
        };

        if (approachArray[i].approachType === undefined) {
            incompleteApproaches.push(drivingLaneArray[0].laneID);
            $("#message_deposit").prop('disabled', true);
            $('#alert_placeholder').html('<div id="approach-alert" class="alert alert-danger alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>'+ "Approach Type empty for approach associated with lane(s) " + incompleteApproaches.toString() + "." +'</span></div>');
        }

        drivingLaneArray = [];
    }

    approachArray[stopFeat.length] = {
        "approachType": "Crosswalk",
        "approachID": -1,
        "crosswalkLanes": crosswalkLaneArray
    };


    for (let a = 0; a < laneFeat.length; a++) {

        if (laneFeat[a].get('inBox') == true && laneFeat[a].get('signalGroupID') != null && laneFeat[a].get('stateConfidence') != null) {

            let obj = {
                "laneSet": laneFeat[a].get('laneNumber'),
                "spatRevision": laneFeat[a].get('spatRevision'),
                "signalGroupID": laneFeat[a].get('signalGroupID'),
                "signalPhase": laneFeat[a].get('signalPhase'),
                "startTime": laneFeat[a].get('startTime'),
                "minEndTime": laneFeat[a].get('minEndTime'),
                "maxEndTime": laneFeat[a].get('maxEndTime'),
                "likelyTime": laneFeat[a].get('likelyTime'),
                "stateConfidence": laneFeat[a].get('stateConfidence').substring(laneFeat[a].get('stateConfidence').lastIndexOf("(")+1,laneFeat[a].get('stateConfidence').lastIndexOf(")")),
                "nextTime": laneFeat[a].get('nextTime')
            };

            let k_index = -1;

            for (let k = 0; k < spatArray.length; k++) {
                if (spatArray[k].stateConfidence == obj.stateConfidence ) {
                    k_index = k;
                }
            }

            if (k_index != -1) {
                spatArray[k_index].laneSet += obj.laneSet;
            } else {
                spatArray.push(obj);
            }

        } else {
            $('#alert_placeholder').append('<div id="spat-alert" class="alert alert-warning alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>'+ "SPaT message empty for lane " + laneFeat[a].get('laneNumber') + "." +'</span></div>');
        }
    }
    errors.getSource().clear();
    

    for(let j=0; j< laneFeat.length; j++){        
        let coords = laneFeat[j].getGeometry().getFirstCoordinate();
        // Create feature for error marker
        let errorFeature = new ol.Feature({
            geometry: new ol.geom.Point(coords)
        });
        //Style the error marker
        errorFeature.setStyle(new ol.style.Style({
            image: new ol.style.Icon({
                src: 'img/error.png',
                size: [21, 25],
                anchor: [0.5, 1],
                anchorXUnits: 'fraction',
                anchorYUnits: 'fraction'
            })
        }));
        if (!laneFeat[j].get("inBox")){
            $("#message_deposit").prop('disabled', true);
            $('#alert_placeholder').html('<div class="alert alert-danger alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>'+ "Lane " + laneFeat[j].get('laneNumber') + " exists outside of an approach." +'</span></div>');
            errors.getSource().addFeature(errorFeature);
        }
        if (!laneFeat[j].get('laneNumber')) {
            // lat lon repeated otherwise the first transform if lane exists outside approach will transform coordinates
            let latlon = ol.proj.toLonLat(laneFeat[j].getGeometry().getFirstCoordinate());
            $("#message_deposit").prop('disabled', true);
            $('#alert_placeholder').html('<div class="alert alert-danger alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>'+ "Lane at " + latlon[1] + ", " + latlon[0] + " is not assigned a lane number. Check overlapping points." +'</span></div>');
            errors.getSource().addFeature(errorFeature);
        }
    }
    let vectorFeatures = vectors.getSource().getFeatures();
    for (let f = 0; f < vectorFeatures.length; f++) {
        let feature = vectorFeatures[f];
        if (feature.get('marker').name == "Reference Point Marker") {

            reference = {
                "descriptiveIntersctionName": feature.get('intersectionName'),
                "layerID": feature.get('layerID'),
                "intersectionID": feature.get('intersectionID'),
                "regionID": feature.get('regionID'),
                "msgCount": feature.get('revisionNum'),
                "masterLaneWidth": feature.get('masterLaneWidth'),
                "referenceLat": feature.getGeometry().getCoordinates()[1],
                "referenceLon": feature.getGeometry().getCoordinates()[0],
                "referenceElevation": feature.get('elevation'),
                "roadAuthorityId": feature.get('roadAuthorityId')?.split(".").map(num => parseInt(num, 10)),
                "roadAuthorityIdType": feature.get('roadAuthorityIdType'),
            };

            let data_frame_rga_base_layer_fields = {}; // Ensure to clear the data for each call
            // Only populate JSON with RGA fields when the RGA toggle is enabled
            if (rga_enabled) { // Global variable rga_enabled is defined in mapping.js
                data_frame_rga_base_layer_fields["majorVersion"] = parseInt(feature.get('majorVersion'));
                data_frame_rga_base_layer_fields["minorVersion"] = parseInt(feature.get('minorVersion'));
                data_frame_rga_base_layer_fields["contentVersion"] = parseInt(feature.get('contentVersion'));
                let date_time = parseDatetimeStr(feature.get('contentDateTime'));
                data_frame_rga_base_layer_fields["timeOfCalculation"] = date_time.date;
                data_frame_rga_base_layer_fields["contentDateTime"] = date_time.time;

                // Add mapped geometry ID to intersection geometry reference point
                reference["mappedGeomID"] = feature.get('mappedGeometryId').split(".").map(num => parseInt(num, 10));

                // Validate RGA required fields
                validateRequiredRGAFields(feature);
            }

            referenceChild = {
                "speedLimitType": feature.get('speedLimitType')
            };

            if (feature.get('intersectionName') == undefined || feature.get('intersectionName') == "") {
                $("#message_deposit").prop('disabled', true);
                $('#alert_placeholder').html('<div class="alert alert-warning alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>' + "No intersection name defined." + '</span></div>');
            }

        }

        if (feature.get('marker').name == "Verified Point Marker") {
            verified = {
                "verifiedMapLat": feature.get('LonLat').lat,
                "verifiedMapLon": feature.get('LonLat').lon,
                "verifiedMapElevation": feature.get('elevation'),
                "verifiedSurveyedLat": feature.get('verifiedLat'),
                "verifiedSurveyedLon": feature.get('verifiedLon'),
                "verifiedSurveyedElevation": feature.get('verifiedElev')
            };

        }

    }

    let spat = {
        "intersections": {
            "status": "00",
            "states": spatArray
        }
    }


    let intersectionGeometry = {
        "referencePoint": reference,
        "referencePointChild": referenceChild,
        "verifiedPoint": verified,
        "laneList": approachesArray
    }


    let mapData = {
        "minuteOfTheYear": minuteOfTheYear,
        "layerType": "intersectionData",
        ...data_frame_rga_base_layer_fields,
        "intersectionGeometry": intersectionGeometry,
        "spatData": spat
    }

    isdMessage.mapData = mapData;
    isdMessage.messageType = $("#message_type").val();
    isdMessage.nodeOffsets = $("#node_offsets").val();
    isdMessage.enableElevation = $("#enable_elevation").is(":checked");

    return isdMessage;
}

function parseDatetimeStr(datetimestring){
    let temp_datetime = datetimestring.split(/\s/);
    try{
        let temp_date = temp_datetime[0]
        let temp_time = temp_datetime[1]
        temp_date = temp_date.split(/\//)
        temp_time = temp_time.split(/\:/)
        let date_time = {
            date: {
                "day": parseInt(temp_date[0]),
                "month": parseInt(temp_date[1]),
                "year": parseInt(temp_date[2]),
            },
            time:{
                "hour": parseInt(temp_time[0]),
                "minute": parseInt(temp_time[1]),
                "second": parseInt(temp_time[2]??0),
            }
        }
        return date_time;
    }catch(e){
        console.error("Incorrect datetime format! Expected datetime format is: d/m/Y H:m:s");
        console.error(e);
    }    
}

/***
 * @brief According to J2945_A RGA definition,  majorVersion, minorVersion, mappedGeometryId, contentVersion, contentDateTime are required
 */
function validateRequiredRGAFields(feature){    
    let map_fields_descriptions= {
        "majorVersion": "RGA message no major version defined",
        "minorVersion": "RGA message no minor version defined",
        "mappedGeometryId": "RGA message no mapped geometry ID defined",
        "contentVersion": "RGA message no content version defined",
        "contentDateTime": "RGA message no content datetime defined",
    }
    for (const [key, value] of Object.entries(map_fields_descriptions)){
        if (feature.get(key) == undefined || feature.get(key) == ""){
            $("#message_deposit").prop('disabled', true);
            $('#alert_placeholder').append('<div class="alert alert-warning alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>'+ value +'</span></div>');
        }
    }
}

/**
 * Purpose: pretty terrible error check
 * @params: DOM elements
 * @event: just checking that a marker exists, etc so that the message can build appropriately
 */

function errorCheck(){

    let status = false; //false means there are no errors

    if (lanes.getSource().getFeatures().length == 0) {
        $('#alert_placeholder').html('<div class="alert alert-danger alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>' + "Cannot deposit without a region defined." + '</span></div>');
        status = true;
    }

    if (vectors.getSource().getFeatures().length < 2) {
        $('#alert_placeholder').html('<div class="alert alert-danger alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>' + "Missing anchor or verified points." + '</span></div>');
        status = true;
    }

    return status;
}
