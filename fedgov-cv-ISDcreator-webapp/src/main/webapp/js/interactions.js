import { barHighlightedStyle } from "./style.js";
import { populateAttributeWindow, populateRefWindow, referencePointWindow, hideRGAFields, toggleLaneTypeAttributes, updateDisplayedLaneAttributes, rebuildConnections, rebuildSpeedForm, removeSpeedForm, addSpeedForm, resetLaneAttributes, getLength, copyTextToClipboard, updateLaneInfoTimePeriod, updateLaneInfoDaySelection, setRGAStatus, rebuildApproaches } from "./utils.js";

function laneSelectInteractionCallback(evt, overlayLayersGroup, lanes, laneWidths, laneMarkers, deleteMode, selected){
    if (evt.selected?.length > 0) {
      console.log('Lane feature selected:', evt.selected[0]);  
    }else{
      console.log("No lane feature selected, ignore");
      return;
    }

    let selectedLane = evt.selected[0];
    // Find the layer by checking which vector source contains the feature
    const laneLayer = overlayLayersGroup.getLayers().getArray().find(layer=>{
      return selectedLane && layer instanceof ol.layer.Vector && layer.getSource().hasFeature(selectedLane)
    })    

    if (deleteMode){
        if(selectedLane.get("source")) {
          // Watch out for computed lanes relying on this lane
          let dependentLanes = [];
          for(let i = 0; i < laneLayer.getSource().getFeatures().length; i++) {
            if(laneLayer.getSource().getFeatures()[i].get("computed") &&
              laneLayer.getSource().getFeatures()[i].get("referenceLaneID") == selectedLane.get("laneNumber")) {
              dependentLanes.push(laneLayer.getSource().getFeatures()[i]);
            }
          }
          let doDelete = confirm(dependentLanes.length + " computed lanes depend on this lane. " +
                      "Deleting this lane will delete them all. Continue?");

          if(doDelete) {
            dependentLanes.push(selectedLane);		// Don't forget to delete this as well
            for(let i = 0; i < dependentLanes.length; i++) {
              deleteMarker(laneLayer, dependentLanes[i], lanes, selected);
            }
            laneWidths.getSource().clear();
          }
          else {
            // controls.del.unselect(selectedLane);
          }
        }
        else if(selectedLane.get("computed")) {
          // Check if the source lane for this computed lane
          // has any other computed lanes.
          let r = Number(selectedLane.get("referenceLaneNumber"));          
          let computedCount = 0;
          for(let i = 0; i < laneLayer.getSource().getFeatures().length; i++) {
              if(laneLayer.getSource().getFeatures()[i].get("computed") &&
              laneLayer.getSource().getFeatures()[i].get("referenceLaneID")  == laneLayer.getSource().getFeatures()[r].get("laneNumber")) {
                computedCount++;
              }
          }

          if(computedCount == 1) {
            // This was the only computed lane for the source lane, it is no longer a source lane
            laneLayer.getSource().getFeatures()[r].set("source", false);
          }

          // Delete this computed lane
          deleteMarker(laneLayer,selectedLane,lanes, selected);
          laneWidths.getSource().clear();
        }
        else {
          deleteMarker(laneLayer, selectedLane,lanes, selected);
          laneWidths.getSource().clear();
        }
    }
}


function laneMarkersInteractionCallback(evt, map, overlayLayersGroup, lanes, laneConnections, deleteMode, selected, speedForm) {
  if (evt.selected?.length > 0) {
    console.log('Lane marker feature selected:', evt.selected[0]);

    let selectedMarker = evt.selected[0];
    $('#attributes').parsley().reset();    
    if(selectedMarker.get("computed")) {
      $(".selection-panel").text('Computed Lane Configuration');
    } else {
      $(".selection-panel").text('Lane Configuration');
    }
    const laneMarkersLayer = overlayLayersGroup.getLayers().getArray().find(layer=>{
      return selectedMarker && layer instanceof ol.layer.Vector && layer.getSource().hasFeature(selectedMarker)
    })
    // delete marker and return
    if (deleteMode){
        deleteMarker(laneMarkersLayer, selectedMarker, lanes, selected);
       return false;
    } else {
      updateLaneFeatureLocation( selectedMarker );
    }
   
    let laneFeatures = lanes.getSource().getFeatures();
    $('#lane_number li').show();
    for (let laneFeature of laneFeatures){
      let usedNum = laneFeature.get("laneNumber");
      $('.lane_number li').filter(function () {
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
    $("#approach-table").hide();
    $(".intersection").hide();
    $(".region").hide();
    $(".revision").hide();
    $('.phases').hide();
    $(".master_lane_width").hide();
    $(".intersection_name").hide();
    hideRGAFields(true);
    $('.road_authority_id').hide();
    $('.road_authority_id_type').hide();
    $(".approach_name").hide();
    $(".shared_with").hide();
    $(".btnClone").hide();
    //-------------------------------------
    $(".lat").show();
    $(".long").show();
    if(selectedMarker.get("computed")) {
      $("#lat").prop('readonly', true);
      $("#long").prop('readonly', true);
    }
    $(".elev").show();
    $(".spat_label").show();
    $(".lane_width").show();

    if (selectedMarker.get("number") == 0) {
      setRGAStatus();
      updateDisplayedLaneAttributes(selectedMarker);
      rebuildConnections(selectedMarker.get("connections"));
      $("#lane_attributes").show();
      $(".descriptive_name").show();
      $(".lane_type").show();
      $(".lane_number").show();
      $('.spat-info-tab').hide(); // Hiding SPaT tab. Revert to .show() when needed
      $('.connection-tab').show();
      $(".intersection-info-tab").find('a:contains("Intersection Info")').text('Speed Limits');
      $('.intersection-info-tab').show();
      $('.layer').hide();
      $('.lane-speed-text').show();
      $(".shared_with").show();
      $(".lane_info_time_restrictions").show();
      if(!selectedMarker.get("computed")) {
        // Only show the button if this lane is already defined with a lane number
        if(typeof selectedMarker.get("laneNumber") !== 'undefined') {
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
      $(".lane_info_time_restrictions").hide();
    }
    let nodeLaneWidth;
    if(laneFeatures[selectedMarker.get("lane")]?.get("laneWidth")){
        nodeLaneWidth = laneFeatures[selectedMarker.get("lane")]?.get("laneWidth");
    }

    if (nodeLaneWidth && nodeLaneWidth[selectedMarker.get("number")]){
      $("#lane_width").val(nodeLaneWidth[selectedMarker.get("number")]);      
    } else {
      $("#lane_width").val("0");
    }
    
    if (! selectedMarker.get("elevation")?.value){
      $("#elev").val("");
    } else {
      $("#elev").val(selectedMarker.get("elevation")?.value);
    }
             
    if (! selectedMarker.get("signalPhase")) {
        $('#phase .dropdown-toggle').html("Select a Signal Phase <span class='caret'></span>");
    } else {
        $('#phase .dropdown-toggle').html(selectedMarker.get("signalPhase") + " <span class='caret'></span>");
        $('#phase' + selectedMarker.get("signalPhase").substring(1, 2)).show();
    }
    
    if (! selectedMarker.get("stateConfidence") ) {
        $('#confidence .dropdown-toggle').html("Select a Confidence <span class='caret'></span>");
    } else {
        $('#confidence .dropdown-toggle').html(selectedMarker.get("stateConfidence") + " <span class='caret'></span>");
    }

    if (! selectedMarker.get("laneNumber")) {
        $('#lane_number .dropdown-toggle').html("Select a Lane Number <span class='caret'></span>");
    } else {
        $('#lane_number .dropdown-toggle').html(selectedMarker.get("laneNumber") + " <span class='caret'></span>");
    }
    
    if (! selectedMarker.get("laneType") ) {
        $('#lane_type .dropdown-toggle').html("Select a Lane Type <span class='caret'></span>");
    } else if (  selectedMarker.get("number") == 0 ) {
        $('#lane_type .dropdown-toggle').html(selectedMarker.get("laneType")  + " <span class='caret'></span>");
        toggleLaneTypeAttributes(selectedMarker.get("laneType") );
    }
    if (!laneFeatures[selectedMarker.get("lane")]?.get("speedLimitType")) {
      removeSpeedForm(speedForm);
      addSpeedForm(speedForm);
    } else {
      rebuildSpeedForm(speedForm, laneFeatures[selectedMarker.get("lane")]?.get("speedLimitType"));
    }
          
    $('#shared_with').multiselect('deselectAll', false);
    $("#shared_with").multiselect("refresh");
                    
    if (selectedMarker.get("sharedWith")) {
      $('#shared_with').multiselect('select', selectedMarker.get("sharedWith"));
      $("#shared_with").multiselect("refresh");
    }
    
    if (selectedMarker.get("typeAttribute") && selectedMarker.get("laneType")) {
      $('#' + selectedMarker.get("laneType") + '_type_attributes').multiselect('select', selectedMarker.get("typeAttribute"));
      $('#' + selectedMarker.get("laneType") + '_type_attributes').multiselect("refresh");
    } 

    updateLaneInfoDaySelection(selectedMarker.get("laneInfoDaySelection"));
    updateLaneInfoTimePeriod(selectedMarker.get("laneInfoTimePeriodType"), selectedMarker.get("laneInfoTimePeriodValue"), selectedMarker.get("laneInfoTimePeriodRange"));

    if (! selectedMarker.get("spatRevision")){
      $('#spat_revision').val(1);
    } else {
      $('#spat_revision').val(selectedMarker.get("spatRevision"));
    }

    $('#descriptive_name').val(selectedMarker.get("descriptiveName"));
    $('#signal_group_id').val(selectedMarker.get("signalGroupID"));
    $('#start_time').val(selectedMarker.get("startTime"));
    $('#min_end_time').val(selectedMarker.get("minEndTime"));
    $('#max_end_time').val(selectedMarker.get("maxEndTime"));
    $('#likely_time').val(selectedMarker.get("likelyTime"));
    $('#next_time').val(selectedMarker.get("nextTime"));              
        
    populateAttributeWindow(selectedMarker.get("LonLat").lat, selectedMarker.get("LonLat").lon);
    $("#attributes").show();


    if(evt.deselected?.length > 0) {
      //Clear previous lane connections
      laneConnections.getSource().clear();
    }
    for(let attrConnection in selectedMarker.get("connections")) {
        if (selectedMarker.get("connections").hasOwnProperty(attrConnection) && selectedMarker.get("number") == 0){
          let connection = selectedMarker.get("connections")[attrConnection];
          let startPoint;
          let endPoint;
          for (let laneFeature of laneFeatures) {
              if (laneFeature.get("laneNumber") && laneFeature.get("laneNumber") !== undefined) {
                  if (parseInt(laneFeature.get("laneNumber")) === parseInt(connection.fromLane)) {
                      startPoint = new ol.geom.Point(laneFeature.getGeometry().getCoordinates()[0], laneFeature.getGeometry().getCoordinates()[1]);
                  } else if (parseInt(laneFeature.get("laneNumber")) === parseInt(connection.toLane)) {
                      endPoint =  new ol.geom.Point(laneFeature.getGeometry().getCoordinates()[0], laneFeature.getGeometry().getCoordinates()[1]);
                  }
              }
          }
          let angleDeg = 0;
          if(typeof startPoint !== 'undefined' && typeof endPoint !== 'undefined') {
              //Q III
              const startPointCoords = startPoint.getCoordinates();
              const endPointCoords = endPoint.getCoordinates();
              if (startPointCoords[0] > endPointCoords[0] && startPointCoords[1] > endPointCoords[1]) {
                  angleDeg = 270 - (Math.atan2(startPointCoords[1] - endPointCoords[1], startPointCoords[0] - endPointCoords[0]) * 180 / Math.PI);
              }
              //Q IV
              if (startPointCoords[0] > endPointCoords[0] && startPointCoords[1] < endPointCoords[1]) {
                  angleDeg = 270 - (Math.atan2(startPointCoords[1] - endPointCoords[1], startPointCoords[0] - endPointCoords[0]) * 180 / Math.PI);
              }
              //Q II
              if (startPointCoords[0] < endPointCoords[0] && startPointCoords[1] > endPointCoords[1]) {
                  angleDeg = 90 - (Math.atan2(endPointCoords[1] - startPointCoords[1], endPointCoords[0] - startPointCoords[0]) * 180 / Math.PI);
              }
              //Q I
              if (startPointCoords[0] < endPointCoords[0] && startPointCoords[1] < endPointCoords[1]) {
                  angleDeg = 90 - (Math.atan2(endPointCoords[1] - startPointCoords[1], endPointCoords[0] - startPointCoords[0]) * 180 / Math.PI);
              }

              let xlen = endPointCoords[0] - startPointCoords[0];
              let ylen = endPointCoords[1] - startPointCoords[1];
              let hlen = Math.sqrt(Math.pow(xlen, 2) + Math.pow(ylen, 2));
              let smallerLen = hlen - 1;
              let ratio = smallerLen / hlen;
              let smallerXLen = xlen * ratio;
              let smallerYLen = ylen * ratio;
              let smallerX = startPointCoords[0] + smallerXLen;
              let smallerY = startPointCoords[1] + smallerYLen;
              laneConnections.getSource().addFeature(new ol.Feature(new ol.geom.LineString([startPointCoords, endPointCoords])));
              let pointFeat = new ol.Feature(new ol.geom.Point([smallerX, smallerY]));
              pointFeat.set("angle", angleDeg);
              laneConnections.getSource().addFeature(pointFeat);
            }
        }
    }
        
    if(selectedMarker.get("computed")) {
      //Show offsetZ in the computed tab
      $(".row.offset-Z").show();
      if (! selectedMarker.get("referenceLaneNumber")){
            $("#referenceLaneNumber").val("");
        } else {
            $("#referenceLaneNumber").val(selectedMarker.get("referenceLaneNumber"));
        }

        if (! selectedMarker.get("referenceLaneID")){
            $("#referenceLaneID").val("");
        } else {
            $("#referenceLaneID").val(selectedMarker.get("referenceLaneID"));
        }

        if (! selectedMarker.get("offsetX")){
            $("#offset-X").val("");
        } else {
            $("#offset-X").val(selectedMarker.get("offsetX"));
        }

        if (! selectedMarker.get("offsetY")){
            $("#offset-Y").val("");
        } else {
            $("#offset-Y").val(selectedMarker.get("offsetY"));
        }

        if (! selectedMarker.get("offsetZ")){
          $("#offset-Z").val("");
        } else {
            $("#offset-Z").val(selectedMarker.get("offsetZ"));
        }

        if (! selectedMarker.get("rotation")){
            $("#rotation").val("");
        } else {
            $("#rotation").val(selectedMarker.get("rotation"));
        }

        if (! selectedMarker.get("scaleX")){
            $("#scale-X").val("");
        } else {
            $("#scale-X").val(selectedMarker.get("scaleX"));
        }

        if (! selectedMarker.get("scaleY")){
            $("#scale-Y").val("");
        } else {
            $("#scale-Y").val(selectedMarker.get("scaleY"));
        }
    }
    return selectedMarker;
  } else if (evt.deselected?.length > 0) {
    console.log('Lane marker feature deselected:', evt.deselected[0]);
    if(!hasSelectedFeatures(map)) {
      $("#attributes").hide();
    }
    resetLaneAttributes();
    laneConnections.getSource().clear();
    return null;
  } else {
    console.log("No lane marker feature selected, ignore");
    return null;
  }
}

function vectorSelectInteractionCallback(evt, map, overlayLayersGroup, lanes, deleteMode, selected, rgaEnabled, speedForm){
  if (evt.selected?.length > 0) {
    console.log('Vector feature selected:', evt.selected[0]);
    let selectedVector = evt.selected[0];

    //Use the marker properties to update the marker icon
    let iconAddress = selectedVector.get("marker")?.img_src;
    if (iconAddress) {
      let IconInfo = { src: iconAddress, height: 50, width: 50 ,anchor: [0.5,1], anchorXUnits: 'fraction', anchorYUnits: 'fraction'};
      selectedVector.setStyle(new ol.style.Style({
        image: new ol.style.Icon(IconInfo)
      }));
    } else {
      console.error("Icon address not found for the selected vector.");
    }

    const vectorLayer = overlayLayersGroup.getLayers().getArray().find(layer=>{
      return selectedVector && layer instanceof ol.layer.Vector && layer.getSource().hasFeature(selectedVector)
    })

    if (deleteMode){
      deleteMarker(vectorLayer, selectedVector, lanes, selected);
    } else {
      updateFeatureLocation( selectedVector, selected, rgaEnabled, speedForm);
    }   
    return selectedVector;
  }else if (evt.deselected?.length >0 ){
    console.log('Vector feature deselected:', evt.deselected[0]);
    if(!hasSelectedFeatures(map)) {
      $("#attributes").hide();
    }
    return null;
  }else{
    console.log("No vector feature selected, ignore");
    return null;
  }  
}

function vectorAddInteractionCallback(evt, selected, rgaEnabled, speedForm) {
  if (evt.feature) {
    console.log('Vector feature added:', evt.feature);
  }else{
    console.log("No vector feature added, ignore");
    return null;
  }

  let selectedVector = evt.feature;
  updateFeatureLocation(selectedVector, selected, rgaEnabled, speedForm);
  return selectedVector;
}

function vectorDragCallback(draggedFeature,  selected, rgaEnabled, speedForm){
  updateFeatureLocation(draggedFeature,  selected, rgaEnabled, speedForm);
}

function boxSelectInteractionCallback(evt, map, overlayLayersGroup, lanes, deleteMode, selected) {
  if (evt.selected?.length > 0) {
    console.log('box/stopBar feature selected:', evt.selected[0]);
    let selectedBox = evt.selected[0];
    selectedBox.setStyle(barHighlightedStyle);
    const boxLayer = overlayLayersGroup.getLayers().getArray().find(layer=>{
      return selectedBox && layer instanceof ol.layer.Vector && layer.getSource().hasFeature(selectedBox)
    })
    if (deleteMode) {
      deleteMarker(boxLayer, selectedBox, lanes, selected);
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
      $(".lane_info_time_restrictions").hide();
      $(".lane_number").hide();
      $(".lat").hide();
      $(".long").hide();
      $(".verified_lat").hide();
      $(".verified_long").hide();
      $(".intersection").hide();
      $(".region").hide();
      hideRGAFields(true);
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
      $("#approach-table").show();
      $(".approach_name").show();
      $('#approach_name li').show();
      for (let boxFeature of boxLayer.getSource().getFeatures()) {
        let usedNum = boxFeature.get("approachID");
        $('.approach_name li:contains(' + usedNum + ')').hide();
      }
      //----------------------------------------
      $("#approach_title").val(selectedBox.get("approach"));
      $("#attributes").show();
    }
    let approaches = selectedBox.get("approaches") || []; // Use default empty array if undefined

    if (approaches.length === 0) { // Check if empty instead of undefined
      approaches.push({
        rowId: 0,
        approachType: selectedBox.get("approachType"),
        daySelection: null,
        timePeriod: null,
        selected: true,
      });
    }

    rebuildApproaches(approaches);
    

    // if (!selectedBox.get("approachType")) {
    //   $('#approach_type .dropdown-toggle').html("Select an Approach Type <span class='caret'></span>");
    // } else {
    //   $('#approach_type .dropdown-toggle').html(selectedBox.get("approachType") + " <span class='caret'></span>");
    // }
        
    if (!selectedBox.get("approachID")) {
      $('#approach_name .dropdown-toggle').html("Select an Approach ID <span class='caret'></span>");
    } else {
      $('#approach_name .dropdown-toggle').html(selectedBox.get("approachID") + " <span class='caret'></span>");
    }
    return selectedBox;
  } else if (evt.deselected?.length > 0) {
    evt.deselected[0].setStyle(null);
    console.log('box/stopBar feature deselected:', evt.deselected[0]);
    if(!hasSelectedFeatures(map)) {
      $("#attributes").hide();
    }
    return null;
  } else {
    console.log("No box/stopBar feature selected, ignore");
    return null;
  }
}
  

/**
 * Purpose: if lat/long is modified, it changes the location
 * @params  The selected feature
 * @params selected: The selected map, available values: parent or child
 * @params rgaEnabled indicator whether RGA fields are enabled
 * @params speedForm A Javascript object that contains the speed form for lane info
 */
function updateFeatureLocation( feature, selected, rgaEnabled, speedForm) {
  referencePointWindow(feature, selected, rgaEnabled, speedForm);
  let lonLatCoordinates = new ol.proj.toLonLat(feature.getGeometry().getCoordinates());
  feature.set("LonLat", {lon: lonLatCoordinates[0], lat: lonLatCoordinates[1]});
	$('#long').val(feature.get("LonLat").lon);
  $('#lat').val(feature.get("LonLat").lat);
	populateRefWindow(feature, feature.get("LonLat").lat, feature.get("LonLat").lon);
  let intersectionID  = feature.get("intersectionID");
  if (feature.get("marker").name == "Reference Point Marker") {
      if (!feature.get("intersectionID") && !feature.get("intersectionIdEdit")) {
          let tempLat = ((Math.abs(feature.get("LonLat").lat) % 1).toString().substr(3,3));
          let tempLon = ((Math.abs(feature.get("LonLat").lon) % 1).toString().substr(3,3));
          intersectionID = (((tempLat & 0xff) << 8) | (tempLon & 0xff)) >>> 0;
          $("#intersection").val(intersectionID);
      } else {
          intersectionID = feature.get("intersectionID");
          $("#intersection").val(feature.get("intersectionID"));
      }
  }

  $("#intersection").on("propertychange change click keyup input paste", function(){
      if ($("#intersection").val() != intersectionID) {
        feature.set("intersectionIdEdit",  true);
        feature.set("intersectionID", $("#intersection").val());
        intersectionID = $("#intersection").val();
      }
  });
}


function updateLaneFeatureLocation(feature) {
  let lonLatCoordinates = new ol.proj.toLonLat(feature.getGeometry().getCoordinates());
  feature.set("LonLat", {lon: lonLatCoordinates[0], lat: lonLatCoordinates[1]});
	$('#long').val(feature.get("LonLat").lon);
	$('#lat').val(feature.get("LonLat").lat);
	populateRefWindow(feature, feature.get("LonLat").lat, feature.get("LonLat").lon);
}


function deleteMarker(featureLayer, feature, lanes, selected) {
  try {
      if (selected == "child" &&
          (typeof feature.get("marker") != 'undefined') &&
          (feature.get("marker").name == "Verified Point Marker" || feature.get("marker").name == "Reference Point Marker")) {
          alert("Cannot delete a reference point in a child map.")
      } else {
        // Computed lanes are dependent on the indexing of source lanes.
        // When removing a lane we must update all computed lanes references
        if(featureLayer === lanes) {
              // Find the index of this lane, we only care about source lanes after this index
              let i;
              for(i = 0; i < featureLayer.getSource().getFeatures().length; i++) {
                if(featureLayer.getSource().getFeatures()[i].get("laneNumber") == feature.get("laneNumber")) {
                  break;
                }
              }
              
              // We only care about computed lanes after the index of this lane being deleted
              // since there is no way computed lanes before this lane could refer to lanes created after
              for(let c = i; c < featureLayer.getSource().getFeatures().length; c++) {
                if(featureLayer.getSource().getFeatures()[c].get("computed")) {
                  let r = Number(featureLayer.getSource().getFeatures()[c].get("referenceLaneNumber"));
                  if(r > i) {
                    // This computed lane references a lane after the lane we are deleting.
                    // This referenced lane will slide down an index when this lane is deleted
                    // so reflect the change in the reference.
                    featureLayer.getSource().getFeatures()[c].set("referenceLaneNumber", r-1);
                  }
                }
              }
            }
            $("#elevation").hide();
            featureLayer.getSource().removeFeature(feature);
        }
  } catch (err){
    // Computed lanes are dependent on the indexing of source lanes.
    // When removing a lane we must update all computed lanes references
    if(featureLayer === lanes) {
        // Find the index of this lane, we only care about source lanes after this index
        let i;
        for(i = 0; i < featureLayer.getSource().getFeatures().length; i++) {
          if(featureLayer.getSource().getFeatures()[i].get("elevation").laneNumber == feature.get("elevation").laneNumber) {
            break;
          }
        }
        
        // We only care about computed lanes after the index of this lane being deleted
        // since there is no way computed lanes before this lane could refer to lanes created after
        for(let c = i; c < featureLayer.getSource().getFeatures().length; c++) {
          if(featureLayer.getSource().getFeatures()[c].get("elevation").computed) {
            let r = Number(featureLayer.getSource().getFeatures()[c].get("elevation").referenceLaneNumber);
            if(r > i) {
              // This computed lane references a lane after the lane we are deleting.
              // This referenced lane will slide down an index when this lane is deleted
              // so reflect the change in the reference.
              featureLayer.getSource().getFeatures()[c].get("elevation").referenceLaneNumber = r-1;
            }
          }
        }
      }
    
      $("#elevation").hide();
      featureLayer.getSource().removeFeature(feature);
  }
}
/**
 * @description draw line control callback function when draw ended and calculate the length of the line and display the length in the measurement div
 * @param {Object} event - The event object
 */
function measureCallback(event){
  const geometry = event.feature.getGeometry();
  geometry.on('change', (event)=>{
    const length = getLength(geometry); 
    $('.measurement').text(length.toFixed(3) + ' m');
    copyTextToClipboard((length).toFixed(3));
  });  
}


// Function to check if any features are selected in the map
function hasSelectedFeatures(map) {
  const interactions = map.getInteractions().getArray();  
  // Filter Select interactions
  const selectInteractions = interactions.filter(interaction => 
    interaction instanceof ol.interaction.Select
  );
  // Check if any Select interaction has selected features
  return selectInteractions.some(select => select.getFeatures().getLength() > 0);
};


export {
  laneSelectInteractionCallback,
  vectorSelectInteractionCallback,
  laneMarkersInteractionCallback,
  vectorAddInteractionCallback,
  boxSelectInteractionCallback,
  deleteMarker,
  measureCallback,
  vectorDragCallback
}