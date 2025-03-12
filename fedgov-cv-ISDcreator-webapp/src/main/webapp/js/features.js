import { getElevation } from "./api.js";
import { laneStyle, midMarkerStyle } from "./style.js";
import { directVincenty, inverseVincenty, toggleWidthArray } from "./utils.js";
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
						laneFeat.get("elevation").splice(j, 0, "tempValue");
						laneFeat.get("laneWidth").splice(j, 0, "tempValue");
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
							for(let c = 0; c < lanes.getSource().getFeatures().length; c++) {
								if(lanes.getSource().getFeatures()[c].get("computed") &&
									lanes.getSource().getFeatures()[c].get("referenceLaneID") == laneFeat.get("laneNumber")) {
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
					let latMatch = ((laneFeat.get("elevation")[j].latlon?.lat).toString().match(/^-?\d+(?:\.\d{0,11})?/)[0] == (latLon.lat).toString().match(/^-?\d+(?:\.\d{0,11})?/)[0]);
					let lonMatch = ((laneFeat.get("elevation")[j].latlon?.lon).toString().match(/^-?\d+(?:\.\d{0,11})?/)[0] == (latLon.lon).toString().match(/^-?\d+(?:\.\d{0,11})?/)[0]);
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

function buildDots(i, j, dot, latLon, lanes, laneMarkers){
	// Don't look at computed dots, they are handled by other functions
	let laneFeatures = lanes.getSource().getFeatures();
	if(!laneFeatures[i].get("computed")) {
    	dot.setProperties({"lane": i, "number": j, "LatLon": latLon,
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

function buildComputedFeature(i, laneNumber, referenceLaneID, referenceLaneNumber, offsetX, offsetY, rotation, scaleX, scaleY, computedLaneID, lanes, laneMarkers){

	let r = Number(referenceLaneNumber);
	let max = lanes.getSource().getFeatures()[r].getGeometry().getCoordinates().length;

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
					lanes.getSource().getFeatures()[r].getGeometry().getCoordinates()[j][0] + offsetX / 100,
					lanes.getSource().getFeatures()[r].getGeometry().getCoordinates()[j][1] + offsetY / 100
			]);
			let zeroDot = new ol.Feature(new ol.geom.Point(zeroPoint.getCoordinates()));
			zeroLonLat = ol.proj.toLonLat(zeroDot.getGeometry().getCoordinates());
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
				(lanes.getSource().getFeatures()[r].getGeometry().getCoordinates()[j][0] - lanes.getSource().getFeatures()[r].getGeometry().getCoordinates()[0][0]) * scaleX / 100;
			let deltaScaleY = 
				(lanes.getSource().getFeatures()[r].getGeometry().getCoordinates()[j][1] - lanes.getSource().getFeatures()[r].getGeometry().getCoordinates()[0][1]) * scaleY / 100;
			let tempPoint = new ol.geom.Point([
					lanes.getSource().getFeatures()[r].getGeometry().getCoordinates()[j][0] + deltaScaleX + (offsetX / 100),
					lanes.getSource().getFeatures()[r].getGeometry().getCoordinates()[j][1] + deltaScaleY + (offsetY / 100)
			]);
			let tempDot = new ol.Feature(tempPoint);
			let tempLatlon = ol.proj.toLonLat(tempDot.getGeometry().getCoordinates());
			
			// Apply rotation
			let inverse = inverseVincenty(zeroLonLat[1], zeroLonLat[0], tempLatlon[1], tempLatlon[0]);
			let direct = directVincenty(zeroLonLat[1], zeroLonLat[0],
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

	lanes.features[r].attributes.source = true;
	if(typeof lanes.features[r].attributes.newNodes !== 'undefined') {
		lanes.features[r].attributes.computedLaneCount--;
		if(lanes.features[r].attributes.computedLaneCount == 0) {
			// Remove any saved new nodes
			delete lanes.features[r].attributes.newNodes;
			delete lanes.features[r].attributes.computedLaneCount;
		}
	}
}

function buildComputedDot(i, j, laneNumber, referenceLaneID, referenceLaneNumber, dot, lonLat, offsetX, offsetY, rotation, scaleX, scaleY, computedLaneID, initialize, lanes, laneMarkers){
	if(typeof initialize === 'undefined') {
		initialize = false;
	}

	let r = Number(referenceLaneNumber);
	
	if(initialize) {
		dot.setProperties({
			"lane": i, 
			"number": j, 
			"LonLat": lonLat,
			"descriptiveName": "",
			"laneNumber": laneNumber, 
			"laneWidth": lanes.getSource().getFeatures()[r].get("laneWidth"), 
			"laneType": lanes.getSource().getFeatures()[r].get("laneType"), 
			"sharedWith": lanes.getSource().getFeatures()[r].get("sharedWith"),
			"stateConfidence": lanes.getSource().getFeatures()[r].get("stateConfidence"), 
			"spatRevision": lanes.getSource().getFeatures()[r].get("spatRevision"), 
			"signalGroupID": lanes.getSource().getFeatures()[r].get("signalGroupID"), 
			"lane_attributes": lanes.getSource().getFeatures()[r].get("lane_attributes"),
			"startTime": lanes.getSource().getFeatures()[r].get("startTime"), 
			"minEndTime": lanes.getSource().getFeatures()[r].get("minEndTime"), 
			"maxEndTime": lanes.getSource().getFeatures()[r].get("maxEndTime"),
			"likelyTime": lanes.getSource().getFeatures()[r].get("likelyTime"), 
			"nextTime": lanes.getSource().getFeatures()[r].get("nextTime"), 
			"signalPhase": lanes.getSource().getFeatures()[r].get("signalPhase"), 
			"typeAttribute": lanes.getSource().getFeatures()[r].get("typeAttribute"),
			"connections": lanes.getSource().getFeatures()[r].get("connections"), 
			"elevation": lanes.getSource().getFeatures()[r].get("elevation")[j].value,
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
			"descriptiveName": lanes.getSource().getFeatures()[i].get("descriptiveName"),
			"laneNumber": laneNumber, 
			"laneWidth": lanes.getSource().getFeatures()[i].get("laneWidth"), 
			"laneType": lanes.getSource().getFeatures()[i].get("laneType"), 
			"sharedWith": lanes.getSource().getFeatures()[i].get("sharedWith"),
			"stateConfidence": lanes.getSource().getFeatures()[i].get("stateConfidence"), 
			"spatRevision": lanes.getSource().getFeatures()[i].get("spatRevision"), 
			"signalGroupID": lanes.getSource().getFeatures()[i].get("signalGroupID"), 
			"lane_attributes": lanes.getSource().getFeatures()[i].get("lane_attributes"),
			"startTime": lanes.getSource().getFeatures()[i].get("startTime"), 
			"minEndTime": lanes.getSource().getFeatures()[i].get("minEndTime"), 
			"maxEndTime": lanes.getSource().getFeatures()[i].get("maxEndTime"),
			"likelyTime": lanes.getSource().getFeatures()[i].get("likelyTime"), 
			"nextTime": lanes.getSource().getFeatures()[i].get("nextTime"), 
			"signalPhase": lanes.getSource().getFeatures()[i].get("signalPhase"), 
			"typeAttribute": lanes.getSource().getFeatures()[i].get("typeAttribute"),
			"connections": lanes.getSource().getFeatures()[i].get("connections"),
			"computed": lanes.getSource().getFeatures()[i].get("computed"), 
			"computedLaneID": lanes.getSource().getFeatures()[i].get("computedLaneID"),
			"referenceLaneID": lanes.getSource().getFeatures()[i].get("referenceLaneID"), 
			"referenceLaneNumber": lanes.getSource().getFeatures()[i].get("referenceLaneNumber"),
			"offsetX": lanes.getSource().getFeatures()[i].get("offsetX"), 
			"offsetY": lanes.getSource().getFeatures()[i].get("offsetY"),
			"rotation": lanes.getSource().getFeatures()[i].get("rotation"),
			"scaleX": lanes.getSource().getFeatures()[i].get("scaleX"), 
			"scaleY": lanes.getSource().getFeatures()[i].get("scaleY")
		});
		
		// Elevation value depends on an array based on the number of nodes in the lane.
		// If a new vertex was added to the source lane via edit mode, then this has shifted
		// the lane's
		let elevationVal;
		if (lanes.getSource().getFeatures()[r].get("newNodes") && lanes.getSource().getFeatures()[r].get("newNodes").includes(j)) {
			// The point at this index is a new point, it does not have any saved data so default to 0
			elevationVal = 0;
		} else {
			// The point at this index is not a new point or no points were added to the source, copy elevation value directly
			elevationVal = lanes.getSource().getFeatures()[i].get("elevation")[j].value;
		}		
		dot.set("elevation", elevationVal);
	}	
    laneMarkers.getSource().addFeature(dot);
}

function connectComputedDots(i, points, initialize, lanes, laneMarkers){
	if(typeof initialize === 'undefined') {
		initialize = false;
	}

	let computedLanePoints = new ol.geom.LineString(points.map(point => point.getCoordinates()));

	if (initialize) {
		let computedLaneFeat = new ol.Feature(computedLanePoints);
		let m;
		for (let k = 0; k < laneMarkers.getSource().getFeatures().length; k++) {
			if (laneMarkers.getSource().getFeatures()[k].get("lane") == i && laneMarkers.getSource().getFeatures()[k].get("number") == 0) {
				// The first node of the matching laneMarkers
				m = k;
				break;
			}
		}

		let r = laneMarkers.getSource().getFeatures()[m].get("referenceLaneNumber");
		computedLaneFeat.setProperties({
			"connections": laneMarkers.getSource().getFeatures()[m].get("connections"),
			"elevation": lanes.getSource().getFeatures()[r].get("elevation"),
			"laneNumber": laneMarkers.getSource().getFeatures()[m].get("laneNumber"),
			"laneType": laneMarkers.getSource().getFeatures()[m].get("laneType"),
			"laneWidth": laneMarkers.getSource().getFeatures()[m].get("laneWidth"),
			"lane_attributes": laneMarkers.getSource().getFeatures()[m].get("lane_attributes"),
			"likelyTime": laneMarkers.getSource().getFeatures()[m].get("likelyTime"),
			"maxEndTime": laneMarkers.getSource().getFeatures()[m].get("maxEndTime"),
			"minEndTime": laneMarkers.getSource().getFeatures()[m].get("minEndTime"),
			"nextTime": laneMarkers.getSource().getFeatures()[m].get("nextTime"),
			"sharedWith": laneMarkers.getSource().getFeatures()[m].get("sharedWith"),
			"signalGroupID": laneMarkers.getSource().getFeatures()[m].get("signalGroupID"),
			"signalPhase": laneMarkers.getSource().getFeatures()[m].get("signalPhase"),
			"spatRevision": laneMarkers.getSource().getFeatures()[m].get("spatRevision"),
			"startTime": laneMarkers.getSource().getFeatures()[m].get("startTime"),
			"stateConfidence": laneMarkers.getSource().getFeatures()[m].get("stateConfidence"),
			"typeAttribute": laneMarkers.getSource().getFeatures()[m].get("typeAttribute"),
			"computed": laneMarkers.getSource().getFeatures()[m].get("computed"),
			"computedLaneID": laneMarkers.getSource().getFeatures()[m].get("computedLaneID"),
			"referenceLaneID": laneMarkers.getSource().getFeatures()[m].get("referenceLaneID"),
			"referenceLaneNumber": laneMarkers.getSource().getFeatures()[m].get("referenceLaneNumber"),
			"offsetX": laneMarkers.getSource().getFeatures()[m].get("offsetX"),
			"offsetY": laneMarkers.getSource().getFeatures()[m].get("offsetY"),
			"rotation": laneMarkers.getSource().getFeatures()[m].get("rotation"),
			"scaleX": laneMarkers.getSource().getFeatures()[m].get("scaleX"),
			"scaleY": laneMarkers.getSource().getFeatures()[m].get("scaleY")
		});

		// Initialize the elevations lat/lon to match the laneMarkers
		for (let l = 0; l < computedLaneFeat.get("elevation").length; l++) {
			for (let k = 0; k < laneMarkers.getSource().getFeatures().length; k++) {
				if (laneMarkers.getSource().getFeatures()[k].get("lane") == i && laneMarkers.getSource().getFeatures()[k].get("number") == l) {
					computedLaneFeat.get("elevation")[l].latlon = laneMarkers.getSource().getFeatures()[k].get("LatLon");
					break;
				}
			}
		}

		lanes.getSource().addFeature(computedLaneFeat);
	} else {
		let r = lanes.getSource().getFeatures()[i].get("referenceLaneNumber");
		for (let j = 0; j < computedLanePoints.getCoordinates().length; j++) {
			if (lanes.getSource().getFeatures()[r].get("newNodes") && lanes.getSource().getFeatures()[r].get("newNodes").includes(j)) {
				// The source lane had points added via edit and this is one of them
				let newPoint = new ol.geom.Point(computedLanePoints.getCoordinates()[j]);
				lanes.getSource().getFeatures()[i].getGeometry().insertCoordinate(j, newPoint.getCoordinates());
			} else {
				lanes.getSource().getFeatures()[i].getGeometry().setCoordinates(computedLanePoints.getCoordinates());
			}
		}
		lanes.changed();
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

        // Add semi-transparent markers at midpoints
        if (i < coordinates.length - 1) {
            const midX = (coordinates[i][0] + coordinates[i + 1][0]) / 2;
            const midY = (coordinates[i][1] + coordinates[i + 1][1]) / 2;
            const midFeature = new ol.Feature(new ol.geom.Point([midX, midY]));
            midFeature.setStyle(midMarkerStyle);
            laneMarkers.getSource().addFeature(midFeature);
        }
    }
}

export {
	onFeatureAdded,
	buildComputedFeature,
	showMarkers
}