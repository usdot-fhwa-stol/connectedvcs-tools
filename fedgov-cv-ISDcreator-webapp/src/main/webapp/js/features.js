import { getElevation } from "./api.js";
import { toggleControlsOn } from "./files.js";
import { laneStyle, pointStyle } from "./style.js";
import { directVincenty, hideRGAFields, inverseVincenty, removeSpeedForm, toggleWidthArray } from "./utils.js";
/*********************************************************************************************************************/
/**
 * Purpose: dot functions that bind the metadata to the feature object
 * @params  the feature and it's metadata
 * @event creates variables attached to the feature object and store the values
 */

function onFeatureAdded(lanes, vectors, laneMarkers, laneWidths, isLoadMap){
	laneMarkers.getSource().clear();
	let laneFeatures = lanes.getSource().getFeatures();
	for(let i = 0; i < laneFeatures.length; i++){
		let laneFeat = laneFeatures[i];
		let max = laneFeat.getGeometry().getCoordinates().length;
		if (typeof laneFeat.get("elevation") == 'undefined') {
			laneFeat.set("elevation", []);
		}
		if (typeof laneFeat.get("nodeInitialized") == 'undefined') {
			laneFeat.set("nodeInitialized", []);
		}
		if (typeof laneFeat.get("laneWidth") == 'undefined') {
			laneFeat.set("laneWidth", []);
		} else if (laneFeat.get("laneWidth").constructor !== Array) {
			// Old maps may contain laneWidth as a single value, so initialize an empty array
			laneFeat.set("laneWidth", []);
			for(let z = 0; z < max; z++) {
				laneFeat.get("laneWidth")[z] = 0;
			}
		}
		if(!laneFeat.get("computed")) 
		{
			// Loop through the lane vertices and see if there are any new dots to
			for(let j=0; j< max; j++){
				// If the laneIndex marker doesn't exist in this vertex, this is a new dot
				if (laneFeat.get("nodeInitialized").length===0 || laneFeat.get("nodeInitialized")[j] === undefined) {
					if(isLoadMap) {
						// Saved maps will not include the nodeInitialized flag.
						// Inject this flag when loading a map
						laneFeat.get("nodeInitialized").splice(j, 0, true);
					} else {
						// Insert dummy values into the laneWidth and elevation arrays so that
						// they are the correct size for the next loop through.  This is done because
						// when a new dot is found, we need to perform a call to look up elevation which
						// is an asynchronous call.  If we try to do this in a single pass over the vertices
						// the call can take too long to return and the code iterates to the next nodes
						// but a value has not yet been inserted into the elevation array causing the array
						// to be too small.
						laneFeat.get("elevation").splice(j, 0, 0);
						laneFeat.get("laneWidth").splice(j, 0, 0);
						laneFeat.get("nodeInitialized").splice(j, 0, undefined);
					}
				}
			}

			// Now run through and 
			let laneCoordinates = laneFeat.getGeometry().getCoordinates();
			for(let j=0; j< max; j++){
				// Create a dot & latlon for this line vertex
				let dot = new ol.Feature(new ol.geom.Point(laneCoordinates[j]));
				let lonLat = new ol.proj.toLonLat(laneCoordinates[j]);
				let latLon = {lat: lonLat[1], lon: lonLat[0]};

				// If the laneIndex marker doesn't exist in this vertex, this is a new dot
				if (laneFeat.get("nodeInitialized").length===0 || laneFeat.get("nodeInitialized")[j] === undefined) {
					// Insert new values for elevation and laneWidth for the new dot
					getElevation(dot, latLon, i, j, function(elev, i, j, latLon, dot){
						laneFeat.get("elevation")[j] = {'value': elev, 'edited': true, 'latlon': latLon};
					});
					laneFeat.get("laneWidth")[j] = 0;
					
					// If this is a source lane for computed lanes, record this index as a new node
					if(laneFeat.get("source")) {
						if(typeof laneFeat.get("newNodes") === 'undefined') {
							laneFeat.set("newNodes", []);							
							// Count how many computes lane exist for this source lane
							laneFeat.set("computedLaneCount", 0);
							for(let c = 0; c < laneFeatures.length; c++) {
								if(laneFeatures[c].get("computed") && laneFeatures[c].get("referenceLaneID") == laneFeat.get("laneNumber")) {
									let computedLaneCount = laneFeat.get("computedLaneCount");
									laneFeat.set("computedLaneCount", computedLaneCount++);
								}
							}
						}
						laneFeat.get("newNodes").push(j);
					}					
					// Mark the dot as being seen
					laneFeat.get("nodeInitialized").splice(j, 1, true);
				} else {
					// This node already existed
					// Compare the latitude and longitude from the existing lane values to see if the node moved
					let latMatch = ((laneFeat.get("elevation")[j]?.latlon?.lat)?.toString()?.match(/^-?\d+(?:\.\d{0,11})?/)[0] == (latLon.lat).toString().match(/^-?\d+(?:\.\d{0,11})?/)[0]);
					let lonMatch = ((laneFeat.get("elevation")[j]?.latlon?.lon)?.toString()?.match(/^-?\d+(?:\.\d{0,11})?/)[0] == (latLon.lon).toString().match(/^-?\d+(?:\.\d{0,11})?/)[0]);
					// If the node elevation has never been edited or has moved along either axis, get a new elevation value
					if (!laneFeat.get("elevation")[j]?.edited || !latMatch || !lonMatch){
						getElevation(dot, latLon, i, j, function(elev, i, j, latLon, dot){
							laneFeat.get("elevation")[j] = {'value': elev, 'edited': true, 'latlon': latLon};
						});
					}
				}
				buildDots(i, j, dot, latLon, lanes, laneMarkers);
			}
		} else {
				buildComputedFeature(i,
					laneFeat.get("laneNumber"),
					laneFeat.get("referenceLaneID"),
					laneFeat.get("referenceLaneNumber"),
					laneFeat.get("offsetX"),
					laneFeat.get("offsetY"),
					laneFeat.get("rotation"),
					laneFeat.get("scaleX"),
					laneFeat.get("scaleY"),
					laneFeat.get("computedLaneID"),
					lanes,
					laneMarkers);
		}
	};

	if (laneWidths.getSource().getFeatures().length != 0) {
		toggleWidthArray(lanes, vectors, laneWidths);
	}
}


/**
 * Function to build dots for lane features.
 * @param {number} i - Index of the lane feature.
 * @param {number} j - Index of the dot.
 * @param {Object} dot - The dot feature.
 * @param {Object} latLon - Latitude and longitude of the dot.
 * @param {Object} lanes - The lanes layer.
 * @param {Object} laneMarkers - The lane markers layer.
 */
function buildDots(i, j, dot, latLon, lanes, laneMarkers){
	// Don't look at computed dots, they are handled by other functions
	let laneFeatures = lanes.getSource().getFeatures();
	if(!laneFeatures[i].get("computed")) {
    	dot.setProperties({"lane": i, "number": j, "LonLat": latLon,
    		"descriptiveName" : laneFeatures[i].get("descriptiveName"),
        	"laneNumber": laneFeatures[i].get("laneNumber"), "laneWidth": laneFeatures[i].get("laneWidth"), "laneType": laneFeatures[i].get("laneType"), "sharedWith": laneFeatures[i].get("sharedWith"),
	        "stateConfidence": laneFeatures[i].get("stateConfidence"), "spatRevision": laneFeatures[i].get("spatRevision"), "signalGroupID": laneFeatures[i].get("signalGroupID"), "lane_attributes": laneFeatures[i].get("lane_attributes"),
    	    "startTime": laneFeatures[i].get("startTime"), "minEndTime": laneFeatures[i].get("minEndTime"), "maxEndTime": laneFeatures[i].get("maxEndTime"),
        	"likelyTime": laneFeatures[i].get("likelyTime"), "nextTime": laneFeatures[i].get("nextTime"), "signalPhase": laneFeatures[i].get("signalPhase"), "typeAttribute": laneFeatures[i].get("typeAttribute"),
	        "connections": laneFeatures[i].get("connections"), "elevation": laneFeatures[i].get("elevation")[j],
    	    "computed": laneFeatures[i].get("computed"), "source": laneFeatures[i].get("source")
	    });
	    laneMarkers.getSource().addFeature(dot);
	}
}

/**
 * Function to place a computed lane on the map.
 * @param {Object} newDotFeature - The new dot feature.
 * @param {Object} lanes - The lanes layer.
 * @param {Object} vectors - The vectors layer.
 * @param {Object} laneMarkers - The lane markers layer.
 * @param {Object} laneWidths - The lane widths layer.
 * @param {boolean} computingLane - Flag indicating if a lane is being computed.
 * @param {Object} computedLaneSource - The source of the computed lane.
 * @param {Array} laneTypeOptions - Array of lane type options.
 * @param {string} typeAttributeNameSaved - Saved type attribute name.
 * @param {Object} controls - The controls object.
 */
function placeComputedLane(newDotFeature, lanes, vectors, laneMarkers, laneWidths, computingLane, computedLaneSource, laneTypeOptions, typeAttributeNameSaved, controls) {
	let laneFeatures = lanes.getSource().getFeatures();
	let newX = newDotFeature.getGeometry().getCoordinates()[0];
	let newY = newDotFeature.getGeometry().getCoordinates()[1];
	
	// We no longer need the newDotFeature since we only needed to save it's x/y values
	// to calculate the offset from the old x/y values
	lanes.getSource().removeFeature(newDotFeature);
	
	if(computingLane) {
		// NOTE: computedLaneSource when computing a new lane is the dot 0 of
		// the source lane & not the source lane itself.  This is because
		// when setting the referenceLaneID and referenceLaneNumber, the source
		// lane does not keep those attributes, but it's dots do.
		
		// Get the offset from the first point of the source lane
	    // Note: Measurement is in meters so multiply by 100 for CM
		let offsetX = Math.round((newX - laneFeatures[computedLaneSource.get("lane")].getGeometry().getCoordinates()[0][0]) * 100);
		let offsetY = Math.round((newY - laneFeatures[computedLaneSource.get("lane")].getGeometry().getCoordinates()[0][1]) * 100);
	    let inRange = true;
	    if(offsetX > 2047 || offsetX < -2047) {
	    	alert("Current offset in X axis from source lane is " + offsetX + "cm. Offset value should be between -2047 and 2047.");
	    	inRange = false;
	    }
	    
	    if(offsetY > 2047 || offsetY < -2047) {
	    	alert("Current offset in Y axis from source lane is " + offsetY + "cm. Offset value should be between -2047 and 2047.");
	    	inRange = false;
	    }
	    
	    if(inRange) {	    	
			$("#attributes").hide();
		    $('#shared_with').multiselect('deselectAll', false);
		    $('#shared_with').multiselect('select', computedLaneSource.get("sharedWith"));
			for (let i = 0; i < laneTypeOptions.length; i++) {
				if (laneTypeOptions[i] != typeAttributeNameSaved && $('.' + laneTypeOptions[i] + '_type_attributes').length !== 0) {
					$('#' + laneTypeOptions[i] + '_type_attributes').multiselect('deselectAll', false);
					$('#' + laneTypeOptions[i] + '_type_attributes').multiselect('refresh');
				}
			}
			$('#attributes').parsley().reset();
			// Don't do anything to the connections, we want to preserve them
		    //rebuildConnections([]);
			$("#referenceLaneID").val(laneFeatures[computedLaneSource.get("lane")].get("laneNumber"));
			$("#referenceLaneNumber").val(computedLaneSource.get("lane"));
		    $('input[name=include-spat]').attr('checked',false);
		    $('.phases').hide();
		    
		    $(".selection-panel").text('Computed Lane Configuration');
		    $(".lane-info-tab").find('a:contains("Marker Info")').text('Lane Info');
			$(".lane-info-tab").find('a:contains("Approach Info")').text('Lane Info');
			$('#lane-info-tab').removeClass('active');
			$('.lane-info-tab').removeClass('active');
			$('#spat-info-tab').removeClass('active');
			$('.spat-info-tab').removeClass('active');
			$('.spat-info-tab').hide();
			$('#intersection-info-tab').removeClass('active');
			$('.intersection-info-tab').removeClass('active');
			$('.intersection-info-tab').hide();
			$('#connection-tab').removeClass('active');
			$('.connection-tab').removeClass('active');
			$('.connection-tab').hide();
			$('#computed-tab').removeClass('active');
			$('.computed-tab').removeClass('active');
			$('.computed-tab').hide();
			$("#lat").prop('readonly', false);
			$("#long").prop('readonly', false);
			$("#elev").prop('readonly', false);
			$('.btnDone').prop('disabled', false);
			$(".lane_type_attributes").hide();
			$(".lane_type_attributes btn-group").hide();
			$("label[for='lane_type_attributes']").hide();
			$(".verified_lat").hide();
			$(".verified_long").hide();
			$(".verified_elev").hide();
			$(".approach_type").hide();
			$(".intersection").hide();
			$(".region").hide();
			hideRGAFields(true);
			$('.road_authority_id').hide();
			$('.road_authority_id_type').hide();
			$(".revision").hide();
			$('.phases').hide();
			$(".master_lane_width").hide();
			$(".intersection_name").hide();
			$(".approach_name").hide();
			$(".shared_with").hide();
		    $(".btnClone").hide();
			//-------------------------------------
			$(".lat").show();
			$(".long").show();
			$(".elev").show();
			$(".spat_label").show();
			$(".lane_width").show();
			$("#lane_attributes").show();
			$(".descriptive_name").show();
			$(".lane_type").show();
			$(".lane_type_attributes").show();
			$(".lane_type_attributes btn-group").show();
			$("label[for='lane_type_attributes']").show();
			$(".lane_number").show();
			$('#lat').prop('readonly', true);
			$('#lat').val(0);
			$('#long').prop('readonly', true);
			$('#long').val(0);
		    $('.spat-info-tab').show();
		    $('.connection-tab').show();
			$('#computed-tab').addClass('active');
			$('.computed-tab').addClass('active');
		    $('.computed-tab').show();
		    $(".shared_with").show();
	
		    $("#offset-X").val(offsetX);
		    $("#offset-Y").val(offsetY);
		    $("#rotation").val(0);
		    $("#scale-X").val(0);
		    $("#scale-Y").val(0);
		    
		    $("#attributes").show();
		    
		    // Turn off the placeComputed control since the user has completed
		    // picking where they want to place the computed lane
		    toggleControlsOn("none", lanes, vectors, laneMarkers, laneWidths, false, controls);
	    }
	}
	else {		
		// NOTE: When editing an existing computed lane, the computedLaneSource
		// is the computed lane itself, not any of it's dots.
		
	    // Get the offset from the first old point of the computed lane
		let offsetX = Math.round((newX - computedLaneSource.getGeometry().getCoordinates()[0][0]) * 100);
		let offsetY = Math.round((newY - computedLaneSource.getGeometry().getCoordinates()[0][1]) * 100);

		// Combining the offsets with the computed lane's current offsets will give the
		// amount of offset from the source lane
		let offsetXFromSource = Number(computedLaneSource.get("offsetX")) + offsetX;
		let offsetYFromSource = Number(computedLaneSource.get("offsetY")) + offsetY;
	    let inRange = true;
	    if(offsetXFromSource > 2047 || offsetXFromSource < -2047) {
	    	alert("Current offset in X axis from source lane is " + offsetXFromSource + "cm. Offset value should be between -2047 and 2047.");
	    	inRange = false;
	    }
	    
	    if(offsetYFromSource > 2047 || offsetYFromSource < -2047) {
	    	alert("Current offset in Y axis from source lane is " + offsetYFromSource + "cm. Offset value should be between -2047 and 2047.");
	    	inRange = false;
	    }
	    
	    if(inRange) {
	    	// Just need to update the lane's offset values since the drawing in the UI
		    // is based off the them
			computedLaneSource.set("offsetX", offsetXFromSource);
			computedLaneSource.set("offsetY", offsetYFromSource);
			
			// Unset the source computed lane since we are done moving it
			computedLaneSource = null;
	
			// This will force a re-draw to show where the lane has moved
			onFeatureAdded(lanes, vectors, laneMarkers, laneWidths, false);
			
			// Toggle the control back to lane editing since this is what the user
			// was in when they selected the lane
			toggleControlsOn("modify", lanes, vectors, laneMarkers, laneWidths, false, controls);
		}
	}
}

/**
 * Function to build a computed feature.
 * @param {*} i Lane identifier
 * @param {*} laneNumber Lane number. 
 * @param {*} referenceLaneID Reference lane ID which is a unique index of the lane within an array of lanes.
 * @param {*} referenceLaneNumber  Reference lane Number used to uniquely identify the lane.
 * @param {*} offsetX X offset
 * @param {*} offsetY Y offset
 * @param {*} rotation Rotation angle
 * @param {*} scaleX 
 * @param {*} scaleY 
 * @param {*} computedLaneID 
 * @param {*} lanes 
 * @param {*} laneMarkers 
 */
function buildComputedFeature(i, laneNumber, referenceLaneID, referenceLaneNumber, offsetX, offsetY, rotation, scaleX, scaleY, computedLaneID, lanes, laneMarkers){

	let laneFeatures = lanes.getSource().getFeatures();
	let referenceLaneFeat = laneFeatures.find(feat=>feat.get("laneNumber") == referenceLaneID);
	let r = laneFeatures.indexOf(referenceLaneFeat);
	if(r !== Number(referenceLaneNumber)){
		//Synchronize the reference lane number with the lane index
		referenceLaneNumber = r;
		laneFeatures[i].set("referenceLaneNumber", referenceLaneNumber);
		//Update the reference lane index in the laneMarkers
		let laneMarkerFeatures = laneMarkers.getSource().getFeatures();
		for (let k = 0; k < laneMarkerFeatures.length; k++) {
			if (laneMarkerFeatures[k].get("lane") == i && laneMarkerFeatures[k].get("number") == 0) {
				laneMarkerFeatures[k].set("referenceLaneNumber", referenceLaneNumber);
				break;
			}
		}
	}
	
	let laneFeatureCoordinates = laneFeatures[r].getGeometry().getCoordinates()
	let max = laneFeatureCoordinates.length;

	let initialize = false;
	if(typeof computedLaneID === 'undefined') {
		computedLaneID = Math.random().toString(36).substr(2, 9);
		initialize = true;
	}

	let points = [];
	let zeroLonLat = "";
	for (let j = 0; j < max; j++) {
		
		if (j == 0 ){
			// Apply offset to first dot's lat/lon.  No scaling or rotation needs to be performed
			let zeroPoint = new ol.geom.Point([
					laneFeatureCoordinates[j][0] + offsetX / 100,
					laneFeatureCoordinates[j][1] + offsetY / 100
			]);
			let zeroDot = new ol.Feature(new ol.geom.Point(zeroPoint.getCoordinates()));
			let zeroCoordinates = ol.proj.toLonLat(zeroDot.getGeometry().getCoordinates());
			zeroLonLat = {lon: zeroCoordinates[0], lat: zeroCoordinates[1]};
			points.push(zeroPoint);
			buildComputedDot(i, j, laneNumber,
								referenceLaneID, referenceLaneNumber, 
								zeroDot, zeroLonLat,
								offsetX, offsetY,
								rotation,
								scaleX, scaleY,
								computedLaneID,
								initialize,
								lanes,
								laneMarkers);
		} else {
			// Apply offset & scaling to dot
			let deltaScaleX = 
				(laneFeatureCoordinates[j][0] - laneFeatureCoordinates[0][0]) * scaleX / 100;
			let deltaScaleY = 
				(laneFeatureCoordinates[j][1] - laneFeatureCoordinates[0][1]) * scaleY / 100;
			let tempPoint = new ol.geom.Point([
					laneFeatureCoordinates[j][0] + deltaScaleX + (offsetX / 100),
					laneFeatureCoordinates[j][1] + deltaScaleY + (offsetY / 100)
			]);
			let tempDot = new ol.Feature(tempPoint);
			let tempCoordinates = ol.proj.toLonLat(tempDot.getGeometry().getCoordinates());
			let tempLatlon = {lon: tempCoordinates[0], lat: tempCoordinates[1]};
			
			// Apply rotation
			let inverse = inverseVincenty(zeroLonLat.lat, zeroLonLat.lon, tempLatlon.lat, tempLatlon.lon);
			let direct = directVincenty(zeroLonLat.lat, zeroLonLat.lon,
							Number(inverse.bearing) + Number(rotation), Number(inverse.distance));
			let newPoint = new ol.geom.Point(ol.proj.fromLonLat([direct.lon, direct.lat]));
			let newDot = new ol.Feature(newPoint);
			let newLonLatCoordinates = ol.proj.toLonLat(newDot.getGeometry().getCoordinates());
			let newLonLat ={lon: newLonLatCoordinates[0], lat: newLonLatCoordinates[1]};
			
			points.push(newPoint);
			buildComputedDot(i, j, laneNumber,
				referenceLaneID, referenceLaneNumber,
				newDot, newLonLat,
				offsetX, offsetY,
				rotation,
				scaleX, scaleY,
				computedLaneID,
				initialize,
				lanes,
				laneMarkers);
		}
	}
	connectComputedDots(i, points, initialize, lanes, laneMarkers);

	laneFeatures[r].set("source", true);
	if (typeof laneFeatures[r].get("newNodes") !== 'undefined') {
		let computedLaneCount = laneFeatures[r].get("computedLaneCount") - 1;
		laneFeatures[r].set("computedLaneCount", computedLaneCount);
		if (computedLaneCount == 0) {
			// Remove any saved new nodes
			laneFeatures[r].unset("newNodes");
			laneFeatures[r].unset("computedLaneCount");
		}
	}
}

function buildComputedDot(i, j, laneNumber, referenceLaneID, referenceLaneNumber, dot, lonLat, offsetX, offsetY, rotation, scaleX, scaleY, computedLaneID, initialize, lanes, laneMarkers){
	if(typeof initialize === 'undefined') {
		initialize = false;
	}

	let r = Number(referenceLaneNumber);
	let laneFeatures = lanes.getSource().getFeatures();
	
	if(initialize) {
		dot.setProperties({
			"lane": i, 
			"number": j, 
			"LonLat": lonLat,
			"descriptiveName": "",
			"laneNumber": laneNumber, 
			"laneWidth": laneFeatures[r].get("laneWidth"), 
			"laneType": laneFeatures[r].get("laneType"), 
			"sharedWith": laneFeatures[r].get("sharedWith"),
			"stateConfidence": laneFeatures[r].get("stateConfidence"), 
			"spatRevision": laneFeatures[r].get("spatRevision"), 
			"signalGroupID": laneFeatures[r].get("signalGroupID"), 
			"lane_attributes": laneFeatures[r].get("lane_attributes"),
			"startTime": laneFeatures[r].get("startTime"), 
			"minEndTime": laneFeatures[r].get("minEndTime"), 
			"maxEndTime": laneFeatures[r].get("maxEndTime"),
			"likelyTime": laneFeatures[r].get("likelyTime"), 
			"nextTime": laneFeatures[r].get("nextTime"), 
			"signalPhase": laneFeatures[r].get("signalPhase"), 
			"typeAttribute": laneFeatures[r].get("typeAttribute"),
			"connections": laneFeatures[r].get("connections"), 
			"elevation": laneFeatures[r].get("elevation")[j].value,
			"computed": true,
			"computedLaneID": computedLaneID, 
			"referenceLaneID": referenceLaneID, 
			"referenceLaneNumber": referenceLaneNumber, 
			"offsetX": offsetX, 
			"offsetY": offsetY, 
			"rotation": rotation, 
			"scaleX": scaleX, 
			"scaleY": scaleY
		});
	} else {
		dot.setProperties({
			"lane": i, 
			"number": j, 
			"LonLat": lonLat,
			"descriptiveName": laneFeatures[i].get("descriptiveName"),
			"laneNumber": laneNumber, 
			"laneWidth": laneFeatures[i].get("laneWidth"), 
			"laneType": laneFeatures[i].get("laneType"), 
			"sharedWith": laneFeatures[i].get("sharedWith"),
			"stateConfidence": laneFeatures[i].get("stateConfidence"), 
			"spatRevision": laneFeatures[i].get("spatRevision"), 
			"signalGroupID": laneFeatures[i].get("signalGroupID"), 
			"lane_attributes": laneFeatures[i].get("lane_attributes"),
			"startTime": laneFeatures[i].get("startTime"), 
			"minEndTime": laneFeatures[i].get("minEndTime"), 
			"maxEndTime": laneFeatures[i].get("maxEndTime"),
			"likelyTime": laneFeatures[i].get("likelyTime"), 
			"nextTime": laneFeatures[i].get("nextTime"), 
			"signalPhase": laneFeatures[i].get("signalPhase"), 
			"typeAttribute": laneFeatures[i].get("typeAttribute"),
			"connections": laneFeatures[i].get("connections"),
			"computed": laneFeatures[i].get("computed"), 
			"computedLaneID": laneFeatures[i].get("computedLaneID"),
			"referenceLaneID": laneFeatures[i].get("referenceLaneID"), 
			"referenceLaneNumber": laneFeatures[i].get("referenceLaneNumber"),
			"offsetX": laneFeatures[i].get("offsetX"), 
			"offsetY": laneFeatures[i].get("offsetY"),
			"rotation": laneFeatures[i].get("rotation"),
			"scaleX": laneFeatures[i].get("scaleX"), 
			"scaleY": laneFeatures[i].get("scaleY")
		});
		
		// Elevation value depends on an array based on the number of nodes in the lane.
		// If a new vertex was added to the source lane via edit mode, then this has shifted
		// the lane's
		let elevationVal;
		if (laneFeatures[r].get("newNodes") && laneFeatures[r].get("newNodes").includes(j)) {
			// The point at this index is a new point, it does not have any saved data so default to 0
			elevationVal = 0;
		} else {
			// The point at this index is not a new point or no points were added to the source, copy elevation value directly
			elevationVal = laneFeatures[i].get("elevation")[j]?.value??-9999;
		}		
		dot.set("elevation", elevationVal);
	}	
    laneMarkers.getSource().addFeature(dot);
}

function connectComputedDots(i, points, initialize, lanes, laneMarkers){
	let laneFeatures = lanes.getSource().getFeatures();
	let laneMarkerFeatures = laneMarkers.getSource().getFeatures();
	if(typeof initialize === 'undefined') {
		initialize = false;
	}

	let computedLanePoints = new ol.geom.LineString(points.map(point => point.getCoordinates()));

	if (initialize) {
		let computedLaneFeat = new ol.Feature(computedLanePoints);
		let m;
		for (let k = 0; k < laneMarkerFeatures.length; k++) {
			if (laneMarkerFeatures[k].get("lane") == i && laneMarkerFeatures[k].get("number") == 0) {
				// The first node of the matching laneMarkers
				m = k;
				break;
			}
		}

		let r = laneMarkerFeatures[m].get("referenceLaneNumber");
		computedLaneFeat.setProperties({
			"connections": laneMarkerFeatures[m].get("connections"),
			"elevation": laneFeatures[r].get("elevation"),
			"laneNumber": laneMarkerFeatures[m].get("laneNumber"),
			"laneType": laneMarkerFeatures[m].get("laneType"),
			"laneWidth": laneMarkerFeatures[m].get("laneWidth"),
			"lane_attributes": laneMarkerFeatures[m].get("lane_attributes"),
			"likelyTime": laneMarkerFeatures[m].get("likelyTime"),
			"maxEndTime": laneMarkerFeatures[m].get("maxEndTime"),
			"minEndTime": laneMarkerFeatures[m].get("minEndTime"),
			"nextTime": laneMarkerFeatures[m].get("nextTime"),
			"sharedWith": laneMarkerFeatures[m].get("sharedWith"),
			"signalGroupID": laneMarkerFeatures[m].get("signalGroupID"),
			"signalPhase": laneMarkerFeatures[m].get("signalPhase"),
			"spatRevision": laneMarkerFeatures[m].get("spatRevision"),
			"startTime": laneMarkerFeatures[m].get("startTime"),
			"stateConfidence": laneMarkerFeatures[m].get("stateConfidence"),
			"typeAttribute": laneMarkerFeatures[m].get("typeAttribute"),
			"computed": laneMarkerFeatures[m].get("computed"),
			"computedLaneID": laneMarkerFeatures[m].get("computedLaneID"),
			"referenceLaneID": laneMarkerFeatures[m].get("referenceLaneID"),
			"referenceLaneNumber": laneMarkerFeatures[m].get("referenceLaneNumber"),
			"offsetX": laneMarkerFeatures[m].get("offsetX"),
			"offsetY": laneMarkerFeatures[m].get("offsetY"),
			"rotation": laneMarkerFeatures[m].get("rotation"),
			"scaleX": laneMarkerFeatures[m].get("scaleX"),
			"scaleY": laneMarkerFeatures[m].get("scaleY")
		});

		// Initialize the elevations lat/lon to match the laneMarkers
		for (let l = 0; l < computedLaneFeat.get("elevation").length; l++) {
			for (let k = 0; k < laneMarkerFeatures.length; k++) {
				if (laneMarkerFeatures[k].get("lane") == i && laneMarkerFeatures[k].get("number") == l) {
					computedLaneFeat.get("elevation")[l].latlon = laneMarkerFeatures[k].get("LonLat");
					break;
				}
			}
		}

		lanes.getSource().addFeature(computedLaneFeat);
	} else {
		let r = laneFeatures[i].get("referenceLaneNumber");
		for (let j = 0; j < computedLanePoints.getCoordinates().length; j++) {
			if (laneFeatures[r].get("newNodes") && laneFeatures[r].get("newNodes").includes(j)) {
				// The source lane had points added via edit and this is one of them
				let newPoint = new ol.geom.Point(computedLanePoints.getCoordinates()[j]);
				let coordinates = laneFeatures[i].getGeometry().getCoordinates();
				coordinates.splice(j, 0, newPoint.getCoordinates());
			} else {
				laneFeatures[i].getGeometry().setCoordinates(computedLanePoints.getCoordinates());
			}
		}
	}
}

// Function to add markers at main and midpoints
function showMarkers(laneFeature, laneMarkers) {
    const coordinates = laneFeature.getGeometry().getCoordinates();

    for (let i = 0; i < coordinates.length; i++) {
        // Add full opacity markers at main coordinates
        const pointFeature = new ol.Feature(new ol.geom.Point(coordinates[i]));
        pointFeature.setStyle(laneStyle);
        laneMarkers.getSource().addFeature(pointFeature);
    }
}

/**
 *@brief Move polygon to target point location
 *@param {*} polygonFeature polygon feature
 *@param {*} targetPointFeature target point feature
 */
function movePolygon(polygonFeature, targetPointFeature){
	let newCenter = targetPointFeature.getGeometry().getCoordinates();
	let oldCenter = polygonFeature.getGeometry().getInteriorPoint().getCoordinates();
	// Calculate translation distance
	let deltaX = newCenter[0] - oldCenter[0];
	let deltaY = newCenter[1] - oldCenter[1];
	// Move polygon coordinates
	polygonFeature.getGeometry().translate(deltaX, deltaY);
}

/**
 * Function to calculate geodesic (real-world) distance between two features
 * @param {*} fromPointFeature A point feature
 * @param {*} toPointFeature Target point feature
 * @returns Distance in meter between two points
 */
function getGeodesicDistance(fromPointFeature, toPointFeature) {
    let coord1 = ol.proj.toLonLat(fromPointFeature.getGeometry().getCoordinates());
    let coord2 = ol.proj.toLonLat(toPointFeature.getGeometry().getCoordinates());
    return ol.sphere.getDistance(coord1, coord2);
}

function selectComputedFeature(laneNum, laneMarkers) {
	const features = laneMarkers.getSource().getFeatures();
	for (let i = 0; i < features.length; i++) {
		const attributes = features[i].getProperties();
		if (attributes.computed && attributes.number === 0 && attributes.laneNumber === laneNum) {
			return features[i];
		}
	}
}

/**
 * Function to calculate the maximum distance (diagonal) of a square polygon
 * @param {*} polygonFeature Polygon feature
 * @returns Maximum distance in meters
 */
function getMaxSquareDistance(polygonFeature) {
    // Get the coordinates of the polygon (outer ring)
    let coordinates = polygonFeature.getGeometry().getCoordinates()[0]; // Outer ring coordinates

    // Assuming the polygon is a square, get two opposite corner points (diagonal)
    let coord1 = ol.proj.toLonLat(coordinates[0]); // First point (corner)
    let coord2 = ol.proj.toLonLat(coordinates[2]); // Opposite corner (diagonal)

    // Calculate the distance between the two diagonal points (max distance)
    let maxDistance = ol.sphere.getDistance(coord1, coord2);
    
    return maxDistance;
}

/**
 * Function to calculate the angle between two points (in radians)
 * @param {*} centerCoord center point coordinates
 * @param {*} pointCoord point coordinates
 * @returns 
 */
function calculateAngle(centerCoord, pointCoord) {
    let dx = pointCoord[0] - centerCoord[0];
    let dy = pointCoord[1] - centerCoord[1];
    return Math.atan2(dy, dx); // Angle in radians
}

/**
 * Create a point with coordinates offset(x,y) from  center feature
 * @param {*} pointId point identifier
 * @param {*} centerFeature a feature
 * @param {*} offsetX offset x from the center feature
 * @param {*} offsetY offset y from the center feature
 * @returns 
 */
function createPointFeature(pointId , centerFeature, offsetX, offsetY){
	let center = centerFeature.getGeometry().getCoordinates();
	let newCoordinate = [center[0] + offsetX, center[1] - offsetY];
	let newPoint = new ol.Feature(new ol.geom.Point(newCoordinate));
	newPoint.setStyle(pointStyle);
	newPoint.setId(pointId);
	return newPoint;
}

/**
 * Scale and rotate the polygon layer based on the center and the draggable feature
  1. **Rotation**: Calculate the angle between the center and the new outside point
  2. **Scaling**: Calculate the distance between the center and the new outside point (radius change)
 * @param {*} polygonFeature polygon feature
 * @param {*} centerFeature center feature of the polygon
 * @param {*} draggableFeature Draggable feature
 * @param {*} draggableFeatStartPosition Start position of the draggable feature
 * @param {*} startRadius start radius of the polygon
 */
function scaleAndRotatePolygon(polygonFeature, centerFeature, draggableFeature, draggableFeatStartPosition, startRadius){
	// Update polygon location while moving the center point
	let draggleFeatCoord = draggableFeature.getGeometry().getCoordinates();
	let centerCoord = centerFeature.getGeometry().getCoordinates();
	// 1. **Rotation**: Calculate the angle between the center and the draggable point
	let initAngle = Math.atan2(draggableFeatStartPosition[1] - centerCoord[1], draggableFeatStartPosition[0] - centerCoord[0]);
	let newAngle = Math.atan2(draggleFeatCoord[1] - centerCoord[1], draggleFeatCoord[0] - centerCoord[0]);
	let deltaAngle = (newAngle - initAngle);
	let scaledPolygon = polygonFeature.getGeometry().clone();
	// Rotate the polygon around its center
	scaledPolygon.rotate(deltaAngle, centerCoord);
	// 2. **Scaling**: Calculate the distance between the center and the new outside point (radius change)
	let radius = getGeodesicDistance(centerFeature, draggableFeature);     
	// Scale the polygon to maintain the radius
	scaledPolygon.scale(radius/Number(startRadius), radius/Number(startRadius), centerCoord); // Normalize the radius
	polygonFeature.setGeometry(scaledPolygon);
}


export {
	onFeatureAdded,
	buildComputedFeature,
	showMarkers,
	placeComputedLane,
	selectComputedFeature,
	movePolygon,
	getGeodesicDistance,
	getMaxSquareDistance,
	calculateAngle,
	createPointFeature,
	scaleAndRotatePolygon
}