/**
 * Created by lewisstet on 3/20/2015.
 * Updated 3/2017 by martzth
 */

/**
 * Purpose: show/hide sidebar sliding in from left
 */

function toggle() {
    var divPosition = $("#sidebar").offset();
    if(divPosition.left < 0){
        $("sidebar").show();
        $("#sidebar").animate({"left":12},1000);
    } else {
        $("#sidebar").animate({"left":-800},1000);
        $("sidebar").hide();
        toggleControlsOn('none');
        laneConnections.removeAllFeatures();
    }
}