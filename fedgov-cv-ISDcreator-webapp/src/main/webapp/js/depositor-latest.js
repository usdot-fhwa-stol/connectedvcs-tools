/**
 * TODO: This temporary file is used to test the message deposit functionality.
 * It will be merged with the original file once the full integration testing is complete.
 */

import { errorMarkerStyle } from "./style.js";
import {lanes, box, vectors, errors, rgaEnabled} from "./map.js";

/**
 * DEFINE GLOBAL VARIABLES
 */

let proj_name, host;
let message_json_input, message_hex_input, message_text_input;
let message_status_div;
const RGASpeedLimitTypes = ["Passenger Vehicles Max Speed", "Passenger Vehicles Min Speed"];


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
            let messageTypeEl = document.getElementById("message_type");
            disableOrEnableExplicitRGA(messageTypeEl.value);
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

    removeExplicitRGA();

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

/**
 * Sets the result message and updates the UI based on the success status and message type.
 *
 * @param {boolean} success - Indicates whether the operation was successful.
 * @param {string} message - The message to be displayed.
 * @param {string} type - The type of the message, either "hex" or other.
 */
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

/**
 * Resets the message form by clearing all input fields, enabling the deposit button,
 * and removing any status indicators.
 */
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
    let stopFeat = box.getSource().getFeatures();
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
    let rgaBaseLayerFields = {};

    for(let b=0; b< laneFeat.length; b++){
        laneFeat[b].set('inBox', false);
    }

    for(let i=0; i< stopFeat.length; i++){
        let tempJ = 0;
        let tempJC = 0;
        for(let j=0; j< laneFeat.length; j++){

            let inside = stopFeat[i].getGeometry().intersectsCoordinate(lanes.getSource().getFeatures()[j].getGeometry().getFirstCoordinate());
            let dfRGALaneTimeRestrictions = {}
            if (rgaEnabled) {
                dfRGALaneTimeRestrictions["timeRestrictions"] = {
                    "daysOfTheWeek": laneFeat[j].get('laneInfoDaySelection'),
                    "timePeriodType": laneFeat[j].get('laneInfoTimePeriodType'),
                    "laneInfoTimePeriodValue": laneFeat[j].get('laneInfoTimePeriodValue'),
                    "laneInfoTimePeriodRange": laneFeat[j].get('laneInfoTimePeriodRange')
                };
            }
            if (inside && laneFeat[j].get('laneType') != "Crosswalk"){
                laneFeat[j].set('inBox', true);

                if (!lanes.getSource().getFeatures()[j].get('computed')) {
                    for(let m=0; m< laneFeat[j].getGeometry().getCoordinates().length; m++){
                        let coord = laneFeat[j].getGeometry().getCoordinates()[m];
                        let lonlat = ol.proj.toLonLat(coord);

                        let currentSpeedLimits = [];
                        if(laneFeat[j].get('speedLimitType')) {
                            let mapSpeedLimits = laneFeat[j].get('speedLimitType');
                            // Filter out "Speed Limit Choice" if RGA is not enabled
                            if (!rgaEnabled && Array.isArray(mapSpeedLimits)) {
                                mapSpeedLimits = mapSpeedLimits.map(item => {
                                    const { speedLimitChoice, ...rest } = item;
                                    return rest;
                                });
                            }
                            let showSpeedLimitAlert = false;
                            for (let mapSpeedLimit of mapSpeedLimits) {
                                // Filter out "Speed Limit Type" and empty strings
                                if (mapSpeedLimit.speedLimitType != "Speed Limit Type"  && mapSpeedLimit.speedLimitType != "") {
                                    currentSpeedLimits.push(mapSpeedLimit)
                                }
                                if(!rgaEnabled && RGASpeedLimitTypes.includes(mapSpeedLimit?.speedLimitType)){
                                    showSpeedLimitAlert = true;
                                }
                            }

                            if(showSpeedLimitAlert){
                               $("#message_deposit").prop('disabled', true);
                               $('#alert_placeholder').append('<div id="speed-limit-type-alert" class="alert alert-danger alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>'+ RGASpeedLimitTypes.join(", ") + " are not allowed for node " + m + " in lane " + laneFeat[j].get('laneNumber') + " when RGA is not enabled." +'</span></div>');
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
                    let dfRGAOffsetZ = {}
                    if (rgaEnabled){
                        dfRGAOffsetZ["offsetZ"] = lanes.getSource().getFeatures()[j].get('offsetZ');
                    }
                    computedLane = {
                        "computedLaneNumber": laneFeat[j].get('computedLaneNumber'),
                        "computedLaneID": laneFeat[j].get('computedLaneID'),
                        "referenceLaneID": laneFeat[j].get('referenceLaneID'),
                        "offsetX": laneFeat[j].get('offsetX'),
                        "offsetY": laneFeat[j].get('offsetY'),
                        ...dfRGAOffsetZ,
                        "rotation": laneFeat[j].get('rotation'),
                        "scaleX": laneFeat[j].get('scaleX'),
                        "scaleY": laneFeat[j].get('scaleY')
                    }
                }

                attributeArray = [];
                for(let k in laneFeat[j].get('lane_attributes')) {
                    attributeArray.push(laneFeat[j].get('lane_attributes')[k].id);
                }
                drivingLaneArray[tempJ] = {
                    "laneID": laneFeat[j].get('laneNumber'),
                    "descriptiveName": laneFeat[j].get('descriptiveName'),
                    "laneType": laneFeat[j].get('laneType'),
                    "typeAttributes": laneFeat[j].get('typeAttribute'),
                    "sharedWith": laneFeat[j].get('sharedWith'),
                    "connections": laneFeat[j].get('connections'),
                    "laneManeuvers": attributeArray,
                    "isComputed": laneFeat[j].get('computed'),
                    ...dfRGALaneTimeRestrictions
                };
                if(!laneFeat[j].get('computed')) {
                    drivingLaneArray[tempJ].laneNodes = nodeArray;
                } else {
                    drivingLaneArray[tempJ].computedLane = computedLane;
                }

                //since some lanes are not in the driving lane
                tempJ++;
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
                    let dfRGAOffsetZ = {}
                    if (rgaEnabled){
                        dfRGAOffsetZ["offsetZ"] = lanes.getSource().getFeatures()[j].get('offsetZ');
                    }
                    computedLane = {
                        "referenceLaneID": laneFeat[j].get('referenceLaneID'),
                        "offsetX": laneFeat[j].get('offsetX'),
                        "offsetY": laneFeat[j].get('offsetY'),
                        ...dfRGAOffsetZ,
                        "rotation": laneFeat[j].get('rotation'),
                        "scaleX": laneFeat[j].get('scaleX'),
                        "scaleY": laneFeat[j].get('scaleY')
                    }
                }

                attributeArray = [];

                let laneAttributes = laneFeat[j].get('lane_attributes');
                for (let attr in laneAttributes) {
                    attributeArray.push(laneAttributes[attr].id);
                }
                crosswalkLaneArray[tempJC] = {
                    "laneID": laneFeat[j].get('laneNumber'),
                    "descriptiveName": laneFeat[j].get('descriptiveName'),
                    "laneType": laneFeat[j].get('laneType'),
                    "typeAttributes": laneFeat[j].get('typeAttribute'),
                    "sharedWith": laneFeat[j].get('sharedWith'),
                    "connections": laneFeat[j].get('connections'),
                    "laneManeuvers": attributeArray,
                    "isComputed": laneFeat[j].get('computed'),
                    ...dfRGALaneTimeRestrictions
                };
                if(!laneFeat[j].get('computed')) {
                    crosswalkLaneArray[tempJC].laneNodes = nodeArray;
                } else {
                    crosswalkLaneArray[tempJC].computedLane = computedLane;
                }

                //since some lanes are not in the driving lane
                tempJC++;

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
            incompleteApproaches.push(drivingLaneArray.length>0 ? drivingLaneArray[0]?.laneID : "NA");
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
            // $('#alert_placeholder').append('<div id="spat-alert" class="alert alert-warning alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>'+ "SPaT message empty for lane " + laneFeat[a].get('laneNumber') + "." +'</span></div>');
        }

        if (laneFeat[a].get('laneType') != null && (laneFeat[a].get('laneType') === "Parking" || laneFeat[a].get('laneType') === "Sidewalk")) {
            let messageType = $('#message_type').val();
            if (messageType === "Frame+RGA") {
                let existingAlert = $('#alert_placeholder').find('#rga-alert-' + laneFeat[a].get('laneNumber'));
                if (existingAlert.length === 0) {
                    $('#alert_placeholder').append('<div id="rga-alert-' + laneFeat[a].get('laneNumber') + '" class="alert alert-warning alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>' + "Lane number " + laneFeat[a].get('laneNumber') + " cannot be encoded for RGA, as " + laneFeat[a].get('laneType') + " lane type is not supported." + '</span></div>');
                }}
        }
    }
    errors.getSource().clear();
    

    for(let j=0; j< laneFeat.length; j++){        
        let coords = laneFeat[j].getGeometry().getFirstCoordinate();
        let errorMarker = new ol.Feature({
            geometry: new ol.geom.Point(coords)
        });
        errorMarker.setStyle(errorMarkerStyle);
        if (!laneFeat[j].get("inBox")){
            $("#message_deposit").prop('disabled', true);
            $('#alert_placeholder').append('<div class="alert alert-danger alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>'+ "Lane " + laneFeat[j].get('laneNumber') + " exists outside of an approach." +'</span></div>');
            errors.getSource().addFeature(errorMarker);
        }
        if (!laneFeat[j].get('laneNumber')) {
            // lat lon repeated otherwise the first transform if lane exists outside approach will transform coordinates
            let latlon = ol.proj.toLonLat(laneFeat[j].getGeometry().getFirstCoordinate());
            $("#message_deposit").prop('disabled', true);
            $('#alert_placeholder').append('<div class="alert alert-danger alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>'+ "Lane at " + latlon[1] + ", " + latlon[0] + " is not assigned a lane number. Check overlapping points." +'</span></div>');
            errors.getSource().addFeature(errorMarker);
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
                "referenceLat": feature.get('LonLat').lat,
                "referenceLon": feature.get('LonLat').lon,
                "referenceElevation": feature.get('elevation'),
                "roadAuthorityId": feature.get('roadAuthorityId')?.split(".").map(num => parseInt(num, 10)),
                "roadAuthorityIdType": feature.get('roadAuthorityIdType'),
            };

            rgaBaseLayerFields = {}; // Ensure to clear the data for each call
            // Only populate JSON with RGA fields when the RGA toggle is enabled
            if (rgaEnabled) { // Global variable rgaEnabled is defined in mapping.js
                rgaBaseLayerFields["contentVersion"] = parseInt(feature.get('contentVersion'));
                let datetime = parseDatetimeStr(feature.get('contentDateTime'));
                rgaBaseLayerFields["timeOfCalculation"] = datetime.date;
                rgaBaseLayerFields["contentDateTime"] = datetime.time;

                // Add mapped geometry ID to intersection geometry reference point
                reference["mappedGeomID"] = feature.get('mappedGeometryId').split(".").map(num => parseInt(num, 10));

                // Validate RGA required fields
                validateRequiredRGAFields(feature);
            }
            //Filter out "Speed Limit Type" and empty strings
            let speedLimitType = (feature.get('speedLimitType') || []).filter((item) => item.speedLimitType !== "Speed Limit Type" && item.speedLimitType !== "");
            // Filter out "Speed Limit Choice" if RGA is not enabled
            if (!rgaEnabled && Array.isArray(speedLimitType)) {
                speedLimitType = speedLimitType.map(item => {
                    const { speedLimitChoice, ...rest } = item;
                    return rest;
                });
            }
            referenceChild = {
                "speedLimitType": speedLimitType
            };

            if (feature.get('intersectionName') == undefined || feature.get('intersectionName') == "") {
                $("#message_deposit").prop('disabled', true);
                $('#alert_placeholder').append('<div class="alert alert-danger alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>' + "No intersection name defined." + '</span></div>');
            }

            let showSpeedLimitAlert = false;
            for (let mapSpeedLimit of speedLimitType) {
                if(!rgaEnabled && RGASpeedLimitTypes.includes(mapSpeedLimit?.speedLimitType)){
                    showSpeedLimitAlert = true;
                }
            }

            if(showSpeedLimitAlert){
                $("#message_deposit").prop('disabled', true);
                $('#alert_placeholder').append('<div id="speed-limit-type-alert" class="alert alert-danger alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>'+ RGASpeedLimitTypes.join(", ") + " are not allowed for reference point when RGA is not enabled." +'</span></div>');
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
        ...rgaBaseLayerFields,
        "intersectionGeometry": intersectionGeometry,
        "spatData": spat
    }

    isdMessage.mapData = mapData;
    isdMessage.messageType = $("#message_type").val();
    isdMessage.nodeOffsets = $("#node_offsets").val();
    isdMessage.enableElevation = $("#enable_elevation").is(":checked");

    return isdMessage;
}

/**
 * Parses a datetime string in the format "d/m/Y H:m:s" and returns an object with separate date and time components.
 *
 * @param {string} datetimestring - The datetime string to parse.
 * @returns {Object} An object containing the parsed date and time components.
 * @returns {Object.date} An object containing the day, month, and year.
 * @returns {number} date.day - The day of the month.
 * @returns {number} date.month - The month of the year.
 * @returns {number} date.year - The year.
 * @returns {Object.time} An object containing the hour, minute, and second.
 * @returns {number} time.hour - The hour of the day.
 * @returns {number} time.minute - The minute of the hour.
 * @returns {number} time.second - The second of the minute.
 */
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
 * @brief According to J2945_A RGA definition, mappedGeometryId, contentVersion, contentDateTime are required
 */
function validateRequiredRGAFields(feature){    
    let map_fields_descriptions= {
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
        $('#alert_placeholder').append('<div class="alert alert-danger alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>' + "Cannot deposit without a region defined." + '</span></div>');
        status = true;
    }
    
    if (vectors.getSource().getFeatures().length < 2) {
        $('#alert_placeholder').append('<div class="alert alert-danger alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>' + "Missing anchor or verified points." + '</span></div>');
        status = true;
    }
    return status;
}


/**
 * This function contains the common logic to enable or disabled the explicit node offset 
 * @param {*} value 
 */
function disableOrEnableExplicitRGA(value) {
    let nodeOffsetsEl = document.getElementById("node_offsets");
    if (value === "RGA" || value === "Frame+RGA") {
        if (nodeOffsetsEl) {
            for (let i = 0; i < nodeOffsetsEl.options.length; i++) {
                if (nodeOffsetsEl.options[i].value === "Explicit") {
                    nodeOffsetsEl.options[i].disabled = true;
                    nodeOffsetsEl.selectedIndex = 1;
                    $('#node_offsets').trigger("change");
                }
            }
        }
    } else {
        if (nodeOffsetsEl) {
            for (let i = 0; i < nodeOffsetsEl.options.length; i++) {
                if (nodeOffsetsEl.options[i].value === "Explicit") {
                    nodeOffsetsEl.options[i].disabled = false;
                }
            }
        }
    }
}

/**
 * Purpose: Greys out explicit node offsets when RGA message type is selected
 * @event: just checking if RGA message type is selected
 */
function removeExplicitRGA() {
    var messageTypeEl = document.getElementById("message_type");
    if (!messageTypeEl) return;

    messageTypeEl.addEventListener("change", function () {
        disableOrEnableExplicitRGA(this.value);
    });
}
document.addEventListener("DOMContentLoaded", removeExplicitRGA);  