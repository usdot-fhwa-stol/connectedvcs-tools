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

1. Note: Placeholder for map API key generation.
2. Enter a map API key and username in [ISDcreator-webapp-keys](/private-resources/js/ISDcreator-webapp-keys.js).
3. Create a new Google Maps API Key using the [Google Maps Platform](https://developers.google.com/maps/documentation/javascript/get-api-key#create-api-keys).
    - Please read the [Google Maps API Key Guidance](/docs/GoogleMaps_API_Key_Guidance.md).
4. Run:
```
sudo ./build.sh
```

## Local Deployment

1. Locate `root.war`, `private-resources.war`, `isd.war`, and `tim.war` in `connectedvcs-tools/`, `connectedvcs-tools/fedgov-cv-ISDcreator-webapp/target` and `connectedvcs-tools/fedgov-cv-TIMcreator-webapp/target` respectively.
2. Deploy as servlets in conjunction with Apache Tomcat.
