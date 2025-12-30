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
// DEFINE GLOBAL FORMATS
import {
    lanes,
    vectors,
    laneMarkers,
    laneWidths,
    polygons,
    box,
    map,
    area,
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

const geojsonFormat = new ol.format.GeoJSON();
const fromLonLat = ol.proj.fromLonLat;
const toLonLat = ol.proj.toLonLat;


// This is what an the default array for the Directionality Heading Slices circle looks like: no directionality selected(active = false).
// Old files may be saved with an empty array which leads to the circles not being initialized correctly.
// If that is the case, initialize with the default of no directionality heading slices.
var BACKWARDS_COMPATABILITY_FOR_MISSING_CIRLCES =
    "[{\"theta\": -1.5707963267948966,\"nxtTheta\": -1.1780972450961724,\"x1\": 140,\"x2\": 140,\"x3\": 178.268343236509,\"y1\": 130,\"y2\": 30,\"y3\": 37.61204674887132,\"active\": false}," +
    "{\"theta\": -1.1780972450961724,\"nxtTheta\": -0.7853981633974483,\"x1\": 140,\"x2\": 178.268343236509,\"x3\": 210.71067811865476,\"y1\": 130,\"y2\": 37.61204674887132,\"y3\": 59.28932188134526,\"active\": false}," +
    "{\"theta\": -0.7853981633974483,\"nxtTheta\": -0.39269908169872414,\"x1\": 140,\"x2\": 210.71067811865476,\"x3\": 232.3879532511287,\"y1\": 130,\"y2\": 59.28932188134526,\"y3\": 91.73165676349103,\"active\": false}," +
    "{\"theta\": -0.39269908169872414,\"nxtTheta\": 0,\"x1\": 140,\"x2\": 232.3879532511287,\"x3\": 240,\"y1\": 130,\"y2\": 91.73165676349103,\"y3\": 130,\"active\": false}," +
    "{\"theta\": 0,\"nxtTheta\": 0.39269908169872414,\"x1\": 140,\"x2\": 240,\"x3\": 232.3879532511287,\"y1\": 130,\"y2\": 130,\"y3\": 168.26834323650897,\"active\": false}," +
    "{\"theta\": 0.39269908169872414,\"nxtTheta\": 0.7853981633974483,\"x1\": 140,\"x2\": 232.3879532511287,\"x3\": 210.71067811865476,\"y1\": 130,\"y2\": 168.26834323650897,\"y3\": 200.71067811865476,\"active\": false}," +
    "{\"theta\": 0.7853981633974483,\"nxtTheta\": 1.1780972450961724,\"x1\": 140,\"x2\": 210.71067811865476,\"x3\": 178.268343236509,\"y1\": 130,\"y2\": 200.71067811865476,\"y3\": 222.3879532511287,\"active\": false}," +
    "{\"theta\": 1.1780972450961724,\"nxtTheta\": 1.5707963267948966,\"x1\": 140,\"x2\": 178.268343236509,\"x3\": 140,\"y1\": 130,\"y2\": 222.3879532511287,\"y3\": 230,\"active\": false}," +
    "{\"theta\": 1.5707963267948966,\"nxtTheta\": 1.9634954084936207,\"x1\": 140,\"x2\": 140,\"x3\": 101.73165676349103,\"y1\": 130,\"y2\": 230,\"y3\": 222.3879532511287,\"active\": false}," +
    "{\"theta\": 1.9634954084936207,\"nxtTheta\": 2.356194490192345,\"x1\": 140,\"x2\": 101.73165676349103,\"x3\": 69.28932188134526,\"y1\": 130,\"y2\": 222.3879532511287,\"y3\": 200.71067811865476,\"active\": false}," +
    "{\"theta\": 2.356194490192345,\"nxtTheta\": 2.748893571891069,\"x1\": 140,\"x2\": 69.28932188134526,\"x3\": 47.61204674887132,\"y1\": 130,\"y2\": 200.71067811865476,\"y3\": 168.268343236509,\"active\": false}," +
    "{\"theta\": 2.748893571891069,\"nxtTheta\": 3.141592653589793,\"x1\": 140,\"x2\": 47.61204674887132,\"x3\": 40,\"y1\": 130,\"y2\": 168.268343236509,\"y3\": 130,\"active\": false}," +
    "{\"theta\": 3.141592653589793,\"nxtTheta\": 3.534291735288517,\"x1\": 140,\"x2\": 40,\"x3\": 47.61204674887131,\"y1\": 130,\"y2\": 130,\"y3\": 91.73165676349107,\"active\": false}," +
    "{\"theta\": 3.534291735288517,\"nxtTheta\": 3.9269908169872414,\"x1\": 140,\"x2\": 47.61204674887131,\"x3\": 69.28932188134523,\"y1\": 130,\"y2\": 91.73165676349107,\"y3\": 59.28932188134526,\"active\": false}," +
    "{\"theta\": 3.9269908169872414,\"nxtTheta\": 4.319689898685966,\"x1\": 140,\"x2\": 69.28932188134523,\"x3\": 101.73165676349106,\"y1\": 130,\"y2\": 59.28932188134526,\"y3\": 37.61204674887132,\"active\": false}," +
    "{\"theta\": 4.319689898685966,\"nxtTheta\": 4.71238898038469,\"x1\": 140,\"x2\": 101.73165676349106,\"x3\": 139.99999999999997,\"y1\": 130,\"y2\": 37.61204674887132,\"y3\": 30,\"active\": false}]";

/**
 * DEFINE VARIABLES NEEDED ACROSS FUNCTIONS THAT SHOULD BE PASSED DIRECTLY BUT AREN'T
 */

var calls = 0;
var filesToSend; //2019/04, MF:Added for onTraceChangeRSM()
var trace;



/**
 * Purpose: saves map object as geojson
 * @params  feature objects
 * @event compiles layer data into object
 */

function saveMap() {
    const polyFeatures = polyMarkers.getSource().getFeatures();
    const polygonFeatures = polygons.getSource().getFeatures();

    if (
        polyFeatures.length > 0 &&
        polyFeatures[0]?.get('title') === 'circle'
    ) {
        const elevation = polygonFeatures[0]?.get('elevation');
        if (elevation && elevation.length > 101) {
            polygonFeatures[0].set('elevation', elevation[101]);
        }
    }

    const layers = {
        vectors: geojsonFormat.writeFeatures(vectors.getSource().getFeatures()),
        lanes: geojsonFormat.writeFeatures(lanes.getSource().getFeatures()),
        polygons: geojsonFormat.writeFeatures(polygons.getSource().getFeatures()),
        area: geojsonFormat.writeFeatures(area.getSource().getFeatures()),
        laneMarkers: geojsonFormat.writeFeatures(laneMarkers.getSource().getFeatures()),
        polyMarkers: geojsonFormat.writeFeatures(polyMarkers.getSource().getFeatures()),
    };

    console.log('Layers:', layers);

    saveFile(layers);
}


/**
 * Purpose: saves compiled object as geojson file
 * @params  map object
 * @event saves as geojson and loads save menu option
 */

function saveFile(data) {
    const textToWrite = JSON.stringify(data, null, 2);
    const blob = new Blob([textToWrite], { type: 'application/geo+json' });
    const fileName = "TIM_message.geojson";

    const downloadLink = document.createElement("a");
    downloadLink.href = URL.createObjectURL(blob);
    downloadLink.download = fileName;

    // Only append/remove if needed
    let appended = false;
    if (!('click' in downloadLink)) {
        // Fallback if programmatic click might not work
        downloadLink.style.display = "none";
        document.body.appendChild(downloadLink);
        appended = true;
    }

    downloadLink.click();
    setTimeout(() => {
        URL.revokeObjectURL(downloadLink.href);
        if (appended && downloadLink.parentNode) {
            document.body.removeChild(downloadLink);
        }
    }, 100);
}



/**
 * Purpose: loads map objects from geojson
 * @params  saved object
 * @event rebuilds markers on map
 */

function loadMap(data) {
    const vectorFeatures = new ol.format.GeoJSON().readFeatures(
        typeof data.vectors === 'string' ? JSON.parse(data.vectors) : data.vectors,
        { featureProjection: 'EPSG:3857', dataProjection: 'EPSG:3857' }
    );

    const lanesFeatures = new ol.format.GeoJSON().readFeatures(
        typeof data.lanes === 'string' ? JSON.parse(data.lanes) : data.lanes,
        { featureProjection: 'EPSG:3857', dataProjection: 'EPSG:3857' }
    );

    const polygonsFeatures = new ol.format.GeoJSON().readFeatures(
        typeof data.polygons === 'string' ? JSON.parse(data.polygons) : data.polygons,
        { featureProjection: 'EPSG:3857', dataProjection: 'EPSG:3857' }
    );

    const areaFeatures = new ol.format.GeoJSON().readFeatures(
        typeof data.area === 'string' ? JSON.parse(data.area) : data.area,
        { featureProjection: 'EPSG:3857', dataProjection: 'EPSG:3857' }
    );

    const laneMarkersFeatures = new ol.format.GeoJSON().readFeatures(
        typeof data.laneMarkers === 'string' ? JSON.parse(data.laneMarkers) : data.laneMarkers,
        { featureProjection: 'EPSG:3857', dataProjection: 'EPSG:3857' }
    );

    const polyMarkersFeatures = new ol.format.GeoJSON().readFeatures(
        typeof data.polyMarkers === 'string' ? JSON.parse(data.polyMarkers) : data.polyMarkers,
        { featureProjection: 'EPSG:3857', dataProjection: 'EPSG:3857' }
    );


    vectors.getSource().addFeatures(vectorFeatures);
    lanes.getSource().addFeatures(lanesFeatures);
    polygons.getSource().addFeatures(polygonsFeatures);
    area.getSource().addFeatures(areaFeatures);
    laneMarkers.getSource().addFeatures(laneMarkersFeatures);
    polyMarkers.getSource().addFeatures(polyMarkersFeatures);

    // Draw circular slices
    setCirclesTemp([]);
    circles = [];
    for (let d = 0; d < 360; d += 22.5) {
        drawCircle(ctx, cx, cy, cr, "black", "white", d, circles_reset);
    }

    // Process vector features
    vectorFeatures.forEach((feature) => {
        const attrs = feature.get('marker');
        if (attrs && attrs.img_src) {
            feature.setStyle(new ol.style.Style({
                image: new ol.style.Icon({
                    src: attrs.img_src,
                    height: 50, 
                    width: 50,
                    anchor: [0.5,1],
                    anchorXUnits: 'fraction',
				    anchorYUnits: 'fraction'
                })
            }));
        }

        if (attrs?.type === 'TIM') {
            const heading = feature.get('heading') || [];
            if (heading.length === 0) {

                setCirclesTemp(JSON.parse(BACKWARDS_COMPATABILITY_FOR_MISSING_CIRLCES));
                circles = JSON.parse(BACKWARDS_COMPATABILITY_FOR_MISSING_CIRLCES);
            } else {
                setCirclesTemp(JSON.parse(JSON.stringify(heading)));
                circles = JSON.parse(JSON.stringify(heading));
            }
            drawCircleSlices(circles);
            setFeatureAttributes(feature);
        }
    });

    // Process elevation for lanes
    lanesFeatures.forEach((feature) => {
        let elev = feature.get('elevation');
        if (typeof elev === 'string') {
            const geometry = feature.getGeometry();
            const coords = geometry.getCoordinates();
            const elevList = coords.map(coord => {
                const [lon, lat] = toLonLat(coord);
                return {
                    value: elev,
                    edited: false,
                    latlon: { lat, lon }
                };
            });
            feature.set('elevation', elevList);
        }
    });

    // Process elevation for polygons
    polygonsFeatures.forEach((feature) => {
        let elev = feature.get('elevation');
        if (typeof elev === 'string') {
            const geometry = feature.getGeometry();
            const coords = geometry.getCoordinates()[0]; // outer ring
            const elevList = coords.map(coord => {
                const [lon, lat] = toLonLat(coord);
                return {
                    value: elev,
                    edited: false,
                    latlon: { lat, lon }
                };
            });
            feature.set('elevation', elevList);
        }
    });

    // Center the map view
    try {
        const firstFeature = vectorFeatures[0];
        const lonLat = firstFeature.get('LonLat');
        const center = fromLonLat([lonLat.lon, lonLat.lat]);

        let viewZoom = 18;
        const zoomCookie = getCookie("tim_zoom");
        if (zoomCookie !== "") viewZoom = parseInt(zoomCookie);

        map.getView().setCenter(center);
        map.getView().setZoom(viewZoom);
    } catch (err) {
        console.log("No vectors to reset view");
    }

    // OL v10 automatically redraws on state change
    // OL v10 automatically redraws on state change
    toggleControlsOn('modify');
    toggleControlsOn('none');
}


/**
 * Purpose: loads file
 * @params  -
 * @event clears map and then opens modal to choose file
 */

function loadFile() {
    calls = 0;

    const c = confirm("Loading a new map will clear all current work. Continue?");
    if (!c) return;

    // Clear layers
    lanes.getSource().clear();
    polygons.getSource().clear();
    laneMarkers.getSource().clear();
    polyMarkers.getSource().clear();
    vectors.getSource().clear();
    area.getSource().clear();
    radiuslayer.getSource().clear();
    laneWidths.getSource().clear();

    deleteTrace();

    // Detect IE/Edge
    const ua = window.navigator.userAgent;
    const isIE10 = ua.indexOf('MSIE ') > 0;
    const isIE11 = ua.indexOf('Trident/') > 0;
    const isEdge = ua.indexOf('Edge/') > 0;

    if (isIE10 || isIE11 || isEdge) {
        $('#open_file_modal').modal('show');
        $('#fileToLoad2').one('change', onChange);
    } else {
        $('#fileToLoad').val(""); // clear previous selection
        $('#fileToLoad').one('change', onChange);
        $('#fileToLoad').trigger('click');
    }

    $('#drawPoly').prop('disabled', false);
    $('#editPoly').prop('disabled', false);
    $('#drawLanes').prop('disabled', false);
    $('#editLanes').prop('disabled', false);
}






//03/2019 MF: Updated function name to differentiate from loadRSMTrace
function loadKMLTrace() {

    var c = confirm("Loading a new KML stencil will clear any other stencil. Continue?");
    if (c === true) {
        if (trace != undefined) {
            trace.getSource().clear();
        }

        var ua = window.navigator.userAgent;
        var msie10 = ua.indexOf('MSIE ');
        var msie11 = ua.indexOf('Trident/');
        var msie12 = ua.indexOf('Edge/');

        if (msie10 > 0 || msie11 > 0 || msie12 > 0) {
            $('#open_file_modal').modal('show');
            $('#fileToLoad2').one('change', onTraceChange); //Modal uses fileToLoad2
        }
        else {
            $('#kmlToLoad').click();
            $('#kmlToLoad').one('change', onTraceChange);
        }

    }
}


//03/2019 MF: Added new function for RSM
function loadRSMTrace() {

    var c = confirm("Loading a new RSM stencil will clear any other stencil. Continue?");
    if (c === true) {
        if (trace != undefined) {
            trace.getSource().clear();
        }

        var ua = window.navigator.userAgent;
        var msie10 = ua.indexOf('MSIE ');
        var msie11 = ua.indexOf('Trident/');
        var msie12 = ua.indexOf('Edge/');

        if (msie10 > 0 || msie11 > 0 || msie12 > 0) {
            $('#open_file_modal').modal('show');
            $('#fileToLoad2').one('change', onTraceChangeRSM); //Modal uses fileToLoad2
        }
        else {
            $('#rsmToLoad').click();
            $('#rsmToLoad').one('change', onTraceChangeRSM);
        }

    }
}

function onChange(event) {
    if (calls == 0) {
        var reader = new FileReader();
        reader.onload = onReaderLoad;
        reader.readAsText(event.target.files[0]);
    }
    calls++;
}

function onReaderLoad(event) {
    var data = JSON.parse(event.target.result);
    loadMap(data);
    $('#open_file_modal').modal('hide');
}

function onTraceChange(event) {
    var reader = new FileReader();
    reader.onload = onTraceReaderLoad;
    reader.readAsText(event.target.files[0]);
}


/*
2019/04, MF: Event to process all the RSM file, convert to KML, and load onto the map.
*/


function onTraceChangeRSM(event) {
    const filesToSend = [];

    console.log("# of files: " + event.target.files.length);

    if (event.target.files.length === 0) {
        alert("No files selected.");
        return;
    }

    const promiseArray = [];

    for (let i = 0; i < event.target.files.length; i++) {
        promiseArray.push(rsmFileReader(event.target.files[i], filesToSend));
    }

    Promise.all(promiseArray)
        .then(() => {
            console.log("All file(s) have been read.");
            callRSMWebservice(filesToSend);
        })
        .catch((err) => {
            alert("Unable to load the files, please try again. Error: " + err);
        });
}

//end onTraceChangeRSM

/*
2019/04, MF: Added to call the read each RSM file into a base64 content.
*/

function rsmFileReader(file, filesToSend) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();

        reader.onload = (e) => {
            const result = e.target.result;
            if (result && result !== "data:") {
                const fileObject = {
                    filename: file.name,
                    text: result.substring(result.indexOf(",") + 1)
                };
                filesToSend.push(fileObject);
                console.log("Loaded file:", fileObject.filename);
            }
            resolve();
        };

        reader.onerror = (err) => {
            console.error("Reader error:", err);
            reject(err);
        };

        reader.readAsDataURL(file);
    });
}

function callRSMWebservice(filesToSend) {
    if (!filesToSend || filesToSend.length === 0) {
        alert("Empty file(s), please try again.");
        return;
    }

    const webapp_root = window.location.pathname.split('/')[1];

    $.ajax({
        url: `/${webapp_root}/builder/messages/rsm_converter`,
        type: "POST",
        contentType: "text/plain",
        data: JSON.stringify({ files: filesToSend }),
        success: (result) => {
            console.log("result.successful:", result.successful);
            console.log("result.errorMessage:", result.errorMessage);

            if (result.successful) {
                addKmltoMap(result.kmlDocument); // Must be OL10-compatible
            } else {
                alert(result.errorMessage);
            }
        },
        error: (xhr, textStatus, errorThrown) => {
            try {
                if (errorThrown === "Bad Request") {
                    alert(JSON.parse(xhr.responseText).errorMessage);
                } else {
                    alert(`${errorThrown}. Please try again or contact the System Administrator.`);
                }
            } catch (e) {
                alert("Unexpected error occurred.");
            }

            console.error("Webservice Error:", textStatus, errorThrown, xhr.responseText);
        }
    });
}
/*
2019/04, MF: Added to parse the KML and add the layer to the map.
             Also added code to center the map based on the KML layer.
*/
function addKmltoMap(kmlDocument) {
let kmlParser = new ol.format.KML({
		extractStyles: false,
		extractAttributes: true,
		projection: 'EPSG:3857'
	});

	let featureList = kmlParser.readFeatures(kmlDocument, {
		featureProjection: 'EPSG:3857'
	});

    //Add KML to map
	trace = new ol.layer.Vector({
		source: new ol.source.Vector(),
		title: 'Trace Layer',
		style: new ol.style.Style({ 
			image: new ol.style.Icon({
				src: 'http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png',
				anchor: [0.5, 1],
				anchorXUnits: 'fraction',
				anchorYUnits: 'fraction'
			}),
			fill: new ol.style.Fill({
			  color: 'rgba(242, 26, 40, 0.83)' 
			}),
			stroke: new ol.style.Stroke({
			  color:  'rgba(242, 26, 40, 0.83)',
			  width: 2
			}),			
		  })
	});
	const filteredFeatures = featureList.filter(feature => {
		return feature.getGeometry().getType() !== 'GeometryCollection';
	});
	trace.getSource().addFeatures(filteredFeatures);	
	map.addLayer(trace);
    $('#open_file_modal').modal('hide');

    //Center on new layer and with current zoom
    let ft = trace.getSource().getFeatures()[0];
	let extent = ft.getGeometry().getExtent();
	const center = ol.extent.getCenter(extent);
	map.getView().setCenter(center);
    map.getView().setZoom(18);

}


// 2019/04, MF: Updated to move common code to addKMLtoMap()
function onTraceReaderLoad(event) {
    var data = event.target.result;
    addKmltoMap(data);
}

function deleteTrace() {
    if (trace !== undefined) {
        map.removeLayer(trace);
        trace.getSource().clear();
         trace = undefined;

         //Todo: create a setter function to make trace undefined 
        alert("Stencil Deleted");
    } else {
        alert("No stencil to delete.");
    }
}


function destroyClickedElement(event) {
    document.body.removeChild(event.target);
}


export {
    saveMap,
    loadMap,
    loadFile,
    loadKMLTrace,
    loadRSMTrace,
    deleteTrace
};

$(document).ready(function () {
    document.getElementById('openFile').addEventListener('click', (e) => {
        e.preventDefault();
        loadFile();
    });
});
