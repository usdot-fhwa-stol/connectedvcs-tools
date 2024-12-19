/**
 * Created by lewisstet on 2/25/2015.
 * Updated 2/2017 by martzth
 */


/**
 * DEFINE GLOBAL VARIABLES
 */
    var hidden_drag, intersection_sidebar, deleteMode, currentControl;
    var tmp_lane_attributes = {}
    var $imgs
    var numRows = -1;
    var rowHtml;
    var speedLimits = [];
    var speedForm;

/**
 * Define functions that must bind on load
 */

$(document).ready(function() {

    hidden_drag = $('#hidden-drag');
    intersection_sidebar = $('#sidebar');

    /***
     * Purpose: Autocomplete for allowing place search
     * @params Address input box
     * @event places API from google -> set cookie and move map to location
     */
    let search_input = $("#address-search");
    search_input.keyup((event)=>{
        if(event.key==="ArrowDown" || event.key==="ArrowLeft" || event.key==="ArrowRight" || event.key==="ArrowUp"){
            return;
        }
        let search_result_dropdown=$("#dropdown-menu-search");
        search_result_dropdown.empty();
        let inputText = event.target.value;
        if(inputText?.length>0){
            populateAutocompleteSearchPlacesDropdown(inputText);
        }else{
            search_result_dropdown.hide();
        }
    });

    //--- Create Intersection markers
    var intersection_contents = $('#intersection-tab-contents');
    var arrayLength = intersection_features.length;
    for (var i = 0; i < arrayLength; i++) {
        var html = '<div class="col-lg-6">';
        html += '<img id="intersection_img_'+ i +'" class="drag-intersection-img" src="'+ intersection_features[i].img_src +'">';
        html += '<p>' + intersection_features[i].name + '</p>';
        html += '</div>';
        intersection_contents.append(html);
    }
    //--- end intersection

    //--- Create Lane attribute markers
    var lane_contents = $('#lane-tab-contents');
    var arrayLength = lane_attributes.length;
    for (var i = 0; i < arrayLength; i++) {
        var html = '<div class="col-lg-4">';
        html += '<img id="lane_img_'+ lane_attributes[i].id +'" class="drag-lane-img" src="'+ lane_attributes[i].img_src +'">';
        html += '</div>';
        lane_contents.append(html);
    }
    //--- end lane


    /**
     * Purpose: allow marker images in sidebar to be dragable onto layer
     * @params  image object
     * @event makes images draggable
     */

    $imgs = intersection_sidebar.find('.drag-intersection-img,.drag-lane-img');
    $imgs.draggable({
        appendTo: 'body', containment: 'body', zIndex: 150000, cursorAt: {left:25, top:50},
        revert: function() {
            if( $(this).hasClass('drag-lane-img') ){
                return 'invalid';
            }
        },
        helper: function() {
            var container = $('<div/>');
            var dragged = $(this).clone();
            dragged.attr('class', 'dragged-img');
            container.append(dragged);
            return container;
        },
        start: function(e, ui) {
            hidden_drag.removeClass('hidden');
        },
        stop: function(e) {
            hidden_drag.addClass('hidden');

            // check to see if intersection markers have already been placed
            var id = parseInt(this.id.match(/(\d+)$/)[0], 10);
            var num_features = vectors.features.length;
            for( var i=0; i < num_features; i++) {
                if( id == vectors.features[i].attributes.marker.id ) {
                    console.log("marker already placed");
                    return;
                }
            }

            if( $(this).hasClass('drag-intersection-img') ) {
            	if (currentControl != 'drag'){
            		$('#dragSigns').click();
            	}
                var point = new OpenLayers.Geometry.Point(e.pageX, e.pageY - 50); // subtract 50px because of navbar
                clone(this, point);
            }
        }
    });
    //--- end drag


    /**
     * Purpose: clone marker image onto layer
     * @params  object, point
     * @event places clone of marker image onto map post drag
     */

    function clone(object, point) {
        var lonlat = map.getLonLatFromPixel(point);

        var cloned_feature = new OpenLayers.Feature.Vector(
            new OpenLayers.Geometry.Point(lonlat.lon, lonlat.lat),
            {some:'data'},
            {externalGraphic: object.src, graphicHeight: 50, graphicWidth: 50, graphicYOffset: -50});

        cloned_feature.attributes = {"LonLat": lonlat.transform(toProjection, fromProjection)};
        cloned_feature.attributes.verifiedLat = cloned_feature.attributes.LonLat.lat;
        cloned_feature.attributes.verifiedLon = cloned_feature.attributes.LonLat.lon;

        var intersection_id = parseInt(object.id.match(/(\d+)$/)[0], 10);
        cloned_feature.attributes['marker'] = intersection_features[intersection_id];

        vectors.addFeatures(cloned_feature);
    }

    makeDroppable(null);
    $.get("js/row.html", function(data) {
        rowHtml = data;
        rebuildConnections(nodeObject);
    });


    /**
     * Purpose: uses sheepit form to allow for multiple speed codes
     * @params  DOM form
     * @event creates form and establishes parameters
     */

    speedForm = $('#speedForm').sheepIt({
        separator: '',
        allowRemoveLast: true,
        allowRemoveCurrent: true,
        allowRemoveAll: true,
        allowAdd: true,
        allowAddN: true,
        maxFormsCount: 13,
        minFormsCount: 0,
        iniFormsCount: 1,
        afterAdd: function(source, newForm) {
            $("[id*=speedLimitType]").change(function() {
                resetSpeedDropdowns();
            });
        }
    });

    $("#speedForm_add").click(function(){
        resetSpeedDropdowns();
    });
    
    $(".datetimepicker").each(function(){
        let config={
            enableTime: true,
            enableSeconds: true,
            allowInput: true,
            minuteIncrement: 1,
            secondIncrement: 1,
            dateFormat: "d/m/Y H:i:s"
        }
        $(this).flatpickr(config);
    });

});

/***
 * Purpose: Display list of autocomplete places suggested by predictionPlaces returned by google places API.
 * @params input search place text
 * @event update dropdown with a list of suggested places and allow to click on a place to move the map center location.
 */
function populateAutocompleteSearchPlacesDropdown(inputText){
    let search_place_dropdown = $("#dropdown-menu-search");
    $.ajax({
                type: 'POST',
                url: "/msp/googlemap/api/places/autocomplete",
                data: JSON.stringify({inputText: inputText}),
                headers: {
                    'Content-Type': 'application/json'
                },
                success: function(response){
                    if(!response){
                        console.error("Failed to retrieve places suggestions from server!");
                        return;
                    }
                    let suggestions = response["suggestions"];
                    suggestions = Object.values(suggestions);
                    for(let key in suggestions){
                        let suggestedResult = suggestions[key]["placePrediction"]["text"]["text"];
                        let place_item = $("<li><a><i class=\"fa fa-map-marker\" style=\"cursor: not-allowed\"></i> <span style=\"margin-left: 5px; cursor: pointer; width: 200px; display: inline-flex; overflow: hidden; text-overflow: ellipsis;\">"+suggestedResult+"</span></a></li> ");
                        place_item.click(clickPlaceHandler);
                        search_place_dropdown.append(place_item);
                    }
                    search_place_dropdown.show();
                },
                error: function(error){
                    search_place_dropdown.hide();
                    console.error(error);
                }
            });
}

/***
 * Purpose: Handler for a click event to navigate to a place
 */
function clickPlaceHandler(event){
    let place = event.target.innerText;
    if(place.length>0){
        $("#address-search").val(place);
        $("#dropdown-menu-search").hide();
        updatePlaceLocationView(place);
    }
}

/***
 * Purpose: Move map view to a location returned by google places API for a given input place.
 * @params input place full text
 * @event Update map center view with new place location.
 */
function updatePlaceLocationView(inputPlaceText){
    $.ajax({
        url: "/msp/googlemap/api/places/searchText",
        data: JSON.stringify({inputText: inputPlaceText}),
        type: 'POST',
        headers: {
            "Content-Type": "application/json"
        },
        success: function(response){
            try {
                let location = response["location"];
                let search_lat = location.lat;
                let search_lon = location.lng;
                setCookie("isd_latitude", search_lat, 365);
                setCookie("isd_longitude", search_lon, 365);
                setCookie("isd_zoom", map.getZoom(), 365);
                location = new OpenLayers.LonLat(search_lon, search_lat);
                location.transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());
                map.setCenter(location, 18);
            }
            catch (err) {
                console.log("No vectors to reset view");
            }
        },
        error: function(error){
            console.error(error);
        }
    });
}

/**
 * Purpose: makes the attributes droppable
 * @params  id of element
 * @event allows icon to be dropped onto the other sidebar
 */

function makeDroppable (id){
    //--- Drop Functionality
    var containerName = "attr_droppable";
    if (id !== null && id !== undefined){
        containerName += id;
    }
    $('#' + containerName).droppable({
        accept: ".drag-lane-img",
        activate: function( event, ui ) {
            $(this).find( "p" ).html( "Drop attributes here" );
        },
        over: function( event, ui ) {
            $(this).find( "p" ).html( "Drop it!" );
        },
        drop: function( event, ui ) {
            $(this).find( "p").remove();
            var container = $(this);
            var attr = $(ui.helper.children());
            if (id !== null && id !== undefined) {
                attr[0].id = "lane_img_" + id + "_" + attr[0].id.match(/\d+$/g)[0];
            }
            console.log(attr);
            makeDraggable( attr );

            if (containerName === "attr_droppable"){
                addLaneAttributeToContainer( container, attr[0] );
            } else {
                addLaneManeuversToContainer( container, attr[0]);
            }
        },
        out: function( event, ui ) {
            $(this).find( "p" ).html( "Come back!" );
        },
        deactivate: function( event, ui ) {
            $(this).find( "p" ).html( "Drop attributes here" );
        }
    });

    $('.trash_droppable').droppable({
        accept: ".dragged-img",
        drop: function( event, ui ) {
            var attr = $(ui.helper)[0];
            attr.remove();
            removeLaneAttributes(attr);
        }
    });
    //--- end drop
}

/**
 * Purpose: makes the attributes dragable
 * @params  element
 * @event allows icon to be dragged onto the other sidebar
 */

function makeDraggable( selector ) {
    selector.draggable({
        appendTo: 'body', containment: 'body', zIndex: 150000, cursorAt: {left:25, top:25},
        revert: function() {
            return 'invalid';
        },
        start: function(e, ui) {
            hidden_drag.removeClass('hidden');
            $('.trash_droppable').toggleClass("hidden");
        },
        stop: function(e) {
            hidden_drag.addClass('hidden');
            $('.trash_droppable').toggleClass("hidden");
        }
    });
}


/**
 * Purpose: series of functions that allow the attribute container to work
 * @params  element, container
 * @event allows icon to be dragged and dropped onto the other sidebar, plus trash
 */

function addLaneManeuversToContainer(container, attribute) {
    for(var i = 0; i < container.children().length; i++) {
        if (container.children()[i].id === attribute.id) {
            return;
        }
    }

    container.append( attribute );
}

// adds the attribute (image) to the displayed container
function addLaneAttributeToContainer( container, attribute ) {
    var attr_id = parseInt(attribute.id.match(/(\d+)$/)[0], 10);
    var lane_attr = lane_attributes[attr_id];

    // if attribute already exists in the lane attributes, skip, do not add
    if (selected_marker.attributes['lane_attributes'] &&
        selected_marker.attributes['lane_attributes'][attr_id]) {
        return;
    }

    // skip adding the attribute if it's already in the temp attributes, otherwise add it
    if(!tmp_lane_attributes[attr_id] ) {
        tmp_lane_attributes[attr_id] = lane_attr
    }
    else { return; }

    container.append( attribute );
}

// sets the temporary lane attributes to the actual lane object
function setLaneAttributes() {
    if( tmp_lane_attributes == null) {
        // no attributes to add
        return;
    }
    if( !selected_marker.attributes['lane_attributes'] ) {
        selected_marker.attributes['lane_attributes'] = {};
    }

    for( var attribute in tmp_lane_attributes ) {
        selected_marker.attributes['lane_attributes'][attribute] = tmp_lane_attributes[attribute]
    }

    resetLaneAttributes()
}

// clears the temporary lane objects
function resetLaneAttributes() {
    tmp_lane_attributes = {};
}

function removeLaneAttributes( attribute ) {
    var attr_id = parseInt(attribute.id.match(/(\d+)$/)[0], 10);
    if( tmp_lane_attributes[attr_id] ) {
        delete tmp_lane_attributes[attr_id];
        return
    }
    if( selected_marker.attributes['lane_attributes'] )
        if( selected_marker.attributes['lane_attributes'][attr_id] )
            delete selected_marker.attributes['lane_attributes'][attr_id];
}

function updateDisplayedLaneAttributes( feature ){
    removeDisplayedLaneAttributes();

    var lane_attributes = feature.attributes['lane_attributes'];
    if( lane_attributes == null || $.isEmptyObject(lane_attributes)) {
        return;
    }

    $('#attr_droppable').find('p').remove();
    for( var attribute in lane_attributes ) {
        var img = '<img id="lane_img_'+ lane_attributes[attribute].id +'" class="dragged-img" src="'+ lane_attributes[attribute].img_src +'">';
        $('#attr_droppable').append(img);
    }


    $('#attr_droppable').find('img').each(function() {
        makeDraggable( $(this) )
    });
}

function removeDisplayedLaneAttributes(){
    console.log("removing displayed attributes")
    $('#attr_droppable').empty().append(
        '<h4 class="list-group-item-heading">Lane Attributes</h4>' +
        '<p class="help-block text-center">drop content here</p>');
}


/**
 * Purpose: toggle marker lock
 * @params  click event
 * @event toggle marker lock
 */

$("button[name='layerControl']").click(function(e) {
	deleteMode = false;
	$("#dragSigns i").removeClass('fa-unlock').addClass('fa-lock')
	$(this).addClass('current').siblings().removeClass('active');
	currentControl = this.value;
	if(!$(this).hasClass('active')){
		if(currentControl === 'drag'){
			$("#dragSigns i").removeClass('fa-lock').addClass('fa-unlock')
		}
		if(currentControl === 'del'){
			deleteMode = true;
		}
		toggleControlsOn(currentControl);
	} else {
		deleteMode = false;
		toggleControlsOn('none');
	}
});


/**
 * Purpose: link to help doc in config
 * @params  click event
 * @event load appropriate help window for a field
 */

$('.fa-question-circle').click(function(){
	var tag = $(this).attr('tag');
	var obj = $.grep(help_notes, function(e){ return e.value === tag; });
	$('#help_modal').modal('show');
	$('#min').html(obj[0].min)
	$('#max').html(obj[0].max)
	$('#units').html(obj[0].units)
	$('#description').html(obj[0].description)
	$('#help_modal h4').html(obj[0].title)
});


/*****************************************
 * Lane connection layer table
 * Methods
 *****************************************/
function changeRow(oldVal, newVal, readOnly, valueSets) {
    $('#row' + oldVal).attr('id', 'row' + newVal);
    $('#connectionId' + oldVal).attr('id', 'connectionId' + newVal);
    $('input[name="remoteID' + oldVal + '"]').attr('name', 'remoteID' + newVal);
    $('input[name="toLane' + oldVal + '"]').attr('name', 'toLane' + newVal);
    $('input[name="signal_id' + oldVal + '"]').attr('name', 'signal_id' + newVal);
    $('#maneuvers' + oldVal).attr('id', 'maneuvers' + newVal);
    $('#attr_droppable' + oldVal).attr('id', 'attr_droppable' + newVal);
    $('#delete' + oldVal).attr('onclick', 'deleteRow(' + newVal + ')')
        .attr('id', 'delete' + newVal);
    if (readOnly && readOnly !== undefined) {
        for (var i = 0; i < readOnly.length; i++) {
            $('input[name="' + readOnly[i] + newVal + '"').prop('readonly', true);
        }
    }
    if (valueSets && valueSets !== undefined) {
        for (var set in valueSets) {
            if (valueSets.hasOwnProperty(set)) {
                if (set === 'maneuvers') {
                    for (var k = 0; k < valueSets[set].length; k++) {
                        for(var j = 0; j < lane_attributes.length; j++) {
                            if (lane_attributes[j].id.toString() === valueSets[set][k]){
                                addLaneManeuversToContainer($('#attr_droppable' + newVal),
                                    '<img id="lane_img_'+ newVal +'_' + lane_attributes[j].id + '" class="dragged-img" src="'+ lane_attributes[j].img_src +'">');
                            }
                        }
                    }
                } else if (set === 'connectionId'){
                	$('#connectionId' + newVal + ' .dropdown-toggle').html(valueSets[set]+'<span class="caret"></span>')
                } else {
                    $('input[name="' + set + newVal + '"').attr('value', valueSets[set]);
                }
            }
        }
    }
}

$("#add_row").click(function() {
    addRow(null, null);
});

function addRow(readOnly, valueSets) {
    $('#tab_intersects tbody').append(rowHtml);
    numRows++;
    changeRow('New', numRows, readOnly, valueSets);
    populateConnectionIdDropdown(numRows)
    makeDroppable(numRows);
}

function deleteRow(rowNum) {
    $('#row' + rowNum).remove();
    for(var i = rowNum + 1; i <= numRows; i++) {
        changeRow(i, i - 1, null, null);
    }
    numRows--;
}

function rebuildConnections(connections) {

    //TODO clear if empty rows
    $('#tab_intersects > tbody').empty();
    numRows = -1;
    if (connections === null || connections === undefined || connections.length < 1) {
        addRow(null, null);
    } else {
        for(var i = 0; i < connections.length; i++) {
            addRow(null, connections[i]);
        }
    }
    nodeObject = [];
}

function saveConnections() {
    nodeObject = [];
    for(var i = 0; i <= numRows; i++) {
        var ids = $('#maneuvers' + i + ' > ul > li > img');
        var maneuvers = [];
        for(var j = 0; j < ids.length; j++) {
            maneuvers.push(ids[j].id.match(/\d+$/g)[0]);
        }
        nodeObject.push({
            connectionId: $('#connectionId' + i + ' .dropdown-toggle').text().replace('\u200b', ""),
            remoteID: $('input[name="remoteID' + i + '"]').val(),
            fromLane: selected_marker.attributes.laneNumber,
            toLane: $('input[name="toLane' + i + '"]').val(),
            signal_id: $('input[name="signal_id' + i + '"]').val(),
            maneuvers: maneuvers
        });
    }
}

function populateConnectionIdDropdown(rowId) {
	// Add an empty option at the top. Use a non-breaking space(uni-code 200b), to force the
	// entry in the dropdown menu to maintain the same height as the rest of the list.
	$('#connectionId' + rowId + ' .dropdown-menu').append(
    		$('<li><a href="#">\u200b</a></li>').click(function(){
    			    var selText = $(this).children('a').text();
    			    $(this).parents('.btn-group').find('.dropdown-toggle').html(selText+'<span class="caret"></span>');
    		    }))
    for(i = 1; i <= 255; i++) {
        $('#connectionId' + rowId + ' .dropdown-menu').append(
        		$('<li><a href="#">' + i + '</a></li>').click(function(){
        			    var selText = $(this).children('a').text();
        			    $(this).parents('.btn-group').find('.dropdown-toggle').html(selText+'<span class="caret"></span>');
        		    }))
    }
}

/*****************************************
 * Speed Limit Values
 * Methods
 *****************************************/


function removeSpeedForm() {
    speedForm.removeAllForms();
}

function addSpeedForm() {
    speedForm.addForm();
}

function rebuildSpeedForm(speedLimitArray) {
    var results = speedLimitArray.length
    for (var i = 0; i < results; i++) {
        speedForm.addForm();
        var forms = speedForm.getForms(i);
        forms[i].inject(
            {'velocity': speedLimitArray[i].velocity}
        );
        $("#speedForm_"+ i + "_speedLimitType").val(speedLimitArray[i].speedLimitType)
    }
    resetSpeedDropdowns();
    speedLimits = [];
}

function saveSpeedForm() {
    var forms = (speedForm.getForms()).length;
    for (var i = 0; i < forms; i++) {
        speedLimits.push({
            speedLimitType: $("#speedForm_"+ i + "_speedLimitType option:selected").text(),
            velocity: $("#speedForm_" + i + "_velocity").val()
        });
    }
    removeSpeedForm();
    return speedLimits;
}

function resetSpeedDropdowns(){
    $("[id*=speedLimitType] > option").each(function() {
        if ($(this).val() !== "") {
            $(this).prop('disabled', false);
        }
    });
    var forms = (speedForm.getForms()).length;
    for (var i = 0; i < forms; i++) {
        var current = $("#speedForm_"+ i + "_speedLimitType option:selected").text();
        $("[id*=speedLimitType] option[value='"+current+"']").prop('disabled', true);
    }
}

