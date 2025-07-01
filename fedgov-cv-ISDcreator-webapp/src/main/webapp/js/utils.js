
let numRows = -1;
let approachNumRows = -1;
let timeRestrictions;
let tmpLaneAttributes = {}
import { getElev, getNearestIntersectionJSON } from "./api.js";
import { toggleControlsOn } from "./files.js";

function getCookie(cname) {
  let name = cname + "=";
  let decodedCookie = decodeURIComponent(document.cookie);
  let ca = decodedCookie.split(';');
  for(let i = 0; i <ca.length; i++) {
      let c = ca[i];
      while (c.charAt(0) == ' ') {
          c = c.substring(1);
      }
      if (c.indexOf(name) == 0) {
          return c.substring(name.length, c.length);
      }
  }
  return "";
}

function isOdd(num) { return (num % 2) == 1;}


/***
 * @brief Get selected days of the week from day selection field on the lane info dialog 
 */
function getLaneInfoDaySelection() {
	let laneInfoDaySelection = []
	let daySelectionOptions = $('#lane_info_day_selection option:selected')?.map(function (a, item) { return item.value; });
	if (daySelectionOptions) {
		for (let i = 0; i < daySelectionOptions.length; i++) {
			laneInfoDaySelection.push(daySelectionOptions[i]);
		}
	}
	return laneInfoDaySelection;
}

/**
 * @brief Populate lane info dialog with selected days of the week
 */
function updateLaneInfoDaySelection(laneInfoDaySelection) {
	$('#lane_info_day_selection').multiselect('deselectAll', false);
	$("#lane_info_day_selection").multiselect("refresh");
	if (laneInfoDaySelection) {
		$("#lane_info_day_selection").multiselect('select', laneInfoDaySelection);
		$("#lane_info_day_selection").multiselect("refresh");
	}
}


/**
 * @brief Get selected time period from time period selection field on the lane info dialog
 */
function getLaneInfoTimePeriod() {
	let laneInfoTimePeriodType = "";
	let laneInfoTimePeriodValue = "";
	let laneInfoTimePeriodRange = {};

	let selectedType = $("input[name='lane_info_time_period']:checked").val();
	laneInfoTimePeriodType = selectedType ? selectedType.trim().toLowerCase() : "";

	if (laneInfoTimePeriodType === "range") {
		laneInfoTimePeriodRange = {};
		laneInfoTimePeriodRange["startDatetime"] = $('#lane_info_time_period_start_datetime').val();
		laneInfoTimePeriodRange["startOffset"] = $('#lane_info_time_period_start_offset').val();
		laneInfoTimePeriodRange["endDatetime"] = $('#lane_info_time_period_end_datetime').val();
		laneInfoTimePeriodRange["endOffset"] = $('#lane_info_time_period_end_offset').val();

		return {
			type: laneInfoTimePeriodType,
			range: laneInfoTimePeriodRange
		}
	} else if (laneInfoTimePeriodType === "general") {
		laneInfoTimePeriodValue = $('input[name="lane_info_time_period_general"]:checked').val();

		return {
			type: laneInfoTimePeriodType,
			value: laneInfoTimePeriodValue
		}
	} else {
    return {
      type: laneInfoTimePeriodType
    }
  }
}

/***
 * @brief Populate lane info dialog with selected time period
 */
function updateLaneInfoTimePeriod(laneInfoTimePeriodType, laneInfoTimePeriodValue, laneInfoTimePeriodRange) {
	if (laneInfoTimePeriodType) {
		$('input[name="lane_info_time_period"][value="' + laneInfoTimePeriodType + '"]').prop('checked', true);
	} else {
		$('input[name="lane_info_time_period"]').prop('checked', false);
	}

	$("#lane_info_time_period_start_datetime").val("");
	$("#lane_info_time_period_end_datetime").val("");
	$("#lane_info_time_period_start_offset").val("");
	$("#lane_info_time_period_end_offset").val("");
	$('input[name="lane_info_time_period_general"]').prop('checked', false);
	$('.time_period_general_fields').css('display', 'none');
	$('.time_period_range_fields').css('display', 'none');

	if (laneInfoTimePeriodRange && laneInfoTimePeriodType == "range") {
		$('.time_period_general_fields').css('display', 'none');
		$('.time_period_range_fields').css('display', '');
		$("#lane_info_time_period_start_datetime").val(laneInfoTimePeriodRange['startDatetime']);
		$("#lane_info_time_period_end_datetime").val(laneInfoTimePeriodRange['endDatetime']);
		$("#lane_info_time_period_start_offset").val(laneInfoTimePeriodRange['startOffset']);
		$("#lane_info_time_period_end_offset").val(laneInfoTimePeriodRange['endOffset']);
	} else if (laneInfoTimePeriodValue && laneInfoTimePeriodType == "general") {
		$('.time_period_range_fields').css('display', 'none');
		$('.time_period_general_fields').css('display', '');
		$('input[name="lane_info_time_period_general"][value="' + laneInfoTimePeriodValue + '"]').prop('checked', true);
	}
}

 /***
  * @brief Show and hide RGA related fields. 
  * Note: extra RGA fields in addition to MAP message should only appear at the "Reference Point" dialog.
  * @param {type} Boolean show or hide RGA fields
  */
 function hideRGAFields(hide=true){
  if(hide){        
     $(".extra_rga_field").hide();
  }else{        
     $(".extra_rga_field").show();
  }
}

/**
 * @brief Calculates geodesic length 
 * @param geometry Feature geometry 
 */
function getLength(geometry){
  if (!geometry) return 0;

  let length = 0;
  const coordinates = geometry.getCoordinates();

  for (let i = 0; i < coordinates.length - 1; i++) {
    const start = ol.proj.toLonLat(coordinates[i]);
    const end = ol.proj.toLonLat(coordinates[i + 1]);
    const segmentLength = inverseVincenty(start[1], start[0], end[1], end[0]).distance;
    length += segmentLength;
  }

  return length;
}


/*********************************************************************************************************************/
/**
 * Purpose: copies measurement to clipboard
 * @params  measurement value
 * @event copy
 */
function copyTextToClipboard(text) {
  let textArea = document.createElement("textarea");

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
      let successful = document.execCommand('copy');
      let msg = successful ? 'successful' : 'unsuccessful';
      console.log('Copying text command was ' + msg);
  } catch (err) {
      console.log('Oops, unable to copy');
  }

  document.body.removeChild(textArea);
}


/*********************************************************************************************************************/
/**
 * Purpose: creates sidebar element for the individual roadsigns
 * @params  the feature and it's metadata
 * @event loads the sidebar and all of the metadata into the forms
 */

function referencePointWindow(feature, selected, rgaEnabled, speedForm){
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
  $(".lane_info_time_restrictions").hide();
  $(".descriptive_name").hide();
  $(".lane_type").hide();
  $(".approach_type").hide();
  $(".maneuver_control_type").hide();
  $("#approach-table").hide();
  $(".verified_lat").hide();
  $(".verified_long").hide();
  $(".verified_elev").hide();
  $(".approach_name").hide();
  $(".maneuver_control_type").hide();
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
  hideRGAFields(false);
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
  if(feature.get("marker").name != "Reference Point Marker"){
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
    $(".maneuver_control_type").hide();
    $("#approach-table").hide();
    $('.intersection-info-tab').hide();
    hideRGAFields(true);
    $('.road_authority_id').hide();
    $('.road_authority_id_type').hide();
  }
        
  if(feature.get("marker").name == "Reference Point Marker"){    
      //Enable or disable rga fields on reference point marker depend on whether current RGA toggle is enabled/disabled.
      enableRGAFields(rgaEnabled);
  }

  $('#revision').val(feature.get("revisionNum"));
  if (! feature.get("elevation")){
    $("#elev").val("");
  } else {
    $("#elev").val(feature.get("elevation"));
  }

  if (! feature.get("verifiedElev")){
    $("#verified_elev").val("");
  } else {
    $("#verified_elev").val(feature.get("verifiedElev"));
  }
  
  if (! feature.get("masterLaneWidth")){
    $("#master_lane_width").val("366");
  } else {
    $("#master_lane_width").val(feature.get("masterLaneWidth"));
  }

  if (! feature.get("layerID")){
      $("#layer").val("1");
  } else {
      $("#layer").val( feature.get("layerID"));
  }

  if ( feature.get("intersectionName") ){
    $("#intersection_name").val(feature.get("intersectionName"));
  }

  if (! feature.get("regionID")){
      $("#region").val("");
  } else {
      $("#region").val(feature.get("regionID"));
  }  
  
  if (!feature.get("roadAuthorityId")){
      $("#road_authority_id").val("");
  } else {
      $("#road_authority_id").val(feature.get("roadAuthorityId"));
  }

  if (! feature.get("roadAuthorityIdType")){
      $("#road_authority_id_type").val("");
  } else {
      $("#road_authority_id_type").val(feature.get("roadAuthorityIdType"));
  }
  
  if (! feature.get("mappedGeometryId")){
      $("#mapped_geometry_id").val("");
  } else {
      $("#mapped_geometry_id").val(feature.get("mappedGeometryId"));
  }
  
  if (! feature.get("contentVersion")){
      $("#content_version").val("");
  } else {
      $("#content_version").val(feature.get("contentVersion"));
  }
  
  if (! feature.get("contentDateTime")){
      $("#content_date_time").val("");
  } else {
      $("#content_date_time").val(feature.get("contentDateTime"));
  }

  if (selected == "child"){
  if(feature.get("marker").name != "Reference Point Marker") {
    $('.btnDone').prop('disabled', true);
  } else {
    $('.btnDone').prop('disabled', false);
  }
      $('.intersection-btn').prop('disabled', false);
      $('.btnClose').prop('readonly', false);
  } else {
      $('.btnDone').prop('disabled', false);
  }
  
  if (! feature.get("speedLimitType")) {
     removeSpeedForm(speedForm);
     addSpeedForm(speedForm);
  } else {
     rebuildSpeedForm(speedForm, feature.get("speedLimitType"));
  }

   if(feature.get("marker").name == "Reference Point Marker"){    
    //Hide RGA related fields in speed limit form
    hideRGAFieldsAssociatedToSpeedLimits();
  }

  $("#attributes").show();
}


async function populateRefWindow(feature, lat, lon)
{
  getNearestIntersectionJSON(feature, lat, lon);
  let elev = -9999;
	if(!feature.get("elevation")){
    elev = await getElev(lat, lon);
    if (!feature.get("elevation")?.value) {
      $('#elev').val(elev);
    }
  }
    
  if (feature.get("verifiedElev")){
    $('#verified_elev').val(feature.get("verifiedElev"));
  } else {
    //If verified elevation does not exist in feature, update it with new elevation value
    $('#verified_elev').val(elev);
  }
  
	if (feature.get("verifiedLat")){
		$('#verified_lat').val(feature.get("verifiedLat"));	
	} else {
		$('#verified_lat').val(lat);
  }
  
	if (feature.get("verifiedLon")){
		$('#verified_long').val(feature.get("verifiedLon"));
	} else {
		$('#verified_long').val(lon);
	}	
}


function toggleLaneTypeAttributes(attribute, values) {
  let laneTypeOptions = [];
  $(".lane_type ul li").each(function() { laneTypeOptions.push($(this).text()) });
	for (let i = 0; i < laneTypeOptions.length; i++) {
		$('.' + laneTypeOptions[i] + '_type_attributes').parent().hide();
	}
	
	let typeAttribute_object = updateTypeAttributes(attribute);
	
	if ( $('.' + attribute + '_type_attributes').length === 0 ){
	    $('#' + attribute + '_type_attributes').multiselect({
	        onChange: function(option, checked){
            typeAttribute_object = updateTypeAttributes(attribute)
	        },
	        maxHeight: 200,
	        buttonClass: attribute + '_type_attributes btn btn-default',
	        buttonText: function(options, select) {
            if (options.length === 0) {
              return 'Select '+ attribute + ' Type Attribute'
            } else if (options.length > 1) {
              return options.length + ' selected';
            } else {
              let labels = [];
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
  return typeAttribute_object;
}

function updateSharedWith(){
    return $('#shared_with option:selected').map(function(a, item){return item.value;})
}

function updateTypeAttributes(attribute) {
  let typeAttribute_object = $('#' + attribute + '_type_attributes option:selected').map(function(a, item){ return item.value;})
  return typeAttribute_object;
}

// sets the temporary lane attributes to the actual lane object
function setLaneAttributes(selectedMarker) {
  if( tmpLaneAttributes == null) {
      // no attributes to add
      return;
  }
  if( !selectedMarker.get('lane_attributes') ) {
      selectedMarker.set('lane_attributes',{});
  }

  for( let attribute in tmpLaneAttributes ) {
      selectedMarker.get('lane_attributes')[attribute] = tmpLaneAttributes[attribute]
  }

  resetLaneAttributes()
}

// clears the temporary lane objects
function resetLaneAttributes() {
  tmpLaneAttributes = {};
}

function removeLaneAttributes(selectedMarker, attribute ) {
  let attrId = parseInt(attribute.id.match(/(\d+)$/)[0], 10);
  if( tmpLaneAttributes[attrId] ) {
      delete tmpLaneAttributes[attrId];
      return
  }
  if( selectedMarker.get('lane_attributes') )
      if( selectedMarker.get('lane_attributes')[attrId] )
          delete selectedMarker.get('lane_attributes')[attrId];
}

function updateDisplayedLaneAttributes( feature ){
  removeDisplayedLaneAttributes();
  let laneAttributes = feature.get('lane_attributes');
  if( laneAttributes == null || $.isEmptyObject(laneAttributes)) {
      return;
  }

  $('#attr_droppable').find('p').remove();
  for( let attribute in laneAttributes ) {
      let img = '<img id="lane_img_'+ laneAttributes[attribute].id +'" class="dragged-img" src="'+ laneAttributes[attribute].img_src +'">';
      $('#attr_droppable').append(img);
  }


  $('#attr_droppable').find('img').each(function() {
      makeDraggable( $(this) )
  });
}


function removeDisplayedLaneAttributes(){
  $('#attr_droppable').empty().append(
      '<h4 class="list-group-item-heading">Lane Attributes</h4>' +
      '<p class="help-block text-center">drop content here</p>');
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
        $('#hidden-drag').removeClass('hidden');
        $('.trash_droppable').toggleClass("hidden");
      },
      stop: function(e) {
        $('#hidden-drag').addClass('hidden');
        $('.trash_droppable').toggleClass("hidden");
      }
  });
}
/**
 * Purpose: populate reference point modal window
 * @params  the feature and it's metadata
 * @event loads the appropriate data - elevation is doen through ajax
 */

function populateAttributeWindow(lat, lon) {
	$('#lat').val(lat);
	$('#long').val(lon);
}

//Add saveApproach
function saveApproaches(selectedMarker) {
  let approaches = [];
  
  for(let i = 0; i <= approachNumRows; i++) {
    let approachType = $('#row' + i + ' .approach_type .dropdown-toggle').text().trim();
    if (approachType === 'Select') {
      approachType = 'Select';
    }
    
    let isSelected = $('#row' + i + ' input[name="rowSelection"]').is(':checked');
    
    let daysOfTheWeek = [];
    $('#row' + i + ' .approach_day_selection_dropdown option:selected').each(function() {
      daysOfTheWeek.push($(this).val());
    });
    
    let timePeriodType = "";
    let timePeriodValue = "";
    let timePeriodRange = {};

    let selectedType = $('#row' + i + ' input[name="approach_time_period' + i + '"]:checked').val();
    timePeriodType = selectedType ? selectedType.trim().toLowerCase() : "";

    if (timePeriodType === "range") {
      timePeriodRange = {};
      timePeriodRange["startDatetime"] = $('#row' + i + ' #approach_time_period_start_datetime' + i).val();
      timePeriodRange["startOffset"] = $('#row' + i + ' #approach_time_period_start_offset' + i).val();
      timePeriodRange["endDatetime"] = $('#row' + i + ' #approach_time_period_end_datetime' + i).val();
      timePeriodRange["endOffset"] = $('#row' + i + ' #approach_time_period_end_offset' + i).val();
    } else if (timePeriodType === "general") {
      timePeriodValue = $('#row' + i + ' input[name="approach_time_period_general' + i + '"]:checked').val() || "";
    }
    
    approaches.push({
      rowId: i,
      approachType: approachType,
      selected: isSelected,
      timeRestrictions: {
        daysOfTheWeek: daysOfTheWeek,
        timePeriodType: timePeriodType,
        timePeriodValue: timePeriodValue,
        timePeriodRange: timePeriodRange
      }
    });
  }
  
  return approaches
}

//Add rebuildApproaches
async function rebuildApproaches(approaches) {
  $('#tab_approaches > tbody').empty();
  approachNumRows = -1;
  if (approaches === null || approaches === undefined || approaches.length < 1) {
     await addApproachRow(null, null);
  } else {
    for(let i = 0; i < approaches.length; i++) {
      await addApproachRow(null, approaches[i]);
      
    }
  }
  setRGAStatus();
  updateDeleteButtonStates();
}
//Add addApproachRow
async function addApproachRow(readOnly, valueSets) {
  let rowHTML;
  let response = await fetch("js/approachRow.html");
  rowHTML = await response.text();
  $('#tab_approaches tbody').append(rowHTML);
  approachNumRows++;
  changeApproachRow('New', approachNumRows, readOnly, valueSets);

  

  $.get("js/time-restrictions.html", function (data) {
      timeRestrictions = data;
      addApproachTimeRestrictions(approachNumRows, timeRestrictions);
      updateApproachTimeRestrictionsHTML();
      setRGAStatus();
  })
}
//Add deleteApproachRow
function deleteApproachRow(approachRowNum) {
  $('#row' + approachRowNum).remove();
  for(let i = approachRowNum + 1; i <= approachNumRows; i++) {
      changeApproachRow(i, i - 1, null, null);
  }
  approachNumRows--;
}

//Add changeApproachRow
function changeApproachRow(oldVal, newVal, readOnly, valueSets) {
  $('#row' + oldVal).attr('id', 'row' + newVal);
  $('#approachSelection' + oldVal).attr('id', 'approachSelection' + newVal);
  $('#approachType' + oldVal).attr('id', 'approachType' + newVal);
  $('#approachTimeRestriction' + oldVal).attr('id', 'approachTimeRestriction' + newVal);
  $('#rowDeleteApproach' + oldVal).attr('id', 'rowDeleteApproach' + newVal);
  
  if (readOnly && readOnly !== undefined) {
      for (let i = 0; i < readOnly.length; i++) {
          $('input[name="' + readOnly[i] + newVal + '"').prop('readonly', true);
      }
  }
  
  if (valueSets && valueSets !== undefined) {
    for (let set in valueSets) {
        if (valueSets.hasOwnProperty(set)) {
            if (set === 'approachType'){
              $('#approachType' + newVal + ' .dropdown-toggle').html(valueSets[set]+'<span class="caret"></span>');
            } else if (set === 'selected') {
              if (valueSets[set] === true) {
                $('tr#row' + newVal + ' input[name="rowSelection"]').prop('checked', true);
              }
            } else if (set === 'timeRestrictions') {
              let timeRestrictions = valueSets[set];
              
              // Handle daysOfTheWeek
              if (timeRestrictions.daysOfTheWeek && Array.isArray(timeRestrictions.daysOfTheWeek)) {
                setTimeout(() => {
                  $('#row' + newVal + ' .approach_day_selection_dropdown').multiselect('deselectAll', false);
                  timeRestrictions.daysOfTheWeek.forEach(function(day) {
                    $('#row' + newVal + ' .approach_day_selection_dropdown').multiselect('select', day);
                  });
                  $('#row' + newVal + ' .approach_day_selection_dropdown').multiselect('refresh');
                }, 100);
              }
              
              // Handle time period
              if (timeRestrictions.timePeriodType) {
                setTimeout(() => {
                  $('input[name="approach_time_period' + newVal + '"][value="' + timeRestrictions.timePeriodType + '"]').prop('checked', true).trigger('change');
                  
                  if (timeRestrictions.timePeriodType === 'range' && timeRestrictions.timePeriodRange) {
                    let range = timeRestrictions.timePeriodRange;
                    if (range.startDatetime) $('#approach_time_period_start_datetime' + newVal).val(range.startDatetime);
                    if (range.startOffset) $('#approach_time_period_start_offset' + newVal).val(range.startOffset);
                    if (range.endDatetime) $('#approach_time_period_end_datetime' + newVal).val(range.endDatetime);
                    if (range.endOffset) $('#approach_time_period_end_offset' + newVal).val(range.endOffset);
                  } else if (timeRestrictions.timePeriodType === 'general' && timeRestrictions.timePeriodValue) {
                    $('input[name="approach_time_period_general' + newVal + '"][value="' + timeRestrictions.timePeriodValue + '"]').prop('checked', true);
                  }
                }, 100);
              }
            }
        }
    }
  }
}

function updateDeleteButtonStates() {
  $('input[name="rowSelection"]').each(function() {
    let rowId = $(this).closest('tr').attr('id').replace('row', '');
    let deleteButton = $('#rowDeleteApproach' + rowId);
    
    if ($(this).is(':checked')) {
      deleteButton.prop('disabled', true).addClass('disabled');
    } else {
      deleteButton.prop('disabled', false).removeClass('disabled');
    }
  });
}

function saveConnections(selectedMarker) {
  let nodeObject = [];
  for(let i = 0; i <= numRows; i++) {
    let ids = $('#maneuvers' + i + ' > ul > li > img');
    let maneuvers = [];
    for(let j = 0; j < ids.length; j++) {
        maneuvers.push(ids[j].id.match(/\d+$/g)[0]);
    }

    let daysOfTheWeek = [];
    $('#row' + i + ' .connections_day_selection_dropdown').each(function() {
      daysOfTheWeek.push($(this).val());
    });
    
    let timePeriodType = "";
    let timePeriodValue = "";
    let timePeriodRange = {};

    let selectedType = $('#row' + i + ' input[name="connections_time_period' + i + '"]:checked').val();
    timePeriodType = selectedType ? selectedType.trim().toLowerCase() : "";

    if (timePeriodType === "range") {
      timePeriodRange = {};
      timePeriodRange["startDatetime"] = $('#row' + i + ' #connections_time_period_start_datetime' + i).val();
      timePeriodRange["startOffset"] = $('#row' + i + ' #connections_time_period_start_offset' + i).val();
      timePeriodRange["endDatetime"] = $('#row' + i + ' #connections_time_period_end_datetime' + i).val();
      timePeriodRange["endOffset"] = $('#row' + i + ' #connections_time_period_end_offset' + i).val();
    } else if (timePeriodType === "general") {
      timePeriodValue = $('#row' + i + ' input[name="connections_time_period_general' + i + '"]:checked').val() || "";
    }

    nodeObject.push({
        connectionId: $('#connectionId' + i + ' .dropdown-toggle').text().replace('\u200b', ""),
        remoteID: $('input[name="remoteID' + i + '"]').val(),
        fromLane: selectedMarker.get("laneNumber"),
        toLane: $('input[name="toLane' + i + '"]').val(),
        signal_id: $('input[name="signal_id' + i + '"]').val(),
        timeRestrictions: {
          daysOfTheWeek: daysOfTheWeek,
          timePeriodType: timePeriodType,
          timePeriodValue: timePeriodValue,
          timePeriodRange: timePeriodRange
        },
        maneuvers: maneuvers
    });
  }
  return nodeObject;
}


async function rebuildConnections(connections) {
  //TODO clear if empty rows
  $('#tab_intersects > tbody').empty();
  numRows = -1;
  if (connections === null || connections === undefined || connections.length < 1) {
     await addRow(null, null);
  } else {
    for(let i = 0; i < connections.length; i++) {
       await addRow(null, connections[i]);
    }
  }
  setRGAStatus();
}


async function addRow(readOnly, valueSets) {
  let rowHtml;
  let response = await fetch("js/row.html");
  rowHtml = await response.text();
  $('#tab_intersects tbody').append(rowHtml);
  numRows++;
  changeRow('New', numRows, readOnly, valueSets);
  $.get("js/time-restrictions.html", function (data) {
    timeRestrictions = data;
    addConnectionsTimeRestrictions(numRows, timeRestrictions);
    updateConnectionsTimeRestrictionsHTML();
    setRGAStatus();
})
  populateConnectionIdDropdown(numRows)
  makeDroppable(numRows);
}

function deleteRow(rowNum) {
  $('#row' + rowNum).remove();
  for(let i = rowNum + 1; i <= numRows; i++) {
      changeRow(i, i - 1, null, null);
  }
  numRows--;
}


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
  $('#connectionsTimeRestrictions' + oldVal).attr('id', 'connectionsTimeRestrictions' + newVal);
  $('#maneuvers' + oldVal).attr('id', 'maneuvers' + newVal);
  $('#attr_droppable' + oldVal).attr('id', 'attr_droppable' + newVal);
  $('#rowDelete' + oldVal).attr('id', 'rowDelete' + newVal);
  if (readOnly && readOnly !== undefined) {
      for (let i = 0; i < readOnly.length; i++) {
          $('input[name="' + readOnly[i] + newVal + '"').prop('readonly', true);
      }
  }
  if (valueSets && valueSets !== undefined) {
      for (let set in valueSets) {
          if (valueSets.hasOwnProperty(set)) {
              if (set === 'maneuvers') {
                  for (let k = 0; k < valueSets[set].length; k++) {
                      for(let j = 0; j < lane_attributes.length; j++) {
                          if (lane_attributes[j].id.toString() === valueSets[set][k]){
                              addLaneManeuversToContainer($('#attr_droppable' + newVal),
                                  '<img id="lane_img_'+ newVal +'_' + lane_attributes[j].id + '" class="dragged-img" src="'+ lane_attributes[j].img_src +'">');
                          }
                      }
                  }
              } else if (set === 'connectionId'){
                $('#connectionId' + newVal + ' .dropdown-toggle').html(valueSets[set]+'<span class="caret"></span>')
              } else if (set === 'timeRestrictions') {
                let timeRestrictions = valueSets[set];
                
                // Handle daysOfTheWeek
                if (timeRestrictions.daysOfTheWeek && Array.isArray(timeRestrictions.daysOfTheWeek)) {
                  setTimeout(() => {
                    $('#row' + newVal + ' .connections_day_selection_dropdown').multiselect('deselectAll', false);
                    timeRestrictions.daysOfTheWeek.forEach(function(day) {
                      $('#row' + newVal + ' .connections_day_selection_dropdown').multiselect('select', day);
                    });
                    $('#row' + newVal + ' .connections_day_selection_dropdown').multiselect('refresh');
                  }, 100);
                }
                
                // Handle time period
                if (timeRestrictions.timePeriodType) {
                  setTimeout(() => {
                    $('input[name="connections_time_period' + newVal + '"][value="' + timeRestrictions.timePeriodType + '"]').prop('checked', true).trigger('change');
                    
                    if (timeRestrictions.timePeriodType === 'range' && timeRestrictions.timePeriodRange) {
                      let range = timeRestrictions.timePeriodRange;
                      if (range.startDatetime) $('#connections_time_period_start_datetime' + newVal).val(range.startDatetime);
                      if (range.startOffset) $('#connections_time_period_start_offset' + newVal).val(range.startOffset);
                      if (range.endDatetime) $('#connections_time_period_end_datetime' + newVal).val(range.endDatetime);
                      if (range.endOffset) $('#connections_time_period_end_offset' + newVal).val(range.endOffset);
                    } else if (timeRestrictions.timePeriodType === 'general' && timeRestrictions.timePeriodValue) {
                      $('input[name="connections_time_period_general' + newVal + '"][value="' + timeRestrictions.timePeriodValue + '"]').prop('checked', true);
                    }
                  }, 100);
                }
              } else {
                  $('input[name="' + set + newVal + '"').attr('value', valueSets[set]);
              }
          }
      }
  }
}


/**
 * Purpose: series of functions that allow the attribute container to work
 * @params  element, container
 * @event allows icon to be dragged and dropped onto the other sidebar, plus trash
 */

function addLaneManeuversToContainer(container, attribute) {
  for(let i = 0; i < container.children().length; i++) {
      if (container.children()[i].id === attribute.id) {
          return;
      }
  }
  container.append( attribute );
}


// adds the attribute (image) to the displayed container
function addLaneAttributeToContainer(container, attribute ) {
  let attr_id = parseInt(attribute.id.match(/(\d+)$/)[0], 10);
  let lane_attr = lane_attributes.find(attr => attr.id === attr_id);

  // if attribute already exists in the lane attributes, skip, do not add
  // if (selectedMarker.get('lane_attributes') &&
  //     selectedMarker.get('lane_attributes')[attr_id]) {
  //     return;
  // }

  // skip adding the attribute if it's already in the temp attributes, otherwise add it
  if(!tmpLaneAttributes[attr_id] ) {
      tmpLaneAttributes[attr_id] = lane_attr
  }
  else { return; }

  container.append( attribute );
}

function populateConnectionIdDropdown(rowId) {
	// Add an empty option at the top. Use a non-breaking space(uni-code 200b), to force the
	// entry in the dropdown menu to maintain the same height as the rest of the list.
	$('#connectionId' + rowId + ' .dropdown-menu').append(
    $('<li><a href="#">\u200b</a></li>').click(function(){
    let selText = $(this).children('a').text();
    $(this).parents('.btn-group').find('.dropdown-toggle').html(selText+'<span class="caret"></span>');
  }))
  for(let i = 1; i <= 255; i++) {
    $('#connectionId' + rowId + ' .dropdown-menu').append(
    $('<li><a href="#">' + i + '</a></li>').click(function(){
      let selText = $(this).children('a').text();
      $(this).parents('.btn-group').find('.dropdown-toggle').html(selText+'<span class="caret"></span>');
    }))
  }
}


/**
 * Purpose: makes the attributes droppable
 * @params  id of element
 * @event allows icon to be dropped onto the other sidebar
 */

function makeDroppable (id){
  //--- Drop Functionality
  let containerName = "attr_droppable";
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
          let container = $(this);
          let attr = $(ui.helper.children());
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
          let attr = $(ui.helper)[0];
          attr.remove();
          removeLaneAttributes(attr);
      }
  });
}


function toggle(lanes, vectors, laneMarkers, laneWidths, laneConnections) {
  let divPosition = $("#sidebar").offset();
  if(divPosition.left < 0){
      $("sidebar").show();
      $("#sidebar").animate({"left":12},1000);
  } else {
      $("#sidebar").animate({"left":-800},1000);
      $("sidebar").hide();
      toggleControlsOn('none', lanes, vectors, laneMarkers, laneWidths, false);
      laneConnections.getSource().clear();
  }
}

function toggleLanes(lanes, laneMarkers){
  if(document.getElementById("laneIcon").className == "fa fa-square-o"){
      lanes.setVisible(true);
      laneMarkers.setVisible(true);
      document.getElementById("laneIcon").className = "fa fa-check-square-o";
  } else {
      lanes.setVisible(false);
      laneMarkers.setVisible(false);
      document.getElementById("laneIcon").className = "fa fa-square-o";
  }
}

function toggleBars(box){
  if(document.getElementById("boxIcon").className == "fa fa-square-o"){
      box.setVisible(true);
      document.getElementById("boxIcon").className = "fa fa-check-square-o";
  } else {
      box.setVisible(false);
      document.getElementById("boxIcon").className = "fa fa-square-o";
  }
}

function togglePoints(vectors){
  if(document.getElementById("pointIcon").className == "fa fa-square-o"){
      vectors.setVisible(true);
      document.getElementById("pointIcon").className = "fa fa-check-square-o";
  } else {
      vectors.setVisible(false);
      document.getElementById("pointIcon").className = "fa fa-square-o";
  }
}


function toggleWidthArray(lanes, vectors, laneWidths) {
  let isNegative = {"value": false, "node": "", "lane": ""};
  if (laneWidths.getSource().getFeatures().length == 0) {
    let masterWidth;
    let vectorFeatures = vectors.getSource().getFeatures();
    for (let vectorFeature of vectorFeatures) {
      console.log(vectorFeature)
      if (vectorFeature.get("marker").name == "Reference Point Marker") {
          masterWidth = parseFloat(vectorFeature.get("masterLaneWidth"));
      }
    }
    let laneFeatures = lanes.getSource().getFeatures();
    for (let i = 0; i < laneFeatures.length; i++) {
      let widthList = [];
      let widthDeltaTotal = 0;
      let flipped = false;

      for (let j = 0; j < laneFeatures[i].getGeometry().getCoordinates().length; j++) {
        let point1 = '';
        let point2 = '';
        if (j < laneFeatures[i].getGeometry().getCoordinates().length - 1) {
            if (laneFeatures[i].getGeometry().getCoordinates()[j][0] == laneFeatures[i].getGeometry().getCoordinates()[j + 1][0] && laneFeatures[i].getGeometry().getCoordinates()[j][1] == laneFeatures[i].getGeometry().getCoordinates()[j + 1][1]) {
                j++; //to prevent dots that are the exact same.
            }
        }

        if (j < laneFeatures[i].getGeometry().getCoordinates().length - 1) {
          point1 = new ol.proj.toLonLat(laneFeatures[i].getGeometry().getCoordinates()[j]);
          point2 = new ol.proj.toLonLat(laneFeatures[i].getGeometry().getCoordinates()[j+1]);
        } else {
          point1 = new ol.proj.toLonLat(laneFeatures[i].getGeometry().getCoordinates()[j]);
          if (laneFeatures[i].getGeometry().getCoordinates()[j][0] == laneFeatures[i].getGeometry().getCoordinates()[j-1][0] && laneFeatures[i].getGeometry().getCoordinates()[j][1] == laneFeatures[i].getGeometry().getCoordinates()[j-1][1]) {
            point2 = new ol.proj.toLonLat(laneFeatures[i].getGeometry()[j - 2]);
            flipped = true;
          } else {
            point2 = new ol.proj.toLonLat(laneFeatures[i].getGeometry().getCoordinates()[j - 1]);
          }
        }
        let widthDelta = parseFloat(laneFeatures[i].get("laneWidth")[j]);
        if (isNaN(widthDelta) || widthDelta == null || typeof widthDelta == "undefined") {
            widthDelta = 0
        }

        widthDeltaTotal = widthDeltaTotal + widthDelta;

        if (masterWidth + widthDeltaTotal < 0){
            console.log(masterWidth + widthDeltaTotal)
            isNegative = {"value": true, "node": j+1, "lane": i};
            widthDeltaTotal = 0 - masterWidth;
        }

        let inverse = inverseVincenty(point1[1], point1[0], point2[1], point2[0]);

        let direct1 = directVincenty(point1[1], point1[0], inverse.bearing + 90, (((masterWidth + widthDeltaTotal) / 2) / 100));
        let direct2 = directVincenty(point1[1], point1[0], inverse.bearing - 90, (((masterWidth + widthDeltaTotal) / 2) / 100));

        let newPoint1 = new ol.geom.Point(ol.proj.fromLonLat([direct1.lon, direct1.lat]));
        let newPoint2 = new ol.geom.Point(ol.proj.fromLonLat([direct2.lon, direct2.lat]));

        if (j == laneFeatures[i].getGeometry().getCoordinates().length - 1) {
            j++; //flips the j value since it's the last lane point and we need to build in reverse
        }

        if (isOdd(j) && !flipped) {
          widthList.push(newPoint1, newPoint2);
          let coordinates = widthList.map(point => point.getCoordinates());
          let widthBox = new ol.geom.LinearRing(coordinates);
          laneWidths.getSource().addFeature(new ol.Feature(new ol.geom.Polygon([widthBox.getCoordinates()])));
          widthList = [];
          widthList.push(newPoint1, newPoint2);
        } else {
          widthList.push(newPoint2, newPoint1);
          let coordinates = widthList.map(point => point.getCoordinates());
          let widthBox =new  ol.geom.LinearRing(coordinates);
          laneWidths.getSource().addFeature(new ol.Feature(new ol.geom.Polygon([widthBox.getCoordinates()])));
          widthList = [];
          widthList.push(newPoint2, newPoint1);
          flipped = false;
        }
      }
    }
  } else {
      laneWidths.getSource().clear();
  }

  if (isNegative.value){
      alert("Width deltas sum to less than zero on lane " + laneFeatures[isNegative.lane].attributes.laneNumber + " at node " + isNegative.node + "!");
  }
}

/*!
 * JavaScript function to calculate the geodetic distance between two points specified by latitude/longitude using the Vincenty inverse formula for ellipsoids.
 *
 * Taken from http://movable-type.co.uk/scripts/latlong-vincenty.html and optimized / cleaned up by Mathias Bynens <http://mathiasbynens.be/>
 * Based on the Vincenty direct formula by T. Vincenty, “Direct and Inverse Solutions of Geodesics on the Ellipsoid with application of nested equations”, Survey Review, vol XXII no 176, 1975 <http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf>
 *
 * @param   {Number} lat1, lon1: first point in decimal degrees
 * @param   {Number} lat2, lon2: second point in decimal degrees
 * @returns {Number} distance in metres between points
 */
function inverseVincenty(lat1, lon1, lat2, lon2) {
  let a = 6378137,
      b = 6356752.314245,
      f = 1 / 298.257223563 // WGS-84 ellipsoid params
      let φ1 = toRad(lat1), λ1 = toRad(lon1);
      let φ2 = toRad(lat2), λ2 = toRad(lon2);

      let L = λ2 - λ1;
      let tanU1 = (1-f) * Math.tan(φ1), cosU1 = 1 / Math.sqrt((1 + tanU1*tanU1)), sinU1 = tanU1 * cosU1;
      let tanU2 = (1-f) * Math.tan(φ2), cosU2 = 1 / Math.sqrt((1 + tanU2*tanU2)), sinU2 = tanU2 * cosU2;

      let sinλ, cosλ, sinSqσ, sinσ=0, cosσ=0, σ=0, sinα, cosSqα=0, cos2σM=0, C;

      let λ = L, λʹ, iterations = 0, antimeridian = Math.abs(L) > Math.PI;
      do {
          sinλ = Math.sin(λ);
          cosλ = Math.cos(λ);
          sinSqσ = (cosU2*sinλ) * (cosU2*sinλ) + (cosU1*sinU2-sinU1*cosU2*cosλ) * (cosU1*sinU2-sinU1*cosU2*cosλ);
          if (sinSqσ == 0) break; // co-incident points
          sinσ = Math.sqrt(sinSqσ);
          cosσ = sinU1*sinU2 + cosU1*cosU2*cosλ;
          σ = Math.atan2(sinσ, cosσ);
          sinα = cosU1 * cosU2 * sinλ / sinσ;
          cosSqα = 1 - sinα*sinα;
          cos2σM = (cosSqα != 0) ? (cosσ - 2*sinU1*sinU2/cosSqα) : 0; // equatorial line: cosSqα=0 (§6)
          C = f/16*cosSqα*(4+f*(4-3*cosSqα));
          λʹ = λ;
          λ = L + (1-C) * f * sinα * (σ + C*sinσ*(cos2σM+C*cosσ*(-1+2*cos2σM*cos2σM)));
          let iterationCheck = antimeridian ? Math.abs(λ)-Math.PI : Math.abs(λ);
          if (iterationCheck > Math.PI) throw new Error('λ > π');
      } while (Math.abs(λ-λʹ) > 1e-12 && ++iterations<1000);
      if (iterations >= 1000) throw new Error('Formula failed to converge');

      let uSq = cosSqα * (a*a - b*b) / (b*b);
      let A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
      let B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));
      let Δσ = B*sinσ*(cos2σM+B/4*(cosσ*(-1+2*cos2σM*cos2σM)-
          B/6*cos2σM*(-3+4*sinσ*sinσ)*(-3+4*cos2σM*cos2σM)));

      let s = b*A*(σ-Δσ);

      let α1 = Math.atan2(cosU2*sinλ,  cosU1*sinU2-sinU1*cosU2*cosλ);
      let α2 = Math.atan2(cosU1*sinλ, -sinU1*cosU2+cosU1*sinU2*cosλ);

      α1 = (α1 + 2*Math.PI) % (2*Math.PI); // normalise to 0..360
      α2 = (α2 + 2*Math.PI) % (2*Math.PI); // normalise to 0..360

      return {
          distance:       s,
          bearing: s==0 ? NaN : toDeg(α1)
      };
};


/*!
* JavaScript function to calculate the destination point given start point latitude / longitude (numeric degrees), bearing (numeric degrees) and distance (in m).
*
* Taken from http://movable-type.co.uk/scripts/latlong-vincenty-direct.html and optimized / cleaned up by Mathias Bynens <http://mathiasbynens.be/>
* Based on the Vincenty direct formula by T. Vincenty, “Direct and Inverse Solutions of Geodesics on the Ellipsoid with application of nested equations”, Survey Review, vol XXII no 176, 1975 <http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf>
*/

function directVincenty(lat1, lon1, brng, dist) {
  let a = 6378137;
  let b = 6356752.3142;
  let f = 1 / 298.257223563;// WGS-84 ellipsiod
  let s = dist;
  let alpha1 = toRad(brng);
  let sinAlpha1 = Math.sin(alpha1);
  let cosAlpha1 = Math.cos(alpha1);
  let tanU1 = (1 - f) * Math.tan(toRad(lat1));
  let cosU1 = 1 / Math.sqrt((1 + tanU1 * tanU1)), sinU1 = tanU1 * cosU1;
  let sigma1 = Math.atan2(tanU1, cosAlpha1);
  let sinAlpha = cosU1 * sinAlpha1;
  let cosSqAlpha = 1 - sinAlpha * sinAlpha;
  let uSq = cosSqAlpha * (a * a - b * b) / (b * b);
  let A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
  let B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
  let sigma = s / (b * A);
  let sigmaP = 2 * Math.PI;
  let sinSigma;
  let cos2SigmaM;
  let cosSigma;
  let deltaSigma;
  while (Math.abs(sigma - sigmaP) > 1e-12) {
    cos2SigmaM = Math.cos(2 * sigma1 + sigma);
    sinSigma = Math.sin(sigma);
    cosSigma = Math.cos(sigma);
    deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
    sigmaP = sigma;
    sigma = s / (b * A) + deltaSigma;
  };
  let tmp = sinU1 * sinSigma - cosU1 * cosSigma * cosAlpha1
  let lat2 = Math.atan2(sinU1 * cosSigma + cosU1 * sinSigma * cosAlpha1, (1 - f) * Math.sqrt(sinAlpha * sinAlpha + tmp * tmp))
  let lambda = Math.atan2(sinSigma * sinAlpha1, cosU1 * cosSigma - sinU1 * sinSigma * cosAlpha1)
  let C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha))
  let L = lambda - (1 - C) * f * sinAlpha * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
  let revAz = Math.atan2(sinAlpha, -tmp); // final bearing
  return {lat: toDeg(lat2), lon: lon1 + toDeg(L)};
};

function toRad(degrees){
  let pi = Math.PI;
  return degrees * (pi/180);
}

function toDeg(radians){
  let pi = Math.PI;
  return radians * (180/pi);
}


/*****************************************
 * Speed Limit Values
 * Methods
 *****************************************/


function removeSpeedForm(speedForm) {
    speedForm.removeAllForms();
}

function addSpeedForm(speedForm) {
    speedForm.addForm();
    //If RGA is disabled, Gray out and disable RGA fields associated to Speed Limits    
    if(!$('#rga_switch').is(":checked")){
      disableRGAFieldsAssociatedToSpeedLimits();
    }
}

function rebuildSpeedForm(speedForm, speedLimitArray) {
    removeSpeedForm(speedForm);
    let results = speedLimitArray.length
    for (let i = 0; i < results; i++) {
        speedForm.addForm();
        let forms = speedForm.getForms(i);
        forms[i].inject(
            {'velocity': speedLimitArray[i].velocity}
        );
        $("#speedForm_"+ i + "_speedLimitType").val(speedLimitArray[i].speedLimitType)
        $(`input[name="speedForm_${i}_speedLimitChoice"][value="${speedLimitArray[i].speedLimitChoice}"]`).prop('checked', true);
    }
    resetSpeedDropdowns(speedForm);
}

function saveSpeedForm(speedForm) {
    let tmpSpeedLimits = [];
    let forms = (speedForm.getForms()).length;
    for (let i = 0; i < forms; i++) {
      tmpSpeedLimits.push({
            speedLimitType: $("#speedForm_"+ i + "_speedLimitType option:selected").text(),
            velocity: $("#speedForm_" + i + "_velocity").val(),
            speedLimitChoice: $(`input[name="speedForm_${i}_speedLimitChoice"]:checked`).val()
        });
    }
    removeSpeedForm(speedForm);
    return tmpSpeedLimits;
}

function resetSpeedDropdowns(speedForm){
    $("[id*=speedLimitType] > option").each(function() {
        if ($(this).val() !== "") {
            $(this).prop('disabled', false);
        }
    });
    //If RGA is disabled, Gray out and disable RGA fields associated to Speed Limits
    if(!$('#rga_switch').is(":checked")){
      disableRGAFieldsAssociatedToSpeedLimits();
    }

    let forms = (speedForm.getForms()).length;
    for (let i = 0; i < forms; i++) {
        let current = $("#speedForm_"+ i + "_speedLimitType option:selected").text();
        $("[id*=speedLimitType] option[value='"+current+"']").prop('disabled', true);
    }
}

function getSelectedInteraction(map, selectedFeature){
  return map.getInteractions().getArray().find(interaction => {
    return selectedFeature && interaction instanceof ol.interaction.Select && interaction.getFeatures().getArray().includes(selectedFeature);
  })
}

function getSelectedLayer(overlayLayersGroup, selectedFeature){
  return overlayLayersGroup.getLayers().getArray().find(layer => {
    return selectedFeature && layer instanceof ol.layer.Vector && layer.getSource().hasFeature(selectedFeature);
  })
}

function unselectFeature(map, overlayLayersGroup,  selectedFeature ) {
  resetLaneAttributes()
	if( getSelectedLayer(overlayLayersGroup, selectedFeature) != null ) {
    console.log("unselecting ", selectedFeature)
    let selectedInteraction = getSelectedInteraction(map, selectedFeature);
    if (selectedInteraction) {
      selectedInteraction.getFeatures().clear(); 
    }		
	}
}


function setRGAStatus() {
  let rgaEnabled = false;
  if($('#rga_switch').is(":checked")){
      rgaEnabled = true;
      $('.day_selection_dropdown').multiselect('enable');
      $('.approach_day_selection_dropdown').multiselect('enable');
      $('.connections_day_selection_dropdown').multiselect('enable');
      enableManeuverControlType(true);
  }else{
      rgaEnabled = false;
      $('.day_selection_dropdown').multiselect('disable');
      $('.approach_day_selection_dropdown').multiselect('disable');
      $('.connections_day_selection_dropdown').multiselect('disable');
      enableManeuverControlType(false);
  }
  $('.day_selection_dropdown').multiselect('refresh');
  $('.approach_day_selection_dropdown').multiselect('refresh');
  $('.connections_day_selection_dropdown').multiselect('refresh');
  enableRGAFields(rgaEnabled);
  return rgaEnabled;
}

function enableManeuverControlType(enable) {
  if (enable) {
      // Enable the dropdown
      $('#maneuver_control_type .btn-select').prop('disabled', false);
      $('#maneuver_control_type .btn-select').css({
          'backgroundColor': '',
          'color': '',
          'borderColor': '',
          'pointerEvents': ''
      });
      $('#maneuver_control_type .btn-select').removeClass('disabled-dropdown');
      $('#maneuver_control_type .btn-select').attr('data-toggle', 'dropdown');
      
      // Enable dropdown menu items
      $('#maneuver_control_type .dropdown-menu a').css('pointerEvents', '');
      $('#maneuver_control_type .dropdown-menu a').removeClass('disabled');
  } else {
      // Disable the dropdown
      $('#maneuver_control_type .btn-select').prop('disabled', true);
      $('#maneuver_control_type .btn-select').css({
          'backgroundColor': "#eee",
          'color': '#999',
          'borderColor': '#ccc',
          'pointerEvents': 'none'
      });
      $('#maneuver_control_type .btn-select').addClass('disabled-dropdown');
      $('#maneuver_control_type .btn-select').removeAttr('data-toggle');
      
      // Disable dropdown menu items
      $('#maneuver_control_type .dropdown-menu a').css('pointerEvents', 'none');
      $('#maneuver_control_type .dropdown-menu a').addClass('disabled');
  }
}

function resetRGAStatus(){
  $("#rga_switch").prop('checked', false);
}

function enableRGAFields(enable=true){    
  if(enable){        
      $(".extra_rga_field_input").prop('disabled', false);
      $(".extra_rga_field_input").css('backgroundColor', "#fff");
  }else{     
      $(".extra_rga_field_input").prop('disabled', true);
      $(".extra_rga_field_input").css('backgroundColor', "#eee");
  }
  addRGAFieldsValidation(enable);
 }
  
function addRGAFieldsValidation(enable=true){
  if(enable){
    $("input:text.required").attr('data-parsley-required', true);
  }else{
    $("input:text.required").attr('data-parsley-required', false);
  }
}


function onRegionIdChangeCallback(regionId){
  console.log(regionId)
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

// Function to debounce the modify event
function debounceModifyEvent(callback, delay) {
  let timeoutId;
  return function (event) {
      if (timeoutId) {
          clearTimeout(timeoutId); // Clear the previous timeout
      }
      timeoutId = setTimeout(() => {
          callback(event); // Execute callback after delay
      }, delay);
  };
}


/**
 * @brief Add time restrictions HTML to the lane info popup on the main page.
 */
function addLaneInfoTimeRestrictions(time_restrictions) {
  // Update time restrictions with lane info specific identifiers
  let lane_info_time_restrictions = $(time_restrictions).clone();
  lane_info_time_restrictions.find('*').each(function() {
      
      if (this.id) {
          $(this).addClass("extra_rga_field_input");
          $(this).attr('id', "lane_info_" + this.id);
      }
      if (this.name) {
          $(this).attr('name', "lane_info_" + this.name);
      }
  });
  $(".lane_info_time_restrictions").html(lane_info_time_restrictions.html());
}

function addApproachTimeRestrictions(rowNum, time_restrictions) {  
  let approach_time_restrictions = $(time_restrictions).clone();
  approach_time_restrictions.find('*').each(function() {
      if (this.id) {
          $(this).addClass("extra_rga_field_input");
          $(this).attr('id', "approach_" + this.id + rowNum);
      }
      if (this.name) {
          $(this).attr('name', "approach_" + this.name + rowNum);
      }
      
      // Replace form-check-input class with approach-form-check-input
      if ($(this).hasClass("form-check-input")) {
          $(this).removeClass("form-check-input");
          $(this).addClass("approach-form-check-input");
      }
      
      // Replace day_selection_dropdown class with approach_day_selection_dropdown
      if ($(this).hasClass("day_selection_dropdown")) {
          $(this).removeClass("day_selection_dropdown");
          $(this).addClass("approach_day_selection_dropdown");
      }
  });
  
  $("[id='row" + rowNum + "'] .approach_time_restrictions").html(approach_time_restrictions);
}

function addConnectionsTimeRestrictions(rowNum, time_restrictions) {  
  let connections_time_restrictions = $(time_restrictions).clone();
  connections_time_restrictions.find('*').each(function() {
      if (this.id) {
          $(this).addClass("extra_rga_field_input");
          $(this).attr('id', "connections_" + this.id + rowNum);
      }
      if (this.name) {
          $(this).attr('name', "connections_" + this.name + rowNum);
      }
      
      // Replace form-check-input class with connections-form-check-input
      if ($(this).hasClass("form-check-input")) {
          $(this).removeClass("form-check-input");
          $(this).addClass("connections-form-check-input");
      }
      
      // Replace day_selection_dropdown class with connections_day_selection_dropdown
      if ($(this).hasClass("day_selection_dropdown")) {
          $(this).removeClass("day_selection_dropdown");
          $(this).addClass("connections_day_selection_dropdown");
      }
  });
  
  $("[id='row" + rowNum + "'] .connections_time_restrictions").html(connections_time_restrictions);
}

function updateConnectionsTimeRestrictionsHTML() {
  let startDateTimePicker = $('.start_datetime_picker');
  let endDateTimePicker = $('.end_datetime_picker');
  let dateConfig = {
      dateFormat: "Y-m-d H:i:S",
      allowInput: true,        
      enableTime: true,
      enableSeconds: true,
      minuteIncrement: 1,
      secondIncrement: 1,
      time_24hr: true
  };
  
  startDateTimePicker.flatpickr(dateConfig);
  endDateTimePicker.flatpickr(dateConfig);

  $(document).on('change', '.connections-form-check-input.time_period', function () {
      let currentRow = $(this).closest('tr');
      let rowId = currentRow.attr('id');
      let rowNum = rowId ? rowId.replace('row', '') : '';
      
      currentRow.find('.time_period_range_fields').hide();
      currentRow.find('.time_period_general_fields').hide();
      
      if ($(this).val() === 'range') {
          currentRow.find('.time_period_range_fields').show();
      } else if ($(this).val() === 'general') {
          currentRow.find('.time_period_general_fields').show();
      }
  });

  $('.connections_day_selection_dropdown').each(function() {
      if (!$(this).hasClass('multiselect')) {
          $(this).multiselect({
              maxHeight: 200,        
              onChange: function(option, checked, select) {
                  let currentDropdown = $(select);
                  let currentRow = currentDropdown.closest('tr');
                  
                  if ($(option).val() === '0' && checked) {
                      currentRow.find('.day_selection input').each(function() {
                          if ($(this).val() !== '0') {
                              $(this).prop('disabled', true);
                              $(this).prop('checked', false).trigger('change');
                              $(this).parents('a').first().css({ 'color': 'grey' });
                              $(this).parents('li').first().removeClass('active');
                          }
                      });
                  } else {
                      let isAllSelected = false;
                      currentRow.find('.day_selection input').each(function () {
                          if ($(this).val() === '0' && $(this).prop('checked')) {
                              isAllSelected = true;
                          }
                      });
                      
                      currentRow.find('.day_selection input').each(function () {
                          if (isAllSelected) {
                              if ($(this).val() !== '0') {
                                  $(this).prop('disabled', true);
                                  $(this).parents('a').first().css({ 'color': 'grey' });
                                  $(this).parents('li').first().removeClass('active');
                              }
                          } else {
                              $(this).prop('disabled', false);
                              $(this).parents('a').first().css({ 'color': 'black' });
                          }
                      });
                  }
              },
              buttonText: function (options, select) {
                  if (options.length === 0) {
                      let currentDropdown = $(select);
                      let currentRow = currentDropdown.closest('tr');
                      currentRow.find('.day_selection input').each(function () {
                          $(this).prop('disabled', false);
                          $(this).parents('a').first().css({ 'color': 'black' });
                      });
                      return 'Select';
                  } else if (options.length > 1) {                
                      return options.length + ' selected';
                  } else {
                      let labels = [];
                      options.each(function () {                    
                          if ($(this).attr('label') !== undefined) {
                              labels.push($(this).attr('label'));
                          } else {
                              labels.push($(this).html());
                          }
                          let value = $(this).val();
                          if (value === "0") {
                              let currentDropdown = $(select);
                              let currentRow = currentDropdown.closest('tr');
                              currentRow.find('.day_selection input').each(function () {
                                  if ($(this).val() !== "0") {
                                      $(this).prop('disabled', true);
                                      $(this).parents('a').first().css({ 'color': 'grey' });
                                  }
                              });
                          }                    
                      });
                      return labels.join(', ') + '';
                  }
              }
          });
      }
  });
}

function updateApproachTimeRestrictionsHTML() {
  let startDateTimePicker = $('.start_datetime_picker');
  let endDateTimePicker = $('.end_datetime_picker');
  let dateConfig = {
      dateFormat: "Y-m-d H:i:S",
      allowInput: true,        
      enableTime: true,
      enableSeconds: true,
      minuteIncrement: 1,
      secondIncrement: 1,
      time_24hr: true
  };
  
  startDateTimePicker.flatpickr(dateConfig);
  endDateTimePicker.flatpickr(dateConfig);

  $(document).on('change', '.approach-form-check-input.time_period', function () {
      let currentRow = $(this).closest('tr');
      let rowId = currentRow.attr('id');
      let rowNum = rowId ? rowId.replace('row', '') : '';
      
      currentRow.find('.time_period_range_fields').hide();
      currentRow.find('.time_period_general_fields').hide();
      
      if ($(this).val() === 'range') {
          currentRow.find('.time_period_range_fields').show();
      } else if ($(this).val() === 'general') {
          currentRow.find('.time_period_general_fields').show();
      }
  });

  $('.approach_day_selection_dropdown').each(function() {
      if (!$(this).hasClass('multiselect')) {
          $(this).multiselect({
              maxHeight: 200,        
              onChange: function(option, checked, select) {
                  let currentDropdown = $(select);
                  let currentRow = currentDropdown.closest('tr');
                  
                  if ($(option).val() === '0' && checked) {
                      currentRow.find('.day_selection input').each(function() {
                          if ($(this).val() !== '0') {
                              $(this).prop('disabled', true);
                              $(this).prop('checked', false).trigger('change');
                              $(this).parents('a').first().css({ 'color': 'grey' });
                              $(this).parents('li').first().removeClass('active');
                          }
                      });
                  } else {
                      let isAllSelected = false;
                      currentRow.find('.day_selection input').each(function () {
                          if ($(this).val() === '0' && $(this).prop('checked')) {
                              isAllSelected = true;
                          }
                      });
                      
                      currentRow.find('.day_selection input').each(function () {
                          if (isAllSelected) {
                              if ($(this).val() !== '0') {
                                  $(this).prop('disabled', true);
                                  $(this).parents('a').first().css({ 'color': 'grey' });
                                  $(this).parents('li').first().removeClass('active');
                              }
                          } else {
                              $(this).prop('disabled', false);
                              $(this).parents('a').first().css({ 'color': 'black' });
                          }
                      });
                  }
              },
              buttonText: function (options, select) {
                  if (options.length === 0) {
                      let currentDropdown = $(select);
                      let currentRow = currentDropdown.closest('tr');
                      currentRow.find('.day_selection input').each(function () {
                          $(this).prop('disabled', false);
                          $(this).parents('a').first().css({ 'color': 'black' });
                      });
                      return 'Select';
                  } else if (options.length > 1) {                
                      return options.length + ' selected';
                  } else {
                      let labels = [];
                      options.each(function () {                    
                          if ($(this).attr('label') !== undefined) {
                              labels.push($(this).attr('label'));
                          } else {
                              labels.push($(this).html());
                          }
                          let value = $(this).val();
                          if (value === "0") {
                              let currentDropdown = $(select);
                              let currentRow = currentDropdown.closest('tr');
                              currentRow.find('.day_selection input').each(function () {
                                  if ($(this).val() !== "0") {
                                      $(this).prop('disabled', true);
                                      $(this).parents('a').first().css({ 'color': 'grey' });
                                  }
                              });
                          }                    
                      });
                      return labels.join(', ') + '';
                  }
              }
          });
      }
  });
  
  $('.fa-question-circle').off('click').on('click', function(){
      let tag = $(this).attr('tag');
      let obj = $.grep(help_notes, function(e){ return e.value === tag; });
      $('#help_modal').modal('show');
      $('#min').html(obj[0].min);
      $('#max').html(obj[0].max);
      $('#units').html(obj[0].units);
      $('#description').html(obj[0].description);
      $('#help_modal h4').html(obj[0].title);
  });
}

/**
* @brief function to update the time restriction HTML.
*/
function updateTimeRestrictionsHTML(){
  let startDateTimePicker = $('.start_datetime_picker');
  let endDateTimePicker = $('.end_datetime_picker');
  let dateConfig = {
      dateFormat: "Y-m-d H:i:S",
      allowInput: true,        
      enableTime: true,
      enableSeconds: true,
      minuteIncrement: 1,
      secondIncrement: 1,
      time_24hr: true
  };
  startDateTimePicker.flatpickr(dateConfig);
  endDateTimePicker.flatpickr(dateConfig);

  $(document).on('change', '.form-check-input.time_period', function () {
      $('.time_period_range_fields').hide();
      $('.time_period_general_fields').hide();
      if ($(this).val() === 'range') {
          $('.time_period_range_fields').show();
      } else if ($(this).val() === 'general') {
          $('.time_period_general_fields').show();
      }
  });
  $('.day_selection_dropdown').multiselect({
      maxHeight: 200,        
      onChange: function(option, checked, select) {            
          if ($(option).val() === '0' && checked) {
              $('.day_selection').find('input').each(function() {
                  if ($(this).val() !== '0') {
                      $(this).prop('disabled', true);
                      $(this).prop('checked', false).trigger('change');
                      $(this).parents('a').first().css({ 'color': 'grey' });
                      $(this).parents('li').first().removeClass('active');
                  }
              });
          } else {
              let isAllSelected = false;
              $('.day_selection').find('input').each(function () {
                  if ($(this).val() === '0' && $(this).prop('checked')) {
                      isAllSelected = true
                  }
              });
              $('.day_selection').find('input').each(function () {
                  if (isAllSelected) {
                      //Disable all options except 0 (All)
                      if ($(this).val() !== '0') {
                          $(this).prop('disabled', true);
                          $(this).parents('a').first().css({ 'color': 'grey' });
                          $(this).parents('li').first().removeClass('active');
                      }
                  }else{
                      //Reset all options to enabled 
                      $(this).prop('disabled', false);
                      $(this).parents('a').first().css({ 'color': 'black' });
                  }
                 
              });
          }
      },
      buttonText: function (options, select) {
          
          if (options.length === 0) {
              $('.day_selection').find('input').each(function () {
                  $(this).prop('disabled', false);
                  $(this).parents('a').first().css({ 'color': 'black' });
              });
              return 'Select Day Of The Week'
          } else if (options.length > 1) {                
              return options.length + ' selected';
          } else {
              let labels = [];
              options.each(function () {                    
                  if ($(this).attr('label') !== undefined) {
                      labels.push($(this).attr('label'));
                  }
                  else {
                      labels.push($(this).html());
                  }
                  let value = $(this).val();
                  if (value === "0") {
                      $('.day_selection').find('input').each(function () {
                          if ($(this).val() !== "0") {
                              $(this).prop('disabled', true);
                              $(this).parents('a').first().css({ 'color': 'grey' });
                          }
                      });
                  }                    
              });
              return labels.join(', ') + '';
          }
      }
  });
  
  $('.fa-question-circle').click(function(){
      let tag = $(this).attr('tag');
      let obj = $.grep(help_notes, function(e){ return e.value === tag; });
      $('#help_modal').modal('show');
      $('#min').html(obj[0].min)
      $('#max').html(obj[0].max)
      $('#units').html(obj[0].units)
      $('#description').html(obj[0].description)
      $('#help_modal h4').html(obj[0].title)
  });
}

/**
 * @brief Disables RGA fields associated with speed limits
 */
function disableRGAFieldsAssociatedToSpeedLimits(){
    $("[id*=speedLimitType] option[value='Passenger Vehicles Max Speed']").prop('disabled', true);
    $("[id*=speedLimitType] option[value='Passenger Vehicles Min Speed']").prop('disabled', true);
    $("input[name^='speedForm_'][name$='_speedLimitChoice'][value='advisory']").prop('disabled', true);
}

/**
 * @brief Hides RGA fields associated with speed limits
 * @details This function hides the speed limit type options for Passenger Vehicles Max Speed and Min Speed, and hides speed limit choices
 */
function hideRGAFieldsAssociatedToSpeedLimits(){
    $("[id*=speedLimitType] option[value='Passenger Vehicles Max Speed']").hide();
    $("[id*=speedLimitType] option[value='Passenger Vehicles Min Speed']").hide();
    $(".speed_limit_choice").hide();
    $("input[name^='speedForm_'][name$='_speedLimitChoice'][value='regulatory']").prop('checked', false);
}

/***
 * @brief Check if the selected speed limit type is Passenger Vehicles Max Speed
 * @return {boolean} true if selected speed limit type is Passenger Vehicles Max Speed, false otherwise
 */
function isSpeedLimitTypePassengerVehicleMaxSpeedSelected(){
  let isChecked = false;
  $("[id*=speedLimitType] option[value='Passenger Vehicles Max Speed']").each(function() {
    if ($(this).is(":selected")) {
      isChecked = true;
    }
  });
  return isChecked;
}

/***
 * @brief Check if the selected speed limit type is Passenger Vehicles Min Speed
 * @return {boolean} true if selected speed limit type is Passenger Vehicles Min Speed, false otherwise
 */
function isSpeedLimitTypePassengerVehicleMinSpeedSelected(){
  let isChecked = false;
  $("[id*=speedLimitType] option[value='Passenger Vehicles Min Speed']").each(function() {
    if ($(this).is(":selected")) {
      isChecked = true;
    }
  });
  return isChecked;
}
export {
  getCookie,
  isOdd,
  setRGAStatus,
  enableRGAFields,
  addRGAFieldsValidation,
  referencePointWindow,
  populateRefWindow,
  hideRGAFields,
  updateSharedWith,
  updateTypeAttributes,
  toggleLaneTypeAttributes,
  populateAttributeWindow,
  toggle,
  toggleBars,
  toggleLanes,
  togglePoints ,
  toggleWidthArray,
  directVincenty,
  inverseVincenty,
  toDeg,
  toRad,
  updateDisplayedLaneAttributes,
  removeLaneAttributes,
  rebuildConnections,
  rebuildApproaches,
  makeDraggable,
  makeDroppable,
  setLaneAttributes,
  saveConnections,
  saveApproaches,
  addSpeedForm,
  resetSpeedDropdowns,
  saveSpeedForm,
  rebuildSpeedForm,
  unselectFeature,
  removeSpeedForm,
  addApproachRow,
  addRow,
  deleteRow,
  deleteApproachRow,
  resetLaneAttributes,
  onMappedGeomIdChangeCallback,
  onRegionIdChangeCallback,
  onRoadAuthorityIdChangeCallback,
  resetRGAStatus,
  getLength,
  copyTextToClipboard,
  debounceModifyEvent,
  getLaneInfoDaySelection,
  getLaneInfoTimePeriod,
  updateLaneInfoDaySelection,
  updateLaneInfoTimePeriod,
  updateTimeRestrictionsHTML,
  updateConnectionsTimeRestrictionsHTML,
  addLaneInfoTimeRestrictions,
  addApproachTimeRestrictions,
  addConnectionsTimeRestrictions,
  updateDeleteButtonStates,
  disableRGAFieldsAssociatedToSpeedLimits,
  isSpeedLimitTypePassengerVehicleMaxSpeedSelected,
  isSpeedLimitTypePassengerVehicleMinSpeedSelected,
  hideRGAFieldsAssociatedToSpeedLimits,
  updateApproachTimeRestrictionsHTML
}