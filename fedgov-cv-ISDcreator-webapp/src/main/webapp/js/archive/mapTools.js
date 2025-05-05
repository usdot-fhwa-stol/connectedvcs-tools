/**
 * Created by lewisstet on 2/25/2015.
 * Updated 3/2017 by martzth
 */


/**
 * DEFINE GLOBAL VARIABLES
 */

	var GEOJSON_PARSER = new OpenLayers.Format.GeoJSON();

	var isLoadMap = false;

/**
 * DEFINE VARIABLES NEEDED ACROSS FUNCTIONS THAT SHOULD BE PASSED DIRECTLY BUT AREN'T
 */

	var tempSpeedLimits = [];
	var loadType;
	var tempLayerID;
	var calls =0;
	var filesToSend; //2019/04, MF:Added for onTraceChangeRSM()
	var trace; //2019/04, MF:Added since it was not declared.

/**
 * Purpose: saves map object as geojson
 * @params  feature objects
 * @event compiles layer data into object
 */

function saveMap()
{
	$('#revision_modal').modal('show')
        let clickCounter = 0;
	$('#revision_modal .btn').click(function(event){
                if(clickCounter++ > 0){
                    return;
                }
		if(($('#revision_num').val()).match(/^\d+$/)){

		    for ( var f = 0; f < vectors.features.length; f++) {
		        if (vectors.features[f].attributes.marker.name == "Reference Point Marker") {
					revisionNum = $('#revision_num').val();
					vectors.features[f].attributes.revisionNum = revisionNum;
		        }
		    }

			var layers = {
					"vectors" : GEOJSON_PARSER.write(vectors.features, true),
					"box" : GEOJSON_PARSER.write(box.features, true),
					"lanes" : GEOJSON_PARSER.write(lanes.features, true),
					"laneMarkers" : GEOJSON_PARSER.write(laneMarkers.features, true)
				};

				saveFile( layers );
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

function saveFile( data )
{
	var textToWrite = JSON.stringify( data );
	var textFileAsBlob = new Blob([textToWrite], {type:'text/plain'});

	var fileNameToSaveAs;
	var referenceExists = false;

	for ( var f = 0; f < vectors.features.length; f++) {
		var feature = vectors.features[f];
		if (feature.attributes.marker.name == "Reference Point Marker") {
			if (feature.attributes.intersectionID){
				referenceExists = true;
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

	var downloadLink = document.createElement("a");
	downloadLink.download = fileNameToSaveAs;
	//downloadLink.innerHTML = "Download File";
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
 * Purpose: loads map objects from geojson
 * @params  saved object
 * @event rebuilds markers on map
 */

function loadMap( data )
{
	isLoadMap = true;

	var vectorLayerAsOL = GEOJSON_PARSER.read(data.vectors);
	var stopLayerAsOL = GEOJSON_PARSER.read(data.box);
	var lanesLayerAsOL = GEOJSON_PARSER.read(data.lanes);
	var laneMarkersLayerAsOL = GEOJSON_PARSER.read(data.laneMarkers);
	if ((lanesLayerAsOL.length != 0 || laneMarkersLayerAsOL.length != 0 || stopLayerAsOL.length != 0) && selected == 'parent'){
		var c = confirm("You are trying to load a child map using the parent open command. Functionality will be limited to parent controls. Continue?");
	} else {
		c = true;
	}

	if (c == true && loadType == "load") {
		vectors.addFeatures(vectorLayerAsOL);
		box.addFeatures(stopLayerAsOL);
		lanes.addFeatures(lanesLayerAsOL);
		laneMarkers.addFeatures(laneMarkersLayerAsOL);

		var feat = vectors.features;
		for (var a = 0; a < feat.length; a++) {
			var iconAddress = feat[a].attributes.marker.img_src;
			feat[a].style = {externalGraphic: iconAddress, graphicHeight: 50, graphicWidth: 50, graphicYOffset: -50};
			if (vectors.features[a].attributes.marker.name == "Reference Point Marker") {
				intersectionID = vectors.features[a].attributes.intersectionID;
				regionID = vectors.features[a].attributes.regionID ? vectors.features[a].attributes.regionID : '';
			}
		}
		;

		var ft = lanes.features;
		for (var i = 0; i < ft.length; i++) {
			if (typeof lanes.features[i].attributes.elevation == 'string') {
				var temp = lanes.features[i].attributes.elevation;
				lanes.features[i].attributes.elevation = [];
				for (j = 0; j < lanes.features[i].geometry.getVertices().length; j++) {
					var dot = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Point(lanes.features[i].geometry.getVertices()[j].x, lanes.features[i].geometry.getVertices()[j].y));
					var latlon = new OpenLayers.LonLat(dot.geometry.x, dot.geometry.y).transform(toProjection, fromProjection);
					lanes.features[i].attributes.elevation[j] = ({'value': temp, 'edited': false, 'latlon': latlon});
				}
			}
		}

		try {
			var center = new OpenLayers.LonLat(feat[0].attributes.LonLat.lon, feat[0].attributes.LonLat.lat);
			center.transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());
			var view_zoom = 18;
			if (getCookie("isd_zoom") !== "") {
				view_zoom = getCookie("isd_zoom");
			}
			map.setCenter(center, view_zoom);
			unselectFeature(feat[0])
		}
		catch (err) {
			console.log("No vectors to reset view");
		}

		vectors.redraw();

		$("#dragSigns").click();
		$("#dragSigns").click();

		toggleControlsOn('modify');
		toggleControlsOn('none');
	} else if( loadType == "update"){
		vectors.destroyFeatures();
		vectors.addFeatures(vectorLayerAsOL);

		var feat = vectors.features;
		for (var a = 0; a < feat.length; a++) {
			var iconAddress = feat[a].attributes.marker.img_src;
			feat[a].style = {externalGraphic: iconAddress, graphicHeight: 50, graphicWidth: 50, graphicYOffset: -50};
			if (vectors.features[a].attributes.marker.name == "Reference Point Marker") {
				intersectionID = vectors.features[a].attributes.intersectionID;
				vectors.features[a].attributes.speedLimitType = tempSpeedLimits;
				vectors.features[a].attributes.layerID = tempLayerID;
				regionID = vectors.features[a].attributes.regionID ? vectors.features[a].attributes.regionID : '';
			}
		}

		try {
			var center = new OpenLayers.LonLat(feat[0].attributes.LonLat.lon, feat[0].attributes.LonLat.lat);
			center.transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());
			var view_zoom = 18;

			if (getCookie("isd_zoom") !== "") {
				view_zoom = getCookie("isd_zoom");
			}

			map.setCenter(center, view_zoom);
			unselectFeature(feat[0])
		}
		catch (err) {
			console.log("No vectors to reset view");
		}

		vectors.redraw();

		$("#dragSigns").click();
		$("#dragSigns").click();

		toggleControlsOn('modify');
		toggleControlsOn('none');
	}

	isLoadMap = false;
}


/**
 * Purpose: loads file
 * @params  -
 * @event clears map and then opens modal to choose file
 */

function loadFile() {
    if (lanes.features.length != 0 || laneMarkers.features.length != 0 || vectors.features.length != 0 || box.features.length != 0) {
        var c = confirm("Loading a new map will clear all current work. Continue?");
    } else {
        c = true;
    }

	loadType= "load";
	calls = 0;

    if (c == true) {
        lanes.destroyFeatures();
        laneMarkers.destroyFeatures();
        vectors.destroyFeatures();
        box.destroyFeatures();
		errors.clearMarkers();
		deleteTrace();
		laneWidths.destroyFeatures();

        var ua = window.navigator.userAgent;
        var msie10 = ua.indexOf('MSIE ');
        var msie11 = ua.indexOf('Trident/');
        var msie12 = ua.indexOf('Edge/');

        if (msie10 > 0 || msie11 > 0 || msie12 > 0) {
            $('#open_file_modal').modal('show');
            $('#fileToLoad2').one('change', onChange);
        }
        else {
            $('#fileToLoad').click();
            $('#fileToLoad').one('change', onChange);
        }
    }
}
//03/2019 MF: Updated function name to differentiate from loadRSMTrace
function loadKMLTrace() {

	var c = confirm("Loading a new KML stencil will clear any other stencil. Continue?");
	if (c === true) {
		if (trace != undefined){
			trace.destroy();
		}

		var ua = window.navigator.userAgent;
		var msie10 = ua.indexOf('MSIE ');
		var msie11 = ua.indexOf('Trident/');
		var msie12 = ua.indexOf('Edge/');

		if (msie10 > 0 || msie11 > 0 || msie12 > 0){
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
		if (trace != undefined){
			trace.destroy();
		}

		var ua = window.navigator.userAgent;
		var msie10 = ua.indexOf('MSIE ');
		var msie11 = ua.indexOf('Trident/');
		var msie12 = ua.indexOf('Edge/');

		if (msie10 > 0 || msie11 > 0 || msie12 > 0){
			$('#open_file_modal').modal('show');
			$('#fileToLoad2').one('change', onTraceChangeRSM); //Modal uses fileToLoad2
		}
		else {
			$('#rsmToLoad').click();
			$('#rsmToLoad').one('change', onTraceChangeRSM);
		}

	}
}

function loadUpdateFile(){

	loadType= "update";
	calls = 0;
	for (var a = 0; a < vectors.features.length; a++) {
		if (vectors.features[a].attributes.marker.name == "Reference Point Marker") {
			tempSpeedLimits = vectors.features[a].attributes.speedLimitType;
			tempLayerID = vectors.features[a].attributes.layerID;
		}
	}

	var ua = window.navigator.userAgent;
	var msie10 = ua.indexOf('MSIE ');
	var msie11 = ua.indexOf('Trident/');
	var msie12 = ua.indexOf('Edge/');

	if (msie10 > 0 || msie11 > 0 || msie12 > 0) {
		$('#open_file_modal').modal('show');
		$('#fileToLoad2').one('change', onChange);
	}
	else {
		$('#fileToLoad').click();
		$('#fileToLoad').one('change', onChange);
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

function onReaderLoad(event){
	var data = JSON.parse(event.target.result);
	loadMap( data, loadType );
	$('#open_file_modal').modal('hide');
}

function destroyClickedElement(event){
	document.body.removeChild(event.target);
}

function onTraceChange(event) {
	var reader = new FileReader();
	reader.onload = onTraceReaderLoad;
	reader.readAsText(event.target.files[0]);
}

/*
2019/04, MF: Event to process all the RSM file, convert to KML, and load onto the map.
*/
function onTraceChangeRSM (event) {

    //var filesToSend = new Array();
    filesToSend = [];

    console.log("# of files: " + event.target.files.length);

    if (event.target.files.length == 0)
    {
        aler("No files selected.");
        return;
    }

    //Need to have a list of promises per file, to call Promise.all, which process each file sequentially
    var promiseArray = [];

    //Loop thru each file and process file/blobs data encoded as a data URL, base64 encoding
    for (var i= 0; i < event.target.files.length; i++)
    {
        promiseArray.push( rsmFileReader(event.target.files[i]) );
    }

    //Need to process using Promise.all, because FileReader is asynchronous
    //Wait for all files to be read into the filesToSend, before calling the "callRSMWebservice"
    Promise.all(promiseArray).then(values => {
      console.log("All file(s) have been read.");
      callRSMWebservice(filesToSend);
    })
    .catch((err)=> {
         alert("Unable to load the files, please try again. Error: " + err);
    });

}//end onTraceChangeRSM

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
                  var fileObject = {};
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
            console.log("reader.error: " + err);
            reject(err);
        };
        //Read each file as a data URL as base64
        reader.readAsDataURL(file); //calls the reader.onLoad() defined above
    });
}

/*
2019/04, MF: Added to call the webservice to convert the RSM files (base64 content) to KML
*/
function callRSMWebservice(filesToSend){

   if ( filesToSend == null ||
        filesToSend == undefined ||
        filesToSend.length == 0)
   {
        alert("Empty file(s), please try again.");
        return;
   }

   webapp_root = window.location.pathname.split( '/' )[1]

   $.ajax({
       url: "/" + webapp_root + "/builder/messages/rsm_converter",
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
                //Add KML to map
                addKmltoMap(result.kmlDocument);
            }
            else{
                //Show error to the user
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
            console.log("Webservice Error: " + textStatus + "; " +  errorThrown +  "; "  + XMLHttpRequest.responseText );
            //$("#results").append(XMLHttpRequest.responseText + "; " + textStatus + "; " + errorThrown).append("<br/>");
       }
   })
};

/*
2019/04, MF: Added to parse the KML and add the layer to the map.
             Also added code to center the map based on the KML layer.
*/
function addKmltoMap(kmlDocument)
{
    var kmlParser = new OpenLayers.Format.KML({
        extractStyles: true,
        extractAttributes: true,
        internalProjection: new OpenLayers.Projection("EPSG:3857"),
        externalProjection: new OpenLayers.Projection("EPSG:4326")
    });

    var feature_list = kmlParser.read(kmlDocument);

    //Add KML to map
    trace = new OpenLayers.Layer.Vector("Trace");
    trace.addFeatures(feature_list);
    map.addLayer(trace);
    $('#open_file_modal').modal('hide');

    //Center on new layer and with current zoom
    var ft = trace.features[0];
    var bounds = ft.geometry.bounds;
    map.setCenter(bounds.getCenterLonLat(),map.getZoom());
    //console.log("map.getZoom(): " + map.getZoom());
}

// 2019/04, MF: Updated to move common code to addKMLtoMap()
function onTraceReaderLoad(event){
	var data = event.target.result;
	addKmltoMap(data);
}

function deleteTrace(){
	if (trace != undefined){
		trace.destroy();
		trace = undefined;
		alert("Stencil Deleted");
	}
	else{
	    alert("No stencil to delete.")
	}
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
