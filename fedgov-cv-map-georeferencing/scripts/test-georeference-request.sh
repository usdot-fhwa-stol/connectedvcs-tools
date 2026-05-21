#!/bin/bash

# Curl request to test the georeferencing API
# Make sure the Spring Boot application is running on localhost:8080

curl -X POST http://localhost:8080/georef/api/georeference \
  -H "Content-Type: multipart/form-data" \
  -F "image=@TFHRC.png;type=image/png" \
  -F 'gcps=[{"pointId":"gcp_1","imageX":352,"imageY":481,"longitude":-77.14927178321017,"latitude":38.95507925856188},{"pointId":"gcp_2","imageX":142,"imageY":92,"longitude":-77.15036875159703,"latitude":38.9567156465794},{"pointId":"gcp_3","imageX":7,"imageY":291,"longitude":-77.1511317719668,"latitude":38.95589516646859},{"pointId":"gcp_4","imageX":790,"imageY":112,"longitude":-77.14684744260903,"latitude":38.95659631154902},{"pointId":"gcp_5","imageX":211,"imageY":26,"longitude":-77.15009896310019,"latitude":38.95700425800709},{"pointId":"gcp_6","imageX":710,"imageY":507,"longitude":-77.14735724553047,"latitude":38.954964556728726}]' \
  -w "\n\nHTTP Status: %{http_code}\nResponse Time: %{time_total}s\n" \
  -v