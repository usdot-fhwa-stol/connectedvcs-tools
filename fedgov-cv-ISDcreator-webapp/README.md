# Connected Vehicles ISD Creator Webapp Project

## Overview

The fedgov-cv-ISDcreator-webapp project is a webapp that allows a user to define the lanes and approaches of an intersection using a graphical interface. Once designed, the user can encode a MAP or RGA message as an ASN.1 UPER Hex string.

The ISD Creator Tool is accessible at <https://webapp.connectedvcs.com/isd>

## Georeferencing

The tool includes a georeferencing feature that lets you align an uploaded image to the map and overlay the warped result. Workflow:

- Open the **Georeferencing** tool from the top-right navbar.
- Click **Open Image** and select a PNG or JPEG.
- Click **Add/Edit GCP**, then for each control point click the location on the map followed by the matching point on the image (minimum 4, maximum 10 GCPs).
- Fine-tune points by dragging markers or editing the longitude/latitude and pixel values in the GCP table.
- Click **Start Georeferencing** to warp and overlay the image on the map.
- Adjust overlay opacity, or edit/delete the overlay, from the Georeferenced Overlays panel.

## Sim Mode

Sim mode lets you draw a MAP message on top of a simulator's Lanelet2 OSM map, whose origin is lat/lon 0,0, and encode it in sim coordinates. Workflow:

- Pan the map to where you want to work, then go to **File** > **Open Sim Map...** and select the `.osm` file. The OSM lanes are drawn as a guide layer centered on the current map view.
- Draw the intersection as usual. When encoding, node, reference point, and verified point coordinates are converted back to sim lat/lon.
- Set the elevation with the **Ground z (m)** field in the SIM MODE panel; it applies to all lane nodes and markers, so elevation deltas stay 0. Esri elevation lookups are skipped while sim mode is on.
- Saving the map file stores the sim state. To reopen it, first open the OSM file with **File** > **Open Sim Map...**, then open the saved map; sim mode is turned back on and the OSM lanes are moved to the origin saved in the file. Opening a map file without sim state while in sim mode asks whether to stay in sim mode; if choosing to stay in sim mode, the sim origin is placed at that map's reference point.
- Go to **File** > **Exit Sim Mode** to return to normal encoding.

## JavaScript Unit Tests

Unit tests for the sim mode coordinate conversions (`js/sim-geo.js`) are in `src/test/js`. They are not run by the Maven build; run them with Node 22.12 or later from this directory:

```bash
node --test "src/test/js/*.test.js"
```

Or, without a local Node install:

```bash
docker run --rm -v "$PWD":/w -w /w node:22 node --test "src/test/js/*.test.js"
```
