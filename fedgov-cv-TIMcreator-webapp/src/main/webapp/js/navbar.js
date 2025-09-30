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

import
{
    lanes,
    polygons,
    polyMarkers,
    laneMarkers,
    vectors,
    area

}  from './mapping.js';

export function ToggleLanes(){
    if(document.getElementById("laneIcon").className == "fa fa-square-o"){
        lanes.setVisible(true);
        area.setVisible(true);
        laneMarkers.setVisible(true);
        polygons.setVisible(true);
        polyMarkers.setVisible(true);
        document.getElementById("laneIcon").className = "fa fa-check-square-o";
    } else {
        area.setVisible(false);
        lanes.setVisible(false);
        laneMarkers.setVisible(false);
        polygons.setVisible(false);
        polyMarkers.setVisible(false);
        document.getElementById("laneIcon").className = "fa fa-square-o";
    }
}

// function ToggleBars(){
//     if(document.getElementById("boxIcon").className == "fa fa-square-o"){
//         box.setVisibility(true);
//         document.getElementById("boxIcon").className = "fa fa-check-square-o";
//     } else {
//         box.setVisibility(false);
//         document.getElementById("boxIcon").className = "fa fa-square-o";
//     }
// }

export function TogglePoints(){
    if(document.getElementById("pointIcon").className == "fa fa-square-o"){
        vectors.setVisible(true);
        document.getElementById("pointIcon").className = "fa fa-check-square-o";
    } else {
        vectors.setVisible(false);
        document.getElementById("pointIcon").className = "fa fa-square-o";
    }
}

export function ToggleControls(){
    if(document.getElementById("controlIcon").className == "fa fa-square-o"){
        $("#controls").show();
        document.getElementById("controlIcon").className = "fa fa-check-square-o";
    } else {
    	 $("#controls").hide();
        document.getElementById("controlIcon").className = "fa fa-square-o";
    }
}

export function minimize(){	
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