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


import {
    lanes,
    vectors,
    laneMarkers,
    laneWidths,
    polygons,
    toProjection, fromProjection,
    box,
    map,
    area,
    trace,
    polyMarkers,
    selected_marker_limit,
    radiuslayer,
    setCirclesTemp,
    toggleControlsOn,
    setFeatureAttributes,
    getCookie,
    drawCircleSlices,
    circles_reset
} from './mapping.js';


var proj_name, host;
var message_json_input, message_hex_input, message_text_input;
var message_status_div;
var message;
var circle_bounds;

/**
 * Define functions that must bind on load
 */

$(document).ready(function () {
    proj_name = window.location.pathname.split('/')[1];
    host = window.location.host;

    message_json_input = $('#message_json');
    message_hex_input = $('#message_hex');
    message_text_input = $('#message_text');
    message_status_div = $('#message_status');

    // Restricting JSON input to readonly to prevent user error - message is generated from map and not user input
    message_json_input.prop("readonly", true);
    message_json_input.on("paste keydown drop", function(e) {
        e.preventDefault();
    });

    /**
     * Purpose: change deposit state
     * @params: click event
     * @event: checks/unchecks deposit on msg type
     */

    $("#message_deposit_modal").on('shown.bs.modal', function (e) {
        resetMessageForm();
        if (!errorCheck()) {
            message = createMessageJSON();
            message_json_input.val(JSON.stringify(message, null, 2))
        } else {
            $("#message_deposit").prop('disabled', true);
        }
    });

    $('#message_type').on("change", function () {
        resetMessageForm();
        message_json_input.val(JSON.stringify(createMessageJSON(), null, 2));
    });

    $('#node_offsets').on("change", function () {
        setCookie("tim_node_offsets", $('#node_offsets').val(), 365);
        resetMessageForm();
        message_json_input.val(JSON.stringify(createMessageJSON(), null, 2));
    });

    $('#enable_elevation').on("change", function () {
        setCookie("tim_enable_elevation", $('#enable_elevation').is(":checked"), 365);
        resetMessageForm();
        message_json_input.val(JSON.stringify(createMessageJSON(), null, 2));
    });


    /**
     * Purpose: to allow deposit of message (ASD only)
     * @params: message header (BBox + ttl)
     * @event: attaches header to message and POSTS to server
     */

    $('#message_deposit').click(function () {
        var message_json = JSON.stringify(createMessageJSON(), null, 2);

        // Send via AJAX
        $.ajax({
            type: "POST",
            url: "/" + proj_name + "/builder/messages/travelerinfo",
            contentType: "text/plain",
            data: message_json,
            success: function (result) {
                // console.log("success: ", result);
                setMessageResult(true, result.hexString, "hex");
                setMessageResult(true, result.readableString, "text");
            },
            error: function (xhr, status, error) {
                console.log("fail: ", xhr.responseText);
                setMessageResult(false, xhr.responseText);
            }
        });
    });
});

function setMessageResult(success, message, type) {

    if (success) {
        message_status_div.removeClass('has-error').addClass('has-success');
    }
    else {
        message_status_div.removeClass('has-success').addClass('has-error');
    }

    if (type == "hex") {
        message_hex_input.val(message);
        $('.message_size').text((message.length / 2) + " bytes");
    } else {
        message_text_input.val(message);
    }
}


/**
 * Purpose: reset message form on close
 * @params: click event
 * @event: clears boxes of messages
 */

$('.close').click(function () {
    resetMessageForm();
});

function resetMessageForm() {
    $('#alert_placeholder').html("");
    $("#message_deposit").prop('disabled', false);
    message_json_input.val("")
    message_hex_input.val("")
    message_text_input.val("")
    $('.message_size').text("");
    message_status_div.removeClass('has-error has-success');
}


/**
 * Purpose: create JSON from map elements
 * @params: map layers and elements
 * @event: builds JSON message for deposit
 * Note: each variable is stored in the feature object model
 */

function buildLaneRegionJSON(laneFeature) {
    const coords = laneFeature.getGeometry().getCoordinates();
    const elevs = laneFeature.get('elevation');
    const widths = laneFeature.get('laneWidth');
    let nodeArray = [];

    coords.forEach(([x, y], m) => {
        const [lon, lat] = ol.proj.transform([x, y], toProjection, fromProjection);
        nodeArray.push({
            nodeNumber: m,
            nodeLat: lat,
            nodeLong: lon,
            nodeElevation: elevs?.[m]?.value || 0,
            laneWidth: widths?.[m] || 0
        });
    });

    let ext = "";
    try {
        ext = getExtent(laneFeature.get('extent'));
    } catch { }

    return {
        regionType: "lane",
        laneNodes: nodeArray,
        extent: ext
    };
}

function buildPolygonRegionJSON(polyFeature) {
    const geom = polyFeature.getGeometry();
    const coords = geom.getCoordinates()[0]; // exterior ring
    const title = polyFeature.get('title');

    if (title === "circle") {
        const bounds = geom.getExtent();
        const [minX, minY, maxX, maxY] = ol.proj.transformExtent(bounds, toProjection, fromProjection);
        const startX = (minX + maxX) / 2;
        const startY = (minY + maxY) / 2;

        const nodeArray = [
            { nodeLat: startY, nodeLong: startX },
            { nodeLat: maxY, nodeLong: startX }
        ];

        return {
            regionType: "circle",
            radius: $('#radius').val(),
            laneNodes: nodeArray,
            extent: ""
        };
    }

    const elevs = polyFeature.get('elevation');
    let nodeArray = [];
    for (let m = 0; m < coords.length - 1; m++) {
        const [x, y] = coords[m];
        const [lon, lat] = ol.proj.transform([x, y], toProjection, fromProjection);
        nodeArray.push({
            nodeNumber: m,
            nodeLat: lat,
            nodeLong: lon,
            nodeElevation: elevs?.[m]?.value || 0
        });
    }

    return {
        regionType: "region",
        laneNodes: nodeArray,
        extent: ""
    };
}

// Finds the region owned by the given marker, tracked by markerId (see mapping.js).
function findOwnedRegionJSON(markerFeature, laneFeat, polyFeat) {
    const markerId = markerFeature.get('markerId');
    const laneMatch = laneFeat.find(f => f.get('ownerMarkerId') === markerId);
    if (laneMatch) return buildLaneRegionJSON(laneMatch);

    const polyMatch = polyFeat.find(f => f.get('ownerMarkerId') === markerId);
    if (polyMatch) return buildPolygonRegionJSON(polyMatch);

    return null;
}

function buildAnchorPointJSON(feature, marker) {
    const attrs = feature.getProperties();
    for (let a = 0; a < attrs.content?.length; a++) {
        if (attrs.content[a] === 0) {
            attrs.content[a] = (12544 + Number(attrs.speedLimit)).toString();
        }
    }

    const anchor = {
        name: marker.name,
        referenceLat: attrs.LonLat?.lat,
        referenceLon: attrs.LonLat?.lon,
        referenceElevation: attrs.elevation,
        masterLaneWidth: attrs.masterLaneWidth,
        sspTimRights: attrs.sspTimRights,
        packetID: attrs.packetID,
        msgCount: attrs.msgCount,
        content: attrs.content,
        sspTypeRights: attrs.sspTypeRights,
        sspContentRights: attrs.sspContentRights,
        sspLocationRights: attrs.sspLocationRights,
        direction: attrs.direction?.substring(1, 2),
        mutcd: attrs.mutcd?.substring(1, 2),
        infoType: attrs.infoType?.substring(1, 2),
        priority: attrs.priority,
        startTime: attrs.startTime,
        endTime: attrs.endTime,
        maxDuration: attrs.maxDuration,
        heading: getHeading(attrs.heading),
        meanVerticalVariation: attrs.meanVerticalVariation,
        verticalVariationStdDev: attrs.verticalVariationStdDev,
        meanHorizontalVariation: attrs.meanHorizontalVariation,
        horizontalVariationStdDev: attrs.horizontalVariationStdDev,
        road_surface: attrs.road_surface,
        road_condition: attrs.road_condition?.substring(1, 2),
        road_surface_type: attrs.road_surface_type
    };

    if (attrs.road_surface_type !== undefined) {
        anchor.road_surface_type = attrs.road_surface_type;
    }

    return anchor;
}

function createMessageJSON() {
    let spatMessage = {};
    let minuteOfTheYear = moment.utc().diff(moment.utc().startOf('year'), 'minutes');

    const laneFeat = lanes.getSource().getFeatures();
    const polyFeat = polygons.getSource().getFeatures();
    const vectorFeat = vectors.getSource().getFeatures();
    const areaFeat = area.getSource().getFeatures();

    // FRAMES: one per TIM content marker, paired with its owned region (if any)
    let frames = [];
    let verified = null;
    vectorFeat.forEach((feature) => {
        const marker = feature.get('marker');

        if (marker?.type === "TIM") {
            const anchor = buildAnchorPointJSON(feature, marker);
            const region = findOwnedRegionJSON(feature, laneFeat, polyFeat);
            frames.push({
                anchorPoint: anchor,
                regions: region ? [region] : []
            });
        }

        if (marker?.type === "VER") {
            const attrs = feature.getProperties();
            verified = {
                verifiedMapLat: attrs.LonLat?.lat,
                verifiedMapLon: attrs.LonLat?.lon,
                verifiedMapElevation: attrs.elevation,
                verifiedSurveyedLat: attrs.verifiedLat,
                verifiedSurveyedLon: attrs.verifiedLon,
                verifiedSurveyedElevation: attrs.verifiedElev
            };
        }
    });

    // AREA → BOUNDING BOX
    if (areaFeat.length > 0) {
        const boxGeom = areaFeat[0].getGeometry();
        const boxCoords = boxGeom.getCoordinates()[0];
        const nw = ol.proj.transform(boxCoords[1], toProjection, fromProjection);
        const se = ol.proj.transform(boxCoords[3], toProjection, fromProjection);

        spatMessage.applicableRegion = {
            nwLat: nw[1],
            nwLon: nw[0],
            seLat: se[1],
            seLon: se[0]
        };
    }

    // Final JSON
    spatMessage.frames = frames;
    spatMessage.verifiedPoint = verified;
    spatMessage.messageType = $("#message_type").val();
    spatMessage.nodeOffsets = $("#node_offsets").val();
    spatMessage.enableElevation = $("#enable_elevation").is(":checked");
    spatMessage.timeStamp = minuteOfTheYear;

    return spatMessage;
}


/**
 * Purpose: pretty terrible error check
 * @params: DOM elements
 * @event: just checking that a marker exists, etc so that the message can build appropriately
 */

function appendAlert(message) {
    $('#alert_placeholder').append(`<div class="alert alert-danger alert-dismissable">
        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
        <span>${message}</span>
    </div>`);
}

function errorCheck() {
    let status = false; // false means no errors

    const lanesFeatures = lanes.getSource().getFeatures();
    const polygonsFeatures = polygons.getSource().getFeatures();
    const vectorFeatures = vectors.getSource().getFeatures();
    const timMarkers = vectorFeatures.filter(f => f.get('marker')?.type === "TIM");
    const verifiedMarkers = vectorFeatures.filter(f => f.get('marker')?.type === "VER");

    if (timMarkers.length === 0) {
        appendAlert("At least one road sign marker is required.");
        status = true;
    }

    if (verifiedMarkers.length !== 1) {
        appendAlert("Missing verified point.");
        status = true;
    }

    // Flag regions with no valid owning marker instead of silently dropping them.
    const timMarkerIds = new Set(timMarkers.map(f => f.get('markerId')).filter(Boolean));
    const orphanedRegionCount = [...lanesFeatures, ...polygonsFeatures].filter((f) => {
        const ownerMarkerId = f.get('ownerMarkerId');
        return !ownerMarkerId || !timMarkerIds.has(ownerMarkerId);
    }).length;
    if (orphanedRegionCount > 0) {
        appendAlert(`${orphanedRegionCount} region(s) on the map are not associated with any road sign marker. Associate each with a marker, or delete it, before encoding.`);
        status = true;
    }

    timMarkers.forEach((feature, index) => {
        const markerLabel = feature.get('marker')?.name || `Marker ${index + 1}`;

        const markerId = feature.get('markerId');
        const hasRegion = lanesFeatures.some(f => f.get('ownerMarkerId') === markerId) ||
            polygonsFeatures.some(f => f.get('ownerMarkerId') === markerId);
        if (!hasRegion) {
            appendAlert(`"${markerLabel}" is missing an associated region (lane, polygon, or circle).`);
            status = true;
        }

        try {
            const startTime = feature.get('startTime');
            const endTime = feature.get('endTime');
            const content = feature.get('content');
            const priority = feature.get('priority');
            const mutcd = feature.get('mutcd');

            if (!startTime || !endTime) {
                appendAlert(`"${markerLabel}" is missing start and end time.`);
                status = true;
            }

            if ((!content || !content[0] || content[0].codes?.length === 0) && !content[0]?.text) {
                appendAlert(`"${markerLabel}" is missing ITIS information.`);
                status = true;
            }

            if (!priority) {
                appendAlert(`"${markerLabel}" is missing a priority level.`);
                status = true;
            }

            if (!mutcd) {
                appendAlert(`"${markerLabel}" is missing mutcd codes.`);
                status = true;
            }
        } catch (err) {
            appendAlert(`"${markerLabel}" is missing one or more required fields.`);
            status = true;
        }
    });

    return status;
}


/**
 * Purpose: figure out which slices are active on the circle
 * @params: headings circle
 * @returns: an array of active slices (headings)
 */

function getHeading(headingsCircle) {

    var totalSlices = 0;

    var headingsArray = { "headings": [] };
    var headingArray = headingsArray["headings"];

    for (var i = 0; i < headingsCircle.length; i++) {
        if (headingsCircle[i].active) {
            totalSlices++;
            headingArray.push(i);
        }
    }

    if (totalSlices == 0) {
        headingArray = [];
    }

    return headingArray;
}


/**
 * Purpose: gets extent
 * @params: full extent text
 * @returns: extent number
 */

function getExtent(text) {
    var result = text.split(")");
    return result[0].slice(1, result[0].length)
}