Map Tool Release Notes
----------------------------

Version 2.3.0, released Sep 19th, 2025
----------------------------------------

### **Summary**  
This release focuses on enhancements for the RGA message, addressing reported issues for the existing MAP Tool, and laying the groundwork for future feature development. The key updates include completing integration for the RGA Movements Container, RGA Way Use Container, and resolving critical bugs found in the production environment. This release also includes improvements to the security of the tool.

As of this update, the tool now fully supports SAE J2945/A RGA message creation and conversion of J2735 MAP to J2945/A RGA, and maintains support for SAE J2735 MAP message creation.

### **<ins>Enhancements in Release:</ins>** 

**Epic MAP-246: Add Movements Container to RGA Message**  
**Summary:**
  - **RGA Movements Container:** Integration for the RGA Movements Container functionality has been completed.   

**<ins>Pull Requests:</ins>**  
- https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/145 , https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/146 , https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/147 , https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/152 , https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/150 , https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/148 , https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/142 , https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/144 , https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/139 ,  https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/149 , https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/137 , https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/141 , https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/143 , https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/156

**Epic MAP-247: Add Way use Container to RGA Message**  
**Summary:**
  - **Way Use Container:** Integration for the RGA Way Use Container functionality has been completed. 

**<ins>Pull Requests:</ins>**  
- https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/158 , https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/161 , (https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/153 , https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/135 , https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/157 , https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/151 , https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/154 , (https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/155 , https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/175

**Epic MAP-289: Production Bug Fixes**  
**Summary:** This epic captured general bugs found in the production environment as follows: 
 - Fixed Null Pointer Exception when RAID is missing (MAP-331). 
 - Addressed a right U-turn maneuver not providing warnings when creating a MAP message (MAP-329). 
 - Corrected a display issue where the configuration panel extended beyond the screen (MAP-335). 
 - Addressed an error loading old MAP files without RGA data (MAP-337). 
 - Investigated a missing approach for sidewalks (MAP-332). 
 - Resolved a problem with duplicate lane IDs (MAP-346). 

**<ins>Pull Requests:</ins>**  
- https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/132, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/165, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/168

### **<ins>Fixes in Release:</ins>**
**Epic MAP-139: General Security Improvements** 
**Summary:** This Epic captured general bugs found in the production environment as below: 
 - Implement caches and zoom control to reduce tile set API calls to Azure Map service 
 - Add Cross Site Request Forgery and Referrer headers protection to tileset API 

**<ins>Pull Requests:</ins>**  
- https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/167, https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/171

Version 2.2.3, released Sep 2nd, 2025
----------------------------------------

### **Summary**  
Maptool release version 2.2.3 is a hotfix release for 2.2.0.

**MAP-349: Hotfix for ESRI Elevation API errors**  

### **<ins>Fixes in Release:</ins>**
 - Fixed a bug that caused MAP Tool to return -9999 for elevation due to the changes in ESRI API interface. 

**<ins>Pull Requests:</ins>**  
- https://github.com/usdot-fhwa-stol/connectedvcs-tools/pull/172

Version 2.2.2, released Aug 8th, 2025
----------------------------------------

### **Summary**  
Maptool release version 2.2.2 is a hotfix release for 2.2.0.
 
### **<ins>Fixes in Release:</ins>**
 - Fixed a bug that caused MAP Tool to freeze and lose the unsaved changes when deleting maneuvers from the Lane Attributes or Lane Connections table.
 - Addressed an issue where already saved maneuvers in the Lane Connections table could not be deleted.

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


Version 2.1.0, released Dec 31st, 2024
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

Version 2.0.1, released Nov 8th, 2024
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

Version 2.0.0, released April 30th, 2024
----------------------------------------

### **Summary**

In this release, MAP tool packages based on opensource ASN1c compiler is updated to support intersection fields that is validated with new and updated unit tests. UI updates include removal of TIM and Message Validator buttons along with an updated text in the ASN.1 text box of MAP tool. After removing all the traces to the proprietary tool, a public GitHub repo is created to release the code to public. All the code is added to this new repo, updated README with build instructions, and updated CI/CD pipeline. Lastly, production environment is created and the code is released at https://webappopen.connectedvcs.com. 

This release includes the several features related to the Map tool:

- After integrating the opensource [ASN1c compiler](https://github.com/usdot-fhwa-stol/connectedvcs-tools/tree/master/fedgov-cv-lib-asn1c), [message-builder](https://github.com/usdot-fhwa-stol/connectedvcs-tools/tree/master/fedgov-cv-message-builder) and [map-encoder](https://github.com/usdot-fhwa-stol/connectedvcs-tools/tree/master/fedgov-cv-mapencoder) code is updated to support all the mandatory intersection fields along with the optional fields used in existing MAP tool. 
- Added and updated unit tests in message-builder and map-encoder to test the new code.
- The message builder package is updated to record the generated MAP message and its encoded hex string in a log file that will be saved on the server for verification purposes. 
- In the UI, removed buttons to [TIM](https://webappopen.connectedvcs.com/#:~:text=the%20SDW%20warehouse.-,This%20version%20is%20disabled.%20Please%20try%20the%20legacy%20version%20here.,-Message%20Validator) and [message validator](https://webappopen.connectedvcs.com/#:~:text=into%20a%20warehouse.-,This%20version%20is%20disabled.%20Please%20try%20the%20legacy%20version%20here.) on landing page since those are not implemented in this version of the open-source tool; updated the ASN.1 text box of the ISD tool that it will not be populated in this version of the Tool.
- Removed all traces of the proprietary MAP tool. 
- Created the connectedvcs-tools GitHub repo to prepare for the public release. 
- Updated the README file with build instruction to build the packages from docker command line. 
- Updated GitHub actions CI/CD workflows to build and run sonar scanner to source code. 
- Created production environment based off connectedvcs-tools (Map tool) GitHub repository. The Production environment has been deployed with a new domain name as https://webappopen.connectedvcs.com.

Known issues in this release related to Map tool: 

- The open-source ASN1C compiler is partially replaced in the Map Tool for this release. ASN1C is only implemented within the ISD tool, while TIM and Message Validator are disabled. 

NOTE: Production URL has the master code

Production website : https://webappopen.connectedvcs.com 

GitHub: https://github.com/usdot-fhwa-stol/connectedvcs-tools 



