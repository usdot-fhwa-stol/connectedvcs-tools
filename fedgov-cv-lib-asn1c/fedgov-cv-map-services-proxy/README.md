# Connected Vehicles Message Builder Project

## Overview

The fedgov-cv-map-services-proxy is a software application to forward map related (BingMap and Google Map) service API calls and monitor the calls. It helps to monitor the service calls for better understanding of how these third party map services are used.

The Map Services Proxy is accessible at <https://webapp.connectedvcs.com/msp>

## Build image
- Update Google API and Bing MAP keys in  fedgov-cv-map-services-proxy folder `resources/application.properties` file.
- Navigate to connectedvcs-tools folder
- Run `docker build -t usdotfhwastol/connectedvcs-tools:<tag> .` to build the docker image

## Run image
Run the docker image with docker run command:  `docker run -p 8080:8080 -v ${PWD}/logs:/var/lib/jetty/logs usdotfhwastol/connectedvcs-tools:<rag>`
`
The map services proxy log file should be available in the logs folder relative to the directory where the docker run command is executed.