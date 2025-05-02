/**
 * Created by martzth on 6/16/2015.
 */


/**
 * Purpose: switch between child and parent maps
 * @event: the reason for this is because certain things should only be accessible by the parent or child
 * so there are functions to hide/show access to form fields
 */

var selected;

$(function() {
    $("#btn-group").children().hide();
    $(".builder").hide();
    $(".encoder").hide();
    $("#attributes").hide();
    $("#help").show();
});


function openChildMap(){
    selected = "child";
    confirmClear();
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
    loadFile();
}

function openParentMap(){
    selected = "parent";
    confirmClear();
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
    loadFile();
}

function newParentMap(){
    selected = "parent";
    confirmClear();
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

function newChildMap(){
    selected = "child";
    confirmClear();
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
    loadFile();
}

function updateChildParent(){
    selected = "child";
    var c = confirm("This will reset the verified point and marker. Continue?");
    if (c == true) {
        alert("Use the file dialog to select the parent map you wish to use to replace the current markers.")
        loadUpdateFile();
    }
}


function confirmClear() {

    if (lanes.features.length != 0 || laneMarkers.features.length != 0 || vectors.features.length != 0 || box.features.length != 0) {
        var r = confirm("This will reset the map and delete any progress. Continue?");
    } else {
        r = true;
    }

    if (r == true) {
        lanes.destroyFeatures();
        laneMarkers.destroyFeatures();
        vectors.destroyFeatures();
        box.destroyFeatures();
        errors.clearMarkers();
        laneWidths.destroyFeatures();
        deleteTrace();

        $("#btn-group").children().hide();
        $(".builder").hide();
        $(".encoder").hide();
        $("#attributes").hide();
        $("#help").show();
    }

}

