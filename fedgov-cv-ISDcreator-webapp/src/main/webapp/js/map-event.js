
//Set cookie anytime map is moved
function onMoveEnd(event, map) {
    let centerPoint = map.getView().getCenter();
    let lonLat = ol.proj.toLonLat(centerPoint);
    setCookie("isd_latitude", lonLat[1], 365);
    setCookie("isd_longitude", lonLat[0], 365);  
    setCookie("isd_zoom", map.getView().getZoom(), 365);
    $('#zoomLevel .zoom').text(map.getView().getZoom());
}

function onPointerMove(event, map) {
    map.getTargetElement().style.cursor = 'default';
    const feature = map.forEachFeatureAtPixel(event.pixel, (feature) => feature);
    if (feature) {
        const geometry = feature.getGeometry();
        const featureType = geometry.getType();
        if(featureType === 'Point' || featureType === 'Polygon' ) {
            map.getTargetElement().style.cursor = 'pointer';
        }
    }
}

function onZoomOut(event, map) {
    let currentZoom = map.getView().getZoom();
    map.getView().animate({
        center: map.getView().getCenter(),
        zoom: currentZoom - 1,
        duration: 500
    });
}

function onZoomIn(event, map) {
    let currentZoom = map.getView().getZoom();
    map.getView().animate({
        center: map.getView().getCenter(),
        zoom: currentZoom + 1,
        duration: 500
    });
}

export {
    onMoveEnd,
    onPointerMove,
    onZoomIn,
    onZoomOut
}