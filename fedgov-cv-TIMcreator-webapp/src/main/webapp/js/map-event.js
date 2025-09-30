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
const minZoom = 1;
const maxZoom = 21;
//Set cookie anytime map is moved
function onMoveEnd(event, map) {
    let centerPoint = map.getView().getCenter();
    let lonLat = ol.proj.toLonLat(centerPoint);
    setCookie("tim_latitude", lonLat[1], 365);
    setCookie("tim_longitude", lonLat[0], 365);  
    setCookie("tim_zoom", map.getView().getZoom(), 365);
    $('#zoomLevel .zoom').text(map.getView().getZoom());
}

function onPointerMove(event, map, controls) {
    map.getTargetElement().style.cursor = 'default';
    const feature = map.forEachFeatureAtPixel(event.pixel, (feature) => feature);
    if (feature) {
        const geometry = feature.getGeometry();
        const featureType = geometry.getType();
        if(featureType === 'Point' || featureType === 'Polygon' ) {
            map.getTargetElement().style.cursor = 'pointer';
        }
        if(controls?.modify.getActive() && featureType === 'LineString'){
            map.getTargetElement().style.cursor = 'pointer';      
        }
    }
}

function onZoomOut(event, map) {
    let currentZoom = map.getView().getZoom();
    map.getView().animate({
        center: map.getView().getCenter(),
        zoom: (currentZoom - 1) > minZoom ? currentZoom - 1 : minZoom,
        duration: 500
    });
}

function onZoomIn(event, map) {
    let currentZoom = map.getView().getZoom();
    map.getView().animate({
        center: map.getView().getCenter(),
        zoom: (currentZoom + 1) > maxZoom ? maxZoom : currentZoom + 1,
        duration: 500
    });
}

function onZoomCallback(event, map) {
    let currentZoom = map.getView().getZoom();
    if (currentZoom < minZoom) {
        map.getView().setZoom(minZoom);
    }
    if (currentZoom > maxZoom) {
        map.getView().setZoom(maxZoom);
    }    
    $('#zoomLevel .zoom').text(currentZoom);
}

export {
    onMoveEnd,
    onPointerMove,
    onZoomIn,
    onZoomOut,
    onZoomCallback
}