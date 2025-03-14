import { getCookie, unselectFeature } from "./utils.js";
import { onFeatureAdded } from "./features.js";
let isLoadMap = false;

/**
 * DEFINE VARIABLES NEEDED ACROSS FUNCTIONS THAT SHOULD BE PASSED DIRECTLY BUT AREN'T
 */

let tempSpeedLimits = [];
let loadType;
let tempLayerID;
let calls =0;
let filesToSend; 
let trace; 
let revisionNum = 0;

/**
 * Purpose: loads file
 * @params  -
 * @event clears map and then opens modal to choose file
 */

function loadFile(map, lanes, vectors, laneMarkers, laneWidths, box, errors, selected, controls) {
	let c = false;
    if (lanes.getSource().getFeatures().length != 0 || laneMarkers.getSource().getFeatures().length != 0 || vectors.getSource().getFeatures().length != 0 || box.getSource().getFeatures().length != 0) {
         c = confirm("Loading a new map will clear all current work. Continue?");
    } else {
        c = true;
    }

	loadType= "load";
	calls = 0;

   if (c === true) {
        lanes.getSource().clear();
        laneMarkers.getSource().clear();
        vectors.getSource().clear();
        box.getSource().clear();
		errors.getSource().clear();
		deleteTrace();
		laneWidths.getSource().clear();

        let ua = window.navigator.userAgent;
        let msie10 = ua.indexOf('MSIE ');
        let msie11 = ua.indexOf('Trident/');
        let msie12 = ua.indexOf('Edge/');

        if (msie10 > 0 || msie11 > 0 || msie12 > 0) {
            $('#open_file_modal').modal('show');
            $('#fileToLoad2').one('change', (event)=>{
              onChange(event,map, lanes, vectors, laneMarkers, box, laneWidths, selected, controls);
            });
        }
        else {
            $('#fileToLoad').click();
            $('#fileToLoad').one('change',  (event)=>{
              onChange(event,map, lanes, vectors, laneMarkers, box, laneWidths, selected, controls);
            });
        }
   }
}

/**
 * Handles the file input change event when loading map data
 * Creates a FileReader to read the selected file and passes the content to onReaderLoad
 * Uses the calls counter to ensure the function is executed only once per file selection
 * 
 * @param {Event} event - The change event from the file input
 * @param {ol.Map} map - OpenLayers map object to display the loaded data
 * @param {ol.layer.Vector} lanes - Vector layer for lane features
 * @param {ol.layer.Vector} vectors - Vector layer for point features
 * @param {ol.layer.Vector} laneMarkers - Vector layer for lane marker features
 * @param {ol.layer.Vector} box - Vector layer for box/polygon features
 * @param {ol.layer.Vector} laneWidths - Vector layer for lane width features
 * @param {string} selected - Current selected mode ('parent' or 'child')
 * @param {Object} controls - Map controls object containing edit/modification controls
 */
function onChange(event,map, lanes, vectors, laneMarkers, box, laneWidths, selected, controls) {
	if (calls == 0) {
		let reader = new FileReader();
		reader.onload = (event)=>{
      onReaderLoad(event,map, lanes, vectors, laneMarkers, box, laneWidths, selected, controls)
    };
		reader.readAsText(event.target.files[0]);
	}
	calls++;
}

/**
 * Processes the loaded file content, parses it as JSON, and loads it into the map
 * This function is called as the onload callback for the FileReader
 * 
 * @param {Event} event - The FileReader load event containing the file contents in event.target.result
 * @param {ol.Map} map - OpenLayers map object to display the loaded data
 * @param {ol.layer.Vector} lanes - Vector layer for lane features
 * @param {ol.layer.Vector} vectors - Vector layer for point features and markers
 * @param {ol.layer.Vector} laneMarkers - Vector layer for lane marker features
 * @param {ol.layer.Vector} box - Vector layer for box/polygon features
 * @param {ol.layer.Vector} laneWidths - Vector layer for lane width features
 * @param {string} selected - Current selected mode ('parent' or 'child')
 * @param {Object} controls - Map controls object containing edit/modification controls
 */
function onReaderLoad(event,map,lanes, vectors, laneMarkers, box, laneWidths, selected, controls){
	let data = JSON.parse(event.target.result);
	loadMap(data, map, lanes, vectors, laneMarkers, box, laneWidths, selected, controls);
	$('#open_file_modal').modal('hide');
}


/**
 * Loads map data from GeoJSON into the OpenLayers map
 * 
 * @param {Object} data - Object containing GeoJSON data for different map components
 * @param {Object} data.vectors - GeoJSON data for vector features (points, markers)
 * @param {Object} data.lanes - GeoJSON data for lane features
 * @param {Object} data.laneMarkers - GeoJSON data for lane marker features
 * @param {Object} data.box - GeoJSON data for box/polygon features
 * @param {ol.Map} map - OpenLayers map instance
 * @param {ol.layer.Vector} lanes - Vector layer for lane features
 * @param {ol.layer.Vector} vectors - Vector layer for point features
 * @param {ol.layer.Vector} laneMarkers - Vector layer for lane marker features
 * @param {ol.layer.Vector} box - Vector layer for box/polygon features
 * @param {ol.layer.Vector} laneWidths - Vector layer for lane width features
 * @param {string} selected - Current mode ('parent' or 'child')
 * @param {Object} controls - Map controls object
 */
function loadMap( data, map ,lanes, vectors, laneMarkers, box, laneWidths, selected, controls)
{
	let vectorLayerAsOL = new ol.format.GeoJSON().readFeatures(data.vectors,{featureProjection: 'EPSG:3857' , dataProjection: 'EPSG:3857'});
	let lanesLayerAsOL = new ol.format.GeoJSON().readFeatures(data.lanes,{featureProjection: 'EPSG:3857', dataProjection: 'EPSG:3857'});
	let laneMarkersLayerAsOL = new ol.format.GeoJSON().readFeatures(data.laneMarkers,{featureProjection: 'EPSG:3857', dataProjection: 'EPSG:3857'});
  	let stopLayerAsOL = new ol.format.GeoJSON().readFeatures(data.box, {featureProjection: 'EPSG:3857' , dataProjection: 'EPSG:3857'});
	let lanesSource = new ol.source.Vector({
    	features: lanesLayerAsOL,
  	});

	let vectorsSource = new ol.source.Vector({
		features: vectorLayerAsOL,
	});

	let laneMarkersSource = new ol.source.Vector({
    	features: laneMarkersLayerAsOL,
  	});
	let stopBoxSource  = new ol.source.Vector({
		features: stopLayerAsOL
	})

	// console.log(data.vectors)
	// console.log(data.lanes)
	// console.log(data.laneMarkers)
	// console.log(data.box)
	let c = false;
	if ((lanesLayerAsOL.length != 0 || laneMarkersLayerAsOL.length != 0 || stopLayerAsOL.length != 0) && selected == 'parent'){
		c = confirm("You are trying to load a child map using the parent open command. Functionality will be limited to parent controls. Continue?");
	} else {
		c = true;
	}
  
	if (c === true) {
    	vectors.setSource(vectorsSource);
		lanes.setSource(lanesSource);
		laneMarkers.setSource(laneMarkersSource);
		box.setSource(stopBoxSource);

		let feat = vectors.getSource().getFeatures();   
		for (let a = 0; a < feat.length; a++) {
			let iconAddress = feat[a].getProperties().marker.img_src;
			let IconInfo = {src: iconAddress, height: 50, width: 50, anchor: [0.5,1], anchorXUnits: 'fraction',
				anchorYUnits: 'fraction'};
			feat[a].setStyle(new ol.style.Style({
				image: new ol.style.Icon(IconInfo)
			}));
			if (feat[a].getProperties().marker.name == "Reference Point Marker") {
				let intersectionID = feat[a].getProperties().intersectionID;
				let regionID = feat[a].getProperties().regionID ? feat[a].getProperties().regionID : '';
        		console.log("intersection ID" + intersectionID + ", region ID" + regionID)
			}
		};

		let ft = lanes.getSource().getFeatures();    
		for (let i = 0; i < ft.length; i++) {
			if ( typeof ft[i].getProperties().elevation == 'string') {
				let temp = ft[i].getProperties().elevation;
        		// console.log(temp)
				ft[i].getProperties().elevation = [];
				for (let j = 0; j < ft[i].getGeometry().getCoordinates().length; j++) {
          			let latlon = new ol.proj.toLonLat(ft[i].getGeometry().getCoordinates()[j])
					ft[i].getProperties().elevation[j] = ({'value': temp, 'edited': false, 'latlon': latlon});
				}
			}
		}

		try {
			let center = new ol.proj.fromLonLat([feat[0].getProperties().LonLat.lon, feat[0].getProperties().LonLat.lat]);
			let viewZoom = 18;
			if (getCookie("isd_zoom") !== "") {
				viewZoom = getCookie("isd_zoom");
			}
			map.getView().setCenter(center);
			map.getView().setZoom(viewZoom);
			let overlayLayersGroup = new ol.layer.Group({
				title: 'Overlays',
				layers: [vectors]
			  });
			unselectFeature(map, overlayLayersGroup, feat[0]);
		}
		catch (err) {
			console.error(err)
			console.log("No vectors to reset view");
		}


		toggleControlsOn('modify', lanes, vectors, laneMarkers, laneWidths, true, controls);
		toggleControlsOn('none', lanes, vectors, laneMarkers, laneWidths, true, controls);
		console.log("loaded map")
	}

}

/**
 * Loads a KML stencil overlay onto the map
 * 
 * @param {ol.Map} map - OpenLayers map instance
 * @description Confirms with user before clearing existing stencil, handles
 * browser compatibility for file selection dialog, and processes selected KML file
 * to display as a visual trace overlay on the map.
 */
function loadKMLTrace(map) {
	let c = confirm("Loading a new KML stencil will clear any other stencil. Continue?");
	if (c === true) {
		if (trace != undefined){
			trace.getSource().clear();
		}

		let ua = window.navigator.userAgent;
		let msie10 = ua.indexOf('MSIE ');
		let msie11 = ua.indexOf('Trident/');
		let msie12 = ua.indexOf('Edge/');

		if (msie10 > 0 || msie11 > 0 || msie12 > 0){
			$('#open_file_modal').modal('show');
			$('#fileToLoad2').one('change', (event)=> onTraceChange(map, event)); //Modal uses fileToLoad2
		}
		else {
			$('#kmlToLoad').click();
			$('#kmlToLoad').one('change',  (event)=> onTraceChange(map, event));
		}

	}
}

/**
 * Loads a Roadside Message (RSM) stencil overlay onto the map
 * 
 * @description Confirms with user before clearing existing stencil, handles
 * browser compatibility for file selection dialog, processes selected RSM file
 * through conversion service, and displays result as a visual trace overlay.
 */
function loadRSMTrace() {
	let c = confirm("Loading a new RSM stencil will clear any other stencil. Continue?");
	if (c === true) {
		if (trace != undefined){
			trace.getSource().clear();
		}

		let ua = window.navigator.userAgent;
		let msie10 = ua.indexOf('MSIE ');
		let msie11 = ua.indexOf('Trident/');
		let msie12 = ua.indexOf('Edge/');

		if (msie10 > 0 || msie11 > 0 || msie12 > 0){
			$('#open_file_modal').modal('show');
			$('#fileToLoad2').one('change', (event)=> onTraceChangeRSM(map, event) ); //Modal uses fileToLoad2
		}
		else {
			$('#rsmToLoad').click();
			$('#rsmToLoad').one('change', (event)=> onTraceChangeRSM(map, event));
		}

	}
}
/**
 * Loads an update file to modify existing map data
 * 
 * @param {ol.Map} map - OpenLayers map instance
 * @param {ol.layer.Vector} lanes - Vector layer for lane features
 * @param {ol.layer.Vector} vectors - Vector layer for point features
 * @param {ol.layer.Vector} laneMarkers - Vector layer for lane markers
 * @param {ol.layer.Vector} laneWidths - Vector layer for lane width features
 * @param {ol.layer.Vector} box - Vector layer for box/polygon features
 * @param {string} selected - Current selected mode ('parent' or 'child')
 * @param {Object} controls - Map controls object
 * @description Sets loadType to "update", stores temporary data from reference point,
 * and handles browser-specific file selection for updating existing map features.
 */
function loadUpdateFile(map, lanes, vectors, laneMarkers, laneWidths, box, selected, controls) {
	loadType= "update";
	calls = 0;
	for (let a = 0; a < vectors.getSource().getFeatures().length; a++) {
		let feature = vectors.getSource().getFeatures()[a];
		if (feature.get('marker').name === "Reference Point Marker") {
			tempSpeedLimits = feature.get('speedLimitType');
			tempLayerID = feature.get('layerID');
		}
	}

	let ua = window.navigator.userAgent;
	let msie10 = ua.indexOf('MSIE ');
	let msie11 = ua.indexOf('Trident/');
	let msie12 = ua.indexOf('Edge/');

	if (msie10 > 0 || msie11 > 0 || msie12 > 0) {
		$('#open_file_modal').modal('show');
		$('#fileToLoad2').one('change', (event) => {
			onchange(event, map, lanes, vectors, laneMarkers, box, laneWidths, selected, controls)
		});
	}
	else {
		$('#fileToLoad').click();
		$('#fileToLoad').one('change', (event) => {
			onchange(event, map, lanes, vectors, laneMarkers, box, laneWidths, selected, controls)
		});
	}
}
/**
 * Manages the UI controls and tool states for map editing
 * 
 * @param {string} state - The control state to activate ('modify', 'del', 'help', etc.)
 * @param {ol.layer.Vector} lanes - Vector layer for lane features
 * @param {ol.layer.Vector} vectors - Vector layer for point features
 * @param {ol.layer.Vector} laneMarkers - Vector layer for lane marker features
 * @param {ol.layer.Vector} laneWidths - Vector layer for lane width features
 * @param {boolean} isLoadMap - Flag indicating if this is called during map loading
 * @param {Object} controls - Map controls object containing edit/modification controls
 * @description Shows help modal when state is 'help', otherwise activates the appropriate
 * control and manages feature cleanup based on the selected state.
 */
function toggleControlsOn(state, lanes, vectors, laneMarkers, laneWidths, isLoadMap, controls) {
	if( state == 'help'){
		$("#instructions_modal").modal('show');
	} else {
		$("#instructions_modal").modal('hide');
		if(controls){
			toggleControl(state, controls);
		}		
		if( state == 'modify' || state == 'del') {
			laneMarkers.getSource().clear();
			controls?.del?.getFeatures().clear();
		} else {
			onFeatureAdded(lanes, vectors, laneMarkers, laneWidths, isLoadMap);
		}
    }
}
/**
 * Controls the active state of map editing controls
 * 
 * @param {string} element - The control to activate ('modify', 'draw', 'del', etc.)
 * @param {Object} controls - Object containing all available map controls
 * @description Activates the specified control while deactivating all others.
 * Clears any measurement text when switching controls.
 */
function toggleControl(element, controls) {
	for(let key in controls) {
		let control = controls[key];
		if(element == key) {
			control.setActive(true);
		} else {
			control.setActive(false);
			$('.measurement').text('');
		}
	}
}

/**
 * Initiates the map saving process with revision control
 * 
 * @param {ol.layer.Vector} vectors - Vector layer containing map features
 * @param {ol.layer.Vector} box - Vector layer containing box/polygon features
 * @param {ol.layer.Vector} lanes - Vector layer containing lane features
 * @param {ol.layer.Vector} laneMarkers - Vector layer containing lane markers
 * @param {string} selected - Current selected mode ('parent' or 'child')
 * @description Opens revision modal for user input, validates revision number,
 * updates reference point with revision number, and compiles layers for saving.
 */
function saveMap(vectors, box, lanes, laneMarkers, selected)
{
	$('#revision_modal').modal('show')
  	let clickCounter = 0;
	$('#revision_modal .btn').click(function(event){
		if(clickCounter++ > 0){
				return;
		}

		if(($('#revision_num').val()).match(/^\d+$/)){
			let vectorsFeatures = vectors.getSource().getFeatures();
			let boxFeatures = box.getSource().getFeatures();
			let lanesFeatures = lanes.getSource().getFeatures();
			let laneMarkersFeatures = laneMarkers.getSource().getFeatures();
			for ( let f = 0; f < vectorsFeatures.length; f++) {
				if (vectorsFeatures[f].get("marker").name == "Reference Point Marker") {
						revisionNum = $('#revision_num').val();
						$('#revision').val(revisionNum);
						vectorsFeatures[f].set("revisionNum", revisionNum);
				}
			}

			let layers = {
					"vectors" : new ol.format.GeoJSON().writeFeatures(vectorsFeatures, true),
					"box" :new ol.format.GeoJSON().writeFeatures(boxFeatures, true),
					"lanes" : new ol.format.GeoJSON().writeFeatures(lanesFeatures, true),
					"laneMarkers" : new ol.format.GeoJSON().writeFeatures(laneMarkersFeatures, true)
				};
				saveFile( layers, vectors, selected );
		} else {
			alert("Must enter a number value.");
			return false;
		}
	});
}

/**
 * Purpose: saves compiled object as geojson file
 * @params  map object
 * @event saves as geojson and loads save menu option
 */

function saveFile( data, vectors, selected )
{
	let textToWrite = JSON.stringify( data );
	let textFileAsBlob = new Blob([textToWrite], {type:'text/plain'});

	let fileNameToSaveAs;
	let referenceExists = false;
	let vectorsFeatures = vectors.getSource().getFeatures();
	let intersectionID = 0;
	for ( let f = 0; f < vectorsFeatures.length; f++) {
		let feature = vectorsFeatures[f];
		if (feature.get("marker").name == "Reference Point Marker") {
			if (feature.get("intersectionID")){
				referenceExists = true;
				intersectionID = feature.get("intersectionID");
			}
		}
	}

	if (!referenceExists){
		alert("Cannot save parent without valid and saved reference point");
		return;
	}

	if (selected == "parent") {
		fileNameToSaveAs = "ISD_" + intersectionID + "_parent_r" + revisionNum + ".geojson"
	}
	if (selected == "child"){
		fileNameToSaveAs = "ISD_" + intersectionID + "_child_r" + revisionNum + ".geojson"
	}

	let downloadLink = document.createElement("a");
	downloadLink.download = fileNameToSaveAs;
	downloadLink.innerHTML = "Download File";
	if (window.webkitURL != null){
		// Chrome allows the link to be clicked
		// without actually adding it to the DOM.
		downloadLink.href = window.webkitURL.createObjectURL(textFileAsBlob);
	}
	else{
		// Firefox requires the link to be added to the DOM
		// before it can be clicked.
		try {
			downloadLink.href = window.URL.createObjectURL(textFileAsBlob);
			downloadLink.onclick = destroyClickedElement;
			downloadLink.style.display = "none";
			document.body.appendChild(downloadLink);
		}
		catch( e ){
			console.log("error saving firefox file")
		}
		// IE 10+
		try {
			window.navigator.msSaveBlob(textFileAsBlob, fileNameToSaveAs);
		}
		catch(e){
			console.log("error saving IE file")
		}
	}

	try {
		downloadLink.click();
	}
	catch(e) {
		console.log("Unable to click the download link.  Are you using IE?")
	}
        
	if(document.body.contains(downloadLink)){
			document.body.removeChild(downloadLink);
	}	
}
/**
 * Removes any existing trace overlay from the map
 * 
 * @description Checks if there is an active trace layer, clears its features and
 * undefines the trace variable if it exists, and provides user feedback via alerts
 * regardless of whether a trace existed.
 */
function deleteTrace() {
	if (trace != undefined){
		trace.getSource().clear();
		trace = undefined;
		alert("Stencil Deleted");
	}
	else{
	    alert("No stencil to delete.")
	}
}

/**
 * Removes a temporary DOM element after it has been used
 * 
 * @param {Event} event - The click event containing the target element to remove
 * @description Utility function used during file download operations to
 * clean up temporary DOM elements after they have served their purpose.
 */
function destroyClickedElement(event){
	document.body.removeChild(event.target);
}

/**
 * Handles the file input event when loading KML trace files
 * 
 * @param {ol.Map} map - OpenLayers map instance
 * @param {Event} event - The file input change event
 * @description Creates a FileReader to asynchronously read the selected KML file
 * and passes the content to the onTraceReaderLoad function for processing.
 */
function onTraceChange(map, event) {
	let reader = new FileReader();
	reader.onload = (event) => { onTraceReaderLoad(map, event) };
	reader.readAsText(event.target.files[0]);
}

/**
 * Handles the file input event when loading RSM (Roadside Message) files
 * 
 * @param {ol.Map} map - OpenLayers map instance
 * @param {Event} event - The file input change event
 * @description Processes one or more RSM files by reading their contents as base64 data,
 * creates an array of promises to handle asynchronous loading, and when all files
 * are processed, calls the RSM conversion web service to generate KML for display.
 */
function onTraceChangeRSM (map, event) {
    filesToSend = [];
    console.log("# of files: " + event.target.files.length);

    if (event.target.files.length == 0)
    {
        alert("No files selected.");
        return;
    }

    //Need to have a list of promises per file, to call Promise.all, which process each file sequentially
	let promiseArray = [];
	
    //Loop thru each file and process file/blobs data encoded as a data URL, base64 encoding
    for (let i= 0; i < event.target.files.length; i++)
    {
        promiseArray.push( rsmFileReader(event.target.files[i]) );
    }

    //Need to process using Promise.all, because FileReader is asynchronous
    //Wait for all files to be read into the filesToSend, before calling the "callRSMWebservice"
    Promise.all(promiseArray).then(values => {
      console.log("All file(s) have been read.");
      callRSMWebservice(map, filesToSend);
    })
    .catch((err)=> {
         alert("Unable to load the files, please try again. Error: " + err);
    });

}

/*
2019/04, MF: Added to call the read each RSM file into a base64 content.
*/
function rsmFileReader(file){
    //Need to create a promise per each file processed.
    return new Promise ((resolve, reject) => {
        let reader = new FileReader();

        //Define the onload of the FileReader, which is asynchronous
        reader.onload = function (theFile) {
           return function(e){

                if (e.target.result != null
                    && e.target.result != undefined
                    && e.target.result != "data:" //meaning empty file(s)
                    )
                {
                  //Create an object with specific properties
                  let fileObject = {};
                  fileObject.filename = theFile.name;
                  fileObject.text = e.target.result.substr(e.target.result.indexOf(",")+1,e.target.result.length);

                  //Append each fileObject to the List
                  filesToSend.push(fileObject);
                  console.log ("fileObject.filename: " + fileObject.filename);
                }
                resolve(); //end promise with a success
           };

        }(file);//end onload

        reader.onerror = function (err){
            console.error("reader.error: " + err);
            reject(err);
        };
        //Read each file as a data URL as base64
        reader.readAsDataURL(file); //calls the reader.onLoad() defined above
    });
}

/*
2019/04, MF: Added to call the webservice to convert the RSM files (base64 content) to KML
*/
function callRSMWebservice(map, filesToSend){
   if ( filesToSend == null ||
        filesToSend == undefined ||
	   filesToSend.length == 0){
        alert("Empty file(s), please try again.");
	   	return;
   }

	let webappRoot = window.location.pathname.split('/')[1];
   	$.ajax({
       url: "/" + webappRoot + "/builder/messages/rsm_converter",
       type: "POST",
       contentType: "text/plain",
       data: JSON.stringify({'files': filesToSend}),
       success: function (result){
           console.log ("result.successful:" + result.successful);
           console.log ("result.errorMessage:" + result.errorMessage);
           /*
            //Keep for DEBUGGING: Add "<div id = "results"> <div id = "inputText">" to index.html
            $("#inputText").empty();
            $("#results").empty();
            $("#inputText").append(JSON.stringify({'files': filesToSend}));
            $("#results").append(result.successful  + "<br/>" + result.errorMessage+ "<br/>" + result.kmlDocument);
           */

            if (result.successful){
                addKmltoMap(map, result.kmlDocument);
            }
            else{
                alert(result.errorMessage);
            }
       },
       error: function (XMLHttpRequest, textStatus, errorThrown) {
            if (errorThrown=="Bad Request") {
                alert(JSON.parse(XMLHttpRequest.responseText).errorMessage);
            }
            else{
                alert(errorThrown + " . Please try again or contact the System Administrator.");
            }
            //Keep for DEBUGGING:
            console.error("Webservice Error: " + textStatus + "; " +  errorThrown +  "; "  + XMLHttpRequest.responseText );
            //$("#results").append(XMLHttpRequest.responseText + "; " + textStatus + "; " + errorThrown).append("<br/>");
       }
   })
};
/**
 * Processes and displays KML data as an overlay on the map
 * 
 * @param {ol.Map} map - OpenLayers map instance
 * @param {string} kmlDocument - KML document content as a string
 * @description Parses the KML document using OpenLayers KML format parser,
 * creates a new vector layer with custom styling for the trace overlay,
 * adds the KML features to this layer, centers the map view on the new features,
 * and closes any open file dialog.
 */
function addKmltoMap(map, kmlDocument)
{
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
			fill: new ol.style.Fill({
			  color: 'rgba(241, 146, 12, 0.83)' 
			}),
			stroke: new ol.style.Stroke({
			  color:  'rgba(242, 127, 26, 0.83)',
			  width: 2
			})
		  })
	});
	trace.getSource().addFeatures(featureList);
	map.addLayer(trace);
    $('#open_file_modal').modal('hide');

    //Center on new layer and with current zoom
    let ft = trace.getSource().getFeatures()[0];
	let extent = ft.getGeometry().getExtent();
	map.getView().fit(extent, { duration: 1000 });
}

/**
 * Processes the loaded KML trace file and adds it to the map
 * 
 * @param {ol.Map} map - OpenLayers map instance
 * @param {Event} event - FileReader load event containing the KML content
 * @description Extracts the KML data from the FileReader result and passes it to
 * addKmltoMap function for processing and display as a visual trace overlay.
 */
function onTraceReaderLoad(map, event){
	let data = event.target.result;
	addKmltoMap(map, data);
}


/**
 * Purpose: misc functions that may not be used?
 * to be @deprecate?
 */

function changeLatLonName(name){
	if (name < 0){
		name = 0xFFFFFFFF + name + 1
	}

	name = name.toString(16).toUpperCase();
	name = parseInt(name, 16).toString(2);
	name = name.substr(name.length -20);
	name = name.substr(0, 13);

	return name;
}

function changeElevName(name){
	if (name < 0){
		name = 0xFFFFFFFF + name + 1
	}

	name = name.toString(16).toUpperCase();

	if (name.length == 1 || name.length == 3){
		name = "0" + name
	}

	name = parseInt(name, 16).toString(2);
	name = name.substr(name.length -7);
	name = name.substr(0, 6);

	return name;
}


export {
    loadFile,
    loadMap,
	saveFile,
	saveMap,
	toggleControlsOn,
	toggleControl,
	revisionNum,
	loadKMLTrace,
	loadRSMTrace,
	loadUpdateFile,
	deleteTrace
}