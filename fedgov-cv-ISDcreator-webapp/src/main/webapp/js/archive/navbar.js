/**
 * Created by lewisstet on 3/20/2015.
 * Updated 3/2017 by martzth
 */

/**
 * Purpose: allow visibility of layers to be toggled
 */

function ToggleLanes(){
    if(document.getElementById("laneIcon").className == "fa fa-square-o"){
        lanes.setVisibility(true);
        laneMarkers.setVisibility(true);
        document.getElementById("laneIcon").className = "fa fa-check-square-o";
    } else {
        lanes.setVisibility(false);
        laneMarkers.setVisibility(false);
        document.getElementById("laneIcon").className = "fa fa-square-o";
    }
}

function ToggleBars(){
    if(document.getElementById("boxIcon").className == "fa fa-square-o"){
        box.setVisibility(true);
        document.getElementById("boxIcon").className = "fa fa-check-square-o";
    } else {
        box.setVisibility(false);
        document.getElementById("boxIcon").className = "fa fa-square-o";
    }
}

function TogglePoints(){
    if(document.getElementById("pointIcon").className == "fa fa-square-o"){
        vectors.setVisibility(true);
        document.getElementById("pointIcon").className = "fa fa-check-square-o";
    } else {
        vectors.setVisibility(false);
        document.getElementById("pointIcon").className = "fa fa-square-o";
    }
}

function ToggleControls(){
    if(document.getElementById("controlIcon").className == "fa fa-square-o"){
        $("#controls").show();
        document.getElementById("controlIcon").className = "fa fa-check-square-o";
    } else {
    	 $("#controls").hide();
        document.getElementById("controlIcon").className = "fa fa-square-o";
    }
}

function minimize(){	
	if ($(".minimize").html() == "x"){
		$(".minimize").html("+");
		$("footer").css("height","10px");
		document.getElementById("controlIcon").className = "fa fa-square-o";
	} else {
		$(".minimize").html("x");
		$("footer").css("height","50px");
		document.getElementById("controlIcon").className = "fa fa-check-square-o";
	}
}

$('button').tooltip();