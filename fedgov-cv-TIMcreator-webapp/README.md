# Connected Vehicles TIM Creator Webapp Project

## Overview

The fedgov-cv-TIMcreator-webapp project is a webapp that allows users to build traveler information messages regarding sign and work zone details using a graphical interface. Once designed, the user can encode a TIM message as an ASN.1 UPER Hex string and deposit it to the SDW warehouse.

The TIM Creator Tool is accessible at <https://webapp.connectedvcs.com/tim>

## Georeferencing

The tool includes a georeferencing feature that lets you align an uploaded image to the map and overlay the warped result. Workflow:

- Open the **Georeferencing** tool from the top-right navbar.
- Click **Open Image** and select a PNG or JPEG.
- Click **Add/Edit GCP**, then for each control point click the location on the map followed by the matching point on the image (minimum 4, maximum 10 GCPs).
- Fine-tune points by dragging markers or editing the longitude/latitude and pixel values in the GCP table.
- Click **Start Georeferencing** to warp and overlay the image on the map.
- Adjust overlay opacity, or edit/delete the overlay, from the Georeferenced Overlays panel.