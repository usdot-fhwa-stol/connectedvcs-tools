/*
 * Copyright (C) 2024 LEIDOS.
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
#include "gov_usdot_cv_mapencoder_Encoder.h"
#include "MessageFrame.h"
#include <stdint.h>

JNIEXPORT jbyteArray JNICALL Java_gov_usdot_cv_mapencoder_Encoder_encodeMap(JNIEnv *env, jobject cls, jint msgCount, jlong timeStamp, jlong layerType, jlong layerID, jobjectArray intersections)
{

	printf("\n ***Inside the wrapper.c file **** \n");
	uint8_t buffer[2302];
	size_t buffer_size = sizeof(buffer);
	asn_enc_rval_t ec;

	// LaneType Constants
	const int VEHICLE = 0;
	const int CROSSWALK = 1;
	const int BIKE_LANE = 2;
	const int SIDEWALK = 3;
	const int MEDIAN = 4;
	const int STRIPING = 5;
	const int TRACKED_VEHICLE = 6;
	const int PARKING = 7;

	// NodeList Choice Constants
	const int NODE_SET_XY = 0;
	const int COMPUTED_LANE = 1;

	// NodeOffSet Point Choice Constants
	const int NODE_XY1 = 1;
	const int NODE_XY2 = 2;
	const int NODE_XY3 = 3;
	const int NODE_XY4 = 4;
	const int NODE_XY5 = 5;
	const int NODE_XY6 = 6;
	const int NODE_LAT_LON = 7;
	const int NODE_REGIONAL = 8;

	// LaneData Attribute Choice Constants
	const int PATH_END_POINT_ANGLE = 1;
	const int LANE_CROWN_POINT_CENTER = 2;
	const int LANE_CROWN_POINT_RIGHT = 3;
	const int LANE_CROWN_POINT_LEFT = 4;
	const int LANE_ANGLE = 5;
	const int SPEED_LIMITS = 6;

	MessageFrame_t *message;

	message = calloc(1, sizeof(MessageFrame_t));
	if (!message)
	{
		return NULL;
	}

	// set default value of testmessage00
	message->messageId = 18;
	message->value.present = MessageFrame__value_PR_MapData;

	message->value.choice.MapData.msgIssueRevision = (long)msgCount;

	// Commenting this out to match it to the production version. Could be reused later.
	// MinuteOfTheYear_t moy;
	// moy = (long)timeStamp;
	// message->value.choice.MapData.timeStamp = &moy;

	LayerType_t lt;
	lt = (int)layerType;
	message->value.choice.MapData.layerType = &lt;

	LayerID_t id;
	id = (int)layerID;
	message->value.choice.MapData.layerID = &id;

	IntersectionGeometryList_t *intersectionsList = calloc(1, sizeof(IntersectionGeometryList_t));
	int finalNodeListChoice = 0;

	// intersections
	jsize int_count = (*env)->GetArrayLength(env, intersections);

	if (int_count > 0)
	{
		printf("**** Encoding intersections **** \n");
		for (int i = 0; i < int_count; i++)
		{
			IntersectionGeometry_t *intersection;
			intersection = calloc(1, sizeof(IntersectionGeometry_t));

			jobject intersectionObj = (jobject)(*env)->GetObjectArrayElement(env, intersections, i);
			jclass intersectionClass = (*env)->GetObjectClass(env, intersectionObj);

			// Check if intersection name exists
			jmethodID isIntersectionNameExists = (*env)->GetMethodID(env, intersectionClass, "isNameExists", "()Z");
			jboolean intersectionNameExists = (*env)->CallBooleanMethod(env, intersectionObj, isIntersectionNameExists);

			if (intersectionNameExists)
			{
				// Get Intersection Name
				jmethodID getName = (*env)->GetMethodID(env, intersectionClass, "getName", "()Ljava/lang/String;");
				jstring name = (*env)->CallObjectMethod(env, intersectionObj, getName);
				const char *nameStr = (*env)->GetStringUTFChars(env, name, 0);

				size_t nameStrLen = strlen(nameStr);
				OCTET_STRING_t octetString;
				octetString.buf = (uint8_t *)calloc(1, (nameStrLen + 1));

				for (size_t n = 0; n < nameStrLen; n++)
				{
					octetString.buf[n] = (uint8_t)nameStr[n];
				}
				octetString.size = nameStrLen;

				DescriptiveName_t *descriptiveName = calloc(1, sizeof(DescriptiveName_t));
				*descriptiveName = (IA5String_t)octetString;
				intersection->name = descriptiveName;
			}

			// Get Intersection Id
			jmethodID getIntrId = (*env)->GetMethodID(env, intersectionClass, "getId", "()Lgov/usdot/cv/mapencoder/IntersectionReferenceID;");
			jobject intrReferenceIDObj = (*env)->CallObjectMethod(env, intersectionObj, getIntrId);
			jclass intersectionReferenceIdClass = (*env)->GetObjectClass(env, intrReferenceIDObj);

			jmethodID getId = (*env)->GetMethodID(env, intersectionReferenceIdClass, "getId", "()I");
			jint geomId = (*env)->CallIntMethod(env, intrReferenceIDObj, getId);

			IntersectionID_t intersectionId;
			intersectionId = (long)geomId;
			intersection->id.id = intersectionId;

			// Check if intersection region exists
			jmethodID isRegionExists = (*env)->GetMethodID(env, intersectionReferenceIdClass, "isRegionExists", "()Z");
			jboolean regionExists = (*env)->CallBooleanMethod(env, intrReferenceIDObj, isRegionExists);

			if (regionExists)
			{
				jmethodID getRegion = (*env)->GetMethodID(env, intersectionReferenceIdClass, "getRegion", "()I");
				jint regionId = (*env)->CallIntMethod(env, intrReferenceIDObj, getRegion);

				RoadRegulatorID_t *roadRegulatorId = calloc(1, sizeof(RoadRegulatorID_t));
				*roadRegulatorId = (long)regionId;
				intersection->id.region = roadRegulatorId;
			}

			RoadAuthorityID_t *roadAuthorityID = calloc(1, sizeof(RoadAuthorityID_t));

			// Check if full RAID exists
			jmethodID isFullRdAuthIDExists = (*env)->GetMethodID(env, intersectionClass, "isFullRdAuthIDExists", "()Z");
			jboolean fullRdAuthIDExists = (*env)->CallBooleanMethod(env, intersectionObj, isFullRdAuthIDExists);

			// Check if relative RAID exists
			jmethodID isRelRdAuthIDExists = (*env)->GetMethodID(env, intersectionClass, "isRelRdAuthIDExists", "()Z");
			jboolean relRdAuthIDExists = (*env)->CallBooleanMethod(env, intersectionObj, isRelRdAuthIDExists);
			
			if (fullRdAuthIDExists)
			{
				// Get full RAID
				jmethodID getFullRdAuthID = (*env)->GetMethodID(env, intersectionClass, "getFullRdAuthID", "()[I");
				jintArray fullRdAuthID = (*env)->CallObjectMethod(env, intersectionObj, getFullRdAuthID);
				const uint32_t* fullRAID = (*env)->GetIntArrayElements(env, fullRdAuthID, NULL);

				size_t fullRAIDLen = (*env)->GetArrayLength(env, fullRdAuthID);

				OBJECT_IDENTIFIER_t *fullRAIDObjID = calloc(1, sizeof(OBJECT_IDENTIFIER_t));
				
				OBJECT_IDENTIFIER_set_arcs(fullRAIDObjID,
                               fullRAID, fullRAIDLen);

				// Set RAID pointer's PRESENT and CHOICE
				roadAuthorityID->present = RoadAuthorityID_PR_fullRdAuthID;
				roadAuthorityID->choice.fullRdAuthID = *fullRAIDObjID;
				intersection->roadAuthorityID = roadAuthorityID;
			} else if (relRdAuthIDExists) {
				// Get relative RAID
				jmethodID getRelRdAuthID = (*env)->GetMethodID(env, intersectionClass, "getRelRdAuthID", "()[I");
				jintArray relRdAuthID = (*env)->CallObjectMethod(env, intersectionObj, getRelRdAuthID);
				const uint32_t* relRAID = (*env)->GetIntArrayElements(env, relRdAuthID, NULL);
				
				size_t relRAIDLen  = (*env)->GetArrayLength(env, relRdAuthID);

				RELATIVE_OID_t *relRAIDObjID = calloc(1, sizeof(RELATIVE_OID_t));

				RELATIVE_OID_set_arcs(relRAIDObjID,
                               relRAID, relRAIDLen);

				// Set RAID pointer's PRESENT and CHOICE
				roadAuthorityID->present = RoadAuthorityID_PR_relRdAuthID;
				roadAuthorityID->choice.relRdAuthID = *relRAIDObjID;
				intersection->roadAuthorityID = roadAuthorityID;
			} else {
				roadAuthorityID->present = RoadAuthorityID_PR_NOTHING;
				intersection->roadAuthorityID = NULL;
			}

			// Get Intersection Revision
			jmethodID getRevision = (*env)->GetMethodID(env, intersectionClass, "getRevision", "()I");
			jint revision = (*env)->CallIntMethod(env, intersectionObj, getRevision);
			intersection->revision = (Common_MsgCount_t)((long)revision);

			// Get Intersection Ref Point
			jmethodID getRefPoint = (*env)->GetMethodID(env, intersectionClass, "getRefPoint", "()Lgov/usdot/cv/mapencoder/Position3D;");
			jobject position3DObj = (*env)->CallObjectMethod(env, intersectionObj, getRefPoint);
			Position3D_t refPoint;

			jclass position3DClass = (*env)->GetObjectClass(env, position3DObj);

			jmethodID getLatitude = (*env)->GetMethodID(env, position3DClass, "getLatitude", "()D");
			jmethodID getLongitude = (*env)->GetMethodID(env, position3DClass, "getLongitude", "()D");

			jdouble latitude = (*env)->CallDoubleMethod(env, position3DObj, getLatitude);
			jdouble longitude = (*env)->CallDoubleMethod(env, position3DObj, getLongitude);

			refPoint.lat = (Common_Latitude_t)((long)latitude);
			refPoint.Long = (Common_Longitude_t)((long)longitude);

			// Check if elevation exists
			jmethodID isElevationExists = (*env)->GetMethodID(env, position3DClass, "isElevationExists", "()Z");
			jboolean elevationExists = (*env)->CallBooleanMethod(env, position3DObj, isElevationExists);

			if (elevationExists)
			{
				jmethodID getElevation = (*env)->GetMethodID(env, position3DClass, "getElevation", "()F");
				jfloat elevation = (*env)->CallFloatMethod(env, position3DObj, getElevation);

				Common_Elevation_t *dsrcElevation = calloc(1, sizeof(Common_Elevation_t));
				*dsrcElevation = (long)elevation;
				refPoint.elevation = dsrcElevation;
			}
			else
			{
				refPoint.elevation = NULL;
			}

			refPoint.regional = NULL;
			intersection->refPoint = refPoint;

			// Check if lane width exists
			jmethodID isLaneWidthExists = (*env)->GetMethodID(env, intersectionClass, "isLaneWidthExists", "()Z");
			jboolean laneWidthExists = (*env)->CallBooleanMethod(env, intersectionObj, isLaneWidthExists);

			if (laneWidthExists)
			{
				// Get Intersection LaneWidth
				jmethodID getLaneWidth = (*env)->GetMethodID(env, intersectionClass, "getLaneWidth", "()I");
				jint laneWidth = (*env)->CallIntMethod(env, intersectionObj, getLaneWidth);

				LaneWidth_t *intrLaneWidth = calloc(1, sizeof(LaneWidth_t));
				*intrLaneWidth = (long)laneWidth;
				intersection->laneWidth = intrLaneWidth;
			}

			// Check if speed limits exist
			jmethodID isSpeedLimitsExists = (*env)->GetMethodID(env, intersectionClass, "isSpeedLimitsExists", "()Z");
			jboolean speedLimitsExists = (*env)->CallBooleanMethod(env, intersectionObj, isSpeedLimitsExists);

			if (speedLimitsExists)
			{
				// Get SpeedLimitList
				jmethodID getSpeedLimits = (*env)->GetMethodID(env, intersectionClass, "getSpeedLimits", "()Lgov/usdot/cv/mapencoder/SpeedLimitList;");
				jobject speedLimitListObj = (*env)->CallObjectMethod(env, intersectionObj, getSpeedLimits);
				jclass speedLimitListClass = (*env)->GetObjectClass(env, speedLimitListObj);

				jmethodID getRegulatorySpeedLimitList = (*env)->GetMethodID(env, speedLimitListClass, "getSpeedLimits", "()[Lgov/usdot/cv/mapencoder/RegulatorySpeedLimit;");
				jobject regulatorySpeedLimitListArray = (*env)->CallObjectMethod(env, speedLimitListObj, getRegulatorySpeedLimitList);

				jsize speedLimitsCount = (*env)->GetArrayLength(env, regulatorySpeedLimitListArray);

				SpeedLimitList_t *speedLimitList;
				speedLimitList = calloc(1, sizeof(SpeedLimitList_t));

				for (int speedIndex = 0; speedIndex < speedLimitsCount; speedIndex++)
				{
					RegulatorySpeedLimit_t *regulatorySpeedLimit;
					regulatorySpeedLimit = calloc(1, sizeof(RegulatorySpeedLimit_t));

					// Get each RegulatorySpeedLimit from Speed Limit List
					jobject regulatorySpeedLimitObj = (jobject)(*env)->GetObjectArrayElement(env, regulatorySpeedLimitListArray, speedIndex);
					jclass regulatorySpeedLimitClass = (*env)->GetObjectClass(env, regulatorySpeedLimitObj);

					// Get Speed from RegulatorySpeedLimit
					jmethodID getSpeed = (*env)->GetMethodID(env, regulatorySpeedLimitClass, "getSpeed", "()D");
					jdouble speed = (*env)->CallDoubleMethod(env, regulatorySpeedLimitObj, getSpeed);

					// Get SpeedType from each RegulatorySpeedLimit
					jmethodID getType = (*env)->GetMethodID(env, regulatorySpeedLimitClass, "getType", "()Lgov/usdot/cv/mapencoder/SpeedLimitType;");
					jobject speedLimitTypeObj = (*env)->CallObjectMethod(env, regulatorySpeedLimitObj, getType);
					jclass speedLimitTypeClass = (*env)->GetObjectClass(env, speedLimitTypeObj);

					// Get SpeedLimitType from Lane Direction Object
					jmethodID getSpeedLimitType = (*env)->GetMethodID(env, speedLimitTypeClass, "getSpeedLimitType", "()B");
					jbyte speedLimitType = (*env)->CallByteMethod(env, speedLimitTypeObj, getSpeedLimitType);

					regulatorySpeedLimit->speed = (Velocity_t)((long)speed);
					regulatorySpeedLimit->type = (SpeedLimitType_t)((long)speedLimitType);

					ASN_SEQUENCE_ADD(&speedLimitList->list, regulatorySpeedLimit);
				}

				intersection->speedLimits = speedLimitList;
			}

			LaneList_t *laneList;
			laneList = calloc(1, sizeof(LaneList_t));

			// Get LaneSet object and class
			jmethodID getLaneSet = (*env)->GetMethodID(env, intersectionClass, "getLaneSet", "()Lgov/usdot/cv/mapencoder/LaneList;");
			jobject laneSetObj = (*env)->CallObjectMethod(env, intersectionObj, getLaneSet);
			jclass laneListClass = (*env)->GetObjectClass(env, laneSetObj);

			// Get sequence of Generic Lane from Lane Set
			jmethodID getLaneList = (*env)->GetMethodID(env, laneListClass, "getLaneList", "()[Lgov/usdot/cv/mapencoder/GenericLane;");
			jobject laneListArray = (*env)->CallObjectMethod(env, laneSetObj, getLaneList);

			jsize laneCount = (*env)->GetArrayLength(env, laneListArray);

			for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
			{
				GenericLane_t *genericLane;
				genericLane = calloc(1, sizeof(GenericLane_t));

				// Get each Generic Lane from Lane Set
				jobject laneObj = (jobject)(*env)->GetObjectArrayElement(env, laneListArray, laneIndex);
				jclass genericLaneClass = (*env)->GetObjectClass(env, laneObj);

				// Get LaneID from each Generic Lane
				jmethodID getLaneID = (*env)->GetMethodID(env, genericLaneClass, "getLaneID", "()I");
				jint laneId = (*env)->CallIntMethod(env, laneObj, getLaneID);
				LaneID_t laneID;
				laneID = (long)laneId;

				genericLane->laneID = laneID;

				// Check if Lane Name exists
				jmethodID isLaneNameExists = (*env)->GetMethodID(env, genericLaneClass, "isNameExists", "()Z");
				jboolean laneNameExists = (*env)->CallBooleanMethod(env, laneObj, isLaneNameExists);

				if (laneNameExists)
				{
					// Get Lane Name from each Generic Lane
					jmethodID getLaneName = (*env)->GetMethodID(env, genericLaneClass, "getName", "()Ljava/lang/String;");
					jstring laneName = (*env)->CallObjectMethod(env, laneObj, getLaneName);
					const char *laneNameStr = (*env)->GetStringUTFChars(env, laneName, 0);

					size_t laneNameStrLen = strlen(laneNameStr);
					OCTET_STRING_t laneNameOctetString;
					laneNameOctetString.buf = (uint8_t *)calloc(1, (laneNameStrLen + 1));

					for (size_t l = 0; l < laneNameStrLen; l++)
					{
						laneNameOctetString.buf[l] = (uint8_t)laneNameStr[l];
					}
					laneNameOctetString.size = laneNameStrLen;

					DescriptiveName_t *laneDescriptiveName = calloc(1, sizeof(DescriptiveName_t));
					*laneDescriptiveName = (IA5String_t)laneNameOctetString;
					genericLane->name = laneDescriptiveName;
				}

				// Check if IngressApproach exists
				jmethodID isIngressApproachExists = (*env)->GetMethodID(env, genericLaneClass, "isIngressApproachExists", "()Z");
				jboolean ingressApproachExists = (*env)->CallBooleanMethod(env, laneObj, isIngressApproachExists);

				if (ingressApproachExists)
				{
					// Get IngressApproach from Generic Lane
					jmethodID getIngressApproach = (*env)->GetMethodID(env, genericLaneClass, "getIngressApproach", "()B");
					jbyte ingressApproachByte = (*env)->CallByteMethod(env, laneObj, getIngressApproach);

					ApproachID_t *ingressApproachId = calloc(1, sizeof(ApproachID_t));
					*ingressApproachId = (long)ingressApproachByte;
					genericLane->ingressApproach = ingressApproachId;
				}

				// Check if EgressApproach exists
				jmethodID isEgressApproachExists = (*env)->GetMethodID(env, genericLaneClass, "isEgressApproachExists", "()Z");
				jboolean egressApproachExists = (*env)->CallBooleanMethod(env, laneObj, isEgressApproachExists);

				if (egressApproachExists)
				{
					// Get EgressApproach from Generic Lane
					jmethodID getEgressApproach = (*env)->GetMethodID(env, genericLaneClass, "getEgressApproach", "()B");
					jbyte egressApproachByte = (*env)->CallByteMethod(env, laneObj, getEgressApproach);

					ApproachID_t *egressApproachId = calloc(1, sizeof(ApproachID_t));
					*egressApproachId = (long)egressApproachByte;
					genericLane->egressApproach = egressApproachId;
				}

				// Check if Maneuvers exists
				jmethodID isManeuversExists = (*env)->GetMethodID(env, genericLaneClass, "isManeuversExists", "()Z");
				jboolean maneuversExists = (*env)->CallBooleanMethod(env, laneObj, isManeuversExists);

				if (maneuversExists)
				{
					// Get Maneuvers from each Generic Lane
					jmethodID getManeuvers = (*env)->GetMethodID(env, genericLaneClass, "getManeuvers", "()Lgov/usdot/cv/mapencoder/AllowedManeuvers;");
					jobject maneuversObj = (*env)->CallObjectMethod(env, laneObj, getManeuvers);
					jclass maneuversClass = (*env)->GetObjectClass(env, maneuversObj);

					// Get Allowed Maneuvers from each Maneuvers
					jmethodID getAllowedManeuvers = (*env)->GetMethodID(env, maneuversClass, "getAllowedManeuvers", "()I");
					jint allowedManeuversData = (*env)->CallIntMethod(env, maneuversObj, getAllowedManeuvers);

					uint16_t allowedManeuversContent = allowedManeuversData;
					BIT_STRING_t allowedManeuversBitString;

					// Converting the allowedManeuversContent to BIT_STRING
					allowedManeuversBitString.buf = (uint8_t *)calloc(2, sizeof(uint8_t));
					allowedManeuversBitString.buf[1] = allowedManeuversContent;
					allowedManeuversBitString.buf[0] = allowedManeuversContent >> 8;
					allowedManeuversBitString.size = (size_t)2;
					allowedManeuversBitString.bits_unused = 4;

					AllowedManeuvers_t *allowedManeuvers = calloc(1, sizeof(AllowedManeuvers_t));
					*allowedManeuvers = allowedManeuversBitString;
					genericLane->maneuvers = allowedManeuvers;
				}

				// Get Lane Attributes from each Generic Lane
				jmethodID getLaneAttributes = (*env)->GetMethodID(env, genericLaneClass, "getLaneAttributes", "()Lgov/usdot/cv/mapencoder/LaneAttributes;");
				jobject laneAttributesObj = (*env)->CallObjectMethod(env, laneObj, getLaneAttributes);
				jclass laneAttributesClass = (*env)->GetObjectClass(env, laneAttributesObj);

				// Get Lane Direction Object from Lane Attributes
				jmethodID getLaneDirectionAttribute = (*env)->GetMethodID(env, laneAttributesClass, "getLaneDirectionAttribute", "()Lgov/usdot/cv/mapencoder/LaneDirection;");
				jobject laneDirectionObj = (*env)->CallObjectMethod(env, laneAttributesObj, getLaneDirectionAttribute);
				jclass laneDirectionClass = (*env)->GetObjectClass(env, laneDirectionObj);

				// Get laneDirection from Lane Direction Object
				jmethodID getLaneDirection = (*env)->GetMethodID(env, laneDirectionClass, "getLaneDirection", "()B");
				jbyte laneDirection = (*env)->CallByteMethod(env, laneDirectionObj, getLaneDirection);

				uint8_t laneDirectionContent = laneDirection;
				BIT_STRING_t laneDirectionBitString;

				// Converting the laneDirection to BIT_STRING
				laneDirectionBitString.buf = (uint8_t *)calloc(1, sizeof(uint8_t));
				*laneDirectionBitString.buf = laneDirectionContent;
				laneDirectionBitString.size = (size_t)1;
				laneDirectionBitString.bits_unused = 6;

				// Assigning laneDirection to laneAttributes
				LaneAttributes_t laneAttributes;
				laneAttributes.directionalUse = (LaneDirection_t)laneDirectionBitString;

				// Get Lane Sharing Object from Lane Attributes
				jmethodID getLaneSharingAttribute = (*env)->GetMethodID(env, laneAttributesClass, "getLaneSharingAttribute", "()Lgov/usdot/cv/mapencoder/LaneSharing;");
				jobject laneSharingObj = (*env)->CallObjectMethod(env, laneAttributesObj, getLaneSharingAttribute);
				jclass laneSharingClass = (*env)->GetObjectClass(env, laneSharingObj);

				// Get laneSharing from Lane Sharing Object
				jmethodID getLaneSharing = (*env)->GetMethodID(env, laneSharingClass, "getLaneSharing", "()S");
				jshort laneSharing = (*env)->CallShortMethod(env, laneSharingObj, getLaneSharing);

				uint16_t laneSharingContent = laneSharing;
				BIT_STRING_t laneSharingBitString;

				// Converting the laneSharing to BIT_STRING
				laneSharingBitString.buf = (uint8_t *)calloc(2, sizeof(uint8_t));
				laneSharingBitString.buf[1] = laneSharingContent;
				laneSharingBitString.buf[0] = laneSharingContent >> 8;
				laneSharingBitString.size = (size_t)2;
				laneSharingBitString.bits_unused = 6;

				laneAttributes.sharedWith = (LaneSharing_t)laneSharingBitString;

				// Get Lane Type Object from Lane Attributes
				jmethodID getLaneTypeAttribute = (*env)->GetMethodID(env, laneAttributesClass, "getLaneTypeAttribute", "()Lgov/usdot/cv/mapencoder/LaneTypeAttributes;");
				jobject laneTypeObj = (*env)->CallObjectMethod(env, laneAttributesObj, getLaneTypeAttribute);
				jclass laneTypeClass = (*env)->GetObjectClass(env, laneTypeObj);

				// Get laneType from Lane Type Object
				jmethodID getLaneType = (*env)->GetMethodID(env, laneTypeClass, "getChoice", "()B");
				jbyte laneType = (*env)->CallByteMethod(env, laneTypeObj, getLaneType);

				LaneTypeAttributes_t laneTypeAttributes;
				BIT_STRING_t laneTypeAttributesBitString;

				// Check laneType and assign the appropriate choice
				if (laneType == VEHICLE)
				{
					// Get vehicle from Lane Type Attributes Object
					jmethodID getVehicle = (*env)->GetMethodID(env, laneTypeClass, "getVehicle", "()Lgov/usdot/cv/mapencoder/LaneAttributesVehicle;");
					jobject vehicleAttributesObj = (*env)->CallObjectMethod(env, laneTypeObj, getVehicle);
					jclass vehicleAttributesClass = (*env)->GetObjectClass(env, vehicleAttributesObj);

					// Get Attributes from vehicleAttribute Object
					jmethodID getLaneAttributesVehicle = (*env)->GetMethodID(env, vehicleAttributesClass, "getLaneAttributesVehicle", "()B");
					jbyte vehicleAttributes = (*env)->CallByteMethod(env, vehicleAttributesObj, getLaneAttributesVehicle);

					uint8_t laneTypeAttributesContent = vehicleAttributes;
					laneTypeAttributesBitString.buf = (uint8_t *)calloc(1, sizeof(uint8_t));
					*laneTypeAttributesBitString.buf = laneTypeAttributesContent;
					laneTypeAttributesBitString.size = (size_t)1;
					laneTypeAttributesBitString.bits_unused = 0;

					laneTypeAttributes.present = LaneTypeAttributes_PR_vehicle;
					laneTypeAttributes.choice.vehicle = (LaneAttributes_Vehicle_t)laneTypeAttributesBitString;
				}
				else if (laneType == CROSSWALK)
				{
					// Get crosswalk from Lane Type Attributes Object
					jmethodID getCrosswalk = (*env)->GetMethodID(env, laneTypeClass, "getCrosswalk", "()Lgov/usdot/cv/mapencoder/LaneAttributesCrosswalk;");
					jobject crosswalkAttributesObj = (*env)->CallObjectMethod(env, laneTypeObj, getCrosswalk);
					jclass crosswalkAttributesClass = (*env)->GetObjectClass(env, crosswalkAttributesObj);

					// Get attributes from crosswalkAttributes Object
					jmethodID getLaneAttributesCrosswalk = (*env)->GetMethodID(env, crosswalkAttributesClass, "getLaneAttributesCrosswalk", "()S");
					jshort crosswalkAttributes = (*env)->CallShortMethod(env, crosswalkAttributesObj, getLaneAttributesCrosswalk);

					uint16_t laneTypeAttributesContent = crosswalkAttributes;
					laneTypeAttributesBitString.buf = (uint8_t *)calloc(2, sizeof(uint8_t));
					laneTypeAttributesBitString.buf[1] = laneTypeAttributesContent;
					laneTypeAttributesBitString.buf[0] = laneTypeAttributesContent >> 8;
					laneTypeAttributesBitString.size = (size_t)2;
					laneTypeAttributesBitString.bits_unused = 7;

					laneTypeAttributes.present = LaneTypeAttributes_PR_crosswalk;
					laneTypeAttributes.choice.crosswalk = (LaneAttributes_Crosswalk_t)laneTypeAttributesBitString;
				}
				else if (laneType == BIKE_LANE)
				{
					// Get bikeLane from Lane Type Attributes Object
					jmethodID getBikeLane = (*env)->GetMethodID(env, laneTypeClass, "getBikeLane", "()Lgov/usdot/cv/mapencoder/LaneAttributesBike;");
					jobject bikeLaneAttributesObj = (*env)->CallObjectMethod(env, laneTypeObj, getBikeLane);
					jclass bikeLaneAttributesClass = (*env)->GetObjectClass(env, bikeLaneAttributesObj);

					// Get attributes from bikeLaneAttributes Object
					jmethodID getLaneAttributesBikeLane = (*env)->GetMethodID(env, bikeLaneAttributesClass, "getLaneAttributesBike", "()S");
					jshort bikeLaneAttributes = (*env)->CallShortMethod(env, bikeLaneAttributesObj, getLaneAttributesBikeLane);

					uint8_t laneTypeAttributesContent = bikeLaneAttributes;
					laneTypeAttributesBitString.buf = (uint8_t *)calloc(1, sizeof(uint8_t));
					*laneTypeAttributesBitString.buf = laneTypeAttributesContent;
					laneTypeAttributesBitString.size = (size_t)1;
					laneTypeAttributesBitString.bits_unused = 1;

					laneTypeAttributes.present = LaneTypeAttributes_PR_bikeLane;
					laneTypeAttributes.choice.bikeLane = (LaneAttributes_Bike_t)laneTypeAttributesBitString;
				}
				else if (laneType == SIDEWALK)
				{
					// Get sidewalk from Lane Type Attributes Object
					jmethodID getSidewalk = (*env)->GetMethodID(env, laneTypeClass, "getSidewalk", "()Lgov/usdot/cv/mapencoder/LaneAttributesSidewalk;");
					jobject sidewalkAttributesObj = (*env)->CallObjectMethod(env, laneTypeObj, getSidewalk);
					jclass sidewalkAttributesClass = (*env)->GetObjectClass(env, sidewalkAttributesObj);

					// Get attributes from sidewalkAttributes Object
					jmethodID getLaneAttributesSideWalk = (*env)->GetMethodID(env, sidewalkAttributesClass, "getLaneAttributesSidewalk", "()S");
					jshort sidewalkAttributes = (*env)->CallShortMethod(env, sidewalkAttributesObj, getLaneAttributesSideWalk);

					uint8_t laneTypeAttributesContent = sidewalkAttributes;
					laneTypeAttributesBitString.buf = (uint8_t *)calloc(1, sizeof(uint8_t));
					*laneTypeAttributesBitString.buf = laneTypeAttributesContent;
					laneTypeAttributesBitString.size = (size_t)1;
					laneTypeAttributesBitString.bits_unused = 4;

					laneTypeAttributes.present = LaneTypeAttributes_PR_sidewalk;
					laneTypeAttributes.choice.sidewalk = (LaneAttributes_Sidewalk_t)laneTypeAttributesBitString;
				}
				else if (laneType == MEDIAN)
				{
					// Get median from Lane Type Attributes Object
					jmethodID getMedian = (*env)->GetMethodID(env, laneTypeClass, "getMedian", "()Lgov/usdot/cv/mapencoder/LaneAttributesBarrier;");
					jobject medianAttributesObj = (*env)->CallObjectMethod(env, laneTypeObj, getMedian);
					jclass medianAttributesClass = (*env)->GetObjectClass(env, medianAttributesObj);

					// Get attributes from medianAttributes Object
					jmethodID getLaneAttributesBarrier = (*env)->GetMethodID(env, medianAttributesClass, "getLaneAttributesBarrier", "()S");
					jshort medianAttributes = (*env)->CallShortMethod(env, medianAttributesObj, getLaneAttributesBarrier);

					uint16_t laneTypeAttributesContent = medianAttributes;
					laneTypeAttributesBitString.buf = (uint8_t *)calloc(2, sizeof(uint8_t));
					laneTypeAttributesBitString.buf[1] = laneTypeAttributesContent;
					laneTypeAttributesBitString.buf[0] = laneTypeAttributesContent >> 8;
					laneTypeAttributesBitString.size = (size_t)2;
					laneTypeAttributesBitString.bits_unused = 7;

					laneTypeAttributes.present = LaneTypeAttributes_PR_median;
					laneTypeAttributes.choice.median = (LaneAttributes_Barrier_t)laneTypeAttributesBitString;
				}
				else if (laneType == STRIPING)
				{
					// Get Striping from Lane Type Attributes Object
					jmethodID getStriping = (*env)->GetMethodID(env, laneTypeClass, "getStriping", "()Lgov/usdot/cv/mapencoder/LaneAttributesStriping;");
					jobject stripingAttributesObj = (*env)->CallObjectMethod(env, laneTypeObj, getStriping);
					jclass stripingAttributesClass = (*env)->GetObjectClass(env, stripingAttributesObj);

					// Get attributes from medianAttributes Object
					jmethodID getLaneAttributesStriping = (*env)->GetMethodID(env, stripingAttributesClass, "getLaneAttributesStriping", "()S");
					jshort stripingAttributes = (*env)->CallShortMethod(env, stripingAttributesObj, getLaneAttributesStriping);

					uint8_t laneTypeAttributesContent = stripingAttributes;
					laneTypeAttributesBitString.buf = (uint8_t *)calloc(1, sizeof(uint8_t));
					*laneTypeAttributesBitString.buf = laneTypeAttributesContent;
					laneTypeAttributesBitString.size = (size_t)1;
					laneTypeAttributesBitString.bits_unused = 2;

					laneTypeAttributes.present = LaneTypeAttributes_PR_striping;
					laneTypeAttributes.choice.striping = (LaneAttributes_Striping_t)laneTypeAttributesBitString;
				}
				else if (laneType == TRACKED_VEHICLE)
				{
					// Get trackedVehicle from Lane Type Attributes Object
					jmethodID getTrackedVehicle = (*env)->GetMethodID(env, laneTypeClass, "getTrackedVehicle", "()Lgov/usdot/cv/mapencoder/LaneAttributesTrackedVehicle;");
					jobject trackedVehicleAttributesObj = (*env)->CallObjectMethod(env, laneTypeObj, getTrackedVehicle);
					jclass trackedVehicleAttributesClass = (*env)->GetObjectClass(env, trackedVehicleAttributesObj);

					// Get attributes from trackedVehicleAttributes Object
					jmethodID getLaneAttributesTrackedVehicle = (*env)->GetMethodID(env, trackedVehicleAttributesClass, "getLaneAttributesTrackedVehicle", "()S");
					jshort trackedVehicleAttributes = (*env)->CallShortMethod(env, trackedVehicleAttributesObj, getLaneAttributesTrackedVehicle);

					uint8_t laneTypeAttributesContent = trackedVehicleAttributes;
					laneTypeAttributesBitString.buf = (uint8_t *)calloc(1, sizeof(uint8_t));
					*laneTypeAttributesBitString.buf = laneTypeAttributesContent;
					laneTypeAttributesBitString.size = (size_t)1;
					laneTypeAttributesBitString.bits_unused = 3;

					laneTypeAttributes.present = LaneTypeAttributes_PR_trackedVehicle;
					laneTypeAttributes.choice.trackedVehicle = (LaneAttributes_TrackedVehicle_t)laneTypeAttributesBitString;
				}
				else if (laneType == PARKING)
				{
					// Get parking from Lane Type Attributes Object
					jmethodID getParking = (*env)->GetMethodID(env, laneTypeClass, "getParking", "()Lgov/usdot/cv/mapencoder/LaneAttributesParking;");
					jobject parkingAttributesObj = (*env)->CallObjectMethod(env, laneTypeObj, getParking);
					jclass parkingVehicleAttributesClass = (*env)->GetObjectClass(env, parkingAttributesObj);

					// Get attributes from parkingAttributes Object
					jmethodID getLaneAttributesParking = (*env)->GetMethodID(env, parkingVehicleAttributesClass, "getLaneAttributesParking", "()S");
					jshort parkingAttributes = (*env)->CallShortMethod(env, parkingAttributesObj, getLaneAttributesParking);

					uint8_t laneTypeAttributesContent = parkingAttributes;
					laneTypeAttributesBitString.buf = (uint8_t *)calloc(1, sizeof(uint8_t));
					*laneTypeAttributesBitString.buf = laneTypeAttributesContent;
					laneTypeAttributesBitString.size = (size_t)1;
					laneTypeAttributesBitString.bits_unused = 1;

					laneTypeAttributes.present = LaneTypeAttributes_PR_parking;
					laneTypeAttributes.choice.parking = (LaneAttributes_Parking_t)laneTypeAttributesBitString;
				}
				else
				{
					laneTypeAttributes.present = LaneTypeAttributes_PR_NOTHING;
				}

				// Assign Lane Type Attributes to Lane Attributes
				laneAttributes.laneType = laneTypeAttributes;
				laneAttributes.regional = NULL;

				// Get NodeListXY Object from Generic Lane
				jmethodID getNodeList = (*env)->GetMethodID(env, genericLaneClass, "getNodeList", "()Lgov/usdot/cv/mapencoder/NodeListXY;");
				jobject nodeListObj = (*env)->CallObjectMethod(env, laneObj, getNodeList);
				jclass nodeListClass = (*env)->GetObjectClass(env, nodeListObj);

				// Get Choice from NodeListXY object
				jmethodID getNodeListChoice = (*env)->GetMethodID(env, nodeListClass, "getChoice", "()B");
				jbyte nodeListChoice = (*env)->CallByteMethod(env, nodeListObj, getNodeListChoice);

				NodeSetXY_t *nodeSetXyList;
				nodeSetXyList = calloc(1, sizeof(NodeSetXY_t));
				finalNodeListChoice = nodeListChoice;

				if (nodeListChoice == NODE_SET_XY)
				{
					// Choice is NodeListXY_PR_nodes;
					genericLane->nodeList.present = NodeListXY_PR_nodes;

					//  Get NodeSetXY Object from NodeListXY
					jmethodID getNodes = (*env)->GetMethodID(env, nodeListClass, "getNodes", "()Lgov/usdot/cv/mapencoder/NodeSetXY;");
					jobject nodesObj = (*env)->CallObjectMethod(env, nodeListObj, getNodes);
					jclass nodesClass = (*env)->GetObjectClass(env, nodesObj);

					jmethodID getNodeSetXY = (*env)->GetMethodID(env, nodesClass, "getNodeSetXY", "()[Lgov/usdot/cv/mapencoder/NodeXY;");
					jobject nodeSetXYArray = (*env)->CallObjectMethod(env, nodesObj, getNodeSetXY);

					jsize nodesCount = (*env)->GetArrayLength(env, nodeSetXYArray);

					for (int nodesIndex = 0; nodesIndex < nodesCount; nodesIndex++)
					{
						NodeXY_t *nodeXy;
						nodeXy = calloc(1, sizeof(NodeXY_t));

						// Get each NodeXY from NodeSetXY
						jobject nodeXYObj = (jobject)(*env)->GetObjectArrayElement(env, nodeSetXYArray, nodesIndex);
						jclass nodeXYClass = (*env)->GetObjectClass(env, nodeXYObj);

						// Get Delta object and class
						jmethodID getDelta = (*env)->GetMethodID(env, nodeXYClass, "getDelta", "()Lgov/usdot/cv/mapencoder/NodeOffsetPointXY;");
						jobject deltaObj = (*env)->CallObjectMethod(env, nodeXYObj, getDelta);
						jclass nodeOffsetPointXYClass = (*env)->GetObjectClass(env, deltaObj);

						// 	Get NodeOffsetPointXY choice
						jmethodID getNodeOffsetPointXYChoice = (*env)->GetMethodID(env, nodeOffsetPointXYClass, "getChoice", "()B");
						jbyte nodeOffsetPointXYChoice = (*env)->CallByteMethod(env, deltaObj, getNodeOffsetPointXYChoice);

						if (nodeOffsetPointXYChoice == NODE_XY1)
						{
							jmethodID getNodeXY1 = (*env)->GetMethodID(env, nodeOffsetPointXYClass, "getNodeXY1", "()Lgov/usdot/cv/mapencoder/NodeXY20b;");
							jobject nodeXY1Obj = (*env)->CallObjectMethod(env, deltaObj, getNodeXY1);
							jclass nodeXY20bClass = (*env)->GetObjectClass(env, nodeXY1Obj);

							jmethodID getNodeXY20bX = (*env)->GetMethodID(env, nodeXY20bClass, "getX", "()F");
							jfloat nodeXY20bX = (*env)->CallFloatMethod(env, nodeXY1Obj, getNodeXY20bX);

							jmethodID getNodeXY20bY = (*env)->GetMethodID(env, nodeXY20bClass, "getY", "()F");
							jfloat nodeXY20bY = (*env)->CallFloatMethod(env, nodeXY1Obj, getNodeXY20bY);

							nodeXy->delta.present = NodeOffsetPointXY_PR_node_XY1;
							nodeXy->delta.choice.node_XY1.x = (Offset_B10_t)((long)nodeXY20bX);
							nodeXy->delta.choice.node_XY1.y = (Offset_B10_t)((long)nodeXY20bY);
						}
						else if (nodeOffsetPointXYChoice == NODE_XY2)
						{
							jmethodID getNodeXY2 = (*env)->GetMethodID(env, nodeOffsetPointXYClass, "getNodeXY2", "()Lgov/usdot/cv/mapencoder/NodeXY22b;");
							jobject nodeXY2Obj = (*env)->CallObjectMethod(env, deltaObj, getNodeXY2);
							jclass nodeXY22bClass = (*env)->GetObjectClass(env, nodeXY2Obj);

							jmethodID getNodeXY22bX = (*env)->GetMethodID(env, nodeXY22bClass, "getX", "()F");
							jfloat nodeXY22bX = (*env)->CallFloatMethod(env, nodeXY2Obj, getNodeXY22bX);

							jmethodID getNodeXY22bY = (*env)->GetMethodID(env, nodeXY22bClass, "getY", "()F");
							jfloat nodeXY22bY = (*env)->CallFloatMethod(env, nodeXY2Obj, getNodeXY22bY);

							nodeXy->delta.present = NodeOffsetPointXY_PR_node_XY2;
							nodeXy->delta.choice.node_XY2.x = (Offset_B11_t)((long)nodeXY22bX);
							nodeXy->delta.choice.node_XY2.y = (Offset_B11_t)((long)nodeXY22bY);
						}
						else if (nodeOffsetPointXYChoice == NODE_XY3)
						{
							jmethodID getNodeXY3 = (*env)->GetMethodID(env, nodeOffsetPointXYClass, "getNodeXY3", "()Lgov/usdot/cv/mapencoder/NodeXY24b;");
							jobject nodeXY3Obj = (*env)->CallObjectMethod(env, deltaObj, getNodeXY3);
							jclass nodeXY24bClass = (*env)->GetObjectClass(env, nodeXY3Obj);

							jmethodID getNodeXY24bX = (*env)->GetMethodID(env, nodeXY24bClass, "getX", "()S");
							jshort nodeXY24bX = (*env)->CallShortMethod(env, nodeXY3Obj, getNodeXY24bX);

							jmethodID getNodeXY24bY = (*env)->GetMethodID(env, nodeXY24bClass, "getY", "()S");
							jshort nodeXY24bY = (*env)->CallShortMethod(env, nodeXY3Obj, getNodeXY24bY);

							nodeXy->delta.present = NodeOffsetPointXY_PR_node_XY3;
							nodeXy->delta.choice.node_XY3.x = (Offset_B12_t)((long)nodeXY24bX);
							nodeXy->delta.choice.node_XY3.y = (Offset_B12_t)((long)nodeXY24bY);
						}
						else if (nodeOffsetPointXYChoice == NODE_XY4)
						{
							jmethodID getNodeXY4 = (*env)->GetMethodID(env, nodeOffsetPointXYClass, "getNodeXY4", "()Lgov/usdot/cv/mapencoder/NodeXY26b;");
							jobject nodeXY4Obj = (*env)->CallObjectMethod(env, deltaObj, getNodeXY4);
							jclass nodeXY26bClass = (*env)->GetObjectClass(env, nodeXY4Obj);

							jmethodID getNodeXY26bX = (*env)->GetMethodID(env, nodeXY26bClass, "getX", "()F");
							jfloat nodeXY26bX = (*env)->CallFloatMethod(env, nodeXY4Obj, getNodeXY26bX);

							jmethodID getNodeXY26bY = (*env)->GetMethodID(env, nodeXY26bClass, "getY", "()F");
							jfloat nodeXY26bY = (*env)->CallFloatMethod(env, nodeXY4Obj, getNodeXY26bY);

							nodeXy->delta.present = NodeOffsetPointXY_PR_node_XY4;
							nodeXy->delta.choice.node_XY4.x = (Offset_B13_t)((long)nodeXY26bX);
							nodeXy->delta.choice.node_XY4.y = (Offset_B13_t)((long)nodeXY26bY);
						}
						else if (nodeOffsetPointXYChoice == NODE_XY5)
						{

							jmethodID getNodeXY5 = (*env)->GetMethodID(env, nodeOffsetPointXYClass, "getNodeXY5", "()Lgov/usdot/cv/mapencoder/NodeXY28b;");
							jobject nodeXY5Obj = (*env)->CallObjectMethod(env, deltaObj, getNodeXY5);
							jclass nodeXY28bClass = (*env)->GetObjectClass(env, nodeXY5Obj);

							jmethodID getNodeXY28bX = (*env)->GetMethodID(env, nodeXY28bClass, "getX", "()F");
							jfloat nodeXY28bX = (*env)->CallFloatMethod(env, nodeXY5Obj, getNodeXY28bX);

							jmethodID getNodeXY28bY = (*env)->GetMethodID(env, nodeXY28bClass, "getY", "()F");
							jfloat nodeXY28bY = (*env)->CallFloatMethod(env, nodeXY5Obj, getNodeXY28bY);

							nodeXy->delta.present = NodeOffsetPointXY_PR_node_XY5;
							nodeXy->delta.choice.node_XY5.x = (Offset_B14_t)((long)nodeXY28bX);
							nodeXy->delta.choice.node_XY5.y = (Offset_B14_t)((long)nodeXY28bY);
						}
						else if (nodeOffsetPointXYChoice == NODE_XY6)
						{
							jmethodID getNodeXY6 = (*env)->GetMethodID(env, nodeOffsetPointXYClass, "getNodeXY6", "()Lgov/usdot/cv/mapencoder/NodeXY32b;");
							jobject nodeXY6Obj = (*env)->CallObjectMethod(env, deltaObj, getNodeXY6);
							jclass nodeXY32bClass = (*env)->GetObjectClass(env, nodeXY6Obj);

							jmethodID getNodeXY32bX = (*env)->GetMethodID(env, nodeXY32bClass, "getX", "()F");
							jfloat nodeXY32bX = (*env)->CallFloatMethod(env, nodeXY6Obj, getNodeXY32bX);

							jmethodID getNodeXY32bY = (*env)->GetMethodID(env, nodeXY32bClass, "getY", "()F");
							jfloat nodeXY32bY = (*env)->CallFloatMethod(env, nodeXY6Obj, getNodeXY32bY);

							nodeXy->delta.present = NodeOffsetPointXY_PR_node_XY6;
							nodeXy->delta.choice.node_XY6.x = (Offset_B16_t)((long)nodeXY32bX);
							nodeXy->delta.choice.node_XY6.y = (Offset_B16_t)((long)nodeXY32bY);
						}
						else if (nodeOffsetPointXYChoice == NODE_LAT_LON)
						{
							jmethodID getNodeLatLon = (*env)->GetMethodID(env, nodeOffsetPointXYClass, "getNodeLatLon", "()Lgov/usdot/cv/mapencoder/NodeLLmD64b;");
							jobject nodeLatLonObj = (*env)->CallObjectMethod(env, deltaObj, getNodeLatLon);
							jclass nodeLLMd64bClass = (*env)->GetObjectClass(env, nodeLatLonObj);

							jmethodID getNodeLLMd64bLat = (*env)->GetMethodID(env, nodeLLMd64bClass, "getLatitude", "()I");
							jint nodeLLMd64bLat = (*env)->CallIntMethod(env, nodeLatLonObj, getNodeLLMd64bLat);

							jmethodID getNodeLLMd64bLon = (*env)->GetMethodID(env, nodeLLMd64bClass, "getLongitude", "()I");
							jint nodeLLMd64bLon = (*env)->CallIntMethod(env, nodeLatLonObj, getNodeLLMd64bLon);

							nodeXy->delta.present = NodeOffsetPointXY_PR_node_LatLon;
							nodeXy->delta.choice.node_LatLon.lon = (Common_Longitude_t)((long)nodeLLMd64bLon);
							nodeXy->delta.choice.node_LatLon.lat = (Common_Latitude_t)((long)nodeLLMd64bLat);
						}
						else if (nodeOffsetPointXYChoice == NODE_REGIONAL)
						{
							printf("nodeOffsetPointXYChoice is NodeOffsetPointXY_PR_regional \n");
							nodeXy->delta.present = NodeOffsetPointXY_PR_regional;
						}
						else
						{
							printf("nodeOffsetPointXYChoice is NodeOffsetPointXY_PR_NOTHING \n");
							nodeXy->delta.present = NodeOffsetPointXY_PR_NOTHING;
						}

						jmethodID isAttributesExists = (*env)->GetMethodID(env, nodeXYClass, "isAttributesExists", "()Z");
						jboolean attributesExists = (*env)->CallBooleanMethod(env, nodeXYObj, isAttributesExists);
						if (attributesExists)
						{
							NodeAttributeSetXY_t *nodeAttributeSetXY;
							nodeAttributeSetXY = calloc(1, sizeof(NodeAttributeSetXY_t));

							// Get Attributes object and class
							jmethodID getAttributes = (*env)->GetMethodID(env, nodeXYClass, "getAttributes", "()Lgov/usdot/cv/mapencoder/NodeAttributeSetXY;");
							jobject attributesObj = (*env)->CallObjectMethod(env, nodeXYObj, getAttributes);
							jclass nodeAttributeSetXYClass = (*env)->GetObjectClass(env, attributesObj);

							// Check if data attribute exists
							jmethodID isDataExists = (*env)->GetMethodID(env, nodeAttributeSetXYClass, "isDataExists", "()Z");
							jboolean dataExists = (*env)->CallBooleanMethod(env, attributesObj, isDataExists);

							if (dataExists)
							{
								// Get LaneDataAttributeList Data and class
								jmethodID getNodeAttributeSetXYData = (*env)->GetMethodID(env, nodeAttributeSetXYClass, "getData", "()Lgov/usdot/cv/mapencoder/LaneDataAttributeList;");
								jobject nodeAttributeSetXYObj = (*env)->CallObjectMethod(env, attributesObj, getNodeAttributeSetXYData);
								jclass laneDataAttributeListClass = (*env)->GetObjectClass(env, nodeAttributeSetXYObj);

								// Get LaneDataAttributeList Array
								jmethodID getLaneAttributeList = (*env)->GetMethodID(env, laneDataAttributeListClass, "getLaneAttributeList", "()[Lgov/usdot/cv/mapencoder/LaneDataAttribute;");
								jobject laneDataAttributeArray = (*env)->CallObjectMethod(env, nodeAttributeSetXYObj, getLaneAttributeList);

								jsize laneDataAttributeCount = (*env)->GetArrayLength(env, laneDataAttributeArray);

								LaneDataAttributeList_t *laneDataAttributeList;
								laneDataAttributeList = calloc(1, sizeof(LaneDataAttributeList_t));

								for (int laneDataAttributeIndex = 0; laneDataAttributeIndex < laneDataAttributeCount; laneDataAttributeIndex++)
								{
									LaneDataAttribute_t *laneDataAttribute;
									laneDataAttribute = calloc(1, sizeof(LaneDataAttribute_t));

									// Get each LaneDataAttribute from LaneDataAttributeList
									jobject laneDataAttributeObj = (jobject)(*env)->GetObjectArrayElement(env, laneDataAttributeArray, laneDataAttributeIndex);
									jclass laneDataAttributeClass = (*env)->GetObjectClass(env, laneDataAttributeObj);

									// Get LaneDataAttribute Choice
									jmethodID getLaneDataAttributeChoice = (*env)->GetMethodID(env, laneDataAttributeClass, "getChoice", "()I");
									jint laneDataAttributeChoice = (*env)->CallIntMethod(env, laneDataAttributeObj, getLaneDataAttributeChoice);
									if (laneDataAttributeChoice == SPEED_LIMITS)
									{
										laneDataAttribute->present = LaneDataAttribute_PR_speedLimits;

										// Get Node SpeedLimitList
										jmethodID getNodeSpeedLimits = (*env)->GetMethodID(env, laneDataAttributeClass, "getSpeedLimits", "()Lgov/usdot/cv/mapencoder/SpeedLimitList;");
										jobject nodeSpeedLimitListObj = (*env)->CallObjectMethod(env, laneDataAttributeObj, getNodeSpeedLimits);
										jclass nodeSpeedLimitListClass = (*env)->GetObjectClass(env, nodeSpeedLimitListObj);

										jmethodID getNodeRegulatorySpeedLimitList = (*env)->GetMethodID(env, nodeSpeedLimitListClass, "getSpeedLimits", "()[Lgov/usdot/cv/mapencoder/RegulatorySpeedLimit;");
										jobject nodeRegulatorySpeedLimitListArray = (*env)->CallObjectMethod(env, nodeSpeedLimitListObj, getNodeRegulatorySpeedLimitList);

										jsize nodeSpeedLimitsCount = (*env)->GetArrayLength(env, nodeRegulatorySpeedLimitListArray);

										SpeedLimitList_t *nodeSpeedLimitList;
										nodeSpeedLimitList = calloc(1, sizeof(SpeedLimitList_t));

										for (int nodeSpeedIndex = 0; nodeSpeedIndex < nodeSpeedLimitsCount; nodeSpeedIndex++)
										{
											RegulatorySpeedLimit_t *nodeRegulatorySpeedLimit;
											nodeRegulatorySpeedLimit = calloc(1, sizeof(RegulatorySpeedLimit_t));

											// Get each RegulatorySpeedLimit from Speed Limit List
											jobject nodeRegulatorySpeedLimitObj = (jobject)(*env)->GetObjectArrayElement(env, nodeRegulatorySpeedLimitListArray, nodeSpeedIndex);
											jclass nodeRegulatorySpeedLimitClass = (*env)->GetObjectClass(env, nodeRegulatorySpeedLimitObj);

											// Get Speed from RegulatorySpeedLimit
											jmethodID getNodeSpeed = (*env)->GetMethodID(env, nodeRegulatorySpeedLimitClass, "getSpeed", "()D");
											jdouble speed = (*env)->CallDoubleMethod(env, nodeRegulatorySpeedLimitObj, getNodeSpeed);

											// Get SpeedType from each RegulatorySpeedLimit
											jmethodID getNodeSpeedType = (*env)->GetMethodID(env, nodeRegulatorySpeedLimitClass, "getType", "()Lgov/usdot/cv/mapencoder/SpeedLimitType;");
											jobject nodeSpeedLimitTypeObj = (*env)->CallObjectMethod(env, nodeRegulatorySpeedLimitObj, getNodeSpeedType);
											jclass nodeSpeedLimitTypeClass = (*env)->GetObjectClass(env, nodeSpeedLimitTypeObj);

											// Get SpeedLimitType from Lane Direction Object
											jmethodID getNodeSpeedLimitType = (*env)->GetMethodID(env, nodeSpeedLimitTypeClass, "getSpeedLimitType", "()B");
											jbyte speedLimitType = (*env)->CallByteMethod(env, nodeSpeedLimitTypeObj, getNodeSpeedLimitType);

											nodeRegulatorySpeedLimit->speed = (Velocity_t)((long)speed);
											nodeRegulatorySpeedLimit->type = (SpeedLimitType_t)((long)speedLimitType);

											ASN_SEQUENCE_ADD(&nodeSpeedLimitList->list, nodeRegulatorySpeedLimit);
										}
										laneDataAttribute->choice.speedLimits = *nodeSpeedLimitList;
									}
									ASN_SEQUENCE_ADD(&laneDataAttributeList->list, laneDataAttribute);
								}
								nodeAttributeSetXY->data = laneDataAttributeList;
							}

							// Check if dWidth exists
							jmethodID isDWidthExists = (*env)->GetMethodID(env, nodeAttributeSetXYClass, "isDWidthExists", "()Z");
							jboolean dWidthExists = (*env)->CallBooleanMethod(env, attributesObj, isDWidthExists);

							if (dWidthExists)
							{
								jmethodID getDWidth = (*env)->GetMethodID(env, nodeAttributeSetXYClass, "getDWidth", "()F");
								jfloat dWidth = (*env)->CallFloatMethod(env, attributesObj, getDWidth);

								Offset_B10_t *nodeDWidth = calloc(1, sizeof(Offset_B10_t));
								*nodeDWidth = (long)dWidth;
								nodeAttributeSetXY->dWidth = nodeDWidth;
							}

							// Check if dElevation exists
							jmethodID isDElevationExists = (*env)->GetMethodID(env, nodeAttributeSetXYClass, "isDElevationExists", "()Z");
							jboolean dElevationExists = (*env)->CallBooleanMethod(env, attributesObj, isDElevationExists);

							if (dElevationExists)
							{
								jmethodID getDElevation = (*env)->GetMethodID(env, nodeAttributeSetXYClass, "getDElevation", "()F");
								jfloat dElevation = (*env)->CallFloatMethod(env, attributesObj, getDElevation);

								Offset_B10_t *nodeDElevation = calloc(1, sizeof(Offset_B10_t));
								*nodeDElevation = (long)dElevation;
								nodeAttributeSetXY->dElevation = nodeDElevation;
							}

							nodeXy->attributes = nodeAttributeSetXY;
						}
						ASN_SEQUENCE_ADD(&nodeSetXyList->list, nodeXy);
					}
					genericLane->nodeList.choice.nodes = *nodeSetXyList;
				}
				else if (nodeListChoice == COMPUTED_LANE)
				{
					// NodeListXY_PR_computed
					genericLane->nodeList.present = NodeListXY_PR_computed;

					jmethodID getComputed = (*env)->GetMethodID(env, nodeListClass, "getComputed", "()Lgov/usdot/cv/mapencoder/ComputedLane;");
					jobject computedObj = (*env)->CallObjectMethod(env, nodeListObj, getComputed);
					jclass computedClass = (*env)->GetObjectClass(env, computedObj);

					jmethodID getReferenceLaneId = (*env)->GetMethodID(env, computedClass, "getReferenceLaneId", "()I");
					jint referenceLaneId = (*env)->CallByteMethod(env, computedObj, getReferenceLaneId);

					ComputedLane_t *computedLane;
					computedLane = calloc(1, sizeof(ComputedLane_t));

					LaneID_t compLaneID;
					compLaneID = (long)referenceLaneId;

					computedLane->referenceLaneId = compLaneID;

					jmethodID getOffsetXaxis = (*env)->GetMethodID(env, computedClass, "getOffsetXaxis", "()Lgov/usdot/cv/mapencoder/OffsetXaxis;");
					jobject offsetXaxisObj = (*env)->CallObjectMethod(env, computedObj, getOffsetXaxis);
					jclass offsetXaxisClass = (*env)->GetObjectClass(env, offsetXaxisObj);

					jmethodID getOffsetXaxisChoice = (*env)->GetMethodID(env, offsetXaxisClass, "getChoice", "()B");
					jbyte offsetXaxisChoice = (*env)->CallByteMethod(env, offsetXaxisObj, getOffsetXaxisChoice);

					jmethodID getOffsetYaxis = (*env)->GetMethodID(env, computedClass, "getOffsetYaxis", "()Lgov/usdot/cv/mapencoder/OffsetYaxis;");
					jobject offsetYaxisObj = (*env)->CallObjectMethod(env, computedObj, getOffsetYaxis);
					jclass offsetYaxisClass = (*env)->GetObjectClass(env, offsetYaxisObj);

					jmethodID getOffsetYaxisChoice = (*env)->GetMethodID(env, offsetYaxisClass, "getChoice", "()B");
					jbyte offsetYaxisChoice = (*env)->CallByteMethod(env, offsetYaxisObj, getOffsetYaxisChoice);

					if (offsetXaxisChoice == 0)
					{
						computedLane->offsetXaxis.present = ComputedLane__offsetXaxis_PR_small;
						jmethodID getOffsetXaxisSmall = (*env)->GetMethodID(env, offsetXaxisClass, "getSmall", "()S");
						jshort offsetXaxisSmall = (*env)->CallShortMethod(env, offsetXaxisObj, getOffsetXaxisSmall);
						computedLane->offsetXaxis.choice.small = (DrivenLineOffsetSm_t)((long)offsetXaxisSmall);
					}
					else
					{
						computedLane->offsetXaxis.present = ComputedLane__offsetXaxis_PR_large;
						jmethodID getOffsetXaxisLarge = (*env)->GetMethodID(env, offsetXaxisClass, "getLarge", "()S");
						jshort offsetXaxisLarge = (*env)->CallShortMethod(env, offsetXaxisObj, getOffsetXaxisLarge);
						computedLane->offsetXaxis.choice.large = (DrivenLineOffsetLg_t)((long)offsetXaxisLarge);
					}

					if (offsetYaxisChoice == 0)
					{
						computedLane->offsetYaxis.present = ComputedLane__offsetYaxis_PR_small;
						jmethodID getOffsetYaxisSmall = (*env)->GetMethodID(env, offsetYaxisClass, "getSmall", "()S");
						jshort offsetYaxisSmall = (*env)->CallShortMethod(env, offsetYaxisObj, getOffsetYaxisSmall);
						computedLane->offsetYaxis.choice.small = (DrivenLineOffsetSm_t)((long)offsetYaxisSmall);
					}
					else
					{
						computedLane->offsetYaxis.present = ComputedLane__offsetYaxis_PR_large;
						jmethodID getOffsetYaxisLarge = (*env)->GetMethodID(env, offsetYaxisClass, "getLarge", "()S");
						jshort offsetYaxisLarge = (*env)->CallShortMethod(env, offsetYaxisObj, getOffsetYaxisLarge);
						computedLane->offsetYaxis.choice.large = (DrivenLineOffsetLg_t)((long)offsetYaxisLarge);
					}
					genericLane->nodeList.choice.computed = *computedLane;
				}
				else
				{
					// NodeListXY_PR_NOTHING
				}

				// Check if Connections exist
				jmethodID isConnectsToExists = (*env)->GetMethodID(env, genericLaneClass, "isConnectsToExists", "()Z");
				jboolean connectsToExists = (*env)->CallBooleanMethod(env, laneObj, isConnectsToExists);

				if (connectsToExists)
				{
					// Get sequence of Connections from Generic Lane
					jmethodID getConnections = (*env)->GetMethodID(env, genericLaneClass, "getConnections", "()[Lgov/usdot/cv/mapencoder/Connection;");
					jobject connectionsArray = (*env)->CallObjectMethod(env, laneObj, getConnections);

					jsize connectionsCount = (*env)->GetArrayLength(env, connectionsArray);

					ConnectsToList_t *connectsToList;
					connectsToList = calloc(1, sizeof(ConnectsToList_t));

					for (int connectionsIndex = 0; connectionsIndex < connectionsCount; connectionsIndex++)
					{
						Connection_t *connection;
						connection = calloc(1, sizeof(Connection_t));

						// Get each Connection from ConnectionsArray
						jobject connectionObj = (jobject)(*env)->GetObjectArrayElement(env, connectionsArray, connectionsIndex);
						jclass connectionClass = (*env)->GetObjectClass(env, connectionObj);

						// Get ConnectingLane from each Connection
						jmethodID getConnectingLane = (*env)->GetMethodID(env, connectionClass, "getConnectingLane", "()Lgov/usdot/cv/mapencoder/ConnectingLane;");
						jobject connectingLaneObj = (*env)->CallObjectMethod(env, connectionObj, getConnectingLane);
						jclass connectingLaneClass = (*env)->GetObjectClass(env, connectingLaneObj);

						// Get LaneID from ConnectingLane
						jmethodID getConnectingLaneID = (*env)->GetMethodID(env, connectingLaneClass, "getLaneId", "()I");
						jint connectingLaneId = (*env)->CallIntMethod(env, connectingLaneObj, getConnectingLaneID);

						LaneID_t connectionLaneID;
						connectionLaneID = (long)connectingLaneId;

						ConnectingLane_t connectingLane;
						connectingLane.lane = connectionLaneID;

						// Check if Connecting Lane Maneuver exists
						jmethodID isConnectingLaneManeuverExists = (*env)->GetMethodID(env, connectingLaneClass, "getManeuverExists", "()Z");
						jboolean connectingLaneManeuverExists = (*env)->CallBooleanMethod(env, connectingLaneObj, isConnectingLaneManeuverExists);

						if (connectingLaneManeuverExists)
						{
							// Get Maneuver from Connecting Lane
							jmethodID getConnectingManeuver = (*env)->GetMethodID(env, connectingLaneClass, "getManeuver", "()Lgov/usdot/cv/mapencoder/AllowedManeuvers;");
							jobject connectingLaneManeuverObj = (*env)->CallObjectMethod(env, connectingLaneObj, getConnectingManeuver);
							jclass connectingLaneManeuverClass = (*env)->GetObjectClass(env, connectingLaneManeuverObj);

							// Get Allowed Maneuvers from each Maneuvers
							jmethodID getConnectingLaneAllowedManeuvers = (*env)->GetMethodID(env, connectingLaneManeuverClass, "getAllowedManeuvers", "()I");
							jint connectingLaneAllowedManeuversData = (*env)->CallIntMethod(env, connectingLaneManeuverObj, getConnectingLaneAllowedManeuvers);

							uint16_t connectingLaneAllowedManeuversContent = connectingLaneAllowedManeuversData;
							BIT_STRING_t connectingLaneAllowedManeuversBitString;

							// Converting the allowedManeuversContent to BIT_STRING
							connectingLaneAllowedManeuversBitString.buf = (uint8_t *)calloc(2, sizeof(uint8_t));
							connectingLaneAllowedManeuversBitString.buf[1] = connectingLaneAllowedManeuversContent;
							connectingLaneAllowedManeuversBitString.buf[0] = connectingLaneAllowedManeuversContent >> 8;
							connectingLaneAllowedManeuversBitString.size = (size_t)2;
							connectingLaneAllowedManeuversBitString.bits_unused = 4;

							AllowedManeuvers_t *connectingLaneAllowedManeuvers = calloc(1, sizeof(AllowedManeuvers_t));
							*connectingLaneAllowedManeuvers = connectingLaneAllowedManeuversBitString;
							connectingLane.maneuver = connectingLaneAllowedManeuvers;
						} else {
							connectingLane.maneuver = NULL;
						}

						// Set Connecting Lane
						connection->connectingLane = connectingLane;

						// Check if remoteIntersection  exists
						jmethodID getRemoteIntersectionExists = (*env)->GetMethodID(env, connectionClass, "getRemoteIntersectionExists", "()Z");
						jboolean remoteIntersectionExists = (*env)->CallBooleanMethod(env, connectionObj, getRemoteIntersectionExists);

						if (remoteIntersectionExists)
						{
							// Get Remote Intersection from Connection
							IntersectionReferenceID_t *remoteIntersectionReferenceID;
							remoteIntersectionReferenceID = calloc(1, sizeof(IntersectionReferenceID_t));

							// Get Remote Intersection Id
							jmethodID getRemoteIntersection = (*env)->GetMethodID(env, connectionClass, "getRemoteIntersection", "()Lgov/usdot/cv/mapencoder/IntersectionReferenceID;");
							jobject remoteIntersectionObj = (*env)->CallObjectMethod(env, connectionObj, getRemoteIntersection);
							jclass remoteIntersectionClass = (*env)->GetObjectClass(env, remoteIntersectionObj);

							jmethodID getRemoteIntersectionId = (*env)->GetMethodID(env, remoteIntersectionClass, "getId", "()I");
							jint remoteIntersectionIdData = (*env)->CallIntMethod(env, remoteIntersectionObj, getRemoteIntersectionId);

							IntersectionID_t remoteIntersectionId;
							remoteIntersectionId = (long)remoteIntersectionIdData;
							remoteIntersectionReferenceID->id = remoteIntersectionId;

							// Check if Remote Intersection region exists
							jmethodID isRemoteIntersectionRegionExists = (*env)->GetMethodID(env, remoteIntersectionClass, "isRegionExists", "()Z");
							jboolean remoteIntersectionRegionExists = (*env)->CallBooleanMethod(env, remoteIntersectionObj, isRemoteIntersectionRegionExists);

							if (remoteIntersectionRegionExists)
							{
								jmethodID getRemoteIntersectionRegion = (*env)->GetMethodID(env, remoteIntersectionClass, "getRegion", "()I");
								jint remoteIntersectionRegionId = (*env)->CallIntMethod(env, remoteIntersectionObj, getRemoteIntersectionRegion);

								RoadRegulatorID_t *remoteIntersectionRoadRegulatorId = calloc(1, sizeof(RoadRegulatorID_t));
								*remoteIntersectionRoadRegulatorId = (long)remoteIntersectionRegionId;
								remoteIntersectionReferenceID->region = remoteIntersectionRoadRegulatorId;
							}
							// Set Remote Intersection
							connection->remoteIntersection = remoteIntersectionReferenceID;
						}

						// Check if Signal Group Exists
						jmethodID getSignalGroupExists = (*env)->GetMethodID(env, connectionClass, "getSignalGroupExists", "()Z");
						jboolean signalGroupExists = (*env)->CallBooleanMethod(env, connectionObj, getSignalGroupExists);

						if (signalGroupExists)
						{
							// Get SignalGroup from Connection
							jmethodID getSignalGroup = (*env)->GetMethodID(env, connectionClass, "getSignalGroup", "()J");
							jlong signalGroup = (*env)->CallLongMethod(env, connectionObj, getSignalGroup);

							SignalGroupID_t *signalGroupID = calloc(1, sizeof(SignalGroupID_t));
							*signalGroupID = signalGroup;
							connection->signalGroup = signalGroupID;
						}

						// Check if userClass Exists
						jmethodID getUserClassExists = (*env)->GetMethodID(env, connectionClass, "getUserClassExists", "()Z");
						jboolean userClassExists = (*env)->CallBooleanMethod(env, connectionObj, getUserClassExists);

						if (userClassExists)
						{
							// Get userClass from Connection
							jmethodID getUserClass = (*env)->GetMethodID(env, connectionClass, "getUserClass", "()J");
							jlong userClass = (*env)->CallLongMethod(env, connectionObj, getUserClass);

							RestrictionClassID_t *restrictionClassID = calloc(1, sizeof(RestrictionClassID_t));
							*restrictionClassID = userClass;
							connection->userClass = restrictionClassID;
						}

						// Check if connectionID Exists
						jmethodID getConnectionIDExists = (*env)->GetMethodID(env, connectionClass, "getConnectionIDExists", "()Z");
						jboolean connectionIDExists = (*env)->CallBooleanMethod(env, connectionObj, getConnectionIDExists);

						if (connectionIDExists)
						{
							// Get connectionID from Connection
							jmethodID getConnectionID = (*env)->GetMethodID(env, connectionClass, "getConnectionID", "()J");
							jlong connectionID = (*env)->CallLongMethod(env, connectionObj, getConnectionID);

							LaneConnectionID_t *laneConnectionID = calloc(1, sizeof(LaneConnectionID_t));
							*laneConnectionID = connectionID;
							connection->connectionID = laneConnectionID;
						}

						ASN_SEQUENCE_ADD(&connectsToList->list, connection);
					}
					genericLane->connectsTo = connectsToList;
				}
				// Assigning laneAttributes to genericLane
				genericLane->laneAttributes = laneAttributes;

				ASN_SEQUENCE_ADD(&laneList->list, genericLane);
			}

			intersection->laneSet.list = laneList->list;

			ASN_SEQUENCE_ADD(&intersectionsList->list, intersection);
		}
		message->value.choice.MapData.intersections = intersectionsList;
	}
	else
	{
		message->value.choice.MapData.intersections = NULL;
	}

	ec = uper_encode_to_buffer(&asn_DEF_MessageFrame, 0, message, buffer, buffer_size);
	if (ec.encoded == -1)
	{
		// printf("Cause of failure %s \n", ec.failed_type->name)
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

	// clear memory
	asn_sequence_empty(intersectionsList);
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
		// printf("%d", elements[i]);
	}
	(*env)->ReleaseByteArrayElements(env, outJNIArray, elements, JNI_ABORT);

	return outJNIArray;
}