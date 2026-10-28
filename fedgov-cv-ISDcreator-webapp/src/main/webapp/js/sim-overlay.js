/*
 * Copyright (C) 2026 LEIDOS.
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

/**
 * Sim Overlay Module
 *
 * "Sim mode" lets a simulator map (a Lanelet2 OSM file whose origin is lat/lon 0,0)
 * be drawn on at a display anchor instead of at 0,0. The anchor is the map center
 * when the OSM is first loaded. Everything is related through local meters (E, N)
 * from the sim origin:
 *
 *   sim lat/lon  <->  (E, N)  <->  display lat/lon at the anchor  <->  EPSG:3857
 *
 * While sim mode is active, the encoder converts drawn coordinates back to sim
 * lat/lon (displayToSimLonLat) and elevation lookups return the sim ground z.
 */

import {
    displayLonLatToLocal, localToDisplayLonLat, localToSimLonLat, simLonLatToLocal
} from './sim-geo.js';

// Kept clear of the drawing palette in style.js (orange, red, yellow, cyan, blue).
const LANE_COLOR = '#C9A7EB';
const LANE_WIDTH = 1.5;
const LANE_DASH = [6, 6];
// Black background under the lanes; padded by this fraction of the lanes' larger side.
const BACKGROUND_MARGIN_FRACTION = 0.05;

let getMap = null;
let options = {};

// anchor is set when sim mode starts (current map center) or restored from a saved map file.
const sim = { active: false, anchor: null, groundZ: 0 };
let laneLayer = null;
let osmFilename = null;
let backgroundLayer = null;

// Coordinate conversions (the math lives in sim-geo.js)

function localToMapCoord(local, anchor) {
    return ol.proj.fromLonLat(localToDisplayLonLat(local, anchor));
}

/**
 * Convert a displayed [lon, lat] to the sim [lon, lat] that should be encoded.
 * Returns the input unchanged when sim mode is off.
 */
export function displayToSimLonLat(lonLat) {
    if (!sim.active) return lonLat;
    return localToSimLonLat(displayLonLatToLocal(lonLat, sim.anchor));
}

/**
 * Same as displayToSimLonLat for separate lat/lon values (e.g. typed form fields).
 * Values that are not numbers are returned unchanged.
 */
export function displayToSimLatLon(lat, lon) {
    if (!sim.active) return { lat, lon };
    const latNum = Number.parseFloat(lat);
    const lonNum = Number.parseFloat(lon);
    if (!Number.isFinite(latNum) || !Number.isFinite(lonNum)) return { lat, lon };
    const [simLon, simLat] = displayToSimLonLat([lonNum, latNum]);
    return { lat: simLat, lon: simLon };
}

export function isSimMode() {
    return sim.active;
}

export function getSimGroundZ() {
    return Math.round(sim.groundZ * 100) / 100;
}

/**
 * Set the sim ground z and apply it to every existing lane node and marker elevation,
 * so the ground plane stays flat and the encoded elevation deltas stay 0.
 */
export function setSimGroundZ(z) {
    sim.groundZ = z;
    const features = options.getElevationFeatures?.();
    if (!features) return;
    const groundZ = getSimGroundZ();
    for (const lane of features.lanes || []) {
        // Computed lanes take their elevation from the source lane plus offsetZ.
        if (lane.get('computed')) continue;
        // Update the node objects in place; the lane marker dots share them.
        for (const node of lane.get('elevation') || []) {
            if (node) node.value = groundZ;
        }
    }
    for (const point of features.points || []) {
        if (point.get('elevation') !== undefined) point.set('elevation', groundZ);
        if (point.get('verifiedElev') !== undefined) point.set('verifiedElev', groundZ);
    }
}

/** State saved with the map file, or null when sim mode is off. */
export function getSimModeState() {
    return sim.active ? { anchor: { ...sim.anchor }, groundZ: sim.groundZ } : null;
}

/** Re-enter sim mode from a saved map file so its features encode correctly. */
export function restoreSimMode(state) {
    const anchor = state?.anchor;
    if (!isValidLatLon(anchor)) return;
    // Loaded sim lanes follow the file's origin so they line up with its features.
    moveAnchor(anchor);
    if (Number.isFinite(state.groundZ)) sim.groundZ = state.groundZ;
    sim.active = true;
    refreshPanel();
}

/**
 * Called when a map file without sim state replaces all features. Asks whether to
 * stay in sim mode; if so, the sim origin moves to the file's reference point.
 * @param {{lat:number, lon:number}|null} referenceLatLon - the file's reference point, if any
 */
export function handleLoadWithoutSimState(referenceLatLon) {
    if (!sim.active) return;
    const keep = confirm('This map file has no sim mode data. Stay in sim mode? The sim map origin will be placed '
        + 'at its reference point and its coordinates encoded as sim coordinates. Cancel exits sim mode.');
    if (!keep) {
        clearSimMode();
        return;
    }
    if (isValidLatLon(referenceLatLon)) moveAnchor(referenceLatLon);
}

function isValidLatLon(latLon) {
    return !!latLon && Number.isFinite(latLon.lat) && Number.isFinite(latLon.lon);
}

/** Set the display anchor and move any loaded sim lanes to it. */
function moveAnchor(anchor) {
    sim.anchor = { lat: anchor.lat, lon: anchor.lon };
    if (!laneLayer) return;
    for (const feature of laneLayer.getSource().getFeatures()) {
        const local = feature.get('simLocal');
        feature.setGeometry(new ol.geom.LineString(local.map(p => localToMapCoord(p, sim.anchor))));
    }
    updateBackgroundLayer();
}

// Initialization

/**
 * @param {function():ol.Map} mapGetter - returns the map once it exists
 * @param {Object} [userOptions]
 * @param {function():boolean} [userOptions.hasUserFeatures] - true when the map already has drawn features
 * @param {function():{lanes:ol.Feature[], points:ol.Feature[]}} [userOptions.getElevationFeatures] - lane and
 *   point features whose elevations follow the sim ground z
 */
export function initSimOverlay(mapGetter, userOptions) {
    getMap = mapGetter;
    options = userOptions || {};
    createPanel();

    $('#openSimMap').on('click', function (e) {
        e.preventDefault();
        openSimMapModal();
    });
    $('#exitSimMode').on('click', function (e) {
        e.preventDefault();
        exitSimMode();
    });
    $('#sim-map-load').on('click', function () {
        // loadFromModal reports its own errors in the modal.
        void loadFromModal();
    });
}

function openSimMapModal() {
    $('#sim-osm-file').val('');
    setModalStatus(laneLayer ? 'A sim OSM is already loaded (' + osmFilename + '). Select a new one to replace it.' : '', 'info');
    $('#sim_map_modal').modal('show');
}

function setModalStatus(message, type) {
    const el = document.getElementById('sim-map-status');
    if (!el) return;
    el.textContent = message;
    const alertType = type === 'error' ? 'danger' : 'info';
    el.className = message ? 'alert alert-' + alertType : '';
}

async function loadFromModal() {
    const osmFile = document.getElementById('sim-osm-file').files[0];
    if (!osmFile) {
        setModalStatus('Select the sim OSM file.', 'error');
        return;
    }
    if (!sim.active && options.hasUserFeatures?.()) {
        const ok = confirm('The map already has drawn features. In sim mode they will be encoded as sim coordinates, '
            + 'relative to the sim map. Continue?');
        if (!ok) return;
    }

    try {
        const text = await osmFile.text();
        // Keep an existing anchor so features already drawn in sim mode stay aligned with the lanes.
        const anchor = sim.anchor || currentMapCenter();
        // Parse before touching the map so a bad file leaves it unchanged.
        const lanes = parseOsm(text, anchor);

        sim.anchor = anchor;
        sim.active = true;
        setLaneLayer(lanes, osmFile.name);
        refreshPanel();
        getMap().getView().fit(laneLayer.getSource().getExtent(), { padding: [50, 50, 50, 50], maxZoom: 20 });
        $('#sim_map_modal').modal('hide');
    } catch (err) {
        console.error('Sim overlay:', err);
        setModalStatus(err.message, 'error');
    }
}

function currentMapCenter() {
    const [lon, lat] = ol.proj.toLonLat(getMap().getView().getCenter());
    return { lat: lat, lon: lon };
}

function exitSimMode() {
    if (!sim.active) return;
    const ok = confirm('Exit sim mode? Features already drawn will be encoded with their displayed coordinates '
        + 'instead of sim coordinates.');
    if (!ok) return;
    clearSimMode();
}

/** Leave sim mode without asking. */
export function clearSimMode() {
    if (!sim.active) return;
    removeSimLayers();
    sim.active = false;
    // The next sim map is anchored at the map center at that time.
    sim.anchor = null;
    sim.groundZ = 0;
    refreshPanel();
}

function removeSimLayers() {
    if (laneLayer) {
        getMap().removeLayer(laneLayer);
        laneLayer = null;
        osmFilename = null;
    }
    updateBackgroundLayer();
}

// OSM lanes

/** Parse Lanelet2 OSM XML into LineString features (one per way) in map coordinates. */
function parseOsm(text, anchor) {
    const doc = new DOMParser().parseFromString(text, 'application/xml');
    if (doc.getElementsByTagName('parsererror').length > 0 || doc.documentElement.nodeName !== 'osm') {
        throw new Error('The selected file is not a valid OSM XML file.');
    }

    const nodes = parseOsmNodes(doc);
    const features = [];
    for (const way of doc.getElementsByTagName('way')) {
        const feature = osmWayToFeature(way, nodes, anchor);
        if (feature) features.push(feature);
    }
    if (features.length === 0) {
        throw new Error('The OSM file contains no ways to draw.');
    }
    return features;
}

/** Map of OSM node id -> local meters [E, N], skipping nodes without a valid lat/lon. */
function parseOsmNodes(doc) {
    const nodes = new Map();
    for (const node of doc.getElementsByTagName('node')) {
        const lat = Number.parseFloat(node.getAttribute('lat'));
        const lon = Number.parseFloat(node.getAttribute('lon'));
        if (Number.isFinite(lat) && Number.isFinite(lon)) {
            nodes.set(node.getAttribute('id'), simLonLatToLocal([lon, lat]));
        }
    }
    return nodes;
}

/**
 * LineString feature for an OSM way, or null when fewer than 2 of its nodes are known.
 * The local meters are kept as simLocal so moveAnchor can re-place the lanes.
 */
function osmWayToFeature(way, nodes, anchor) {
    const local = [];
    for (const nd of way.getElementsByTagName('nd')) {
        const point = nodes.get(nd.getAttribute('ref'));
        if (point) local.push(point);
    }
    if (local.length < 2) return null;
    const tags = {};
    for (const tag of way.getElementsByTagName('tag')) {
        tags[tag.getAttribute('k')] = tag.getAttribute('v');
    }
    return new ol.Feature({
        geometry: new ol.geom.LineString(local.map(p => localToMapCoord(p, anchor))),
        simLocal: local,
        osmId: way.getAttribute('id'),
        type: tags.type,
        subtype: tags.subtype
    });
}

const laneStyles = {
    solid: new ol.style.Style({
        stroke: new ol.style.Stroke({ color: LANE_COLOR, width: LANE_WIDTH })
    }),
    dashed: new ol.style.Style({
        stroke: new ol.style.Stroke({ color: LANE_COLOR, width: LANE_WIDTH, lineDash: LANE_DASH })
    })
};

function setLaneLayer(features, filename) {
    const map = getMap();
    if (laneLayer) map.removeLayer(laneLayer);
    laneLayer = new ol.layer.Vector({
        title: 'Sim OSM Lanes',
        source: new ol.source.Vector({ features: features }),
        style: function (feature) {
            return feature.get('subtype') === 'dashed' ? laneStyles.dashed : laneStyles.solid;
        },
        // Above the sim background (1), below all user-created components (zIndex >= 2).
        zIndex: 1.5
    });
    osmFilename = filename;
    map.addLayer(laneLayer);
    updateBackgroundLayer();
}

// Background

/**
 * Show a black rectangle slightly larger than the sim lanes.
 * It is a stretched 1x1 image rather than a vector polygon so map clicks
 * (forEachFeatureAtPixel without a layer filter) never hit it.
 */
function updateBackgroundLayer() {
    const map = getMap();
    if (backgroundLayer) {
        map.removeLayer(backgroundLayer);
        backgroundLayer = null;
    }
    if (!laneLayer) return;

    const extent = laneLayer.getSource().getExtent();
    const margin = Math.max(ol.extent.getWidth(extent), ol.extent.getHeight(extent)) * BACKGROUND_MARGIN_FRACTION;
    const canvas = document.createElement('canvas');
    canvas.width = canvas.height = 1;
    const ctx = canvas.getContext('2d');
    ctx.fillStyle = '#000';
    ctx.fillRect(0, 0, 1, 1);
    backgroundLayer = new ol.layer.Image({
        title: 'Sim Background',
        source: new ol.source.ImageStatic({
            url: canvas.toDataURL(),
            imageExtent: ol.extent.buffer(extent, margin),
            interpolate: false
        }),
        // Just above the base map (zIndex 0), below the sim lanes and user components.
        zIndex: 1
    });
    map.addLayer(backgroundLayer);
}

// Panel

function createPanel() {
    const panel = document.createElement('div');
    panel.className = 'sim-overlay-panel';
    panel.id = 'sim-overlay-panel';
    panel.style.display = 'none';
    panel.innerHTML =
        '<h6><span class="sim-mode-badge">SIM MODE</span><span id="sim-osm-name" class="sim-overlay-name"></span></h6>'
        + '<div class="sim-overlay-row">'
        + '  <label for="sim-ground-z" title="Elevation of every lane node and marker in sim mode">Ground z (m)</label>'
        + '  <input type="number" id="sim-ground-z" step="0.01">'
        + '</div>'
        + '<p id="sim-no-lanes-hint" class="sim-overlay-hint">Use File &gt; Open Sim Map to show the sim lanes.</p>';

    const mapEl = document.getElementById('map');
    if (mapEl) {
        mapEl.style.position = 'relative';
        mapEl.appendChild(panel);
    }

    document.getElementById('sim-ground-z').addEventListener('change', function () {
        const z = Number.parseFloat(this.value);
        if (Number.isFinite(z)) {
            setSimGroundZ(z);
        } else {
            this.value = sim.groundZ;
        }
    });
}

function refreshPanel() {
    // The File menu's Exit Sim Mode item is only offered while in sim mode.
    $('#exitSimMode').parent().toggle(sim.active);
    const panel = document.getElementById('sim-overlay-panel');
    if (!panel) return;
    panel.style.display = sim.active ? 'block' : 'none';
    if (!sim.active) return;

    const osmName = document.getElementById('sim-osm-name');
    osmName.textContent = osmFilename || '';
    osmName.title = osmFilename || '';
    document.getElementById('sim-no-lanes-hint').style.display = laneLayer ? 'none' : '';
    document.getElementById('sim-ground-z').value = sim.groundZ;
}
