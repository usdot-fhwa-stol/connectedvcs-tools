// Minimal OpenLayers UI to pick GCPs and send to backend
const imageInput = document.getElementById('imageInput');
const startPick = document.getElementById('startPick');
const sendBtn = document.getElementById('sendBtn');
const countEl = document.getElementById('count');

let map, rasterLayer, imgElement, overlayLayer, markerLayer, imageLayer;
let picking = false;
let imgSize = null; // {width, height}
const gcps = []; // {pixel: [x,y], lonlat: [lon,lat]}
let currentPickedPixel = null; // [x,y]
let enableAddMarker = false;

const markerSource = new ol.source.Vector();

markerLayer = new ol.layer.Vector({
  source: markerSource,
  zIndex: 3
});

function renderGcpTable() {
  const table = document.getElementById('gcpTable');
  const tbody = table.querySelector('tbody');
  tbody.innerHTML = '';
  if (gcps.length === 0) {
    tbody.innerHTML = '<tr class="gcp-empty-row"><td colspan="5" style="padding:1rem; color:#666;">No GCPs picked yet</td></tr>';
  } else {
    gcps.forEach((gcp, idx) => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td style="padding:0.5rem;">${idx + 1}</td>
        <td style="padding:0.5rem;">${gcp?.pixel[0]}, ${gcp?.pixel[1]}</td>
        <td style="padding:0.5rem;">${gcp?.lonlat[0].toFixed(6)}, ${gcp?.lonlat[1].toFixed(6)}</td>
      `;
      tbody.appendChild(tr);
    });
  }
  // Update total
  const totalEl = document.getElementById('gcpTotal');
  if (totalEl) totalEl.textContent = gcps.length;
}
function initMap() {
  map = new ol.Map({
    target: 'map',
    layers: [
      new ol.layer.Tile({ source: new ol.source.OSM() })
    ],
    view: new ol.View({ center: ol.proj.fromLonLat([-77.145875, 38.955841]), zoom: 16 })
  });

  overlayLayer = new ol.layer.Vector({
    source: new ol.source.Vector(),
    zIndex: 1
  });
  map.addLayer(overlayLayer);
  // const imgUrl = 'http://localhost:5500/georef.png';
    // compute a proper image extent in EPSG:3857 from lon/lat corners.
    // Replace the lon/lat coordinates below with the real georeferenced bbox of your image.
    // const bottomLeft = ol.proj.fromLonLat([-77.1512196000, 38.9549078000]); // minLon, minLat
    // const topRight = ol.proj.fromLonLat([-77.1468121000, 38.9571312000]);   // maxLon, maxLat
    // const extent = [bottomLeft[0], bottomLeft[1], topRight[0], topRight[1]];
    // console.log('Image extent (EPSG:3857):', extent);
    // const imageLayer = new ol.layer.Image({
    //   source: new ol.source.ImageStatic({
    //     url: imgUrl,
    //     imageExtent: extent,
    //     projection: 'EPSG:3857'
    //   }),
    //   opacity: 0.6
    // });
    // map.addLayer(imageLayer);

  const svgMarker = `
  <svg xmlns="http://www.w3.org/2000/svg" width="32" height="48" viewBox="0 0 32 48">
    <path d="M16 0C9.372 0 4 5.372 4 12c0 10 12 24 12 24s12-14 12-24C28 5.372 22.628 0 16 0z" fill="#d00"/>
    <circle cx="16" cy="12" r="5" fill="#fff"/>
  </svg>
  `;

  markerLayer.setStyle(new ol.style.Style({
    image: new ol.style.Icon({
      anchor: [0.5, 1],
      src: 'data:image/svg+xml;utf8,' + encodeURIComponent(svgMarker),
      scale: 1
    })
  }));
  map.addLayer(markerLayer);

  map.on('singleclick', (evt) => {
    if(enableAddMarker) {
      const feat = new ol.Feature(new ol.geom.Point(evt.coordinate));
      markerSource.addFeature(feat);
      return;
    }
    if (!picking || !imgElement) return;
    const coord = ol.proj.toLonLat(evt.coordinate);
    if(currentPickedPixel === null) {
      alert('Please pick a pixel from the image preview first.');
      return;
    }
    console.log('Using picked pixel:', currentPickedPixel);
    gcps.push({ pixel: currentPickedPixel, lonlat: coord });
    console.log('Added GCP:', gcps[gcps.length - 1]);
    
    updateGCPMarkers(coord);
    countEl.textContent = gcps.length;
    sendBtn.disabled = gcps.length < 4;
    renderGcpTable();
  });

  
}

function updateGCPMarkers(lonlat) {
  const source = overlayLayer.getSource();
  const feat = new ol.Feature(new ol.geom.Point(ol.proj.fromLonLat(lonlat)));
  feat.setStyle(new ol.style.Style({
    image: new ol.style.Circle({ radius: 6, fill: new ol.style.Fill({color:'red'}), stroke: new ol.style.Stroke({color:'white', width:2}) })
  }));
  source.addFeature(feat);
}

imageInput.addEventListener('change', (e) => {
  const file = e.target.files && e.target.files[0];
  if (!file) return;
  const url = URL.createObjectURL(file);
  imgElement = new Image();
  imgElement.onload = () => {
    imgSize = { width: imgElement.naturalWidth, height: imgElement.naturalHeight };
    startPick.disabled = false;
    // Show preview dialog
    const dialog = document.getElementById('imgPreviewDialog');
    const previewImg = document.getElementById('imgPreview');
    const currentPickedPixelSpan = document.getElementById('currentPickedPixel');
    previewImg.src = url;
    // Attach click event for pixel picking
    previewImg.onclick = function(event) {
      // Get click coordinates relative to the image
      const rect = previewImg.getBoundingClientRect();
      const scaleX = imgElement.naturalWidth / previewImg.width;
      const scaleY = imgElement.naturalHeight / previewImg.height;
      const x = Math.round((event.clientX - rect.left) * scaleX);
      const y = Math.round((event.clientY - rect.top) * scaleY);
      // Store picked pixel for later use
      previewImg.dataset.pickedX = x;
      previewImg.dataset.pickedY = y;
      // Optionally, show feedback
      previewImg.title = `Picked: (${x}, ${y})`;
      if(currentPickedPixelSpan) {
        currentPickedPixelSpan.textContent = `Current Picked Pixel: (${x}, ${y})`;
      }
      console.log(previewImg.dataset);
      currentPickedPixel = [x, y];
    };
    dialog.showModal();
    // Optionally, you can add instructions here
  };
  imgElement.src = url;
  // retain file for upload
  imageInput.dataset.fileUrl = url;
});
// Helper to get picked pixel from preview image
function getPickedPixel() {
  const previewImg = document.getElementById('imgPreview');
  const x = previewImg.dataset.pickedX;
  const y = previewImg.dataset.pickedY;
  if (x !== undefined && y !== undefined) {
    return [Number(x), Number(y)];
  }
  return null;
}

// Optionally, you can use getPickedPixel() in your GCP picking workflow.

// Close preview dialog handler
document.getElementById('closePreviewBtn').addEventListener('click', () => {
  document.getElementById('imgPreviewDialog').close();
});

const showPreviewBtn = document.getElementById('showPreviewBtn');
showPreviewBtn.addEventListener('click', () => {
  const dialog = document.getElementById('imgPreviewDialog');
  const previewImg = document.getElementById('imgPreview');
  const url = imageInput.dataset.fileUrl;
  if (url) {
    previewImg.src = url;
    dialog.showModal();
  } else {
    // If no image selected yet, open file picker
    imageInput.click();
  }
});

startPick.addEventListener('click', () => {
  if (!imgElement) return;
  picking = !picking;
  startPick.textContent = picking ? 'Stop picking' : 'Start picking GCPs';
});

addMarker.addEventListener('click', () => {
  enableAddMarker = !enableAddMarker;
  if(enableAddMarker) {
    addMarker.textContent = 'Finish adding markers';
  } else {
    addMarker.textContent = 'Add Marker';
  }
});

sendBtn.addEventListener('click', async () => {
  const file = imageInput.files && imageInput.files[0];
  if (!file) { alert('No image'); return; }
  if (gcps.length < 4) { alert('Need at least 4 GCPs'); return; }

  // Transform GCPs to match the expected DTO format
  const gcpsFormatted = gcps.map((g, index) => ({
    pointId: `gcp_${index + 1}`,
    imageX: g.pixel[0],
    imageY: g.pixel[1],
    longitude: g.lonlat[0],
    latitude: g.lonlat[1]
  }));

  const form = new FormData();
  form.append('image', file, file.name);
  form.append('gcps', JSON.stringify(gcpsFormatted));

  sendBtn.disabled = true;
  sendBtn.textContent = 'Processing...';
  
  try {
    console.log('Sending request to backend with GCPs:', form);
    const res = await fetch('http://localhost:8080/api/georeference', { 
      method: 'POST', 
      body: form 
    });
    console.log('Raw response:', res);
    
    if (!res.ok) {
      const errorText = await res.text();
      throw new Error(`Server error (${res.status}): ${errorText}`);
    }
    
    const responseData = await res.json();
    console.log('Response data:', responseData);
    
    // If we have a processed image URL, create overlay
    if (responseData.processedImageUrl) {
      const imgUrl = `http://localhost:8080${responseData.processedImageUrl}`;
      
      // Debug: Log the raw response data
      console.log('Raw extent from server:', responseData.extent);
      console.log('Extent projection:', responseData.extentProjection);
      console.log('Raw GCP coordinates:', gcps);
      console.log('Image dimensions from response:', responseData.imageInfo);
      console.log('Image URL:', imgUrl);
      
      // Calculate extent from GCPs or use provided extent
      let extent;
      if (responseData.extent && responseData.extent.minLongitude !== undefined) {
        // Server provided WGS84 extent coordinates (EPSG:4326) - convert to Web Mercator for OpenLayers
        const bottomLeft = ol.proj.fromLonLat([responseData.extent.minLongitude, responseData.extent.minLatitude]);
        const topRight = ol.proj.fromLonLat([responseData.extent.maxLongitude, responseData.extent.maxLatitude]);
        extent = [bottomLeft[0], bottomLeft[1], topRight[0], topRight[1]];
        console.log('Using server-provided WGS84 extent (EPSG:4326), converted to Web Mercator');
        console.log('Original WGS84 extent:', responseData.extent);
        console.log('Converted Web Mercator extent:', extent);
      } else if (responseData.extent && responseData.extent.minX !== undefined) {
        // Server extent is in Web Mercator coordinates (fallback case)
        extent = [
          responseData.extent.minX,
          responseData.extent.minY,
          responseData.extent.maxX,
          responseData.extent.maxY
        ];
        console.log('Using server-provided Web Mercator extent directly');
      } else {
        // Fallback: calculate extent from GCPs with padding
        const lons = gcps.map(g => g.lonlat[0]);
        const lats = gcps.map(g => g.lonlat[1]);
        const minLon = Math.min(...lons);
        const maxLon = Math.max(...lons);
        const minLat = Math.min(...lats);
        const maxLat = Math.max(...lats);
        
        // Add some padding to the extent (10% on each side)
        const lonRange = maxLon - minLon;
        const latRange = maxLat - minLat;
        const padding = 0.1;
        
        const paddedMinLon = minLon - (lonRange * padding);
        const paddedMaxLon = maxLon + (lonRange * padding);
        const paddedMinLat = minLat - (latRange * padding);
        const paddedMaxLat = maxLat + (latRange * padding);
        
        const bottomLeft = ol.proj.fromLonLat([paddedMinLon, paddedMinLat]);
        const topRight = ol.proj.fromLonLat([paddedMaxLon, paddedMaxLat]);
        extent = [bottomLeft[0], bottomLeft[1], topRight[0], topRight[1]];
        console.log('Using calculated extent with padding from GCPs');
      }
      
      console.log('Image extent (EPSG:3857):', extent);
      console.log('Creating image layer with URL length:', imgUrl.length);
      console.log('Extent width:', extent[2] - extent[0], 'height:', extent[3] - extent[1]);
      
      // Remove existing image layer if any
      if (imageLayer) {
        map.removeLayer(imageLayer);
      }
      
      // Test if the image can be loaded by creating a test Image element
      const testImg = new Image();
      testImg.onload = function() {
        console.log('Image loaded successfully, dimensions:', this.width, 'x', this.height);
        
        // Create the image layer after confirming the image loads
        imageLayer = new ol.layer.Image({
          source: new ol.source.ImageStatic({
            url: imgUrl,
            imageExtent: extent,
            projection: 'EPSG:3857'
          }),
          opacity: 0.7,
          zIndex: 2
        });
        
        // Add error handling for image loading
        imageLayer.getSource().on('imageloaderror', function(event) {
          console.error('Image layer failed to load:', event);
          alert('Failed to load the georeferenced image overlay. Check console for details.');
        });
        
        imageLayer.getSource().on('imageloadend', function(event) {
          console.log('Image layer loaded successfully:', event);
        });
        
        map.addLayer(imageLayer);
        
        // Zoom to extent
        map.getView().fit(extent, { padding: [50, 50, 50, 50] });
        
        alert('Georeferenced image processed and added as overlay!');
      };
      testImg.onerror = function() {
        console.error('Failed to load image data');
        alert('Failed to load the processed image. The image format may not be supported.');
      };
      testImg.src = imgUrl;
    } else {
      alert('Processing completed successfully, but no georeferenced image was returned.');
    }
    
    addMarker.disabled = false;
    
  } catch (err) {
    console.error('Request failed:', err);
    alert('Failed: ' + err.message);
  } finally {
    sendBtn.disabled = false;
    sendBtn.textContent = 'Send to backend';
  }
});


// Opacity slider handler: updates display and adjusts topmost Image layer opacity live
(function() {
  const slider = document.getElementById('opacitySlider');
  const display = document.getElementById('opacityValue'); // optional span to show percent

  if (!slider) return;

  const max = Number(slider.max || 100);
  const toFraction = v => (max > 1 ? Number(v) / max : Number(v));

  const setDisplay = (frac) => {
    if (!display) return;
    const pct = Math.round(frac * 100);
    display.textContent = `${pct}%`;
  };

  // initialize display from current slider value
  setDisplay(toFraction(slider.value));

  slider.addEventListener('input', (evt) => {
    const frac = toFraction(evt.target.value);
    setDisplay(frac);

    if (!map) return;
    const layers = map.getLayers().getArray();
    // find the most recently added Image layer and set its opacity
    for (let i = layers.length - 1; i >= 0; i--) {
      const lyr = layers[i];
      if (lyr instanceof ol.layer.Image) {
        lyr.setOpacity(frac);
        break;
      }
    }
  });
})();

initMap();
