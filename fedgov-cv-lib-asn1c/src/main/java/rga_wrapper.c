/*
 * Copyright (C) 2025 LEIDOS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

#include <stdio.h>
#include <sys/types.h>
#include "gov_usdot_cv_rgaencoder_Encoder.h"
#include "MessageFrame.h"
#include <stdint.h>

JNIEXPORT jbyteArray JNICALL Java_gov_usdot_cv_rgaencoder_Encoder_encodeRGA(JNIEnv *env, jobject cls, jobject baseLayer, jobject geometryContainers, jobject movementsContainers, jobject wayUseContainers)
{
	printf("\n ***Inside the rga_wrapper.c file **** \n");
	uint8_t buffer[2302];
	size_t buffer_size = sizeof(buffer);
	asn_enc_rval_t ec;

	MessageFrame_t *message;

	message = calloc(1, sizeof(MessageFrame_t));
	if (!message)
	{
		return NULL;
	}

	// set default value of messageId of DSRCmsgID_roadGeometryAndAttributes
	message->messageId = 43;

	// set default Message Frame Value for RoadGeometryAndAttributes
	message->value.present = MessageFrame__value_PR_RoadGeometryAndAttributes;

	RGABaseLayer_t rgaBaseLayer;
	jclass baseLayerClass = (*env)->GetObjectClass(env, baseLayer);

	// ================== Data Set Format Version Info (Major and Minor Version) ==================
	RGADataSetFormatVersionInfo_t dataSetFmtVerInfo;

	jmethodID getMajorVersion = (*env)->GetMethodID(env, baseLayerClass, "getMajorVer", "()I");
	jint majorVersion = (*env)->CallIntMethod(env, baseLayer, getMajorVersion);

	jmethodID getMinorVersion = (*env)->GetMethodID(env, baseLayerClass, "getMinorVer", "()I");
	jint minorVersion = (*env)->CallIntMethod(env, baseLayer, getMinorVersion);

	dataSetFmtVerInfo.majorVersion = (long)majorVersion;
	dataSetFmtVerInfo.minorVersion = (long)minorVersion;

	rgaBaseLayer.dataSetFmtVerInfo = dataSetFmtVerInfo;

	// ================== Reference Point Info (Reference Point) ==================
	ReferencePointInfo_t refPointInfo;

	jmethodID getLocation = (*env)->GetMethodID(env, baseLayerClass, "getLocation", "()Lgov/usdot/cv/mapencoder/Position3D;");
	jobject locationObj = (*env)->CallObjectMethod(env, baseLayer, getLocation);
	Position3D_t location;
	jclass locationClass = (*env)->GetObjectClass(env, locationObj);

	jmethodID getLatitude = (*env)->GetMethodID(env, locationClass, "getLatitude", "()D");
	jmethodID getLongitude = (*env)->GetMethodID(env, locationClass, "getLongitude", "()D");

	jdouble latitude = (*env)->CallDoubleMethod(env, locationObj, getLatitude);
	jdouble longitude = (*env)->CallDoubleMethod(env, locationObj, getLongitude);

	location.lat = (Common_Latitude_t)((long)latitude);
	location.Long = (Common_Longitude_t)((long)longitude);

	// Check if elevation exists
	jmethodID isElevationExists = (*env)->GetMethodID(env, locationClass, "isElevationExists", "()Z");
	jboolean elevationExists = (*env)->CallBooleanMethod(env, locationObj, isElevationExists);

	if (elevationExists)
	{
		jmethodID getElevation = (*env)->GetMethodID(env, locationClass, "getElevation", "()F");
		jfloat elevation = (*env)->CallFloatMethod(env, locationObj, getElevation);

		Common_Elevation_t *dsrcElevation = calloc(1, sizeof(Common_Elevation_t));
		*dsrcElevation = (long)elevation;
		location.elevation = dsrcElevation;
	}
	else
	{
		location.elevation = NULL;
	}
	location.regional = NULL;
	
	refPointInfo.location = location;

	// ================== Reference Point Info (Time Of Calculation) ==================
	DDate_t timeOfCalculation;

	jmethodID getTimeOfCalc = (*env)->GetMethodID(env, baseLayerClass, "getTimeOfCalculation", "()Lgov/usdot/cv/rgaencoder/DDate;");
	jobject timeOfCalcObj = (*env)->CallObjectMethod(env, baseLayer, getTimeOfCalc);
	jclass timeOfCalculationClass = (*env)->GetObjectClass(env, timeOfCalcObj);

	jmethodID getYear = (*env)->GetMethodID(env, timeOfCalculationClass, "getYear", "()I");
	jmethodID getMonth = (*env)->GetMethodID(env, timeOfCalculationClass, "getMonth", "()I");
	jmethodID getDay = (*env)->GetMethodID(env, timeOfCalculationClass, "getDay", "()I");

	jint year = (*env)->CallIntMethod(env, timeOfCalcObj, getYear);
	jint month = (*env)->CallIntMethod(env, timeOfCalcObj, getMonth);
	jint day = (*env)->CallIntMethod(env, timeOfCalcObj, getDay);

	timeOfCalculation.year = (long)year;
	timeOfCalculation.month = (long)month;
	timeOfCalculation.day = (long)day;

	refPointInfo.timeOfCalculation = timeOfCalculation;

	rgaBaseLayer.refPointInfo = refPointInfo;

	// ================== Road Geometry Ref ID Info (relativeToRdAuthID) ==================
	RoadGeometryRefIDInfo_t rdGeoRefID;

	RoadAuthorityID_t *roadAuthorityID = calloc(1, sizeof(RoadAuthorityID_t));

	// Check if full RAID exists
	jmethodID isFullRdAuthIDExists = (*env)->GetMethodID(env, baseLayerClass, "isFullRdAuthIDExists", "()Z");
	jboolean fullRdAuthIDExists = (*env)->CallBooleanMethod(env, baseLayer, isFullRdAuthIDExists);
	
	// Check if relative RAID exists
	jmethodID isRelRdAuthIDExists = (*env)->GetMethodID(env, baseLayerClass, "isRelRdAuthIDExists", "()Z");
	jboolean relRdAuthIDExists = (*env)->CallBooleanMethod(env, baseLayer, isRelRdAuthIDExists);
	
	//TODO: RAID Type must be updated to OBJECT_IDENTIFIER_t.
	if (fullRdAuthIDExists)
	{
		// Get full RAID
		jmethodID getFullRdAuthID = (*env)->GetMethodID(env, baseLayerClass, "getFullRdAuthID", "()[I");
		jintArray fullRdAuthID = (*env)->CallObjectMethod(env, baseLayer, getFullRdAuthID);
		const uint32_t* fullRAID = (*env)->GetIntArrayElements(env, fullRdAuthID, NULL);

		size_t fullRAIDLen = (*env)->GetArrayLength(env, fullRdAuthID);

		OBJECT_IDENTIFIER_t *fullRAIDObjID = calloc(1, sizeof(OBJECT_IDENTIFIER_t));
		
		OBJECT_IDENTIFIER_set_arcs(fullRAIDObjID,
                               fullRAID, fullRAIDLen);

		roadAuthorityID->present = RoadAuthorityID_PR_fullRdAuthID;
		roadAuthorityID->choice.fullRdAuthID = *fullRAIDObjID;
		rdGeoRefID.rdAuthorityID = roadAuthorityID;
	} else if (relRdAuthIDExists) {
		// Get relative RAID
		jmethodID getRelRdAuthID = (*env)->GetMethodID(env, baseLayerClass, "getRelRdAuthID", "()[I");
		jintArray relRdAuthID = (*env)->CallObjectMethod(env, baseLayer, getRelRdAuthID);
		const uint32_t* relRAID = (*env)->GetIntArrayElements(env, relRdAuthID, NULL);
		
		size_t relRAIDLen  = (*env)->GetArrayLength(env, relRdAuthID);

		RELATIVE_OID_t *relRAIDObjID = calloc(1, sizeof(RELATIVE_OID_t));

		RELATIVE_OID_set_arcs(relRAIDObjID,
                               relRAID, relRAIDLen);

		// Set RAID pointer's PRESENT and CHOICE
		roadAuthorityID->present = RoadAuthorityID_PR_relRdAuthID;
		roadAuthorityID->choice.relRdAuthID = *relRAIDObjID;
		rdGeoRefID.rdAuthorityID = roadAuthorityID;
	} else {
		roadAuthorityID->present = RoadAuthorityID_PR_NOTHING;
		rdGeoRefID.rdAuthorityID = NULL;
	}
	
	rdGeoRefID.rdAuthorityID = roadAuthorityID;

	// ================== Road Geometry Ref ID Info (relativeToRdAuthID) ==================
	jmethodID getRelToRdAuthID = (*env)->GetMethodID(env, baseLayerClass, "getRelativeToRdAuthID", "()[I"); 
	jintArray relativeToRdAuthID = (*env)->CallObjectMethod(env, baseLayer, getRelToRdAuthID);
	const uint32_t *relToRdAuthID = (*env)->GetIntArrayElements(env, relativeToRdAuthID, NULL);

	size_t relToAuthIDLen = (*env)->GetArrayLength(env, relativeToRdAuthID);

	RELATIVE_OID_t *relativeOID = calloc(1, sizeof(RELATIVE_OID_t));

	RELATIVE_OID_set_arcs(relativeOID,
                               relToRdAuthID, relToAuthIDLen);

	MappedGeometryID_t mappedGeomID;
	mappedGeomID.present = MappedGeometryID_PR_relativeToRdAuthID;
	mappedGeomID.choice.relativeToRdAuthID = *relativeOID;
	
	rdGeoRefID.mappedGeomID = mappedGeomID;
	rgaBaseLayer.rdGeomRefID = rdGeoRefID;

	// ================== Data Set Content Identification (Content Version) ==================
	DataSetContentIdentification_t rgaContentIdentification;

	jmethodID getContentVer = (*env)->GetMethodID(env, baseLayerClass, "getContentVer", "()I");
	jint contentVer = (*env)->CallIntMethod(env, baseLayer, getContentVer);

	rgaContentIdentification.contentVer = (long)contentVer;

	// ================== Data Set Content Identification (Content Date Time) ==================
	DDateTime_t contentDateTime;

	jmethodID getContentDateTime = (*env)->GetMethodID(env, baseLayerClass, "getContentDateTime", "()Lgov/usdot/cv/rgaencoder/DDateTime;");
	jobject contentDateTimeObj = (*env)->CallObjectMethod(env, baseLayer, getContentDateTime);
	jclass contentDateTimeClass = (*env)->GetObjectClass(env, contentDateTimeObj);

	jmethodID getHour = (*env)->GetMethodID(env, contentDateTimeClass, "getHour", "()I");
	jmethodID getMinute = (*env)->GetMethodID(env, contentDateTimeClass, "getMinute", "()I");
	jmethodID getSecond = (*env)->GetMethodID(env, contentDateTimeClass, "getSecond", "()I");

	jint hour = (*env)->CallIntMethod(env, contentDateTimeObj, getHour);
	jint minute = (*env)->CallIntMethod(env, contentDateTimeObj, getMinute);
	jint second = (*env)->CallIntMethod(env, contentDateTimeObj, getSecond);

	DHour_t *ddtHour = calloc(1, sizeof(DHour_t));
	DMinute_t *ddtMinute = calloc(1, sizeof(DMinute_t));
	DSecond_t *ddtSecond = calloc(1, sizeof(DSecond_t));
	DYear_t *ddtYear = calloc(1,sizeof(DYear_t));
	DMonth_t *ddtMonth = calloc(1, sizeof(DMonth_t));
	DDay_t *ddtDay = calloc(1, sizeof(DDay_t));
	DOffset_t *ddtOffset = calloc(1, sizeof(DOffset_t));

	*ddtYear = (long)year;
	*ddtMonth = (long)month;
	*ddtDay = (long)day;
	*ddtHour = (long)hour;
	*ddtMinute = (long)minute;
	*ddtSecond = (long)second;
	*ddtOffset = (long)0;
	
	contentDateTime.year = ddtYear;
	contentDateTime.month = ddtMonth;
	contentDateTime.day = ddtDay;
	contentDateTime.hour = ddtHour;
	contentDateTime.minute = ddtMinute;
	contentDateTime.second = ddtSecond;
	contentDateTime.offset = ddtOffset;

	rgaContentIdentification.contentDateTime = contentDateTime;
	rgaBaseLayer.rgaContentIdentification = rgaContentIdentification;

	rgaBaseLayer.partitionInfo = NULL;

	// Set BaseLayer, RGADataSet, and MessageFrame
	RGADataSet_t rgaDataSet;
	memset(&rgaDataSet, 0, sizeof(RGADataSet_t));

	rgaDataSet.baseLayer = rgaBaseLayer;

	if (geometryContainers != NULL)
	{
		// Extracting and Setting Geometry Container
		jclass geometryContainersList = (*env)->GetObjectClass(env, geometryContainers);
		jmethodID geometryContainersListSizeMethod = (*env)->GetMethodID(env, geometryContainersList, "size", "()I");
		jmethodID geometryContainersListGetMethod = (*env)->GetMethodID(env, geometryContainersList, "get", "(I)Ljava/lang/Object;");

		jint geometryContainersListSize = (*env)->CallIntMethod(env, geometryContainers, geometryContainersListSizeMethod);

		if (geometryContainersListSize > 0)
		{
			rgaDataSet.geometryContainer = calloc(1, sizeof(*rgaDataSet.geometryContainer));
			printf("Encoding Geometry Containers \n");

			for (int gIndex = 0; gIndex < geometryContainersListSize; gIndex++)
			{
				RGAGeometryLayers_t *geometryLayer = calloc(1, sizeof(RGAGeometryLayers_t));
				jobject geometryContainerObject = (*env)->CallObjectMethod(env, geometryContainers, geometryContainersListGetMethod, gIndex);
				jclass geometryContainerClass = (*env)->GetObjectClass(env, geometryContainerObject);

				// Retrieve geometryContainer-ID
				jmethodID getGeometryContainerID = (*env)->GetMethodID(env, geometryContainerClass, "getGeometryContainerID", "()I");
				jint geometryContainerID = (*env)->CallIntMethod(env, geometryContainerObject, getGeometryContainerID);

				geometryLayer->geometryContainer_ID = geometryContainerID;

				// Populate the geometryContainer_Value based on the containerID
				switch (geometryContainerID)
				{
				case APPROACH_GEOMETRY_LAYER_ID: // ApproachGeometryLayer
					geometryLayer->geometryContainer_Value.present = RGAGeometryLayers__geometryContainer_Value_PR_ApproachGeometryLayer;

					// Retrieving the ApproachGeometryLayer object
					jmethodID getApproachGeometryLayerMethod = (*env)->GetMethodID(env, geometryContainerClass, "getApproachGeometryLayer", "()Lgov/usdot/cv/rgaencoder/ApproachGeometryLayer;");
					jobject approachGeometryLayerObj = (*env)->CallObjectMethod(env, geometryContainerObject, getApproachGeometryLayerMethod);

					// Populating ApproachGeometryLayer_t
					ApproachGeometryLayer_t *approachGeometryLayer = calloc(1, sizeof(ApproachGeometryLayer_t));

					// Populating the approachGeomApproachSet from the ApproachGeometryLayer object
					jclass approachGeometryLayerClass = (*env)->GetObjectClass(env, approachGeometryLayerObj);
					jmethodID getApproachGeomApproachSetMethod = (*env)->GetMethodID(env, approachGeometryLayerClass, "getApproachGeomApproachSet", "()Ljava/util/List;");
					jobject approachGeomApproachSetList = (*env)->CallObjectMethod(env, approachGeometryLayerObj, getApproachGeomApproachSetMethod);

					jclass approachGeomApproachSetClass = (*env)->GetObjectClass(env, approachGeomApproachSetList);
					jmethodID approachGeomApproachSetSizeMethod = (*env)->GetMethodID(env, approachGeomApproachSetClass, "size", "()I");
					jmethodID approachGeomApproachSetGetMethod = (*env)->GetMethodID(env, approachGeomApproachSetClass, "get", "(I)Ljava/lang/Object;");

					jint approachGeomApproachSetSize = (*env)->CallIntMethod(env, approachGeomApproachSetList, approachGeomApproachSetSizeMethod);

					for (jint aIndex = 0; aIndex < approachGeomApproachSetSize; aIndex++)
					{
						jobject individualApproachGeometryInfoObj = (*env)->CallObjectMethod(env, approachGeomApproachSetList, approachGeomApproachSetGetMethod, aIndex);
						jclass individualApproachGeometryInfoClass = (*env)->GetObjectClass(env, individualApproachGeometryInfoObj);

						jmethodID getApproachIDMethod = (*env)->GetMethodID(env, individualApproachGeometryInfoClass, "getApproachID", "()I");
						jint approachID = (*env)->CallIntMethod(env, individualApproachGeometryInfoObj, getApproachIDMethod);

						IndividualApproachGeometryInfo_t *approachInfo = calloc(1, sizeof(IndividualApproachGeometryInfo_t));

						approachInfo->approachID = approachID;

						// Get the WayTypeIDSet object
						jmethodID getWayTypesSetMethod = (*env)->GetMethodID(env, individualApproachGeometryInfoClass, "getApproachWayTypeIDSet", "()Ljava/util/List;");
						jobject wayTypesSetListObj = (*env)->CallObjectMethod(env, individualApproachGeometryInfoObj, getWayTypesSetMethod); // this is a list

						approachInfo->wayTypesSet = calloc(1, sizeof(struct IndividualApproachGeometryInfo__wayTypesSet));
						
						// Check to see if the WayTypeIDSet object exists
						if (wayTypesSetListObj != NULL) {

							jclass wayTypeIDSetListClass = (*env)->GetObjectClass(env, wayTypesSetListObj);


							jmethodID wayTypeIDSetSizeMethod = (*env)->GetMethodID(env, wayTypeIDSetListClass, "size", "()I");
							jmethodID wayTypeIDSetGetMethod = (*env)->GetMethodID(env, wayTypeIDSetListClass, "get", "(I)Ljava/lang/Object;");
							jint wayTypeIDSetSize = (*env)->CallIntMethod(env, wayTypesSetListObj, wayTypeIDSetSizeMethod);

							for (jint tIndex = 0; tIndex < wayTypeIDSetSize; tIndex++)
							{
								jobject wayTypeIDSetObj = (*env)->CallObjectMethod(env, wayTypesSetListObj, wayTypeIDSetGetMethod, tIndex);
								ApproachWayTypeIDSet_t *wayTypeIDSet = calloc(1, sizeof(ApproachWayTypeIDSet_t));

								jclass approachWayTypeIDSetClass = (*env)->GetObjectClass(env, wayTypeIDSetObj);
							
								// WayType
								jmethodID getWayTypeMethod = (*env)->GetMethodID(env, approachWayTypeIDSetClass, "getWayType", "()Lgov/usdot/cv/rgaencoder/WayType;");
								jobject wayTypeObj = (*env)->CallObjectMethod(env, wayTypeIDSetObj, getWayTypeMethod);
								
								// WayType Value
								jclass wayTypeClass = (*env)->GetObjectClass(env, wayTypeObj);
								jmethodID getWayTypeValueMethod = (*env)->GetMethodID(env, wayTypeClass, "getWayTypeValue", "()J");
								jlong wayTypeValue = (*env)->CallLongMethod(env, wayTypeObj, getWayTypeValueMethod);
							
								wayTypeIDSet->wayType = (WayType_t)((long)wayTypeValue);

								// wayIDSet
								jmethodID getWayIDSetMethod = (*env)->GetMethodID(env, approachWayTypeIDSetClass, "getWayIDSet", "()Ljava/util/List;");
								jobject wayIDSetListObj = (*env)->CallObjectMethod(env, wayTypeIDSetObj, getWayIDSetMethod); // this is a list
								jclass wayIDSetListClass = (*env)->GetObjectClass(env, wayIDSetListObj);
							
								jmethodID wayIDSetSizeMethod = (*env)->GetMethodID(env, wayIDSetListClass, "size", "()I");
								jmethodID wayTypeIDSetGetMethod = (*env)->GetMethodID(env, wayIDSetListClass, "get", "(I)Ljava/lang/Object;");
							
								jint wayTypeIDSetSize = (*env)->CallIntMethod(env, wayIDSetListObj, wayIDSetSizeMethod);
							
								for (jint tIndex = 0; tIndex < wayTypeIDSetSize; tIndex++)
								{						
									jobject laneIDObject = (*env)->CallObjectMethod(env, wayIDSetListObj, wayTypeIDSetGetMethod, tIndex);
									jclass laneIDClass = (*env)->GetObjectClass(env, laneIDObject);
							
									jmethodID getLaneIDMethod = (*env)->GetMethodID(env, laneIDClass, "longValue", "()J");
							
									jlong laneIDLong = (*env)->CallLongMethod(env, laneIDObject, getLaneIDMethod);
							
									LaneID_t *laneIDApproach = calloc(1, sizeof(LaneID_t));
									*laneIDApproach = (LaneID_t)laneIDLong; 
									ASN_SEQUENCE_ADD(&wayTypeIDSet->wayIDSet.list, laneIDApproach);
								}

								ASN_SEQUENCE_ADD(&approachInfo->wayTypesSet->list, wayTypeIDSet);
							}
						}
						else {
							approachInfo->wayTypesSet = NULL;
						}
						
						// Adding to approachGeomApproachSet
						ASN_SEQUENCE_ADD(&approachGeometryLayer->approachGeomApproachSet.list, approachInfo);
					}

					geometryLayer->geometryContainer_Value.choice.ApproachGeometryLayer = *approachGeometryLayer;
					break;
				case MOTOR_VEHICLE_LANE_GEOMETRY_LAYER_ID: // MotorVehicleLaneGeometryLayer
					geometryLayer->geometryContainer_Value.present = RGAGeometryLayers__geometryContainer_Value_PR_MotorVehicleLaneGeometryLayer;

					// Retrieving the MotorVehicleLaneGeometryLayer object
					jmethodID getMotorVehicleLaneGeometryLayerMethod = (*env)->GetMethodID(env, geometryContainerClass, "getMotorVehicleLaneGeometryLayer", "()Lgov/usdot/cv/rgaencoder/MotorVehicleLaneGeometryLayer;");
					jobject motorVehicleLaneGeometryLayerObj = (*env)->CallObjectMethod(env, geometryContainerObject, getMotorVehicleLaneGeometryLayerMethod);

					// Populating MotorVehicleLaneGeometryLayer_t
					MotorVehicleLaneGeometryLayer_t *motorVehicleLaneGeometryLayer = calloc(1, sizeof(MotorVehicleLaneGeometryLayer_t));

					// Populating the MotorVehicleLaneGeometryLayer laneGeomLaneSet from the MotorVehicleLaneGeometryLayer object
					jclass motorVehicleLaneGeometryLayerClass = (*env)->GetObjectClass(env, motorVehicleLaneGeometryLayerObj);
					jmethodID getLaneGeomLaneSetMethod = (*env)->GetMethodID(env, motorVehicleLaneGeometryLayerClass, "getLaneGeomLaneSet", "()Ljava/util/List;");
					jobject laneGeomLaneSetList = (*env)->CallObjectMethod(env, motorVehicleLaneGeometryLayerObj, getLaneGeomLaneSetMethod);

					jclass laneGeomLaneSetClass = (*env)->GetObjectClass(env, laneGeomLaneSetList);
					jmethodID laneGeomLaneSetSizeMethod = (*env)->GetMethodID(env, laneGeomLaneSetClass, "size", "()I");
					jmethodID laneGeomLaneSetGetMethod = (*env)->GetMethodID(env, laneGeomLaneSetClass, "get", "(I)Ljava/lang/Object;");

					jint laneGeomLaneSetSize = (*env)->CallIntMethod(env, laneGeomLaneSetList, laneGeomLaneSetSizeMethod);

					for (jint lIndex = 0; lIndex < laneGeomLaneSetSize; lIndex++)
					{
						jobject indvMtrVehLaneGeometryInfoObj = (*env)->CallObjectMethod(env, laneGeomLaneSetList, laneGeomLaneSetGetMethod, lIndex);
						jclass indvMtrVehLaneGeometryInfoClass = (*env)->GetObjectClass(env, indvMtrVehLaneGeometryInfoObj);

						jmethodID getLaneIDMethod = (*env)->GetMethodID(env, indvMtrVehLaneGeometryInfoClass, "getLaneID", "()I");
						jint laneID = (*env)->CallIntMethod(env, indvMtrVehLaneGeometryInfoObj, getLaneIDMethod);

						IndvMtrVehLaneGeometryInfo_t *indvMtrVehLaneGeometryInfo = calloc(1, sizeof(IndvMtrVehLaneGeometryInfo_t));
						indvMtrVehLaneGeometryInfo->laneID = laneID;

						jmethodID getLaneConstructorTypeMethod = (*env)->GetMethodID(env, indvMtrVehLaneGeometryInfoClass, "getLaneConstructorType", "()Lgov/usdot/cv/rgaencoder/LaneConstructorType;");
						jobject laneConstructorTypeObj = (*env)->CallObjectMethod(env, indvMtrVehLaneGeometryInfoObj, getLaneConstructorTypeMethod);

						populateLaneConstructorType(env, laneConstructorTypeObj, &(indvMtrVehLaneGeometryInfo->laneConstructorType));

						// Check to see if timeRestrictions in MotorVehicleLaneGeometryInfo exists
						jmethodID getMtrTimeRestrictionsMethod = (*env)->GetMethodID(env, indvMtrVehLaneGeometryInfoClass, "getTimeRestrictions", "()Lgov/usdot/cv/rgaencoder/RGATimeRestrictions;");
						jobject mtrTimeRestrictionsObj = (*env)->CallObjectMethod(env, indvMtrVehLaneGeometryInfoObj, getMtrTimeRestrictionsMethod);

						if (mtrTimeRestrictionsObj != NULL)
						{
							RGATimeRestrictions_t *mtrTimeRestrictions = calloc(1, sizeof(RGATimeRestrictions_t));
							populateTimeRestrictions(env, mtrTimeRestrictionsObj, mtrTimeRestrictions);
							indvMtrVehLaneGeometryInfo->timeRestrictions = mtrTimeRestrictions;
						}
						else
						{
							indvMtrVehLaneGeometryInfo->timeRestrictions = NULL;
						}

						ASN_SEQUENCE_ADD(&motorVehicleLaneGeometryLayer->laneGeomLaneSet.list, indvMtrVehLaneGeometryInfo);
					}

					geometryLayer->geometryContainer_Value.choice.MotorVehicleLaneGeometryLayer = *motorVehicleLaneGeometryLayer;
					break;

				case BICYCLE_LANE_GEOMETRY_LAYER_ID:
					// Handle BicycleLaneGeometryLayer
					geometryLayer->geometryContainer_Value.present = RGAGeometryLayers__geometryContainer_Value_PR_BicycleLaneGeometryLayer;

					// Retrieving the BicycleLaneGeometryLayer object
					jmethodID getBicycleLaneGeometryLayerMethod = (*env)->GetMethodID(env, geometryContainerClass, "getBicycleLaneGeometryLayer", "()Lgov/usdot/cv/rgaencoder/BicycleLaneGeometryLayer;");
					jobject bicycleLaneGeometryLayerObj = (*env)->CallObjectMethod(env, geometryContainerObject, getBicycleLaneGeometryLayerMethod);

					// Populating BicycleLaneGeometryLayer_t
					BicycleLaneGeometryLayer_t *bicycleLaneGeometryLayer = calloc(1, sizeof(BicycleLaneGeometryLayer_t));

					// Retrieve and populate BicycleLaneGeometryLayer laneGeomLaneSet
					jclass bicycleLaneGeometryLayerClass = (*env)->GetObjectClass(env, bicycleLaneGeometryLayerObj);
					jmethodID getBicycleLaneGeomLaneSetMethod = (*env)->GetMethodID(env, bicycleLaneGeometryLayerClass, "getLaneGeomLaneSet", "()Ljava/util/List;");
					jobject bicycleLaneGeomLaneSetList = (*env)->CallObjectMethod(env, bicycleLaneGeometryLayerObj, getBicycleLaneGeomLaneSetMethod);

					jclass bicycleLaneGeomLaneSetClass = (*env)->GetObjectClass(env, bicycleLaneGeomLaneSetList);
					jmethodID bicycleLaneGeomLaneSetSizeMethod = (*env)->GetMethodID(env, bicycleLaneGeomLaneSetClass, "size", "()I");
					jmethodID bicycleLaneGeomLaneSetGetMethod = (*env)->GetMethodID(env, bicycleLaneGeomLaneSetClass, "get", "(I)Ljava/lang/Object;");

					jint bicycleLaneGeomLaneSetSize = (*env)->CallIntMethod(env, bicycleLaneGeomLaneSetList, bicycleLaneGeomLaneSetSizeMethod);

					for (jint bIndex = 0; bIndex < bicycleLaneGeomLaneSetSize; bIndex++)
					{
						jobject indvBikeLaneGeometryInfoObj = (*env)->CallObjectMethod(env, bicycleLaneGeomLaneSetList, bicycleLaneGeomLaneSetGetMethod, bIndex);
						jclass indvBikeLaneGeometryInfoClass = (*env)->GetObjectClass(env, indvBikeLaneGeometryInfoObj);

						jmethodID getBikeLaneIDMethod = (*env)->GetMethodID(env, indvBikeLaneGeometryInfoClass, "getLaneID", "()I");
						jint bikeLaneID = (*env)->CallIntMethod(env, indvBikeLaneGeometryInfoObj, getBikeLaneIDMethod);

						IndvBikeLaneGeometryInfo_t *indvBikeLaneGeometryInfo = calloc(1, sizeof(IndvBikeLaneGeometryInfo_t));
						indvBikeLaneGeometryInfo->laneID = bikeLaneID;

						// Retrieve and set laneConstructorType
						jmethodID getBikeLaneConstructorTypeMethod = (*env)->GetMethodID(env, indvBikeLaneGeometryInfoClass, "getLaneConstructorType", "()Lgov/usdot/cv/rgaencoder/LaneConstructorType;");
						jobject bikeLaneConstructorTypeObj = (*env)->CallObjectMethod(env, indvBikeLaneGeometryInfoObj, getBikeLaneConstructorTypeMethod);

						populateLaneConstructorType(env, bikeLaneConstructorTypeObj, &(indvBikeLaneGeometryInfo->laneConstructorType));

						// Check to see if timeRestrictions in BikeLaneGeometryInfo exists
						jmethodID getBikeTimeRestrictionsMethod = (*env)->GetMethodID(env, indvBikeLaneGeometryInfoClass, "getTimeRestrictions", "()Lgov/usdot/cv/rgaencoder/RGATimeRestrictions;");
						jobject bikeTimeRestrictionsObj = (*env)->CallObjectMethod(env, indvBikeLaneGeometryInfoObj, getBikeTimeRestrictionsMethod);

						if (bikeTimeRestrictionsObj != NULL)
						{
							RGATimeRestrictions_t *bikeTimeRestrictions = calloc(1, sizeof(RGATimeRestrictions_t));
							populateTimeRestrictions(env, bikeTimeRestrictionsObj, bikeTimeRestrictions);
							indvBikeLaneGeometryInfo->timeRestrictions = bikeTimeRestrictions;
						}
						else
						{
							indvBikeLaneGeometryInfo->timeRestrictions = NULL;
						}

						ASN_SEQUENCE_ADD(&bicycleLaneGeometryLayer->laneGeomLaneSet.list, indvBikeLaneGeometryInfo);
					}
					geometryLayer->geometryContainer_Value.choice.BicycleLaneGeometryLayer = *bicycleLaneGeometryLayer;

					break;

				case CROSSWALK_LANE_GEOMETRY_LAYER_ID:
					// Handle CrosswalkLaneGeometryLayer
					geometryLayer->geometryContainer_Value.present = RGAGeometryLayers__geometryContainer_Value_PR_CrosswalkLaneGeometryLayer;

					// Retrieving the CrosswalkLaneGeometryLayer object
					jmethodID getCrosswalkLaneGeometryLayerMethod = (*env)->GetMethodID(env, geometryContainerClass, "getCrosswalkLaneGeometryLayer", "()Lgov/usdot/cv/rgaencoder/CrosswalkLaneGeometryLayer;");
					jobject crosswalkLaneGeometryLayerObj = (*env)->CallObjectMethod(env, geometryContainerObject, getCrosswalkLaneGeometryLayerMethod);

					// Populating CrosswalkLaneGeometryLayer_t
					CrosswalkLaneGeometryLayer_t *crosswalkLaneGeometryLayer = calloc(1, sizeof(CrosswalkLaneGeometryLayer_t));

					// Retrieve and populate CrosswalkLaneGeometryLayer laneGeomLaneSet
					jclass crosswalkLaneGeometryLayerClass = (*env)->GetObjectClass(env, crosswalkLaneGeometryLayerObj);
					jmethodID getCrosswalkLaneGeomLaneSetMethod = (*env)->GetMethodID(env, crosswalkLaneGeometryLayerClass, "getLaneGeomLaneSet", "()Ljava/util/List;");
					jobject crossWalkLaneGeomLaneSetList = (*env)->CallObjectMethod(env, crosswalkLaneGeometryLayerObj, getCrosswalkLaneGeomLaneSetMethod);

					jclass crosswalkLaneGeomLaneSetClass = (*env)->GetObjectClass(env, crossWalkLaneGeomLaneSetList);
					jmethodID crosswalkLaneGeomLaneSetSizeMethod = (*env)->GetMethodID(env, crosswalkLaneGeomLaneSetClass, "size", "()I");
					jmethodID crosswalkLaneGeomLaneSetGetMethod = (*env)->GetMethodID(env, crosswalkLaneGeomLaneSetClass, "get", "(I)Ljava/lang/Object;");

					jint crosswalkLaneGeomLaneSetSize = (*env)->CallIntMethod(env, crossWalkLaneGeomLaneSetList, crosswalkLaneGeomLaneSetSizeMethod);

					for (jint cIndex = 0; cIndex < crosswalkLaneGeomLaneSetSize; cIndex++)
					{
						jobject indvCrosswalkLaneGeometryInfoObj = (*env)->CallObjectMethod(env, crossWalkLaneGeomLaneSetList, crosswalkLaneGeomLaneSetGetMethod, cIndex);
						jclass indvCrosswalkLaneGeometryInfoClass = (*env)->GetObjectClass(env, indvCrosswalkLaneGeometryInfoObj);

						jmethodID getCrossWalkLaneIDMethod = (*env)->GetMethodID(env, indvCrosswalkLaneGeometryInfoClass, "getLaneID", "()I");
						jint crosswalkLaneID = (*env)->CallIntMethod(env, indvCrosswalkLaneGeometryInfoObj, getCrossWalkLaneIDMethod);

						IndvCrosswalkLaneGeometryInfo_t *indvCrosswalkLaneGeometryInfo = calloc(1, sizeof(IndvCrosswalkLaneGeometryInfo_t));
						indvCrosswalkLaneGeometryInfo->laneID = crosswalkLaneID;

						// Retrieve and set laneConstructorType
						jmethodID getCrosswalkLaneConstructorTypeMethod = (*env)->GetMethodID(env, indvCrosswalkLaneGeometryInfoClass, "getLaneConstructorType", "()Lgov/usdot/cv/rgaencoder/LaneConstructorType;");
						jobject crosswalkLaneConstructorTypeObj = (*env)->CallObjectMethod(env, indvCrosswalkLaneGeometryInfoObj, getCrosswalkLaneConstructorTypeMethod);

						populateLaneConstructorType(env, crosswalkLaneConstructorTypeObj, &indvCrosswalkLaneGeometryInfo->laneConstructorType);

						// Check to see if timeRestrictions in CrosswalkLaneGeometryInfo exists
						jmethodID getCrosswalkTimeRestrictionsMethod = (*env)->GetMethodID(env, indvCrosswalkLaneGeometryInfoClass, "getTimeRestrictions", "()Lgov/usdot/cv/rgaencoder/RGATimeRestrictions;");
						jobject crosswalkTimeRestrictionsObj = (*env)->CallObjectMethod(env, indvCrosswalkLaneGeometryInfoObj, getCrosswalkTimeRestrictionsMethod);

						if (crosswalkTimeRestrictionsObj != NULL)
						{
							RGATimeRestrictions_t *crosswalkTimeRestrictions = calloc(1, sizeof(RGATimeRestrictions_t));
							populateTimeRestrictions(env, crosswalkTimeRestrictionsObj, crosswalkTimeRestrictions);
							indvCrosswalkLaneGeometryInfo->timeRestrictions = crosswalkTimeRestrictions;
						}
						else
						{
							indvCrosswalkLaneGeometryInfo->timeRestrictions = NULL;
						}

						ASN_SEQUENCE_ADD(&crosswalkLaneGeometryLayer->laneGeomLaneSet.list, indvCrosswalkLaneGeometryInfo);
					}
					geometryLayer->geometryContainer_Value.choice.CrosswalkLaneGeometryLayer = *crosswalkLaneGeometryLayer;
					break;

				default:
					// Handle unknown ID
					geometryLayer->geometryContainer_Value.present = RGAGeometryLayers__geometryContainer_Value_PR_NOTHING;
					break;
				}

				ASN_SEQUENCE_ADD(&rgaDataSet.geometryContainer->list, geometryLayer);
			}
		}
	}
	
	if(movementsContainers != NULL) {
		// Extracting and Setting Movements Containers
		jclass movementsContainersList = (*env)->GetObjectClass(env, movementsContainers);
		jmethodID movementsContainersListSizeMethod = (*env)->GetMethodID(env, movementsContainersList, "size", "()I");
		jmethodID movementsContainersListGetMethod = (*env)->GetMethodID(env, movementsContainersList, "get", "(I)Ljava/lang/Object;");

		jint movementsContainersListSize = (*env)->CallIntMethod(env, movementsContainers, movementsContainersListSizeMethod);

		if (movementsContainersListSize > 0)
		{
			rgaDataSet.movementsContainer = calloc(1, sizeof(*rgaDataSet.movementsContainer));

			for (int mIndex = 0; mIndex < movementsContainersListSize; mIndex++)
			{
				RGAMovementsLayers_t *movementsLayer = calloc(1, sizeof(RGAMovementsLayers_t));
				jobject movementsContainerObject = (*env)->CallObjectMethod(env, movementsContainers, movementsContainersListGetMethod, mIndex);
				jclass movementsContainerClass = (*env)->GetObjectClass(env, movementsContainerObject);

				// Retrieve geometryContainer-ID
				jmethodID getMovementsContainerID = (*env)->GetMethodID(env, movementsContainerClass, "getMovementsContainerId", "()I");
				jint movementsContainerID = (*env)->CallIntMethod(env, movementsContainerObject, getMovementsContainerID);

				movementsLayer->movementsContainer_ID = movementsContainerID;

				// Populate the movementsContainer_Value based on the containerID
				switch (movementsContainerID)
				{
				case MTR_VEH_LANE_DIRECTION_OF_TRAVEL_LAYER_ID: // MtrVehLaneDirectionOfTravelLayer
					movementsLayer->movementsContainer_Value.present = RGAMovementsLayers__movementsContainer_Value_PR_MtrVehLaneDirectionOfTravelLayer;

					// Retrieving the MtrVehLaneDirectionOfTravelLayer object
					jmethodID getMtrVehLaneDirectionOfTravelLayerMethod = (*env)->GetMethodID(env, movementsContainerClass, "getMtrVehLaneDirectionOfTravelLayer", "()Lgov/usdot/cv/rgaencoder/MtrVehLaneDirectionOfTravelLayer;");
					jobject mtrVehLaneDirectionOfTravelLayerObj = (*env)->CallObjectMethod(env, movementsContainerObject, getMtrVehLaneDirectionOfTravelLayerMethod);

					// Populating MtrVehLaneDirectionOfTravelLayer_t
					MtrVehLaneDirectionOfTravelLayer_t *mtrVehLaneDirectionOfTravelLayer = calloc(1, sizeof(MtrVehLaneDirectionOfTravelLayer_t));

					// Populating the laneDirOfTravelLaneSet from the MtrVehLaneDirectionOfTravelLayer object
					jclass mtrVehLaneDirectionOfTravelLayerClass = (*env)->GetObjectClass(env, mtrVehLaneDirectionOfTravelLayerObj);
					jmethodID getLaneDirOfTravelLaneSetMethod = (*env)->GetMethodID(env, mtrVehLaneDirectionOfTravelLayerClass, "getLaneDirOfTravelLaneSet", "()Ljava/util/List;");
					jobject laneDirOfTravelLaneSetList = (*env)->CallObjectMethod(env, mtrVehLaneDirectionOfTravelLayerObj, getLaneDirOfTravelLaneSetMethod);

					jclass laneDirOfTravelLaneSetClass = (*env)->GetObjectClass(env, laneDirOfTravelLaneSetList);
					jmethodID laneDirOfTravelLaneSetSizeMethod = (*env)->GetMethodID(env, laneDirOfTravelLaneSetClass, "size", "()I");
					jmethodID laneDirOfTravelLaneSetGetMethod = (*env)->GetMethodID(env, laneDirOfTravelLaneSetClass, "get", "(I)Ljava/lang/Object;");

					jint laneDirOfTravelLaneSetSize = (*env)->CallIntMethod(env, laneDirOfTravelLaneSetList, laneDirOfTravelLaneSetSizeMethod);

					for (jint lIndex = 0; lIndex < laneDirOfTravelLaneSetSize; lIndex++)
					{
						jobject individualWayDirectionsOfTravelObj = (*env)->CallObjectMethod(env, laneDirOfTravelLaneSetList, laneDirOfTravelLaneSetGetMethod, lIndex);
						jclass individualWayDirectionsOfTravelClass = (*env)->GetObjectClass(env, individualWayDirectionsOfTravelObj);

						jmethodID getIndividualWayDirectionsOfTravelLaneIDMethod = (*env)->GetMethodID(env, individualWayDirectionsOfTravelClass, "getWayID", "()I");
						jint individualWayDirectionsOfTravelLaneID = (*env)->CallIntMethod(env, individualWayDirectionsOfTravelObj, getIndividualWayDirectionsOfTravelLaneIDMethod);

						IndividualWayDirectionsOfTravel_t *indWayDirOfTravel = calloc(1, sizeof(IndividualWayDirectionsOfTravel_t));

						indWayDirOfTravel->wayID = individualWayDirectionsOfTravelLaneID;

						// Get the directionsOfTravelSet
						jmethodID getDirectionsOfTravelSetMethod = (*env)->GetMethodID(env, individualWayDirectionsOfTravelClass, "getDirectionsOfTravelSet", "()Ljava/util/List;");
						jobject directionsOfTravelSetList = (*env)->CallObjectMethod(env, individualWayDirectionsOfTravelObj, getDirectionsOfTravelSetMethod); // this is a list

						if (directionsOfTravelSetList != NULL) {
							jclass directionsOfTravelSetClass = (*env)->GetObjectClass(env, directionsOfTravelSetList);
							jmethodID directionsOfTravelSetSizeMethod = (*env)->GetMethodID(env, directionsOfTravelSetClass, "size", "()I");
							jmethodID directionsOfTravelSetGetMethod = (*env)->GetMethodID(env, directionsOfTravelSetClass, "get", "(I)Ljava/lang/Object;");
	
							jint directionsOfTravelSetSize = (*env)->CallIntMethod(env, directionsOfTravelSetList, directionsOfTravelSetSizeMethod);
	
							for (jint dIndex = 0; dIndex < directionsOfTravelSetSize; dIndex++)
							{
								jobject wayDirectionOfTravelInfoObj = (*env)->CallObjectMethod(env, directionsOfTravelSetList, directionsOfTravelSetGetMethod, dIndex);
								jclass wayDirectionOfTravelInfoClass = (*env)->GetObjectClass(env, wayDirectionOfTravelInfoObj);
	
								jmethodID getWayNodeDirectionOfTravelMethod = (*env)->GetMethodID(env, wayDirectionOfTravelInfoClass, "getWayNodeDirectionOfTravel", "()S");
								jshort wayNodeDirectionOfTravelShort = (*env)->CallShortMethod(env, wayDirectionOfTravelInfoObj, getWayNodeDirectionOfTravelMethod);
	
								WayDirectionOfTravelInfo_t *wayDirectionOfTravelInfo = calloc(1, sizeof(WayDirectionOfTravelInfo_t));
	
								WayNodeDirOfTravel_t wayNodeDirOfTravel;
								wayNodeDirOfTravel.buf = (uint8_t *)calloc(1, sizeof(uint8_t));
								*wayNodeDirOfTravel.buf = (uint8_t)wayNodeDirectionOfTravelShort;
								wayNodeDirOfTravel.size = 1;
								wayNodeDirOfTravel.bits_unused = 6;
	
								wayDirectionOfTravelInfo->wayNodeDirectionOfTravel = wayNodeDirOfTravel;
	
								// Check to see if timeRestrictions in WayDirectionOfTravelInfo exists
								jmethodID getWayDirectionOfTravelInfoTimeRestrictionsMethod = (*env)->GetMethodID(env, wayDirectionOfTravelInfoClass, "getTimeRestrictions", "()Lgov/usdot/cv/rgaencoder/RGATimeRestrictions;");
								jobject wayDirectionOfTravelInfoTimeRestrictionsObj = (*env)->CallObjectMethod(env, wayDirectionOfTravelInfoObj, getWayDirectionOfTravelInfoTimeRestrictionsMethod);
	
								if (wayDirectionOfTravelInfoTimeRestrictionsObj != NULL)
								{
									RGATimeRestrictions_t *wayDirectionOfTravelInfoTimeRestrictions = calloc(1, sizeof(RGATimeRestrictions_t));
									populateTimeRestrictions(env, wayDirectionOfTravelInfoTimeRestrictionsObj, wayDirectionOfTravelInfoTimeRestrictions);
									wayDirectionOfTravelInfo->timeRestrictions = wayDirectionOfTravelInfoTimeRestrictions;
								}
								else
								{
									wayDirectionOfTravelInfo->timeRestrictions = NULL;
								}
								ASN_SEQUENCE_ADD(&indWayDirOfTravel->directionsOfTravelSet.list, wayDirectionOfTravelInfo);
							} // dIndex for loop ends
							ASN_SEQUENCE_ADD(&mtrVehLaneDirectionOfTravelLayer->laneDirOfTravelLaneSet.list, indWayDirOfTravel);
						}
					} // lIndex for loops ends
					movementsLayer->movementsContainer_Value.choice.MtrVehLaneDirectionOfTravelLayer = *mtrVehLaneDirectionOfTravelLayer;
					break;
				case MTR_VEH_LANE_CONNECTIONS_LAYER_ID: // MtrVehLaneConnectionsLayer
					movementsLayer->movementsContainer_Value.present = RGAMovementsLayers__movementsContainer_Value_PR_MtrVehLaneConnectionsLayer;

					// Retrieving the MtrVehLaneConnectionsLayer object
					jmethodID getMtrVehLaneCnxnsLayerMethod = (*env)->GetMethodID(env, movementsContainerClass, "getMtrVehLnCnxnsLayer", "()Lgov/usdot/cv/rgaencoder/MtrVehLaneConnectionsLayer;");
					jobject mtrVehLaneCnxnsLayerObj = (*env)->CallObjectMethod(env, movementsContainerObject, getMtrVehLaneCnxnsLayerMethod);

					// Populating MtrVehLaneConnectionsLayer_t
					MtrVehLaneConnectionsLayer_t *mtrVehLaneCnxnsLayer = calloc(1, sizeof(MtrVehLaneConnectionsLayer_t));

					jclass mtrVehLaneCnxnsLayerClass = (*env)->GetObjectClass(env, mtrVehLaneCnxnsLayerObj);
					jmethodID getLaneCnxnsLaneSetMethod = (*env)->GetMethodID(env, mtrVehLaneCnxnsLayerClass, "getMtrVehLaneCnxnsLaneSet", "()Ljava/util/List;");
					jobject indWayCnxnsList = (*env)->CallObjectMethod(env, mtrVehLaneCnxnsLayerObj, getLaneCnxnsLaneSetMethod);
				
					jclass indWayCnxnsListClass = (*env)->GetObjectClass(env, indWayCnxnsList);
					jmethodID indWayCnxnsListSizeMethod = (*env)->GetMethodID(env, indWayCnxnsListClass, "size", "()I");
					jmethodID indWayCnxnsListGetMethod = (*env)->GetMethodID(env, indWayCnxnsListClass, "get", "(I)Ljava/lang/Object;");
				
					jint indWayCnxnsListSize = (*env)->CallIntMethod(env, indWayCnxnsList, indWayCnxnsListSizeMethod);
				
					for (jint i = 0; i < indWayCnxnsListSize; i++) {
						jobject wayConnObj = (*env)->CallObjectMethod(env, indWayCnxnsList, indWayCnxnsListGetMethod, i);
						
						IndividualWayConnections_t *indWayCnxn = calloc(1, sizeof(IndividualWayConnections_t));

						populateIndividualWayConnection(env, wayConnObj, indWayCnxn);

						ASN_SEQUENCE_ADD(&mtrVehLaneCnxnsLayer->laneCnxnsLaneSet.list, indWayCnxn);
					}
					movementsLayer->movementsContainer_Value.choice.MtrVehLaneConnectionsLayer = *mtrVehLaneCnxnsLayer;
					break;
				case MTR_VEH_LANE_CONNECTIONS_MANEUVERS_LAYER_ID: // MtrVehLaneConnectionsManeuversLayer
					movementsLayer->movementsContainer_Value.present = RGAMovementsLayers__movementsContainer_Value_PR_MtrVehLaneConnectionsManeuversLayer;

					// Retrieving the MtrVehLaneConnectionsManeuversLayer object
					jmethodID getMtrVehLaneConnectionsManeuversLayerMethod = (*env)->GetMethodID(env, movementsContainerClass, "getMtrVehLnCnxnxMnvrLayer", "()Lgov/usdot/cv/rgaencoder/MtrVehLaneConnectionsManeuversLayer;");
					jobject mtrVehLaneConnectionsManeuversLayerObject = (*env)->CallObjectMethod(env, movementsContainerObject, getMtrVehLaneConnectionsManeuversLayerMethod);

					// Populating MtrVehLaneConnectionsManeuversLayer_t
					MtrVehLaneConnectionsManeuversLayer_t *mtrVehLaneCnxnsManeuversLayer = calloc(1, sizeof(MtrVehLaneConnectionsManeuversLayer_t));

					jclass mtrVehLaneConnectionsManeuversLayerClass = (*env)->GetObjectClass(env, mtrVehLaneConnectionsManeuversLayerObject);
					jmethodID getLaneCnxnsManeuversLaneSetLayer = (*env)->GetMethodID(env, mtrVehLaneConnectionsManeuversLayerClass, "getMtrVehLaneConnectionsManeuversLayer", "()Ljava/util/List;");
					jobject indivLaneCnxnsManeuversSetObject = (*env)->CallObjectMethod(env, mtrVehLaneConnectionsManeuversLayerObject, getLaneCnxnsManeuversLaneSetLayer);
				
					jclass indivLaneCnxnsManeuversSetClass = (*env)->GetObjectClass(env, indivLaneCnxnsManeuversSetObject);
					jmethodID indivLaneCnxnsManeuversSetSizeMethod = (*env)->GetMethodID(env, indivLaneCnxnsManeuversSetClass, "size", "()I");
					jmethodID indivLaneCnxnsManeuversSetGetMethod = (*env)->GetMethodID(env, indivLaneCnxnsManeuversSetClass, "get", "(I)Ljava/lang/Object;");

					jint indivLaneCnxnsManeuversSetSize = (*env)->CallIntMethod(env, indivLaneCnxnsManeuversSetObject, indivLaneCnxnsManeuversSetSizeMethod);
					
					for (jint i = 0; i < indivLaneCnxnsManeuversSetSize; i++) {
						jobject wayConnObj = (*env)->CallObjectMethod(env, indivLaneCnxnsManeuversSetObject, indivLaneCnxnsManeuversSetGetMethod, i);

						IndividualWayCnxnsManeuvers_t *indWayCnxnManeuvers = calloc(1, sizeof(IndividualWayCnxnsManeuvers_t));

						populateIndividualWayCnxnsManeuvers(env, wayConnObj, indWayCnxnManeuvers);

						ASN_SEQUENCE_ADD(&mtrVehLaneCnxnsManeuversLayer->laneCnxnsManeuversLaneSet.list, indWayCnxnManeuvers);
					}
					movementsLayer->movementsContainer_Value.choice.MtrVehLaneConnectionsManeuversLayer = *mtrVehLaneCnxnsManeuversLayer;
					break;

				case BIKE_LANE_CONNECTIONS__LAYER_ID: // BicycleLaneConnectionsLayer
					movementsLayer->movementsContainer_Value.present = RGAMovementsLayers__movementsContainer_Value_PR_BicycleLaneConnectionsLayer;

					// Retrieving the BicycleLaneConnectionsLayer object
					jmethodID getBicycleLaneCnxnsLayerMethod = (*env)->GetMethodID(env, movementsContainerClass, "getBikeLnCnxnsLayer", "()Lgov/usdot/cv/rgaencoder/BicycleLaneConnectionsLayer;");
					jobject bicycleLaneCnxnsLayerObj = (*env)->CallObjectMethod(env, movementsContainerObject, getBicycleLaneCnxnsLayerMethod);

					// Populating MtrVehLaneConnectionsLayer_t
					BicycleLaneConnectionsLayer_t *bicycleLaneConnectionsLayer = calloc(1, sizeof(BicycleLaneConnectionsLayer_t));

					jclass bicycleLaneCnxnsLayerClass = (*env)->GetObjectClass(env, bicycleLaneCnxnsLayerObj);
					jmethodID getBicycleLaneCnxnsLaneSetMethod = (*env)->GetMethodID(env, bicycleLaneCnxnsLayerClass, "getBicycleLaneCnxnsLaneSet", "()Ljava/util/List;");
					jobject bicycleIndWayCnxnsList = (*env)->CallObjectMethod(env, bicycleLaneCnxnsLayerObj, getBicycleLaneCnxnsLaneSetMethod);
				
					jclass bicycleIndWayCnxnsListClass = (*env)->GetObjectClass(env, bicycleIndWayCnxnsList);
					jmethodID bicycleIndWayCnxnsListSizeMethod = (*env)->GetMethodID(env, bicycleIndWayCnxnsListClass, "size", "()I");
					jmethodID bicycleIndWayCnxnsListGetMethod = (*env)->GetMethodID(env, bicycleIndWayCnxnsListClass, "get", "(I)Ljava/lang/Object;");
				
					jint bicycleIndWayCnxnsListSize = (*env)->CallIntMethod(env, bicycleIndWayCnxnsList, bicycleIndWayCnxnsListSizeMethod);
				
					for (jint i = 0; i < bicycleIndWayCnxnsListSize; i++) {
						jobject wayBicycleConnObj = (*env)->CallObjectMethod(env, bicycleIndWayCnxnsList, bicycleIndWayCnxnsListGetMethod, i);
						
						IndividualWayConnections_t *indWayBicycleCnxn = calloc(1, sizeof(IndividualWayConnections_t));

						populateIndividualWayConnection(env, wayBicycleConnObj, indWayBicycleCnxn);

						ASN_SEQUENCE_ADD(&bicycleLaneConnectionsLayer->laneCnxnsLaneSet.list, indWayBicycleCnxn);
					}
					movementsLayer->movementsContainer_Value.choice.BicycleLaneConnectionsLayer = *bicycleLaneConnectionsLayer;
					break;
				default:
					// Handle unknown ID
					movementsLayer->movementsContainer_Value.present = RGAMovementsLayers__movementsContainer_Value_PR_NOTHING;
					break;
				} // switch ends
				ASN_SEQUENCE_ADD(&rgaDataSet.movementsContainer->list, movementsLayer);
			}
		}
	}

	if(wayUseContainers != NULL) {
		// Extracting and Setting WayUse Containers
		jclass wayUseContainersList = (*env)->GetObjectClass(env, wayUseContainers);
		jmethodID wayUseContainersListSizeMethod = (*env)->GetMethodID(env, wayUseContainersList, "size", "()I");
		jmethodID wayUseContainersListGetMethod = (*env)->GetMethodID(env, wayUseContainersList, "get", "(I)Ljava/lang/Object;");

		jint wayUseContainersListSize = (*env)->CallIntMethod(env, wayUseContainers, wayUseContainersListSizeMethod);

		if (wayUseContainersListSize > 0)
		{
			rgaDataSet.wayUseContainer = calloc(1, sizeof(*rgaDataSet.wayUseContainer));

			for (int wIndex = 0; wIndex < wayUseContainersListSize; wIndex++)
			{
				RGAWayUseLayers_t *wayUseLayer = calloc(1, sizeof(RGAWayUseLayers_t));
				jobject wayUseContainerObject = (*env)->CallObjectMethod(env, wayUseContainers, wayUseContainersListGetMethod, wIndex);
				jclass wayUseContainerClass = (*env)->GetObjectClass(env, wayUseContainerObject);

				// Retrieve wayUseContainer-ID
				jmethodID getWayUseContainerID = (*env)->GetMethodID(env, wayUseContainerClass, "getWaUseContainerId", "()I");
				jint wayUseContainerID = (*env)->CallIntMethod(env, wayUseContainerObject, getWayUseContainerID);

				wayUseLayer->wayUseContainer_ID = wayUseContainerID;

				// Populate the wayUseContainer_Value based on the containerID
				switch (wayUseContainerID)
				{
				case MTR_VEH_LANE_SPEED_LIMITS_LAYER_ID: // MtrVehLaneSpeedLimitsLayer
					wayUseLayer->wayUseContainer_Value.present = RGAWayUseLayers__wayUseContainer_Value_PR_MtrVehLaneSpeedLimitsLayer;

					// Get MtrVehLaneSpeedLimitsLayer object from Java
					jmethodID getMtrVehLaneSpeedLimitsLayer = (*env)->GetMethodID(env, wayUseContainerClass, "getMtrVehLaneSpeedLimitsLayer", "()Lgov/usdot/cv/rgaencoder/MtrVehLaneSpeedLimitsLayer;");
					jobject mtrVehSpeedLimitsLayerObj = (*env)->CallObjectMethod(env, wayUseContainerObject, getMtrVehLaneSpeedLimitsLayer);

					if (mtrVehSpeedLimitsLayerObj != NULL) {
						jclass mtrVehSpeedLimitsLayerClass = (*env)->GetObjectClass(env, mtrVehSpeedLimitsLayerObj);

						MtrVehLaneSpeedLimitsLayer_t *mtrVehLaneSpeedLimitsLayer = calloc(1, sizeof(MtrVehLaneSpeedLimitsLayer_t));

						jmethodID getLaneSpeedLimitLaneSet = (*env)->GetMethodID(env, mtrVehSpeedLimitsLayerClass, "getLaneSpeedLimitLaneSet", "()Ljava/util/List;");
						jobject laneSpeedLimitLaneSetList = (*env)->CallObjectMethod(env, mtrVehSpeedLimitsLayerObj, getLaneSpeedLimitLaneSet);

						jclass laneSpeedLimitLaneSetListClass = (*env)->GetObjectClass(env, laneSpeedLimitLaneSetList);
						jmethodID laneSpeedLimitLaneSetListSizeMethod = (*env)->GetMethodID(env, laneSpeedLimitLaneSetListClass, "size", "()I");
						jmethodID laneSpeedLimitLaneSetListGetMethod = (*env)->GetMethodID(env, laneSpeedLimitLaneSetListClass, "get", "(I)Ljava/lang/Object;");

						jint laneSpeedLimitLaneSetSize = (*env)->CallIntMethod(env, laneSpeedLimitLaneSetList, laneSpeedLimitLaneSetListSizeMethod);

						for (int lIndex = 0; lIndex < laneSpeedLimitLaneSetSize; lIndex++) {
							jobject indivSpeedLimitObj = (*env)->CallObjectMethod(env, laneSpeedLimitLaneSetList, laneSpeedLimitLaneSetListGetMethod, lIndex);
							jclass indivSpeedLimitClass = (*env)->GetObjectClass(env, indivSpeedLimitObj);

							IndividualWaySpeedLimits_t *individualWaySpeedLimits = calloc(1, sizeof(IndividualWaySpeedLimits_t));

							// Get wayID
							jmethodID getIndivSpeedLimitWayID = (*env)->GetMethodID(env, indivSpeedLimitClass, "getWayID", "()I");
							jint indivSpeedLimitWayID = (*env)->CallIntMethod(env, indivSpeedLimitObj, getIndivSpeedLimitWayID);

							individualWaySpeedLimits->wayID = indivSpeedLimitWayID;

							// Get locationSpeedLimits
							jmethodID getLocationSpeedLimits = (*env)->GetMethodID(env, indivSpeedLimitClass, "getLocationSpeedLimitSet", "()Ljava/util/List;");
							jobject locationSpeedLimitsList = (*env)->CallObjectMethod(env, indivSpeedLimitObj, getLocationSpeedLimits);

							jclass locationSpeedLimitsListClass = (*env)->GetObjectClass(env, locationSpeedLimitsList);
							jmethodID locationSpeedLimitsListSizeMethod = (*env)->GetMethodID(env, locationSpeedLimitsListClass, "size", "()I");
							jmethodID locationSpeedLimitsListGetMethod = (*env)->GetMethodID(env, locationSpeedLimitsListClass, "get", "(I)Ljava/lang/Object;");

							jint locationSpeedLimitsListSize = (*env)->CallIntMethod(env, locationSpeedLimitsList, locationSpeedLimitsListSizeMethod);

							for (int j = 0; j < locationSpeedLimitsListSize; j++) {
								jobject locSpeedLimitObj = (*env)->CallObjectMethod(env, locationSpeedLimitsList, locationSpeedLimitsListGetMethod, j);
								jclass locSpeedLimitClass = (*env)->GetObjectClass(env, locSpeedLimitObj);

								LocationSpeedLimits_t *locationSpeedLimits = calloc(1, sizeof(LocationSpeedLimits_t));

								// === 1. NodeIndexOffset ===
								jmethodID getNodeIndexOffset = (*env)->GetMethodID(env, locSpeedLimitClass, "getLocation", "()Lgov/usdot/cv/rgaencoder/NodeIndexOffset;");
								jobject nodeIndexOffsetObj = (*env)->CallObjectMethod(env, locSpeedLimitObj, getNodeIndexOffset);

								if (nodeIndexOffsetObj != NULL) {
									jclass nodeIndexOffsetClass = (*env)->GetObjectClass(env, nodeIndexOffsetObj);

									// nodeIndex
									jmethodID getNodeIndex = (*env)->GetMethodID(env, nodeIndexOffsetClass, "getNodeIndex", "()J");
									jlong nodeIndex = (*env)->CallLongMethod(env, nodeIndexOffsetObj, getNodeIndex);
									locationSpeedLimits->location.nodeIndex = (NodeIndex_t)nodeIndex;

									// nodeIndexXYZOffset (optional)
									jmethodID getNodeIndexXYZOffset = (*env)->GetMethodID(env, nodeIndexOffsetClass, "getNodeIndexXYZOffset", "()Lgov/usdot/cv/rgaencoder/NodeXYZOffsetInfo;");
									jobject nodeIndexXYZOffsetObj = (*env)->CallObjectMethod(env, nodeIndexOffsetObj, getNodeIndexXYZOffset);

									if (nodeIndexXYZOffsetObj != NULL) {
										// Retrieve NodeXYZOffsetInfo class
										jclass nodeIndexXYZOffsetClass = (*env)->GetObjectClass(env, nodeIndexXYZOffsetObj);

										// Allocate and populate NodeXYZOffsetInfo
										NodeXYZOffsetInfo_t *nodeXYZOffset = calloc(1, sizeof(NodeXYZOffsetInfo_t));

										// Populate nodeXOffsetValue
										jmethodID getLocationNodeXOffsetMethod = (*env)->GetMethodID(env, nodeIndexXYZOffsetClass, "getNodeXOffsetValue", "()Lgov/usdot/cv/rgaencoder/NodeXYZOffsetValue;");
										jobject locationNodeXOffsetValueObj = (*env)->CallObjectMethod(env, nodeIndexXYZOffsetObj, getLocationNodeXOffsetMethod);
										populateNodeXYZOffsetValue(env, locationNodeXOffsetValueObj, &nodeXYZOffset->nodeXOffsetValue);

										// Populate nodeYOffsetValue
										jmethodID getLocationNodeYOffsetMethod = (*env)->GetMethodID(env, nodeIndexXYZOffsetClass, "getNodeYOffsetValue", "()Lgov/usdot/cv/rgaencoder/NodeXYZOffsetValue;");
										jobject locationNodeYOffsetValueObj = (*env)->CallObjectMethod(env, nodeIndexXYZOffsetObj, getLocationNodeYOffsetMethod);
										populateNodeXYZOffsetValue(env, locationNodeYOffsetValueObj, &nodeXYZOffset->nodeYOffsetValue);

										// Populate nodeZOffsetValue
										jmethodID getLocationNodeZOffsetMethod = (*env)->GetMethodID(env, nodeIndexXYZOffsetClass, "getNodeZOffsetValue", "()Lgov/usdot/cv/rgaencoder/NodeXYZOffsetValue;");
										jobject locationNodeZOffsetValueObj = (*env)->CallObjectMethod(env, nodeIndexXYZOffsetObj, getLocationNodeZOffsetMethod);
										populateNodeXYZOffsetValue(env, locationNodeZOffsetValueObj, &nodeXYZOffset->nodeZOffsetValue);

										// Assign to the location struct
										locationSpeedLimits->location.nodeIndexXYZOffset = nodeXYZOffset;
									} else {
										locationSpeedLimits->location.nodeIndexXYZOffset = NULL;
									}
								}

								// === 2. SpeedLimitInfo ===
								jmethodID getSpeedLimitInfo = (*env)->GetMethodID(env, locSpeedLimitClass, "getSpeedLimitInfo", "()Lgov/usdot/cv/rgaencoder/SpeedLimitInfo;");
								jobject speedLimitInfoObj = (*env)->CallObjectMethod(env, locSpeedLimitObj, getSpeedLimitInfo);

								if (speedLimitInfoObj != NULL)
								{
									jclass speedLimitInfoClass = (*env)->GetObjectClass(env, speedLimitInfoObj);

									SpeedLimitInfo_t *speedLimitInfo = calloc(1, sizeof(SpeedLimitInfo_t));

									// --- maxSpeedLimitSettingsSet ---
									jmethodID getMaxSpeedLimitList = (*env)->GetMethodID(env, speedLimitInfoClass, "getMaxSpeedLimitSettingsSet", "()Ljava/util/List;");
									jobject maxSpeedLimitListObj = (*env)->CallObjectMethod(env, speedLimitInfoObj, getMaxSpeedLimitList);

									jclass speedLimitListClass = (*env)->FindClass(env, "java/util/List");
									jmethodID speedLimitListSizeMethod = (*env)->GetMethodID(env, speedLimitListClass, "size", "()I");
									jmethodID speedLimitListGetMethod = (*env)->GetMethodID(env, speedLimitListClass, "get", "(I)Ljava/lang/Object;");

									jint maxSpeedLimitListSize = (*env)->CallIntMethod(env, maxSpeedLimitListObj, speedLimitListSizeMethod);

									for (int maxIndex = 0; maxIndex < maxSpeedLimitListSize; maxIndex++)
									{
										jobject indvMaxSpeedLimitSettingObj = (*env)->CallObjectMethod(env, maxSpeedLimitListObj, speedLimitListGetMethod, maxIndex);
										IndividualSpeedLimitSettings_t *maxIndividualSpeedLimitSettings = calloc(1, sizeof(IndividualSpeedLimitSettings_t));
										populateIndividualSpeedLimitSettings(env, indvMaxSpeedLimitSettingObj, maxIndividualSpeedLimitSettings);

										ASN_SEQUENCE_ADD(&speedLimitInfo->maxSpeedLimitSettingsSet.list, maxIndividualSpeedLimitSettings);
									}

									// --- minSpeedLimitSettingsSet (optional) ---
									jmethodID getMinSpeedLimitList = (*env)->GetMethodID(env, speedLimitInfoClass, "getMinSpeedLimitSettingsSet", "()Ljava/util/List;");
									jobject minSpeedLimitListObj = (*env)->CallObjectMethod(env, speedLimitInfoObj, getMinSpeedLimitList);

									if (minSpeedLimitListObj != NULL)
									{
										jint minSpeedLimitListSize = (*env)->CallIntMethod(env, minSpeedLimitListObj, speedLimitListSizeMethod);

										if (minSpeedLimitListSize > 0) {
											speedLimitInfo->minSpeedLimitSettingsSet = calloc(1, sizeof(struct SpeedLimitInfo__minSpeedLimitSettingsSet));
										}

										for (int minIndex = 0; minIndex < minSpeedLimitListSize; minIndex++)
										{
											jobject indvMinSpeedLimitSettingObj = (*env)->CallObjectMethod(env, minSpeedLimitListObj, speedLimitListGetMethod, minIndex);
											IndividualSpeedLimitSettings_t *minIndividualSpeedLimitSettings = calloc(1, sizeof(IndividualSpeedLimitSettings_t));
											populateIndividualSpeedLimitSettings(env, indvMinSpeedLimitSettingObj, minIndividualSpeedLimitSettings);

											ASN_SEQUENCE_ADD(&speedLimitInfo->minSpeedLimitSettingsSet->list, minIndividualSpeedLimitSettings);
										}
									}

									locationSpeedLimits->speedLimitInfo = *speedLimitInfo;
								}
								ASN_SEQUENCE_ADD(&individualWaySpeedLimits->locationSpeedLimitSet.list, locationSpeedLimits);
							}
							ASN_SEQUENCE_ADD(&mtrVehLaneSpeedLimitsLayer->laneSpeedLimitLaneSet.list, individualWaySpeedLimits);
						}
						wayUseLayer->wayUseContainer_Value.choice.MtrVehLaneSpeedLimitsLayer = *mtrVehLaneSpeedLimitsLayer;
					}
					break;
				default:
					// Handle unknown ID
					wayUseLayer->wayUseContainer_Value.present = RGAWayUseLayers__wayUseContainer_Value_PR_NOTHING;
					break;
				} // switch ends
				ASN_SEQUENCE_ADD(&rgaDataSet.wayUseContainer->list, wayUseLayer);

			}
		}
	}

	// rgaDataSet.movementsContainer = NULL;
	// rgaDataSet.wayUseContainer = NULL;
	rgaDataSet.signalControlSupportContainer = NULL;

	RGADataSet_t *rgaDataSetList = calloc(1, sizeof(RGADataSet_t));
	RoadGeometryAndAttributes_t *roadGeomAndAttr = calloc(1, sizeof(RoadGeometryAndAttributes_t));

	if (rgaDataSetList)
	{
		*rgaDataSetList = rgaDataSet;
		ASN_SEQUENCE_ADD(&roadGeomAndAttr->dataSetSet.list, rgaDataSetList);
	}

	message->value.choice.RoadGeometryAndAttributes = *roadGeomAndAttr;

	ec = uper_encode_to_buffer(&asn_DEF_MessageFrame, 0, message, buffer, buffer_size);
	if (ec.encoded == -1)
	{
		printf("Cause of failure %s \n", ec.failed_type->name);
		printf("Unsuccessful!\n");
		return NULL;
	}
	printf("Successful!\n");

	jsize length = ec.encoded / 8;
	jbyteArray outJNIArray = (*env)->NewByteArray(env, length);
	if (outJNIArray == NULL)
	{
		return NULL;
	}

	(*env)->SetByteArrayRegion(env, outJNIArray, 0, length, buffer);

	free(message);

	// Get a pointer to the array elements
	jbyte *elements = (*env)->GetByteArrayElements(env, outJNIArray, NULL);

	if (elements == NULL)
	{
		return; // Error handling, if necessary
	}

	// Loop through and print each element
	for (jsize i = 0; i < length; i++)
	{
		// printf("Element %d: %d\n", i, elements[i]);
		printf("%d, ", elements[i]);
	}
	(*env)->ReleaseByteArrayElements(env, outJNIArray, elements, JNI_ABORT);

	return outJNIArray;
}

void populateIndividualSpeedLimitSettings(JNIEnv *env, jobject indvSpeedLimitSettingObj, IndividualSpeedLimitSettings_t *individualSpeedLimitSettings)
{
	jclass indvSpeedLimitSettingClass = (*env)->GetObjectClass(env, indvSpeedLimitSettingObj);

	// speedLimit
	jmethodID getSpeedLimit = (*env)->GetMethodID(env, indvSpeedLimitSettingClass, "getSpeedLimit", "()J");
	jlong speedLimitValue = (*env)->CallLongMethod(env, indvSpeedLimitSettingObj, getSpeedLimit);
	individualSpeedLimitSettings->speedLimit = (Speed_t)speedLimitValue;

	// speedLimitType
	jmethodID getSpeedLimitType = (*env)->GetMethodID(env, indvSpeedLimitSettingClass, "getSpeedLimitType", "()Lgov/usdot/cv/rgaencoder/SpeedLimitTypeRGA;");
	jobject speedLimitTypeObj = (*env)->CallObjectMethod(env, indvSpeedLimitSettingObj, getSpeedLimitType);
	jclass speedLimitTypeClass = (*env)->GetObjectClass(env, speedLimitTypeObj);
	jmethodID getSpeedLimitTypeVal = (*env)->GetMethodID(env, speedLimitTypeClass, "getSpeedLimitTypeValue", "()J");
	jlong speedLimitTypeVal = (*env)->CallLongMethod(env, speedLimitTypeObj, getSpeedLimitTypeVal);

	individualSpeedLimitSettings->speedLimitType = (SpeedLimitType_t)speedLimitTypeVal;

	// vehicleTypes
	jmethodID getVehicleTypes = (*env)->GetMethodID(env, indvSpeedLimitSettingClass, "getVehicleTypes", "()Lgov/usdot/cv/rgaencoder/SpeedLimitVehicleType;");
	jobject vehTypesObj = (*env)->CallObjectMethod(env, indvSpeedLimitSettingObj, getVehicleTypes);
	jclass vehTypesClass = (*env)->GetObjectClass(env, vehTypesObj);
	jmethodID getVehTypesVal = (*env)->GetMethodID(env, vehTypesClass, "getSpeedLimitVehicleTypeValue", "()S");
	jshort vehTypeVal = (*env)->CallShortMethod(env, vehTypesObj, getVehTypesVal);

	individualSpeedLimitSettings->vehicleTypes.buf = (uint8_t *)calloc(1, sizeof(uint8_t));
	*(individualSpeedLimitSettings->vehicleTypes.buf) = (uint8_t)vehTypeVal;
	individualSpeedLimitSettings->vehicleTypes.size = 1;
	individualSpeedLimitSettings->vehicleTypes.bits_unused = 5;

	// Populate timeRestrictions if exists
	jmethodID getSpeedLimitTimeRestrictionsMethod = (*env)->GetMethodID(env, indvSpeedLimitSettingClass, "getTimeRestrictions", "()Lgov/usdot/cv/rgaencoder/RGATimeRestrictions;");
	jobject speedLimitTimeRestrictionsObj = (*env)->CallObjectMethod(env, indvSpeedLimitSettingObj, getSpeedLimitTimeRestrictionsMethod);

	if (speedLimitTimeRestrictionsObj != NULL)
	{
		RGATimeRestrictions_t *speedLimitTimeRestrictions = calloc(1, sizeof(RGATimeRestrictions_t));
		populateTimeRestrictions(env, speedLimitTimeRestrictionsObj, speedLimitTimeRestrictions);
		individualSpeedLimitSettings->timeRestrictions = speedLimitTimeRestrictions;
	}
	else
	{
		individualSpeedLimitSettings->timeRestrictions = NULL;
	}
}

void populateIndividualWayConnection(JNIEnv *env, jobject wayConnObj, IndividualWayConnections_t *indWayCnxn) {
	jclass wayConnClass = (*env)->GetObjectClass(env, wayConnObj);

	jmethodID getWayConnWayIDMethod = (*env)->GetMethodID(env, wayConnClass, "getWayID", "()I");
	jint wayConnWayID = (*env)->CallIntMethod(env, wayConnObj, getWayConnWayIDMethod);
	indWayCnxn->wayID = wayConnWayID;

	jmethodID getConnSetMethod = (*env)->GetMethodID(env, wayConnClass, "getConnectionsSet", "()Ljava/util/List;");
	jobject connSetListObject = (*env)->CallObjectMethod(env, wayConnObj, getConnSetMethod);

	jclass connSetListClass = (*env)->GetObjectClass(env, connSetListObject);
	jmethodID connSetListSizeMethod = (*env)->GetMethodID(env, connSetListClass, "size", "()I");
	jmethodID connSetListGetMethod = (*env)->GetMethodID(env, connSetListClass, "get", "(I)Ljava/lang/Object;");

	jint connSetListSize = (*env)->CallIntMethod(env, connSetListObject, connSetListSizeMethod);

	if (connSetListSize > 0)
	{
		for (jint c = 0; c < connSetListSize; c++)
		{
			jobject connectionInfoObj = (*env)->CallObjectMethod(env, connSetListObject, connSetListGetMethod, c);
			jclass connectionInfoClass = (*env)->GetObjectClass(env, connectionInfoObj);

			WayToWayConnectionInfo_t *connectionInfo = calloc(1, sizeof(WayToWayConnectionInfo_t));

			jmethodID getLaneConnIDMethod = (*env)->GetMethodID(env, connectionInfoClass, "getLaneConnectionID", "()I");
			jint laneConnectionID = (*env)->CallIntMethod(env, connectionInfoObj, getLaneConnIDMethod);
			connectionInfo->connectionID = laneConnectionID;

			// Populate connectionFromInfo
			jmethodID getConnectionFromInfoMethod = (*env)->GetMethodID(env, connectionInfoClass, "getConnectionFromInfo", "()Lgov/usdot/cv/rgaencoder/LaneConnectionFromInfo;");
			jobject fromInfoObj = (*env)->CallObjectMethod(env, connectionInfoObj, getConnectionFromInfoMethod);

			if (fromInfoObj != NULL)
			{
				jclass fromInfoClass = (*env)->GetObjectClass(env, fromInfoObj);
				jmethodID getNodeFromPositionMethod = (*env)->GetMethodID(env, fromInfoClass, "getNodeFromPosition", "()I");
				jint nodeFromPosition = (*env)->CallIntMethod(env, fromInfoObj, getNodeFromPositionMethod);

				NodeSetNode_t fromNodeSetNode;

				if (nodeFromPosition == FIRST_NODE)
				{
					fromNodeSetNode.present = NodeSetNode_PR_firstNode;
					fromNodeSetNode.choice.firstNode = 1;
				}
				else if (nodeFromPosition == LAST_NODE)
				{
					fromNodeSetNode.present = NodeSetNode_PR_lastNode;
					fromNodeSetNode.choice.lastNode = 1;
				}
				else
				{
					fromNodeSetNode.present = NodeSetNode_PR_NOTHING;
				}

				connectionInfo->connectionFromInfo.nodePosition = fromNodeSetNode;
			}

			// Populate connectionToInfo
			jmethodID getConnectionToInfoMethod = (*env)->GetMethodID(env, connectionInfoClass, "getConnectionToInfo", "()Lgov/usdot/cv/rgaencoder/LaneConnectionToInfo;");
			jobject toInfoObj = (*env)->CallObjectMethod(env, connectionInfoObj, getConnectionToInfoMethod);
			if (toInfoObj)
			{
				jclass toInfoClass = (*env)->GetObjectClass(env, toInfoObj);

				// Populate WayType
				jmethodID getToInfoWayTypeMethod = (*env)->GetMethodID(env, toInfoClass, "getWayType", "()Lgov/usdot/cv/rgaencoder/WayType;");
				jobject toInfoWayTypeObj = (*env)->CallObjectMethod(env, toInfoObj, getToInfoWayTypeMethod);

				if (toInfoWayTypeObj)
				{
					jclass toInfoWayTypeClass = (*env)->GetObjectClass(env, toInfoWayTypeObj);
					jmethodID getToInfoWayTypeValueMethod = (*env)->GetMethodID(env, toInfoWayTypeClass, "getWayTypeValue", "()J");
					jlong toInfoWayTypeValue = (*env)->CallLongMethod(env, toInfoWayTypeObj, getToInfoWayTypeValueMethod);

					WayType_t *toInfoWayType = calloc(1, sizeof(WayType_t));

					*toInfoWayType = (WayType_t)toInfoWayTypeValue;
					connectionInfo->connectionToInfo.wayType = toInfoWayType;
				}

				// Populate wayID
				jmethodID getToInfoWayIDMethod = (*env)->GetMethodID(env, toInfoClass, "getWayID", "()I");
				jint toInfoWayID = (*env)->CallIntMethod(env, toInfoObj, getToInfoWayIDMethod);

				connectionInfo->connectionToInfo.wayID = toInfoWayID;

				// Populate nodeToPosition
				jmethodID getNodeToPositionMethod = (*env)->GetMethodID(env, toInfoClass, "getNodeToPosition", "()I");
				jint nodeToPosition = (*env)->CallIntMethod(env, toInfoObj, getNodeToPositionMethod);

				NodeSetNode_t toNodeSetNode;

				if (nodeToPosition == FIRST_NODE)
				{
					toNodeSetNode.present = NodeSetNode_PR_firstNode;
					toNodeSetNode.choice.firstNode = 1;
				}
				else if (nodeToPosition == LAST_NODE)
				{
					toNodeSetNode.present = NodeSetNode_PR_lastNode;
					toNodeSetNode.choice.lastNode = 1;
				}
				else
				{
					toNodeSetNode.present = NodeSetNode_PR_NOTHING;
				}

				connectionInfo->connectionToInfo.nodePosition = toNodeSetNode;
			}
		
			// Populate timeRestrictions if exists
			jmethodID getConnectionInfoTimeRestrictionsMethod = (*env)->GetMethodID(env, connectionInfoClass, "getTimeRestrictions", "()Lgov/usdot/cv/rgaencoder/RGATimeRestrictions;");
			jobject connectionInfoTimeRestrictionsObj = (*env)->CallObjectMethod(env, connectionInfoObj, getConnectionInfoTimeRestrictionsMethod);

			if (connectionInfoTimeRestrictionsObj != NULL)
			{
				RGATimeRestrictions_t *connectionInfoTimeRestrictions = calloc(1, sizeof(RGATimeRestrictions_t));
				populateTimeRestrictions(env, connectionInfoTimeRestrictionsObj, connectionInfoTimeRestrictions);
				connectionInfo->timeRestrictions = connectionInfoTimeRestrictions;
			}
			else
			{
				connectionInfo->timeRestrictions = NULL;
			}

			ASN_SEQUENCE_ADD(&indWayCnxn->connectionsSet, connectionInfo);
		}
	}
}

void populateIndividualWayCnxnsManeuvers(JNIEnv *env, jobject wayCnxnsManeuversObj, IndividualWayCnxnsManeuvers_t *indWayCnxnManeuvers)
{
	jclass wayCnxnsManeuversClass = (*env)->GetObjectClass(env, wayCnxnsManeuversObj);
	jmethodID getWayIDMethod = (*env)->GetMethodID(env, wayCnxnsManeuversClass, "getWayID", "()I");
	jint wayID = (*env)->CallIntMethod(env, wayCnxnsManeuversObj, getWayIDMethod);
	indWayCnxnManeuvers->wayID = wayID;
	jmethodID getCnxnsManeuversSetMethod = (*env)->GetMethodID(env, wayCnxnsManeuversClass, "getCnxnManeuversSet", "()Ljava/util/List;");
	jobject wayCnxnsManeuversSetList = (*env)->CallObjectMethod(env, wayCnxnsManeuversObj, getCnxnsManeuversSetMethod);
	jclass wayCnxnManeuverInfoClass = (*env)->GetObjectClass(env, wayCnxnsManeuversSetList);

	jmethodID wayCnxnManeuverInfoSetSizeMethod = (*env)->GetMethodID(env, wayCnxnManeuverInfoClass, "size", "()I");
	jmethodID wayCnxnManeuverInfoSetGetMethod = (*env)->GetMethodID(env, wayCnxnManeuverInfoClass, "get", "(I)Ljava/lang/Object;");

	jint wayCnxnManeuverInfoSetSize = (*env)->CallIntMethod(env, wayCnxnsManeuversSetList, wayCnxnManeuverInfoSetSizeMethod);

	for (jint j = 0; j < wayCnxnManeuverInfoSetSize; j++)
	{
		jobject wayCnxnManeuverInfoObj = (*env)->CallObjectMethod(env, wayCnxnsManeuversSetList, wayCnxnManeuverInfoSetGetMethod, j);
		jclass wayCnxnmaneuverInfoIndClass = (*env)->GetObjectClass(env, wayCnxnManeuverInfoObj);

		WayCnxnManeuverInfo_t *wayCnxnManeuverInfo = calloc(1, sizeof(WayCnxnManeuverInfo_t));

		jmethodID getConnectionIDMethod = (*env)->GetMethodID(env, wayCnxnmaneuverInfoIndClass, "getConnectionID", "()I");
		jint connectionID = (*env)->CallIntMethod(env, wayCnxnManeuverInfoObj, getConnectionIDMethod);
		wayCnxnManeuverInfo->connectionID = connectionID;    

		//Populate the WayCnxnManueverInfo Set 
		jmethodID getManeuversSetMethod = (*env)->GetMethodID(env, wayCnxnmaneuverInfoIndClass, "getManeuversSet", "()Ljava/util/List;");
		jobject cnxnManeuversSetObject = (*env)->CallObjectMethod(env, wayCnxnManeuverInfoObj, getManeuversSetMethod);

		jclass cnxnsManeuversSetClass = (*env)->GetObjectClass(env, cnxnManeuversSetObject);
		jmethodID cnxnsManeuversSetSizeMethod = (*env)->GetMethodID(env, cnxnsManeuversSetClass, "size", "()I");
		jmethodID cnxnsManeuversSetGetMethod = (*env)->GetMethodID(env, cnxnsManeuversSetClass, "get", "(I)Ljava/lang/Object;");
		jint cnxnsManeuversSetSize = (*env)->CallIntMethod(env, cnxnManeuversSetObject, cnxnsManeuversSetSizeMethod);

		for (jint i = 0; i < cnxnsManeuversSetSize; i++)
		{
			CnxnManeuverInfo_t *cnxnManeuverInfo = calloc(1, sizeof(CnxnManeuverInfo_t));

			jobject cnxnsManeuversObj = (*env)->CallObjectMethod(env, cnxnManeuversSetObject, cnxnsManeuversSetGetMethod, i);
			jclass cnxnsManeuversClass = (*env)->GetObjectClass(env, cnxnsManeuversObj);

			// Populate WayCnxnManeuvers
			jmethodID getWayCnxnManeuvers = (*env)->GetMethodID(env, cnxnsManeuversClass, "getAllowedManeuver", "()Lgov/usdot/cv/rgaencoder/WayCnxnManeuvers;");
			jobject wayCnxnManeuvers = (*env)->CallObjectMethod(env, cnxnsManeuversObj, getWayCnxnManeuvers);
			jclass wayCnxnManeuversClass = (*env)->GetObjectClass(env, wayCnxnManeuvers);

			jmethodID getWayCnxnManeuversValue = (*env)->GetMethodID(env, wayCnxnManeuversClass, "getWayCnxnManeuvers", "()J");
			jlong wayCnxnManeuversValue = (*env)->CallLongMethod(env, wayCnxnManeuvers, getWayCnxnManeuversValue);

			cnxnManeuverInfo->allowedManeuver = (WayCnxnManeuvers_t)wayCnxnManeuversValue;

			//Populate WayCnxnManeuverControlType
			jmethodID getWayCnxnManeuverControlType = (*env)->GetMethodID(env, cnxnsManeuversClass, "getManeuverControlType", "()Lgov/usdot/cv/rgaencoder/WayCnxnManeuverControlType;");
			jobject wayCnxnManeuverControlTypeObj = (*env)->CallObjectMethod(env, cnxnsManeuversObj, getWayCnxnManeuverControlType);

			WayCnxnManeuverControlType_t wayCnxnManeuverControlType;
			jclass wayCnxnManeuverControlTypeClass = (*env)->GetObjectClass(env, wayCnxnManeuverControlTypeObj);
			jmethodID getChoice = (*env)->GetMethodID(env, wayCnxnManeuverControlTypeClass, "getChoice", "()I");
			jint choice = (*env)->CallIntMethod(env, wayCnxnManeuverControlTypeObj, getChoice);
			
			switch (choice)
			{
				case SIGNALIZED_CONTROL:
					wayCnxnManeuverControlType.present = WayCnxnManeuverControlType_PR_signalizedControl;
					wayCnxnManeuverControlType.choice.signalizedControl = 1;
					break;
				case UNSIGNALIZED_CONTROL:
					wayCnxnManeuverControlType.present = WayCnxnManeuverControlType_PR_unsignalizedControl;
					jmethodID getUnsignalizedMovementStates = (*env)->GetMethodID(env, wayCnxnManeuverControlTypeClass, "getUnsignalizedMovementStates", "()Lgov/usdot/cv/rgaencoder/UnsignalizedMovementStates;");
					jobject unsignalizedMovementStatesObj = (*env)->CallObjectMethod(env, wayCnxnManeuverControlTypeObj, getUnsignalizedMovementStates);

					jclass unsignalizedMovementStatesClass = (*env)->GetObjectClass(env, unsignalizedMovementStatesObj);
					jmethodID getUnsignalizedMovementStatesOption = (*env)->GetMethodID(env, unsignalizedMovementStatesClass, "getUnsignalizedMovementStatesValue", "()J" );
					jlong unsignalizedMovementStatesValue = (*env)->CallLongMethod(env, unsignalizedMovementStatesObj, getUnsignalizedMovementStatesOption);
					wayCnxnManeuverControlType.choice.unsignalizedControl = (UnsignalizedMovementStates_t)unsignalizedMovementStatesValue;
					break;
				case UNCONTROLLED:
					wayCnxnManeuverControlType.present = WayCnxnManeuverControlType_PR_uncontrolled;
					wayCnxnManeuverControlType.choice.uncontrolled = 1;
					break;
				default:
					wayCnxnManeuverControlType.present = WayCnxnManeuverControlType_PR_NOTHING;
			}
			cnxnManeuverInfo->maneuverControlType = wayCnxnManeuverControlType;

			// Populate TimeRestrictions

			jmethodID getTimeRestrictions = (*env)->GetMethodID(env, cnxnsManeuversClass, "getTimeRestrictions", "()Lgov/usdot/cv/rgaencoder/RGATimeRestrictions;");
			jobject timeRestrictionsObject = (*env)->CallObjectMethod(env, cnxnsManeuversObj, getTimeRestrictions);

			if (timeRestrictionsObject != NULL)
			{
				RGATimeRestrictions_t *timeRestrictions = calloc(1, sizeof(RGATimeRestrictions_t));
				populateTimeRestrictions(env, timeRestrictionsObject, timeRestrictions);
				cnxnManeuverInfo->timeRestrictions = timeRestrictions;
			}
			else
			{
				cnxnManeuverInfo->timeRestrictions = NULL;
			}
			ASN_SEQUENCE_ADD(&wayCnxnManeuverInfo->maneuversSet, cnxnManeuverInfo);
		}
		ASN_SEQUENCE_ADD(&indWayCnxnManeuvers->cnxnManeuversSet, wayCnxnManeuverInfo);
	}

}

void populateLaneConstructorType(JNIEnv *env, jobject laneConstructorTypeObj, LaneConstructorType_t *laneConstructorType)
{
	jclass laneConstructorTypeClass = (*env)->GetObjectClass(env, laneConstructorTypeObj);

	// Get the choice type for LaneConstructorType
	jmethodID getLaneConstructorTypeChoiceMethod = (*env)->GetMethodID(env, laneConstructorTypeClass, "getChoice", "()I");
	jint laneConstructorTypeChoice = (*env)->CallIntMethod(env, laneConstructorTypeObj, getLaneConstructorTypeChoiceMethod);

	// Get the LaneConstructor Type
	if (laneConstructorTypeChoice == PHYSICAL_NODE)
	{
		// Populate physicalXYZNodeInfo
		jmethodID getPhysicalXYZNodeInfoMethod = (*env)->GetMethodID(env, laneConstructorTypeClass, "getPhysicalXYZNodeInfo", "()Lgov/usdot/cv/rgaencoder/PhysicalXYZNodeInfo;");
		jobject physicalXYZNodeInfoObj = (*env)->CallObjectMethod(env, laneConstructorTypeObj, getPhysicalXYZNodeInfoMethod);
		jclass physicalXYZNodeInfoClass = (*env)->GetObjectClass(env, physicalXYZNodeInfoObj);

		jmethodID getNodeXYZGeometryNodeSetMethod = (*env)->GetMethodID(env, physicalXYZNodeInfoClass, "getNodeXYZGeometryNodeSet", "()Ljava/util/List;");
		jobject nodeXYZGeometryNodeSetList = (*env)->CallObjectMethod(env, physicalXYZNodeInfoObj, getNodeXYZGeometryNodeSetMethod);

		jclass nodeXYZGeometryNodeSetClass = (*env)->GetObjectClass(env, nodeXYZGeometryNodeSetList);
		jmethodID nodeXYZGeometryNodeSetSizeMethod = (*env)->GetMethodID(env, nodeXYZGeometryNodeSetClass, "size", "()I");
		jmethodID nodeXYZGeometryNodeSetGetMethod = (*env)->GetMethodID(env, nodeXYZGeometryNodeSetClass, "get", "(I)Ljava/lang/Object;");

		jint nodeXYZGeometryNodeSetSize = (*env)->CallIntMethod(env, nodeXYZGeometryNodeSetList, nodeXYZGeometryNodeSetSizeMethod);

		PhysicalXYZNodeInfo_t *physicalXYZNodeInfo = calloc(1, sizeof(PhysicalXYZNodeInfo_t));

		for (jint nIndex = 0; nIndex < nodeXYZGeometryNodeSetSize; nIndex++)
		{
			jobject individualXYZNodeGeometryInfoObj = (*env)->CallObjectMethod(env, nodeXYZGeometryNodeSetList, nodeXYZGeometryNodeSetGetMethod, nIndex);
			jclass individualXYZNodeGeometryInfoClass = (*env)->GetObjectClass(env, individualXYZNodeGeometryInfoObj);

			// Retrieve NodeXYZOffsetInfo
			jmethodID getNodeXYZOffsetInfoMethod = (*env)->GetMethodID(env, individualXYZNodeGeometryInfoClass, "getNodeXYZOffsetInfo", "()Lgov/usdot/cv/rgaencoder/NodeXYZOffsetInfo;");
			jobject nodeXYZOffsetInfoObj = (*env)->CallObjectMethod(env, individualXYZNodeGeometryInfoObj, getNodeXYZOffsetInfoMethod);
			jclass nodeXYZOffsetInfoClass = (*env)->GetObjectClass(env, nodeXYZOffsetInfoObj);

			IndividualXYZNodeGeometryInfo_t *individualXYZNodeGeometryInfo = calloc(1, sizeof(IndividualXYZNodeGeometryInfo_t));

			// Populate nodeXOffsetValue
			jmethodID getNodeXOffsetMethod = (*env)->GetMethodID(env, nodeXYZOffsetInfoClass, "getNodeXOffsetValue", "()Lgov/usdot/cv/rgaencoder/NodeXYZOffsetValue;");
			jobject nodeXOffsetValueObj = (*env)->CallObjectMethod(env, nodeXYZOffsetInfoObj, getNodeXOffsetMethod);
			populateNodeXYZOffsetValue(env, nodeXOffsetValueObj, &individualXYZNodeGeometryInfo->nodeXYZOffsetInfo.nodeXOffsetValue);

			// Populate nodeYOffsetValue
			jmethodID getNodeYOffsetMethod = (*env)->GetMethodID(env, nodeXYZOffsetInfoClass, "getNodeYOffsetValue", "()Lgov/usdot/cv/rgaencoder/NodeXYZOffsetValue;");
			jobject nodeYOffsetValueObj = (*env)->CallObjectMethod(env, nodeXYZOffsetInfoObj, getNodeYOffsetMethod);
			populateNodeXYZOffsetValue(env, nodeYOffsetValueObj, &individualXYZNodeGeometryInfo->nodeXYZOffsetInfo.nodeYOffsetValue);

			// Populate nodeZOffsetValue
			jmethodID getNodeZOffsetMethod = (*env)->GetMethodID(env, nodeXYZOffsetInfoClass, "getNodeZOffsetValue", "()Lgov/usdot/cv/rgaencoder/NodeXYZOffsetValue;");
			jobject nodeZOffsetValueObj = (*env)->CallObjectMethod(env, nodeXYZOffsetInfoObj, getNodeZOffsetMethod);
			populateNodeXYZOffsetValue(env, nodeZOffsetValueObj, &individualXYZNodeGeometryInfo->nodeXYZOffsetInfo.nodeZOffsetValue);

			jmethodID getNodeLocPlanarGeometryInfoMethod = (*env)->GetMethodID(env, individualXYZNodeGeometryInfoClass, "getNodeLocPlanarGeomInfo", "()Lgov/usdot/cv/rgaencoder/WayPlanarGeometryInfo;");
			jobject nodeLocPlanarGeometryInfoObj = (*env)->CallObjectMethod(env, individualXYZNodeGeometryInfoObj, getNodeLocPlanarGeometryInfoMethod);

			WayPlanarGeometryInfo_t *nodeLocPlanarGeomInfo = calloc(1, sizeof(WayPlanarGeometryInfo_t));

			if (nodeLocPlanarGeometryInfoObj != NULL)
			{
				// Populate wayWidth
				jclass nodeLocPlanarGeometryInfoClass = (*env)->GetObjectClass(env, nodeLocPlanarGeometryInfoObj);
				jmethodID getWayWidthMethod = (*env)->GetMethodID(env, nodeLocPlanarGeometryInfoClass, "getWayWidth", "()Lgov/usdot/cv/rgaencoder/WayWidth;");
				jobject wayWidthObj = (*env)->CallObjectMethod(env, nodeLocPlanarGeometryInfoObj, getWayWidthMethod);

				if (wayWidthObj != NULL)
				{
					WayWidth_t *wayWidth = calloc(1, sizeof(WayWidth_t));
					jclass wayWidthClass = (*env)->GetObjectClass(env, wayWidthObj);
					jmethodID getChoice = (*env)->GetMethodID(env, wayWidthClass, "getChoice", "()B");
					jbyte choice = (*env)->CallByteMethod(env, wayWidthObj, getChoice);

					switch (choice)
					{
						case 0:
							wayWidth->present = WayWidth_PR_fullWidth;
							jmethodID getFullWidth = (*env)->GetMethodID(env, wayWidthClass, "getFullWidth", "()I");
							jint fullWidth = (*env)->CallIntMethod(env, wayWidthObj, getFullWidth);
							wayWidth->choice.fullWidth = (LaneWidth_t)((long)fullWidth);
							break;
						case 1:
							wayWidth->present = WayWidth_PR_deltaWidth;
							jmethodID getDeltaWidth = (*env)->GetMethodID(env, wayWidthClass, "getDeltaWidth", "()I");
							jint deltaWidth = (*env)->CallIntMethod(env, wayWidthObj, getDeltaWidth);
							wayWidth->choice.deltaWidth = (Offset_B10_t)((long)deltaWidth);
							break;
						default:
							wayWidth->present = WayWidth_PR_NOTHING;
							break;
					}
					nodeLocPlanarGeomInfo->wayWidth = wayWidth;
				}
				else
				{
					nodeLocPlanarGeomInfo->wayWidth = NULL;
				}
			}
			else
			{
				nodeLocPlanarGeomInfo = NULL;
			}
			individualXYZNodeGeometryInfo->nodeLocPlanarGeomInfo = nodeLocPlanarGeomInfo;
			ASN_SEQUENCE_ADD(&physicalXYZNodeInfo->nodeXYZGeometryNodeSet.list, individualXYZNodeGeometryInfo);
		}

		jmethodID getReferencePointInfoMethod = (*env)->GetMethodID(env, physicalXYZNodeInfoClass, "getReferencePointInfo", "()Lgov/usdot/cv/rgaencoder/ReferencePointInfo;");
		jobject referencePointInfoObj = (*env)->CallObjectMethod(env, physicalXYZNodeInfoObj, getReferencePointInfoMethod);

		if (referencePointInfoObj != NULL)
		{
			ReferencePointInfo_t *referencePointInfo = calloc(1, sizeof(ReferencePointInfo_t));
			populateReferencePointInfo(env, referencePointInfoObj, referencePointInfo);
			physicalXYZNodeInfo->alternateRefPt = referencePointInfo;
		} else {
			physicalXYZNodeInfo->alternateRefPt = NULL;
		}

		laneConstructorType->present = LaneConstructorType_PR_physicalXYZNodeInfo;
		laneConstructorType->choice.physicalXYZNodeInfo = *physicalXYZNodeInfo;
	}
	else if (laneConstructorTypeChoice == COMPUTED_NODE)
	{
		// Populate computedXYZNodeInfo
		jmethodID getComputedXYZNodeInfoMethod = (*env)->GetMethodID(env, laneConstructorTypeClass, "getComputedXYZNodeInfo", "()Lgov/usdot/cv/rgaencoder/ComputedXYZNodeInfo;");
		jobject computedXYZNodeInfoObj = (*env)->CallObjectMethod(env, laneConstructorTypeObj, getComputedXYZNodeInfoMethod);
		jclass computedXYZNodeInfoClass = (*env)->GetObjectClass(env, computedXYZNodeInfoObj);

		// Get refLaneID
		jmethodID getRefLaneIDMethod = (*env)->GetMethodID(env, computedXYZNodeInfoClass, "getRefLaneID", "()I");
		jint refLaneID = (*env)->CallIntMethod(env, computedXYZNodeInfoObj, getRefLaneIDMethod);

		// Allocate memory for ComputedXYZNodeInfo_t
		ComputedXYZNodeInfo_t *computedXYZNodeInfo = calloc(1, sizeof(ComputedXYZNodeInfo_t));
		computedXYZNodeInfo->refLaneID = refLaneID;

		// Get laneCenterLineXYZOffset
		jmethodID getLaneCenterLineXYZOffsetMethod = (*env)->GetMethodID(env, computedXYZNodeInfoClass, "getLaneCenterLineXYZOffset", "()Lgov/usdot/cv/rgaencoder/NodeXYZOffsetInfo;");
		jobject laneCenterLineXYZOffsetObj = (*env)->CallObjectMethod(env, computedXYZNodeInfoObj, getLaneCenterLineXYZOffsetMethod);
		jclass laneCenterLineXYZOffsetClass = (*env)->GetObjectClass(env, laneCenterLineXYZOffsetObj);

		// Populate nodeXOffsetValue
		jmethodID getNodeXOffsetMethod = (*env)->GetMethodID(env, laneCenterLineXYZOffsetClass, "getNodeXOffsetValue", "()Lgov/usdot/cv/rgaencoder/NodeXYZOffsetValue;");
		jobject nodeXOffsetValueObj = (*env)->CallObjectMethod(env, laneCenterLineXYZOffsetObj, getNodeXOffsetMethod);
		populateNodeXYZOffsetValue(env, nodeXOffsetValueObj, &computedXYZNodeInfo->laneCenterLineXYZOffset.nodeXOffsetValue);

		// Populate nodeYOffsetValue
		jmethodID getNodeYOffsetMethod = (*env)->GetMethodID(env, laneCenterLineXYZOffsetClass, "getNodeYOffsetValue", "()Lgov/usdot/cv/rgaencoder/NodeXYZOffsetValue;");
		jobject nodeYOffsetValueObj = (*env)->CallObjectMethod(env, laneCenterLineXYZOffsetObj, getNodeYOffsetMethod);
		populateNodeXYZOffsetValue(env, nodeYOffsetValueObj, &computedXYZNodeInfo->laneCenterLineXYZOffset.nodeYOffsetValue);

		// Populate nodeZOffsetValue
		jmethodID getNodeZOffsetMethod = (*env)->GetMethodID(env, laneCenterLineXYZOffsetClass, "getNodeZOffsetValue", "()Lgov/usdot/cv/rgaencoder/NodeXYZOffsetValue;");
		jobject nodeZOffsetValueObj = (*env)->CallObjectMethod(env, laneCenterLineXYZOffsetObj, getNodeZOffsetMethod);
		populateNodeXYZOffsetValue(env, nodeZOffsetValueObj, &computedXYZNodeInfo->laneCenterLineXYZOffset.nodeZOffsetValue);

		// Populate WayPlanarGeometryInfo
		jmethodID getLanePlanarGeometryInfoMethod = (*env)->GetMethodID(env, computedXYZNodeInfoClass, "getLanePlanarGeomInfo", "()Lgov/usdot/cv/rgaencoder/WayPlanarGeometryInfo;");
		jobject lanePlanarGeometryInfoObj = (*env)->CallObjectMethod(env, computedXYZNodeInfoObj, getLanePlanarGeometryInfoMethod);

		WayPlanarGeometryInfo_t *lanePlanarGeomInfo = calloc(1, sizeof(WayPlanarGeometryInfo_t));

		jclass lanePlanarGeometryInfoClass = (*env)->GetObjectClass(env, lanePlanarGeometryInfoObj);

		jmethodID getWayWidthMethod = (*env)->GetMethodID(env, lanePlanarGeometryInfoClass, "getWayWidth", "()Lgov/usdot/cv/rgaencoder/WayWidth;");
		jobject wayWidthObj = (*env)->CallObjectMethod(env, lanePlanarGeometryInfoObj, getWayWidthMethod);

		if (wayWidthObj != NULL)
				{
					WayWidth_t *wayWidth = calloc(1, sizeof(WayWidth_t));
					jclass wayWidthClass = (*env)->GetObjectClass(env, wayWidthObj);

					jmethodID getChoice = (*env)->GetMethodID(env, wayWidthClass, "getChoice", "()B");
					jbyte choice = (*env)->CallByteMethod(env, wayWidthObj, getChoice);

					switch (choice)
					{
						case 0:
							wayWidth->present = WayWidth_PR_fullWidth;
							jmethodID getFullWidth = (*env)->GetMethodID(env, wayWidthClass, "getFullWidth", "()I");
							jint fullWidth = (*env)->CallIntMethod(env, wayWidthObj, getFullWidth);
							wayWidth->choice.fullWidth = (LaneWidth_t)((long)fullWidth);
							break;
						case 1:
							wayWidth->present = WayWidth_PR_deltaWidth;
							jmethodID getDeltaWidth = (*env)->GetMethodID(env, wayWidthClass, "getDeltaWidth", "()I");
							jint deltaWidth = (*env)->CallIntMethod(env, wayWidthObj, getDeltaWidth);
							wayWidth->choice.deltaWidth = (Offset_B10_t)((long)deltaWidth);
							break;
						default:
							wayWidth->present = WayWidth_PR_NOTHING;
							break;
					}
					lanePlanarGeomInfo->wayWidth = wayWidth;
				}
				else
				{
					lanePlanarGeomInfo->wayWidth = NULL;
				}

		computedXYZNodeInfo->lanePlanarGeomInfo = *lanePlanarGeomInfo;
		laneConstructorType->present = LaneConstructorType_PR_computedXYZNodeInfo;
		laneConstructorType->choice.computedXYZNodeInfo = *computedXYZNodeInfo;
	}
	else if (laneConstructorTypeChoice == DUPLICATE_NODE)
	{
		// Populate duplicateXYZNodeInfo
		jmethodID getDuplicateXYZNodeInfoMethod = (*env)->GetMethodID(env, laneConstructorTypeClass, "getDuplicateXYZNodeInfo", "()Lgov/usdot/cv/rgaencoder/DuplicateXYZNodeInfo;");
		jobject duplicateXYZNodeInfoObj = (*env)->CallObjectMethod(env, laneConstructorTypeObj, getDuplicateXYZNodeInfoMethod);
		jclass duplicateXYZNodeInfoClass = (*env)->GetObjectClass(env, duplicateXYZNodeInfoObj);

		// Get refLaneID
		jmethodID getRefLaneIDMethod = (*env)->GetMethodID(env, duplicateXYZNodeInfoClass, "getRefLaneID", "()I");
		jint refLaneID = (*env)->CallIntMethod(env, duplicateXYZNodeInfoObj, getRefLaneIDMethod);

		DuplicateXYZNodeInfo_t *duplicateXYZNodeInfo = calloc(1, sizeof(DuplicateXYZNodeInfo_t));
		duplicateXYZNodeInfo->refLaneID = refLaneID;

		laneConstructorType->present = LaneConstructorType_PR_duplicateXYZNodeInfo;
		laneConstructorType->choice.duplicateXYZNodeInfo = *duplicateXYZNodeInfo;
	}
}

// Function to handle offset values
void populateNodeXYZOffsetValue(JNIEnv *env, jobject offsetValueObj, NodeXYZOffsetValue_t *offsetValue)
{
	jclass offsetValueClass = (*env)->GetObjectClass(env, offsetValueObj);

	jmethodID getChoiceMethod = (*env)->GetMethodID(env, offsetValueClass, "getChoice", "()I");
	jint choice = (*env)->CallIntMethod(env, offsetValueObj, getChoiceMethod);

	if (choice == OFFSET_B10)
	{
		jmethodID getOffsetB10Method = (*env)->GetMethodID(env, offsetValueClass, "getOffsetB10", "()J");
		jlong offsetB10 = (*env)->CallLongMethod(env, offsetValueObj, getOffsetB10Method);

		offsetValue->present = NodeXYZOffsetValue_PR_plusMinus5pt11m;
		offsetValue->choice.plusMinus5pt11m = (Offset_B10_t)offsetB10;
	}
	else if (choice == OFFSET_B11)
	{
		jmethodID getOffsetB11Method = (*env)->GetMethodID(env, offsetValueClass, "getOffsetB11", "()J");
		jlong offsetB11 = (*env)->CallLongMethod(env, offsetValueObj, getOffsetB11Method);

		offsetValue->present = NodeXYZOffsetValue_PR_plusMinus10pt23m;
		offsetValue->choice.plusMinus10pt23m = (Offset_B11_t)offsetB11;
	}
	else if (choice == OFFSET_B12)
	{
		jmethodID getOffsetB12Method = (*env)->GetMethodID(env, offsetValueClass, "getOffsetB12", "()J");
		jlong offsetB12 = (*env)->CallLongMethod(env, offsetValueObj, getOffsetB12Method);

		offsetValue->present = NodeXYZOffsetValue_PR_plusMinus20pt47m;
		offsetValue->choice.plusMinus20pt47m = (Offset_B12_t)offsetB12;
	}
	else if (choice == OFFSET_B13)
	{
		jmethodID getOffsetB13Method = (*env)->GetMethodID(env, offsetValueClass, "getOffsetB13", "()J");
		jlong offsetB13 = (*env)->CallLongMethod(env, offsetValueObj, getOffsetB13Method);

		offsetValue->present = NodeXYZOffsetValue_PR_plusMinus40pt95m;
		offsetValue->choice.plusMinus40pt95m = (Offset_B13_t)offsetB13;
	}
	else if (choice == OFFSET_B14)
	{
		jmethodID getOffsetB14Method = (*env)->GetMethodID(env, offsetValueClass, "getOffsetB14", "()J");
		jlong offsetB14 = (*env)->CallLongMethod(env, offsetValueObj, getOffsetB14Method);

		offsetValue->present = NodeXYZOffsetValue_PR_plusMinus81pt91m;
		offsetValue->choice.plusMinus81pt91m = (Offset_B14_t)offsetB14;
	}
	else if (choice == OFFSET_B16)
	{
		jmethodID getOffsetB16Method = (*env)->GetMethodID(env, offsetValueClass, "getOffsetB16", "()J");
		jlong offsetB16 = (*env)->CallLongMethod(env, offsetValueObj, getOffsetB16Method);

		offsetValue->present = NodeXYZOffsetValue_PR_plusMinus327pt67m;
		offsetValue->choice.plusMinus327pt67m = (Offset_B16_t)offsetB16;
	}
	else
	{
		offsetValue->present = NodeXYZOffsetValue_PR_NOTHING;
	}
}

// Function that populates the fields inside the ReferencePointInfo class
void populateReferencePointInfo(JNIEnv *env, jobject referencePointObj, ReferencePointInfo_t *referencePoint) {
    jclass referencePointClass = (*env)->GetObjectClass(env, referencePointObj);

    jmethodID getLocation = (*env)->GetMethodID(env, referencePointClass, "getLocation", "()Lgov/usdot/cv/mapencoder/Position3D;");
    jobject locationObj = (*env)->CallObjectMethod(env, referencePointObj, getLocation);
	jclass locationClass = (*env)->GetObjectClass(env, locationObj);

    jmethodID getTimeOfCalculation = (*env)->GetMethodID(env, referencePointClass, "getTimeOfCalculation", "()Lgov/usdot/cv/rgaencoder/DDate;");
    jobject timeOfCalculationObj = (*env)->CallObjectMethod(env, referencePointObj, getTimeOfCalculation);
	jclass timeOfCalculationClass = (*env)->GetObjectClass(env, timeOfCalculationObj);

	// ================== Reference Point Info (Reference Point) ==================

	Position3D_t location;

	jmethodID getLatitude = (*env)->GetMethodID(env, locationClass, "getLatitude", "()D");
	jmethodID getLongitude = (*env)->GetMethodID(env, locationClass, "getLongitude", "()D");

	jdouble latitude = (*env)->CallDoubleMethod(env, locationObj, getLatitude);
	jdouble longitude = (*env)->CallDoubleMethod(env, locationObj, getLongitude);

	location.lat = (Common_Latitude_t)((long)latitude);
	location.Long = (Common_Longitude_t)((long)longitude);

	// Check if elevation exists
	jmethodID isElevationExists = (*env)->GetMethodID(env, locationClass, "isElevationExists", "()Z");
	jboolean elevationExists = (*env)->CallBooleanMethod(env, locationObj, isElevationExists);

	if (elevationExists)
	{
		jmethodID getElevation = (*env)->GetMethodID(env, locationClass, "getElevation", "()F");
		jfloat elevation = (*env)->CallFloatMethod(env, locationObj, getElevation);

		Common_Elevation_t *dsrcElevation = calloc(1, sizeof(Common_Elevation_t));
		*dsrcElevation = (long)elevation;
		location.elevation = dsrcElevation;
	}
	else
	{
		location.elevation = NULL;
	}
	location.regional = NULL;
	
	referencePoint->location = location;

	// ================== Reference Point Info (Time Of Calculation) ==================
	DDate_t timeOfCalculation;

	jmethodID getYear = (*env)->GetMethodID(env, timeOfCalculationClass, "getYear", "()I");
	jmethodID getMonth = (*env)->GetMethodID(env, timeOfCalculationClass, "getMonth", "()I");
	jmethodID getDay = (*env)->GetMethodID(env, timeOfCalculationClass, "getDay", "()I");

	jint year = (*env)->CallIntMethod(env, timeOfCalculationObj, getYear);
	jint month = (*env)->CallIntMethod(env, timeOfCalculationObj, getMonth);
	jint day = (*env)->CallIntMethod(env, timeOfCalculationObj, getDay);

	timeOfCalculation.year = (long)year;
	timeOfCalculation.month = (long)month;
	timeOfCalculation.day = (long)day;

	referencePoint->timeOfCalculation = timeOfCalculation;    
}

// Function that populates the fields inside the RGATimeRestrictions class
void populateTimeRestrictions(JNIEnv *env, jobject timeRestrictionsObj, RGATimeRestrictions_t *timeRestrictions) {
    jclass timeRestrictionsClass = (*env)->GetObjectClass(env, timeRestrictionsObj);
	jmethodID getTimeRestrictionsTypeChoiceMethod = (*env)->GetMethodID(env, timeRestrictionsClass, "getChoice", "()I");
	jint timeRestrictionsTypeChoice = (*env)->CallIntMethod(env, timeRestrictionsObj, getTimeRestrictionsTypeChoiceMethod);

	if (timeRestrictionsTypeChoice == 1) {
		timeRestrictions->present = RGATimeRestrictions_PR_fixedTimeWindowItemCtrl;
		jmethodID getTimeWindowItemControlInfoMethod = (*env)->GetMethodID(env, timeRestrictionsClass, "getFixedTimeWindowCtrl", "()Lgov/usdot/cv/rgaencoder/TimeWindowItemControlInfo;");
		jobject getFixedTimeWindowCtrlObj = (*env)->CallObjectMethod(env, timeRestrictionsObj, getTimeWindowItemControlInfoMethod);
		jclass timeWindowItemControlInfoClass = (*env)->GetObjectClass(env, getFixedTimeWindowCtrlObj);

		jmethodID getTimeWindowSetMethod = (*env)->GetMethodID(env, timeWindowItemControlInfoClass, "getTimeWindowSet", "()Ljava/util/List;");
		jobject timeWindowSetList = (*env)->CallObjectMethod(env, getFixedTimeWindowCtrlObj, getTimeWindowSetMethod);

		jclass timeWindowSetClass = (*env)->GetObjectClass(env, timeWindowSetList);
		jmethodID timeWindowSetSizeMethod = (*env)->GetMethodID(env, timeWindowSetClass, "size", "()I");
		jmethodID timeWindowSetGetMethod = (*env)->GetMethodID(env, timeWindowSetClass, "get", "(I)Ljava/lang/Object;");

		jint timeWindowSetSize = (*env)->CallIntMethod(env, timeWindowSetList, timeWindowSetSizeMethod);

		TimeWindowItemControlInfo_t *fixedTimeWindowItemCtrl = calloc(1, sizeof(TimeWindowItemControlInfo_t));

		for (jint tIndex = 0; tIndex < timeWindowSetSize; tIndex++)
		{
			jobject timeWindowInformationObj = (*env)->CallObjectMethod(env, timeWindowSetList, timeWindowSetGetMethod, tIndex);
			jclass timeWindowInformationClass = (*env)->GetObjectClass(env, timeWindowInformationObj);

			TimeWindowInformation_t *timeWindowInformation = calloc(1, sizeof(TimeWindowInformation_t));

			// DaysOfTheWeek  
			jmethodID getDaysOfTheWeekMethod = (*env)->GetMethodID(env, timeWindowInformationClass, "getDaysOfTheWeek", "()Lgov/usdot/cv/rgaencoder/DaysOfTheWeek;");
			jobject daysOfTheWeekObj = (*env)->CallObjectMethod(env, timeWindowInformationObj, getDaysOfTheWeekMethod);

			if (daysOfTheWeekObj != NULL)
			{
				jclass daysOfTheWeekClass = (*env)->GetObjectClass(env, daysOfTheWeekObj);
				jmethodID getDaysOfTheWeekValue = (*env)->GetMethodID(env, daysOfTheWeekClass, "getDaysOfTheWeekValue", "()S");
				jshort daysOfTheWeekShort = (*env)->CallShortMethod(env, daysOfTheWeekObj, getDaysOfTheWeekValue);

				DaysOfTheWeek_t *daysOfTheWeek = calloc(1, sizeof(DaysOfTheWeek_t));
                daysOfTheWeek->buf = (uint8_t *)calloc(1, sizeof(uint8_t));
                *daysOfTheWeek->buf = (uint8_t)daysOfTheWeekShort;
                daysOfTheWeek->size = 1;
                daysOfTheWeek->bits_unused = 0;

                timeWindowInformation->daysOfTheWeek = daysOfTheWeek;
			}
			else
			{
				timeWindowInformation->daysOfTheWeek = NULL;
			}

			// StartPeriod
			jmethodID getStartPeriodMethod = (*env)->GetMethodID(env, timeWindowInformationClass, "getStartPeriod", "()Lgov/usdot/cv/rgaencoder/DDateTime;");
			jobject startPeriodObj = (*env)->CallObjectMethod(env, timeWindowInformationObj, getStartPeriodMethod);

			if (startPeriodObj != NULL)
			{
				jclass startPeriodClass = (*env)->GetObjectClass(env, startPeriodObj);

				jmethodID getStartYear = (*env)->GetMethodID(env, startPeriodClass, "getYear", "()I");
				jmethodID getStartMonth = (*env)->GetMethodID(env, startPeriodClass, "getMonth", "()I");
				jmethodID getStartDay = (*env)->GetMethodID(env, startPeriodClass, "getDay", "()I");
				jmethodID getStartHour = (*env)->GetMethodID(env, startPeriodClass, "getHour", "()I");
				jmethodID getStartMinute = (*env)->GetMethodID(env, startPeriodClass, "getMinute", "()I");
				jmethodID getStartSecond = (*env)->GetMethodID(env, startPeriodClass, "getSecond", "()I");
				jmethodID getStartOffset = (*env)->GetMethodID(env, startPeriodClass, "getOffset", "()I");


				jint startYear = (*env)->CallIntMethod(env, startPeriodObj, getStartYear);
				jint startMonth = (*env)->CallIntMethod(env, startPeriodObj, getStartMonth);
				jint startDay = (*env)->CallIntMethod(env, startPeriodObj, getStartDay);
				jint startHour = (*env)->CallIntMethod(env, startPeriodObj, getStartHour);
				jint startMinute = (*env)->CallIntMethod(env, startPeriodObj, getStartMinute);
				jint startSecond = (*env)->CallIntMethod(env, startPeriodObj, getStartSecond);
				jint startOffset = (*env)->CallIntMethod(env, startPeriodObj, getStartOffset);
		
				DYear_t *ddtStartYear = calloc(1,sizeof(DYear_t));
				DMonth_t *ddtStartMonth = calloc(1, sizeof(DMonth_t));
				DDay_t *ddtStartDay = calloc(1, sizeof(DDay_t));
				DOffset_t *ddtStartOffset = calloc(1, sizeof(DOffset_t));
				DHour_t *ddtStartHour = calloc(1, sizeof(DHour_t));
				DMinute_t *ddtStartMinute = calloc(1, sizeof(DMinute_t));
				DSecond_t *ddtStartSecond = calloc(1, sizeof(DSecond_t));

				*ddtStartYear = (long)startYear;
				*ddtStartMonth = (long)startMonth;
				*ddtStartDay = (long)startDay;
				*ddtStartHour = (long)startHour;
				*ddtStartMinute = (long)startMinute;
				*ddtStartSecond = (long)startSecond;
				*ddtStartOffset = (long)startOffset;

				timeWindowInformation->startPeriod = calloc(1, sizeof(DDateTime_t));

				timeWindowInformation->startPeriod->year = ddtStartYear;
				timeWindowInformation->startPeriod->month = ddtStartMonth;
				timeWindowInformation->startPeriod->day = ddtStartDay;
				timeWindowInformation->startPeriod->hour = ddtStartHour;
				timeWindowInformation->startPeriod->minute = ddtStartMinute;
				timeWindowInformation->startPeriod->second = ddtStartSecond;
				timeWindowInformation->startPeriod->offset = ddtStartOffset;
			}
			else
			{
				timeWindowInformation->startPeriod = NULL;
			}

			// EndPeriod
			jmethodID getEndPeriodMethod = (*env)->GetMethodID(env, timeWindowInformationClass, "getEndPeriod", "()Lgov/usdot/cv/rgaencoder/DDateTime;");
			jobject endPeriodObj = (*env)->CallObjectMethod(env, timeWindowInformationObj, getEndPeriodMethod);

			if (endPeriodObj == NULL)
			{
				timeWindowInformation->endPeriod = NULL;
			}
			else
			{
				jclass endPeriodClass = (*env)->GetObjectClass(env, endPeriodObj);
				jmethodID getEndYear = (*env)->GetMethodID(env, endPeriodClass, "getYear", "()I");
				jmethodID getEndMonth = (*env)->GetMethodID(env, endPeriodClass, "getMonth", "()I");
				jmethodID getEndDay = (*env)->GetMethodID(env, endPeriodClass, "getDay", "()I");
				jmethodID getEndHour = (*env)->GetMethodID(env, endPeriodClass, "getHour", "()I");
				jmethodID getEndMinute = (*env)->GetMethodID(env, endPeriodClass, "getMinute", "()I");
				jmethodID getEndSecond = (*env)->GetMethodID(env, endPeriodClass, "getSecond", "()I");
				jmethodID getEndOffset = (*env)->GetMethodID(env, endPeriodClass, "getOffset", "()I");

				jint endYear = (*env)->CallIntMethod(env, endPeriodObj, getEndYear);
				jint endMonth = (*env)->CallIntMethod(env, endPeriodObj, getEndMonth);
				jint endDay = (*env)->CallIntMethod(env, endPeriodObj, getEndDay);
				jint endHour = (*env)->CallIntMethod(env, endPeriodObj, getEndHour);
				jint endMinute = (*env)->CallIntMethod(env, endPeriodObj, getEndMinute);
				jint endSecond = (*env)->CallIntMethod(env, endPeriodObj, getEndSecond);
				jint endOffset = (*env)->CallIntMethod(env, endPeriodObj, getEndOffset);

				DYear_t *ddtEndYear = calloc(1,sizeof(DYear_t));
				DMonth_t *ddtEndMonth = calloc(1, sizeof(DMonth_t));
				DDay_t *ddtEndDay = calloc(1, sizeof(DDay_t));
				DOffset_t *ddtEndOffset = calloc(1, sizeof(DOffset_t));
				DHour_t *ddtEndHour = calloc(1, sizeof(DHour_t));
				DMinute_t *ddtEndMinute = calloc(1, sizeof(DMinute_t));
				DSecond_t *ddtEndSecond = calloc(1, sizeof(DSecond_t));

				*ddtEndYear = (long)endYear;
				*ddtEndMonth = (long)endMonth;
				*ddtEndDay = (long)endDay;
				*ddtEndHour = (long)endHour;
				*ddtEndMinute = (long)endMinute;
				*ddtEndSecond = (long)endSecond;
				*ddtEndOffset = (long)endOffset;

				timeWindowInformation->endPeriod = calloc(1, sizeof(DDateTime_t));

				timeWindowInformation->endPeriod->year = ddtEndYear;
				timeWindowInformation->endPeriod->month = ddtEndMonth;
				timeWindowInformation->endPeriod->day = ddtEndDay;
				timeWindowInformation->endPeriod->hour = ddtEndHour;
				timeWindowInformation->endPeriod->minute = ddtEndMinute;
				timeWindowInformation->endPeriod->second = ddtEndSecond;
				timeWindowInformation->endPeriod->offset = ddtEndOffset;
			}

			// GeneralPeriod
			jmethodID getGeneralPeriodMethod = (*env)->GetMethodID(env, timeWindowInformationClass, "getGeneralPeriod", "()Lgov/usdot/cv/rgaencoder/GeneralPeriod;");
			jobject generalPeriodObj = (*env)->CallObjectMethod(env, timeWindowInformationObj, getGeneralPeriodMethod);

			if (generalPeriodObj != NULL)
			{
				jclass generalPeriodClass = (*env)->GetObjectClass(env, generalPeriodObj);
				jmethodID getGeneralPeriodMethodValue = (*env)->GetMethodID(env, generalPeriodClass, "getGeneralPeriodValue", "()I");
				jint generalPeriodInt = (*env)->CallIntMethod(env, generalPeriodObj, getGeneralPeriodMethodValue);

				GeneralPeriod_t *generalPeriodValue = calloc(1, sizeof(GeneralPeriod_t));
				*generalPeriodValue = (long)generalPeriodInt;
				timeWindowInformation->generalPeriod = generalPeriodValue;
			}
			else
			{
				timeWindowInformation->generalPeriod = NULL;
			}

			// Adding to timeRestrictions
			ASN_SEQUENCE_ADD(&fixedTimeWindowItemCtrl->timeWindowSet.list, timeWindowInformation);
			timeRestrictions->choice.fixedTimeWindowItemCtrl = *fixedTimeWindowItemCtrl;
		}
	} else if (timeRestrictionsTypeChoice == 2) {
		timeRestrictions->present = RGATimeRestrictions_PR_otherDataSetItemCtrl;

		jmethodID getOtherDataSetItemCtrlMethod = (*env)->GetMethodID(env, timeRestrictionsClass, "getOtherDataSetItemCtrl", "()Lgov/usdot/cv/rgaencoder/OtherDSItemControlInfo;");
		jobject otherDataSetItemCtrlObj = (*env)->CallObjectMethod(env, timeRestrictionsObj, getOtherDataSetItemCtrlMethod);
		jclass otherDataSetItemCtrlClass = (*env)->GetObjectClass(env, otherDataSetItemCtrlObj);

		OtherDSItemControlInfo_t *otherDataSetItemCtrl = calloc(1, sizeof(OtherDSItemControlInfo_t));

		jmethodID getMessageID = (*env)->GetMethodID(env, otherDataSetItemCtrlClass, "getMessageID", "()J");
		jlong messageID = (*env)->CallLongMethod(env, otherDataSetItemCtrlObj, getMessageID);

		jmethodID getEnaAttributeID = (*env)->GetMethodID(env, otherDataSetItemCtrlClass, "getEnaAttributeID", "()J");
		jlong enaAttributeID = (*env)->CallIntMethod(env, otherDataSetItemCtrlObj, getEnaAttributeID);

		otherDataSetItemCtrl->messageID = (DSRCmsgID_t)((long)messageID);

		EnabledAttributeID_t *enabledAttributeID = calloc(1, sizeof(EnabledAttributeID_t));
		*enabledAttributeID = (long)enaAttributeID;
		otherDataSetItemCtrl->enaAttributeID = enabledAttributeID;

		timeRestrictions->choice.otherDataSetItemCtrl = *otherDataSetItemCtrl;
	}
}