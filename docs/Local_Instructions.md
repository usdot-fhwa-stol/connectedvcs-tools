# Running ConnectedVCS Tools Locally
If you are running ConnectedVCS Tools locally, you can use the following instructions:

**NOTE:** Bing Maps is now deprecated and new users are no longer allowed to create API keys. We will be migrating to a different map provider and updating this documentation soon.

## Prerequisites
ConnectedVCS Tools has been developed using Ubuntu 20.04 and Ubuntu 22.04. Further testing with other operating systems is needed before guidance is created. For the moment, please use Ubuntu 20.04 or later [Ubuntu LTS Release](https://releases.ubuntu.com/).

1. Install JDK (openjdk-8-jdk).
```
sudo apt-get install -y openjdk-8-jdk
```
2. Install Maven (3.6.3).
```
sudo apt-get install -y maven
```
3. Install gettext-base
```
sudo apt-get install -y gettext-base
```

## Clone repository

1. Clone the ConnectedVCS Tools respository:
```
git clone https://github.com/usdot-fhwa-stol/connectedvcs-tools.git
```

## Local Setup Path

1. Set up LD_LIBRARY_PATH by running:
```
echo export LD_LIBRARY_PATH="[path_to_connectedvcs-tools]/fedgov-cv-lib-asn1c/third_party_lib" >> ~/.bashrc
```
   - **NOTE**: Change the above path to point to the correct [third_party_lib path](/fedgov-cv-lib-asn1c/third_party_lib).

## Update web.xml files for use with or without SSL

### Update the web.xml based on SSL selection
- If using SSL certificates: 
```
export SECURITY_CONSTRAINT="<security-constraint><web-resource-collection><web-resource-name>Everything</web-resource-name><url-pattern>/*</url-pattern></web-resource-collection><user-data-constraint><transport-guarantee>CONFIDENTIAL</transport-guarantee></user-data-constraint></security-constraint>";
```
- If not using SSL:
```
export SECURITY_CONSTRAINT="";
```

```
envsubst '$SECURITY_CONSTRAINT' < root/WEB-INF/web.xml > /tmp/web.xml.tmp && \
mv /tmp/web.xml.tmp root/WEB-INF/web.xml && \
envsubst '$SECURITY_CONSTRAINT' < fedgov-cv-TIMcreator-webapp/src/main/webapp/WEB-INF/web.xml > /tmp/web.xml.tmp && \
mv /tmp/web.xml.tmp fedgov-cv-TIMcreator-webapp/src/main/webapp/WEB-INF/web.xml && \
envsubst '$SECURITY_CONSTRAINT' < fedgov-cv-ISDcreator-webapp/src/main/webapp/WEB-INF/web.xml > /tmp/web.xml.tmp && \
mv /tmp/web.xml.tmp fedgov-cv-ISDcreator-webapp/src/main/webapp/WEB-INF/web.xml
```

## Local Build Instructions

1. To generate API keys required for the MAP Tool, first create user accounts with [Google Maps Platform](https://developers.google.com/maps/get-started), [Esri ArcGIS Location Platform](https://developers.arcgis.com/documentation/security-and-authentication/get-started/), [Azure Maps Platform](https://learn.microsoft.com/en-us/azure/azure-maps/quick-demo-map-app#create-an-azure-maps-account).

2. Generate new API Keys for Google Maps, Esri, and Azure. Use the [Google Maps Platform](https://developers.google.com/maps/documentation/javascript/get-api-key#create-api-keys), [Esri ArcGIS Location Platform](https://developers.arcgis.com/documentation/security-and-authentication/api-key-authentication/tutorials/create-an-api-key/), [Azure Maps Platform](https://learn.microsoft.com/en-us/azure/azure-maps/quick-demo-map-app#get-the-subscription-key-for-your-account).
    - Please read the [Google Maps API Key Guidance](/docs/GoogleMaps_API_Key_Guidance.md).
    - Please read the [Esri Maps API Key Guidance](/docs/Azure_Maps_API_Key_Guidance.md)
    - Please read the [Azure Maps API Key Guidance](/docs/Esri_API_Key_Guidance.md)

3. Enter your keys into the [application.properties](/fedgov-cv-map-services-proxy/src/main/resources/application.properties#L1) file (within "google.map.api.key", "azure.map.api.key", and "esri.map.api.key").
4. Enter your Google API key to the end of the Geocomplete src link (indicated by "YOUR_API_KEY") at the [index.html](/fedgov-cv-ISDcreator-webapp/src/main/webapp/index.html)
5. Run:
```
sudo ./build.sh
```

## Local Deployment

1. Locate `root.war`, `private-resources.war`, `isd.war`, and `tim.war` in `connectedvcs-tools/`, `connectedvcs-tools/fedgov-cv-ISDcreator-webapp/target` and `connectedvcs-tools/fedgov-cv-TIMcreator-webapp/target` respectively.
2. Deploy as servlets in conjunction with Apache Tomcat.
