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
import { barHighlightedStyle, barStyle, connectionsStyle, errorMarkerStyle, measureStyle, pointStyle } from "./style.js";
import { onMoveEnd, onPointerMove, onZoomCallback, onZoomIn, onZoomOut } from "./map-event.js";
import { getElev, populateAutocompleteSearchPlacesDropdown, getElevation } from "./api.js";
import { deleteTrace } from "./mapTools.js"

import { deleteMode, addITISForm, removeITISForm, rebuildITISForm } from "./main.js";
var map;
var vectors, lanes, laneMarkers, area, polygons, polyMarkers, radiuslayer, trace, laneWidths;
var fromProjection, toProjection;
var temp_lat, temp_lon, selected_marker, selected_layer, selected_marker_limit;
var mutcd, priority, direction, extent, info_type, ttl;
var circle_bounds;
let box, laneConnections, errors;
let overlayLayersGroup, baseLayersGroup;
const aerialMaxZoom = 19;
var layerSwitcher;
let controls;
let activeControlKey = null;
const transparentTile =
  "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/w8AAn8B9p4n6wAAAABJRU5ErkJggg==";
var viewLat, viewLon, viewZoom;
var content = [];
var elevation_url = 'https://dev.virtualearth.net/REST/v1/Elevation/List?hts=ellipsoid&points=';
var nodeLaneWidth = [];
var circles_temp = [];
var circles_reset = [];
/**Adding Azure Map */
const tilesetURL = "/msp/azuremap/api/proxy/tileset/";
const tokenURL = "/msp/security/api/csrf-token/";
var aerialTilesetId = "microsoft.imagery";
const roadTilesetId = "microsoft.base.road";
const hybridTilesetId = "microsoft.base.hybrid.road";
let temporaryBoxMarkers;
let temporaryLaneMarkers;
let measureSource = new ol.source.Vector();
let measureLayer = new ol.layer.Vector({
  source: measureSource,
  style: measureStyle
})
// Helper to get cookie
export function getCookie(cname) {
  var name = cname + "=";
  var decodedCookie = decodeURIComponent(document.cookie);
  var ca = decodedCookie.split(';');
  for (var i = 0; i < ca.length; i++) {
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

const laneDefault = {
  strokeColor: "#FF9900",
  fillColor: "#FF9900",
  strokeOpacity: 1,
  strokeWidth: 5,
  fillOpacity: 0.9,
  pointRadius: 8,
  fontFamily: "Arial",
  fontSize: "10px",
  cursor: "pointer"
};

const laneStyle = (feature) => new ol.style.Style({
  stroke: new ol.style.Stroke({
    color: feature.get('computed') ? '#FF5000' : (feature.get('source') ? '#00EDFF' : laneDefault.strokeColor),
    width: laneDefault.strokeWidth
  }),
  fill: new ol.style.Fill({
    color: feature.get('computed') ? '#FF5000' : (feature.get('source') ? '#00EDFF' : laneDefault.fillColor)
  }),
  text: new ol.style.Text({
    text: feature.getGeometry().getType() === "LineString" ? '' : (
      feature.get('laneNumber')
        ? String(parseInt(feature.get('laneNumber'), 10))
        : ''
    ),
    font: `${laneDefault.fontSize} ${laneDefault.fontFamily}`,
    fill: new ol.style.Fill({ color: '#000' })
  }),
  image: new ol.style.Circle({
    radius: laneDefault.pointRadius,
    fill: new ol.style.Fill({
      color: feature.get('computed') ? '#FF5000' : (feature.get('source') ? '#00EDFF' : laneDefault.fillColor)
    })
  })
});

// AREA STYLE
const areaStyle = new ol.style.Style({
  stroke: new ol.style.Stroke({
    color: 'rgba(0, 255, 51, 1)',  // #00FF33
    width: 3
  }),
  fill: new ol.style.Fill({
    color: 'rgba(0, 255, 51, 0)'  // Transparent
  }),
  image: new ol.style.Circle({
    radius: 2,
    fill: new ol.style.Fill({ color: 'rgba(0, 255, 51, 0)' }),
    stroke: new ol.style.Stroke({ color: 'rgba(0, 255, 51, 1)', width: 3 })
  })
});

// POLY STYLE
const polyStyle = new ol.style.Style({
  stroke: new ol.style.Stroke({
    color: 'rgba(255, 153, 0, 1)',  // #FF9900
    width: 4
  }),
  fill: new ol.style.Fill({
    color: 'rgba(255, 153, 0, 0.2)'  // Semi-transparent
  }),
  image: new ol.style.Circle({
    radius: 6,
    fill: new ol.style.Fill({ color: 'rgba(255, 153, 0, 0.2)' }),
    stroke: new ol.style.Stroke({ color: 'rgba(255, 153, 0, 1)', width: 4 })
  })
});

// POLY STYLE 2
const polyStyle2 = new ol.style.Style({
  stroke: new ol.style.Stroke({
    color: 'rgba(255, 153, 0, 1)',
    width: 4
  }),
  fill: new ol.style.Fill({
    color: 'rgba(255, 153, 0, 0.9)'
  }),
  image: new ol.style.Circle({
    radius: 6,
    fill: new ol.style.Fill({ color: 'rgba(255, 153, 0, 0.9)' }),
    stroke: new ol.style.Stroke({ color: 'rgba(255, 153, 0, 1)', width: 4 })
  })
});

// VECTOR STYLE
const vectorStyle = new ol.style.Style({
  stroke: new ol.style.Stroke({
    color: 'rgba(255, 153, 0, 1)',
    width: 1
  }),
  fill: new ol.style.Fill({
    color: 'rgba(255, 153, 0, 0)'  // Transparent fill
  }),
  image: new ol.style.Circle({
    radius: 1,
    fill: new ol.style.Fill({ color: 'rgba(255, 153, 0, 0)' }),
    stroke: new ol.style.Stroke({ color: 'rgba(255, 153, 0, 1)', width: 1 })
  })
});

// WIDTH STYLE
const widthStyle = new ol.style.Style({
  stroke: new ol.style.Stroke({
    color: 'rgba(255, 255, 0, 0.5)',  // #FFFF00
    width: 1
  }),
  fill: new ol.style.Fill({
    color: 'rgba(255, 255, 0, 0.1)'  // Semi-transparent yellow
  }),
  image: new ol.style.Circle({
    radius: 1,
    fill: new ol.style.Fill({ color: 'rgba(255, 255, 0, 0.1)' }),
    stroke: new ol.style.Stroke({ color: 'rgba(255, 255, 0, 0.5)', width: 1 })
  })
});

let csrfToken = null;

const getCSRFToken = () => {
  // Fetch CSRF token from the server and return the token string, or null if unavailable
  return fetch(tokenURL)
    .then(response => {
      if(response.status !== 200) {
        console.error("Failed to fetch CSRF token");
        return null;
      }
      return response.json();
    })
    .then(data => {
      csrfToken = data.csrfToken;
      return csrfToken;
    });
};

/**
 * Custom tile load function to fetch tiles with CSRF token.
 * Used for the baseAerialLayer in initMap().
 */
const customTileLoadFunction = (imageTile, src) => {
  const xhr = new XMLHttpRequest();
  xhr.open('GET', src);
  xhr.responseType = 'blob';
  if (csrfToken) {
    xhr.setRequestHeader('X-CSRF-TOKEN', csrfToken);
  }
  xhr.onload = function() {
    if (xhr.status === 200) {
      const url = URL.createObjectURL(xhr.response);
      imageTile.getImage().src = url;
    }else {
      imageTile.getImage().src = transparentTile;
    }
  };
  xhr.onerror = function() {
    imageTile.getImage().src = transparentTile;
  };
  xhr.send();
};

function init() {

  const d = new Date();
  const t = d.getTime().toString();
  $('#packet_id').val(t.slice(-9));

  $('option:selected').prop("selected", false);
  $('#deposit_check').prop('checked', false);
  $('#ttl').hide();

  $('#drawPoly').prop('disabled', false);
  $('#editPoly').prop('disabled', false);
  $('#drawLanes').prop('disabled', false);
  $('#editLanes').prop('disabled', false);
  $('#drawCircle').prop('disabled', false);
  $('#dragPoly').prop('disabled', false);



  /**
   * Base layers using XYZ tile sources
   */
  const baseAerialLayer = new ol.layer.Tile({
    title: "Azure Aerial",
    source: new ol.source.XYZ({
      url: tilesetURL + aerialTilesetId + "/{z}/{x}/{y}",
      tileLoadFunction: customTileLoadFunction
    }),
    type: "base",
    visible: true,
  });

  const baseRoadLayer = new ol.layer.Tile({
    title: "Azure Roads",
    source: new ol.source.XYZ({
      url: tilesetURL + roadTilesetId + "/{z}/{x}/{y}",
      tileLoadFunction: customTileLoadFunction
    }),
    type: "base",
    visible: false,
  });

  const baseHybridLayer = new ol.layer.Tile({
    title: "Azure Hybrid",
    source: new ol.source.XYZ({
      url: tilesetURL + hybridTilesetId + "/{z}/{x}/{y}",
      tileLoadFunction: customTileLoadFunction
    }),
    type: "base",
    visible: false,
  });
  /**
   * Layer groups
   */
  baseLayersGroup = new ol.layer.Group({
    title: "Base Layers",
    layers: [baseAerialLayer, baseRoadLayer, baseHybridLayer],
  });


  /**
   * Restore map view from cookies or use defaults
   * 
   * 
   */
  fromProjection = ol.proj.get('EPSG:4326');  // WGS 1984
  toProjection = ol.proj.get('EPSG:3857');    // Spherical Mercator

  viewLat = parseFloat(getCookie("tim_latitude")) || 42.3373873;
  viewLon = parseFloat(getCookie("tim_longitude")) || -83.051308;
  viewZoom = parseInt(getCookie("tim_zoom")) || 18;
  viewZoom = viewZoom < aerialMaxZoom ? viewZoom : aerialMaxZoom;

  const center = ol.proj.fromLonLat([viewLon, viewLat]);
  map = new ol.Map({
    view: new ol.View({
      center: center,
      zoom: viewZoom,
      projection: "EPSG:3857",
    }),
    target: "map",
  });


  map.addLayer(baseLayersGroup);


  // map.addLayer(temporaryBoxMarkers);

  /**
   * Save map state on movement
   */
  map.on("moveend", () => {
    const view = map.getView();
    const [lon, lat] = ol.proj.toLonLat(view.getCenter());
    const zoom = view.getZoom();

    document.cookie = `tim_latitude=${lat}; max-age=${365 * 24 * 60 * 60}`;
    document.cookie = `tim_longitude=${lon}; max-age=${365 * 24 * 60 * 60}`;
    document.cookie = `tim_zoom=${zoom}; max-age=${365 * 24 * 60 * 60}`;

    document.querySelector("#zoomLevel .zoom").textContent = zoom;
  });

  /**
   * Restore additional UI settings
   */
  if (getCookie("tim_node_offsets") !== "") {
    $("#node_offsets").val(getCookie("tim_node_offsets"));
  }

  if (getCookie("tim_enable_elevation") !== "") {
    $("#enable_elevation").prop("checked", getCookie("tim_enable_elevation") === "true");
  } else {
    $("#enable_elevation").prop("checked", true);
  }
}

function registerMapEvents() {
  map.on("moveend", (event) => {
    onMoveEnd(event, map);
  });
  map.on("pointermove", (event) => {
    onPointerMove(event, map, controls);
  });
  document.getElementById('customZoomOut').addEventListener('click', (event) => {
    onZoomOut(event, map);
  });
  document.getElementById('customZoomIn').addEventListener('click', (event) => {
    onZoomIn(event, map);
  });
  map.getView().on("change:resolution", (event) => {
    onZoomCallback(event, map);
  });

  // Define vector source and layer
  const laneSource = new ol.source.Vector();

  lanes = new ol.layer.Vector({
    source: laneSource,
    style: laneStyle, // equivalent to laneStyleMap
    properties: {
      name: "Lane Layer"
    }
  });

  // Handle "beforefeatureadded" + "featureadded" via 'addfeature' listener
  laneSource.on('addfeature', function (evt) {
    const features = laneSource.getFeatures();

    if (features.length > 1) {
      alert("Service Region already defined.");
      laneSource.removeFeature(evt.feature);
      return;
    }

    // Disable controls on feature add
    $('#drawPoly').prop('disabled', true);
    $('#editPoly').prop('disabled', true);
    $('#drawCircle').prop('disabled', true);
    $('#dragPoly').prop('disabled', true);
  });

  // Simulate 'featureselected' via interaction
  const selectLane = new ol.interaction.Select({
    layers: [lanes],
    condition: ol.events.condition.click
  });

  selectLane.on('select', function (evt) {
    const selected = evt.selected[0];
    if (deleteMode && selected) {


      deleteMarker(lanes, selected);

      // Re-enable controls
      document.getElementById('drawPoly').disabled = false;
      document.getElementById('editPoly').disabled = false;
      document.getElementById('drawCircle').disabled = false;
      document.getElementById('dragPoly').disabled = false;
    }
  });

  // Add to map
  map.addLayer(lanes);
  map.addInteraction(selectLane);
  const polygonSource = new ol.source.Vector();

  temporaryLaneMarkers = new ol.layer.Vector({
    source: new ol.source.Vector(),
    style: laneStyle
  });
  map.addLayer(temporaryLaneMarkers);

  polygons = new ol.layer.Vector({
    source: polygonSource,
    style: polyStyle,
    properties: {
      name: "Polygon Layer"
    }
  });

  polygonSource.on('addfeature', function (evt) {
    const features = polygonSource.getFeatures();

    if (features.length > 1) {
      alert("Region already defined.");
      polygonSource.removeFeature(evt.feature);
      return;
    }

    $('#drawLanes').prop('disabled', true);
    $('#editLanes').prop('disabled', true);
  });

  const polygonSelect = new ol.interaction.Select({
    layers: [polygons],
    condition: ol.events.condition.click
  });

  polygonSelect.on('select', function (evt) {
    const selected = evt.selected[0];
    if (deleteMode && selected) {
      deleteMarker(polygons, selected);
      $('#drawLanes').prop('disabled', false);
      $('#editLanes').prop('disabled', false);
    }
  });

  map.addLayer(polygons);
  map.addInteraction(polygonSelect);

  const areaSource = new ol.source.Vector();

  area = new ol.layer.Vector({
    source: areaSource,
    style: areaStyle,
    properties: {
      name: "Deposit Area Layer"
    }
  });

  areaSource.on('addfeature', function (evt) {
    const features = areaSource.getFeatures();
    if (features.length > 1) {
      alert("Applicable Region already defined.");
      areaSource.removeFeature(evt.feature);
      return;
    }
  });

  const areaSelect = new ol.interaction.Select({
    layers: [area],
    condition: ol.events.condition.click
  });

  areaSelect.on('select', function (evt) {
    const selected = evt.selected[0];
    if (!selected) return;

    selected_marker = selected;

    $(".selection-panel").text('Applicable Region');
    $(".marker-info-tab a").text("Region Info");

    if (deleteMode) {
      deleteMarker(area, selected_marker);
      return;
    }

    $('#attribute-tabs li').removeClass('active');
    $('#itis-tab').removeClass('active');
    $('#direction-tab').removeClass('active');
    $('#content-tab').removeClass('active');
    $('#road-condition-tab').removeClass('active');
    $('#marker-info-tab').addClass('active');

    $("#nwlat").prop('readonly', true);
    $("#nwlong").prop('readonly', true);
    $("#selat").prop('readonly', true);
    $("#selong").prop('readonly', true);

    $(".lat, .long, .elev, .radius, .verified_lat, .verified_long, .verified_elev, .start_time, .end_time, .info-type, .extent, .lane_width, .master_lane_width, .speed_limit, .direction-tab, .content-tab, .itis-tab, .road-condition-tab, .ssp_tim_rights, .ssp_loc_rights, .ssp_type_rights, .ssp_content_rights, .road_condition, .road_condition").hide();
    $(".nwlat, .nwlong, .selat, .selong").show();

    const geom = selected_marker.getGeometry().getCoordinates()[0];
    const nwCoord = ol.proj.transform(geom[1], toProjection, fromProjection);
    const seCoord = ol.proj.transform(geom[3], toProjection, fromProjection);

    selected_layer = area;

    $("#nwlat").val(nwCoord[1]);
    $("#nwlong").val(nwCoord[0]);
    $("#selat").val(seCoord[1]);
    $("#selong").val(seCoord[0]);

    $("#attributes").show();
  });

  areaSelect.on('deselect', function (evt) {
    $("#attributes").hide();
    selected_marker = null;
  });

  map.addLayer(area);
  map.addInteraction(areaSelect);

  const laneMarkerSource = new ol.source.Vector();

  laneMarkers = new ol.layer.Vector({
    source: laneMarkerSource,
    style: laneStyle,
    properties: {
      name: "Lane Marker Layer"
    }
  });

  const laneMarkerSelect = new ol.interaction.Select({
    layers: [laneMarkers],
    condition: ol.events.condition.click
  });

  laneMarkerSelect.on('select', function (evt) {
    const selected = evt.selected[0];
    if (!selected) return;

    selected_marker = selected;

    $(".selection-panel").text('Region of Use');
    $(".marker-info-tab a").text("Lane Info");

    if (deleteMode) {
      deleteMarker(laneMarkers, selected_marker);
      return;
    } else {
      updateNonReferenceFeatureLocation(selected_marker);
    }

    $('#attribute-tabs li').removeClass('active');
    $('#itis-tab, #direction-tab, #content-tab', '#road-condition-tab').removeClass('active');
    $('#marker-info-tab').addClass('active');

    $("#lat, #long, #elev").prop('readonly', false);

    $('.radius, .verified_lat, .verified_long, .verified_elev, .start_time, .end_time, .info-type, .nwlat, .nwlong, .selat, .selong, .speed_limit, .master_lane_width, .ssp_tim_rights, .ssp_loc_rights, .ssp_type_rights, .ssp_content_rights, .road_condition, .road_surface').hide();
    $('.direction-tab, .content-tab, .itis-tab').hide();

    $(".lat, .long, .elev, .lane_width").show();

    if (selected_marker.get('number') === 0) {
      $(".extent").show();
      $(".regionFeatures br").show();
    } else {
      $(".extent").hide();
      $(".regionFeatures br").hide();
    }

    selected_layer = laneMarkers;

    const laneIndex = selected_marker.get('lane');
    const laneFeature = lanes.getSource().getFeatures()[laneIndex];

    if (laneFeature && laneFeature.get('laneWidth')) {
      nodeLaneWidth = laneFeature.get('laneWidth');
    }

    const markerNumber = selected_marker.get('number');
    const widthValue = nodeLaneWidth?.[markerNumber] ?? "0";
    $("#lane_width").val(widthValue);

    const elev = selected_marker.get('elevation')?.value;
    $("#elev").val(elev ?? "");

    const extentValue = selected_marker.get('extent');
    const extentHtml = extentValue ? `${extentValue} <span class='caret'></span>` : `Select An Extent <span class='caret'></span>`;
    $('#extent .dropdown-toggle').html(extentHtml);

    temp_lat = selected_marker.get('LonLat')?.lat;
    temp_lon = selected_marker.get('LonLat')?.lon;
    populateAttributeWindow(temp_lat, temp_lon);

    $("#attributes").show();
  });

  laneMarkerSelect.on('deselect', function (evt) {
    $("#attributes").hide();
    selected_marker = null;
  });

  map.addLayer(laneMarkers);
  map.addInteraction(laneMarkerSelect);

  const polyMarkerSource = new ol.source.Vector();

  polyMarkers = new ol.layer.Vector({
    source: polyMarkerSource,
    style: polyStyle2,
    properties: {
      name: "Poly Marker Layer"
    }
  });

  const polyMarkerSelect = new ol.interaction.Select({
    layers: [polyMarkers],
    condition: ol.events.condition.click
  });

  polyMarkerSelect.on('select', function (evt) {
    const selected = evt.selected[0];
    if (!selected) return;

    selected_marker = selected;

    $(".selection-panel").text('Region of Use');

    if (selected.get('title') === "circle") {
      $(".marker-info-tab a").text("Circle Info");
      $(".radius").show();
    } else {
      $(".marker-info-tab a").text("Region Info");
      $(".radius").hide();
    }

    if (deleteMode) {
      deleteMarker(polyMarkers, selected_marker);
      return;
    } else {
      updateNonReferenceFeatureLocation(selected_marker);
    }

    $('#attribute-tabs li').removeClass('active');
    $('#itis-tab, #direction-tab, #content-tab, #road-condition-tab').removeClass('active');
    $('#marker-info-tab').addClass('active');

    $("#lat, #long, #elev").prop('readonly', false);

    $(".verified_lat, .verified_long, .verified_elev, .start_time, .end_time, .info-type, .nwlat, .nwlong, .selat, .selong, .lane_width, .master_lane_width, .extent, .speed_limit, .regionFeatures br, .ssp_tim_rights, .ssp_type_rights, .ssp_content_rights, .ssp_loc_rights, .road_surface, .road_condition").hide();
    $(".direction-tab, .content-tab, .itis-tab").hide();

    $(".lat, .long, .elev").show();

    selected_layer = polyMarkers;

    const elev = selected.get('elevation')?.value;
    $("#elev").val(elev ?? "");

    temp_lat = selected.get('LonLat')?.lat;
    temp_lon = selected.get('LonLat')?.lon;

    populateAttributeWindow(temp_lat, temp_lon);

    $("#attributes").show();
  });

  polyMarkerSelect.on('deselect', function (evt) {
    $("#attributes").hide();
    selected_marker = null;
  });

  map.addLayer(polyMarkers);
  map.addInteraction(polyMarkerSelect);

  const vectorSource = new ol.source.Vector();

  vectors = new ol.layer.Vector({
    source: vectorSource,
    style: vectorStyle,
    properties: {
      name: "Vector Layer"
    }
  });

  vectorSource.on('addfeature', function (evt) {
    selected_marker = evt.feature;
    updateFeatureLocation(evt.feature);
  });

  const vectorSelect = new ol.interaction.Select({
    layers: [vectors],
    condition: ol.events.condition.click
  });

  vectorSelect.on('select', function (evt) {
    const selected = evt.selected[0];
    if (!selected) return;

    selected_marker = selected;

    if (deleteMode) {
      deleteMarker(vectors, selected_marker);
      content = [];
    } else {
      updateFeatureLocation(selected_marker);
    }
  });

  vectorSelect.on('deselect', function (evt) {
    $("#attributes").hide();
    selected_marker = null;
  });

  map.addLayer(vectors);
  map.addInteraction(vectorSelect);

  const laneWidthSource = new ol.source.Vector();

  laneWidths = new ol.layer.Vector({
    source: laneWidthSource,
    style: widthStyle,
    properties: {
      name: "Width Layer"
    }
  });

  laneWidthSource.on('addfeature', function (evt) {
    // no-op
  });

  const radiuslayerSource = new ol.source.Vector();

  radiuslayer = new ol.layer.Vector({
    source: radiuslayerSource,
    properties: {
      name: "Radius Layer"
    }
  });

}


function initTopNavBar() {

  /***
   * Purpose: Autocomplete for allowing place search
   */
  let searchInput = $("#address-search");
  searchInput.keyup((event) => {
    if (
      event.key === "ArrowDown" ||
      event.key === "ArrowLeft" ||
      event.key === "ArrowRight" ||
      event.key === "ArrowUp"
    ) {
      return;
    }
    let searchResultDropdown = $("#dropdown-menu-search");
    searchResultDropdown.empty();
    let inputText = event.target.value;
    if (inputText?.length > 0) {
      populateAutocompleteSearchPlacesDropdown(map, inputText);
    } else {
      searchResultDropdown.hide();
    }
  });
}

const updateDisplay = function (event) {
  $('.measurement').text(event.measure.toFixed(3) + ' ' + event.units);
  copyTextToClipboard(event.measure.toFixed(3));
};

function registerDrawInteractions() {

  //Controls for the lane layer to draw and modify
  // Initialize all controls

  controls = {
    line: new ol.interaction.Draw({
      source: lanes.getSource(),
      type: 'LineString'
    }),

    modify: new ol.interaction.Modify({
      source: lanes.getSource(),

    }),
    change: new ol.interaction.Modify({
      source: polygons.getSource()
    }),

    drag: new ol.interaction.Translate({
      features: new ol.Collection()  

    }),

    area: new ol.interaction.Draw({
      source: area.getSource(),
      type: 'Circle',
      geometryFunction: ol.interaction.Draw.createBox()
    }),

    polygon: new ol.interaction.Draw({
      source: polygons.getSource(),
      type: 'Polygon'
    }),
    circle: new ol.interaction.Draw({
    source: polygons.getSource(),          // same target layer
    type: 'Circle',                        // circle interaction
    geometryFunction: ol.interaction.Draw.createRegularPolygon(32) 
    }),

    dragPoly: new ol.interaction.Translate({
      layers: [polygons]
    }),

    edit: new ol.interaction.Transform({
      layers: [area],
      enableScaling: true,
      enableTranslate: true,
      enableRotate: false
    }),


    del: new ol.interaction.Select({
      layers: [lanes, vectors, area, polygons],
      toggleCondition: ol.events.condition.never
    }),

    none: new ol.interaction.Select({
      layers: [laneMarkers, polyMarkers, vectors, area],
      toggleCondition: ol.events.condition.always
    }),

    measure: new ol.interaction.Draw({
      source: measureSource,
      type: 'LineString'
    })
  };

  controls.modify.on('modifystart', function (event) {
    event.features.forEach(feature => {
      if (feature.getGeometry() instanceof ol.geom.LineString) {
        // Update markers while moving (geometry changes)
        feature.getGeometry().on('change', function () {
          temporaryLaneMarkers.getSource().clear();
          showMarkers(feature, temporaryLaneMarkers);
        });
      }
    });
  });
  // End modifying lanes: Update markers when a vertex is moved
  controls.modify.on('modifyend', function (event) {
    event.features.forEach(feature => {
      if (feature.getGeometry() instanceof ol.geom.LineString) {
        temporaryLaneMarkers.getSource().clear();
        showMarkers(feature, temporaryLaneMarkers);
      }
    });
  });

  // === Add all to map and deactivate ===
  for (const key in controls) {
    map.addInteraction(controls[key]);
    controls[key].setActive(false);
  }

  // === Feature-added callbacks ===
  controls.line.on('drawend', (event) => {
    // lanes.getSource().addFeature(event.feature);
    onFeatureAdded(lanes, vectors, laneMarkers, laneWidths, false);
  });

  controls.polygon.on('drawend', (event) => {
    event.feature.set('title', 'region');
    onFeatureAdded(polygons, vectors, laneMarkers, laneWidths, false);
  });

  controls.circle.on('drawend', (event) => {
    event.feature.set('title', 'circle');
    onFeatureAdded(polygons, vectors, laneMarkers, laneWidths, false);
  });


  controls.measure.on('drawstart', (event) => {
    measureSource.clear();
    measureCallback(event);
  });
  controls.measure.on('drawend', (event) => {
    measureCallback(event);
  });
  map.addLayer(radiuslayer);
  map.addLayer(laneWidths);

  try {
    const location = ol.proj.fromLonLat([viewLon, viewLat]);
    map.getView().setCenter(location);
    map.getView().setZoom(viewZoom);
  } catch (err) {
    console.log("No vectors to reset view");
  }

  $('#OpenLayers_Control_MinimizeDiv_innerImage').attr('src', "img/layer-switcher-minimize.png");
  $('#OpenLayers_Control_MaximizeDiv_innerImage').attr('src', "img/layer-switcher-maximize.png");

}

// /**
//  * Purpose: copies measurement to clipboard
//  * @params  measurement value
//  * @event copy
//  */

function copyTextToClipboard(text) {
  var textArea = document.createElement("textarea");

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

//  * Purpose: removes features from the map
//  * @params  map layers and features
//  * @event remove features and all of it's metadata
//  */

export function Clear() {
  if (confirm("Clear all of the map features?")) {
    lanes.getSource().clear();
    laneMarkers.getSource().clear();
    vectors.getSource().clear();
    area.getSource().clear();
    polygons.getSource().clear();
    polyMarkers.getSource().clear();
    radiuslayer.getSource().clear();
    laneWidths.getSource().clear();

    deleteTrace();

    try {
      for (let d = 0; d < 360; d += 22.5) {
        drawCircle(ctx, cx, cy, cr, "black", "white", d, circles_reset);
      }
    } catch (err) {
      console.log("could not reset circle");
    }

    //         circles_temp = JSON.parse(JSON.stringify(circles_reset));
    //         circles = JSON.parse(JSON.stringify(circles_reset));

    $('#drawPoly').prop('disabled', false);
    $('#editPoly').prop('disabled', false);
    $('#drawLanes').prop('disabled', false);
    $('#editLanes').prop('disabled', false);
    $('#drawCircle').prop('disabled', false);
    $('#dragPoly').prop('disabled', false);
  }
}


function deleteMarker(layer, feature) {
  $("#attributes").hide();

  try {
    if (feature.attributes?.marker?.type == "TIM") {
      for (let d = 0; d < 360; d += 22.5) {
        drawCircle(ctx, cx, cy, cr, "black", "white", d, circles_reset);
      }
    }
  } catch (err) {
    console.log("type not defined");
  }

  circles_temp = JSON.parse(JSON.stringify(circles_reset));
  circles = JSON.parse(JSON.stringify(circles_reset));

  if (feature.getGeometry() instanceof ol.geom.LineString) {
    console.log("Deleting a LineString feature");
    temporaryLaneMarkers.getSource().clear();  // Example
  }
  if (feature.getGeometry() instanceof ol.geom.Polygon && layer === polygons) {
    polyMarkers.getSource().clear();  
  }

  layer.getSource().removeFeature(feature);
  layer.changed();
}



// /*********************************************************************************************************************/
// /**
//  * Purpose: toggle control of all the layers and modal windows
//  * @params  click events and the corresponding feature type
//  * @event loads help or the drawing control with a specific element in mind
//  */
// /*********************************************************************************************************************/


function toggleControlsOn(state) {
  if (state == 'help') {
    $("#instructions_modal").modal('show');
  } else {
    $("#instructions_modal").modal('hide');
    if (controls) {
      toggleControl(state);
    }
    if (state == 'modify' || state == 'del' || state == 'dragPoly') {
      laneMarkers.getSource().clear();
      polyMarkers.getSource().clear();
      radiuslayer.getSource().clear();
      controls?.del?.getFeatures().clear();

    } else {
      onFeatureAdded();
    }
  }
}



function toggleControl(selectedKey) {
  for (const key in controls) {
    const control = controls[key];

    // Add interaction if not already on the map
    if (!map.getInteractions().getArray().includes(control)) {
      map.addInteraction(control);
    }

    // Activate the selected control; deactivate others
    if (key === selectedKey) {
      control.setActive(true);
    } else {
      control.setActive(false);
    }
  }
}



function unselectFeature(feature) {

  if (feature.layer != null) {
    console.log("unselecting ", feature)
    controls.none.unselect(feature);
  }
}

// /*********************************************************************************************************************/
// /**
//  * Purpose: dot functions that bind the metadata to the feature object
//  * @params  the feature and it's metadata
//  * @event creates variables attached to the feature object and store the values
//  */
// /*********************************************************************************************************************/

function onFeatureAdded() {
  laneMarkers.getSource().clear();

  lanes.getSource().getFeatures().forEach(function (laneFeature, i) {
    var coords = laneFeature.getGeometry().getCoordinates();
    if (!laneFeature.get('elevation')) laneFeature.set('elevation', []);
    if (!laneFeature.get('laneWidth')) laneFeature.set('laneWidth', []);

    var elevation = laneFeature.get('elevation');
    var laneWidth = laneFeature.get('laneWidth');
    var nodeElevations = elevation.slice();
    var nodeWidths = laneWidth.slice();

    var added = (typeof laneWidth[coords.length - 1] === 'undefined');

    coords.forEach(function (coord, j) {
      var dot = new ol.Feature(new ol.geom.Point(coord));
      var latlon = ol.proj.transform(coord, toProjection, fromProjection);

      if (typeof laneWidth[j] === 'undefined') {
        laneWidth[j] = 0;
      }

      if (typeof elevation[j] === 'undefined') {
        elevation[j] = { value: -9999, edited: false, latlon: { lat: latlon[1], lon: latlon[0] } };
      }

      var found = false;
      for (var k = 0; k < nodeElevations.length; k++) {
        var el = nodeElevations[k];
        if (!el || !el.latlon) continue;

        var latMatch = el.latlon.lat.toFixed(8) === latlon[1].toFixed(8);
        var lonMatch = el.latlon.lon.toFixed(8) === latlon[0].toFixed(8);
        if (latMatch && lonMatch) {
          laneWidth[j] = nodeWidths[k];
          if (el.edited) {
            elevation[j] = el;
            buildLaneDots(i, j, dot, latlon);
          }
          found = true;
          break;
        }
      }

      if (!elevation[j].edited || !found) {
        getElevation(dot, latlon, i, j, function (elev, i, j, latlon, dot) {
          lanes.getSource().getFeatures()[i].get('elevation')[j] = {
            value: elev,
            edited: true,
            latlon: { lat: latlon[1], lon: latlon[0] }
          };
          buildLaneDots(i, j, dot, latlon);
        });
      }
    });
  });

  if (laneWidths.getSource().getFeatures().length !== 0) {
    laneWidths.getSource().clear();
    toggleWidthArray();
  }

  polyMarkers.getSource().clear();
  radiuslayer.getSource().clear();

  polygons.getSource().getFeatures().forEach(function (polyFeature, i) {
    var geom = polyFeature.getGeometry();
    var coords = geom.getCoordinates()[0]; // exterior ring

    if (!polyFeature.get('elevation')) polyFeature.set('elevation', []);
    var elevation = polyFeature.get('elevation');
    var nodeElevations = elevation.slice();

    if (polyFeature.get('title') === 'circle'){
      var extent = geom.getExtent();
      var startX = (extent[0] + extent[2]) / 2;
      var startY = (extent[1] + extent[3]) / 2;
      var center = [startX, startY];
      var centerDot = new ol.Feature(new ol.geom.Point(center));
      var centerLatLon = ol.proj.transform(center, toProjection, fromProjection);

      getElevation(centerDot, centerLatLon, i, 101, function (elev, i, j, latlon, dot) {
        polygons.getSource().getFeatures()[i].get('elevation')[j] = {
          value: elev,
          edited: true,
          latlon: { lat: latlon[1], lon: latlon[0] }
        };
        buildPolyDots(i, j, dot, latlon, "circle");
      });

      var endPoint = [extent[2], startY];
      var radiusLine = new ol.geom.LineString([center, endPoint]);

      var radius = polyFeature.get('radius');
      var len = radius || ol.sphere.getLength(radiusLine);
      $('#radius').val(len);

      var radiusFeature = new ol.Feature(radiusLine);
      radiusFeature.setStyle(new ol.style.Style({
        stroke: new ol.style.Stroke({
          color: '#0500bd',
          width: 3
        }),
        text: new ol.style.Text({
          text: len.toFixed(1) + " m",
          offsetX: 20,
          offsetY: 10
        })
      }));
      radiuslayer.getSource().addFeature(radiusFeature);

      polyFeature.set('title', 'circle');
      coords = []; // skip rest
    }

    $('#editPoly').prop('disabled', polyFeature.get('title') === 'circle');

    coords.forEach(function (coord, j) {
      var dot = new ol.Feature(new ol.geom.Point(coord));
      var latlon = ol.proj.transform(coord, toProjection, fromProjection);

      if (typeof elevation[j] === 'undefined') {
        elevation[j] = { value: -9999, edited: false, latlon: { lat: latlon[1], lon: latlon[0] } };
      }

      var found = false;
      for (var k = 0; k < nodeElevations.length; k++) {
        var el = nodeElevations[k];
        if (!el || !el.latlon) continue;

        var latMatch = el.latlon.lat.toFixed(8) === latlon[1].toFixed(8);
        var lonMatch = el.latlon.lon.toFixed(8) === latlon[0].toFixed(8);
        if (latMatch && lonMatch && el.edited) {
          elevation[j] = el;
          buildPolyDots(i, j, dot, latlon);
          found = true;
          break;
        }
      }

      if (!elevation[j].edited || !found) {
        getElevation(dot, latlon, i, j, function (elev, i, j, latlon, dot) {
          polygons.getSource().getFeatures()[i].get('elevation')[j] = {
            value: elev,
            edited: true,
            latlon: { lat: latlon[1], lon: latlon[0] }
          };
          buildPolyDots(i, j, dot, latlon);
        });
      }
    });
  });
}


function buildLaneDots(i, j, dot, latlon) {
  dot.setProperties({
    lane: i,
    number: j,
    LatLon: latlon,
    laneNumber: lanes.getSource().getFeatures()[i].get('laneNumber'),
    laneWidth: lanes.getSource().getFeatures()[i].get('laneWidth'),
    extent: lanes.getSource().getFeatures()[i].get('extent'),
    elevation: lanes.getSource().getFeatures()[i].get('elevation')[j]
  });
  laneMarkers.getSource().addFeature(dot);
}

function buildPolyDots(i, j, dot, latlon, title) {
  dot.setProperties({
    area: i,
    number: j,
    LatLon: latlon,
    elevation: polygons.getSource().getFeatures()[i].get('elevation')[j],
    title: title
  });
  polyMarkers.getSource().addFeature(dot);
}



// /*********************************************************************************************************************/
// /**
//  * Purpose: drag handler for the vector layer
//  * @params  the feature and it's metadata
//  * @event creates variables attached to the feature object and store the values
//  */

function dragHandler() {
  // Create a select interaction
  var select = new ol.interaction.Select({
    layers: [vectors],
    toggleCondition: ol.events.condition.singleClick
  });

  // Create a translate (drag) interaction
  var translate = new ol.interaction.Translate({
    features: select.getFeatures(),
  });

  // Store selected feature when translation starts
  translate.on('translatestart', function (evt) {
    selected_marker = evt.features.item(0);
  });

  // Update location after translation completes
  translate.on('translateend', function (evt) {
    var draggedFeature = evt.features.item(0);
    console.log("dragged:", draggedFeature);
    updateFeatureLocation(draggedFeature);
  });

  // Return both interactions as a group
  return [select, translate];
}

// /*********************************************************************************************************************/
// /**
//  * Purpose: creates sidebar element for the individual roadsigns
//  * @params  the feature and it's metadata
//  * @event loads the sidebar and all of the metadata into the forms
//  */

function referencePointWindow(feature) {
  $("#attributes").hide();
  $(".selection-panel").text(feature.get('marker').name + ' Sign');
  $(".marker-info-tab a").text("Marker Info");
  selected_marker_limit = feature.get('marker').limit;

  $("#lat").prop('readonly', false);
  $("#long").prop('readonly', false);
  $("#elev").prop('readonly', false);

  $('#attribute-tabs li').removeClass('active');
  $('#itis-tab').removeClass('active');
  $('#direction-tab').removeClass('active');
  $('#content-tab').removeClass('active');
  $('#road-condition-tab').removeClass('active');
  $('#marker-info-tab').addClass('active');

  $('.radius').hide();
  $(".lane_width").hide();
  $(".verified_lat").hide();
  $(".verified_long").hide();
  $(".verified_elev").hide();
  $(".extent").hide();
  $(".nwlat").hide();
  $(".nwlong").hide();
  $(".selat").hide();
  $(".selong").hide();
  $(".regionFeatures br").hide();
  $('.ssp_loc_rights').hide();

  $(".start_time").show();
  $(".end_time").show();
  $(".lat").show();
  $(".long").show();
  $(".intersection").show();
  $(".elev").show();
  $(".info-type").show();
  $('.direction-tab').show();
  $('.content-tab').show();
  $('.itis-tab').show();
  $(".speed_limit").show();
  $(".master_lane_width").show();
  $('.ssp_tim_rights').show();
  $('.ssp_type_rights').show();
  $('.ssp_content_rights').show();
  $('.ssp_loc_rights').show();
  $(".road_condition").show();
  $(".road_surface").show();

  if (feature.get('marker').name === "Verified Point Marker") {
    $(".selection-panel").text('Verified Point Configuration');
    $(".start_time").hide();
    $(".end_time").hide();
    $(".intersection").hide();
    $(".lane_width").hide();
    $(".info-type").hide();
    $('.direction-tab').hide();
    $('.content-tab').hide();
    $('.itis-tab').hide();
    $(".speed_limit").hide();
    $(".master_lane_width").hide();
    $('.ssp_tim_rights').hide();
    $('.ssp_loc_rights').hide();
    $('.road_condition').hide();
    $('.road_surface').hide();

    $("#lat").prop('readonly', true);
    $("#long").prop('readonly', true);
    $("#elev").prop('readonly', true);
    $(".verified_lat").show();
    $(".verified_long").show();
    $(".verified_elev").show();
  }

  if (selected_marker.get('verifiedElev')) {
    $("#verified_elev").val(selected_marker.get('verifiedElev'));
  }

  $('#elev').val(selected_marker.get('elevation') || '');
  $('#ssp_loc_rights').val(selected_marker.get('sspLocationRights') || '');
  $('#master_lane_width').val(selected_marker.get('masterLaneWidth') || '366');
  $('#start_time input').val(selected_marker.get('startTime') || '');
  $('#end_time input').val(selected_marker.get('endTime') || '');

  const content = selected_marker.get('content');
  if (!content) {
    removeITISForm();
    addITISForm();
  } else {
    rebuildITISForm(content);
  }

  $('#ssp_type_rights').val(selected_marker.get('sspTypeRights') || '');
  $('#ssp_content_rights').val(selected_marker.get('sspContentRights') || '');
  $('#ssp_tim_rights').val(selected_marker.get('sspTimRights') || '');

  const priority = selected_marker.get('priority');
  $('#priority .dropdown-toggle').html((priority || "Select A Priority") + " <span class='caret'></span>");

  const mutcd = selected_marker.get('mutcd');
  $('#mutcd .dropdown-toggle').html((mutcd || "Select A MUTCD Code") + " <span class='caret'></span>");

  const direction = selected_marker.get('direction');
  $('#direction .dropdown-toggle').html((direction || "Select A Direction") + " <span class='caret'></span>");

  const infoType = selected_marker.get('infoType');
  $('#info-type .dropdown-toggle').html((infoType || "Select A Type") + " <span class='caret'></span>");

  const roadCondition = selected_marker.get('road_condition');
  $('#road-condition .dropdown-toggle').html((roadCondition || "Select A Condition") + " <span class='caret'></span>");

  if (selected_marker.get('heading')) {
    drawCircleSlices(selected_marker.get('heading'));
  }

  map.getLayers().forEach(layer => {
    if (layer instanceof ol.layer.Vector && layer.getSource().hasFeature(feature)) {
      selected_layer = layer;
    }
  });

  $("#attributes").show();
}



// /**
//  * Purpose: if lat/long is modified, it changes the location
//  * @params  the feature and it's metadata
//  * @event changes the location on the map by redrawing
//  */

function updateFeatureLocation(feature) {
  referencePointWindow(feature);

  var coord = feature.getGeometry().getCoordinates(); // [x, y]
  var lonLat = ol.proj.transform(coord, toProjection, fromProjection); // [lon, lat]

  feature.set('LonLat', { lon: lonLat[0], lat: lonLat[1] });

  $('#long').val(lonLat[0]);
  $('#lat').val(lonLat[1]);

  populateRefWindow(feature, lonLat[1], lonLat[0]);
}


function updateNonReferenceFeatureLocation(feature) {
  var coord = feature.getGeometry().getCoordinates(); // [x, y]
  var lonLat = ol.proj.transform(coord, toProjection, fromProjection); // [lon, lat]

  feature.set('LonLat', { lon: lonLat[0], lat: lonLat[1] });

  $('#long').val(lonLat[0]);
  $('#lat').val(lonLat[1]);

  populateRefWindow(feature, lonLat[1], lonLat[0]);
}



// /**
//  * Purpose: populate reference point modal window
//  * @params  the feature and it's metadata
//  * @event loads the appropriate data - elevation is doen through ajax
//  */

function populateAttributeWindow(temp_lat, temp_lon){
	$('#lat').val(temp_lat);
	$('#long').val(temp_lon);
}

async function populateRefWindow(feature, lat, lon) {
  // getNearestIntersectionJSON(feature, lat, lon);
  let elev = -9999;
  if (!feature.get("elevation")) {
    elev = await getElev(lat, lon);
    if (!feature.get("elevation")?.value) {
      $('#elev').val(elev);
    }
  }

  if (feature.get("verifiedElev")) {
    $('#verified_elev').val(feature.get("verifiedElev"));
  } else {
    //If verified elevation does not exist in feature, update it with new elevation value
    $('#verified_elev').val(elev);
  }

  if (feature.get("verifiedLat")) {
    $('#verified_lat').val(feature.get("verifiedLat"));
  } else {
    $('#verified_lat').val(lat);
  }

  if (feature.get("verifiedLon")) {
    $('#verified_long').val(feature.get("verifiedLon"));
  } else {
    $('#verified_long').val(lon);
  }
}

// /*********************************************************************************************************************/
// /**
//  * Purpose: validate the data and save the data to the feature
//  * @params  the sidebar form elements
//  * @event validates all the visible data using parsley js. If it is not accepted, it turns the form locations
//  * with issues red, otherwise, it allows the data object to be created and saved to the feature
//  */

$(".btnDone").click(function () {
  $('#attributes').parsley().validate();
  let error_count = 0;
  const code_check = /^(n((1-((4|8|10|16)th|3rd|2)|3-4)|[1-9]\d*)|\d+)$/;
  let itis_check = false;

  $('.itis_code_list').each(function (i, obj) {
    const val = $(this).val();
    if (val !== null) {
      $.each(val, function (j, code) {
        if (!code_check.test(code)) {
          error_count++;
          $('.select2-selection').css('border-color', 'red');
          itis_check = true;
        }
      });
    }
  });

  if (!selected_marker.get('marker')) {
    error_count += $(".parsley-errors-list li:visible").length;
  } else {
    if (selected_marker.get('marker').type === "TIM") {
      error_count += $("#marker-info-tab .row:not([style='display: none;']) .parsley-errors-list li").length
        + $("#content-tab .parsley-errors-list li").length
        + $("#direction-tab .parsley-errors-list li").length
        + $("#itis-tab .parsley-errors-list li").length;
        + $("#road-condition-tab .parsley-errors-list li").length;
    } else {
      error_count += $(".parsley-errors-list li:visible").length;
    }
  }

  if (error_count === 0) {
    $("#attributes").hide();
    $('.select2-selection').css('border-color', '');
    content = [];

    $('.itis_code_list').each(function (i, obj) {
      content[i] = new createElement($(this).val(), $('#itisForm_' + i + '_itis_text').val());
    });

    const move = ol.proj.transform(
      [parseFloat($('#long').val()), parseFloat($('#lat').val())],
      fromProjection,
      toProjection
    );

    const layerName = selected_layer.get('name');

    if (layerName === "Lane Marker Layer") {
      const vert = lanes.getSource().getFeatures()[selected_marker.get('lane')]
        .getGeometry().getCoordinates()[selected_marker.get('number')];
      // You need to update geometry manually since OL10 doesn't allow `.move`
      const newCoord = move;
      selected_marker.getGeometry().setCoordinates(newCoord);

      // Update attributes
      selected_marker.set('LatLon', [parseFloat($('#long').val()), parseFloat($('#lat').val())]);
      selected_marker.set('elevation', $('#elev').val());

      const feature = lanes.getSource().getFeatures()[selected_marker.get('lane')];
      let elevation = feature.get('elevation') || [];
      elevation[selected_marker.get('number')] = {
        value: $("#elev").val(),
        edited: true
      };
      feature.set('elevation', elevation);

      nodeLaneWidth[selected_marker.get('number')] = $("#lane_width").val();
      feature.set('laneWidth', nodeLaneWidth);
      nodeLaneWidth = [];
    }

    if (layerName === "Poly Marker Layer") {
      const polyFeature = polygons.getSource().getFeatures()[selected_marker.get('area')];

      if (polyFeature.get('title') === "circle") {
        const user_move = move;
        const change_z = polyFeature.get('elevation');
        const radiusVal = parseFloat($('#radius').val());

        polygons.getSource().clear();

        const temp_proj = ol.proj.transform(user_move, toProjection, fromProjection);
        const radius = radiusVal * (1 / Math.cos(temp_proj[1] * Math.PI / 180)) * 0.999769942386;

        const circleGeom = ol.geom.Polygon.circular(ol.proj.get(toProjection), user_move, radius, 32);
        const newCircle = new ol.Feature(circleGeom);

        newCircle.set('title', 'circle');
        newCircle.set('elevation', change_z);
        newCircle.set('radius', radiusVal);

        polygons.getSource().addFeature(newCircle);
      } else {
        selected_marker.getGeometry().setCoordinates(move);
        selected_marker.set('LatLon', [parseFloat($('#long').val()), parseFloat($('#lat').val())]);
        selected_marker.set('elevation', $('#elev').val());

        const elevation = polyFeature.get('elevation') || [];
        elevation[selected_marker.get('number')] = {
          value: $("#elev").val(),
          edited: true
        };
        polyFeature.set('elevation', elevation);
      }
    }

    if (layerName === "Vector Layer") {
      selected_marker.getGeometry().setCoordinates(move);

      const attrs = selected_marker.get('marker');
      if (attrs.name === "Verified Point Marker") {
        selected_marker.set('verifiedLat', $("#verified_lat").val());
        selected_marker.set('verifiedLon', $("#verified_long").val());
        selected_marker.set('verifiedElev', $("#verified_elev").val());
      } else {
        selected_marker.set('startTime', $("#start_time input").val());
        selected_marker.set('endTime', $("#end_time input").val());
        selected_marker.set('packetID', $("#packet_id").val());
        selected_marker.set('content', content);
        selected_marker.set('elevation', $("#elev").val());
        selected_marker.set('masterLaneWidth', $("#master_lane_width").val());
        selected_marker.set('sspTimRights', $("#ssp_tim_rights").val());
        selected_marker.set('sspTypeRights', $("#ssp_type_rights").val());
        selected_marker.set('sspContentRights', $("#ssp_content_rights").val());
        selected_marker.set('sspLocationRights', $("#ssp_loc_rights").val());
        selected_marker.set('mutcd', mutcd);
        selected_marker.set('infoType', info_type);
        selected_marker.set('priority', priority);
        selected_marker.set('direction', direction || '');
        selected_marker.set('heading', JSON.parse(JSON.stringify(circles)));
        selected_marker.set('road_condition', $("#road_condition").val());
        selected_marker.set('road_surface', $("#road_surface").val());
      }
    }

    $('#attributes').parsley().reset();
    unselectFeature(selected_marker);
  } else {
    if ($("#marker-info-tab .row:not([style='display: none;']) .parsley-errors-list li").length > 0) {
      $('#marker-info-tab').addClass('active');
      $('#itis-tab, #direction-tab, #content-tab, #road-condition-tab').removeClass('active');
    } else if ($("#content-tab .parsley-errors-list li").length > 0) {
      $('#content-tab').addClass('active');
      $('#marker-info-tab, #direction-tab, #itis-tab, #road-condition-tab').removeClass('active');
    } else if ($("#direction-tab .parsley-errors-list li").length > 0) {
      $('#direction-tab').addClass('active');
      $('#marker-info-tab, #content-tab, #itis-tab, #road-condition-tab').removeClass('active');
    } else if ($("#itis-tab .parsley-errors-list li").length > 0 || itis_check) {
      $('#itis-tab').addClass('active');
      $('#marker-info-tab, #content-tab, #direction-tab, #road-condition-tab').removeClass('active');
    } else if ($("#road-condition-tab .parsley-errors-list li").length > 0) {
      $('#road-condition-tab').addClass('active');
      $('#marker-info-tab, #content-tab, #direction-tab, #itis-tab').removeClass('active');
    }
  }

  onFeatureAdded();
});



// /*********************************************************************************************************************/
// /**
//  * Purpose: if cancel - prevents data from being stored
//  * @params  the sidebar form elements
//  * @event removes all form data and clears any temp objects that may be housing data so that next load can start clean
//  * from the feature object
//  */

$(".btnClose").click(function () {
  $("#attributes").hide();

  $('.itis_code_list').each(function (i, obj) {
    $('#itisForm_' + i + '_itis_text').val("");
    $('#itisForm_' + i + '_itis_codes').empty();
  });
  removeITISForm();
  $('.select2-selection').css('border-color', '');

  ctx.clearRect(0, 0, 300, 300);

  for (d = 0; d < 360; (d = d + 22.5)) {
    drawCircle(ctx, cx, cy, cr, "black", "white", d, circles_reset);
  }

  circles = JSON.parse(JSON.stringify(circles_temp));

  drawCircleSlices(circles);
  circles_reset = [];
  nodeLaneWidth = [];

  $('#attributes').parsley().reset();
  unselectFeature(selected_marker);

});

// /*********************************************************************************************************************/
// /**
//  * Purpose: misc. functions that allow specific data to be visible a certain way
//  * @param {Array|string|null} code - ITIS code(s) or null.
//  * @param {string} value - Description text for the code(s).
//  * @event createElement is for select2 itis codes box, drawCircleSlices is used with the heading,
//  * other options allow time to be chosen, or for data to parse ul/li select box text into text and number
//  *
//  * Note: the ul/li select boxes should one da become select boxes with options, but the styling was hard to replicate
//  * at first.
//  */

function createElement(code, value) {
  if (code === null) {
    this.codes = []
  } else {
    this.codes = code
  }
  this.text = value
}

function drawCircleSlices(circles) {

  for (var i = 0; i < circles.length; i++) {
    if (circles[i].active) {
      ctx.beginPath();
      ctx.moveTo(circles[i].x1, circles[i].y1);
      ctx.arc(circles[i].x1, circles[i].y1, cr, circles[i].theta, circles[i].nxtTheta);
      ctx.lineTo(circles[i].x1, circles[i].y1);
      ctx.closePath();
      ctx.fillStyle = "LightSkyBlue";
      ctx.fill();
      ctx.strokeStyle = "black";
      ctx.stroke();
    }

    ctx.beginPath();
    ctx.arc((cr * (Math.cos(circles[i].theta))) + circles[i].x1, (cr * (Math.sin(circles[i].theta))) + circles[i].y1, 4, 0, 2 * Math.PI);
    ctx.fillStyle = "blue";
    ctx.fill();
    ctx.closePath();

    ctx.beginPath();
    ctx.arc((cr * (Math.cos(circles[i].nxtTheta))) + circles[i].x1, (cr * (Math.sin(circles[i].nxtTheta))) + circles[i].y1, 4, 0, 2 * Math.PI);
    ctx.fillStyle = "blue";
    ctx.fill();
    ctx.closePath();

  }

}

$(function () {
  $('#start_time').datetimepicker({
    format: 'MM/DD/YYYY LT'
  });
  $('#end_time').datetimepicker({
    format: 'MM/DD/YYYY LT'
  });
});

$(document).ready(function () {
  $('input[type=radio][name=sign-type]').change(function () {
    console.log(this.value);
  });
});

$(".dropdown-menu li a").click(function () {
  var selText = $(this).text();
  $(this).parents('.btn-group').find('.dropdown-toggle').html(selText + ' <span class="caret"></span>');

  var type = $(this).parents('.btn-group').attr('id');

  if (type == "mutcd") {
    mutcd = selText;
    changePriority(mutcd);
  }

  if (type == "priority") {
    priority = selText;
  }

  if (type == "direction") {
    direction = selText;
  }

  if (type == "extent") {
    extent = selText;
  }

  if (type == "info-type") {
    info_type = selText;
  }

  if (type == "time") {
    ttl = selText;
  }
});


function changePriority(mutcd) {

  var change = true;

  switch (mutcd.substring(1, 2)) {
    case '0':
      priority = 0
      break;
    case '1':
      priority = 6
      break;
    case '2':
      priority = 5
      break;
    case '3':
      priority = 4
      break;
    case '4':
      priority = 3
      break;
    case '5':
      priority = 2
      break;
    case '6':
      priority = 1
      break;
    default:
      change = false;
  }

  if (change) {
    $('#priority .dropdown-toggle').html(priority + " <span class='caret'></span>");
  }
}



export function toggleWidthArray() {
  const laneFeatures = lanes.getSource().getFeatures();
  const vectorFeatures = vectors.getSource().getFeatures();
  const laneWidthSource = laneWidths.getSource();
   var isNegative = { value: false, node: "", lane: "" };

  if (laneWidthSource.getFeatures().length === 0) {
    let masterWidth;

    for (let f = 0; f < vectorFeatures.length; f++) {
      const attr = vectorFeatures[f].getProperties();
      if (attr.marker?.type === "TIM") {
        masterWidth = parseFloat(attr.masterLaneWidth);
      }
    }

   

    for (let i = 0; i < laneFeatures.length; i++) {
      const lane = laneFeatures[i];
      const coords = lane.getGeometry().getCoordinates();
      const props = lane.getProperties();

      let widthList = [];
      let widthDeltaTotal = 0;
      let flipped = false;

      for (let j = 0; j < coords.length; j++) {
        let point1, point2;

        // Skip duplicate points
        if (
          j < coords.length - 1 &&
          coords[j][0] === coords[j + 1][0] &&
          coords[j][1] === coords[j + 1][1]
        ) {
          j++;
        }

        if (j < coords.length - 1) {
          point1 = ol.proj.transform(coords[j], toProjection, fromProjection);
          point2 = ol.proj.transform(coords[j + 1], toProjection, fromProjection);
        } else {
          point1 = ol.proj.transform(coords[j], toProjection, fromProjection);
          if (
            coords[j][0] === coords[j - 1][0] &&
            coords[j][1] === coords[j - 1][1]
          ) {
            point2 = ol.proj.transform(coords[j - 2], toProjection, fromProjection);
            flipped = true;
          } else {
            point2 = ol.proj.transform(coords[j - 1], toProjection, fromProjection);
          }
        }

        let widthDelta = parseFloat(props.laneWidth?.[j]);
        if (isNaN(widthDelta)) widthDelta = 0;

        widthDeltaTotal += widthDelta;

        if (masterWidth + widthDeltaTotal < 0) {
          console.log(masterWidth + widthDeltaTotal);
          isNegative = { value: true, node: j + 1, lane: i };
          widthDeltaTotal = -masterWidth;
        }

        const inverse = inverseVincenty(point1[1], point1[0], point2[1], point2[0]);
        const direct1 = directVincenty(point1[1], point1[0], inverse.bearing + 90, ((masterWidth + widthDeltaTotal) / 2) / 100);
        const direct2 = directVincenty(point1[1], point1[0], inverse.bearing - 90, ((masterWidth + widthDeltaTotal) / 2) / 100);

        const newPoint1 = ol.proj.transform([direct1.lon, direct1.lat], fromProjection, toProjection);
        const newPoint2 = ol.proj.transform([direct2.lon, direct2.lat], fromProjection, toProjection);

        if (j === coords.length - 1) j++;

        if (isOdd(j) && !flipped) {
          widthList.push(newPoint1, newPoint2);
          const ring = new ol.geom.LinearRing(widthList);
          const polygon = new ol.geom.Polygon([ring.getCoordinates()]);
          laneWidthSource.addFeature(new ol.Feature({ geometry: polygon }));

          widthList = [newPoint1, newPoint2];
        } else {
          widthList.push(newPoint2, newPoint1);
          const ring = new ol.geom.LinearRing(widthList);
          const polygon = new ol.geom.Polygon([ring.getCoordinates()]);
          laneWidthSource.addFeature(new ol.Feature({ geometry: polygon }));

          widthList = [newPoint2, newPoint1];
          flipped = false;
        }
      }
    }
  } else {
    laneWidthSource.clear();
  }

  if (isNegative.value) {
    const laneNum = laneFeatures[isNegative.lane].get("laneNumber");
    alert(`Width deltas sum to less than zero on lane ${laneNum} at node ${isNegative.node}!`);
  }
}

function isOdd(num) { return (num % 2) == 1;}


function measureCallback(event) {
  const geometry = event.feature.getGeometry();
  geometry.on('change', (event) => {
    const length = getLength(geometry);
    $('.measurement').text(length.toFixed(3) + ' m');
    copyTextToClipboard((length).toFixed(3));
  });
}
function getLength(geometry) {
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
function setContent(val) {
  content = val;
}
export function setCirclesTemp(newCirclesTemp) {
  if (Array.isArray(newCirclesTemp)) {
    circles_temp = newCirclesTemp;
  } else {
    console.error("setCirclesTemp expects an array.");
  }
}

export function setFeatureAttributes(feature) {
  mutcd = feature.get('mutcd');
  priority = feature.get('priority');
  direction = feature.get('direction');
  info_type = feature.get('infoType');
  content = feature.get('content');
}

function showMarkers(laneFeature, laneMarkers) {
  const coordinates = laneFeature.getGeometry().getCoordinates();

  for (let i = 0; i < coordinates.length; i++) {
    // Add full opacity markers at main coordinates
    const pointFeature = new ol.Feature(new ol.geom.Point(coordinates[i]));
    pointFeature.setStyle(laneStyle);
    laneMarkers.getSource().addFeature(pointFeature);
  }
}
$(document).ready(() => {
  
  getCSRFToken().then(token => {
    init();
    registerMapEvents();
    registerDrawInteractions();
    initTopNavBar();
  });
});

export {
  toggleControlsOn,
  drawCircleSlices,
  map,
  lanes,
  setContent,
  radiuslayer,
  circles_reset,
  vectors,
  laneMarkers,
  laneWidths,
  box,
  area,
  trace,
  polyMarkers,
  polygons,
  selected_marker_limit,
  fromProjection,
  circle_bounds,
  toProjection
};