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
let gcpTsCounter = 0;
let addGcpMode = false;
let deleteGcpMode = false;
let pendingMapClick = null; // 'map' | 'image' | null
let gcpMapLayer = null;
let gcpTranslateInteraction = null;
let overlayEntries = []; // { layer, file, gcps, filename, id }
let overlayIdCounter = 0;
let editingOverlayId = null;
let editingSavedOpacity = null;
let mapClickListenerKey = null;
let imageZoom = 1;
let pendingGcpTs = null;
let pendingFeature = null;
let initialized = false;
let mapArtifactsReady = false;
let configReady = false;

const DEFAULTS = {
    georefEndpoint: '/georef/api/georeference',
    csrfTokenUrl: '/msp/security/api/csrf-token/',
    imageBaseUrl: null,
    minGcps: 4,
    maxGcps: 10,
    initPollInterval: 100,
    initMaxAttempts: 150,
    zoomMin: 0.5,
    zoomMax: 10,
    zoomStep: 0.1,
    coordPrecision: 7,
    xhrTimeout: 30000
};

// Initialization

/**
 * Initialize the georeferencer module against an OpenLayers map.
 * @param {ol.Map|function():ol.Map} olMapOrGetter - Map instance or getter; if getter returns falsy, retries on a poll interval.
 * @param {Object} [userOptions] - Overrides for DEFAULTS (endpoints, GCP limits, zoom, timeouts).
 */
export function initGeoreferencer(olMapOrGetter, userOptions) {
    // Guard against repeat init() calls (duplicate script include, SPA re-init) that would
    // inject a second #georef_modal and stack document-level listeners.
    if (initialized) return;
    options = Object.assign({}, DEFAULTS, userOptions);

    function tryInit() {
        if (initialized) return true; // a concurrent poll already completed init
        var resolvedMap = typeof olMapOrGetter === 'function' ? olMapOrGetter() : olMapOrGetter;
        if (!resolvedMap) {
            return false;
        }
        map = resolvedMap;
        initialized = true;
        // Host-map artifacts (GCP layer, interactions, overlay panel) are created lazily on
        // first open so users who never use the tool incur no host-map side-effects.
        injectModalHTML();
        injectInstructionsTab();
        bindEvents();
        fetchBackendConfig();
        return true;
    }

    if (!tryInit()) {
        var attempts = 0;
        var interval = setInterval(function () {
            attempts++;
            if (tryInit() || attempts > options.initMaxAttempts) {
                clearInterval(interval);
                if (!map) {
                    console.error('Georeferencer: map object not available after waiting');
                }
            }
        }, options.initPollInterval);
    }
}

/** Fetches GCP min/max config from the backend; falls back to DEFAULTS on failure. */
async function fetchBackendConfig() {
    try {
        const response = await fetch(options.georefEndpoint + '/config');
        if (response.ok) {
            const config = await response.json();
            if (typeof config?.gcp?.minCount === 'number') {
                options.minGcps = config.gcp.minCount;
            }
            if (typeof config?.gcp?.maxCount === 'number' || config?.gcp?.maxCount === null) {
                options.maxGcps = config.gcp.maxCount; // null means no limit
            }
        }
    } catch (e) {
        console.warn('Georeferencer: could not fetch backend config, using defaults', e);
    } finally {
        configReady = true;
        // Refresh anything that depends on the GCP limits now that they're known.
        const statusEl = document.getElementById('georef-status');
        if (statusEl) {
            updateButtonStates();
            updateGcpCount();
            // Only replace the loading placeholder so we don't clobber an in-progress status
            // if the config resolved slowly while the user was already working.
            if (statusEl.textContent === 'Loading configuration...') {
                restoreDefaultStatus();
            }
        }
    }
}

/** Lazily creates the host-map artifacts (GCP layer, interactions, overlay panel) once. */
function ensureMapArtifacts() {
    if (mapArtifactsReady) return;
    mapArtifactsReady = true;
    createGcpMapLayer();
    createOverlayPanel();
}

// Modal HTML Injection

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
                            <i class="fa fa-plus-circle"></i> Add/Edit GCP
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

/**
 * Injects a "Georeferencing" help tab into each of the host page's existing
 * Instructions modals, if present. The ISD/MAP tool exposes two such modals
 * (#instructions_modal for MAP, #rgainstructions_modal for RGA) and the TIM tool
 * exposes one (#instructions_modal). Keeping this content in the shared module
 * means every tool documents the georeferencer from a single source instead of
 * duplicating markup in each tool's index.html.
 */
function injectInstructionsTab() {
    // paneId must be unique per modal so multiple modals on the same page don't collide.
    injectGeorefTab('instructions_modal', 'instructions_tab_georef');
    injectGeorefTab('rgainstructions_modal', 'rgainstructions_tab_georef');
}

/** Appends the Georeferencing nav tab + pane to a single Instructions modal. */
function injectGeorefTab(modalId, paneId) {
    const modal = document.getElementById(modalId);
    if (!modal) return; // Host doesn't have this modal; nothing to document.

    const navTabs = modal.querySelector('.nav-tabs');
    const tabContent = modal.querySelector('.tab-content');
    if (!navTabs || !tabContent) return;

    // Guard against double-injection (e.g. duplicate script include).
    if (document.getElementById(paneId)) return;

    const navItem = document.createElement('li');
    navItem.setAttribute('role', 'presentation');
    navItem.innerHTML = '<a href="#' + paneId + '" aria-controls="georeferencing" ' +
        'role="tab" data-toggle="tab">Georeferencing</a>';
    navTabs.appendChild(navItem);

    const pane = document.createElement('div');
    pane.setAttribute('role', 'tabpanel');
    pane.className = 'tab-pane';
    pane.id = paneId;
    pane.innerHTML = `
        <div class="container-fluid">
            <h4>Georeferencing an Image</h4>
            <p class="help-block">Align an aerial or scanned image to the map by
                matching points, then overlay the warped result on the map.</p>
            <ol>
                <li>Open the tool by clicking <strong>Georeferencing</strong> in the
                    top-right of the navbar to bring up the <strong>Georeferencer</strong> dialog.</li>
                <li>Click <strong>Open Image</strong> (<i class="fa fa-folder-open"></i>)
                    and select a PNG or JPEG image from your local machine.</li>
                <li>Adding Ground Control Points (GCPs)</li>
                <ol type="a">
                    <li>Click <strong>Add/Edit GCP</strong> (<i class="fa fa-plus-circle"></i>) to begin picking points.</li>
                    <li>For each control point, click the location on the <strong>map</strong> first,
                        then click the matching point on the <strong>image</strong>.</li>
                    <li>A minimum of <strong>4</strong> and a maximum of <strong>10</strong> GCPs are required.</li>
                </ol>
                <li>Adjusting Points</li>
                <ol type="a">
                    <li>Drag a marker on the map to reposition it; its longitude and latitude update automatically.</li>
                    <li>Edit the <strong>Longitude</strong>, <strong>Latitude</strong>,
                        <strong>Image X (px)</strong>, or <strong>Image Y (px)</strong> values directly in the GCP table.</li>
                    <li>Use the mouse wheel to zoom and middle-click drag to pan in both the map and image views.</li>
                </ol>
                <li>Deleting Points</li>
                <ol type="a">
                    <li>Click <strong>Delete GCP</strong> (<i class="fa fa-minus-circle"></i>) then click a marker, or</li>
                    <li>Click the <strong>&times;</strong> button in the point's row in the GCP table.</li>
                </ol>
                <li>Click <strong>Start Georeferencing</strong> (<i class="fa fa-play"></i>)
                    to warp the image and overlay it on the map.</li>
                <li>Managing Overlays</li>
                <ol type="a">
                    <li>Use the <strong>Georeferenced Overlays</strong> panel to adjust an overlay's <strong>opacity</strong>.</li>
                    <li>Click <strong>Edit</strong> to reopen the dialog and modify its control points.</li>
                    <li>Click <strong>Delete</strong> to remove the overlay from the map.</li>
                </ol>
            </ol>
        </div>`;
    tabContent.appendChild(pane);
}

// GCP Helpers

/** A GCP is complete once its image (pixel) point has been picked. */
function isGcpComplete(gcp) {
    return gcp.imageX != null && gcp.imageY != null;
}

// GCP Map Layer

/** Creates the OL vector layer and translate interaction for GCP map markers. */
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
        const ts = feature.get('gcpTs');
        const coord = feature.getGeometry().getCoordinates();
        const lonLat = ol.proj.toLonLat(coord);

        const gcp = gcps.find(g => g._ts === ts);
        if (gcp) {
            gcp.longitude = parseFloat(lonLat[0].toFixed(options.coordPrecision));
            gcp.latitude = parseFloat(lonLat[1].toFixed(options.coordPrecision));
            refreshGcpTable();
        }
    });

    map.on('pointermove', function (evt) {
        if (evt.dragging) return;
        var hit = map.hasFeatureAtPixel(evt.pixel, { layerFilter: function (l) { return l === gcpMapLayer; } });
        var vp = map.getViewport();
        if (hit) {
            vp.classList.add('georef-map-marker-hover');
        } else {
            vp.classList.remove('georef-map-marker-hover');
        }
    });

    map.addInteraction(new ol.interaction.DragPan({
        condition: function (evt) {
            return evt.originalEvent.button === 1;
        }
    }));
}

function gcpMarkerStyle(feature) {
    const label = feature.get('gcpLabel') || '';
    var size = 10;
    var gap = 2;
    return [
        new ol.style.Style({
            image: new ol.style.Circle({
                radius: size,
                fill: new ol.style.Fill({ color: 'rgba(0, 0, 0, 0.0)' })
            })
        }),
        new ol.style.Style({
            renderer: function (coords, state) {
                var ctx = state.context;
                var x = coords[0], y = coords[1];
                ctx.save();
                ctx.strokeStyle = 'rgba(255, 0, 0, 0.8)';
                ctx.lineWidth = 2;
                ctx.beginPath();
                ctx.moveTo(x, y - size); ctx.lineTo(x, y - gap);
                ctx.moveTo(x, y + gap);  ctx.lineTo(x, y + size);
                ctx.moveTo(x - size, y); ctx.lineTo(x - gap, y);
                ctx.moveTo(x + gap, y);  ctx.lineTo(x + size, y);
                ctx.stroke();
                ctx.restore();
            }
        }),
        new ol.style.Style({
            text: new ol.style.Text({
                text: label,
                offsetX: 10,
                offsetY: -10,
                textAlign: 'left',
                textBaseline: 'bottom',
                font: 'bold 11px Arial',
                fill: new ol.style.Fill({ color: '#d9534f' }),
                stroke: new ol.style.Stroke({ color: '#fff', width: 3 })
            })
        })
    ];
}

// Overlay Panel

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
        const nameSpan = document.createElement('span');
        nameSpan.className = 'georef-overlay-name';
        nameSpan.title = entry.filename;
        nameSpan.textContent = entry.filename;
        item.appendChild(nameSpan);

        const slider = document.createElement('input');
        slider.type = 'range';
        slider.min = '0';
        slider.max = '1';
        slider.step = '0.05';
        slider.value = entry.layer.getOpacity();
        slider.title = 'Opacity';
        slider.dataset.overlayId = entry.id;
        item.appendChild(slider);

        const editIcon = document.createElement('span');
        editIcon.className = 'georef-overlay-edit fa fa-pencil';
        editIcon.title = 'Edit';
        editIcon.dataset.overlayId = entry.id;
        item.appendChild(editIcon);

        const removeIcon = document.createElement('span');
        removeIcon.className = 'georef-overlay-remove fa fa-times';
        removeIcon.title = 'Remove';
        removeIcon.dataset.overlayId = entry.id;
        item.appendChild(removeIcon);

        slider.addEventListener('input', function () {
            entry.layer.setOpacity(parseFloat(this.value));
        });

        editIcon.addEventListener('click', function () {
            editOverlay(entry.id);
        });

        removeIcon.addEventListener('click', function () {
            removeOverlay(entry.id);
        });

        list.appendChild(item);
    });
}

function removeOverlay(overlayId) {
    const idx = overlayEntries.findIndex(e => e.id === overlayId);
    if (idx === -1) return;

    if (!confirm('Remove this overlay? This cannot be undone.')) return;

    map.removeLayer(overlayEntries[idx].layer);
    overlayEntries.splice(idx, 1);
    refreshOverlayPanel();
}

/**
 * Re-opens the modal pre-populated with an existing overlay's GCPs for editing.
 * @param {number} overlayId - ID of the overlay entry to edit.
 */
function editOverlay(overlayId) {
    // An edit session stores a single overlay's pre-edit opacity in editingSavedOpacity.
    // Starting a second edit before finishing the first would overwrite that and strand the
    // first overlay at opacity 0, so refuse to enter edit while one is already in progress.
    if (editingOverlayId !== null) {
        setStatus('Finish or cancel the current edit before editing another overlay.', 'error');
        return;
    }

    ensureMapArtifacts();

    const entry = overlayEntries.find(e => e.id === overlayId);
    if (!entry) return;

    editingOverlayId = overlayId;
    editingSavedOpacity = entry.layer.getOpacity();
    selectedImage = entry.file;
    gcps = JSON.parse(JSON.stringify(entry.gcps));
    // Backfill _ts for any GCPs saved before this field existed
    gcps.forEach(function (g, i) {
        if (!g._ts) g._ts = i + 1;
    });
    gcpTsCounter = gcps.length > 0
        ? Math.max(...gcps.map(g => g._ts)) + 1
        : 1;
    renumberGcps();

    entry.layer.setOpacity(0);
    refreshOverlayPanel();

    showImagePreview(selectedImage);
    restoreGcpMarkers();
    refreshGcpTable();
    updateButtonStates();

    $('#georef_modal').modal('show');
    enableAddGcpMode();
    setStatus('Add more points or drag existing ones, then click "Start Georeferencing" to update.', 'waiting');
}

// Event Binding

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

    document.getElementById('georef-image-container').addEventListener('wheel', function (e) {
        handleImageZoom(e);
    });

    var imgContainer = document.getElementById('georef-image-container');
    var isPanning = false;
    var panStartX, panStartY, scrollStartX, scrollStartY;

    imgContainer.addEventListener('mousedown', function (e) {
        if (e.button !== 1) return;
        e.preventDefault();
        isPanning = true;
        panStartX = e.clientX;
        panStartY = e.clientY;
        scrollStartX = imgContainer.scrollLeft;
        scrollStartY = imgContainer.scrollTop;
        imgContainer.style.cursor = 'grabbing';
    });

    window.addEventListener('mousemove', function (e) {
        if (!isPanning) return;
        imgContainer.scrollLeft = scrollStartX - (e.clientX - panStartX);
        imgContainer.scrollTop = scrollStartY - (e.clientY - panStartY);
    });

    window.addEventListener('mouseup', function (e) {
        if (e.button !== 1 || !isPanning) return;
        isPanning = false;
        imgContainer.style.cursor = '';
    });

    $('#georef_modal').on('hidden.bs.modal', function () {
        onModalClose();
    });

    patchBootstrapEnforceFocus();
}

// Disable Bootstrap 3's modal focus enforcement ONLY for the georef modal, so the user
// can click the underlying map while the modal is open. This monkeypatches a shared
// Bootstrap prototype, so it must run exactly once — guard against repeated init() calls
// re-wrapping (and stacking) the override.
let enforceFocusPatched = false;
function patchBootstrapEnforceFocus() {
    if (enforceFocusPatched) return;
    if (!($.fn.modal && $.fn.modal.Constructor)) return;
    enforceFocusPatched = true;

    var origEnforceFocus = $.fn.modal.Constructor.prototype.enforceFocus;
    $.fn.modal.Constructor.prototype.enforceFocus = function () {
        if (this.$element && this.$element.attr('id') === 'georef_modal') return;
        origEnforceFocus.call(this);
    };
}

// Open / Close

function openGeoreferencer() {
    ensureMapArtifacts();
    editingOverlayId = null;
    resetState();
    $('#georef_modal').modal('show');
    if (!configReady) {
        setStatus('Loading configuration...', 'waiting');
    }
}

function resetState() {
    selectedImage = null;
    imageNaturalWidth = 0;
    imageNaturalHeight = 0;
    gcps = [];
    gcpTsCounter = 0;
    addGcpMode = false;
    deleteGcpMode = false;
    pendingMapClick = null;
    imageZoom = 1;

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
    updateGcpCount();
    setStatus('Open an image to begin.', 'info');
}

function onModalClose() {
    disableAddGcpMode();
    disableDeleteGcpMode();
    clearGcpMapMarkers();

    // If an edit session was opened but not applied (cancelled), restore the
    // original overlay's opacity that editOverlay() set to 0 while editing.
    if (editingOverlayId !== null && editingSavedOpacity !== null) {
        const entry = overlayEntries.find(e => e.id === editingOverlayId);
        if (entry) {
            entry.layer.setOpacity(editingSavedOpacity);
            refreshOverlayPanel();
        }
    }
    editingSavedOpacity = null;

    if (editingOverlayId === null) {
        gcps = [];
    }
    editingOverlayId = null;
}

// Image Handling

function handleImageSelect(file) {
    const validTypes = ['image/png', 'image/jpeg', 'image/jpg'];
    if (!validTypes.includes(file.type)) {
        setStatus('Unsupported format. Please select a PNG or JPEG image.', 'error');
        return;
    }

    selectedImage = file;
    showImagePreview(file);
    setStatus('Image loaded. Click "Add/Edit GCP" to start picking control points.', 'info');
    updateButtonStates();
}

/**
 * Renders the selected image into the preview container inside a positioning
 * stage element, and restores any existing GCP markers once the image loads.
 * @param {File} file - Image file to preview.
 */
function showImagePreview(file) {
    const container = document.getElementById('georef-image-container');
    container.innerHTML = '';
    imageZoom = 1;

    // Stage wraps the image so markers can be positioned relative to the image itself,
    // not the scroll container — keeps them aligned regardless of any container offset.
    const stage = document.createElement('div');
    stage.className = 'georef-image-stage';
    stage.id = 'georef-image-stage';

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
        img.onerror = function () {
            setStatus('Failed to load image. The file may not be in the correct format.', 'error');
        };
    };
    reader.readAsDataURL(file);

    stage.appendChild(img);
    container.appendChild(stage);
}

/** Zooms the image preview on mouse wheel, keeping the cursor point stationary. */
function handleImageZoom(e) {
    var img = document.getElementById('georef-preview-img');
    if (!img) return;

    e.preventDefault();

    var container = document.getElementById('georef-image-container');
    var delta = e.deltaY > 0 ? -options.zoomStep : options.zoomStep;
    var newZoom = Math.min(Math.max(imageZoom + delta, options.zoomMin), options.zoomMax);
    if (newZoom === imageZoom) return;

    var rect = container.getBoundingClientRect();
    var mouseX = e.clientX - rect.left + container.scrollLeft;
    var mouseY = e.clientY - rect.top + container.scrollTop;

    var ratio = newZoom / imageZoom;
    imageZoom = newZoom;

    applyImageZoom();

    container.scrollLeft = mouseX * ratio - (e.clientX - rect.left);
    container.scrollTop = mouseY * ratio - (e.clientY - rect.top);
}

function applyImageZoom() {
    var img = document.getElementById('georef-preview-img');
    if (!img) return;

    img.style.width = (imageZoom * 100) + '%';

    repositionAllImageMarkers();
}

// GCP Mode Toggles

function toggleAddGcpMode() {
    if (addGcpMode) {
        disableAddGcpMode();
    } else {
        enableAddGcpMode();
    }
}

function enableAddGcpMode() {
    if (addGcpMode) return; // already active — avoid rebinding (and leaking) the map click listener
    disableDeleteGcpMode();
    addGcpMode = true;
    pendingMapClick = 'map';

    document.getElementById('georef-add-gcp').classList.add('active');
    document.getElementById('georef-image-container').classList.add('georef-add-mode');
    map.getViewport().classList.add('georef-map-add-mode');

    gcpTranslateInteraction.setActive(true);

    mapClickListenerKey = map.on('click', handleMapClick);
    setStatus('Click on the map to pick a geographic point or drag existing ones to reposition them.', 'waiting');
}

function disableAddGcpMode() {
    addGcpMode = false;

    // Discard any in-progress GCP that only has its map point (no image point yet).
    if (pendingMapClick === 'image' && pendingGcpTs !== null) {
        discardPendingGcp();
    }
    pendingMapClick = null;
    pendingGcpTs = null;
    pendingFeature = null;

    document.getElementById('georef-add-gcp').classList.remove('active');
    document.getElementById('georef-image-container').classList.remove('georef-add-mode');
    map.getViewport().classList.remove('georef-map-add-mode');

    gcpTranslateInteraction.setActive(false);

    if (mapClickListenerKey) {
        ol.Observable.unByKey(mapClickListenerKey);
        mapClickListenerKey = null;
    }

    updateButtonStates();
    restoreDefaultStatus();
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
    restoreDefaultStatus();
}

// Map Click Handler

/**
 * Handles a map click during GCP add mode. Places a temporary map marker and
 * transitions the pending state to expect the corresponding image click.
 * @param {ol.MapBrowserEvent} evt
 */
function handleMapClick(evt) {
    if (!addGcpMode) return;

    if (pendingMapClick === 'map') {
        evt.stopPropagation();

        if (options.maxGcps != null && gcps.length >= options.maxGcps) {
            setStatus('Maximum of ' + options.maxGcps + ' GCPs reached. Delete a point before adding another.', 'error');
            return;
        }

        const coord = evt.coordinate;
        const lonLat = ol.proj.toLonLat(coord);

        // Create the GCP immediately (image point still pending) so it appears in the
        // table right away and stays in sync when the map marker is dragged or edited.
        gcpTsCounter++;
        const gcp = {
            _ts: gcpTsCounter,
            pointId: 'GCP ' + (gcps.length + 1),
            imageX: null,
            imageY: null,
            longitude: parseFloat(lonLat[0].toFixed(options.coordPrecision)),
            latitude: parseFloat(lonLat[1].toFixed(options.coordPrecision))
        };
        gcps.push(gcp);

        const feature = new ol.Feature({
            geometry: new ol.geom.Point(coord),
            gcpTs: gcp._ts,
            gcpLabel: gcp.pointId.replace('GCP ', '')
        });

        gcpMapLayer.getSource().addFeature(feature);

        pendingMapClick = 'image';
        pendingGcpTs = gcp._ts;
        pendingFeature = feature;

        renumberGcps();
        refreshGcpTable();
        updateButtonStates();

        setStatus('Now click the corresponding point on the image.', 'waiting');
    }
}

// Image Click Handler

/**
 * Handles a click on the image preview. In add-GCP mode with a pending map point,
 * computes the pixel coordinate and finalizes the GCP pair. In delete mode,
 * removes the clicked marker's GCP.
 * @param {MouseEvent} e
 */
function handleImageClick(e) {
    const img = document.getElementById('georef-preview-img');
    if (!img) return;

    if (deleteGcpMode) {
        const marker = e.target.closest('.georef-image-marker');
        if (marker) {
            deleteGcp(parseInt(marker.dataset.gcpTs));
        }
        return;
    }

    if (!addGcpMode || pendingMapClick !== 'image') return;

    const rect = img.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const clickY = e.clientY - rect.top;

    const pixelX = Math.round((clickX / img.clientWidth) * imageNaturalWidth);
    const pixelY = Math.round((clickY / img.clientHeight) * imageNaturalHeight);

    // Ignore clicks outside the image (the container is larger than the rendered image, e.g.
    // the letterbox margin when zoomed out).
    if (pixelX < 0 || pixelX >= imageNaturalWidth || pixelY < 0 || pixelY >= imageNaturalHeight) {
        return;
    }

    // Complete the in-progress GCP created on the map click by filling in its pixel point.
    const gcp = gcps.find(g => g._ts === pendingGcpTs);
    if (!gcp) return;

    gcp.imageX = pixelX;
    gcp.imageY = pixelY;

    addImageMarker(gcp, img);
    refreshGcpTable();
    updateButtonStates();

    pendingGcpTs = null;
    pendingFeature = null;
    pendingMapClick = 'map';

    if (options.maxGcps != null && gcps.length >= options.maxGcps) {
        setStatus('GCP added. Maximum of ' + options.maxGcps + ' GCPs reached. Click "Start Georeferencing".', 'info');
    } else if (gcps.length >= options.minGcps) {
        setStatus('GCP added. Sufficient points added. Add more or click "Start Georeferencing".', 'info');
    } else {
        setStatus('GCP added (' + gcps.length + '/' + options.minGcps + ' minimum). Click on the map to pick the next point.', 'waiting');
    }
}

// Image Markers

function addImageMarker(gcp, img) {
    if (!img) img = document.getElementById('georef-preview-img');
    if (!img) return;

    const stage = document.getElementById('georef-image-stage');
    if (!stage) return;

    const marker = document.createElement('div');
    marker.className = 'georef-image-marker';
    marker.dataset.gcpTs = gcp._ts;
    const label = document.createElement('span');
    label.className = 'georef-marker-label';
    label.textContent = gcp.pointId.replace('GCP ', '');
    marker.appendChild(label);

    positionImageMarker(marker, gcp, img);
    setupImageMarkerDrag(marker);

    stage.appendChild(marker);
}

/**
 * Sets a marker's left/top to the GCP's pixel coords scaled to current display size.
 * @param {HTMLElement} marker
 * @param {Object} gcp - Must have imageX, imageY in natural-pixel coords.
 * @param {HTMLImageElement} [img] - Preview image element; looked up if omitted.
 */
function positionImageMarker(marker, gcp, img) {
    if (!img) img = document.getElementById('georef-preview-img');
    if (!img) return;
    // Natural dimensions are 0 until the image's async onload; bail to avoid NaN positions.
    // restoreImageMarkers() runs on load and will position correctly once they're known.
    if (!imageNaturalWidth || !imageNaturalHeight) return;

    const displayX = (gcp.imageX / imageNaturalWidth) * img.clientWidth;
    const displayY = (gcp.imageY / imageNaturalHeight) * img.clientHeight;

    marker.style.left = displayX + 'px';
    marker.style.top = displayY + 'px';
}

let dragState = null;

function setupImageMarkerDrag(marker) {
    marker.addEventListener('mousedown', function (e) {
        if (deleteGcpMode) return;
        if (addGcpMode && pendingMapClick === 'image') return;

        const ts = parseInt(marker.dataset.gcpTs);
        dragState = { marker: marker, gcpTs: ts };
        marker.classList.add('georef-dragging');
        e.preventDefault();
        e.stopPropagation();
    });
}

document.addEventListener('mousemove', function (e) {
    if (!dragState) return;

    const img = document.getElementById('georef-preview-img');
    if (!img) return;

    const rect = img.getBoundingClientRect();
    const newX = e.clientX - rect.left;
    const newY = e.clientY - rect.top;

    dragState.marker.style.left = Math.max(0, Math.min(newX, img.clientWidth)) + 'px';
    dragState.marker.style.top = Math.max(0, Math.min(newY, img.clientHeight)) + 'px';
});

document.addEventListener('mouseup', function (e) {
    if (!dragState) return;

    const marker = dragState.marker;
    const gcp = gcps.find(g => g._ts === dragState.gcpTs);
    dragState = null;
    marker.classList.remove('georef-dragging');

    const img = document.getElementById('georef-preview-img');
    if (!img || !gcp) return;

    const rect = img.getBoundingClientRect();
    const newX = e.clientX - rect.left;
    const newY = e.clientY - rect.top;

    gcp.imageX = Math.round((Math.max(0, Math.min(newX, img.clientWidth)) / img.clientWidth) * imageNaturalWidth);
    gcp.imageY = Math.round((Math.max(0, Math.min(newY, img.clientHeight)) / img.clientHeight) * imageNaturalHeight);
    refreshGcpTable();
});

function repositionAllImageMarkers() {
    var img = document.getElementById('georef-preview-img');
    if (!img) return;
    gcps.forEach(function (gcp) {
        var marker = document.querySelector('.georef-image-marker[data-gcp-ts="' + gcp._ts + '"]');
        if (marker) positionImageMarker(marker, gcp, img);
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

// GCP Map Markers

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
            gcpTs: gcp._ts,
            gcpLabel: gcp.pointId.replace('GCP ', '')
        });
        gcpMapLayer.getSource().addFeature(feature);
    });
}

// GCP Table

function refreshGcpTable() {
    const tbody = document.getElementById('georef-gcp-tbody');
    tbody.innerHTML = '';

    gcps.forEach(function (gcp) {
        const row = document.createElement('tr');
        row.dataset.gcpTs = gcp._ts;
        // Image point is pending until picked: show blank, non-editable cells for it.
        var imagePending = !isGcpComplete(gcp);
        var fields = [
            { text: gcp.pointId, editable: false },
            { text: imagePending ? '' : gcp.imageX, editable: !imagePending, field: 'imageX' },
            { text: imagePending ? '' : gcp.imageY, editable: !imagePending, field: 'imageY' },
            { text: gcp.longitude, editable: true, field: 'longitude' },
            { text: gcp.latitude, editable: true, field: 'latitude' }
        ];
        fields.forEach(function (f) {
            var td = document.createElement('td');
            td.textContent = f.text;
            if (f.editable) {
                td.contentEditable = 'true';
                td.dataset.field = f.field;
            }
            row.appendChild(td);
        });
        var deleteTd = document.createElement('td');
        var deleteBtn = document.createElement('span');
        deleteBtn.className = 'georef-delete-btn';
        deleteBtn.title = 'Delete';
        deleteBtn.textContent = '×';
        deleteTd.appendChild(deleteBtn);
        row.appendChild(deleteTd);

        row.querySelectorAll('td[contenteditable]').forEach(function (cell) {
            cell.addEventListener('blur', function () {
                handleCellEdit(gcp._ts, cell.dataset.field, cell.textContent.trim());
            });
            cell.addEventListener('keydown', function (e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    cell.blur();
                }
            });
        });

        row.querySelector('.georef-delete-btn').addEventListener('click', function () {
            deleteGcp(gcp._ts);
        });

        tbody.appendChild(row);
    });

    updateGcpCount();
}

/**
 * Applies a user edit from a contenteditable table cell back to the GCP model.
 * Validates numeric range, clamps image coords, and syncs the corresponding marker.
 * @param {number} gcpTs - Internal timestamp key identifying the GCP.
 * @param {string} field - One of 'imageX', 'imageY', 'longitude', 'latitude'.
 * @param {string} value - Raw text from the cell.
 */
function handleCellEdit(gcpTs, field, value) {
    const gcp = gcps.find(g => g._ts === gcpTs);
    if (!gcp) return;

    const numValue = parseFloat(value);
    if (isNaN(numValue)) {
        refreshGcpTable();
        return;
    }

    if (field === 'imageX') {
        gcp.imageX = Math.max(0, Math.min(Math.round(numValue), imageNaturalWidth));
    } else if (field === 'imageY') {
        gcp.imageY = Math.max(0, Math.min(Math.round(numValue), imageNaturalHeight));
    } else if (field === 'longitude') {
        if (numValue < -180 || numValue > 180) { refreshGcpTable(); return; }
        gcp.longitude = numValue;
    } else if (field === 'latitude') {
        if (numValue < -90 || numValue > 90) { refreshGcpTable(); return; }
        gcp.latitude = numValue;
    }

    if (field === 'longitude' || field === 'latitude') {
        updateMapMarkerPosition(gcpTs);
    }
    if (field === 'imageX' || field === 'imageY') {
        updateImageMarkerPosition(gcpTs);
    }
}

function updateMapMarkerPosition(gcpTs) {
    const gcp = gcps.find(g => g._ts === gcpTs);
    if (!gcp) return;

    const features = gcpMapLayer.getSource().getFeatures();
    const feature = features.find(f => f.get('gcpTs') === gcpTs);
    if (feature) {
        const coord = ol.proj.fromLonLat([gcp.longitude, gcp.latitude]);
        feature.getGeometry().setCoordinates(coord);
    }
}

function updateImageMarkerPosition(gcpTs) {
    const gcp = gcps.find(g => g._ts === gcpTs);
    if (!gcp) return;

    const marker = document.querySelector('.georef-image-marker[data-gcp-ts="' + gcpTs + '"]');
    if (marker) {
        positionImageMarker(marker, gcp);
    }
}

function updateGcpCount() {
    const countEl = document.getElementById('georef-gcp-count');
    if (countEl) {
        const completeCount = gcps.filter(isGcpComplete).length;
        countEl.textContent = completeCount + ' GCP(s) — minimum ' + options.minGcps + ' required';
    }
}

// GCP Renumbering

/** Re-sorts GCPs by creation order and reassigns sequential pointId labels and marker text. */
function renumberGcps() {
    gcps.sort(function (a, b) { return a._ts - b._ts; });
    gcps.forEach(function (gcp, i) {
        gcp.pointId = 'GCP ' + (i + 1);
    });

    // Update map feature labels
    if (gcpMapLayer) {
        var features = gcpMapLayer.getSource().getFeatures();
        gcps.forEach(function (gcp) {
            var feature = features.find(function (f) { return f.get('gcpTs') === gcp._ts; });
            if (feature) {
                feature.set('gcpLabel', gcp.pointId.replace('GCP ', ''));
            }
        });
    }

    // Update image marker labels
    gcps.forEach(function (gcp) {
        var marker = document.querySelector('.georef-image-marker[data-gcp-ts="' + gcp._ts + '"]');
        if (marker) {
            var label = marker.querySelector('.georef-marker-label');
            if (label) {
                label.textContent = gcp.pointId.replace('GCP ', '');
            }
        }
    });
}

// GCP Deletion

function deleteGcp(gcpTs) {
    const idx = gcps.findIndex(g => g._ts === gcpTs);
    if (idx === -1) return;

    gcps.splice(idx, 1);

    // Remove map marker
    const features = gcpMapLayer.getSource().getFeatures();
    const feature = features.find(f => f.get('gcpTs') === gcpTs);
    if (feature) {
        gcpMapLayer.getSource().removeFeature(feature);
    }

    // Remove image marker
    const marker = document.querySelector('.georef-image-marker[data-gcp-ts="' + gcpTs + '"]');
    if (marker) marker.remove();

    // If the in-progress GCP was deleted, reset the pending pair state.
    if (gcpTs === pendingGcpTs) {
        pendingGcpTs = null;
        pendingFeature = null;
        pendingMapClick = 'map';
    }

    renumberGcps();
    refreshGcpTable();
    updateButtonStates();
}

/** Removes the in-progress GCP (map point picked, image point not yet) and its marker. */
function discardPendingGcp() {
    if (pendingGcpTs === null) return;

    const idx = gcps.findIndex(g => g._ts === pendingGcpTs);
    if (idx !== -1) {
        gcps.splice(idx, 1);
    }

    if (pendingFeature) {
        gcpMapLayer.getSource().removeFeature(pendingFeature);
    }

    renumberGcps();
    refreshGcpTable();
}

// Button State Management

function updateButtonStates() {
    const hasImage = selectedImage !== null;
    const completeCount = gcps.filter(isGcpComplete).length;
    // Block submission while any GCP is still missing its image point.
    const allComplete = gcps.every(isGcpComplete);
    const hasEnoughGcps = completeCount >= options.minGcps && allComplete;

    document.getElementById('georef-add-gcp').disabled = !hasImage;
    document.getElementById('georef-delete-gcp').disabled = !hasImage || gcps.length === 0;
    document.getElementById('georef-start').disabled = !hasImage || !hasEnoughGcps;
}

// Status Messages

function restoreDefaultStatus() {
    // Count only complete GCPs so the status stays consistent with the count badge and the
    // Start button, which both ignore the pending (image-point-less) GCP.
    if (gcps.filter(isGcpComplete).length >= options.minGcps) {
        setStatus('Sufficient points selected. Add more or click "Start Georeferencing".', 'info');
    } else if (selectedImage) {
        setStatus('Click "Add/Edit GCP" to start picking control points.', 'info');
    } else {
        setStatus('Open an image to begin.', 'info');
    }
}

function setStatus(message, type) {
    const el = document.getElementById('georef-status');
    el.textContent = message;
    el.className = 'georef-status';

    if (type === 'waiting') el.classList.add('georef-status-waiting');
    else if (type === 'error') el.classList.add('georef-status-error');
    else if (type === 'success') el.classList.add('georef-status-success');
}

// CSRF Token

/** Fetches the CSRF token from the security endpoint; returns null on failure. */
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

// Start Georeferencing

/**
 * Submits the image and GCPs to the backend, validates the response, and
 * adds (or replaces) the georeferenced image as an OL static-image layer.
 */
async function startGeoreferencing() {
    if (!selectedImage) {
        setStatus('Please select an image first.', 'error');
        return;
    }

    // Discard any in-progress (incomplete) GCP before validating counts.
    disableAddGcpMode();
    disableDeleteGcpMode();

    if (gcps.length < options.minGcps) {
        setStatus('Need at least ' + options.minGcps + ' GCP points. Currently have ' + gcps.length + '.', 'error');
        return;
    }
    if (options.maxGcps != null && gcps.length > options.maxGcps) {
        setStatus('Maximum ' + options.maxGcps + ' GCP points allowed. Currently have ' + gcps.length + '.', 'error');
        return;
    }

    setStatus('Processing... Please wait.', 'waiting');
    document.getElementById('georef-start').disabled = true;

    try {
        const csrfToken = await fetchCsrfToken();

        const formData = new FormData();
        formData.append('image', selectedImage);
        var gcpsForApi = gcps.map(function (g) {
            return { pointId: g.pointId, imageX: g.imageX, imageY: g.imageY, longitude: g.longitude, latitude: g.latitude };
        });
        formData.append('gcps', JSON.stringify(gcpsForApi));

        const headers = {};
        if (csrfToken) {
            headers['X-CSRF-TOKEN'] = csrfToken;
        }

        const controller = new AbortController();
        const timeoutId = setTimeout(function () { controller.abort(); }, options.xhrTimeout);

        let response;
        try {
            response = await fetch(options.georefEndpoint, {
                method: 'POST',
                headers: headers,
                body: formData,
                signal: controller.signal
            });
        } finally {
            clearTimeout(timeoutId);
        }

        let result;
        try {
            result = await response.json();
        } catch (parseErr) {
            setStatus('Server returned an invalid response.', 'error');
            document.getElementById('georef-start').disabled = false;
            return;
        }

        if (!response.ok || !result.success) {
            setStatus('Georeferencing failed: ' + (result.message || 'Unknown error'), 'error');
            document.getElementById('georef-start').disabled = false;
            return;
        }

        const details = result.details;
        const extent = details && details.extent;
        const imageUrl = details && details.processedImageUrl;

        const extentValid = extent &&
            ['minLongitude', 'maxLongitude', 'minLatitude', 'maxLatitude']
                .every(k => typeof extent[k] === 'number' && isFinite(extent[k]));

        if (!imageUrl || !extentValid) {
            setStatus('Georeferencing did not return a usable result. Please try again.', 'error');
            document.getElementById('georef-start').disabled = false;
            return;
        }

        const epsg3857Extent = ol.proj.transformExtent(
            [extent.minLongitude, extent.minLatitude, extent.maxLongitude, extent.maxLatitude],
            'EPSG:4326',
            'EPSG:3857'
        );

        // Derive the gateway prefix from the configured endpoint (e.g. '/georef/api/georeference'
        // -> '/georef') so the overlay URL stays correct if the service is mounted elsewhere.
        const apiBase = options.imageBaseUrl != null
            ? options.imageBaseUrl
            : options.georefEndpoint.replace(/\/api\/georeference\/?$/, '');
        const fullImageUrl = apiBase + imageUrl;

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
                    xhr.timeout = options.xhrTimeout;
                    xhr.onload = function () {
                        if (xhr.status === 200) {
                            var objectUrl = URL.createObjectURL(xhr.response);
                            var imgEl = image.getImage();
                            // Revoke once the blob has been decoded (or failed to) to avoid leaking object URLs.
                            imgEl.onload = function () { URL.revokeObjectURL(objectUrl); };
                            imgEl.onerror = function () { URL.revokeObjectURL(objectUrl); };
                            imgEl.src = objectUrl;
                        } else {
                            console.error('Georeferencer: failed to load overlay image, status ' + xhr.status);
                        }
                    };
                    xhr.onerror = function () {
                        console.error('Georeferencer: network error loading overlay image');
                    };
                    xhr.ontimeout = function () {
                        console.error('Georeferencer: timeout loading overlay image');
                    };
                    xhr.send();
                }
            }),
            // Sit just above the base map (zIndex 0) but below all user-created
            // components (utensils), which live at zIndex >= 2.
            zIndex: 1
        });

        // If editing, replace the old overlay
        if (editingOverlayId !== null) {
            const existingIdx = overlayEntries.findIndex(e => e.id === editingOverlayId);
            if (existingIdx !== -1) {
                map.removeLayer(overlayEntries[existingIdx].layer);
                // Carry over the overlay's prior opacity to the replacement layer, then
                // mark the edit as applied so onModalClose doesn't try to "restore" it.
                imageLayer.setOpacity(editingSavedOpacity != null ? editingSavedOpacity : 1);
                editingSavedOpacity = null;
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
        if (e.name === 'AbortError') {
            setStatus('Georeferencing timed out. Please try again.', 'error');
        } else {
            setStatus('Error: ' + e.message, 'error');
        }
        document.getElementById('georef-start').disabled = false;
    }
}