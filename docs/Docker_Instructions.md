# Running Dockerized ConnectedVCS Tools
If you are running ConnectedVCS Tools using a docker image, you can use the following instructions:

**NOTE:** Bing Maps is now deprecated and new users are no longer allowed to create API keys. We will be migrating to a different map provider and updating this documentation soon.

## Prerequisites
ConnectedVCS Tools has been developed using Ubuntu 20.04 and Ubuntu 22.04. Further testing with other operating systems is needed before guidance is created. For the moment, please use Ubuntu 20.04 or later [Ubuntu LTS Release](https://releases.ubuntu.com/).

### Install Docker CE
Instructions for installing Docker may change, so please use the current instructions at the Docker website:
https://docs.docker.com/desktop/install/linux-install/.

## Run the ConnectedVCS Tools Image

### Build a Custom Image

1. Clone the ConnectedVCS Tools respository:
```
git clone https://github.com/usdot-fhwa-stol/connectedvcs-tools.git
```

2. To generate API keys required for the MAP Tool, first create user accounts with [Google Maps Platform](https://developers.google.com/maps/get-started), [Esri ArcGIS Location Platform](https://developers.arcgis.com/documentation/security-and-authentication/get-started/), [Azure Maps Platform](https://learn.microsoft.com/en-us/azure/azure-maps/quick-demo-map-app#create-an-azure-maps-account).

3. Generate new API Keys for Google Maps, Esri, and Azure. Use the [Google Maps Platform](https://developers.google.com/maps/documentation/javascript/get-api-key#create-api-keys), [Esri ArcGIS Location Platform](https://developers.arcgis.com/documentation/security-and-authentication/api-key-authentication/tutorials/create-an-api-key/), [Azure Maps Platform](https://learn.microsoft.com/en-us/azure/azure-maps/quick-demo-map-app#get-the-subscription-key-for-your-account).
    - Please read the [Google Maps API Key Guidance](/docs/GoogleMaps_API_Key_Guidance.md).
    - Please read the [Esri Maps API Key Guidance](/docs/Azure_Maps_API_Key_Guidance.md)
    - Please read the [Azure Maps API Key Guidance](/docs/Esri_API_Key_Guidance.md)
    - Please read the [S3 Bucket Configuration](/docs/S3_Bucket_Configuration.md).

4. Enter your keys into the [application.properties](/fedgov-cv-map-services-proxy/src/main/resources/application.properties#L1) file (within "google.map.api.key", "azure.map.api.key", and "esri.map.api.key").

4. Using SSL vs not using SSL:

    - If using SSL certificates, you may look up instructions to generate a keystore and SSL certficiates with your certificate authority (CA) of choice. In this case, the ssl.ini and keystore files will need to be updated or replaced to copy your applicable keystore information to the image. **NOTE**: The current ssl.ini and keystore files are examples only. Please update or replace before running the following command.
    ```
    sudo docker build -t usdotfhwastol/connectedvcs-tools:<tag> --build-arg USE_SSL=true .
    ```

    - If running the tool without certificates, no changes are needed. Run the following command.
    ```
    sudo docker build -t usdotfhwastol/connectedvcs-tools:<tag> --build-arg USE_SSL=false .
    ```

### Run Image with SSL certificate
```
sudo docker run -d -p 443:443 usdotfhwastol/connectedvcs-tools:<tag>
```

### Run Image without SSL certificate
```
sudo docker run -d -p 8080:8080 usdotfhwastol/connectedvcs-tools:<tag>
```

## Access the ConnectedVCS Tools Interface

1.  In your browser, navigate to:
    - with SSL: https://127.0.0.1:443/
    - without SSL: http://127.0.0.1:8080/
