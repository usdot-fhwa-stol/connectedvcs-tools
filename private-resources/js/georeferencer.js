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

/**
 * Georeferencer Module
 *
 * Self-contained ES module that provides image georeferencing functionality
 * for OpenLayers-based map applications. Manages the full workflow: image upload,
 * GCP picking, backend API submission, and overlay display.
 *
 * Usage:
 *   import { initGeoreferencer } from '/private-resources/js/georeferencer.js';
 *   initGeoreferencer(map, { georefEndpoint: '/georef/api/georeference', ... });
 */

let map = null;
let options = {};

let selectedImage = null;
let imageNaturalWidth = 0;
let imageNaturalHeight = 0;
let gcps = [];
let gcpIdCounter = 0;
let addGcpMode = false;
let deleteGcpMode = false;
let pendingMapClick = null; // 'map' | 'image' | null
let gcpMapLayer = null;
let gcpTranslateInteraction = null;
let overlayEntries = []; // { layer, file, gcps, filename, id }
let overlayIdCounter = 0;
let editingOverlayId = null;
let mapClickListenerKey = null;

const DEFAULTS = {
    georefEndpoint: '/georef/api/georeference',
    csrfTokenUrl: '/msp/security/api/csrf-token/',
    minGcps: 4,
    maxGcps: 10
};

// ─── Initialization ──────────────────────────────────────────────────────────

export function initGeoreferencer(olMapOrGetter, userOptions) {
    options = Object.assign({}, DEFAULTS, userOptions);

    function tryInit() {
        var resolvedMap = typeof olMapOrGetter === 'function' ? olMapOrGetter() : olMapOrGetter;
        if (!resolvedMap) {
            return false;
        }
        map = resolvedMap;
        injectModalHTML();
        createGcpMapLayer();
        createOverlayPanel();
        bindEvents();
        fetchBackendConfig();
        return true;
    }

    if (!tryInit()) {
        var attempts = 0;
        var interval = setInterval(function () {
            attempts++;
            if (tryInit() || attempts > 50) {
                clearInterval(interval);
                if (!map) {
                    console.error('Georeferencer: map object not available after waiting');
                }
            }
        }, 100);
    }
}

async function fetchBackendConfig() {
    try {
        const response = await fetch(options.georefEndpoint + '/config');
        if (response.ok) {
            const config = await response.json();
            if (config.gcp) {
                if (typeof config.gcp.minCount === 'number') {
                    options.minGcps = config.gcp.minCount;
                }
                options.maxGcps = config.gcp.maxCount; // null means no limit
            }
        }
    } catch (e) {
        console.warn('Georeferencer: could not fetch backend config, using defaults', e);
    }
}

// ─── Modal HTML Injection ────────────────────────────────────────────────────

function injectModalHTML() {
    const modalHTML = `
    <div class="modal fade georef-modal" id="georef_modal" tabindex="-1" role="dialog"
         data-backdrop="false" data-keyboard="true" aria-labelledby="georef_modal_label" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title" id="georef_modal_label">Georeferencer</h4>
                </div>
                <div class="modal-body">
                    <div class="georef-toolbar">
                        <button type="button" class="btn btn-default btn-sm" id="georef-open-image">
                            <i class="fa fa-folder-open"></i> Open Image
                        </button>
                        <input type="file" id="georef-file-input" accept="image/png,image/jpeg" style="display:none;">
                        <button type="button" class="btn btn-default btn-sm" id="georef-add-gcp" disabled>
                            <i class="fa fa-plus-circle"></i> Add GCP
                        </button>
                        <button type="button" class="btn btn-default btn-sm" id="georef-delete-gcp" disabled>
                            <i class="fa fa-minus-circle"></i> Delete GCP
                        </button>
                        <button type="button" class="btn btn-primary btn-sm" id="georef-start" disabled>
                            <i class="fa fa-play"></i> Start Georeferencing
                        </button>
                    </div>
                    <div class="georef-status" id="georef-status">
                        Open an image to begin.
                    </div>
                    <div class="georef-image-container" id="georef-image-container">
                        <div class="georef-no-image">No image loaded. Click "Open Image" to select a PNG or JPEG file.</div>
                    </div>
                    <div id="georef-table-wrapper">
                        <table class="table table-condensed table-bordered georef-gcp-table" id="georef-gcp-table">
                            <thead>
                                <tr>
                                    <th>Point ID</th>
                                    <th>Image X (px)</th>
                                    <th>Image Y (px)</th>
                                    <th>Longitude</th>
                                    <th>Latitude</th>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody id="georef-gcp-tbody"></tbody>
                        </table>
                    </div>
                </div>
                <div class="modal-footer">
                    <span id="georef-gcp-count" class="pull-left" style="line-height:34px; color:#666;"></span>
                    <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                </div>
            </div>
        </div>
    </div>`;

    document.body.insertAdjacentHTML('beforeend', modalHTML);
}

// ─── GCP Map Layer ───────────────────────────────────────────────────────────

function createGcpMapLayer() {
    const gcpSource = new ol.source.Vector();

    gcpMapLayer = new ol.layer.Vector({
        source: gcpSource,
        style: gcpMarkerStyle,
        zIndex: 999,
        properties: { name: 'GCP Layer' }
    });

    map.addLayer(gcpMapLayer);

    gcpTranslateInteraction = new ol.interaction.Translate({
        layers: [gcpMapLayer]
    });
    gcpTranslateInteraction.setActive(false);
    map.addInteraction(gcpTranslateInteraction);

    gcpTranslateInteraction.on('translateend', function (evt) {
        const feature = evt.features.item(0);
        const gcpId = feature.get('gcpId');
        const coord = feature.getGeometry().getCoordinates();
        const lonLat = ol.proj.toLonLat(coord);

        const gcp = gcps.find(g => g.pointId === gcpId);
        if (gcp) {
            gcp.longitude = parseFloat(lonLat[0].toFixed(7));
            gcp.latitude = parseFloat(lonLat[1].toFixed(7));
            refreshGcpTable();
        }
    });
}

function gcpMarkerStyle(feature) {
    const label = feature.get('gcpLabel') || '';
    return new ol.style.Style({
        image: new ol.style.Circle({
            radius: 7,
            fill: new ol.style.Fill({ color: 'rgba(255, 0, 0, 0.8)' }),
            stroke: new ol.style.Stroke({ color: '#fff', width: 2 })
        }),
        text: new ol.style.Text({
            text: label,
            offsetY: -15,
            font: 'bold 11px Arial',
            fill: new ol.style.Fill({ color: '#d9534f' }),
            stroke: new ol.style.Stroke({ color: '#fff', width: 3 })
        })
    });
}

// ─── Overlay Panel ───────────────────────────────────────────────────────────

function createOverlayPanel() {
    const panel = document.createElement('div');
    panel.className = 'georef-overlay-panel';
    panel.id = 'georef-overlay-panel';
    panel.style.display = 'none';
    panel.innerHTML = '<h6>Georeferenced Overlays</h6><div id="georef-overlay-list"></div>';

    const mapEl = map.getTargetElement();
    if (mapEl) {
        mapEl.style.position = 'relative';
        mapEl.appendChild(panel);
    }
}

function refreshOverlayPanel() {
    const panel = document.getElementById('georef-overlay-panel');
    const list = document.getElementById('georef-overlay-list');
    if (!panel || !list) return;

    if (overlayEntries.length === 0) {
        panel.style.display = 'none';
        return;
    }

    panel.style.display = 'block';
    list.innerHTML = '';

    overlayEntries.forEach(function (entry) {
        const item = document.createElement('div');
        item.className = 'georef-overlay-item';
        item.innerHTML = `
            <span class="georef-overlay-name" title="${entry.filename}">${entry.filename}</span>
            <input type="range" min="0" max="1" step="0.05" value="${entry.layer.getOpacity()}"
                   title="Opacity" data-overlay-id="${entry.id}">
            <span class="georef-overlay-edit fa fa-pencil" title="Edit" data-overlay-id="${entry.id}"></span>
            <span class="georef-overlay-remove fa fa-times" title="Remove" data-overlay-id="${entry.id}"></span>
        `;

        const slider = item.querySelector('input[type="range"]');
        slider.addEventListener('input', function () {
            entry.layer.setOpacity(parseFloat(this.value));
        });

        item.querySelector('.georef-overlay-edit').addEventListener('click', function () {
            editOverlay(entry.id);
        });

        item.querySelector('.georef-overlay-remove').addEventListener('click', function () {
            removeOverlay(entry.id);
        });

        list.appendChild(item);
    });
}

function removeOverlay(overlayId) {
    const idx = overlayEntries.findIndex(e => e.id === overlayId);
    if (idx === -1) return;

    map.removeLayer(overlayEntries[idx].layer);
    overlayEntries.splice(idx, 1);
    refreshOverlayPanel();
}

function editOverlay(overlayId) {
    const entry = overlayEntries.find(e => e.id === overlayId);
    if (!entry) return;

    editingOverlayId = overlayId;
    selectedImage = entry.file;
    gcps = JSON.parse(JSON.stringify(entry.gcps));
    gcpIdCounter = gcps.length > 0
        ? Math.max(...gcps.map(g => parseInt(g.pointId.replace('GCP', '')) || 0)) + 1
        : 1;

    showImagePreview(selectedImage);
    restoreGcpMarkers();
    refreshGcpTable();
    updateButtonStates();
    setStatus('Edit GCP points as needed, then click "Start Georeferencing" to update.', 'info');

    $('#georef_modal').modal('show');
}

// ─── Event Binding ───────────────────────────────────────────────────────────

function bindEvents() {
    document.addEventListener('click', function (e) {
        if (e.target && (e.target.id === 'openGeoreferencer' || e.target.closest('#openGeoreferencer'))) {
            e.preventDefault();
            openGeoreferencer();
        }
    });

    document.getElementById('georef-open-image').addEventListener('click', function () {
        document.getElementById('georef-file-input').click();
    });

    document.getElementById('georef-file-input').addEventListener('change', function (e) {
        if (e.target.files && e.target.files[0]) {
            handleImageSelect(e.target.files[0]);
        }
    });

    document.getElementById('georef-add-gcp').addEventListener('click', function () {
        toggleAddGcpMode();
    });

    document.getElementById('georef-delete-gcp').addEventListener('click', function () {
        toggleDeleteGcpMode();
    });

    document.getElementById('georef-start').addEventListener('click', function () {
        startGeoreferencing();
    });

    document.getElementById('georef-image-container').addEventListener('click', function (e) {
        handleImageClick(e);
    });

    $('#georef_modal').on('hidden.bs.modal', function () {
        onModalClose();
    });

    // Disable Bootstrap 3 enforceFocus so clicks on the map behind the modal work
    $.fn.modal.Constructor.prototype.enforceFocus = function () {};
}

// ─── Open / Close ────────────────────────────────────────────────────────────

function openGeoreferencer() {
    editingOverlayId = null;
    resetState();
    $('#georef_modal').modal('show');
}

function resetState() {
    selectedImage = null;
    imageNaturalWidth = 0;
    imageNaturalHeight = 0;
    gcps = [];
    gcpIdCounter = 0;
    addGcpMode = false;
    deleteGcpMode = false;
    pendingMapClick = null;

    clearGcpMapMarkers();
    clearImageMarkers();

    const container = document.getElementById('georef-image-container');
    container.innerHTML = '<div class="georef-no-image">No image loaded. Click "Open Image" to select a PNG or JPEG file.</div>';
    container.classList.remove('georef-add-mode', 'georef-delete-mode');

    document.getElementById('georef-gcp-tbody').innerHTML = '';
    document.getElementById('georef-add-gcp').classList.remove('active');
    document.getElementById('georef-delete-gcp').classList.remove('active');
    document.getElementById('georef-file-input').value = '';

    updateButtonStates();
    setStatus('Open an image to begin.', 'info');
}

function onModalClose() {
    disableAddGcpMode();
    disableDeleteGcpMode();
    clearGcpMapMarkers();

    if (editingOverlayId === null) {
        gcps = [];
    }
    editingOverlayId = null;
}

// ─── Image Handling ──────────────────────────────────────────────────────────

function handleImageSelect(file) {
    const validTypes = ['image/png', 'image/jpeg', 'image/jpg'];
    if (!validTypes.includes(file.type)) {
        setStatus('Unsupported format. Please select a PNG or JPEG image.', 'error');
        return;
    }

    selectedImage = file;
    showImagePreview(file);
    setStatus('Image loaded. Click "Add GCP" to start picking control points.', 'info');
    updateButtonStates();
}

function showImagePreview(file) {
    const container = document.getElementById('georef-image-container');
    container.innerHTML = '';

    const img = document.createElement('img');
    img.id = 'georef-preview-img';

    const reader = new FileReader();
    reader.onload = function (e) {
        img.src = e.target.result;
        img.onload = function () {
            imageNaturalWidth = img.naturalWidth;
            imageNaturalHeight = img.naturalHeight;
            restoreImageMarkers();
        };
    };
    reader.readAsDataURL(file);

    container.appendChild(img);
}

// ─── GCP Mode Toggles ───────────────────────────────────────────────────────

function toggleAddGcpMode() {
    if (addGcpMode) {
        disableAddGcpMode();
    } else {
        enableAddGcpMode();
    }
}

function enableAddGcpMode() {
    disableDeleteGcpMode();
    addGcpMode = true;
    pendingMapClick = 'map';

    document.getElementById('georef-add-gcp').classList.add('active');
    document.getElementById('georef-image-container').classList.add('georef-add-mode');
    map.getViewport().classList.add('georef-map-add-mode');

    gcpTranslateInteraction.setActive(true);

    mapClickListenerKey = map.on('click', handleMapClick);
    setStatus('Click on the map to pick a geographic point.', 'waiting');
}

function disableAddGcpMode() {
    addGcpMode = false;
    pendingMapClick = null;

    document.getElementById('georef-add-gcp').classList.remove('active');
    document.getElementById('georef-image-container').classList.remove('georef-add-mode');
    map.getViewport().classList.remove('georef-map-add-mode');

    gcpTranslateInteraction.setActive(false);

    if (mapClickListenerKey) {
        ol.Observable.unByKey(mapClickListenerKey);
        mapClickListenerKey = null;
    }

    updateButtonStates();
}

function toggleDeleteGcpMode() {
    if (deleteGcpMode) {
        disableDeleteGcpMode();
    } else {
        enableDeleteGcpMode();
    }
}

function enableDeleteGcpMode() {
    disableAddGcpMode();
    deleteGcpMode = true;

    document.getElementById('georef-delete-gcp').classList.add('active');
    document.getElementById('georef-image-container').classList.add('georef-delete-mode');

    setStatus('Click a GCP marker on the map or image to delete it.', 'waiting');
}

function disableDeleteGcpMode() {
    deleteGcpMode = false;

    document.getElementById('georef-delete-gcp').classList.remove('active');
    document.getElementById('georef-image-container').classList.remove('georef-delete-mode');

    updateButtonStates();
}

// ─── Map Click Handler ───────────────────────────────────────────────────────

function handleMapClick(evt) {
    if (!addGcpMode) return;

    if (pendingMapClick === 'map') {
        evt.stopPropagation();

        const coord = evt.coordinate;
        const lonLat = ol.proj.toLonLat(coord);

        const tempGcpId = 'GCP' + (gcpIdCounter + 1);

        const feature = new ol.Feature({
            geometry: new ol.geom.Point(coord),
            gcpId: tempGcpId,
            gcpLabel: String(gcpIdCounter + 1)
        });

        gcpMapLayer.getSource().addFeature(feature);

        pendingMapClick = 'image';
        window._georefPendingLonLat = lonLat;
        window._georefPendingFeature = feature;

        setStatus('Now click the corresponding point on the image.', 'waiting');
    }
}

// ─── Image Click Handler ─────────────────────────────────────────────────────

function handleImageClick(e) {
    const img = document.getElementById('georef-preview-img');
    if (!img) return;

    if (deleteGcpMode) {
        const marker = e.target.closest('.georef-image-marker');
        if (marker) {
            const gcpId = marker.dataset.gcpId;
            deleteGcp(gcpId);
        }
        return;
    }

    if (!addGcpMode || pendingMapClick !== 'image') return;

    const rect = img.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const clickY = e.clientY - rect.top;

    const pixelX = Math.round((clickX / img.clientWidth) * imageNaturalWidth);
    const pixelY = Math.round((clickY / img.clientHeight) * imageNaturalHeight);

    if (pixelX < 0 || pixelX > imageNaturalWidth || pixelY < 0 || pixelY > imageNaturalHeight) {
        return;
    }

    gcpIdCounter++;
    const lonLat = window._georefPendingLonLat;
    const gcp = {
        pointId: 'GCP' + gcpIdCounter,
        imageX: pixelX,
        imageY: pixelY,
        longitude: parseFloat(lonLat[0].toFixed(7)),
        latitude: parseFloat(lonLat[1].toFixed(7))
    };
    gcps.push(gcp);

    addImageMarker(gcp, img);
    refreshGcpTable();
    updateButtonStates();

    window._georefPendingLonLat = null;
    window._georefPendingFeature = null;
    pendingMapClick = 'map';

    if (gcps.length >= options.minGcps) {
        setStatus('GCP added. You have enough points to georeference. Add more or click "Start Georeferencing".', 'info');
    } else {
        setStatus('GCP added (' + gcps.length + '/' + options.minGcps + ' minimum). Click on the map to pick the next point.', 'waiting');
    }
}

// ─── Image Markers ───────────────────────────────────────────────────────────

function addImageMarker(gcp, img) {
    if (!img) img = document.getElementById('georef-preview-img');
    if (!img) return;

    const container = document.getElementById('georef-image-container');
    const marker = document.createElement('div');
    marker.className = 'georef-image-marker';
    marker.dataset.gcpId = gcp.pointId;
    marker.textContent = gcp.pointId.replace('GCP', '');

    positionImageMarker(marker, gcp, img);
    setupImageMarkerDrag(marker, gcp);

    container.appendChild(marker);
}

function positionImageMarker(marker, gcp, img) {
    if (!img) img = document.getElementById('georef-preview-img');
    if (!img) return;

    const displayX = (gcp.imageX / imageNaturalWidth) * img.clientWidth;
    const displayY = (gcp.imageY / imageNaturalHeight) * img.clientHeight;

    marker.style.left = displayX + 'px';
    marker.style.top = displayY + 'px';
}

function setupImageMarkerDrag(marker, gcp) {
    let isDragging = false;
    let startX, startY;

    marker.addEventListener('mousedown', function (e) {
        if (deleteGcpMode) return;
        if (addGcpMode && pendingMapClick === 'image') return;

        isDragging = true;
        startX = e.clientX;
        startY = e.clientY;
        marker.classList.add('georef-dragging');
        e.preventDefault();
        e.stopPropagation();
    });

    document.addEventListener('mousemove', function (e) {
        if (!isDragging) return;

        const img = document.getElementById('georef-preview-img');
        if (!img) return;

        const rect = img.getBoundingClientRect();
        const newX = e.clientX - rect.left;
        const newY = e.clientY - rect.top;

        marker.style.left = Math.max(0, Math.min(newX, img.clientWidth)) + 'px';
        marker.style.top = Math.max(0, Math.min(newY, img.clientHeight)) + 'px';
    });

    document.addEventListener('mouseup', function (e) {
        if (!isDragging) return;
        isDragging = false;
        marker.classList.remove('georef-dragging');

        const img = document.getElementById('georef-preview-img');
        if (!img) return;

        const rect = img.getBoundingClientRect();
        const newX = e.clientX - rect.left;
        const newY = e.clientY - rect.top;

        const pixelX = Math.round((Math.max(0, Math.min(newX, img.clientWidth)) / img.clientWidth) * imageNaturalWidth);
        const pixelY = Math.round((Math.max(0, Math.min(newY, img.clientHeight)) / img.clientHeight) * imageNaturalHeight);

        gcp.imageX = pixelX;
        gcp.imageY = pixelY;
        refreshGcpTable();
    });
}

function clearImageMarkers() {
    const container = document.getElementById('georef-image-container');
    if (!container) return;
    container.querySelectorAll('.georef-image-marker').forEach(m => m.remove());
}

function restoreImageMarkers() {
    clearImageMarkers();
    const img = document.getElementById('georef-preview-img');
    if (!img) return;

    gcps.forEach(function (gcp) {
        addImageMarker(gcp, img);
    });
}

// ─── GCP Map Markers ─────────────────────────────────────────────────────────

function clearGcpMapMarkers() {
    if (gcpMapLayer) {
        gcpMapLayer.getSource().clear();
    }
}

function restoreGcpMarkers() {
    clearGcpMapMarkers();
    gcps.forEach(function (gcp) {
        const coord = ol.proj.fromLonLat([gcp.longitude, gcp.latitude]);
        const feature = new ol.Feature({
            geometry: new ol.geom.Point(coord),
            gcpId: gcp.pointId,
            gcpLabel: gcp.pointId.replace('GCP', '')
        });
        gcpMapLayer.getSource().addFeature(feature);
    });
}

// ─── GCP Table ───────────────────────────────────────────────────────────────

function refreshGcpTable() {
    const tbody = document.getElementById('georef-gcp-tbody');
    tbody.innerHTML = '';

    gcps.forEach(function (gcp) {
        const row = document.createElement('tr');
        row.dataset.gcpId = gcp.pointId;
        row.innerHTML = `
            <td>${gcp.pointId}</td>
            <td contenteditable="true" data-field="imageX">${gcp.imageX}</td>
            <td contenteditable="true" data-field="imageY">${gcp.imageY}</td>
            <td contenteditable="true" data-field="longitude">${gcp.longitude}</td>
            <td contenteditable="true" data-field="latitude">${gcp.latitude}</td>
            <td><span class="georef-delete-btn" title="Delete">&times;</span></td>
        `;

        row.querySelectorAll('td[contenteditable]').forEach(function (cell) {
            cell.addEventListener('blur', function () {
                handleCellEdit(gcp.pointId, cell.dataset.field, cell.textContent.trim());
            });
            cell.addEventListener('keydown', function (e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    cell.blur();
                }
            });
        });

        row.querySelector('.georef-delete-btn').addEventListener('click', function () {
            deleteGcp(gcp.pointId);
        });

        tbody.appendChild(row);
    });

    updateGcpCount();
}

function handleCellEdit(gcpId, field, value) {
    const gcp = gcps.find(g => g.pointId === gcpId);
    if (!gcp) return;

    const numValue = parseFloat(value);
    if (isNaN(numValue)) {
        refreshGcpTable();
        return;
    }

    if (field === 'imageX') {
        gcp.imageX = Math.round(numValue);
    } else if (field === 'imageY') {
        gcp.imageY = Math.round(numValue);
    } else if (field === 'longitude') {
        gcp.longitude = numValue;
    } else if (field === 'latitude') {
        gcp.latitude = numValue;
    }

    if (field === 'longitude' || field === 'latitude') {
        updateMapMarkerPosition(gcpId);
    }
    if (field === 'imageX' || field === 'imageY') {
        updateImageMarkerPosition(gcpId);
    }
}

function updateMapMarkerPosition(gcpId) {
    const gcp = gcps.find(g => g.pointId === gcpId);
    if (!gcp) return;

    const features = gcpMapLayer.getSource().getFeatures();
    const feature = features.find(f => f.get('gcpId') === gcpId);
    if (feature) {
        const coord = ol.proj.fromLonLat([gcp.longitude, gcp.latitude]);
        feature.getGeometry().setCoordinates(coord);
    }
}

function updateImageMarkerPosition(gcpId) {
    const gcp = gcps.find(g => g.pointId === gcpId);
    if (!gcp) return;

    const marker = document.querySelector('.georef-image-marker[data-gcp-id="' + gcpId + '"]');
    if (marker) {
        positionImageMarker(marker, gcp);
    }
}

function updateGcpCount() {
    const countEl = document.getElementById('georef-gcp-count');
    if (countEl) {
        countEl.textContent = gcps.length + ' GCP(s) — minimum ' + options.minGcps + ' required';
    }
}

// ─── GCP Deletion ────────────────────────────────────────────────────────────

function deleteGcp(gcpId) {
    const idx = gcps.findIndex(g => g.pointId === gcpId);
    if (idx === -1) return;

    gcps.splice(idx, 1);

    // Remove map marker
    const features = gcpMapLayer.getSource().getFeatures();
    const feature = features.find(f => f.get('gcpId') === gcpId);
    if (feature) {
        gcpMapLayer.getSource().removeFeature(feature);
    }

    // Remove image marker
    const marker = document.querySelector('.georef-image-marker[data-gcp-id="' + gcpId + '"]');
    if (marker) marker.remove();

    refreshGcpTable();
    updateButtonStates();
}

// ─── Button State Management ─────────────────────────────────────────────────

function updateButtonStates() {
    const hasImage = selectedImage !== null;
    const hasEnoughGcps = gcps.length >= options.minGcps;

    document.getElementById('georef-add-gcp').disabled = !hasImage;
    document.getElementById('georef-delete-gcp').disabled = !hasImage || gcps.length === 0;
    document.getElementById('georef-start').disabled = !hasImage || !hasEnoughGcps;
}

// ─── Status Messages ─────────────────────────────────────────────────────────

function setStatus(message, type) {
    const el = document.getElementById('georef-status');
    el.textContent = message;
    el.className = 'georef-status';

    if (type === 'waiting') el.classList.add('georef-status-waiting');
    else if (type === 'error') el.classList.add('georef-status-error');
    else if (type === 'success') el.classList.add('georef-status-success');
}

// ─── CSRF Token ──────────────────────────────────────────────────────────────

async function fetchCsrfToken() {
    try {
        const response = await fetch(options.csrfTokenUrl);
        if (response.status === 200) {
            const data = await response.json();
            return data.csrfToken;
        }
    } catch (e) {
        console.warn('Georeferencer: failed to fetch CSRF token', e);
    }
    return null;
}

// ─── Start Georeferencing ────────────────────────────────────────────────────

async function startGeoreferencing() {
    if (!selectedImage) {
        setStatus('Please select an image first.', 'error');
        return;
    }
    if (gcps.length < options.minGcps) {
        setStatus('Need at least ' + options.minGcps + ' GCP points. Currently have ' + gcps.length + '.', 'error');
        return;
    }
    if (options.maxGcps != null && gcps.length > options.maxGcps) {
        setStatus('Maximum ' + options.maxGcps + ' GCP points allowed. Currently have ' + gcps.length + '.', 'error');
        return;
    }

    disableAddGcpMode();
    disableDeleteGcpMode();

    setStatus('Processing... Please wait.', 'waiting');
    document.getElementById('georef-start').disabled = true;

    try {
        const csrfToken = await fetchCsrfToken();

        const formData = new FormData();
        formData.append('image', selectedImage);
        formData.append('gcps', JSON.stringify(gcps));

        const headers = {};
        if (csrfToken) {
            headers['X-CSRF-TOKEN'] = csrfToken;
        }

        const response = await fetch(options.georefEndpoint, {
            method: 'POST',
            headers: headers,
            body: formData
        });

        const result = await response.json();

        if (!response.ok || !result.success) {
            setStatus('Georeferencing failed: ' + (result.message || 'Unknown error'), 'error');
            document.getElementById('georef-start').disabled = false;
            return;
        }

        const details = result.details;
        const extent = details.extent;
        const imageUrl = details.processedImageUrl;

        const epsg3857Extent = ol.proj.transformExtent(
            [extent.minLongitude, extent.minLatitude, extent.maxLongitude, extent.maxLatitude],
            'EPSG:4326',
            'EPSG:3857'
        );

        const fullImageUrl = '/georef' + imageUrl;

        const imageLayer = new ol.layer.Image({
            source: new ol.source.ImageStatic({
                url: fullImageUrl,
                imageExtent: epsg3857Extent,
                imageLoadFunction: function (image, src) {
                    const xhr = new XMLHttpRequest();
                    xhr.open('GET', src);
                    xhr.responseType = 'blob';
                    if (csrfToken) {
                        xhr.setRequestHeader('X-CSRF-TOKEN', csrfToken);
                    }
                    xhr.onload = function () {
                        if (xhr.status === 200) {
                            image.getImage().src = URL.createObjectURL(xhr.response);
                        }
                    };
                    xhr.send();
                }
            }),
            zIndex: 50
        });

        // If editing, replace the old overlay
        if (editingOverlayId !== null) {
            const existingIdx = overlayEntries.findIndex(e => e.id === editingOverlayId);
            if (existingIdx !== -1) {
                map.removeLayer(overlayEntries[existingIdx].layer);
                overlayEntries[existingIdx].layer = imageLayer;
                overlayEntries[existingIdx].gcps = JSON.parse(JSON.stringify(gcps));
                overlayEntries[existingIdx].file = selectedImage;
            }
        } else {
            overlayIdCounter++;
            overlayEntries.push({
                id: overlayIdCounter,
                layer: imageLayer,
                file: selectedImage,
                gcps: JSON.parse(JSON.stringify(gcps)),
                filename: selectedImage.name
            });
        }

        map.addLayer(imageLayer);
        map.getView().fit(epsg3857Extent, { padding: [50, 50, 50, 50], maxZoom: 20 });

        clearGcpMapMarkers();
        refreshOverlayPanel();

        setStatus('Georeferencing complete! Image overlaid on the map.', 'success');

        setTimeout(function () {
            $('#georef_modal').modal('hide');
        }, 1000);

    } catch (e) {
        console.error('Georeferencer: API error', e);
        setStatus('Error: ' + e.message, 'error');
        document.getElementById('georef-start').disabled = false;
    }
}
