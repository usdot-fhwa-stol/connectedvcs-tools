/**
 * Created by martzth on 3/20/2015.
 * Updated 3/2017 by martzth
 */

/**
 * DEFINE GLOBAL VARIABLES
 */

var proj_name, host;
var message_json_input, message_hex_input, message_text_input;
var message_status_div;


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
            var message = createMessageJSON();
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
        var message =  JSON.parse(message_json_input.val());
        var type = $("#message_type").val();
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

    var isdMessage = {};
    var minuteOfTheYear = moment().diff(moment().startOf('year'), 'minutes');

    //Feature object models
    var stopFeat = box.features;
    var laneFeat = lanes.features;

    //Building of nested layers
    var approachesArray = { "approach": []};
    var drivingLanesArray = {"drivingLanes":[]};
    var crosswalkLanesArray = {"crosswalkLanes":[]};
    var attributesArray = {"laneAttributes":[]};
    var nodesArray = {"laneNodes":[]};
    var spatsArray = {"spatNodes":[]};

    var approachArray = approachesArray["approach"];
    var drivingLaneArray = drivingLanesArray["drivingLanes"];
    var crosswalkLaneArray = crosswalkLanesArray["crosswalkLanes"];
    var attributeArray = attributesArray["laneAttributes"];
    var nodeArray = nodesArray["laneNodes"];
    var computedLane = "";
    var spatArray = spatsArray["spatNodes"];

    for(var b=0; b< laneFeat.length; b++){
        lanes.features[b].attributes.inBox = false;
    }

    for(var i=0; i< stopFeat.length; i++){
        var temp_j = 0;
        var temp_j_c = 0;
        for(var j=0; j< laneFeat.length; j++){

            var inside = (box.features[i].geometry).containsPoint(lanes.features[j].geometry.components[0]);
            if (inside && lanes.features[j].attributes.laneType != "Crosswalk"){
                //console.log("Stop Box: " + i + " contains lead point of feature " + j);
                lanes.features[j].attributes.inBox = true;

                if(!lanes.features[j].attributes.computed) {
	                for(var m=0; m< lanes.features[j].geometry.components.length; m++){
	                    var latlon = new OpenLayers.LonLat(lanes.features[j].geometry.components[m].x,lanes.features[j].geometry.components[m].y).transform(toProjection, fromProjection);
	                    nodeArray[m] = {
	                        "nodeNumber": m,
	                        "nodeLat": latlon.lat,
	                        "nodeLong": latlon.lon,
	                        "nodeElev": lanes.features[j].attributes.elevation[m]?.value,
	                        "laneWidthDelta": lanes.features[j].attributes.laneWidth[m]
	                    }
	                }
                } else {
                	computedLane = {
                			"computedLaneNumber": lanes.features[j].attributes.computedLaneNumber,
                            "computedLaneID": lanes.features[j].attributes.computedLaneID,
                            "referenceLaneID": lanes.features[j].attributes.referenceLaneID,
                            "offsetX": lanes.features[j].attributes.offsetX,
                            "offsetY": lanes.features[j].attributes.offsetY,
                            "rotation": lanes.features[j].attributes.rotation,
                            "scaleX": lanes.features[j].attributes.scaleX,
                            "scaleY": lanes.features[j].attributes.scaleY
                	}
                }

                attributeArray = [];

                for (var k in lanes.features[j].attributes.lane_attributes) {
                    var id = lanes.features[j].attributes.lane_attributes[k].id;
                    attributeArray.push(id)
                }
                console.log(lanes.features[j].attributes);
                drivingLaneArray[temp_j] = {
                    "laneID": lanes.features[j].attributes.laneNumber,
                    "descriptiveName": lanes.features[j].attributes.descriptiveName,
                    "laneType": lanes.features[j].attributes.laneType,
                    "typeAttributes": lanes.features[j].attributes.typeAttribute,
                    "sharedWith": lanes.features[j].attributes.sharedWith,
                    "connections": lanes.features[j].attributes.connections,
                    "laneManeuvers": attributeArray,
                    "isComputed": lanes.features[j].attributes.computed
                };
                if(!lanes.features[j].attributes.computed) {
                	drivingLaneArray[temp_j].laneNodes = nodeArray;
                } else {
                	drivingLaneArray[temp_j].computedLane = computedLane;
                }

                //since some lanes are not in the driving lane
                temp_j++;
            } else if(lanes.features[j].attributes.laneType == "Crosswalk"){
                //even though not in a "box" it's still allowed to be outside as a crosswalk - still want to be able to catch vehicle lanes outside
                lanes.features[j].attributes.inBox = true;

                if(!lanes.features[j].attributes.computed) {
	                for(var m=0; m< lanes.features[j].geometry.components.length; m++){
	                    var latlon = new OpenLayers.LonLat(lanes.features[j].geometry.components[m].x,lanes.features[j].geometry.components[m].y).transform(toProjection, fromProjection);
	                    nodeArray[m] = {
	                        "nodeNumber": m,
	                        "nodeLat": latlon.lat,
	                        "nodeLong": latlon.lon,
	                        "nodeElev": lanes.features[j].attributes.elevation[m]?.value,
	                        "laneWidthDelta": lanes.features[j].attributes.laneWidth[m]
	                    }
	                }
                } else {
                	computedLane = {
                            "referenceLaneID": lanes.features[j].attributes.referenceLaneID,
                            "offsetX": lanes.features[j].attributes.offsetX,
                            "offsetY": lanes.features[j].attributes.offsetY,
                            "rotation": lanes.features[j].attributes.rotation,
                            "scaleX": lanes.features[j].attributes.scaleX,
                            "scaleY": lanes.features[j].attributes.scaleY
                	}
                }

                attributeArray = [];

                for (var k in lanes.features[j].attributes.lane_attributes) {
                    var id = lanes.features[j].attributes.lane_attributes[k].id;
                    attributeArray.push(id)
                }
                console.log(lanes.features[j].attributes);
                crosswalkLaneArray[temp_j_c] = {
                    "laneID": lanes.features[j].attributes.laneNumber,
                    "descriptiveName": lanes.features[j].attributes.descriptiveName,
                    "laneType": lanes.features[j].attributes.laneType,
                    "typeAttributes": lanes.features[j].attributes.typeAttribute,
                    "sharedWith": lanes.features[j].attributes.sharedWith,
                    "connections": lanes.features[j].attributes.connections,
                    "laneManeuvers": attributeArray,
                    "isComputed": lanes.features[j].attributes.computed
                };
                if(!lanes.features[j].attributes.computed) {
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
            "approachType": box.features[i].attributes.approachType,
            "approachID": box.features[i].attributes.approachID,
            "descriptiveName": box.features[i].attributes.approachName,
            "speedLimit": box.features[i].attributes.speedLimit,
            "drivingLanes": drivingLaneArray
        };

        drivingLaneArray = [];
    }

    approachArray[stopFeat.length] = {
        "approachType": "Crosswalk",
        "approachID": -1,
        "crosswalkLanes": crosswalkLaneArray
    };


    for (var a = 0; a < laneFeat.length; a++) {

        if (lanes.features[a].attributes.inBox == true && lanes.features[a].attributes.signalGroupID != null && lanes.features[a].attributes.stateConfidence != null) {

            var obj = {
                "laneSet": lanes.features[a].attributes.laneNumber,
                "spatRevision": lanes.features[a].attributes.spatRevision,
                "signalGroupID": lanes.features[a].attributes.signalGroupID,
                "signalPhase": lanes.features[a].attributes.signalPhase,
                "startTime": lanes.features[a].attributes.startTime,
                "minEndTime": lanes.features[a].attributes.minEndTime,
                "maxEndTime": lanes.features[a].attributes.maxEndTime,
                "likelyTime": lanes.features[a].attributes.likelyTime,
                "stateConfidence": lanes.features[a].attributes.stateConfidence.substring(lanes.features[a].attributes.stateConfidence.lastIndexOf("(")+1,lanes.features[a].attributes.stateConfidence.lastIndexOf(")")),
                "nextTime": lanes.features[a].attributes.nextTime
            };

            var k_index = -1;

            //what if the spat doesn't exist?

            for (var k = 0; k < spatArray.length; k++) {
                if (spatArray[k].stateConfidence == obj.stateConfidence ) {
                    console.log("spat: ", spatArray[k])
                    k_index = k;
                    console.log("k_index=", k_index)
                }
            }

            if (k_index != -1) {
                (spatArray[k_index].laneSet) = (spatArray[k_index].laneSet)+(obj.laneSet)
            } else {
                spatArray.push(obj);
            }

        } else {
            $('#alert_placeholder').html('<div class="alert alert-warning alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>'+ "SPaT message empty for lane " + lanes.features[a].attributes.laneNumber + "." +'</span></div>');
        }
    }
    errors.clearMarkers();
    var size = new OpenLayers.Size(21,25);
    var offset = new OpenLayers.Pixel(-(size.w/2), -size.h);
    var icon = new OpenLayers.Icon('img/error.png',size,offset);

    for(var j=0; j< laneFeat.length; j++){
        var latlon;
        if (!lanes.features[j].attributes.inBox){
            latlon = new OpenLayers.LonLat(lanes.features[j].geometry.components[0].x,lanes.features[j].geometry.components[0].y).transform(toProjection, fromProjection);
            $("#message_deposit").prop('disabled', true);
            $('#alert_placeholder').html('<div class="alert alert-danger alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>'+ "Lane " + lanes.features[j].attributes.laneNumber + " exists outside of an approach." +'</span></div>');
            errors.addMarker(new OpenLayers.Marker(latlon.transform(fromProjection, toProjection),icon));
        }
        if (!lanes.features[j].attributes.laneNumber){
            //lat lon repeated otherwise the first transform if lane exists outside approach will transform coordinates
            latlon = new OpenLayers.LonLat(lanes.features[j].geometry.components[0].x,lanes.features[j].geometry.components[0].y).transform(toProjection, fromProjection);
            $("#message_deposit").prop('disabled', true);
            $('#alert_placeholder').html('<div class="alert alert-danger alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>'+ "Lane at " + latlon.lat + ", " + latlon.lon + " is not assigned a lane number. Check overlapping points." +'</span></div>');
            errors.addMarker(new OpenLayers.Marker(latlon.transform(fromProjection, toProjection),icon));
        }
    }

    for ( var f = 0; f < vectors.features.length; f++) {
        var feature = vectors.features[f];
        if (vectors.features[f].attributes.marker.name == "Reference Point Marker") {

            var reference = {
                "descriptiveIntersctionName": feature.attributes.intersectionName,
                "layerID": feature.attributes.layerID,
                "intersectionID": feature.attributes.intersectionID,
                "msgCount": feature.attributes.revisionNum,
                "masterLaneWidth": feature.attributes.masterLaneWidth,
                "referenceLat": feature.attributes.LonLat.lat,
                "referenceLon": feature.attributes.LonLat.lon,
                "referenceElevation": feature.attributes.elevation
            }

            var referenceChild = {
                "speedLimitType": feature.attributes.speedLimitType
            }

            if (feature.attributes.intersectionName == undefined || feature.attributes.intersectionName == ""){
                $("#message_deposit").prop('disabled', true);
                $('#alert_placeholder').html('<div class="alert alert-warning alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>'+ "No intersection name defined." +'</span></div>');
            }

        }

        if (vectors.features[f].attributes.marker.name == "Verified Point Marker") {

            var verified = {
                "verifiedMapLat": feature.attributes.LonLat.lat,
                "verifiedMapLon": feature.attributes.LonLat.lon,
                "verifiedMapElevation": feature.attributes.elevation,
                "verifiedSurveyedLat": feature.attributes.verifiedLat,
                "verifiedSurveyedLon": feature.attributes.verifiedLon,
                "verifiedSurveyedElevation": feature.attributes.verifiedElev
            }

        }

    }

    var spat = {
        "intersections": {
            "status": "00",
            "states": spatArray
        }
    }


    var intersectionGeometry = {
        "referencePoint": reference,
        "referencePointChild": referenceChild,
        "verifiedPoint": verified,
        "laneList": approachesArray
    }


    var mapData = {
        "minuteOfTheYear": minuteOfTheYear,
        "layerType": "intersectionData",
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
 * Purpose: pretty terrible error check
 * @params: DOM elements
 * @event: just checking that a marker exists, etc so that the message can build appropriately
 */

function errorCheck(){

    var status = false; //false means there are no errors

    if (lanes.features.length == 0){
        $('#alert_placeholder').html('<div class="alert alert-danger alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>' + "Cannot deposit without a region defined." + '</span></div>')
        status = true;
    }

    if (vectors.features.length < 2){
        $('#alert_placeholder').html('<div class="alert alert-danger alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><span>' + "Missing anchor or verified points." + '</span></div>')
        status = true;
    }

    return status;
}
