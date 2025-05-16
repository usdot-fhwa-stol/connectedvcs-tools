Map Tool Release Notes
----------------------------
Version 2.2.1, released May 16th, 2025
----------------------------------------

### **Summary**  
Maptool release version 2.2.1 is a hotfix release for 2.2.0.
 
### **<ins>Fixes in Release:</ins>**
  - Fixed release version of the MAP tool UI when clicked on About button under the Help Menu.
    
Version 2.2.0, released May 16th, 2025
----------------------------------------

### **Summary**  
This release focuses on replacing the MAP Tool’s base map, improving its security, and integrating the RGA Geometry Container. Key updates include switching from Bing Maps to Azure Maps, implementing the Esri API for elevation data, supporting RGA lane-level time restrictions, updating encoder pop-up warnings, incorporating RGA instructions, and updating the documentation. 

### **<ins>Enhancements in Release:</ins>** 

**Epic MAP-171: Add Geometry Container to RGA Message**  

**Summary:**  Below are the Updates made under this Epic:
  - **RGA Geometry Container Integration with JNI:** Introduced Java classes and an updated RGA JNI wrapper required to support mandatory and optional fields of the Geometry Container for RGA Geometry Container.  

  - **RGA Geometry Container Integration with Message Builder:** Implemented mandatory and optional fields for the RGA Geometry Container in the MAP Tool message builder. Test cases were updated, and bugs were resolved. 

  - **Addition of Time Restrictions to Lane Info Configuration and Encoder:** For RGA messages, lane-level time restrictions can be specified that includes days of the week, start date, end date, as well define time period as day or night. To support this, UI is updated with time restrictions in lane info configuration. 

  - **Implementation of Esri Elevation API:** Replaced Google elevation API with Esri elevation API to retrieve elevation relative to ellipsoid.   

**<ins>Pull Requests:</ins>**  
- https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/87, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/106, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/86, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/91, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/85, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/92, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/79, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/97, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/88, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/107, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/102, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/103, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/84, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/96, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/78, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/101


**Epic MAP-209: Update Base Maps**  

**Summary:** The implementation of Map Tool has been using Bing Maps as the base tile set provider, which is planned for retirement in June 2025. This epic involves migrating to Azure Maps to ensure continued platform support, improved performance. 

Map Tool also uses OpenLayers 2, which is outdated and incompatible with modern JavaScript standards. Now, we upgraded the mapping library to OpenLayers 10.4.0. The migration requires full refactoring of the map interaction logic, layers, controls, and event handling to align with the modular architecture of the latest version. This upgrade is critical for long-term maintainability, browser compatibility, and performance optimization. 

Note: Since we updated Openlayers library from v2 to v10, a mismatch is introduced with lat/lon between v2 and v10 after 7 decimal points (~1mm difference) and this happens because of changes to the internal projection library used by the two versions. 

**<ins>Pull Requests:</ins>**  
- https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/115, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/116


### **<ins>Fixes in Release:</ins>**

**Epic MAP-289: Production Bug Fixes**  

**Summary:** This Epic captured general bugs found in the production environment as below:

  - Latitude and longitude mismatch with decoded result. 
  - Fixed an issue where MAP tool was not able to encode the messages due to speed limit issues on map tool. 
  - Fixed a bug where RGA fields in reference point marker were still greyed out even when RGA is enabled.
  - Removed SPAT warning messages, SPAT related message types and hidden lane SPAT tab in lane config 
  - Removed all the SPAT and ISD references from the entire tool. 

**<ins>Pull Requests:</ins>**  
- https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/109, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/110































Version 2.1.0, released 31st, 2024
----------------------------------------

### **Summary**  
This release focuses on compliance with the 2024 J2735 standard and initial integration of J2945/A RGA messages. Updates include enhancements to the UI and new message generation capabilities.  

### **<ins>Enhancements in Release:</ins>** 

**Epic MAP-124: MAP Tool UI Improvements for J2735 2024 Standard**  

**Summary:**  Updated the MAP Tool to comply with the 2024 J2735 version. Added the following features:
  - Road Regulator ID  
  - Road Authority ID  
  - Speed Limits by lane  
- Includes bug fixes to improve functionality.  
- Updated MAP Tool Deployment Documentation.  

**<ins>Pull Requests:</ins>**  
- https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/30, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/34, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/12, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/29, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/26, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/16, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/17, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/28

**<ins>Fixed Issues:</ins>**  
- Fixed occasionally saving child map dialog box popup multiple times after clicking on save.  

**Epic MAP-148: Implement J2945/A RGA Base Layer Message with the ISD Tool**  

**Summary:** Updated the ISD Tool to support J2945/A RGA-encoded base layer messages, allowing users to generate updated JSON messages with RGA base layer details and encode them.

**<ins>Pull Requests:</ins>**  
- https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/21, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/68, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/24, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/66, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/36, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/45, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/33, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/19, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/49, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/22, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/50

**<ins>Known Issues:</ins>**  
- None  

**Epic MAP-163: Integrate J2945/A RGA Base Layer Message with ISD Message Creator UI**  

**Summary:** Enhanced the ISD Tool UI to generate both J2735 MAP and J2945/A RGA-encoded JSON messages.

**<ins>Pull Requests:</ins>**  
- https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/31, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/27, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/67

**<ins>Known Issues:</ins>**  
- None  

### **<ins>Fixes in Release:</ins>**
- N/A
  
### **<ins>Known Issues in Release:</ins>**  
- Currently, only values up to **2,147,483,647** are permitted for nodes in **RoadAuthorityID** and **MappedGeometryID**.  


Version 2.0.2, released Nov 8th, 2024
----------------------------------------

### **Summary**
This Hotfix 2.0.2 focuses on addressing tileAge data displayed on the MAPTool ISD map which was updated every time when user zoom in/out, move around a map in Map tool websites. Allowing these calls at the current rate is expected to prematurely exhaust the current Bing API key.


**<ins>Fixes:</ins>** 
- Removed tileAge API calls to bing maps. NOTE: tile age will no longer be available to users of the current MAP Tool.

Version 2.0.1, released Nov 1st, 2024
----------------------------------------

### **Summary**
This Hotfix 2.0.1 focuses on addressing security issues related to the Bing Map API usage and includes several improvements related to security of the Map tool websites. 

**<ins>Fixes:</ins>** 
- Epic 203 Implement ConnectedVCS Tool Security Fixes:  Investigation revealed that ConnectedVCS ISD Tool could no longer load map tiles from Bing due to Bing API keys being suspended due to abnormal usage. The changes included below were applied to the webappopen.connectedvcs.com and webapptest.connectedvcs.com (test site). 
   - Implementing backend monitoring of API calls 

   - Switching to Google Maps API for elevation data 

   - Adding protection from bots and common DDOS attacks 

- These security fixes apply to the open-source sites: 
   - webappopen.connectedvcs.com 
   - webapptest.connectedvcs.com (test site) 

**<ins>Pull Requests:</ins>**

- https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/39, (use session keys) 
- https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/40, (backend monitoring) 
- https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/43  (error log fix) 

 

**<ins>Known Issues:</ins>**  

- (N/A) 
 
In addition to the security fixes above, we have implemented AWS security features for all three map tool websites: 
   - webapp.connectedvcs.com (legacy site) 
   - webappopen.connectedvcs.com 
   - webapptest.connectedvcs.com (test site) 

Version 2.0.0, released April 25th, 2024
----------------------------------------

### **Summary**

In this release, the MAP (also known as ISD) tool packages have been updated to use the open-source ASN1C compiler. These updates are validated with new and enhanced unit tests. UI updates include the removal of TIM and Message Validator buttons from the landing page, along with updated text in the ASN.1 text box of the MAP tool. After removing all traces of the proprietary tool, a public GitHub repository was created to release the code. All code has been added to this new repo, the README has been updated with build instructions, and the CI/CD pipeline has been revised. Lastly, a production environment has been established, and the code is now live at https://webappopen.connectedvcs.com. 

This release of the Map Tool introduces several significant updates:

- **<ins> Integration of Open-Source ASN1C Compiler:</ins>** After integrating the open-source ASN1c compiler, the message-builder and map-encoder packages have been updated to support all mandatory and selected optional intersection fields used in the existing MAP tool.

- **<ins> Enhanced Testing:</ins>** Added and updated unit tests for message-builder and map-encoder to cover the new functionalities.

- **<ins> Server Logs:</ins>** The message builder package now logs the generated MAP message and its encoded hex string to a server-side file for further verification purposes.

- **<ins> UI Adjustments:</ins>**
    - Removed buttons for TIM and message validator from the landing page as these features are not implemented in this version.
    - Updated the ASN.1 text box in the MAP tool to reflect that it will not be populated in this release.

- **<ins> Removal of Proprietay Components:</ins>** All components of the proprietary tool have been removed.

- **<ins> Repository and CI/CD Updates:</ins>**
    - Created the connectedvcs-tools GitHub repository in preparation for public release.
    - Updated the README file with command line build instructions.
    - Enhanced GitHub Actions CI/CD workflows to include a sonar scanner for source code analysis.

- **<ins> Production Environment:</ins>** Established a production environment based on the connectedvcs-tools (MAP tool, etc.) GitHub repository. The production site is now live at https://webappopen.connectedvcs.com.

Known Issues related to this release:

- **<ins>Partial Implementation of ASN1C:</ins>** The open-source ASN1C compiler has been integrated only within the  MAP tool. Features such as TIM and Message Validator remain disabled pending further updates. The original implementation of the CVCS Tools is still available and accessible at https://www.webapp.connectedvcs.com.
