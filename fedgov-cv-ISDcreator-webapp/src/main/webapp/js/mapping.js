/**
 * Created by lewisstet on 2/25/2015.
 * Updated 3/2017 by martzth
 */

/**
 * DEFINE GLOBAL VARIABLES
 */
    var map;
    var vectors, lanes, laneMarkers, box, laneConnections, errors, trace, laneWidths;
    var fromProjection, toProjection;
    var temp_lat, temp_lon, selected_marker, selected_layer;
    var intersection_url = '//api.geonames.org/findNearestIntersectionJSON';
    var google_elevation_url='/msp/googlemap/api/elevation';
    var computingLane = false;
    var computedLaneSource;
    var sharedWith_object = '';
    var typeAttribute_object = '';
    var typeAttributeName = '';
    var typeAttributeNameSaved = '';
    var sharedWith = [];
    var typeAttribute = [];
    var laneTypeOptions = [];
    var nodeLaneWidth = [];
    var signalPhase, stateConfidence, laneNum, laneType, approachType, intersectionID, approachID;
    var nodeObject = [];
    var revisionNum = 0;
    var cachedSessionKey = null;
    let cachedApiKey='';
    let cachedUsername = null;

    var bingResolutions = [156543.03390625, 78271.516953125, 39135.7584765625,
        19567.87923828125, 9783.939619140625, 4891.9698095703125,
        2445.9849047851562, 1222.9924523925781, 611.4962261962891,
        305.74811309814453, 152.87405654907226, 76.43702827453613,
        38.218514137268066, 19.109257068634033, 9.554628534317017,
        4.777314267158508, 2.388657133579254, 1.194328566789627,
        0.5971642833948135, 0.29858214169740677, 0.14929107084870338,
        0.07464553542435169];
    var bingServerResolutions = [156543.03390625, 78271.516953125, 39135.7584765625,
        19567.87923828125, 9783.939619140625, 4891.9698095703125,
        2445.9849047851562, 1222.9924523925781, 611.4962261962891,
        305.74811309814453, 152.87405654907226, 76.43702827453613,
        38.218514137268066, 19.109257068634033, 9.554628534317017,
        4.777314267158508, 2.388657133579254, 1.194328566789627,
        0.5971642833948135, 0.29858214169740677];

/***
 * A global variable to store current RGA toggle status. 
 * Some UI fields are disabled or enabled depends on the current RGA status.
 * @type Boolean Indicator whether RGA fields are enabled.
 */
let rga_enabled=false;


function set_rga_status() {
    if($('#rga_switch').is(":checked")){
        rga_enabled = true;
    }else{
        rga_enabled = false;
    }
    enable_rga_fields(enable=rga_enabled);
}

async function getApiKey() {
    if (cachedApiKey) {
        return cachedApiKey;
    }

    try {
        const res = await fetch('/private-resources/js/ISDcreator-webapp-keys.js');
        const text = await res.text();

        // Extract the API key from the file content
        const regex = /const\s+apiKey\s*=\s*"([^"]+)"/;
        const match = regex.exec(text);
        cachedApiKey = match?.[1];

        if (!cachedApiKey) {
            throw new Error('API key not found in the file');
        }

        return cachedApiKey;
    } catch (error) {
        console.error('Failed to fetch API key:', error);
        throw new Error('Failed to fetch API key');
    }
}

async function getUsername() {
    if (cachedUsername) {
            return cachedUsername;
    }
    try {
            const res = await fetch('/private-resources/js/ISDcreator-webapp-keys.js');
            const text = await res.text();
            // Extract the API key from the file content
            const regex = /const\s+geoNamesUserName\s*=\s*"([^"]+)"/;
            const match = regex.exec(text);
            cachedUsername = match?.[1];
            if (!cachedUsername) {
                    throw new Error('API key not found in the file');
            }
            return cachedUsername;
    } catch (error) {
            console.error('Failed to fetch API key:', error);
            throw new Error('Failed to fetch API key');
    }
}

async function GetHiddenMap() {
	let apiKey = await getApiKey();
	let hiddenMap = new Microsoft.Maps.Map('#myHiddenMap', {
		credentials: apiKey
	});
	await hiddenMap.getCredentials(async function (c) {
		cachedSessionKey = c;
		if(cachedSessionKey!==null){
			await init();
		}
	});
}
/**
 * Define functions that must bind on load
 */

async function init() {
	// cannot call http service from our https deployed application:
	// making call to backend to do GET for us if not deployed on localhost
	if( host.indexOf("localhost") == -1 ) {
		intersection_url = '/' + proj_name + '/builder/geonames/findNearestIntersectionJSON'
	}

    //Set initial status of various form elements
    $('.phases').hide();
    $('.lane_type_attributes select').hide();
	$("#lane_num_check").hide();
	$("#lane_type_check").hide();
    
    $('[data-toggle="tooltip"]').tooltip();


    /*********************************************************************************************************************/
    /**
     * Purpose: create map object which will house bing map tiles
     * @params  openlayers2
     * @event setting all the parameters -> will note certain areas
     *
     * Note: each layer is defined in this section, and layers interact with the sidebar
     * by showing/hiding DOM elements. Also, all data is loaded into the forms via these feature objects
     */

	const apiKey = await getApiKey();

	map = new OpenLayers.Map('map', {
        allOverlays: false,
        fractionalZoom: true,
        controls: [
            new OpenLayers.Control.Navigation({
                dragPanOptions: {
                    enableKinetic: true
                }
            }),
            new OpenLayers.Control.LayerSwitcher(),
            new OpenLayers.Control.Zoom({
                zoomInId: "customZoomIn",
                zoomOutId: "customZoomOut"
            })
        ]});
    fromProjection = new OpenLayers.Projection("EPSG:4326");   // Transform from WGS 1984
    toProjection   = new OpenLayers.Projection("EPSG:3857"); // to Spherical Mercator Projection

    //Sets the standard viewport to Detroit unless overwritten by cookie
    var view_lat = 42.3373873;
    var view_lon = -83.051308;
    var view_zoom = 19;
    if (getCookie("isd_latitude") !== ""){
        view_lat = getCookie("isd_latitude")
    }
    if (getCookie("isd_longitude") !== ""){
        view_lon = getCookie("isd_longitude")
    }
    if (getCookie("isd_zoom") !== ""){
        view_zoom = getCookie("isd_zoom")
    }
    if (getCookie("isd_message_type") !== ""){
        $('#message_type').val(getCookie("isd_message_type"));
    }
    if (getCookie("isd_node_offsets") !== ""){
        $('#node_offsets').val(getCookie("isd_node_offsets"));
    }
    if (getCookie("isd_enable_elevation") !== ""){
        $('#enable_elevation').prop('checked',"true" == getCookie("isd_enable_elevation"));
    } else {
        $('#enable_elevation').prop('checked', true);
    }

    //Set cookie anytime map is moved
    map.events.register("moveend", map, function() {
        var center_point = map.getCenter();
        var center_lonlat = new OpenLayers.LonLat(center_point.lon,center_point.lat).transform(toProjection, fromProjection)
        setCookie("isd_latitude", center_lonlat.lat, 365);
        setCookie("isd_longitude", center_lonlat.lon, 365);
        setCookie("isd_zoom", map.getZoom(), 365);
        $('#zoomLevel .zoom').text(map.getZoom());
    });

    /* Establish bing layer types - zoom is defined using the zoom level and resolutions. sever resolutions is
     a smaller array because bing only has so many tile sets. once we pass those, we use the resolutions array for fractional zoom.
     http://stackoverflow.com/questions/42396112/magnifying-tiles-in-openlayers-2-or-increasing-maxzoom-without-distorting-projec*/

    var road = new OpenLayers.Layer.Bing({
        name: "Road",
        key: cachedSessionKey,
        type: "Road",
        numZoomLevels: 22,
        resolutions: bingResolutions,
        serverResolutions: bingServerResolutions,
        transitionEffect: 'resize'
    });
    var hybrid = new OpenLayers.Layer.Bing({
        name: "Hybrid",
        key: cachedSessionKey,
        type: "AerialWithLabels",
        numZoomLevels: 22,
        resolutions: bingResolutions,
        serverResolutions: bingServerResolutions,
        transitionEffect: 'resize'
    });
    var aerial = new OpenLayers.Layer.Bing({
        name: "Aerial",
        key: cachedSessionKey,
        type: "Aerial",
        numZoomLevels: 22,
        resolutions: bingResolutions,
        serverResolutions: bingServerResolutions,
        transitionEffect: 'resize'
    });


    //Create style maps for the lanes
    var laneDefault = {
    		strokeColor: "${getStrokeColor}",
    		fillColor: "${getFillColor}",
            strokeOpacity: 1,
            strokeWidth: 4,
            fillOpacity: .9,
            pointRadius: 6,
            label: "${getLabel}",
            fontFamily: "Arial",
            fontSize: "8px",
            cursor: "pointer"
        };
    
    var barDefault = {
    		strokeColor: "#FF0000",
    		fillColor: "#FF0000",
            strokeOpacity: 1,
            strokeWidth: 3,
            fillOpacity: 0,
            pointRadius: 2
        };
    
    var vectorDefault = {
    		strokeColor: "#FF9900",
    		fillColor: "#FF9900",
            strokeOpacity: 1,
            strokeWidth: 1,
            fillOpacity: 0,
            pointRadius: 1
        };

    var widthDefault = {
        strokeColor: "#FFFF00",
        fillColor: "#FFFF00",
        strokeOpacity: .5,
        strokeWidth: 1,
        fillOpacity: .1,
        pointRadius: 1
        };

    var connectionsDefault = {
        strokeColor: "#0000FF",
        fillColor: "#0000FF",
        strokeOpacity: 1,
        strokeWidth: 1,
        fillOpacity:.5,
        pointRadius: 6,
        graphicName: "triangle",
        rotation: "${angle}"
    };
    
    var context = null;
    
    var laneStyleMap = new OpenLayers.StyleMap({
        "default": new OpenLayers.Style(laneDefault, {context: {
        	getStrokeColor: function(feature) {
                if (feature.attributes.computed) {
                    return "#FF5000";
                } else if (feature.attributes.source) {
                	return "#00EDFF";
                } else {
                    return "#FF9900";
                }
            },
            getFillColor: function(feature) {
                if (feature.attributes.computed) {
                    return "#FF5000";
                } else if (feature.attributes.source) {
                	return "#00EDFF";
                } else {
                    return "#FF9900";
                }
            },
            getLabel: function(feature) {
                if (feature.attributes.laneNumber) {
                    return feature.attributes.laneNumber;
                } else {
                    return '';
                }
            }
        }})
    });
    
    var barStyleMap = new OpenLayers.StyleMap({
        "default": new OpenLayers.Style(barDefault, {context:context})
    });

    var vectorStyleMap = new OpenLayers.StyleMap({
        "default": new OpenLayers.Style(vectorDefault, {context: context})
    });

    var widthStyleMap = new OpenLayers.StyleMap({
        "default": new OpenLayers.Style(widthDefault, {context: context})
    });

    var connectionsStyleMap = new OpenLayers.StyleMap({
        "default": new OpenLayers.Style(connectionsDefault, {context: context})
    });

    
    //Create new layers for images
    lanes = new OpenLayers.Layer.Vector("Lane Layer", {
		eventListeners:{
	        'featureselected':function(evt){
	        	var selectedLane = evt.feature;

	        	if (deleteMode){
            		if(selectedLane.attributes.source) {
            			// Watch out for computed lanes relying on this lane
            			var dependentLanes = [];
            			for(var i = 0; i < this.features.length; i++) {
            				if(this.features[i].attributes.computed &&
            						this.features[i].attributes.referenceLaneID == selectedLane.attributes.laneNumber) {
            					dependentLanes.push(this.features[i]);
            				}
            			}
                		var doDelete = confirm(dependentLanes.length + " computed lanes depend on this lane. " +
                								"Deleting this lane will delete them all. Continue?");

                		if(doDelete) {
                			dependentLanes.push(selectedLane);		// Don't forget to delete this as well
                			for(var i = 0; i < dependentLanes.length; i++) {
                				deleteMarker(this, dependentLanes[i]);
                			}
                			laneWidths.destroyFeatures();
                		}
                		else {
                			controls.del.unselect(selectedLane);
                		}
            		}
            		else if(selectedLane.attributes.computed) {
            			// Check if the source lane for this computed lane
            			// has any other computed lanes.
            			var r = Number(selectedLane.attributes.referenceLaneNumber);
            			
            			var computedCount = 0;
            			for(var i = 0; i < this.features.length; i++) {
            				if(this.features[i].attributes.computed &&
            					this.features[i].attributes.referenceLaneID == this.features[r].attributes.laneNumber) {
            					computedCount++;
            				}
            			}

            			if(computedCount == 1) {
            				// This was the only computed lane for the source lane, it is no longer a source lane
            				this.features[r].attributes.source = false;
            			}

            			// Delete this computed lane
            			deleteMarker(this, selectedLane);
	                    laneWidths.destroyFeatures();
            		}
            		else {
		        		deleteMarker(this, selectedLane);
	                    laneWidths.destroyFeatures();
            		}
	        	}
	        }
		}, styleMap: laneStyleMap
    });
    
    $('#shared_with').multiselect({
        onChange: function(option, checked){
            updateSharedWith()
        },
        maxHeight: 200,
        buttonText: function(options, select) {
            if (options.length === 0) {
                return 'Select Shared With Type'
            } else if (options.length > 1) {
                return options.length + ' selected';
            } else {
                var labels = [];
                options.each(function() {
                    if ($(this).attr('label') !== undefined) {
                        labels.push($(this).attr('label'));
                    }
                    else {
                        labels.push($(this).html());
                    }
                });
                return labels.join(', ') + '';
            }
        }
    });
    
    $(".lane_type ul li").each(function() { laneTypeOptions.push($(this).text()) });
    
    box = new OpenLayers.Layer.Vector("Stop Bar Layer", {
		eventListeners:{
	        'featureselected':function(evt){
	        	selected_marker = evt.feature;
	        	if (deleteMode){
	        		deleteMarker(this, selected_marker);
	        	} else {
                    $(".lane-info-tab").find('a:contains("Lane Info")').text('Approach Info');
                    $(".lane-info-tab").find('a:contains("Marker Info")').text('Approach Info');
                    $('#lane-info-tab').addClass('active');
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
	        		//---------------------------------------
		        	$(".selection-panel").text('Approach Configuration');
		        	$("#lane_attributes").hide();
		        	$(".lane_type_attributes").hide();
		        	$(".lane_number").hide();
		        	$(".lat").hide();
		        	$(".long").hide();
		        	$(".verified_lat").hide();
		        	$(".verified_long").hide();
		        	$(".intersection").hide();
					$(".region").hide();
					show_rga_fields(hide=true);
					$('.road_authority_id').hide();
					$('.road_authority_id_type').hide();
		        	$(".elev").hide();
		        	$(".verified_elev").hide();
		        	$(".lane_width").hide();
		        	$(".descriptive_name").hide();
		        	$(".lane_type").hide();
                    $(".revision").hide();
                    $(".master_lane_width").hide();
                    $(".intersection_name").hide();
                    $(".shared_with").hide();
             //       $("#clone").hide();
                    $(".btnClone").hide();
		        	//----------------------------------------
		        	$(".approach_type").show();
                    $(".approach_name").show();
                    $('#approach_name li').show();
                    for (i=0; i < box.features.length; i++){
                        var usedNum = box.features[i].attributes.approachID;
                        $('.approach_name li:contains(' + usedNum + ')').hide();
                    }
		        	//----------------------------------------
		        	$("#approach_title").val(selected_marker.attributes.approach);
		        	$("#attributes").show();	        	
		        	selected_layer = this;
	        	}
	        	
                if (! selected_marker.attributes.approachType) {
                    approachType = null;
                    $('#approach_type .dropdown-toggle').html("Select an Approach Type <span class='caret'></span>");
                } else {
                    approachType = selected_marker.attributes.approachType;
                    $('#approach_type .dropdown-toggle').html(selected_marker.attributes.approachType + " <span class='caret'></span>");
                }
                
                if (! selected_marker.attributes.approachID) {
                    approachID = null;
                    $('#approach_name .dropdown-toggle').html("Select an Approach ID <span class='caret'></span>");
                } else {
                    approachID = selected_marker.attributes.approachID;
                    $('#approach_name .dropdown-toggle').html(selected_marker.attributes.approachID + " <span class='caret'></span>");
                }
                
	        },
        	'featureunselected':function(evt){
        		$("#attributes").hide();
        		selected_marker = null;
	        }
		},
    	styleMap: barStyleMap
    });

    laneMarkers = new OpenLayers.Layer.Vector("Lane Marker Layer", {
		eventListeners:{
	        'featureselected':function(evt){
	        	selected_marker = evt.feature;
	        	if(selected_marker.attributes.computed) {
	        		$(".selection-panel").text('Computed Lane Configuration');
	        	} else {
	        		$(".selection-panel").text('Lane Configuration');
	        	}
				console.log("selected: ", selected_marker)

                // delete marker and return
	        	if (deleteMode){
	        		deleteMarker(this, selected_marker);
	        		 return false;
	        	} else {
	        		updateLaneFeatureLocation( selected_marker );
	        	}

                $('#lane_number li').show();
                for (i=0; i < lanes.features.length; i++){
                    var usedNum = lanes.features[i].attributes.laneNumber;
                    $('.lane_number li').filter(function() {
                    								return $(this).text() === usedNum;
                    							}).hide();
                }
                $(".lane-info-tab").find('a:contains("Marker Info")').text('Lane Info');
                $(".lane-info-tab").find('a:contains("Approach Info")').text('Lane Info');
                $('#lane-info-tab').addClass('active');
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
                $(".revision").hide();
                $('.phases').hide();
                $(".master_lane_width").hide();
                $(".intersection_name").hide();
                show_rga_fields(hide=true);
				$('.road_authority_id').hide();
				$('.road_authority_id_type').hide();
                $(".approach_name").hide();
                $(".shared_with").hide();
                $(".btnClone").hide();
				//-------------------------------------
	        	$(".lat").show();
	        	$(".long").show();
	        	if(selected_marker.attributes.computed) {
	        		$("#lat").prop('readonly', true);
	        		$("#long").prop('readonly', true);
	        	}
	        	$(".elev").show();
	        	$(".spat_label").show();
	        	$(".lane_width").show();
		
        		if ( selected_marker.attributes.number == 0 ) {
        			updateDisplayedLaneAttributes( selected_marker );
                    rebuildConnections(selected_marker.attributes.connections);
		        	$("#lane_attributes").show();
		        	$(".descriptive_name").show();
		        	$(".lane_type").show();
		        	$(".lane_number").show();
                    $('.spat-info-tab').show();
                    $('.connection-tab').show();
					$(".intersection-info-tab").find('a:contains("Intersection Info")').text('Speed Limits');
					$('.intersection-info-tab').show();
					$('.layer').hide();
					$('.lane-speed-text').show();
                    $(".shared_with").show();
                    if(!selected_marker.attributes.computed) {
                		// Only show the button if this lane is already defined with a lane number
                    	if(typeof selected_marker.attributes.laneNumber !== 'undefined') {
                    		$(".btnClone").show();
                		}
                    }
                    else {
    	        	    $('.computed-tab').show();
                    }
        		} else {
		        	$("#lane_attributes").hide();
		        	$(".descriptive_name").hide();
		        	$(".lane_type").hide();
		        	$(".lane_number").hide();
        		}
        		
        		selected_layer = this;
        		
                if(lanes.features[selected_marker.attributes.lane].attributes.laneWidth){
                    nodeLaneWidth = lanes.features[selected_marker.attributes.lane].attributes.laneWidth;
                }

        		if (! nodeLaneWidth[selected_marker.attributes.number]){
        			$("#lane_width").val("0");
        		} else {
        			$("#lane_width").val(nodeLaneWidth[selected_marker.attributes.number]);
        		}
        		
        		if (! selected_marker.attributes.elevation.value){
        			$("#elev").val("");
        		} else {
        			$("#elev").val(selected_marker.attributes.elevation.value);
        		}
        		     		
                if (! selected_marker.attributes.signalPhase) {
                    signalPhase = null;
                    $('#phase .dropdown-toggle').html("Select a Signal Phase <span class='caret'></span>");
                } else {
                    signalPhase = selected_marker.attributes.signalPhase;
                    $('#phase .dropdown-toggle').html(selected_marker.attributes.signalPhase + " <span class='caret'></span>");
                    $('#phase' + selected_marker.attributes.signalPhase.substring(1, 2)).show();
                }
        		
        		if (! selected_marker.attributes.stateConfidence) {
                    stateConfidence = null;
                    $('#confidence .dropdown-toggle').html("Select a Confidence <span class='caret'></span>");
                } else {
                    stateConfidence = selected_marker.attributes.stateConfidence;
                    $('#confidence .dropdown-toggle').html(selected_marker.attributes.stateConfidence + " <span class='caret'></span>");
                }

                if (! selected_marker.attributes.laneNumber) {
                    laneNum = null;
                    $('#lane_number .dropdown-toggle').html("Select a Lane Number <span class='caret'></span>");
                } else {
                    laneNum = selected_marker.attributes.laneNumber;
                    $('#lane_number .dropdown-toggle').html(selected_marker.attributes.laneNumber + " <span class='caret'></span>");
                }
                
                if (! selected_marker.attributes.laneType) {
                    laneType = null;
                    $('#lane_type .dropdown-toggle').html("Select a Lane Type <span class='caret'></span>");
                } else if ( selected_marker.attributes.number == 0 ) {
                    laneType = selected_marker.attributes.laneType;
                    $('#lane_type .dropdown-toggle').html(selected_marker.attributes.laneType + " <span class='caret'></span>");
                    toggleLaneTypeAttributes(selected_marker.attributes.laneType);
                }

				if (!lanes.features[selected_marker.attributes.lane].attributes.speedLimitType) {
					removeSpeedForm();
					addSpeedForm();
				} else {
					rebuildSpeedForm(lanes.features[selected_marker.attributes.lane].attributes.speedLimitType);
				}
                
            	$('#shared_with').multiselect('deselectAll', false);
            	$("#shared_with").multiselect("refresh");
                               
                if (selected_marker.attributes.sharedWith) {
                	$('#shared_with').multiselect('select', selected_marker.attributes.sharedWith);
                	$("#shared_with").multiselect("refresh");
                }
                
                if (selected_marker.attributes.typeAttribute && selected_marker.attributes.laneType) {
                	$('#' + selected_marker.attributes.laneType + '_type_attributes').multiselect('select', selected_marker.attributes.typeAttribute);
                	$('#' + selected_marker.attributes.laneType + '_type_attributes').multiselect("refresh");
                } 

                if (! selected_marker.attributes.spatRevision){
                	$('#spat_revision').val(1);
                } else {
                	$('#spat_revision').val(selected_marker.attributes.spatRevision);
                }

                $('#descriptive_name').val(selected_marker.attributes.descriptiveName);
                $('#signal_group_id').val(selected_marker.attributes.signalGroupID);
                $('#start_time').val(selected_marker.attributes.startTime);
                $('#min_end_time').val(selected_marker.attributes.minEndTime);
                $('#max_end_time').val(selected_marker.attributes.maxEndTime);
                $('#likely_time').val(selected_marker.attributes.likelyTime);
                $('#next_time').val(selected_marker.attributes.nextTime);              
                
	            temp_lat = selected_marker.attributes.LonLat.lat;
	            temp_lon = selected_marker.attributes.LonLat.lon;
	            populateAttributeWindow(temp_lat, temp_lon);
	            $("#attributes").show();

                for(var attrConnection in selected_marker.attributes.connections) {
                    if (selected_marker.attributes.connections.hasOwnProperty(attrConnection) && selected_marker.attributes.number == 0){
                        var connection = selected_marker.attributes.connections[attrConnection];
                        var start_point;
                        var end_point;

                        for (var i = 0; i < lanes.features.length; i++) {
                            var lanefeature = lanes.features[i];

                            if (lanefeature.attributes.laneNumber && lanefeature.attributes.laneNumber !== undefined) {
                                if (parseInt(lanefeature.attributes.laneNumber) === parseInt(connection.fromLane)) {
                                    start_point = new OpenLayers.Geometry.Point(lanefeature.geometry.components[0].x, lanefeature.geometry.components[0].y);
                                } else if (parseInt(lanefeature.attributes.laneNumber) === parseInt(connection.toLane)) {
                                    end_point = new OpenLayers.Geometry.Point(lanefeature.geometry.components[0].x, lanefeature.geometry.components[0].y);
                                }
                            }
                        }
                        var angleDeg = 0;
                        if(typeof start_point !== 'undefined' && typeof end_point !== 'undefined') {
                            //Q III
                            if (start_point.x > end_point.x && start_point.y > end_point.y) {
                                angleDeg = 270 - (Math.atan2(start_point.y - end_point.y, start_point.x - end_point.x) * 180 / Math.PI);
                            }
                            //Q IV
                            if (start_point.x > end_point.x && start_point.y < end_point.y) {
                                angleDeg = 270 - (Math.atan2(start_point.y - end_point.y, start_point.x - end_point.x) * 180 / Math.PI);
                            }
                            //Q II
                            if (start_point.x < end_point.x && start_point.y > end_point.y) {
                                angleDeg = 90 - (Math.atan2(end_point.y - start_point.y, end_point.x - start_point.x) * 180 / Math.PI);
                            }
                            //Q I
                            if (start_point.x < end_point.x && start_point.y < end_point.y) {
                                angleDeg = 90 - (Math.atan2(end_point.y - start_point.y, end_point.x - start_point.x) * 180 / Math.PI);
                            }

                            var xlen = end_point.x - start_point.x;
                            var ylen = end_point.y - start_point.y;
                            var hlen = Math.sqrt(Math.pow(xlen, 2) + Math.pow(ylen, 2));
                            var smallerLen = hlen - 1;
                            var ratio = smallerLen / hlen;
                            var smallerXLen = xlen * ratio;
                            var smallerYLen = ylen * ratio;
                            var smallerX = start_point.x + smallerXLen;
                            var smallerY = start_point.y + smallerYLen;

                            laneConnections.addFeatures([new OpenLayers.Feature.Vector(new OpenLayers.Geometry.LineString([start_point, end_point]))]);
                            laneConnections.addFeatures([new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Point(smallerX, smallerY), {angle: angleDeg})]);
                        }
                    }
                }
                
                if(selected_marker.attributes.computed) {
                	if (! selected_marker.attributes.referenceLaneNumber){
                        $("#referenceLaneNumber").val("");
                    } else {
                        $("#referenceLaneNumber").val(selected_marker.attributes.referenceLaneNumber);
                    }

                    if (! selected_marker.attributes.referenceLaneID){
                        $("#referenceLaneID").val("");
                    } else {
                        $("#referenceLaneID").val(selected_marker.attributes.referenceLaneID);
                    }

                    if (! selected_marker.attributes.offsetX){
                        $("#offset-X").val("");
                    } else {
                        $("#offset-X").val(selected_marker.attributes.offsetX);
                    }

                    if (! selected_marker.attributes.offsetY){
                        $("#offset-Y").val("");
                    } else {
                        $("#offset-Y").val(selected_marker.attributes.offsetY);
                    }

                    if (! selected_marker.attributes.rotation){
                        $("#rotation").val("");
                    } else {
                        $("#rotation").val(selected_marker.attributes.rotation);
                    }

                    if (! selected_marker.attributes.scaleX){
                        $("#scale-X").val("");
                    } else {
                        $("#scale-X").val(selected_marker.attributes.scaleX);
                    }

                    if (! selected_marker.attributes.scaleY){
                        $("#scale-Y").val("");
                    } else {
                        $("#scale-Y").val(selected_marker.attributes.scaleY);
                    }
                }
	        },
	        'featureunselected':function(evt){
	        	$("#attributes").hide();
				resetLaneAttributes();
	            selected_marker = null;
                laneConnections.removeAllFeatures();
	        }
		},
		styleMap: laneStyleMap
    });

    laneWidths = new OpenLayers.Layer.Vector("Width Layer", {
        eventListeners:{
            'featureadded':function(evt){
            }
        }, styleMap: widthStyleMap
    });

    laneConnections = new OpenLayers.Layer.Vector("Connection Layer", {
        eventListeners:{
            'featureadded':function(evt){
            }
        }, styleMap: connectionsStyleMap
    });
    
    vectors = new OpenLayers.Layer.Vector("Vector Layer",{
		eventListeners:{
			'featureadded' : function (evt) {
				selected_marker = evt.feature;
				updateFeatureLocation(evt.feature);
			},
	        'featureselected':function(evt){
	        	selected_marker = evt.feature;
	        	if (deleteMode){
	        		deleteMarker(this, selected_marker);
	        	} else {
	        		updateFeatureLocation( selected_marker );
	        	}
	        },
	        'featureunselected':function(evt){
	        	$("#attributes").hide();
	        	selected_marker = null;
	        }
		}, styleMap: vectorStyleMap
    });

    errors = new OpenLayers.Layer.Markers("Error Layer");


    var updateDisplay = function( event ) { // 5
        $('.measurement').text( (event.measure).toFixed(3) + ' ' + event.units );
        copyTextToClipboard((event.measure).toFixed(3));
    };


    //Controls for the lane layer to draw and modify
    controls = {
            line: new OpenLayers.Control.DrawFeature(lanes,
                        OpenLayers.Handler.Path, {featureAdded: onFeatureAdded}),
            modify: new OpenLayers.Control.ModifyFeature(lanes, 
            					{beforeSelectFeature: function(feature) {
	            						// NOTE: This code is run in beforeSelectFeature because if it is ran in
	            						// selectFeature then it will override OpenLayers implementation of
	            						// selectFeature prevent the selection of regular lanes from working
	            						// correctly
            						
										if(feature.attributes.computed) {
											// Set the selected computed lane as the source for
											// drawing a new computed lane
											computedLaneSource = feature;
											toggleControlsOn("placeComputed");
											
											// NOTE: This will throw an error and kick out of the selection process.
											// This is intentional since we don't actually want to select the lane
											// itself for editing, rather we just want to determine which lane
											// the user wanted to select.
											// All other attempts to cancel selecting the feature behaved incorrectly.
											console.log("The following TypeError can be ignored safely:");
											this.unselectFeature(feature);
										}
									}
            					}),
            placeComputed: new OpenLayers.Control.DrawFeature(lanes,
            			OpenLayers.Handler.Point, {featureAdded: placeComputedLane}),
            drag: dragHandler(),
            bar: new OpenLayers.Control.DrawFeature(box,
                    OpenLayers.Handler.RegularPolygon, {
                        handlerOptions: {
                            sides: 4,
                            irregular: true
                        }
                    }),
            edit: new OpenLayers.Control.ModifyFeature(box),
            del: new OpenLayers.Control.SelectFeature([lanes, vectors, box], {toggle: false, autoActivate:true}),
            none: new OpenLayers.Control.SelectFeature([laneMarkers, box, vectors], {toggle:true, autoActivate:true}),
            measure: new OpenLayers.Control.Measure(
                    OpenLayers.Handler.Path, {
                        persist: true,
                        immediate: true,
                        geodesic: true,
                        displaySystem: 'metric',
                        eventListeners: {
                            'measurepartial': updateDisplay
                        }
                    })
        };
    
	controls.edit.mode = OpenLayers.Control.ModifyFeature.DRAG | OpenLayers.Control.ModifyFeature.RESIZE | OpenLayers.Control.ModifyFeature.ROTATE;

	for(var key in controls) {
		map.addControl(controls[key]);
	}
	//Temporarily commented out
    // map.events.register("moveend", map, tileAge);
    
    map.addLayers([aerial, road, hybrid, laneConnections, box, laneMarkers, lanes, vectors, errors, laneWidths]);
    try {
        var location = new OpenLayers.LonLat(view_lon, view_lat);
        location.transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());
        map.setCenter(location, view_zoom);
    }
    catch (err) {
        console.log("No vectors to reset view");
    }

    $('#OpenLayers_Control_MinimizeDiv_innerImage').attr('src', "img/layer-switcher-minimize.png");
    $('#OpenLayers_Control_MaximizeDiv_innerImage').attr('src', "img/layer-switcher-maximize.png");

    //Init toggle switches for the layers

}


/*********************************************************************************************************************/
/**
 * Purpose: copies measurement to clipboard
 * @params  measurement value
 * @event copy
 */

function copyTextToClipboard(text) {
    var textArea = document.createElement("textarea");

    //
    // *** This styling is an extra step which is likely not required. ***
    //
    // Why is it here? To ensure:
    // 1. the element is able to have focus and selection.
    // 2. if element was to flash render it has minimal visual impact.
    // 3. less flakyness with selection and copying which **might** occur if
    //    the textarea element is not visible.
    //
    // The likelihood is the element won't even render, not even a flash,
    // so some of these are just precautions. However in IE the element
    // is visible whilst the popup box asking the user for permission for
    // the web page to copy to the clipboard.
    //

    // Place in top-left corner of screen regardless of scroll position.
    textArea.style.position = 'fixed';
    textArea.style.top = 0;
    textArea.style.left = 0;

    // Ensure it has a small width and height. Setting to 1px / 1em
    // doesn't work as this gives a negative w/h on some browsers.
    textArea.style.width = '2em';
    textArea.style.height = '2em';

    // We don't need padding, reducing the size if it does flash render.
    textArea.style.padding = 0;

    // Clean up any borders.
    textArea.style.border = 'none';
    textArea.style.outline = 'none';
    textArea.style.boxShadow = 'none';

    // Avoid flash of white box if rendered for any reason.
    textArea.style.background = 'transparent';


    textArea.value = text;

    document.body.appendChild(textArea);

    textArea.select();

    try {
        var successful = document.execCommand('copy');
        var msg = successful ? 'successful' : 'unsuccessful';
        console.log('Copying text command was ' + msg);
    } catch (err) {
        console.log('Oops, unable to copy');
    }

    document.body.removeChild(textArea);
}

/**
 * Purpose: removes features from the map
 * @params  map layers and features
 * @event remove features and all of it's metadata
 */

function Clear(){
	var r = confirm("Clear and reset all of the map features?");
	if (r == true) {
		lanes.destroyFeatures();
		laneMarkers.destroyFeatures();
		vectors.destroyFeatures();
		box.destroyFeatures();
        errors.clearMarkers();
        deleteTrace();
        laneWidths.destroyFeatures();
	}

    $("#map-type").text("");
    $("#builder, #drawLanes, #editLanes, #measureLanes, #drawStopBar, #editStopBar, #deleteMarker, #approachControlLabel, #laneControlLabel, #measureControlLabel, #dragSigns").hide();
}

function deleteMarker(layer, feature) {
    try {
        if (selected == "child" &&
        		(typeof feature.attributes.marker != 'undefined') &&
        		(feature.attributes.marker.name == "Verified Point Marker" || feature.attributes.marker.name == "Reference Point Marker")) {
            alert("Cannot delete a reference point in a child map.")
        } else {
    		// Computed lanes are dependent on the indexing of source lanes.
    		// When removing a lane we must update all computed lanes references
			if(layer === lanes) {
        		// Find the index of this lane, we only care about source lanes after this index
        		var i;
        		for(i = 0; i < layer.features.length; i++) {
        			if(layer.features[i].attributes.laneNumber == feature.attributes.laneNumber) {
        				break;
        			}
        		}
        		
        		// We only care about computed lanes after the index of this lane being deleted
        		// since there is no way computed lanes before this lane could refer to lanes created after
        		for(var c = i; c < layer.features.length; c++) {
        			if(layer.features[c].attributes.computed) {
        				var r = Number(layer.features[c].attributes.referenceLaneNumber);
        				if(r > i) {
        					// This computed lane references a lane after the lane we are deleting.
        					// This referenced lane will slide down an index when this lane is deleted
        					// so reflect the change in the reference.
        					layer.features[c].attributes.referenceLaneNumber = r-1;
        				}
        			}
        		}
        	}
        	$("#attributes").hide();
            layer.removeFeatures(feature);
        }
    } catch (err){

    	// Computed lanes are dependent on the indexing of source lanes.
		// When removing a lane we must update all computed lanes references
		if(layer === lanes) {
    		// Find the index of this lane, we only care about source lanes after this index
    		var i;
    		for(i = 0; i < layer.features.length; i++) {
    			if(layer.features[i].attributes.laneNumber == feature.attributes.laneNumber) {
    				break;
    			}
    		}
    		
    		// We only care about computed lanes after the index of this lane being deleted
    		// since there is no way computed lanes before this lane could refer to lanes created after
    		for(var c = i; c < layer.features.length; c++) {
    			if(layer.features[c].attributes.computed) {
    				var r = Number(layer.features[c].attributes.referenceLaneNumber);
    				if(r > i) {
    					// This computed lane references a lane after the lane we are deleting.
    					// This referenced lane will slide down an index when this lane is deleted
    					// so reflect the change in the reference.
    					layer.features[c].attributes.referenceLaneNumber = r-1;
    				}
    			}
    		}
    	}
		
        $("#attributes").hide();
        layer.removeFeatures(feature);
    }
    
    layer.redraw();
}


/*********************************************************************************************************************/
/**
 * Purpose: toggle control of all the layers and modal windows
 * @params  click events and the corresponding feature type
 * @event loads help or the drawing control with a specific element in mind
 */

function toggleControlsOn(state) {
	if( state == 'help'){
		$("#instructions_modal").modal('show');
	} else {
	$("#instructions_modal").modal('hide');
	toggleControl(state);
        if( state == 'modify' || state == 'del') {
            laneMarkers.destroyFeatures();
            controls.del.unselectAll();
        } else {
            onFeatureAdded();
        }
    }
}

function toggleControl(element) {
    for(key in controls) {
        var control = controls[key];
        if(element == key) {
            control.activate();
        } else {
            control.deactivate();
            $('.measurement').text('');
        }
    }
}

function unselectFeature( feature ) {

	resetLaneAttributes()

	if( feature.layer != null ) {
		console.log("unselecting ", feature)
		controls.none.unselect( feature );
	}
}

//this is just chillin' not sure what it's for :-/
var tmp = 0;

/*********************************************************************************************************************/
/**
 * Purpose: dot functions that bind the metadata to the feature object
 * @params  the feature and it's metadata
 * @event creates variables attached to the feature object and store the values
 */

function onFeatureAdded(){

	laneMarkers.destroyFeatures();
	for(var i=0; i< lanes.features.length; i++){

        var max = lanes.features[i].geometry.getVertices().length;
        
        if (typeof lanes.features[i].attributes.elevation == 'undefined') {
            lanes.features[i].attributes.elevation = [];
        }
        if (typeof lanes.features[i].attributes.laneWidth == 'undefined') {
            lanes.features[i].attributes.laneWidth = [];
        } else if (lanes.features[i].attributes.laneWidth.constructor !== Array) {
        	// Old maps may contain laneWidth as a single value, so initialize an empty array
        	lanes.features[i].attributes.laneWidth = [];
        	for(var z = 0; z < max; z++) {
        		lanes.features[i].attributes.laneWidth[z] = 0;
        	}
        }
        
        var nodeElevations = (lanes.features[i].attributes.elevation).slice(0);
        var nodeLaneWidths = (lanes.features[i].attributes.laneWidth).slice(0);
        
		if(!lanes.features[i].attributes.computed) {
			// Loop through the lane vertices and see if there are any new dots to
			for(var j=0; j< max; j++){
				// If the laneIndex marker doesn't exist in this vertex, this is a new dot
				if(typeof lanes.features[i].geometry.components[j].nodeInitialized === 'undefined') {
					if(isLoadMap) {
						// Saved maps will not include the nodeInitialized flag.
						// Inject this flag when loading a map
						lanes.features[i].geometry.components[j].nodeInitialized = true;
					} else {
						// Insert dummy values into the laneWidth and elevation arrays so that
						// they are the correct size for the next loop through.  This is done because
						// when a new dot is found, we need to perform a call to look up elevation which
						// is an asynchronous call.  If we try to do this in a single pass over the vertices
						// the call can take too long to return and the code iterates to the next nodes
						// but a value has not yet been inserted into the elevation array causing the array
						// to be too small.
						(lanes.features[i].attributes.elevation).splice(j, 0, "tempValue");
						(lanes.features[i].attributes.laneWidth).splice(j, 0, "tempValue");
					}
				}
			}
			
			// Now run through and 
	        for(var j=0; j< max; j++){
	        	
	        	// Create a dot & latlon for this line vertex
	 			var dot = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Point(lanes.features[i].geometry.getVertices()[j].x, lanes.features[i].geometry.getVertices()[j].y));
	            var latlon = new OpenLayers.LonLat(dot.geometry.x, dot.geometry.y).transform(toProjection, fromProjection);

	        	// If the laneIndex marker doesn't exist in this vertex, this is a new dot
				if(typeof lanes.features[i].geometry.components[j].nodeInitialized === 'undefined') {
					// Insert new values for elevation and laneWidth for the new dot
					getElevation(dot, latlon, i, j, function(elev, i, j, latlon, dot){
						lanes.features[i].attributes.elevation[j] = {'value': elev, 'edited': true, 'latlon': latlon};
					});
					lanes.features[i].attributes.laneWidth[j] = 0;
					
					// If this is a source lane for computed lanes, record this index as a new node
					if(lanes.features[i].attributes.source) {
		            	if(typeof lanes.features[i].attributes.newNodes === 'undefined') {
	                		lanes.features[i].attributes.newNodes = [];
	                		
	                		// Count how many computes lane exist for this source lane
	                		lanes.features[i].attributes.computedLaneCount = 0;
	                		for(var c = 0; c < lanes.features.length; c++) {
	            				if(lanes.features[c].attributes.computed &&
	            				  lanes.features[c].attributes.referenceLaneID == lanes.features[i].attributes.laneNumber) {
	            					lanes.features[i].attributes.computedLaneCount++;
	            				}
	            			}
			            }
		            	lanes.features[i].attributes.newNodes.push(j);
		            }
					
					// Mark the dot as being seen
					lanes.features[i].geometry.components[j].nodeInitialized = true;
				} else {
					// This node already existed
					
					// Compare the latitude and longitude from the existing lane values to see if the node moved
					var latMatch = ((lanes.features[i].attributes.elevation[j].latlon.lat).toString().match(/^-?\d+(?:\.\d{0,11})?/)[0] == (latlon.lat).toString().match(/^-?\d+(?:\.\d{0,11})?/)[0]);
					var lonMatch = ((lanes.features[i].attributes.elevation[j].latlon.lon).toString().match(/^-?\d+(?:\.\d{0,11})?/)[0] == (latlon.lon).toString().match(/^-?\d+(?:\.\d{0,11})?/)[0]);

					// If the node elevation has never been edited or has moved along either axis,
					// get a new elevation value
					if (!lanes.features[i].attributes.elevation[j].edited || !latMatch || !lonMatch){
		                getElevation(dot, latlon, i, j, function(elev, i, j, latlon, dot){
		                    lanes.features[i].attributes.elevation[j] = {'value': elev, 'edited': true, 'latlon': latlon};
		                });
		            }
				}

                buildDots(i, j, dot, latlon);
			}
		} else {
	    	buildComputedFeature(i,
								lanes.features[i].attributes.laneNumber,
								lanes.features[i].attributes.referenceLaneID,
				        		lanes.features[i].attributes.referenceLaneNumber,
				        		lanes.features[i].attributes.offsetX,
				        		lanes.features[i].attributes.offsetY,
				        		lanes.features[i].attributes.rotation,
				        		lanes.features[i].attributes.scaleX,
				        		lanes.features[i].attributes.scaleY,
				        		lanes.features[i].attributes.computedLaneID);
		}
	}

    if (laneWidths.features.length != 0) {
        laneWidths.destroyFeatures();
        toggleWidthArray();
    }
}

function buildDots(i, j, dot, latlon){

	// Don't look at computed dots, they are handled by other functions
	if(!lanes.features[i].attributes.computed) {
    	dot.attributes={"lane": i, "number": j, "LatLon": latlon,
    		"descriptiveName" : lanes.features[i].attributes.descriptiveName,
        	"laneNumber": lanes.features[i].attributes.laneNumber, "laneWidth": lanes.features[i].attributes.laneWidth, "laneType": lanes.features[i].attributes.laneType, "sharedWith": lanes.features[i].attributes.sharedWith,
	        "stateConfidence": lanes.features[i].attributes.stateConfidence, "spatRevision": lanes.features[i].attributes.spatRevision, "signalGroupID": lanes.features[i].attributes.signalGroupID, "lane_attributes": lanes.features[i].attributes.lane_attributes,
    	    "startTime": lanes.features[i].attributes.startTime, "minEndTime": lanes.features[i].attributes.minEndTime, "maxEndTime": lanes.features[i].attributes.maxEndTime,
        	"likelyTime": lanes.features[i].attributes.likelyTime, "nextTime": lanes.features[i].attributes.nextTime, "signalPhase": lanes.features[i].attributes.signalPhase, "typeAttribute": lanes.features[i].attributes.typeAttribute,
	        "connections": lanes.features[i].attributes.connections, "elevation": lanes.features[i].attributes.elevation[j],
    	    "computed": lanes.features[i].attributes.computed, "source": lanes.features[i].attributes.source
	    };
	    
	    laneMarkers.addFeatures(dot);
	}
}

function placeComputedLane(newDotFeature) {
	var newX = newDotFeature.geometry.x;
	var newY = newDotFeature.geometry.y;
	
	// We no longer need the newDotFeature since we only needed to save it's x/y values
	// to calculate the offset from the old x/y values
	lanes.removeFeatures(newDotFeature);
	
	if(computingLane) {
		// NOTE: computedLaneSource when computing a new lane is the dot 0 of
		// the source lane & not the source lane itself.  This is because
		// when setting the referenceLaneID and referenceLaneNumber, the source
		// lane does not keep those attributes, but it's dots do.
		
		// Get the offset from the first point of the source lane
	    // Note: Measurement is in meters so multiply by 100 for CM
	    var offsetX = Math.round((newX - lanes.features[computedLaneSource.attributes.lane].geometry.components[0].x) * 100);
	    var offsetY = Math.round((newY - lanes.features[computedLaneSource.attributes.lane].geometry.components[0].y) * 100);
	    
	    var inRange = true;
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
		    $('#shared_with').multiselect('select', sharedWith);
			for (var i = 0; i < laneTypeOptions.length; i++) {
				if (laneTypeOptions[i] != typeAttributeNameSaved && $('.' + laneTypeOptions[i] + '_type_attributes').length !== 0) {
					$('#' + laneTypeOptions[i] + '_type_attributes').multiselect('deselectAll', false);
					$('#' + laneTypeOptions[i] + '_type_attributes').multiselect('refresh');
				}
			}
		    removeSpeedForm();
			$('#attributes').parsley().reset();
			// Don't do anything to the connections, we want to preserve them
		    //rebuildConnections([]);
		    $("#referenceLaneID").val(lanes.features[computedLaneSource.attributes.lane].attributes.laneNumber);
		    $("#referenceLaneNumber").val(computedLaneSource.attributes.lane);
		    $('input[name=include-spat]').attr('checked',false);
		    $('.phases').hide();
		    stateConfidence = null;
		    signalPhase = null;
		    laneNum = null;
		    nodeLaneWidth = [];
		    
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
			show_rga_fields(hide=true);
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
			var nextAvailableLaneNum = $('#lane_number .dropdown-menu li:not([style*="display: none"]):first').text();
		    $('#lane_number .dropdown-toggle').html(nextAvailableLaneNum + " <span class='caret'></span>");
		    laneNum = nextAvailableLaneNum;
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
		    toggleControlsOn("none");
	    }
	}
	else {		
		// NOTE: When editing an existing computed lane, the computedLaneSource
		// is the computed lane itself, not any of it's dots.
		
	    // Get the offset from the first old point of the computed lane
		var offsetX = Math.round((newX - computedLaneSource.geometry.components[0].x) * 100);
	    var offsetY = Math.round((newY - computedLaneSource.geometry.components[0].y) * 100);

		// Combining the offsets with the computed lane's current offsets will give the
	    // amount of offset from the source lane
	    var offsetXFromSource = Number(computedLaneSource.attributes.offsetX) + offsetX;
	    var offsetYFromSource = Number(computedLaneSource.attributes.offsetY) + offsetY;
		
	    var inRange = true;
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
			computedLaneSource.attributes.offsetX = offsetXFromSource;
			computedLaneSource.attributes.offsetY = offsetYFromSource;
			
			// Unset the source computed lane since we are done moving it
			computedLaneSource = null;
	
			// This will force a re-draw to show where the lane has moved
			onFeatureAdded();
			
			// Toggle the control back to lane editing since this is what the user
			// was in when they selected the lane
			toggleControlsOn("modify");
		}
	}
}

function buildComputedFeature(i, laneNumber, referenceLaneID, referenceLaneNumber, offsetX, offsetY, rotation, scaleX, scaleY, computedLaneID){

	var r = Number(referenceLaneNumber);
	var max = lanes.features[r].geometry.getVertices().length;

	var initialize = false;
	if(typeof computedLaneID === 'undefined') {
		computedLaneID = Math.random().toString(36).substr(2, 9);
		initialize = true;
	}

	var points = [];
	var zeroLatlon = "";
	for (var j = 0; j < max; j++) {
		if (j == 0 ){
			// Apply offset to first dot's lat/lon.  No scaling or rotation needs to be performed
			var zeroPoint = new OpenLayers.Geometry.Point(
					lanes.features[r].geometry.getVertices()[j].x + offsetX / 100,
					lanes.features[r].geometry.getVertices()[j].y + offsetY / 100);
			var zeroDot = new OpenLayers.Feature.Vector(zeroPoint);
			zeroLatlon = new OpenLayers.LonLat(zeroDot.geometry.x, zeroDot.geometry.y).transform(toProjection, fromProjection);
			points.push(zeroPoint);
			buildComputedDot(i, j, laneNumber,
								referenceLaneID, referenceLaneNumber, 
								zeroDot, zeroLatlon,
								offsetX, offsetY,
								rotation,
								scaleX, scaleY,
								computedLaneID,
								initialize);
		} else {
			// Apply offset & scaling to dot
			var deltaScaleX = 
				(lanes.features[r].geometry.getVertices()[j].x - lanes.features[r].geometry.getVertices()[0].x) * scaleX/100;
			var deltaScaleY = 
				(lanes.features[r].geometry.getVertices()[j].y - lanes.features[r].geometry.getVertices()[0].y) * scaleY/100;
			var tempPoint = new OpenLayers.Geometry.Point(
					lanes.features[r].geometry.getVertices()[j].x + deltaScaleX + (offsetX / 100),
					lanes.features[r].geometry.getVertices()[j].y + deltaScaleY + (offsetY / 100));
			var tempDot = new OpenLayers.Feature.Vector(tempPoint);
			var tempLatlon = new OpenLayers.LonLat(tempDot.geometry.x, tempDot.geometry.y).transform(toProjection, fromProjection);
			
			// Apply rotation
			var inverse = inverseVincenty(zeroLatlon.lat, zeroLatlon.lon, tempLatlon.lat, tempLatlon.lon);
			var direct = directVincenty(zeroLatlon.lat, zeroLatlon.lon,
							Number(inverse.bearing) + Number(rotation), Number(inverse.distance));
			var newPoint = new OpenLayers.Geometry.Point(direct.lon, direct.lat).transform(fromProjection, toProjection);
			var newDot = new OpenLayers.Feature.Vector(newPoint);
			var newLatlon = new OpenLayers.LonLat(newDot.geometry.x, newDot.geometry.y).transform(toProjection, fromProjection);
			
			points.push(newPoint);
			buildComputedDot(i, j, laneNumber,
								referenceLaneID, referenceLaneNumber,
								newDot, newLatlon,
								offsetX, offsetY,
								rotation,
								scaleX, scaleY,
								computedLaneID,
								initialize);
		}
	}
	connectComputedDots(i, points, initialize);

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

function buildComputedDot(i, j, laneNumber, referenceLaneID, referenceLaneNumber, dot, latlon, offsetX, offsetY, rotation, scaleX, scaleY, computedLaneID, initialize){
	if(typeof initialize === 'undefined') {
		initialize = false;
	}

	var r = Number(referenceLaneNumber);
	
	if(initialize) {
		dot.attributes={"lane": i, "number": j, "LatLon": latlon,
	    		"descriptiveName" : "",
		        "laneNumber": laneNumber, "laneWidth": lanes.features[r].attributes.laneWidth, "laneType": lanes.features[r].attributes.laneType, "sharedWith": lanes.features[r].attributes.sharedWith,
		        "stateConfidence": lanes.features[r].attributes.stateConfidence, "spatRevision": lanes.features[r].attributes.spatRevision, "signalGroupID": lanes.features[r].attributes.signalGroupID, "lane_attributes": lanes.features[r].attributes.lane_attributes,
		        "startTime": lanes.features[r].attributes.startTime, "minEndTime": lanes.features[r].attributes.minEndTime, "maxEndTime": lanes.features[r].attributes.maxEndTime,
		        "likelyTime": lanes.features[r].attributes.likelyTime, "nextTime": lanes.features[r].attributes.nextTime, "signalPhase": lanes.features[r].attributes.signalPhase, "typeAttribute": lanes.features[r].attributes.typeAttribute,
		        "connections": lanes.features[r].attributes.connections, "elevation": lanes.features[r].attributes.elevation[j].value,
		        "computed" : true,
		        "computedLaneID": computedLaneID, "referenceLaneID": referenceLaneID, "referenceLaneNumber": referenceLaneNumber, "offsetX": offsetX, "offsetY": offsetY, "rotation": rotation, "scaleX": scaleX, "scaleY": scaleY
		    };
	} else {
		dot.attributes={"lane": i, "number": j, "LatLon": latlon,
	    		"descriptiveName" : lanes.features[i].attributes.descriptiveName,
		        "laneNumber": laneNumber, "laneWidth": lanes.features[i].attributes.laneWidth, "laneType": lanes.features[i].attributes.laneType, "sharedWith": lanes.features[i].attributes.sharedWith,
		        "stateConfidence": lanes.features[i].attributes.stateConfidence, "spatRevision": lanes.features[i].attributes.spatRevision, "signalGroupID": lanes.features[i].attributes.signalGroupID, "lane_attributes": lanes.features[i].attributes.lane_attributes,
		        "startTime": lanes.features[i].attributes.startTime, "minEndTime": lanes.features[i].attributes.minEndTime, "maxEndTime": lanes.features[i].attributes.maxEndTime,
		        "likelyTime": lanes.features[i].attributes.likelyTime, "nextTime": lanes.features[i].attributes.nextTime, "signalPhase": lanes.features[i].attributes.signalPhase, "typeAttribute": lanes.features[i].attributes.typeAttribute,
		        "connections": lanes.features[i].attributes.connections,
		        "computed" : lanes.features[i].attributes.computed, "computedLaneID": lanes.features[i].attributes.computedLaneID,
		        "referenceLaneID": lanes.features[i].attributes.referenceLaneID, "referenceLaneNumber": lanes.features[i].attributes.referenceLaneNumber,
		        "offsetX": lanes.features[i].attributes.offsetX, "offsetY": lanes.features[i].attributes.offsetY,
		        "rotation": lanes.features[i].attributes.rotation,
		        "scaleX": lanes.features[i].attributes.scaleX, "scaleY": lanes.features[i].attributes.scaleY
		    };
		
		// Elevation value depends on an array based on the number of nodes in the lane.
		// If a new vertex was added to the source lane via edit mode, then this has shifted
		// the lane's
		var elevationVal;
		if(typeof lanes.features[r].attributes.newNodes !== 'undefined') {
			// There are nodes added via editing
			if(lanes.features[r].attributes.newNodes.includes(j)) {
				// The point at this index is a new point, it does not have any saved data so default to 0
				elevationVal = 0;
			} else {
				// The point at this index is not a new point
				elevationVal = lanes.features[i].attributes.elevation[j].value;
			}
		} else {
			// No points were added to the source, copy elevation value directly
			elevationVal = lanes.features[i].attributes.elevation[j].value;
		}
		
		dot.attributes.elevation = elevationVal;
	}
	
    laneMarkers.addFeatures(dot);
}

function connectComputedDots(i, points, initialize){
	if(typeof initialize === 'undefined') {
		initialize = false;
	}

	var computedLanePoints = new OpenLayers.Geometry.LineString(points);

    if(initialize) {
    	var computedLaneFeat = new OpenLayers.Feature.Vector(computedLanePoints);
    	var m;
    	for (var k = 0; k < laneMarkers.features.length; k++) {
    		if(laneMarkers.features[k].attributes.lane == i && laneMarkers.features[k].attributes.number == 0) {
    			// The first node of the matching laneMarkers
    			m = k;
    			break;
    		}
    	}

        var r = laneMarkers.features[m].attributes.referenceLaneNumber;
	    computedLaneFeat.attributes={
		        "connections": laneMarkers.features[m].attributes.connections, "elevation": lanes.features[r].attributes.elevation,
		        "laneNumber": laneMarkers.features[m].attributes.laneNumber, "laneType": laneMarkers.features[m].attributes.laneType,
		        "laneWidth": laneMarkers.features[m].attributes.laneWidth, "lane_attributes": laneMarkers.features[m].attributes.lane_attributes,
		        "likelyTime": laneMarkers.features[m].attributes.likelyTime, "maxEndTime": laneMarkers.features[m].attributes.maxEndTime,
		        "minEndTime": laneMarkers.features[m].attributes.minEndTime,"nextTime": laneMarkers.features[m].attributes.nextTime,
		        "sharedWith": laneMarkers.features[m].attributes.sharedWith,
		        "signalGroupID": laneMarkers.features[m].attributes.signalGroupID, "signalPhase": laneMarkers.features[m].attributes.signalPhase,
		        "spatRevision": laneMarkers.features[m].attributes.spatRevision,
		        "startTime": laneMarkers.features[m].attributes.startTime,
		        "stateConfidence": laneMarkers.features[m].attributes.stateConfidence,
		        "typeAttribute": laneMarkers.features[m].attributes.typeAttribute,
		        
		        "computed": laneMarkers.features[m].attributes.computed, "computedLaneID": laneMarkers.features[m].attributes.computedLaneID,
		        "referenceLaneID": laneMarkers.features[m].attributes.referenceLaneID, "referenceLaneNumber": laneMarkers.features[m].attributes.referenceLaneNumber,
		        "offsetX": laneMarkers.features[m].attributes.offsetX, "offsetY": laneMarkers.features[m].attributes.offsetY,
		        "rotation": laneMarkers.features[m].attributes.rotation,
		        "scaleX": laneMarkers.features[m].attributes.scaleX, "scaleY": laneMarkers.features[m].attributes.scaleY
		    };
	    
	    // Initialize the elevations lat/lon to match the laneMarkers
	    for(var l = 0; l < computedLaneFeat.attributes.elevation.length; l++) {
	    	for (var k = 0; k < laneMarkers.features.length; k++) {
	    		if(laneMarkers.features[k].attributes.lane == i && laneMarkers.features[k].attributes.number == l) {
	    			computedLaneFeat.attributes.elevation[l].latlon = laneMarkers.features[k].attributes.LatLon;
	    			break;
	    		}
	    	}
	    }
	    
    	lanes.addFeatures(computedLaneFeat);
    } else {
    	var r = lanes.features[i].attributes.referenceLaneNumber;
		for(var j = 0; j < computedLanePoints.components.length; j++) {
			if(typeof lanes.features[r].attributes.newNodes !== 'undefined' &&
					lanes.features[r].attributes.newNodes.includes(j)) {
				// The source lane had points added via edit and this is one of them
				var newPoint = new OpenLayers.Geometry.Point(
													computedLanePoints.components[j].x,
													computedLanePoints.components[j].y);
				lanes.features[i].geometry.addPoint(newPoint, j);
			}
			else {
				lanes.features[i].geometry.components[j].move(
											computedLanePoints.components[j].x - lanes.features[i].geometry.components[j].x,
											computedLanePoints.components[j].y - lanes.features[i].geometry.components[j].y);
			}
		}
		lanes.redraw();
    }
}

function selectComputedFeature(laneNum) {
    for (var i = 0; i < laneMarkers.features.length; i++) {
    	if(laneMarkers.features[i].attributes.computed &&
    			laneMarkers.features[i].attributes.number == 0 &&
    			laneMarkers.features[i].attributes.laneNumber == laneNum) {
    		return laneMarkers.features[i];
    	}
    }
}


/*********************************************************************************************************************/
/**
 * Purpose: drag handler for the vector layer
 * @params  the feature and it's metadata
 * @event creates variables attached to the feature object and store the values
 */

function dragHandler () {
	var selectFeature = new OpenLayers.Control.SelectFeature(vectors,{
		toggle:true
	});

    return new OpenLayers.Control.DragFeature(vectors, {
		autoActivate: true,
		clickFeature: function(feature) {
			selectFeature.clickFeature(feature);
		},
		clickoutFeature: function(feature) {
			selectFeature.clickoutFeature(feature);
		},
		onStart: function(feature){
			selected_marker = feature;
		},
        onComplete: function() {
            console.log("dragged: ", this.feature);
			updateFeatureLocation( this.feature )
        }
    });
}


/*********************************************************************************************************************/
/**
 * Purpose: creates sidebar element for the individual roadsigns
 * @params  the feature and it's metadata
 * @event loads the sidebar and all of the metadata into the forms
 */

function referencePointWindow(feature){
	$("#attributes").hide();
	//---------------------------------------
	$(".selection-panel").text('Reference Point Configuration');
    $(".lane-info-tab").find('a:contains("Lane Info")').text('Marker Info');
    $(".lane-info-tab").find('a:contains("Approach Info")').text('Marker Info');
    $('#lane-info-tab').addClass('active');
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
	$("#lane_attributes").hide();
	$(".lane_type_attributes").hide();
	$(".lane_number").hide();
	$(".lane_width").hide();
	$(".descriptive_name").hide();
	$(".lane_type").hide();
	$(".approach_type").hide();
	$(".verified_lat").hide();
	$(".verified_long").hide();
	$(".verified_elev").hide();
    $(".approach_name").hide();
    $(".shared_with").hide();
//    $("#clone").hide()
    $(".btnClone").hide();
	//----------------------------------------
	$(".lat").show();
	$(".long").show();
	$(".intersection").show();
	$(".region").show();
	$(".elev").show();
    $(".revision").show();
    
    //Show additional RGA related fields
    show_rga_fields(hide=false);
	$('.road_authority_id').show();
	$('.road_authority_id_type').show();
    
    $(".master_lane_width").show();
    $(".intersection_name").show();
    if (selected == "child"){
		$(".intersection-info-tab").find('a:contains("Speed Limits")').text('Intersection Info');
		$('.layer').show();
		$('.lane-speed-text').hide();		
    	$('.intersection-info-tab').show();
        $(".velocity").show();
    }
	//----------------------------------------
	if(feature.attributes.marker.name != "Reference Point Marker"){
		$(".selection-panel").text('Verified Point Configuration');
		$("#lat").prop('readonly', true);
		$("#long").prop('readonly', true);
		$("#elev").prop('readonly', true);
		$(".intersection").hide();
		$(".region").hide();
    	$(".verified_lat").show();
    	$(".verified_long").show();
    	$(".verified_elev").show();
        $(".revision").hide();
        $(".master_lane_width").hide();
        $(".intersection_name").hide();
        $(".approach_name").hide();
        $('.intersection-info-tab').hide();        
        
        show_rga_fields(hide=true);
		$('.road_authority_id').hide();
		$('.road_authority_id_type').hide();
	}
        
        if(feature.attributes.marker.name == "Reference Point Marker"){    
            //Enable or disable rga fields on reference point marker depend on whether current RGA toggle is enabled/disabled.
            enable_rga_fields(enable=rga_enabled);
        }
	
	$('#revision').val(revisionNum);
	if (! selected_marker.attributes.elevation){
		$("#elev").val("");
	} else {
		$("#elev").val(selected_marker.attributes.elevation);
	}
	
	if (! selected_marker.attributes.verifiedElev){
		$("#verified_elev").val("");
	} else {
		$("#verified_elev").val(selected_marker.attributes.verifiedElev);
	}
	 
	selected_layer = this;
	if (! selected_marker.attributes.masterLaneWidth){
		$("#master_lane_width").val("366");
	} else {
		$("#master_lane_width").val(selected_marker.attributes.masterLaneWidth);
	}

    if (! selected_marker.attributes.layerID){
        $("#layer").val("1");
    } else {
        $("#layer").val(selected_marker.attributes.layerID);
    }

    if (selected_marker.attributes.intersectionName){
		$("#intersection_name").val(selected_marker.attributes.intersectionName);
	}

	if (! selected_marker.attributes.regionID){
        $("#region").val("");
    } else {
        $("#region").val(selected_marker.attributes.regionID);
    }
    
    if (! selected_marker.attributes.majorVersion){
        $("#major_version").val("");
    } else {
        $("#major_version").val(selected_marker.attributes.majorVersion);
    }
    
    if (! selected_marker.attributes.minorVersion){
        $("#minor_version").val("");
    } else {
        $("#minor_version").val(selected_marker.attributes.minorVersion);
    }
    
    if (! selected_marker.attributes.roadAuthorityId){
        $("#road_authority_id").val("");
    } else {
        $("#road_authority_id").val(selected_marker.attributes.roadAuthorityId);
    }

	if (! selected_marker.attributes.roadAuthorityIdType){
        $("#road_authority_id_type").val("");
    } else {
        $("#road_authority_id_type").val(selected_marker.attributes.roadAuthorityIdType);
    }
    
    if (! selected_marker.attributes.mappedGeometryId){
        $("#mapped_geometry_id").val("");
    } else {
        $("#mapped_geometry_id").val(selected_marker.attributes.mappedGeometryId);
    }
    
    if (! selected_marker.attributes.contentVersion){
        $("#content_version").val("");
    } else {
        $("#content_version").val(selected_marker.attributes.contentVersion);
    }
    
    if (! selected_marker.attributes.contentDateTime){
        $("#content_date_time").val("");
    } else {
        $("#content_date_time").val(selected_marker.attributes.contentDateTime);
    }

    if (selected == "child"){
		if(feature.attributes.marker.name != "Reference Point Marker") {
			$('.btnDone').prop('disabled', true);
		} else {
			$('.btnDone').prop('disabled', false);
		}
        $('.intersection-btn').prop('disabled', false);
        $('.btnClose').prop('readonly', false);
    } else {
        $('.btnDone').prop('disabled', false);
    }
   
   if (! selected_marker.attributes.speedLimitType) {
       removeSpeedForm();
       addSpeedForm();
   } else {
       rebuildSpeedForm(selected_marker.attributes.speedLimitType);
   }

	selected_layer = feature.layer;
	$("#attributes").show();
}

 /***
  * @brief Show and hide RGA related fields. 
  * Note: extra RGA fields in addition to MAP message should only appear at the "Reference Point" dialog.
  * @param {type} Boolean show or hide RGA fields
  */
function show_rga_fields(hide=true){
    if(hide){        
       $(".extra_rga_field").hide();
    }else{        
       $(".extra_rga_field").show();
    }
}

function enable_rga_fields(enable=true){    
    if(enable){        
         $(".extra_rga_field_input").prop('disabled', false);
		 $(".extra_rga_field_input").css('backgroundColor', "#fff");
    }else{     
         $(".extra_rga_field_input").prop('disabled', true);
		 $(".extra_rga_field_input").css('backgroundColor', "#eee");
    }
	add_rga_fields_validation(enable);
}

function add_rga_fields_validation(enable=true){
	if(enable){
		$("input:text.required").attr('data-parsley-required', true);
	}else{
		$("input:text.required").attr('data-parsley-required', false);
	}
	
}

/**
 * Purpose: if lat/long is modified, it changes the location
 * @params  the feature and it's metadata
 * @event changes the location on the map by redrawing
 */

function updateFeatureLocation( feature ) {
	referencePointWindow(feature);
	feature.attributes.LonLat = (new OpenLayers.LonLat(feature.geometry.x, feature.geometry.y)).transform(toProjection, fromProjection);
	$('#long').val(feature.attributes.LonLat.lon);
	$('#lat').val(feature.attributes.LonLat.lat);
	populateRefWindow(feature, feature.attributes.LonLat.lat, feature.attributes.LonLat.lon);

    if (feature.attributes.marker.name == "Reference Point Marker") {
        if (!feature.attributes.intersectionID && !feature.attributes.intersectionIdEdit) {
            var tempLat = ((Math.abs(feature.attributes.LonLat.lat) % 1).toString().substr(3,3));
            var tempLon = ((Math.abs(feature.attributes.LonLat.lon) % 1).toString().substr(3,3));
            intersectionID = (((tempLat & 0xff) << 8) | (tempLon & 0xff)) >>> 0;
            $("#intersection").val(intersectionID);
        } else {
            intersectionID = feature.attributes.intersectionID;
            $("#intersection").val(feature.attributes.intersectionID);
        }
    }

    $("#intersection").on("propertychange change click keyup input paste", function(){
        if ($("#intersection").val() != intersectionID) {
            feature.attributes.intersectionIdEdit = true;
            feature.attributes.intersectionID = $("#intersection").val();
            intersectionID = $("#intersection").val();
        }
    });
}

function updateLaneFeatureLocation( feature ) {
	feature.attributes.LonLat = (new OpenLayers.LonLat(feature.geometry.x, feature.geometry.y)).transform(toProjection, fromProjection);
	$('#long').val(feature.attributes.LonLat.lon);
	$('#lat').val(feature.attributes.LonLat.lat);
	populateRefWindow(feature, feature.attributes.LonLat.lat, feature.attributes.LonLat.lon);
}

/*********************************************************************************************************************/
/**
 * Purpose: misc. functions that allow specific data to be visible a certain way
 * @params  -
 * @event varies
 *
 * Note: the ul/li select boxes should one da become select boxes with options, but the styling was hard to replicate
 * at first.
 */

$("#approach_type .dropdown-menu li a").click(function(){
	var selText = $(this).text();
	approachType = selText;
	$(this).parents('.btn-group').find('.dropdown-toggle').html(selText+' <span class="caret"></span>');
});

$("#phase .dropdown-menu li a").click(function(){
    var selText = $(this).text();
    signalPhase = selText;
    $(this).parents('.btn-group').find('.dropdown-toggle').html(selText+' <span class="caret"></span>');
    val = selText.substring(1,2);
    $('.phases').hide();
    $('#phase' + val).show();
});

$("#confidence .dropdown-menu li a").click(function(){
    var selText = $(this).text();
    stateConfidence = selText;
    $(this).parents('.btn-group').find('.dropdown-toggle').html(selText+' <span class="caret"></span>');
});

$("#lane_number .dropdown-menu li a").click(function(){
    var selText = $(this).text();
    laneNum = selText;
    $(this).parents('.btn-group').find('.dropdown-toggle').html(selText+' <span class="caret"></span>');
});

$("#approach_name .dropdown-menu li a").click(function(){
    var selText = $(this).text();
    approachID = selText;
    $(this).parents('.btn-group').find('.dropdown-toggle').html(selText+' <span class="caret"></span>');
});

$("#lane_type .dropdown-menu li a").click(function(){
    var selText = $(this).text();
    laneType = selText;
    $(this).parents('.btn-group').find('.dropdown-toggle').html(selText+' <span class="caret"></span>');
    toggleLaneTypeAttributes(laneType);
});

function toggleLaneTypeAttributes(attribute, values){
	for (var i = 0; i < laneTypeOptions.length; i++) {
		$('.' + laneTypeOptions[i] + '_type_attributes').parent().hide();
	}
	
	updateTypeAttributes(attribute)
	
	if ( $('.' + attribute + '_type_attributes').length === 0 ){
	    $('#' + attribute + '_type_attributes').multiselect({
	        onChange: function(option, checked){
	            updateTypeAttributes(attribute)
	        },
	        maxHeight: 200,
	        buttonClass: attribute + '_type_attributes btn btn-default',
	        buttonText: function(options, select) {
	            if (options.length === 0) {
	                return 'Select '+ attribute + ' Type Attribute'
	            } else if (options.length > 1) {
	                return options.length + ' selected';
	            } else {
	                var labels = [];
	                options.each(function() {
	                    if ($(this).attr('label') !== undefined) {
	                        labels.push($(this).attr('label'));
	                    }
	                    else {
	                        labels.push($(this).html());
	                    }
	                });
	                return labels.join(', ') + '';
	            }
	        }
	    });
	}
	
	$('#' + attribute + '_type_attributes').multiselect('deselectAll', false);
    $('#' + attribute + '_type_attributes').multiselect("refresh");
    $('.' + attribute + '_type_attributes').parent().show();
    $("label[for='lane_type_attributes']").show();
    $(".lane_type_attributes").show();
}

function updateSharedWith(){
    sharedWith_object = $('#shared_with option:selected').map(function(a, item){return item.value;})
}

function updateTypeAttributes(attribute) {
	typeAttributeName = attribute;
	typeAttribute_object = $('#' + attribute + '_type_attributes option:selected').map(function(a, item){return item.value;})
}


/**
 * Purpose: populate reference point modal window
 * @params  the feature and it's metadata
 * @event loads the appropriate data - elevation is doen through ajax
 */

function populateAttributeWindow(temp_lat, temp_lon){
	$('#lat').val(temp_lat);
	$('#long').val(temp_lon);
}

async function populateRefWindow(feature, lat, lon)
{
	const apiKey = await getApiKey();
	const geoNamesUserName = await getUsername();

	$.ajax({
		type: 'GET',
		url: intersection_url,
		data: {
			lat: lat,
			lng: lon,
			username: geoNamesUserName
		},
		datatype: 'json',
		cache: false,
		success: function(result){
            if( result.intersection ) {
            	var name = result.intersection.street1 + " & " + result.intersection.street2
                $('#intersection_name').val(name);
                feature.attributes.intersectionName = name;
            } else {
                console.log("intersection not found");
                $('#intersection_name').val("Temporary Name");
            }
		}
	});
	if(!feature.attributes.elevation)
	{
		var elev;
		$.ajax({
			url: google_elevation_url+"/"+lat+'/'+lon,
			success: function(result){
				console.log(result);
				elev = result?.elevation;
				// elev = result.resourceSets[0].resources[0].elevations[0];
				if (elev == null|| elev==undefined){
					elev = -9999; //any sea value is set to -9999 by default. This brings it back to sea level as we know it
				}else{
					elev = Math.round(elev);
				}
				if (! feature.attributes.elevation ) {
					$('#elev').val(elev);
				} else {
					if (feature.attributes.number > -1) {
						if (!feature.attributes.elevation.value) {
							$('#elev').val(elev);
						}
					}
				}
				
				if (feature.attributes.verifiedElev){
					$('#verified_elev').val(feature.attributes.verifiedElev);
				} else {
					//If verified elevation does not exist in feature, update it with new elevation value
					$('#verified_elev').val(elev);
				}
			}
		});
	}
	if (feature.attributes.verifiedLat){
		$('#verified_lat').val(feature.attributes.verifiedLat);	
	} else {
		$('#verified_lat').val(lat);
	}
	if (feature.attributes.verifiedLon){
		$('#verified_long').val(feature.attributes.verifiedLon);
	} else {
		$('#verified_long').val(lon);
	}
	
}


/*********************************************************************************************************************/
/**
 * Purpose: validate the data and save the data to the feature
 * @params  the sidebar form elements
 * @event validates all the visible data using parsley js. If it is not accepted, it turns the form locations
 * with issues red, otherwise, it allows the data object to be created and saved to the feature
 */

$(".btnDone").click(function(){
	//Update Reference Point Configuration fields with parsley attributes
	let road_authority_id = $('#road_authority_id');
	let road_authority_id_type = $('#road_authority_id_type');
	road_authority_id_type.attr('data-parsley-required','false');
	road_authority_id.attr('data-parsley-required','false');
	if($('#region').val()?.trim() === "0"){
		road_authority_id.attr('data-parsley-required','true');
		road_authority_id_type.attr('data-parsley-required','true');
	}else if(road_authority_id.val()?.length){
		road_authority_id_type.attr('data-parsley-required','true');
	}

	if(road_authority_id_type.val()?.trim()?.toLowerCase()){
		road_authority_id.attr('data-parsley-required','true');
	}

	$('#attributes').parsley().validate();

	if (selected_layer.name == "Lane Marker Layer" &&
			(computingLane ||		// Check if in computingLane since that means this is the first time this lane is created
			selected_marker.attributes.number == 0) ){
		if ( laneType != null && laneNum != null){
			dropdownCheck = true;
		} else {
			dropdownCheck = false;
		}
	
		if (laneType == null){
			$("#lane_type_check").show();
		} else {
			$("#lane_type_check").hide();
		}
		if (laneNum == null){
			$("#lane_num_check").show();
		} else {
			$("#lane_num_check").hide();
		}
	} else {
		dropdownCheck = true;
	}
	
	if ( $(".parsley-errors-list li:visible").length === 0 && dropdownCheck === true) {
			// When computing a lane, there is no initial marker since we are generating them from another lane.
			// Therefore we have to build the markers before we select the lane.
			if (selected_layer.name == "Lane Marker Layer" && computingLane){
				if (laneNum == null){
				    var selText = $("#lane_number .dropdown-menu li a").text();
				    laneNum = selText;
			    }
				
				buildComputedFeature(lanes.features.length, laneNum,
						 				$("#referenceLaneID").val(), $("#referenceLaneNumber").val(),
						 				$("#offset-X").val(), $("#offset-Y").val(),
						 				$("#rotation").val(),
						 				$("#scale-X").val(), $("#scale-Y").val());
				selected_marker = selectComputedFeature(laneNum);

				// The Latitude and Longitude text boxes in the Lane Info tab have not yet been set
				// since the lane's nodes were just created
				$('#lat').val(selected_marker.attributes.LatLon.lat);
				$('#long').val(selected_marker.attributes.LatLon.lon);
				
				 nodeLaneWidth = lanes.features[selected_marker.attributes.lane].attributes.laneWidth;
			}
			
			setLaneAttributes();
			$("#attributes").hide();
			
			updateSharedWith();
			updateTypeAttributes(typeAttributeName);
            saveConnections();
			
	        sharedWith = [];
	        for(i = 0; i < sharedWith_object.length ; i++){
	        	sharedWith[i] = sharedWith_object[i]
	        }
	        
	        typeAttributeNameSaved = typeAttributeName;
	        typeAttribute = [];
	        for(i = 0; i < typeAttribute_object.length ; i++){
	        	typeAttribute[i] = typeAttribute_object[i]
	        }
		
			var move = new OpenLayers.LonLat($('#long').val(), $('#lat').val()).transform(fromProjection, toProjection)
		
			if (selected_layer.name == "Lane Marker Layer"){   
				var currentLaneSpeedLimits = saveSpeedForm();
				(lanes.features[selected_marker.attributes.lane]).attributes.speedLimitType = currentLaneSpeedLimits;
				speedLimits = [];

				var vert = lanes.features[selected_marker.attributes.lane].geometry.components[selected_marker.attributes.number];
				vert.move(move.lon - vert.x, move.lat - vert.y);
				selected_marker.move(move);
				lanes.redraw();
				if ( selected_marker.attributes.number == 0 ) {
					selected_marker.attributes.spatRevision = $('#spat_revision').val();
					selected_marker.attributes.signalGroupID = $('#signal_group_id').val();
					selected_marker.attributes.startTime = $('#start_time').val();
					selected_marker.attributes.minEndTime = $('#min_end_time').val();
					selected_marker.attributes.maxEndTime = $('#max_end_time').val();
					selected_marker.attributes.likelyTime = $('#likely_time').val();
					selected_marker.attributes.nextTime = $('#next_time').val();
					selected_marker.attributes.sharedWith = sharedWith;
					selected_marker.attributes.typeAttribute = typeAttribute;

                        if (nodeObject != null) {
                            selected_marker.attributes.connections = nodeObject;
                            (lanes.features[selected_marker.attributes.lane]).attributes.connections = nodeObject;
                        }

	                    if (laneNum != null){
	                        selected_marker.attributes.laneNumber = laneNum;
	                        (lanes.features[selected_marker.attributes.lane]).attributes.laneNumber = laneNum;
	                    }
	                    if (laneType != null){
	                        selected_marker.attributes.laneType = laneType;
							
	                        (lanes.features[selected_marker.attributes.lane]).attributes.laneType = laneType;
	                    }
	                    if (stateConfidence != null){
	                        selected_marker.attributes.stateConfidence = stateConfidence;
	                        (lanes.features[selected_marker.attributes.lane]).attributes.stateConfidence = stateConfidence;
	                    }
	                    if (signalPhase != null){
	                        selected_marker.attributes.signalPhase = signalPhase;
	                        (lanes.features[selected_marker.attributes.lane]).attributes.signalPhase = signalPhase;
	                    }

					(lanes.features[selected_marker.attributes.lane]).attributes.descriptiveName = $('#descriptive_name').val();
					(lanes.features[selected_marker.attributes.lane]).attributes.spatRevision = $('#spat_revision').val();
					(lanes.features[selected_marker.attributes.lane]).attributes.signalGroupID = $('#signal_group_id').val();
					(lanes.features[selected_marker.attributes.lane]).attributes.startTime = $('#start_time').val();
					(lanes.features[selected_marker.attributes.lane]).attributes.minEndTime = $('#min_end_time').val();
					(lanes.features[selected_marker.attributes.lane]).attributes.maxEndTime = $('#max_end_time').val();
					(lanes.features[selected_marker.attributes.lane]).attributes.likelyTime = $('#likely_time').val();
					(lanes.features[selected_marker.attributes.lane]).attributes.nextTime = $('#next_time').val();
					(lanes.features[selected_marker.attributes.lane]).attributes.sharedWith = sharedWith;
					(lanes.features[selected_marker.attributes.lane]).attributes.typeAttribute = typeAttribute;
					(lanes.features[selected_marker.attributes.lane]).attributes.lane_attributes = selected_marker.attributes.lane_attributes;
				}
				selected_marker.attributes.LatLon = new OpenLayers.LonLat($('#long').val(), $('#lat').val());

                nodeLaneWidth[selected_marker.attributes.number] = $("#lane_width").val();
				(lanes.features[selected_marker.attributes.lane]).attributes.laneWidth = nodeLaneWidth;
                nodeLaneWidth = [];

                selected_marker.attributes.elevation = $('#elev').val();
                (lanes.features[selected_marker.attributes.lane]).attributes.elevation[selected_marker.attributes.number].value = $("#elev").val();
                (lanes.features[selected_marker.attributes.lane]).attributes.elevation[selected_marker.attributes.number].edited = true;
                 
                if(selected_marker.attributes.computed) {
					selected_marker.attributes.offsetX = $("#offset-X").val();
					selected_marker.attributes.offsetY = $("#offset-Y").val();
					selected_marker.attributes.rotation = $("#rotation").val();
					selected_marker.attributes.scaleX = $("#scale-X").val();
					selected_marker.attributes.scaleY = $("#scale-Y").val();

					(lanes.features[selected_marker.attributes.lane]).attributes.offsetX = $("#offset-X").val();
					(lanes.features[selected_marker.attributes.lane]).attributes.offsetY = $("#offset-Y").val();
					(lanes.features[selected_marker.attributes.lane]).attributes.rotation = $("#rotation").val();
					(lanes.features[selected_marker.attributes.lane]).attributes.scaleX = $("#scale-X").val();
					(lanes.features[selected_marker.attributes.lane]).attributes.scaleY = $("#scale-Y").val();
                }
            }
			
			if (selected_layer.name == "Stop Bar Layer"){
				if (approachType != null){
					selected_marker.attributes.approachType = approachType;
				}
				
	            if (approachID != null){
	                selected_marker.attributes.approachID = approachID;
	            }
			}
			
			if (selected_layer.name == "Vector Layer"){
				if (selected == "child"){
                    selected_marker.attributes.speedLimitType = saveSpeedForm();
                    selected_marker.attributes.layerID = $("#layer").val();
                    speedLimits = [];
				} else {
					selected_marker.move(move);
					if (selected_marker.attributes.marker.name == "Verified Point Marker"){
						selected_marker.attributes.verifiedLat = $("#verified_lat").val();
						selected_marker.attributes.verifiedLon = $("#verified_long").val();
						selected_marker.attributes.verifiedElev = $("#verified_elev").val();
						selected_marker.attributes.elevation = $("#elev").val();
					}
				}
				if (selected_marker.attributes.marker.name == "Reference Point Marker") {
					selected_marker.attributes.intersectionName = $("#intersection_name").val();
					selected_marker.attributes.elevation = $("#elev").val();
					selected_marker.attributes.intersectionID = $("#intersection").val();
					intersectionID = $("#intersection").val();
					selected_marker.attributes.regionID = $("#region").val();
					selected_marker.attributes.roadAuthorityIdType = $("#road_authority_id_type").val();
					selected_marker.attributes.roadAuthorityId = $("#road_authority_id").val();
					selected_marker.attributes.majorVersion = $("#major_version").val();
					selected_marker.attributes.minorVersion = $("#minor_version").val();
					selected_marker.attributes.mappedGeometryId = $("#mapped_geometry_id").val();
					selected_marker.attributes.contentVersion = $("#content_version").val();
					selected_marker.attributes.contentDateTime = $("#content_date_time").val();
					selected_marker.attributes.masterLaneWidth = $("#master_lane_width").val();
					selected_marker.attributes.revisionNum = revisionNum;
				}
			}
			$('#attributes').parsley().reset();
			unselectFeature( selected_marker );
			
			computingLane = false;
			computedLaneSource = null;
	}
	onFeatureAdded();
});


/*********************************************************************************************************************/
/**
 * Purpose: if cancel - prevents data from being stored
 * @params  the sidebar form elements
 * @event removes all form data and clears any temp objects that may be housing data so that next load can start clean
 * from the feature object
 */

$(".btnClose").click(function(){
	$("#attributes").hide();
    $('#shared_with').multiselect('deselectAll', false);
    $('#shared_with').multiselect('select', sharedWith);
	for (var i = 0; i < laneTypeOptions.length; i++) {
		if (laneTypeOptions[i] != typeAttributeNameSaved && $('.' + laneTypeOptions[i] + '_type_attributes').length !== 0) {
			$('#' + laneTypeOptions[i] + '_type_attributes').multiselect('deselectAll', false);
			$('#' + laneTypeOptions[i] + '_type_attributes').multiselect('refresh');
		}
	}
    removeSpeedForm();
	$('#attributes').parsley().reset();
    rebuildConnections([]);
    if (selected_marker != null){
    	unselectFeature( selected_marker );
    }
    $('input[name=include-spat]').attr('checked',false);
    $('.phases').hide();
    stateConfidence = null;
    signalPhase = null;
    $('#descriptive_name').val("");
    laneNum = null;
    nodeLaneWidth = [];
    onFeatureAdded();
    
    if (computingLane) {
	    $("#offset-X").val("");
	    $("#offset-Y").val("");
	    $("#rotation").val("");
	    $("#scale-X").val("");
	    $("#scale-Y").val("");
	    computingLane = false;
	    computedLaneSource = null;
    }
});

$(".btnClone").click(function(){
	computingLane = true;
	
	// The current selected_marker is the 0 node of the lane clicked to clone.
	// Set it as the source for the computed lane and then unselect it
	computedLaneSource = selected_marker;
	unselectFeature(selected_marker);
	
	// Turning on placeComputed will allow the user to select a point on the map
	// for the computed lane
	toggleControlsOn("placeComputed");
	
});


function getCookie(cname) {
    var name = cname + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    for(var i = 0; i <ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

async function getElevation(dot, latlon, i, j, callback){
	const apiKey = await getApiKey();

    $.ajax({
		url: google_elevation_url+"/"+latlon.lat+'/'+latlon.lon,
        success: function(result){
			elev = result?.elevation;
            if (elev == null || elev == undefined){
                elev = -9999; //any sea value is set to -9999 by default. This brings it back to sea level as we know it
            }else{
				elev = Math.round(elev);
			}
            callback(elev, i, j, latlon, dot);
        },
        error: function(error){
            callback(-9999, i, j, latlon, dot);
        }
    });
}

function toggleWidthArray() {

    if (laneWidths.features.length == 0) {
        var masterWidth;

        for (var f = 0; f < vectors.features.length; f++) {
            if (vectors.features[f].attributes.marker.name == "Reference Point Marker") {
                masterWidth = parseFloat(vectors.features[f].attributes.masterLaneWidth);
            }
        }

        for (var i = 0; i < lanes.features.length; i++) {

            var widthList = [];
            var widthDeltaTotal = 0;
            var flipped = false;
            var isNegative = {"value": false, "node": "", "lane": ""};

            for (var j = 0; j < lanes.features[i].geometry.components.length; j++) {

                var point1 = '';
                var point2 = '';

                if (j < lanes.features[i].geometry.components.length - 1) {
                    if (lanes.features[i].geometry.components[j].x == lanes.features[i].geometry.components[j + 1].x && lanes.features[i].geometry.components[j].y == lanes.features[i].geometry.components[j + 1].y) {
                        j++; //to prevent dots that are the exact same.
                    }
                }

                if (j < lanes.features[i].geometry.components.length - 1) {
                    point1 = new OpenLayers.LonLat(lanes.features[i].geometry.components[j].x, lanes.features[i].geometry.components[j].y).transform(toProjection, fromProjection);
                    point2 = new OpenLayers.LonLat(lanes.features[i].geometry.components[j + 1].x, lanes.features[i].geometry.components[j + 1].y).transform(toProjection, fromProjection);
                } else {
                    point1 = new OpenLayers.LonLat(lanes.features[i].geometry.components[j].x, lanes.features[i].geometry.components[j].y).transform(toProjection, fromProjection);
                    if (lanes.features[i].geometry.components[j].x == lanes.features[i].geometry.components[j - 1].x && lanes.features[i].geometry.components[j].y == lanes.features[i].geometry.components[j - 1].y) {
                        point2 = new OpenLayers.LonLat(lanes.features[i].geometry.components[j - 2].x, lanes.features[i].geometry.components[j - 2].y).transform(toProjection, fromProjection); //to prevent dots that are the exact same.
                        flipped = true;
                    } else {
                        point2 = new OpenLayers.LonLat(lanes.features[i].geometry.components[j - 1].x, lanes.features[i].geometry.components[j - 1].y).transform(toProjection, fromProjection);
                    }
                }

                var widthDelta = parseFloat(lanes.features[i].attributes.laneWidth[j]);
                if (isNaN(widthDelta) || widthDelta == null || typeof widthDelta == "undefined") {
                    widthDelta = 0
                }

                widthDeltaTotal = widthDeltaTotal + widthDelta;

                if (masterWidth + widthDeltaTotal < 0){
                    console.log(masterWidth + widthDeltaTotal)
                    isNegative = {"value": true, "node": j+1, "lane": i};
                    widthDeltaTotal = 0 - masterWidth;
                }

                var inverse = inverseVincenty(point1.lat, point1.lon, point2.lat, point2.lon);

                var direct1 = directVincenty(point1.lat, point1.lon, inverse.bearing + 90, (((masterWidth + widthDeltaTotal) / 2) / 100));
                var direct2 = directVincenty(point1.lat, point1.lon, inverse.bearing - 90, (((masterWidth + widthDeltaTotal) / 2) / 100));

                var newPoint1 = new OpenLayers.Geometry.Point(direct1.lon, direct1.lat).transform(fromProjection, toProjection);
                var newPoint2 = new OpenLayers.Geometry.Point(direct2.lon, direct2.lat).transform(fromProjection, toProjection);

                if (j == lanes.features[i].geometry.components.length - 1) {
                    j++; //flips the j value since it's the last lane point and we need to build in reverse
                }

                if (isOdd(j) && !flipped) {
                    widthList.push(newPoint1, newPoint2);
                    widthBox = new OpenLayers.Geometry.LinearRing(widthList);
                    laneWidths.addFeatures(new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Polygon([widthBox])));
                    widthList = [];
                    widthList.push(newPoint1, newPoint2);
                } else {
                    widthList.push(newPoint2, newPoint1);
                    widthBox = new OpenLayers.Geometry.LinearRing(widthList);
                    laneWidths.addFeatures(new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Polygon([widthBox])));
                    widthList = [];
                    widthList.push(newPoint2, newPoint1);
                    flipped = false;
                }

            }
        }
    } else {
        laneWidths.destroyFeatures();
    }

    if (isNegative.value){
        alert("Width deltas sum to less than zero on lane " + lanes.features[isNegative.lane].attributes.laneNumber + " at node " + isNegative.node + "!");
    }
}

function onRegionIdChangeCallback(regionId){
    if(!isNaN(regionId) && parseFloat(regionId)===0){
        $("#road_authority_id").attr('data-parsley-required', true);
        $("#road_authority_id_type").attr('data-parsley-required', true);
    }else{
        $("#road_authority_id").attr('data-parsley-required', false);
        $("#road_authority_id_type").attr('data-parsley-required', false);
    }
}

function onRoadAuthorityIdChangeCallback() {
	let roadAuthorityIdType = $("#road_authority_id_type").val();
	const roadAuthorityIdInput = $("#road_authority_id");
	// Get the Parsley instance of the input field
    const parsleyInstance = roadAuthorityIdInput.parsley();

    // Reset previous errors
    parsleyInstance.removeError('raid'); // Ensure no lingering custom errors

	if (roadAuthorityIdType !== "") {
		$("#road_authority_id").attr('data-parsley-required', true);
		let roadAuthorityIdInputVal = $("#road_authority_id").val();
		if (roadAuthorityIdInputVal != "") {
			let roadAuthorityIdInputValArr = roadAuthorityIdInputVal.split(".").map(Number);
			// Refer to this for the limit on individual components: https://luca.ntop.org/Teaching/Appunti/asn1.html
			if(roadAuthorityIdInputValArr.length < 2) {
				parsleyInstance.addError('raid', {
                    message: "For RAID, enter at least two integers separated by a period.",
                    updateClass: true
                });
                return;
			}
			if (roadAuthorityIdType === "full") {
				if (roadAuthorityIdInputValArr[0] != 0 && roadAuthorityIdInputValArr[0] != 1 && roadAuthorityIdInputValArr[0] != 2) {
					parsleyInstance.addError('raid', {
                        message: "For Full RAID, the first integer must be 0-2.",
                        updateClass: true
                    });
					return;
				}

				if ((roadAuthorityIdInputValArr[1] < 0 || roadAuthorityIdInputValArr[1] > 39) && (roadAuthorityIdInputValArr[0] == 0 || roadAuthorityIdInputValArr[0] == 1)) {
					parsleyInstance.addError('raid', {
                        message: "For Full RAID, if the first integer is either 0 or 1, the second integer cannot be greater than 39.",
                        updateClass: true
                    });
					return;
				}

				for (let r = 1; r < roadAuthorityIdInputValArr.length; r++) {
					if (roadAuthorityIdInputValArr[r] < 0 || roadAuthorityIdInputValArr[r] > 2147483647) {
						parsleyInstance.addError('raid', {
                            message: `For Full RAID, integer at position ${r + 1} cannot be greater than 2147483647.`,
                            updateClass: true
                        });
						return;
					}
				}
			} else if (roadAuthorityIdType === "relative") {
				for (let r = 0; r < roadAuthorityIdInputValArr.length; r++) {
					if (roadAuthorityIdInputValArr[r] < 0 || roadAuthorityIdInputValArr[r] > 2147483647) {
						parsleyInstance.addError('raid', {
                            message: `For Relative RAID, integer at position ${r + 1} cannot be greater than 2147483647.`,
                            updateClass: true
                        });
						return;
					}
				}
			}
		}
	} else {
		$("#road_authority_id").attr('data-parsley-required', false);
	}
}

function onMappedGeomIdChangeCallback(){
	const mappedGeomIdInput = $("#mapped_geometry_id");
	// Get the Parsley instance of the input field
    const parsleyInstance = mappedGeomIdInput.parsley();

	$("#mapped_geometry_id").attr('data-parsley-required', true);
	let mappedGeomIdInputVal = $("#mapped_geometry_id").val();

    // Reset previous errors
    parsleyInstance.removeError('mapped'); // Ensure no lingering custom errors

	if (mappedGeomIdInputVal != "") {
		let mappedGeomIdInputValArr = mappedGeomIdInputVal.split(".").map(Number);
			// Refer to this for the limit on individual components: https://luca.ntop.org/Teaching/Appunti/asn1.html
			if(mappedGeomIdInputValArr.length < 2) {
				parsleyInstance.addError('mapped', {
                    message: "For Mapped Geometry ID, enter at least two integers separated by a period.",
                    updateClass: true
                });
                return;
			}

			for (let r = 0; r < mappedGeomIdInputValArr.length; r++) {
				if (mappedGeomIdInputValArr[r] < 0 || mappedGeomIdInputValArr[r] > 2147483647) {
					parsleyInstance.addError('mapped', {
						message: `For Mapped Geometry ID, integer at position ${r + 1} cannot be greater than 2147483647.`,
						updateClass: true
					});
					return;
				}
			}
	}

}

function isOdd(num) { return (num % 2) == 1;}
