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
- Set the elevation for new nodes with the **Ground z (m)** field in the SIM MODE panel. Esri elevation lookups are skipped while sim mode is on.
- Saving the map file stores the sim state. Opening that file later turns sim mode back on; use **Open Sim Map...** again to show the lanes. Opening a map file without sim state exits sim mode.
- Go to **File** > **Exit Sim Mode** to return to normal encoding.
