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
#include "gov_usdot_cv_rgaencoder_Encoder.h"
#include "MessageFrame.h"
#include <stdint.h>

JNIEXPORT jbyteArray JNICALL Java_gov_usdot_cv_rgaencoder_Encoder_encodeRGA(JNIEnv *env, jobject cls, jobject baseLayer)
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

	// Set BaseLayer, RGADataSet, and MessageFrame
	RGADataSet_t rgaDataSet;
	rgaDataSet.baseLayer = rgaBaseLayer;
	rgaDataSet.geometryContainer = NULL;
	rgaDataSet.movementsContainer = NULL;
	rgaDataSet.wayUseContainer = NULL;
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
		printf("Element %d: %d\n", i, elements[i]);
	}
	(*env)->ReleaseByteArrayElements(env, outJNIArray, elements, JNI_ABORT);

	return outJNIArray;
}