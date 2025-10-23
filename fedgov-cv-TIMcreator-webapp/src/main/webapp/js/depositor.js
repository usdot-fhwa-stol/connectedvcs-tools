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

    $('#message_deposit_modal :checkbox').click(function () {
        var $this = $(this);

        if ($this.is(':checked')) {
            $("#ttl").show();
            $("#message_deposit").html('Encode & Deposit')
        } else {
            $("#ttl").hide();
            $("#message_deposit").html('Encode')
        }
    });

    $('#message_type').on("change", function () {
        var msg_type = $('#message_type').val();
        $('#deposit_check').prop('checked', false);
        $("#ttl").hide();
        $("#message_deposit").html('Encode')

        if (msg_type !== "ASD") {
            $('#deposit_check').prop('disabled', true);
        } else {
            $('#deposit_check').prop('disabled', false);
        }
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
    var message_json = message_json_input.val();

    if (document.getElementById('deposit_check').checked) {
            // OpenLayers 10 (global) way to get features from the area layer
            var areaFeatures = area.getSource().getFeatures();

            if (areaFeatures.length === 0) {
                document.getElementById('alert_placeholder').innerHTML =
                    '<div class="alert alert-danger alert-dismissable">' +
                    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>' +
                    '<span>Cannot deposit without an applicable region defined.</span>' +
                    '</div>';
            } else {
                var time = document.getElementById('time').value;

                var spatHeader = {
                    "timeToLive": time
                };

            message.deposit = spatHeader;
            message_json = JSON.stringify(message, null, 2);
        }
    }
        // Send via AJAX
        $.ajax({
            type: "POST",
            url: "/" + proj_name + "/builder/messages/travelerinfo",
            contentType: "text/plain",
            data: message_json,
            success: function (result) {
                console.log("success: ", result);
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

function createMessageJSON() {
    let spatMessage = {};

    const laneFeat = lanes.getSource().getFeatures();
    const polyFeat = polygons.getSource().getFeatures();
    const vectorFeat = vectors.getSource().getFeatures();
    const areaFeat = area.getSource().getFeatures();

    let regionsArray = { regions: [] };
    let regionArray = regionsArray.regions;

    // LANES
    laneFeat.forEach((laneFeature, j) => {
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

        regionArray.push({
            regionType: "lane",
            laneNodes: nodeArray,
            extent: ext
        });
    });

    // POLYGONS
    polyFeat.forEach((polyFeature, j) => {
        const geom = polyFeature.getGeometry();
        const coords = geom.getCoordinates()[0]; // exterior ring
        const title = polyFeature.get('title');
        let nodeArray = [];
        let ext = "";

        if (title === "circle") {
            const bounds = geom.getExtent();
            const [minX, minY, maxX, maxY] = ol.proj.transformExtent(bounds, toProjection, fromProjection);
            const startX = (minX + maxX) / 2;
            const startY = (minY + maxY) / 2;

            nodeArray = [
                { nodeLat: startY, nodeLong: startX },
                { nodeLat: maxY, nodeLong: startX }
            ];

            regionArray.push({
                regionType: "circle",
                radius: $('#radius').val(),
                laneNodes: nodeArray,
                extent: ext
            });
        } else {
            const elevs = polyFeature.get('elevation');
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

            regionArray.push({
                regionType: "region",
                laneNodes: nodeArray,
                extent: ext
            });
        }
    });

    // MARKERS
    let anchor = null;
    let verified = null;
    vectorFeat.forEach((feature) => {
        const marker = feature.get('marker');

        if (marker?.type === "TIM") {
            const attrs = feature.getProperties();
            for (let a = 0; a < attrs.content?.length; a++) {
                if (attrs.content[a] === 0) {
                    attrs.content[a] = (12544 + Number(attrs.speedLimit)).toString();
                }
            }

            anchor = {
                name: marker.name,
                referenceLat: attrs.LonLat?.lat,
                referenceLon: attrs.LonLat?.lon,
                referenceElevation: attrs.elevation,
                masterLaneWidth: attrs.masterLaneWidth,
                sspTimRights: attrs.sspTimRights,
                packetID: attrs.packetID,
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
                heading: getHeading(attrs.heading)
            };
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
    spatMessage.regions = regionArray;
    spatMessage.anchorPoint = anchor;
    spatMessage.verifiedPoint = verified;
    spatMessage.messageType = $("#message_type").val();
    spatMessage.nodeOffsets = $("#node_offsets").val();
    spatMessage.enableElevation = $("#enable_elevation").is(":checked");

    return spatMessage;
}


/**
 * Purpose: pretty terrible error check
 * @params: DOM elements
 * @event: just checking that a marker exists, etc so that the message can build appropriately
 */

function errorCheck() {
    let status = false; // false means no errors

    const lanesFeatures = lanes.getSource().getFeatures();
    const polygonsFeatures = polygons.getSource().getFeatures();
    const vectorFeatures = vectors.getSource().getFeatures();

    if (lanesFeatures.length === 0 && polygonsFeatures.length === 0) {
        $('#alert_placeholder').html(`<div class="alert alert-danger alert-dismissable">
            <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
            <span>Cannot deposit without a region defined.</span>
        </div>`);
        status = true;
    }

    if (vectorFeatures.length !== 2) {
        $('#alert_placeholder').html(`<div class="alert alert-danger alert-dismissable">
            <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
            <span>Missing anchor or verified points.</span>
        </div>`);
        status = true;
    }

    try {
        for (let f = 0; f < vectorFeatures.length; f++) {
            const feature = vectorFeatures[f];
            const marker = feature.get('marker');

            if (marker?.type === "TIM") {
                const startTime = feature.get('startTime');
                const endTime = feature.get('endTime');
                const content = feature.get('content');
                const priority = feature.get('priority');
                const mutcd = feature.get('mutcd');

                if (!startTime || !endTime) {
                    $('#alert_placeholder').html(`<div class="alert alert-danger alert-dismissable">
                        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
                        <span>Set start and end time on the anchor point.</span>
                    </div>`);
                    status = true;
                }

                if ((!content || !content[0] || content[0].codes?.length === 0) && !content[0]?.text) {
                    $('#alert_placeholder').html(`<div class="alert alert-danger alert-dismissable">
                        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
                        <span>ITIS information is required.</span>
                    </div>`);
                    status = true;
                }

                if (!priority) {
                    $('#alert_placeholder').html(`<div class="alert alert-danger alert-dismissable">
                        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
                        <span>Missing priority level.</span>
                    </div>`);
                    status = true;
                }

                if (!mutcd) {
                    $('#alert_placeholder').html(`<div class="alert alert-danger alert-dismissable">
                        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
                        <span>Missing mutcd codes.</span>
                    </div>`);
                    status = true;
                }
            }
        }
    } catch (err) {
        $('#alert_placeholder').html(`<div class="alert alert-danger alert-dismissable">
            <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
            <span>Missing one or more fields on anchor or verified point.</span>
        </div>`);
        status = true;
    }

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