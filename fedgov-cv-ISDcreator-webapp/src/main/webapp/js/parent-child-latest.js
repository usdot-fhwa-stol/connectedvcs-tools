/**
 * Created by martzth on 6/16/2015.
 */
import {deleteTrace, loadFile, loadUpdateFile} from "./files.js"

/**
 * Purpose: switch between child and parent maps
 * @event: the reason for this is because certain things should only be accessible by the parent or child
 * so there are functions to hide/show access to form fields
 */

let selected;

$(function() {
    $("#btn-group").children().hide();
    $(".builder").hide();
    $(".encoder").hide();
    $("#attributes").hide();
    $("#help").show();
});


function openChildMap(map, lanes,vectors, laneMarkers, laneWidths, box, errors, controls){
    selected = "child";
    if(!confirmClear(lanes, vectors, laneMarkers, laneWidths, box, errors)){
        return;
    };
    $("#map-type").text("CHILD MAP");
    $(".builder").show();
    $(".encoder").show();
    $("#intersection-tab-header").hide();
    $("#intersection-tab-header").removeClass("active");
    $("#intersection-tab").removeClass("active");
    $("#lane-tab-header").show()
    $("#lane-tab-header").addClass("active");
    $("#lane-tab").addClass("active");
    $("#builder, #drawLanes, #measureLanes, #editLanes, #drawStopBar, #editStopBar, #deleteMarker, #approachControlLabel, #laneControlLabel, #measureControlLabel").show();
    loadFile(map, lanes, vectors, laneMarkers , laneWidths, box, errors, selected, controls);
}

function openParentMap(map, lanes,vectors, laneMarkers, laneWidths, box, errors, controls){
    selected = "parent";
    if(!confirmClear(lanes, vectors, laneMarkers, laneWidths, box, errors)){
        return;
    };
    $("#map-type").text("PARENT MAP");
    $(".builder").show();
    $(".encoder").hide();
    $("#lane-tab-header").hide()
    $("#lane-tab-header").removeClass("active");
    $("#lane-tab").removeClass("active");
    $("#intersection-tab-header").show()
    $("#intersection-tab-header").addClass("active");
    $("#intersection-tab").addClass("active");
    $("#dragSigns, #deleteMarker, #builder").show();
    loadFile(map, lanes, vectors, laneMarkers, laneWidths, box, errors, selected, controls);
}

function newParentMap(lanes, vectors, laneMarkers, laneWidths, box, errors){
    selected = "parent";
    if(!confirmClear(lanes, vectors, laneMarkers, laneWidths, box, errors)){
        return;
    };
    $("#map-type").text("PARENT MAP");
    $(".builder").show();
    $(".encoder").hide();
    $("#lane-tab-header").hide()
    $("#lane-tab-header").removeClass("active");
    $("#lane-tab").removeClass("active");
    $("#intersection-tab-header").show()
    $("#intersection-tab-header").addClass("active");
    $("#intersection-tab").addClass("active");
    $("#dragSigns, #deleteMarker, #builder").show();
}

function newChildMap(map, lanes,vectors, laneMarkers, laneWidths, box, errors, controls){
    selected = "child";
    if(!confirmClear(lanes, vectors, laneMarkers, laneWidths, box, errors)){
        return;
    };
    alert("Use the file dialog to open a parent map.")
    $("#map-type").text("CHILD MAP");
    $(".builder").show();
    $(".encoder").show();
    $("#intersection-tab-header").hide();
    $("#intersection-tab-header").removeClass("active");
    $("#intersection-tab").removeClass("active");
    $("#lane-tab-header").show()
    $("#lane-tab-header").addClass("active");
    $("#lane-tab").addClass("active");
    $("#builder, #drawLanes, #measureLanes, #editLanes, #drawStopBar, #editStopBar, #deleteMarker, #approachControlLabel, #laneControlLabel, #measureControlLabel").show();
    loadFile(map, lanes,vectors, laneMarkers, laneWidths, box, errors, selected, controls);
}

function updateChildParent(map, lanes, vectors, laneMarkers, laneWidths, box, controls){
    selected = "child";
    let c = confirm("This will reset the verified point and marker. Continue?");
    if (c === true) {
        alert("Use the file dialog to select the parent map you wish to use to replace the current markers.")
        loadUpdateFile(map, lanes, vectors, laneMarkers, laneWidths, box, selected, controls);
    }
}


function confirmClear(lanes, vectors, laneMarkers, laneWidths, box, errors) {
    let lanesFeatures = lanes.getSource().getFeatures();
    let laneMarkersFeatures = laneMarkers.getSource().getFeatures();
    let vectorsFeatures = vectors.getSource().getFeatures();
    let boxFeatures = box.getSource().getFeatures();
    let r = true;
    if (lanesFeatures.length != 0 || laneMarkersFeatures.length != 0 || vectorsFeatures.length != 0 || boxFeatures.length != 0) {
        r = confirm("This will reset the map and delete any progress. Continue?");
    }

    if (r === true) {
        lanes.getSource().clear();
        laneMarkers.getSource().clear();
        vectors.getSource().clear();
        box.getSource().clear();
        laneWidths.getSource().clear();
        errors.getSource().clear();
        deleteTrace();

        $("#btn-group").children().hide();
        $(".builder").hide();
        $(".encoder").hide();
        $("#attributes").hide();
        $("#help").show();
    }
    return r;
}

export {
    openChildMap,
    openParentMap,
    newParentMap,
    newChildMap,
    updateChildParent,
    confirmClear,
    selected
}