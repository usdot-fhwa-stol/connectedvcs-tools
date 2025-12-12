/*
 * Copyright (C) 2025 LEIDOS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include <stdio.h>
#include <stdlib.h> // calloc, free
#include <string.h> // memset
#include <sys/types.h>
#include <stdint.h>
#include <jni.h> // JNIEXPORT, JNIEnv
#include "gov_usdot_cv_timencoder_Encoder.h"
#include "MessageFrame.h"

static int fill_octet_from_java_string_like(JNIEnv *env, OCTET_STRING_t *dst, jobject obj, int maxLen)
{
    if (!obj)
        return 0;
    jclass cls = (*env)->GetObjectClass(env, obj);

    jmethodID midGetVal = (*env)->GetMethodID(env, cls, "getValue", "()Ljava/lang/String;");
    jstring s = midGetVal ? (jstring)(*env)->CallObjectMethod(env, obj, midGetVal) : NULL;

    if (!s)
    {
        jmethodID midToStr = (*env)->GetMethodID(env, cls, "toString", "()Ljava/lang/String;");
        s = midToStr ? (jstring)(*env)->CallObjectMethod(env, obj, midToStr) : NULL;
    }

    int ok = 0;
    if (s)
    {
        const char *u = (*env)->GetStringUTFChars(env, s, 0);
        if (u)
        {
            int useLen = ia5_truncate_len(u, maxLen);
            OCTET_STRING_fromBuf(dst, u, useLen);
            (*env)->ReleaseStringUTFChars(env, s, u);
            ok = 1;
        }
        (*env)->DeleteLocalRef(env, s);
    }
    (*env)->DeleteLocalRef(env, cls);
    return ok;
}
static long get_long_from_java_number_like(JNIEnv *env, jobject obj)
{
    if (!obj)
        return 0;
    jclass cls = (*env)->GetObjectClass(env, obj);

    jmethodID midGetVal = (*env)->GetMethodID(env, cls, "intValue", "()I");
    if (midGetVal)
    {
        jint v = (*env)->CallIntMethod(env, obj, midGetVal);
        (*env)->DeleteLocalRef(env, cls);
        return (long)v;
    }

    jmethodID midIntVal = (*env)->GetMethodID(env, cls, "intValue", "()I");
    if (midIntVal)
    {
        jint v = (*env)->CallIntMethod(env, obj, midIntVal);
        (*env)->DeleteLocalRef(env, cls);
        return (long)v;
    }

    jmethodID midToStr = (*env)->GetMethodID(env, cls, "toString", "()Ljava/lang/String;");
    jstring s = midToStr ? (jstring)(*env)->CallObjectMethod(env, obj, midToStr) : NULL;
    long out = 0;
    if (s)
    {
        const char *u = (*env)->GetStringUTFChars(env, s, 0);
        if (u)
            out = strtol(u, NULL, 10);
        if (u)
            (*env)->ReleaseStringUTFChars(env, s, u);
        (*env)->DeleteLocalRef(env, s);
    }
    (*env)->DeleteLocalRef(env, cls);
    return out;
}
static long parse_itis_code_string(const char *s)
{
    if (!s || !*s)
        return 0;
    int neg = 0;
    if (s[0] == 'n' || s[0] == 'N' || s[0] == '-')
    {
        neg = 1;
        s++;
    }
    char *endp = NULL;
    long v = strtol(s, &endp, 10);
    if (neg)
        v = -v;
    return v;
}
int ia5_truncate_len(const char *s, int maxlen)
{
    if (!s)
        return 0;
    int len = (int)strlen(s);
    return (len > maxlen) ? maxlen : len;
}

JNIEXPORT jbyteArray JNICALL Java_gov_usdot_cv_timencoder_Encoder_encodeTIM(
    JNIEnv *env, jobject cls, jobject timobject)
{
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

    // Initilize empty TIM
    TravelerInformation_t tim;
    memset(&tim, 0, sizeof(tim));

    if (timobject == NULL)
    {
        fprintf(stderr, "timobject is NULL\n");

        return NULL;
    }
    jclass timClass = (*env)->GetObjectClass(env, timobject);
    if (!timClass)
    {
        fprintf(stderr, "Failed to GetObjectClass for TravelerInformation\n");

        return NULL;
    }

    jmethodID midGetMsgCnt = (*env)->GetMethodID(env, timClass, "getMsgCnt", "()I");
    if (!midGetMsgCnt)
    {
        fprintf(stderr, "Failed to find TravelerInformation.getMsgCnt()\n");
        (*env)->DeleteLocalRef(env, timClass);

        return NULL;
    }

    jint j_msgCnt = (*env)->CallIntMethod(env, timobject, midGetMsgCnt);
    if ((*env)->ExceptionCheck(env))
    {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
        (*env)->DeleteLocalRef(env, timClass);

        return NULL;
    }
    tim.msgCnt = (long)j_msgCnt;

    // --- packetID ---
    jmethodID getPacketID = (*env)->GetMethodID(env, timClass, "getPacketID", "()Lgov/usdot/cv/timencoder/UniqueMSGID;");
    jobject packetIDObj = (*env)->CallObjectMethod(env, timobject, getPacketID);

    if (packetIDObj != NULL)
    {
        jclass uniqueMsgIDClass = (*env)->GetObjectClass(env, packetIDObj);
        jmethodID getValue = (*env)->GetMethodID(env, uniqueMsgIDClass, "getValue", "()[B");
        jbyteArray jByteArrayValue = (jbyteArray)(*env)->CallObjectMethod(env, packetIDObj, getValue);
    
        if (jByteArrayValue != NULL)
        {
            jsize length = (*env)->GetArrayLength(env, jByteArrayValue);
            jbyte *bytes = (*env)->GetByteArrayElements(env, jByteArrayValue, NULL);
    
            // Allocate ASN.1 structure
            UniqueMSGID_t *packetID = (UniqueMSGID_t *)calloc(1, sizeof(UniqueMSGID_t));
            packetID->size = length;
            packetID->buf = (uint8_t *)calloc(1, length);
            memcpy(packetID->buf, bytes, length);
    
            tim.packetID = packetID;
    
            (*env)->ReleaseByteArrayElements(env, jByteArrayValue, bytes, JNI_ABORT);
            (*env)->DeleteLocalRef(env, jByteArrayValue);
        }
    }

    // ---- TravelerInformation.dataFrames (TravelerDataFrameList) ----
    jmethodID midGetDataFrames = (*env)->GetMethodID(
        env, timClass, "getDataFrames", "()Lgov/usdot/cv/timencoder/TravelerDataFrameList;");
    if (!midGetDataFrames)
    {
        fprintf(stderr, "Failed to find TravelerInformation.getDataFrames()\n");
        (*env)->DeleteLocalRef(env, timClass);

        return NULL;
    }

    jobject tdfListObj = (*env)->CallObjectMethod(env, timobject, midGetDataFrames);
    if ((*env)->ExceptionCheck(env))
    {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
        (*env)->DeleteLocalRef(env, tdfListObj);

        return NULL;
    }
    if (!tdfListObj)
    {
        printf("TravelerDataFrameList is null (0 frames)\n");
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
        (*env)->DeleteLocalRef(env, timClass);
        return NULL;
    }

    jclass tdfCls = (*env)->GetObjectClass(env, tdfListObj);
    if (!tdfCls)
    {
        fprintf(stderr, "Failed to GetObjectClass for TravelerDataFrameList\n");
        (*env)->DeleteLocalRef(env, tdfListObj);
        (*env)->DeleteLocalRef(env, timClass);
        return NULL;
    }

    jmethodID midGetFrames = (*env)->GetMethodID(env, tdfCls, "getFrames", "()Ljava/util/List;");
    if (!midGetFrames)
    {
        fprintf(stderr, "Failed to find TravelerDataFrameList.getFrames()\n");
        (*env)->DeleteLocalRef(env, tdfCls);
        (*env)->DeleteLocalRef(env, tdfListObj);
        (*env)->DeleteLocalRef(env, timClass);
        return NULL;
    }

    jobject listObj = (*env)->CallObjectMethod(env, tdfListObj, midGetFrames);
    if ((*env)->ExceptionCheck(env) || !listObj)
    {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
        fprintf(stderr, "Failed to get frames list\n");
        (*env)->DeleteLocalRef(env, tdfCls);
        (*env)->DeleteLocalRef(env, tdfListObj);
        (*env)->DeleteLocalRef(env, timClass);
        return NULL;
    }

    jclass listCls = (*env)->GetObjectClass(env, listObj);
    jmethodID midSize = (*env)->GetMethodID(env, listCls, "size", "()I");
    jmethodID midGet = (*env)->GetMethodID(env, listCls, "get", "(I)Ljava/lang/Object;");
    if (!midSize || !midGet)
    {
        fprintf(stderr, "Failed to get List methods\n");
        (*env)->DeleteLocalRef(env, listCls);
        (*env)->DeleteLocalRef(env, listObj);
        (*env)->DeleteLocalRef(env, tdfCls);
        (*env)->DeleteLocalRef(env, tdfListObj);
        (*env)->DeleteLocalRef(env, timClass);
        return NULL;
    }

    jint numberOfFrames = (*env)->CallIntMethod(env, listObj, midSize);
    if ((*env)->ExceptionCheck(env))
    {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
    }

    jclass frameCls = NULL;
    jmethodID mid_getDoNotUse1 = NULL, mid_getDoNotUse2 = NULL, mid_getDoNotUse3 = NULL, mid_getDoNotUse4 = NULL;
    jmethodID mid_getFrameType = NULL, mid_getMsgId = NULL, mid_getStartTime = NULL, mid_getDurationTime = NULL;
    jmethodID mid_getPriority = NULL, mid_getRegions = NULL, mid_getContent = NULL, mid_getContentNew = NULL;
    // If frame exist initilize the methods to get the method IDs
    if (numberOfFrames > 0)
    {
        jobject first = (*env)->CallObjectMethod(env, listObj, midGet, 0);
        if ((*env)->ExceptionCheck(env) || !first)
        {
            (*env)->ExceptionDescribe(env);
            (*env)->ExceptionClear(env);
        }
        else
        {
            frameCls = (*env)->GetObjectClass(env, first);

            if ((*env)->ExceptionCheck(env) ||
                !mid_getDoNotUse1 || !mid_getDoNotUse2 || !mid_getDoNotUse3 || !mid_getDoNotUse4 ||
                !mid_getFrameType || !mid_getMsgId || !mid_getStartTime || !mid_getDurationTime ||
                !mid_getPriority || !mid_getRegions || !mid_getContent || !mid_getContentNew)
            {
                (*env)->ExceptionDescribe(env);
                (*env)->ExceptionClear(env);
            }
            (*env)->DeleteLocalRef(env, first);
        }
    }

    // ---- Iterate frames to get the value of fields from Traveler Data Frame ----
    for (jint i = 0; i < numberOfFrames; i++)
    {
        jobject frame = (*env)->CallObjectMethod(env, listObj, midGet, i);
        TravelerDataFrame_t *tdf = (TravelerDataFrame_t *)calloc(1, sizeof(*tdf));

        if ((*env)->ExceptionCheck(env) || !frame)
        {
            (*env)->ExceptionDescribe(env);
            (*env)->ExceptionClear(env);
            continue;
        }

        if ((*env)->ExceptionCheck(env))
        {
            (*env)->ExceptionDescribe(env);
            (*env)->ExceptionClear(env);
        }


        // doNotUse1
        mid_getDoNotUse1 = (*env)->GetMethodID(env, frameCls, "getDoNotUse1", "()Lgov/usdot/cv/timencoder/SSPindex;");
        jobject doNotUse1 = mid_getDoNotUse1 ? (*env)->CallObjectMethod(env, frame, mid_getDoNotUse1) : NULL;
        if (doNotUse1)
        {
            jclass doNotUse1Cls = (*env)->GetObjectClass(env, doNotUse1);
            jmethodID midGetIndex = (*env)->GetMethodID(env, doNotUse1Cls, "get", "()I");
            jint index = (*env)->CallIntMethod(env, doNotUse1, midGetIndex);
            tdf->doNotUse1 = (long)index;
            (*env)->DeleteLocalRef(env, doNotUse1Cls);
        }

        // frameType (TravelerInfoType)
        mid_getFrameType = (*env)->GetMethodID(env, frameCls, "getFrameType", "()Lgov/usdot/cv/timencoder/TravelerInfoType;");
        jobject frameType = mid_getFrameType ? (*env)->CallObjectMethod(env, frame, mid_getFrameType) : NULL;
        if (frameType)
        {
            jclass frameTypeCls = (*env)->GetObjectClass(env, frameType);
            jmethodID midGetVal = (*env)->GetMethodID(env, frameTypeCls, "getValue", "()I");
            jint val = (*env)->CallIntMethod(env, frameType, midGetVal);
            tdf->frameType = (TravelerInfoType_t)val;
            (*env)->DeleteLocalRef(env, frameTypeCls);
        }

        // msgId (MsgId -> RoadSignID)
        mid_getMsgId = (*env)->GetMethodID(env, frameCls, "getMsgId", "()Lgov/usdot/cv/timencoder/MsgId;");
        jobject msgId = mid_getMsgId ? (*env)->CallObjectMethod(env, frame, mid_getMsgId) : NULL;
        if (msgId)
        {

            jclass msgIdCls = (*env)->GetObjectClass(env, msgId);
            jmethodID midGetRoadSign = (*env)->GetMethodID(
                env, msgIdCls, "getRoadSignID",
                "()Lgov/usdot/cv/timencoder/RoadSignID;");
            jobject rsObj = midGetRoadSign ? (*env)->CallObjectMethod(env, msgId, midGetRoadSign) : NULL;

            if (rsObj)
            {
                tdf->msgId.present = TravelerDataFrame__msgId_PR_roadSignID;
                RoadSignID_t *rs = &tdf->msgId.choice.roadSignID;

                jclass rsCls = (*env)->GetObjectClass(env, rsObj);

                // --- position (Position3D) ---
                jmethodID midGetPos = (*env)->GetMethodID(
                    env, rsCls, "getPosition", "()Lgov/usdot/cv/mapencoder/Position3D;");
                jobject posObj = midGetPos ? (*env)->CallObjectMethod(env, rsObj, midGetPos) : NULL;

                if (posObj)
                {
                    jclass posCls = (*env)->GetObjectClass(env, posObj);
                    jmethodID midLat = (*env)->GetMethodID(env, posCls, "getLatitude", "()D");
                    jmethodID midLon = (*env)->GetMethodID(env, posCls, "getLongitude", "()D");
                    jmethodID midEle = (*env)->GetMethodID(env, posCls, "getElevation", "()F");
                    jmethodID midEleExists = (*env)->GetMethodID(env, posCls, "isElevationExists", "()Z");

                    double lat = midLat ? (*env)->CallDoubleMethod(env, posObj, midLat) : 0.0;
                    double lon = midLon ? (*env)->CallDoubleMethod(env, posObj, midLon) : 0.0;
                    float ele = midEle ? (*env)->CallFloatMethod(env, posObj, midEle) : 0.0f;
                    jboolean haveEle = midEleExists ? (*env)->CallBooleanMethod(env, posObj, midEleExists) : JNI_FALSE;

                    rs->position.lat = (Common_Latitude_t)((long)lat);
                    rs->position.Long = (Common_Latitude_t)((long)lon);

                    // Optional elevation: decimeters (0.1 m)
                    if (haveEle)
                    {
                        rs->position.elevation = (Common_Elevation_t *)calloc(1, sizeof(*rs->position.elevation));
                        if (rs->position.elevation)
                        {
                            *rs->position.elevation = ele;
                        }
                    }

                    (*env)->DeleteLocalRef(env, posCls);
                    (*env)->DeleteLocalRef(env, posObj);
                }

                // --- viewAngle (HeadingSlice -> BIT STRING(16)) ---
                jmethodID midGetView = (*env)->GetMethodID(
                    env, rsCls, "getViewAngle", "()Lgov/usdot/cv/timencoder/HeadingSlice;");
                jobject hsObj = midGetView ? (*env)->CallObjectMethod(env, rsObj, midGetView) : NULL;

                if (hsObj)
                {
                    jclass hsCls = (*env)->GetObjectClass(env, hsObj);
                    jmethodID midIntVal = (*env)->GetMethodID(env, hsCls, "intValue", "()I");

                    jint mask = 0;
                    if (midIntVal)
                    {
                        mask = (*env)->CallIntMethod(env, hsObj, midIntVal);
                        if ((*env)->ExceptionCheck(env))
                        {
                            (*env)->ExceptionDescribe(env);
                            (*env)->ExceptionClear(env);
                            mask = 0;
                        }
                    }

                    // Constrain to 16 bits
                    mask &= 0xFFFF;
                    rs->viewAngle.buf = (uint8_t *)calloc(1, 2);
                    if (rs->viewAngle.buf)
                    {

                        rs->viewAngle.buf[0] = (uint8_t)((mask >> 8) & 0xFF);
                        rs->viewAngle.buf[1] = (uint8_t)(mask & 0xFF);
                        rs->viewAngle.bits_unused = 0;
                        rs->viewAngle.size = 2;
                    }

                    (*env)->DeleteLocalRef(env, hsCls);
                    (*env)->DeleteLocalRef(env, hsObj);
                }
                else
                {
                    tdf->msgId.present = TravelerDataFrame__msgId_PR_NOTHING;
                }

                  // --- mutcdCode ---
                jmethodID midGetMutcd = (*env)->GetMethodID(
                    env, rsCls, "getMutcdCode", "()Lgov/usdot/cv/timencoder/MUTCDCode;");
                jobject mutcdObj = midGetMutcd ? (*env)->CallObjectMethod(env, rsObj, midGetMutcd) : NULL;

                if (mutcdObj)
                {
                    jclass mutcdCls = (*env)->GetObjectClass(env, mutcdObj);
                    jmethodID midGetValue = (*env)->GetMethodID(env, mutcdCls, "getValue", "()I");

                    if (midGetValue)
                    {
                        jint codeValue = (*env)->CallIntMethod(env, mutcdObj, midGetValue);
                        if ((*env)->ExceptionCheck(env))
                        {
                            (*env)->ExceptionDescribe(env);
                            (*env)->ExceptionClear(env);
                            codeValue = 0;
                        }

                        rs->mutcdCode = (MUTCDCode_t *)calloc(1, sizeof(MUTCDCode_t));
                        if (rs->mutcdCode)
                        {
                            *rs->mutcdCode = (MUTCDCode_t)codeValue;
                        }
                    }

                    (*env)->DeleteLocalRef(env, mutcdCls);
                    (*env)->DeleteLocalRef(env, mutcdObj);
                }

                (*env)->DeleteLocalRef(env, msgIdCls);
            }
            else
            {
                tdf->msgId.present = TravelerDataFrame__msgId_PR_NOTHING;
            }
        }

        // --- startYear (DYear) ---
        jmethodID mid_getStartYear = (*env)->GetMethodID(env, frameCls, "getStartYear", "()Lgov/usdot/cv/timencoder/DYear;");
        jobject startYearObj = mid_getStartYear ? (*env)->CallObjectMethod(env, frame, mid_getStartYear) : NULL;
        if (startYearObj)
        {
            jclass startYearCls = (*env)->GetObjectClass(env, startYearObj);
            jmethodID midGetYearVal = (*env)->GetMethodID(env, startYearCls, "getValue", "()I");
            jint yearVal = midGetYearVal ? (*env)->CallIntMethod(env, startYearObj, midGetYearVal) : 0;

            // Allocate and set DYear_t (long *)
            DYear_t *yearPtr = (DYear_t *)calloc(1, sizeof(DYear_t));
            *yearPtr = (long)yearVal;
            tdf->startYear = yearPtr;

            (*env)->DeleteLocalRef(env, startYearCls);
            (*env)->DeleteLocalRef(env, startYearObj);
        }
        else
        {
            tdf->startYear = NULL; // optional field
        }

        // startTime (MinuteOfTheYear)
        mid_getStartTime = (*env)->GetMethodID(env, frameCls, "getStartTime", "()Lgov/usdot/cv/timencoder/MinuteOfTheYear;");
        jobject startTime = mid_getStartTime ? (*env)->CallObjectMethod(env, frame, mid_getStartTime) : NULL;
        if (startTime)
        {

            jclass startTimeCls = (*env)->GetObjectClass(env, startTime);
            jmethodID midGetMinute = (*env)->GetMethodID(env, startTimeCls, "getValue", "()J");
            jint start = (*env)->CallIntMethod(env, startTime, midGetMinute);
            tdf->startTime = (long)start;
            (*env)->DeleteLocalRef(env, startTimeCls);
        }

        mid_getDurationTime = (*env)->GetMethodID(env, frameCls, "getDurationTime", "()Lgov/usdot/cv/timencoder/MinutesDuration;");
        jobject durationTime = mid_getDurationTime ? (*env)->CallObjectMethod(env, frame, mid_getDurationTime) : NULL;
        if (durationTime)
        {
            jclass durCls = (*env)->GetObjectClass(env, durationTime);
            jmethodID midGetDur = (*env)->GetMethodID(env, durCls, "getValue", "()I");
            jint dur = midGetDur ? (*env)->CallIntMethod(env, durationTime, midGetDur) : 0;
            tdf->durationTime = (long)dur;
            (*env)->DeleteLocalRef(env, durCls);
        }

        // priority (SignPriority)
        mid_getPriority = (*env)->GetMethodID(env, frameCls, "getPriority", "()Lgov/usdot/cv/timencoder/SignPriority;");
        jobject priority = mid_getPriority ? (*env)->CallObjectMethod(env, frame, mid_getPriority) : NULL;
        if (priority)
        {

            jclass priorityCls = (*env)->GetObjectClass(env, priority);
            jmethodID midGetPri = (*env)->GetMethodID(env, priorityCls, "getValue", "()I");
            jint pri = (*env)->CallIntMethod(env, priority, midGetPri);
            tdf->priority = (long)pri;
            (*env)->DeleteLocalRef(env, priorityCls);
        }

        // doNotUse2
        mid_getDoNotUse2 = (*env)->GetMethodID(env, frameCls, "getDoNotUse2", "()Lgov/usdot/cv/timencoder/SSPindex;");
        jobject doNotUse2 = mid_getDoNotUse2 ? (*env)->CallObjectMethod(env, frame, mid_getDoNotUse2) : NULL;
        if (doNotUse2)
        {

            jclass doNotUse2Cls = (*env)->GetObjectClass(env, doNotUse2);
            jmethodID midGetIndex2 = (*env)->GetMethodID(env, doNotUse2Cls, "get", "()I");
            jint index2 = (*env)->CallIntMethod(env, doNotUse2, midGetIndex2);
            tdf->doNotUse2 = (long)index2;
            (*env)->DeleteLocalRef(env, doNotUse2Cls);
        }

        // --- Get regions list from TravelerDataFrame ---
        mid_getRegions = (*env)->GetMethodID(env, frameCls, "getRegions", "()Ljava/util/List;");
        jobject regionsListObj = (*env)->CallObjectMethod(env, frame, mid_getRegions);

        if (regionsListObj)
        {
            jclass regionsListCls = (*env)->GetObjectClass(env, regionsListObj);
            jmethodID midRegionsSize = (*env)->GetMethodID(env, regionsListCls, "size", "()I");
            jmethodID midRegionsGet = (*env)->GetMethodID(env, regionsListCls, "get", "(I)Ljava/lang/Object;");
            jint numRegions = (*env)->CallIntMethod(env, regionsListObj, midRegionsSize);

            for (jint r = 0; r < numRegions; r++)
            {
                jobject geoPathObj = (*env)->CallObjectMethod(env, regionsListObj, midRegionsGet, r);
                GeographicalPath_t *gp = (GeographicalPath_t *)calloc(1, sizeof(*gp));

                if (ASN_SEQUENCE_ADD(&tdf->regions.list, gp) != 0)
                {
                    fprintf(stderr, "ASN_SEQUENCE_ADD(GeographicalPath) failed at index %d\n", (int)r);
                    free(gp);
                    (*env)->DeleteLocalRef(env, geoPathObj);
                    continue;
                }

                jclass geoPathCls = (*env)->GetObjectClass(env, geoPathObj);

                // --- getAnchor() ---
                jmethodID midGetAnchor = (*env)->GetMethodID(env, geoPathCls, "getAnchor", "()Lgov/usdot/cv/mapencoder/Position3D;");
                if (midGetAnchor)
                {
                    jobject anchorPosObj = (*env)->CallObjectMethod(env, geoPathObj, midGetAnchor);
                    if (anchorPosObj)
                    {
                        jclass anchorPosCls = (*env)->GetObjectClass(env, anchorPosObj);
                        jmethodID midAnchorLat = (*env)->GetMethodID(env, anchorPosCls, "getLatitude", "()D");
                        jmethodID midAnchorLon = (*env)->GetMethodID(env, anchorPosCls, "getLongitude", "()D");
                        jmethodID midAnchorEle = (*env)->GetMethodID(env, anchorPosCls, "getElevation", "()F");
                        jmethodID midAnchorEleExists = (*env)->GetMethodID(env, anchorPosCls, "isElevationExists", "()Z");

                        double anchorLat = midAnchorLat ? (*env)->CallDoubleMethod(env, anchorPosObj, midAnchorLat) : 0.0;
                        double anchorLon = midAnchorLon ? (*env)->CallDoubleMethod(env, anchorPosObj, midAnchorLon) : 0.0;
                        float anchorEle = midAnchorEle ? (*env)->CallFloatMethod(env, anchorPosObj, midAnchorEle) : 0.0f;
                        jboolean haveAnchorEle = midAnchorEleExists ? (*env)->CallBooleanMethod(env, anchorPosObj, midAnchorEleExists) : JNI_FALSE;

                        Position3D_t *anchor = (Position3D_t *)calloc(1, sizeof(*anchor));
                        if (!anchor)
                        {
                            fprintf(stderr, "calloc Position3D failed\n");
                        }
                        else
                        {
                            anchor->lat = (Common_Latitude_t)((long)anchorLat);
                            anchor->Long = (Common_Longitude_t)((long)anchorLon);

                            if (haveAnchorEle)
                            {
                                anchor->elevation = (Common_Elevation_t *)calloc(1, sizeof(*anchor->elevation));
                                if (anchor->elevation)
                                {
                                    *anchor->elevation = (long)(anchorEle); // convert float -> long if ASN type is integer-based
                                }
                            }

                            gp->anchor = anchor;
                        }

                        (*env)->DeleteLocalRef(env, anchorPosCls);
                        (*env)->DeleteLocalRef(env, anchorPosObj);
                    }
                }

                /* --- getLaneWidth() --- */
                jmethodID midGetLaneWidth = (*env)->GetMethodID(env, geoPathCls, "getLaneWidth", "()I");
                if (midGetLaneWidth)
                {
                    jint lwVal = (*env)->CallIntMethod(env, geoPathObj, midGetLaneWidth);
                    // allocate and store into gp->laneWidth (optional ASN.1 field)
                    gp->laneWidth = (LaneWidth_t *)calloc(1, sizeof(*gp->laneWidth));
                    if (gp->laneWidth)
                    {
                        *gp->laneWidth = (LaneWidth_t)lwVal;
                    }
                    else
                    {
                        fprintf(stderr, "calloc LaneWidth failed\n");
                    }
                }
                else
                {
                    {
                        fprintf(stderr, "Warning: getLaneWidth() method not found on GeographicalPath\n");
                    }
                }

                /* --- getDirectionality() --- */
                jmethodID midGetDirectionality = (*env)->GetMethodID(env, geoPathCls, "getDirectionality", "()Lgov/usdot/cv/timencoder/DirectionOfUse;");
                if (midGetDirectionality)
                {
                    jobject directionalityObj = (*env)->CallObjectMethod(env, geoPathObj, midGetDirectionality);
                    if (directionalityObj)
                    {
                        jclass directionalityCls = (*env)->GetObjectClass(env, directionalityObj);
                        jmethodID midGetValue = (*env)->GetMethodID(env, directionalityCls, "getValue", "()I");

                        if (midGetValue)
                        {
                            jint dirVal = (*env)->CallIntMethod(env, directionalityObj, midGetValue);

                            gp->directionality = (DirectionOfUse_t *)calloc(1, sizeof(*gp->directionality));
                            if (gp->directionality)
                            {
                                *gp->directionality = (DirectionOfUse_t)dirVal;
                            }
                            else
                            {
                                fprintf(stderr, "calloc DirectionOfUse failed\n");
                            }
                        }
                        else
                        {
                            fprintf(stderr, "Warning: getValue() method not found on DirectionOfUse\n");
                        }

                        (*env)->DeleteLocalRef(env, directionalityCls);
                        (*env)->DeleteLocalRef(env, directionalityObj);
                    }
                }
                else
                {
                    gp->directionality = NULL;
                }

                /* --- isClosedPath() --- */
                jmethodID midIsClosedPath = (*env)->GetMethodID(env, geoPathCls, "isClosedPath", "()Z");
                if (midIsClosedPath)
                {
                    jboolean closedVal = (*env)->CallBooleanMethod(env, geoPathObj, midIsClosedPath);

                    gp->closedPath = (BOOLEAN_t *)calloc(1, sizeof(*gp->closedPath));
                    if (gp->closedPath)
                    {
                        *gp->closedPath = closedVal ? 1 : 0;
                    }
                    else
                    {
                        fprintf(stderr, "calloc closedPath failed\n");
                    }
                }
                else
                {
                    fprintf(stderr, "Warning: isClosedPath() method not found on GeographicalPath\n");
                }

                /* --- getDirection() --- */
                jmethodID midGetDirection = (*env)->GetMethodID(env, geoPathCls, "getDirection", "()Lgov/usdot/cv/timencoder/HeadingSlice;");
                jobject directionObj = midGetDirection ? (*env)->CallObjectMethod(env, geoPathObj, midGetDirection) : NULL;

                if (directionObj)
                {
                    jclass directionCls = (*env)->GetObjectClass(env, directionObj);
                    jmethodID dirIntValue = (*env)->GetMethodID(env, directionCls, "intValue", "()I");

                    jint dirMask = 0;
                    if (dirIntValue)
                    {
                        dirMask = (*env)->CallIntMethod(env, directionObj, dirIntValue);
                        if ((*env)->ExceptionCheck(env))
                        {
                            (*env)->ExceptionDescribe(env);
                            (*env)->ExceptionClear(env);
                            dirMask = 0;
                        }
                    }

                    // Constrain to 16 bits
                    dirMask &= 0xFFFF;
                    gp->direction = (HeadingSlice_t *)calloc(1, sizeof(HeadingSlice_t));
                    if (gp->direction)
                    {
                        gp->direction->buf = (uint8_t *)calloc(1, 2);
                        if (gp->direction->buf)
                        {
                            gp->direction->buf[0] = (uint8_t)((dirMask >> 8) & 0xFF);
                            gp->direction->buf[1] = (uint8_t)(dirMask & 0xFF);
                            gp->direction->bits_unused = 0;
                            gp->direction->size = 2;
                        }
                    }

                    (*env)->DeleteLocalRef(env, directionCls);
                    (*env)->DeleteLocalRef(env, directionObj);
                }
                else
                {
                    gp->direction = NULL;
                }

                /* --- getDescription() --- */
                jmethodID midGetDescription = (*env)->GetMethodID(env, geoPathCls, "getDescription", "()Lgov/usdot/cv/timencoder/GeographicalPath$Description;");
                jobject descObj = midGetDescription ? (*env)->CallObjectMethod(env, geoPathObj, midGetDescription) : NULL;
                if (descObj)
                {
                    // Get the Choice enum object returned by Description.getChoice()
                    jclass descCls = (*env)->GetObjectClass(env, descObj);
                    jmethodID midGetChoice = (*env)->GetMethodID(env, descCls, "getChoice", "()Lgov/usdot/cv/timencoder/GeographicalPath$Description$Choice;");
                    jobject choiceObj = midGetChoice ? (*env)->CallObjectMethod(env, descObj, midGetChoice) : NULL;

                    int choiceOrdinal = -1;
                    if (choiceObj)
                    {
                        jclass choiceCls = (*env)->GetObjectClass(env, choiceObj);
                        jmethodID midOrdinal = (*env)->GetMethodID(env, choiceCls, "ordinal", "()I");
                        if (midOrdinal)
                        {
                            jint ord = (*env)->CallIntMethod(env, choiceObj, midOrdinal);
                            choiceOrdinal = (int)ord;
                        }
                    }

                    gp->description = (struct GeographicalPath__description *)calloc(1, sizeof(*gp->description));
                    switch (choiceOrdinal)
                    {
                    case 0: /* path_chosen */
                    {
                        gp->description->present = GeographicalPath__description_PR_path;

                        // get OffsetSystem from Description.getPathChosen()
                        jmethodID midGetPath = (*env)->GetMethodID(env, descCls, "getPathChosen", "()Lgov/usdot/cv/timencoder/OffsetSystem;");
                        jobject offsetSysObj = midGetPath ? (*env)->CallObjectMethod(env, descObj, midGetPath) : NULL;
                        if (!offsetSysObj)
                        {
                            printf("path_chosen: OffsetSystem is NULL\n");
                            break;
                        }
                        jclass offsetSysCls = (*env)->GetObjectClass(env, offsetSysObj);

                        // --- scale (Zoom) optional ---
                        jmethodID midGetScale = (*env)->GetMethodID(env, offsetSysCls, "getScale", "()Lgov/usdot/cv/timencoder/Zoom;");
                        jobject zoomObj = midGetScale ? (*env)->CallObjectMethod(env, offsetSysObj, midGetScale) : NULL;
                        if (zoomObj)
                        {
                            jclass zoomCls = (*env)->GetObjectClass(env, zoomObj);
                            jmethodID midGetValue = (*env)->GetMethodID(env, zoomCls, "getValue", "()I");
                            if (midGetValue)
                            {
                                jint zVal = (*env)->CallIntMethod(env, zoomObj, midGetValue);
                                gp->description->choice.path.scale = (Zoom_t *)calloc(1, sizeof(Zoom_t));
                                if (gp->description->choice.path.scale)
                                {
                                    *(gp->description->choice.path.scale) = (Zoom_t)zVal;
                                }
                                else
                                {
                                    fprintf(stderr, "calloc failed for OffsetSystem.scale\n");
                                }
                            }
                            (*env)->DeleteLocalRef(env, zoomCls);
                            (*env)->DeleteLocalRef(env, zoomObj);
                        }
                        else
                        {
                            gp->description->choice.path.scale = NULL;
                        }

                        // --- offset (choice: xy ) ---
                        jmethodID midGetOffset = (*env)->GetMethodID(env, offsetSysCls, "getOffset", "()Lgov/usdot/cv/timencoder/OffsetSystem$Offset;");
                        jobject offsetObj = midGetOffset ? (*env)->CallObjectMethod(env, offsetSysObj, midGetOffset) : NULL;
                        if (!offsetObj)
                        {
                            printf("OffsetSystem.offset is NULL\n");
                            (*env)->DeleteLocalRef(env, offsetSysCls);
                            (*env)->DeleteLocalRef(env, offsetSysObj);
                            break;
                        }

                        jclass offsetCls = (*env)->GetObjectClass(env, offsetObj);
                        // xy_chosen object
                        jmethodID midGetNodeListXY = (*env)->GetMethodID(env, offsetCls, "getXy_chosen", "()Lgov/usdot/cv/mapencoder/NodeListXY;");
                        jobject pathNodeListObj = midGetNodeListXY ? (*env)->CallObjectMethod(env, offsetObj, midGetNodeListXY) : NULL;

                        if (pathNodeListObj)
                        {
                            // offset.present = xy
                            gp->description->choice.path.offset.present = OffsetSystem__offset_PR_xy;

                            jclass pathNodeListCls = (*env)->GetObjectClass(env, pathNodeListObj);

                            jmethodID midGetPathNodeListChoice = (*env)->GetMethodID(env, pathNodeListCls, "getChoice", "()B");
                            jbyte pathNodeListChoice = pathNodeListChoice ? (*env)->CallByteMethod(env, pathNodeListObj, midGetPathNodeListChoice) : 0;

                            NodeSetXY_t *pathNodeSetXY = calloc(1, sizeof(NodeSetXY_t));

                            if (pathNodeListChoice == NODE_SET_XY)
                            {
                                gp->description->choice.path.offset.choice.xy.present = NodeListXY_PR_nodes;

                                // Get NodeSetXY object
                                jmethodID midGetNodes = (*env)->GetMethodID(env, pathNodeListCls, "getNodes", "()Lgov/usdot/cv/mapencoder/NodeSetXY;");
                                jobject pathNodesObj = (*env)->CallObjectMethod(env, pathNodeListObj, midGetNodes);
                                jclass pathNodesCls = (*env)->GetObjectClass(env, pathNodesObj);

                                // Retrieve NodeXY[] array
                                jmethodID midGetNodeSetXY = (*env)->GetMethodID(env, pathNodesCls, "getNodeSetXY", "()[Lgov/usdot/cv/mapencoder/NodeXY;");
                                jobject pathNodeSetXYArray = (*env)->CallObjectMethod(env, pathNodesObj, midGetNodeSetXY);

                                jsize pathNodesCount = (*env)->GetArrayLength(env, pathNodeSetXYArray);

                                for (int pathIndex = 0; pathIndex < pathNodesCount; pathIndex++)
                                {
                                    NodeXY_t *pathNodeXY = calloc(1, sizeof(NodeXY_t));

                                    jobject pathNodeXYObj = (jobject)(*env)->GetObjectArrayElement(env, pathNodeSetXYArray, pathIndex);
                                    jclass pathNodeXYCls = (*env)->GetObjectClass(env, pathNodeXYObj);

                                    // Get delta object
                                    jmethodID midGetDelta = (*env)->GetMethodID(env, pathNodeXYCls, "getDelta", "()Lgov/usdot/cv/mapencoder/NodeOffsetPointXY;");
                                    jobject pathDeltaObj = (*env)->CallObjectMethod(env, pathNodeXYObj, midGetDelta);
                                    jclass pathDeltaCls = (*env)->GetObjectClass(env, pathDeltaObj);

                                    // Get delta choice
                                    jmethodID midGetDeltaChoice = (*env)->GetMethodID(env, pathDeltaCls, "getChoice", "()B");
                                    jbyte pathDeltaChoice = (*env)->CallByteMethod(env, pathDeltaObj, midGetDeltaChoice);

                                    if (pathDeltaChoice == NODE_XY1)
                                    {
                                        jmethodID midGetXY1 = (*env)->GetMethodID(env, pathDeltaCls, "getNodeXY1", "()Lgov/usdot/cv/mapencoder/NodeXY20b;");
                                        jobject nodeXY1Obj = (*env)->CallObjectMethod(env, pathDeltaObj, midGetXY1);
                                        jclass nodeXY1Cls = (*env)->GetObjectClass(env, nodeXY1Obj);

                                        jmethodID midGetX = (*env)->GetMethodID(env, nodeXY1Cls, "getX", "()F");
                                        jmethodID midGetY = (*env)->GetMethodID(env, nodeXY1Cls, "getY", "()F");

                                        jfloat xVal = (*env)->CallFloatMethod(env, nodeXY1Obj, midGetX);
                                        jfloat yVal = (*env)->CallFloatMethod(env, nodeXY1Obj, midGetY);

                                        pathNodeXY->delta.present = NodeOffsetPointXY_PR_node_XY1;
                                        pathNodeXY->delta.choice.node_XY1.x = (Offset_B10_t)((long)xVal);
                                        pathNodeXY->delta.choice.node_XY1.y = (Offset_B10_t)((long)yVal);
                                    }
                                    else if (pathDeltaChoice == NODE_XY2)
                                    {
                                        jmethodID midGetXY2 = (*env)->GetMethodID(env, pathDeltaCls, "getNodeXY2", "()Lgov/usdot/cv/mapencoder/NodeXY22b;");
                                        jobject nodeXY2Obj = (*env)->CallObjectMethod(env, pathDeltaObj, midGetXY2);
                                        jclass nodeXY2Cls = (*env)->GetObjectClass(env, nodeXY2Obj);

                                        jmethodID midGetX = (*env)->GetMethodID(env, nodeXY2Cls, "getX", "()F");
                                        jmethodID midGetY = (*env)->GetMethodID(env, nodeXY2Cls, "getY", "()F");

                                        jfloat xVal = (*env)->CallFloatMethod(env, nodeXY2Obj, midGetX);
                                        jfloat yVal = (*env)->CallFloatMethod(env, nodeXY2Obj, midGetY);

                                        pathNodeXY->delta.present = NodeOffsetPointXY_PR_node_XY2;
                                        pathNodeXY->delta.choice.node_XY2.x = (Offset_B11_t)((long)xVal);
                                        pathNodeXY->delta.choice.node_XY2.y = (Offset_B11_t)((long)yVal);
                                    }
                                    else if (pathDeltaChoice == NODE_XY3)
                                    {
                                        jmethodID midGetXY3 = (*env)->GetMethodID(env, pathDeltaCls, "getNodeXY3", "()Lgov/usdot/cv/mapencoder/NodeXY24b;");
                                        jobject nodeXY3Obj = (*env)->CallObjectMethod(env, pathDeltaObj, midGetXY3);
                                        jclass nodeXY3Cls = (*env)->GetObjectClass(env, nodeXY3Obj);

                                        jmethodID midGetX = (*env)->GetMethodID(env, nodeXY3Cls, "getX", "()S");
                                        jmethodID midGetY = (*env)->GetMethodID(env, nodeXY3Cls, "getY", "()S");

                                        jshort xVal = (*env)->CallShortMethod(env, nodeXY3Obj, midGetX);
                                        jshort yVal = (*env)->CallShortMethod(env, nodeXY3Obj, midGetY);

                                        pathNodeXY->delta.present = NodeOffsetPointXY_PR_node_XY3;
                                        pathNodeXY->delta.choice.node_XY3.x = (Offset_B12_t)((long)xVal);
                                        pathNodeXY->delta.choice.node_XY3.y = (Offset_B12_t)((long)yVal);
                                    }
                                    else if (pathDeltaChoice == NODE_XY4)
                                    {
                                        jmethodID midGetXY4 = (*env)->GetMethodID(env, pathDeltaCls, "getNodeXY4", "()Lgov/usdot/cv/mapencoder/NodeXY26b;");
                                        jobject nodeXY4Obj = (*env)->CallObjectMethod(env, pathDeltaObj, midGetXY4);
                                        jclass nodeXY4Cls = (*env)->GetObjectClass(env, nodeXY4Obj);

                                        jmethodID midGetX = (*env)->GetMethodID(env, nodeXY4Cls, "getX", "()F");
                                        jmethodID midGetY = (*env)->GetMethodID(env, nodeXY4Cls, "getY", "()F");

                                        jfloat xVal = (*env)->CallFloatMethod(env, nodeXY4Obj, midGetX);
                                        jfloat yVal = (*env)->CallFloatMethod(env, nodeXY4Obj, midGetY);

                                        pathNodeXY->delta.present = NodeOffsetPointXY_PR_node_XY4;
                                        pathNodeXY->delta.choice.node_XY4.x = (Offset_B13_t)((long)xVal);
                                        pathNodeXY->delta.choice.node_XY4.y = (Offset_B13_t)((long)yVal);
                                    }
                                    else if (pathDeltaChoice == NODE_XY5)
                                    {
                                        jmethodID midGetXY5 = (*env)->GetMethodID(env, pathDeltaCls, "getNodeXY5", "()Lgov/usdot/cv/mapencoder/NodeXY28b;");
                                        jobject nodeXY5Obj = (*env)->CallObjectMethod(env, pathDeltaObj, midGetXY5);
                                        jclass nodeXY5Cls = (*env)->GetObjectClass(env, nodeXY5Obj);

                                        jmethodID midGetX = (*env)->GetMethodID(env, nodeXY5Cls, "getX", "()F");
                                        jmethodID midGetY = (*env)->GetMethodID(env, nodeXY5Cls, "getY", "()F");

                                        jfloat xVal = (*env)->CallFloatMethod(env, nodeXY5Obj, midGetX);
                                        jfloat yVal = (*env)->CallFloatMethod(env, nodeXY5Obj, midGetY);

                                        pathNodeXY->delta.present = NodeOffsetPointXY_PR_node_XY5;
                                        pathNodeXY->delta.choice.node_XY5.x = (Offset_B14_t)((long)xVal);
                                        pathNodeXY->delta.choice.node_XY5.y = (Offset_B14_t)((long)yVal);
                                    }
                                    else if (pathDeltaChoice == NODE_XY6)
                                    {
                                        jmethodID midGetXY6 = (*env)->GetMethodID(env, pathDeltaCls, "getNodeXY6", "()Lgov/usdot/cv/mapencoder/NodeXY32b;");
                                        jobject nodeXY6Obj = (*env)->CallObjectMethod(env, pathDeltaObj, midGetXY6);
                                        jclass nodeXY6Cls = (*env)->GetObjectClass(env, nodeXY6Obj);

                                        jmethodID midGetX = (*env)->GetMethodID(env, nodeXY6Cls, "getX", "()F");
                                        jmethodID midGetY = (*env)->GetMethodID(env, nodeXY6Cls, "getY", "()F");

                                        jfloat xVal = (*env)->CallFloatMethod(env, nodeXY6Obj, midGetX);
                                        jfloat yVal = (*env)->CallFloatMethod(env, nodeXY6Obj, midGetY);

                                        pathNodeXY->delta.present = NodeOffsetPointXY_PR_node_XY6;
                                        pathNodeXY->delta.choice.node_XY6.x = (Offset_B16_t)((long)xVal);
                                        pathNodeXY->delta.choice.node_XY6.y = (Offset_B16_t)((long)yVal);
                                    }
                                    else if (pathDeltaChoice == NODE_LAT_LON)
                                    {
                                        jmethodID midGetLatLon = (*env)->GetMethodID(env, pathDeltaCls, "getNodeLatLon", "()Lgov/usdot/cv/mapencoder/NodeLLmD64b;");
                                        jobject nodeLatLonObj = (*env)->CallObjectMethod(env, pathDeltaObj, midGetLatLon);
                                        jclass nodeLatLonCls = (*env)->GetObjectClass(env, nodeLatLonObj);

                                        jmethodID midGetLat = (*env)->GetMethodID(env, nodeLatLonCls, "getLatitude", "()I");
                                        jmethodID midGetLon = (*env)->GetMethodID(env, nodeLatLonCls, "getLongitude", "()I");

                                        jint lat = (*env)->CallIntMethod(env, nodeLatLonObj, midGetLat);
                                        jint lon = (*env)->CallIntMethod(env, nodeLatLonObj, midGetLon);

                                        pathNodeXY->delta.present = NodeOffsetPointXY_PR_node_LatLon;
                                        pathNodeXY->delta.choice.node_LatLon.lat = (Common_Latitude_t)((long)lat);
                                        pathNodeXY->delta.choice.node_LatLon.lon = (Common_Longitude_t)((long)lon);
                                    }
                                    else if (pathDeltaChoice == NODE_REGIONAL)
                                    {
                                        pathNodeXY->delta.present = NodeOffsetPointXY_PR_regional;
                                    }
                                    else
                                    {
                                        pathNodeXY->delta.present = NodeOffsetPointXY_PR_NOTHING;
                                    }

                                    jmethodID isPathAttributesExists = (*env)->GetMethodID(env, pathNodeXYCls, "isAttributesExists", "()Z");
                                    jboolean pathAttributesExists = (*env)->CallBooleanMethod(env, pathNodeXYObj, isPathAttributesExists);
                                    if (pathAttributesExists)
                                    {
                                        NodeAttributeSetXY_t *pathNodeAttributeSetXY;
                                        pathNodeAttributeSetXY = calloc(1, sizeof(NodeAttributeSetXY_t));

                                        // Get Attributes object and class
                                        jmethodID getPathAttributes = (*env)->GetMethodID(env, pathNodeXYCls, "getAttributes", "()Lgov/usdot/cv/mapencoder/NodeAttributeSetXY;");
                                        jobject pathAttributesObj = (*env)->CallObjectMethod(env, pathNodeXYObj, getPathAttributes);
                                        jclass pathNodeAttributeSetXYClass = (*env)->GetObjectClass(env, pathAttributesObj);

                                        // Check if dWidth exists
                                        jmethodID isPathDWidthExists = (*env)->GetMethodID(env, pathNodeAttributeSetXYClass, "isDWidthExists", "()Z");
                                        jboolean pathDWidthExists = (*env)->CallBooleanMethod(env, pathAttributesObj, isPathDWidthExists);

                                        if (pathDWidthExists)
                                        {
                                            jmethodID getPathDWidth = (*env)->GetMethodID(env, pathNodeAttributeSetXYClass, "getDWidth", "()F");
                                            jfloat pathDWidth = (*env)->CallFloatMethod(env, pathAttributesObj, getPathDWidth);

                                            Offset_B10_t *pathNodeDWidth = calloc(1, sizeof(Offset_B10_t));
                                            *pathNodeDWidth = (long)pathDWidth;
                                            pathNodeAttributeSetXY->dWidth = pathNodeDWidth;
                                        }

                                        // Check if dElevation exists
                                        jmethodID isPathDElevationExists = (*env)->GetMethodID(env, pathNodeAttributeSetXYClass, "isDElevationExists", "()Z");
                                        jboolean pathDElevationExists = (*env)->CallBooleanMethod(env, pathAttributesObj, isPathDElevationExists);

                                        if (pathDElevationExists)
                                        {
                                            jmethodID getPathDElevation = (*env)->GetMethodID(env, pathNodeAttributeSetXYClass, "getDElevation", "()F");
                                            jfloat pathDElevation = (*env)->CallFloatMethod(env, pathAttributesObj, getPathDElevation);

                                            Offset_B10_t *pathNodeDElevation = calloc(1, sizeof(Offset_B10_t));
                                            *pathNodeDElevation = (long)pathDElevation;
                                            pathNodeAttributeSetXY->dElevation = pathNodeDElevation;
                                        }

                                        pathNodeXY->attributes = pathNodeAttributeSetXY;

                                    }

                                    ASN_SEQUENCE_ADD(&pathNodeSetXY->list, pathNodeXY);
                                }
                                gp->description->choice.path.offset.choice.xy.choice.nodes = *pathNodeSetXY;
                            }
                        }

                        break;
                    }
                    case 1: /* geometry_chosen */
                    {
                        gp->description->present = GeographicalPath__description_PR_geometry;

                        jmethodID midGetGeometry = (*env)->GetMethodID(env, descCls, "getGeometryChosen", "()Lgov/usdot/cv/timencoder/GeometricProjection;");
                        jobject geomObj = midGetGeometry ? (*env)->CallObjectMethod(env, descObj, midGetGeometry) : NULL;

                        if (!geomObj)
                        {
                            printf("geometry_chosen: GeometricProjection object is NULL\n");
                            break;
                        }

                        jclass geomCls = (*env)->GetObjectClass(env, geomObj);

                        // getHeadingSlice() : HeadingSlice
                        jmethodID midGetHeadingSlice = (*env)->GetMethodID(env, geomCls, "getHeadingSlice", "()Lgov/usdot/cv/timencoder/HeadingSlice;");
                        jobject headingDirObj = midGetHeadingSlice ? (*env)->CallObjectMethod(env, geomObj, midGetHeadingSlice) : NULL;

                        if (headingDirObj)
                        {
                            jclass headingDirCls = (*env)->GetObjectClass(env, headingDirObj);
                            jmethodID headIntValue = (*env)->GetMethodID(env, headingDirCls, "intValue", "()I");

                            jint headMask = 0;
                            if (headIntValue)
                            {
                                headMask = (*env)->CallIntMethod(env, headingDirObj, headIntValue);
                                if ((*env)->ExceptionCheck(env))
                                {
                                    (*env)->ExceptionDescribe(env);
                                    (*env)->ExceptionClear(env);
                                    headMask = 0;
                                }
                            }

                            // Constrain to 16 bits
                            headMask &= 0xFFFF;
                            gp->description->choice.geometry.direction.buf = (uint8_t *)calloc(1, 2);
                            if (gp->description->choice.geometry.direction.buf)
                            {
                                gp->description->choice.geometry.direction.buf[0] = (uint8_t)((headMask >> 8) & 0xFF);
                                gp->description->choice.geometry.direction.buf[1] = (uint8_t)(headMask & 0xFF);
                                gp->description->choice.geometry.direction.bits_unused = 0;
                                gp->description->choice.geometry.direction.size = 2;
                            }

                            (*env)->DeleteLocalRef(env, headingDirCls);
                            (*env)->DeleteLocalRef(env, headingDirObj);
                        }
                        else
                        {
                            fprintf(stderr, "Warning: getDirection() returned NULL\n");
                        }

                        /* ---- extent (optional enum) ---- */
                        jmethodID midGetExtent = (*env)->GetMethodID(env, geomCls, "getExtent", "()Lgov/usdot/cv/timencoder/Extent;");
                        jobject extentObj = midGetExtent ? (*env)->CallObjectMethod(env, geomObj, midGetExtent) : NULL;
                        if (extentObj)
                        {
                            // Extent class has getValue() returning int
                            jclass extentCls = (*env)->GetObjectClass(env, extentObj);
                            jmethodID midGetValue = (*env)->GetMethodID(env, extentCls, "getValue", "()I");
                            if (midGetValue)
                            {
                                jint extentVal = (*env)->CallIntMethod(env, extentObj, midGetValue);
                                // assign to pointer field (note ASN type Extent_t is typedef long)
                                gp->description->choice.geometry.extent = (Extent_t *)calloc(1, sizeof(Extent_t));
                                if (gp->description->choice.geometry.extent)
                                {
                                    *(gp->description->choice.geometry.extent) = (Extent_t)extentVal;
                                }
                                else
                                {
                                    fprintf(stderr, "calloc failed for extent\n");
                                }
                            }
                            (*env)->DeleteLocalRef(env, extentCls);
                            (*env)->DeleteLocalRef(env, extentObj);
                        }
                        else
                        {
                            // optional - extent absent
                            gp->description->choice.geometry.extent = NULL;
                        }

                        /* ---- laneWidth (optional int) ---- */
                        jmethodID midGetLaneWidth = (*env)->GetMethodID(env, geomCls, "getLaneWidth", "()I");
                        if (midGetLaneWidth)
                        {
                            jint laneWidthVal = (*env)->CallIntMethod(env, geomObj, midGetLaneWidth);
                            gp->description->choice.geometry.laneWidth = (LaneWidth_t *)calloc(1, sizeof(LaneWidth_t));
                            if (gp->description->choice.geometry.laneWidth)
                            {
                                *(gp->description->choice.geometry.laneWidth) = (LaneWidth_t)laneWidthVal;
                            }
                            else
                            {
                                fprintf(stderr, "calloc failed for laneWidth\n");
                            }
                        }

                        /* ---- circle (required field ) ---- */
                        jmethodID midGetCircle = (*env)->GetMethodID(env, geomCls, "getCircle", "()Lgov/usdot/cv/timencoder/Circle;");
                        jobject circleObj = midGetCircle ? (*env)->CallObjectMethod(env, geomObj, midGetCircle) : NULL;
                        if (circleObj)
                        {
                            jclass circleCls = (*env)->GetObjectClass(env, circleObj);

                            /* 4a) center : Position3D */
                            jmethodID midGetCenter = (*env)->GetMethodID(env, circleCls, "getCenter", "()Lgov/usdot/cv/mapencoder/Position3D;");
                            jobject centerObj = midGetCenter ? (*env)->CallObjectMethod(env, circleObj, midGetCenter) : NULL;
                            if (centerObj)
                            {
                                jclass posCls = (*env)->GetObjectClass(env, centerObj);
                                jmethodID midGetLat = (*env)->GetMethodID(env, posCls, "getLatitude", "()D");
                                jmethodID midGetLon = (*env)->GetMethodID(env, posCls, "getLongitude", "()D");
                                jmethodID midGetElevation = (*env)->GetMethodID(env, posCls, "getElevation", "()F");
                                jmethodID midElevationExists = (*env)->GetMethodID(env, posCls, "isElevationExists", "()Z");

                                double centerLat = midGetLat ? (*env)->CallDoubleMethod(env, centerObj, midGetLat) : 0.0;
                                double centerLon = midGetLon ? (*env)->CallDoubleMethod(env, centerObj, midGetLon) : 0.0;
                                float centerElevation = midGetElevation ? (*env)->CallFloatMethod(env, centerObj, midGetElevation) : 0.0f;
                                jboolean centerElevExists = midElevationExists ? (*env)->CallBooleanMethod(env, centerObj, midElevationExists) : JNI_FALSE;

                                Common_Latitude_t lat_asn = (Common_Latitude_t)((long)centerLat);
                                Common_Longitude_t lon_asn = (Common_Longitude_t)((long)centerLon);

                                gp->description->choice.geometry.circle.center.lat = lat_asn;
                                gp->description->choice.geometry.circle.center.Long = lon_asn;

                                if (centerElevExists)
                                {
                                    gp->description->choice.geometry.circle.center.elevation = (Common_Elevation_t *)calloc(1, sizeof(Common_Elevation_t));
                                    if (gp->description->choice.geometry.circle.center.elevation)
                                    {
                                        *(gp->description->choice.geometry.circle.center.elevation) = (long)(centerElevation);
                                    }
                                    else
                                    {
                                        fprintf(stderr, "calloc failed for center elevation\n");
                                    }
                                }
                                else
                                {
                                    gp->description->choice.geometry.circle.center.elevation = NULL;
                                }
                            }
                            else
                            {
                                printf("Circle.center is NULL\n");
                            }

                            /* 4b) radius : Radius_B12 -> typically integer */
                            jmethodID midGetRadius = (*env)->GetMethodID(env, circleCls, "getRadius", "()Lgov/usdot/cv/timencoder/Radius_B12;");
                            jobject radiusObj = midGetRadius ? (*env)->CallObjectMethod(env, circleObj, midGetRadius) : NULL;
                            if (radiusObj)
                            {
                                jclass radCls = (*env)->GetObjectClass(env, radiusObj);
                                jmethodID midGetRadValue = (*env)->GetMethodID(env, radCls, "getValue", "()I");
                                if (midGetRadValue)
                                {
                                    jint radiusVal = (*env)->CallIntMethod(env, radiusObj, midGetRadValue);
                                    gp->description->choice.geometry.circle.radius = (Radius_B12_t)radiusVal;
                                }
                                (*env)->DeleteLocalRef(env, radCls);
                                (*env)->DeleteLocalRef(env, radiusObj);
                            }
                            else
                            {
                                printf("Circle.radius is NULL\n");
                            }

                            /* units : DistanceUnits */
                            jmethodID midGetUnits = (*env)->GetMethodID(env, circleCls, "getUnits", "()Lgov/usdot/cv/timencoder/DistanceUnits;");
                            jobject unitsObj = midGetUnits ? (*env)->CallObjectMethod(env, circleObj, midGetUnits) : NULL;
                            if (unitsObj)
                            {
                                jclass unitsCls = (*env)->GetObjectClass(env, unitsObj);
                                jmethodID midGetUnitsVal = (*env)->GetMethodID(env, unitsCls, "getValue", "()I");
                                if (midGetUnitsVal)
                                {
                                    jint uVal = (*env)->CallIntMethod(env, unitsObj, midGetUnitsVal);
                                    gp->description->choice.geometry.circle.units = (DistanceUnits_t)uVal;
                                }
                                (*env)->DeleteLocalRef(env, unitsCls);
                                (*env)->DeleteLocalRef(env, unitsObj);
                            }
                            else
                            {
                                printf("Circle.units is NULL (using default?)\n");
                            }

                            (*env)->DeleteLocalRef(env, circleCls);
                            (*env)->DeleteLocalRef(env, circleObj);
                        }
                        else
                        {
                            printf("GeometricProjection.circle is NULL\n");
                        }

                        break;
                    }
                    default:
                        gp->description->present = GeographicalPath__description_PR_NOTHING;
                        break;
                    }
                }
            }
        }
        else
        {
            printf("Regions array is null\n");
        }

        // doNotUse3
        mid_getDoNotUse3 = (*env)->GetMethodID(env, frameCls, "getDoNotUse3", "()Lgov/usdot/cv/timencoder/SSPindex;");
        jobject doNotUse3 = mid_getDoNotUse3 ? (*env)->CallObjectMethod(env, frame, mid_getDoNotUse3) : NULL;
        if (doNotUse3)
        {
            jclass doNotUse3Cls = (*env)->GetObjectClass(env, doNotUse3);
            jmethodID midGetIndex3 = (*env)->GetMethodID(env, doNotUse3Cls, "get", "()I");
            jint index3 = (*env)->CallIntMethod(env, doNotUse3, midGetIndex3);
            tdf->doNotUse3 = (long)index3;
            (*env)->DeleteLocalRef(env, doNotUse3Cls);
        }

        // doNotUse4
        mid_getDoNotUse4 = (*env)->GetMethodID(env, frameCls, "getDoNotUse4", "()Lgov/usdot/cv/timencoder/SSPindex;");
        jobject doNotUse4 = mid_getDoNotUse4 ? (*env)->CallObjectMethod(env, frame, mid_getDoNotUse4) : NULL;
        if (doNotUse4)
        {
            jclass doNotUse4Cls = (*env)->GetObjectClass(env, doNotUse4);
            jmethodID midGetIndex4 = (*env)->GetMethodID(env, doNotUse4Cls, "get", "()I");
            jint index4 = (*env)->CallIntMethod(env, doNotUse4, midGetIndex4);
            tdf->doNotUse4 = (long)index4;
            (*env)->DeleteLocalRef(env, doNotUse4Cls);
        }

        // ---- content (legacy Part II) ----
        mid_getContent = (*env)->GetMethodID(env, frameCls, "getContent", "()Lgov/usdot/cv/timencoder/TravelerDataFrame$Content;");
        jobject content = mid_getContent ? (*env)->CallObjectMethod(env, frame, mid_getContent) : NULL;
        if (content)
        {
            jclass contentCls = (*env)->GetObjectClass(env, content);
            jmethodID midGetContentChoice = (*env)->GetMethodID(
                env, contentCls, "getChoice",
                "()Lgov/usdot/cv/timencoder/TravelerDataFrame$Content$Choice;");
            if (midGetContentChoice)
            {
                jobject contentChoiceObj = (*env)->CallObjectMethod(env, content, midGetContentChoice);
                if (contentChoiceObj)
                {
                    jclass enumCls = (*env)->GetObjectClass(env, contentChoiceObj);
                    jmethodID midName = (*env)->GetMethodID(env, enumCls, "name", "()Ljava/lang/String;");
                    // ordinal() returns the position in enum declaration, starting from 0
                    jmethodID midOrdinal = (*env)->GetMethodID(env, enumCls, "ordinal", "()I");
                    jstring nameStr = (jstring)(*env)->CallObjectMethod(env, contentChoiceObj, midName);
                    jint ordinal = (*env)->CallIntMethod(env, contentChoiceObj, midOrdinal);

                    switch (ordinal)
                    {

                    case 0:
                    {
                        tdf->content.present = TravelerDataFrame__content_PR_advisory;

                        // Content.getSpeedLimit(): SpeedLimit
                        jmethodID midGetSL = (*env)->GetMethodID(
                            env, contentCls, "getAdvisory", "()Lgov/usdot/cv/timencoder/ITIScodesAndText;");
                        jobject slObj = midGetSL ? (*env)->CallObjectMethod(env, content, midGetSL) : NULL;
                        if ((*env)->ExceptionCheck(env))
                        {
                            (*env)->ExceptionDescribe(env);
                            (*env)->ExceptionClear(env);
                            slObj = NULL;
                        }
                        if (!slObj)
                        {
                            printf("ITISCodeandText is null\n");
                            break;
                        }

                        jclass slCls = (*env)->GetObjectClass(env, slObj);
                        // SpeedLimit.getItems(): List<RegulatorySpeedLimit> (or similar)
                        jmethodID midGetItems = (*env)->GetMethodID(env, slCls, "getItems", "()Ljava/util/List;");
                        jobject itemsList = midGetItems ? (*env)->CallObjectMethod(env, slObj, midGetItems) : NULL;
                        if ((*env)->ExceptionCheck(env))
                        {
                            (*env)->ExceptionDescribe(env);
                            (*env)->ExceptionClear(env);
                            itemsList = NULL;
                        }
                        if (!itemsList)
                        {
                            printf("SpeedLimit.items is null\n");
                            (*env)->DeleteLocalRef(env, slCls);
                            (*env)->DeleteLocalRef(env, slObj);
                            break;
                        }

                        jclass listCls = (*env)->GetObjectClass(env, itemsList);
                        jmethodID midIterator = (*env)->GetMethodID(env, listCls, "iterator", "()Ljava/util/Iterator;");
                        jobject iterator = (*env)->CallObjectMethod(env, itemsList, midIterator);
                        if ((*env)->ExceptionCheck(env))
                        {
                            (*env)->ExceptionDescribe(env);
                            (*env)->ExceptionClear(env);
                            iterator = NULL;
                        }

                        if (iterator)
                        {
                            jclass itCls = (*env)->GetObjectClass(env, iterator);
                            jmethodID midHasNext = (*env)->GetMethodID(env, itCls, "hasNext", "()Z");
                            jmethodID midNext = (*env)->GetMethodID(env, itCls, "next", "()Ljava/lang/Object;");

                            while ((*env)->CallBooleanMethod(env, iterator, midHasNext))
                            {
                                jobject itemObj = (*env)->CallObjectMethod(env, iterator, midNext);
                                if ((*env)->ExceptionCheck(env))
                                {
                                    (*env)->ExceptionDescribe(env);
                                    (*env)->ExceptionClear(env);
                                    break;
                                }
                                if (!itemObj)
                                    continue;

                                jclass itemCls = (*env)->GetObjectClass(env, itemObj);
                                jmethodID midGetChoice = (*env)->GetMethodID(
                                    env, itemCls, "getChoice",
                                    "()Lgov/usdot/cv/timencoder/ITIScodesAndText$Item$Choice;");
                                jobject choiceObj = midGetChoice ? (*env)->CallObjectMethod(env, itemObj, midGetChoice) : NULL;

                                if ((*env)->ExceptionCheck(env))
                                {
                                    (*env)->ExceptionDescribe(env);
                                    (*env)->ExceptionClear(env);
                                    choiceObj = NULL;
                                }
                                const char *ename = NULL;
                                jstring jname = NULL;

                                if (choiceObj)
                                {
                                    jclass choiceCls = (*env)->GetObjectClass(env, choiceObj);
                                    jmethodID midName = (*env)->GetMethodID(env, choiceCls, "name", "()Ljava/lang/String;");
                                    if (midName)
                                    {
                                        jname = (jstring)(*env)->CallObjectMethod(env, choiceObj, midName);
                                        if ((*env)->ExceptionCheck(env))
                                        {
                                            (*env)->ExceptionDescribe(env);
                                            (*env)->ExceptionClear(env);
                                            jname = NULL;
                                        }
                                        if (jname)
                                            ename = (*env)->GetStringUTFChars(env, jname, 0);
                                    }
                                    (*env)->DeleteLocalRef(env, choiceCls);
                                    (*env)->DeleteLocalRef(env, choiceObj);
                                }
                                if (!ename)
                                {

                                    (*env)->DeleteLocalRef(env, itemCls);
                                    (*env)->DeleteLocalRef(env, itemObj);
                                    if (jname)
                                        (*env)->DeleteLocalRef(env, jname);

                                    continue;
                                }
                                if (strcmp(ename, "ITIS") == 0)
                                {
                                    // GenericSignageItem.getItis(): ITIScodes
                                    jmethodID midGetItis = (*env)->GetMethodID(
                                        env, itemCls, "getItis", "()Lgov/usdot/cv/timencoder/ITIScodes;");
                                    jobject itisObj = midGetItis ? (*env)->CallObjectMethod(env, itemObj, midGetItis) : NULL;
                                    if ((*env)->ExceptionCheck(env))
                                    {
                                        (*env)->ExceptionDescribe(env);
                                        (*env)->ExceptionClear(env);
                                        itisObj = NULL;
                                    }
                                    if (itisObj)
                                    {
                                        long code = get_long_from_java_number_like(env, itisObj); // Calling helper function to get ITISCODE
                                        struct ITIS_ITIScodesAndText__Member *node =
                                            (struct ITIS_ITIScodesAndText__Member *)calloc(1, sizeof(*node));
                                        if (node)
                                        {
                                            node->item.present = ITIS_ITIScodesAndText__Member__item_PR_itis;
                                            node->item.choice.itis = (ITIS_ITIScodes_t)code;
                                            if (ASN_SEQUENCE_ADD(&tdf->content.choice.speedLimit.list, node) != 0)
                                            {
                                                fprintf(stderr, "ASN_SEQUENCE_ADD(ITISCodeandText itis) failed\n");
                                                free(node);
                                            }
                                        }
                                        (*env)->DeleteLocalRef(env, itisObj);
                                    }
                                }
                                else if (strcmp(ename, "TEXT") == 0)
                                {
                                    // GenericSignageItem.getText(): ITISTextPhrase (Java)
                                    jmethodID midGetText = (*env)->GetMethodID(
                                        env, itemCls, "getText", "()Lgov/usdot/cv/timencoder/ITISTextPhrase;");
                                    jobject textObj = midGetText ? (*env)->CallObjectMethod(env, itemObj, midGetText) : NULL;
                                    if ((*env)->ExceptionCheck(env))
                                    {
                                        (*env)->ExceptionDescribe(env);
                                        (*env)->ExceptionClear(env);
                                        textObj = NULL;
                                    }
                                    if (textObj)
                                    {
                                        struct ITIS_ITIScodesAndText__Member *node =
                                            (struct ITIS_ITIScodesAndText__Member *)calloc(1, sizeof(*node));
                                        if (node)
                                        {
                                            node->item.present = ITIS_ITIScodesAndText__Member__item_PR_text; // IA5String(1..63)
                                            if (!fill_octet_from_java_string_like(env, &node->item.choice.text, textObj, 63))
                                            {
                                                free(node);
                                            }
                                            else if (ASN_SEQUENCE_ADD(&tdf->content.choice.genericSign.list, node) != 0)
                                            {
                                                fprintf(stderr, "ASN_SEQUENCE_ADD(ITISCodeandText text) failed\n");
                                                free(node);
                                            }
                                        }
                                        (*env)->DeleteLocalRef(env, textObj);
                                    }
                                }

                                if (jname && ename)
                                    (*env)->ReleaseStringUTFChars(env, jname, ename);
                                if (jname)
                                    (*env)->DeleteLocalRef(env, jname);
                                (*env)->DeleteLocalRef(env, itemCls);
                                (*env)->DeleteLocalRef(env, itemObj);
                            }
                            (*env)->DeleteLocalRef(env, itCls);
                            (*env)->DeleteLocalRef(env, iterator);
                        }
                        else
                        {
                            fprintf(stderr, "ITISCodeandText : iterator() returned null\n");
                        }

                        (*env)->DeleteLocalRef(env, listCls);
                        (*env)->DeleteLocalRef(env, slCls);
                        (*env)->DeleteLocalRef(env, itemsList);
                        (*env)->DeleteLocalRef(env, slObj);
                        break;
                    }

                    case 1:
                    { // WORK_ZONE
                        tdf->content.present = TravelerDataFrame__content_PR_workZone;
                        jmethodID midGetWZ = (*env)->GetMethodID(
                            env, contentCls, "getWorkZone", "()Lgov/usdot/cv/timencoder/WorkZone;");
                        jobject wzObj = midGetWZ ? (*env)->CallObjectMethod(env, content, midGetWZ) : NULL;
                        if ((*env)->ExceptionCheck(env))
                        {
                            (*env)->ExceptionDescribe(env);
                            (*env)->ExceptionClear(env);
                            wzObj = NULL;
                        }
                        if (!wzObj)
                        {
                            printf("WorkZone is null\n");
                            return;
                        }

                        jclass wzCls = (*env)->GetObjectClass(env, wzObj);
                        jmethodID midGetItems = (*env)->GetMethodID(env, wzCls,
                                                                    "getItems", "()Ljava/util/List;");
                        jobject itemsList = midGetItems ? (*env)->CallObjectMethod(env, wzObj, midGetItems) : NULL;
                        if ((*env)->ExceptionCheck(env))
                        {
                            (*env)->ExceptionDescribe(env);
                            (*env)->ExceptionClear(env);
                            itemsList = NULL;
                        }

                        if (!itemsList)
                        {
                            printf("WorkZone.items is null\n");
                            (*env)->DeleteLocalRef(env, wzCls);
                            (*env)->DeleteLocalRef(env, wzObj);
                            return;
                        }

                        jclass listCls = (*env)->GetObjectClass(env, itemsList);
                        jmethodID midIterator = (*env)->GetMethodID(env, listCls,
                                                                    "iterator", "()Ljava/util/Iterator;");
                        jobject iterator = (*env)->CallObjectMethod(env, itemsList, midIterator);

                        if ((*env)->ExceptionCheck(env))
                        {
                            (*env)->ExceptionDescribe(env);
                            (*env)->ExceptionClear(env);
                            iterator = NULL;
                        }

                        if (iterator)
                        {
                            jclass itCls = (*env)->GetObjectClass(env, iterator);
                            jmethodID midHasNext = (*env)->GetMethodID(env, itCls, "hasNext", "()Z");
                            jmethodID midNext = (*env)->GetMethodID(env, itCls, "next", "()Ljava/lang/Object;");

                            int idx = 0;
                            while ((*env)->CallBooleanMethod(env, iterator, midHasNext))
                            {
                                jobject itemObj = (*env)->CallObjectMethod(env, iterator, midNext);
                                if ((*env)->ExceptionCheck(env))
                                {
                                    (*env)->ExceptionDescribe(env);
                                    (*env)->ExceptionClear(env);
                                    break;
                                }
                                if (!itemObj)
                                {
                                    idx++;
                                    continue;
                                }

                                jclass itemCls = (*env)->GetObjectClass(env, itemObj);

                                // WorkZoneItem.getChoice(): Choice (enum)
                                jmethodID midGetChoice = (*env)->GetMethodID(
                                    env, itemCls, "getChoice",
                                    "()Lgov/usdot/cv/timencoder/WorkZone$WorkZoneItem$Choice;");
                                jobject choiceObj = midGetChoice ? (*env)->CallObjectMethod(env, itemObj, midGetChoice) : NULL;

                                jint ordinal = -1;
                                if (choiceObj)
                                {

                                    jclass choiceCls = (*env)->GetObjectClass(env, choiceObj);
                                    jmethodID midOrdinal = (*env)->GetMethodID(env, choiceCls, "ordinal", "()I");
                                    if (midOrdinal)
                                        ordinal = (*env)->CallIntMethod(env, choiceObj, midOrdinal);
                                    (*env)->DeleteLocalRef(env, choiceCls);
                                    (*env)->DeleteLocalRef(env, choiceObj);
                                }

                                if (ordinal == 0)
                                {

                                    jmethodID midGetItis = (*env)->GetMethodID(
                                        env, itemCls, "getItis", "()Lgov/usdot/cv/timencoder/ITIScodes;");
                                    jobject itisObj = midGetItis ? (*env)->CallObjectMethod(env, itemObj, midGetItis) : NULL;
                                    if ((*env)->ExceptionCheck(env))
                                    {
                                        (*env)->ExceptionDescribe(env);
                                        (*env)->ExceptionClear(env);
                                    }
                                    if (itisObj)
                                    {
                                        long code = get_long_from_java_number_like(env, itisObj);
                                        struct WorkZone__Member *witem = (struct WorkZone__Member *)calloc(1, sizeof(*witem));
                                        if (witem)
                                        {
                                            witem->item.present = WorkZone__Member__item_PR_itis;
                                            witem->item.choice.itis = code; // ITIScodes is INTEGER in ASN.1
                                            if (ASN_SEQUENCE_ADD(&tdf->content.choice.workZone.list, witem) != 0)
                                            {
                                                fprintf(stderr, "  ASN_SEQUENCE_ADD(itis) failed\n");
                                                free(witem);
                                            }
                                        }
                                        (*env)->DeleteLocalRef(env, itisObj);
                                    }
                                    else
                                    {
                                        // printf("  WZ[%d] ITIS object null\n", (int)k);
                                    }
                                }
                                else if (ordinal == 1)
                                {

                                    jmethodID midGetText = (*env)->GetMethodID(
                                        env, itemCls, "getText", "()Lgov/usdot/cv/timencoder/ITISTextPhrase;");
                                    jobject textObj = midGetText ? (*env)->CallObjectMethod(env, itemObj, midGetText) : NULL;
                                    if ((*env)->ExceptionCheck(env))
                                    {
                                        (*env)->ExceptionDescribe(env);
                                        (*env)->ExceptionClear(env);
                                    }
                                    if (textObj)
                                    {
                                        struct WorkZone__Member *witem = (struct WorkZone__Member *)calloc(1, sizeof(*witem));
                                        if (witem)
                                        {
                                            witem->item.present = WorkZone__Member__item_PR_text;
                                            // ITISTextPhrase ::= IA5String(SIZE(1..63))
                                            if (!fill_octet_from_java_string_like(env, &witem->item.choice.text, textObj, 63))
                                            {

                                                free(witem);
                                            }
                                            else
                                            {
                                                if (ASN_SEQUENCE_ADD(&tdf->content.choice.workZone.list, witem) != 0)
                                                {
                                                    fprintf(stderr, "  ASN_SEQUENCE_ADD(text) failed\n");
                                                    free(witem);
                                                }
                                            }
                                        }
                                        (*env)->DeleteLocalRef(env, textObj);
                                    }
                                    else
                                    {
                                        // printf("  WZ[%d] TEXT object null\n", (int)k);
                                    }
                                }
                                else
                                {
                                    // printf("  WZ[%d] unknown choice (ordinal=%d)\n", (int)k, (int)ordinal);
                                }

                                (*env)->DeleteLocalRef(env, itemCls);
                                (*env)->DeleteLocalRef(env, itemObj);
                            }

                            // cleanup locals
                            (*env)->DeleteLocalRef(env, listCls);
                            (*env)->DeleteLocalRef(env, itemsList);
                            (*env)->DeleteLocalRef(env, wzCls);
                            (*env)->DeleteLocalRef(env, wzObj);
                            break;
                        }

                    case 2:
                    {

                        tdf->content.present = TravelerDataFrame__content_PR_genericSign;

                        // ---- get GenericSignage ----
                        jmethodID midGetGS = (*env)->GetMethodID(
                            env, contentCls, "getGenericSign", "()Lgov/usdot/cv/timencoder/GenericSignage;");
                        jobject gsObj = midGetGS ? (*env)->CallObjectMethod(env, content, midGetGS) : NULL;
                        if ((*env)->ExceptionCheck(env))
                        {
                            (*env)->ExceptionDescribe(env);
                            (*env)->ExceptionClear(env);
                            gsObj = NULL;
                        }
                        if (!gsObj)
                        {

                            break;
                        }

                        jclass gsCls = (*env)->GetObjectClass(env, gsObj);
                        jmethodID midGetItems = (*env)->GetMethodID(env, gsCls, "getItems", "()Ljava/util/List;");
                        jobject itemsList = midGetItems ? (*env)->CallObjectMethod(env, gsObj, midGetItems) : NULL;
                        if ((*env)->ExceptionCheck(env))
                        {
                            (*env)->ExceptionDescribe(env);
                            (*env)->ExceptionClear(env);
                            itemsList = NULL;
                        }
                        if (!itemsList)
                        {
                            (*env)->DeleteLocalRef(env, gsCls);
                            (*env)->DeleteLocalRef(env, gsObj);
                            break;
                        }

                        jclass listCls = (*env)->GetObjectClass(env, itemsList);
                        jmethodID midIter = (*env)->GetMethodID(env, listCls, "iterator", "()Ljava/util/Iterator;");
                        jobject iterator = midIter ? (*env)->CallObjectMethod(env, itemsList, midIter) : NULL;
                        if ((*env)->ExceptionCheck(env))
                        {
                            (*env)->ExceptionDescribe(env);
                            (*env)->ExceptionClear(env);
                            iterator = NULL;
                        }

                        if (iterator)
                        {
                            jclass itCls = (*env)->GetObjectClass(env, iterator);
                            jmethodID midHasNext = (*env)->GetMethodID(env, itCls, "hasNext", "()Z");
                            jmethodID midNext = (*env)->GetMethodID(env, itCls, "next", "()Ljava/lang/Object;");

                            while (midHasNext && (*env)->CallBooleanMethod(env, iterator, midHasNext))
                            {
                                if ((*env)->ExceptionCheck(env))
                                {
                                    (*env)->ExceptionDescribe(env);
                                    (*env)->ExceptionClear(env);
                                    break;
                                }

                                jobject itemObj = midNext ? (*env)->CallObjectMethod(env, iterator, midNext) : NULL;
                                if ((*env)->ExceptionCheck(env))
                                {
                                    (*env)->ExceptionDescribe(env);
                                    (*env)->ExceptionClear(env);
                                    break;
                                }
                                if (!itemObj)
                                    continue;

                                jclass itemCls = (*env)->GetObjectClass(env, itemObj);

                                jmethodID midGetChoice = (*env)->GetMethodID(
                                    env, itemCls, "getChoice",
                                    "()Lgov/usdot/cv/timencoder/GenericSignage$GenericSignageItem$Choice;");
                                jobject choiceObj = midGetChoice ? (*env)->CallObjectMethod(env, itemObj, midGetChoice) : NULL;
                                if ((*env)->ExceptionCheck(env))
                                {
                                    (*env)->ExceptionDescribe(env);
                                    (*env)->ExceptionClear(env);
                                    choiceObj = NULL;
                                }

                                const char *ename = NULL;
                                jstring jname = NULL;
                                if (choiceObj)
                                {
                                    jclass choiceCls = (*env)->GetObjectClass(env, choiceObj);
                                    jmethodID midName = (*env)->GetMethodID(env, choiceCls, "name", "()Ljava/lang/String;");
                                    if (midName)
                                    {
                                        jname = (jstring)(*env)->CallObjectMethod(env, choiceObj, midName);
                                        if ((*env)->ExceptionCheck(env))
                                        {
                                            (*env)->ExceptionDescribe(env);
                                            (*env)->ExceptionClear(env);
                                            jname = NULL;
                                        }
                                        if (jname)
                                            ename = (*env)->GetStringUTFChars(env, jname, 0);
                                    }
                                    (*env)->DeleteLocalRef(env, choiceCls);
                                    (*env)->DeleteLocalRef(env, choiceObj);
                                }

                                if (ename)
                                {
                                    if (strcmp(ename, "ITIS") == 0)
                                    {
                                        jmethodID midGetItis = (*env)->GetMethodID(
                                            env, itemCls, "getItis", "()Lgov/usdot/cv/timencoder/ITIScodes;");
                                        jobject itisObj = midGetItis ? (*env)->CallObjectMethod(env, itemObj, midGetItis) : NULL;
                                        if ((*env)->ExceptionCheck(env))
                                        {
                                            (*env)->ExceptionDescribe(env);
                                            (*env)->ExceptionClear(env);
                                            itisObj = NULL;
                                        }
                                        if (itisObj)
                                        {
                                            long code = get_long_from_java_number_like(env, itisObj);
                                            struct GenericSignage__Member *node =
                                                (struct GenericSignage__Member *)calloc(1, sizeof(*node));
                                            if (node)
                                            {
                                                node->item.present = GenericSignage__Member__item_PR_itis;
                                                node->item.choice.itis = (ITIS_ITIScodes_t)code;
                                                if (ASN_SEQUENCE_ADD(&tdf->content.choice.genericSign.list, node) != 0)
                                                {
                                                    free(node);
                                                }
                                            }
                                            (*env)->DeleteLocalRef(env, itisObj);
                                        }
                                    }
                                    else if (strcmp(ename, "TEXT") == 0)
                                    {
                                        jmethodID midGetText = (*env)->GetMethodID(
                                            env, itemCls, "getText", "()Lgov/usdot/cv/timencoder/ITISTextPhrase;");
                                        jobject textObj = midGetText ? (*env)->CallObjectMethod(env, itemObj, midGetText) : NULL;
                                        if ((*env)->ExceptionCheck(env))
                                        {
                                            (*env)->ExceptionDescribe(env);
                                            (*env)->ExceptionClear(env);
                                            textObj = NULL;
                                        }
                                        if (textObj)
                                        {
                                            struct GenericSignage__Member *node =
                                                (struct GenericSignage__Member *)calloc(1, sizeof(*node));
                                            if (node)
                                            {
                                                node->item.present = GenericSignage__Member__item_PR_text; // IA5String(1..63)
                                                if (!fill_octet_from_java_string_like(env, &node->item.choice.text, textObj, 63))
                                                {
                                                    free(node);
                                                }
                                                else if (ASN_SEQUENCE_ADD(&tdf->content.choice.genericSign.list, node) != 0)
                                                {
                                                    free(node);
                                                }
                                            }
                                            (*env)->DeleteLocalRef(env, textObj);
                                        }
                                    }
                                }

                                if (jname && ename)
                                    (*env)->ReleaseStringUTFChars(env, jname, ename);
                                if (jname)
                                    (*env)->DeleteLocalRef(env, jname);
                                (*env)->DeleteLocalRef(env, itemCls);
                                (*env)->DeleteLocalRef(env, itemObj);
                            }

                            (*env)->DeleteLocalRef(env, itCls);
                            (*env)->DeleteLocalRef(env, iterator);
                        }

                        // cleanup locals
                        (*env)->DeleteLocalRef(env, listCls);
                        (*env)->DeleteLocalRef(env, gsCls);
                        (*env)->DeleteLocalRef(env, itemsList);
                        (*env)->DeleteLocalRef(env, gsObj);
                        break;
                    }

                    case 3:
                    {
                        tdf->content.present = TravelerDataFrame__content_PR_speedLimit;

                        // Content.getSpeedLimit(): SpeedLimit
                        jmethodID midGetSL = (*env)->GetMethodID(
                            env, contentCls, "getSpeedLimit", "()Lgov/usdot/cv/timencoder/SpeedLimit;");
                        jobject slObj = midGetSL ? (*env)->CallObjectMethod(env, content, midGetSL) : NULL;
                        if ((*env)->ExceptionCheck(env))
                        {
                            (*env)->ExceptionDescribe(env);
                            (*env)->ExceptionClear(env);
                            slObj = NULL;
                        }
                        if (!slObj)
                        {
                            printf("SpeedLimit is null\n");
                            break;
                        }

                        jclass slCls = (*env)->GetObjectClass(env, slObj);
                        jmethodID midGetItems = (*env)->GetMethodID(env, slCls, "getItems", "()Ljava/util/List;");
                        jobject itemsList = midGetItems ? (*env)->CallObjectMethod(env, slObj, midGetItems) : NULL;
                        if ((*env)->ExceptionCheck(env))
                        {
                            (*env)->ExceptionDescribe(env);
                            (*env)->ExceptionClear(env);
                            itemsList = NULL;
                        }
                        if (!itemsList)
                        {
                            printf("SpeedLimit.items is null\n");
                            (*env)->DeleteLocalRef(env, slCls);
                            (*env)->DeleteLocalRef(env, slObj);
                            break;
                        }

                        jclass listCls = (*env)->GetObjectClass(env, itemsList);
                        jmethodID midIterator = (*env)->GetMethodID(env, listCls, "iterator", "()Ljava/util/Iterator;");
                        jobject iterator = (*env)->CallObjectMethod(env, itemsList, midIterator);
                        if ((*env)->ExceptionCheck(env))
                        {
                            (*env)->ExceptionDescribe(env);
                            (*env)->ExceptionClear(env);
                            iterator = NULL;
                        }

                        if (iterator)
                        {
                            jclass itCls = (*env)->GetObjectClass(env, iterator);
                            jmethodID midHasNext = (*env)->GetMethodID(env, itCls, "hasNext", "()Z");
                            jmethodID midNext = (*env)->GetMethodID(env, itCls, "next", "()Ljava/lang/Object;");

                            while ((*env)->CallBooleanMethod(env, iterator, midHasNext))
                            {
                                jobject itemObj = (*env)->CallObjectMethod(env, iterator, midNext);
                                if ((*env)->ExceptionCheck(env))
                                {
                                    (*env)->ExceptionDescribe(env);
                                    (*env)->ExceptionClear(env);
                                    break;
                                }
                                if (!itemObj)
                                    continue;

                                jclass itemCls = (*env)->GetObjectClass(env, itemObj);
                                jmethodID midGetChoice = (*env)->GetMethodID(
                                    env, itemCls, "getChoice",
                                    "()Lgov/usdot/cv/timencoder/SpeedLimit$SpeedLimitItem$Choice;");
                                jobject choiceObj = midGetChoice ? (*env)->CallObjectMethod(env, itemObj, midGetChoice) : NULL;

                                if ((*env)->ExceptionCheck(env))
                                {
                                    (*env)->ExceptionDescribe(env);
                                    (*env)->ExceptionClear(env);
                                    choiceObj = NULL;
                                }
                                const char *ename = NULL;
                                jstring jname = NULL;

                                if (choiceObj)
                                {
                                    jclass choiceCls = (*env)->GetObjectClass(env, choiceObj);
                                    jmethodID midName = (*env)->GetMethodID(env, choiceCls, "name", "()Ljava/lang/String;");
                                    if (midName)
                                    {
                                        jname = (jstring)(*env)->CallObjectMethod(env, choiceObj, midName);
                                        if ((*env)->ExceptionCheck(env))
                                        {
                                            (*env)->ExceptionDescribe(env);
                                            (*env)->ExceptionClear(env);
                                            jname = NULL;
                                        }
                                        if (jname)
                                            ename = (*env)->GetStringUTFChars(env, jname, 0);
                                    }
                                    (*env)->DeleteLocalRef(env, choiceCls);
                                    (*env)->DeleteLocalRef(env, choiceObj);
                                }
                                if (!ename)
                                {

                                    (*env)->DeleteLocalRef(env, itemCls);
                                    (*env)->DeleteLocalRef(env, itemObj);
                                    if (jname)
                                        (*env)->DeleteLocalRef(env, jname);

                                    continue;
                                }
                                if (strcmp(ename, "ITIS") == 0)
                                {
                                    // GenericSignageItem.getItis(): ITIScodes
                                    jmethodID midGetItis = (*env)->GetMethodID(
                                        env, itemCls, "getItis", "()Lgov/usdot/cv/timencoder/ITIScodes;");
                                    jobject itisObj = midGetItis ? (*env)->CallObjectMethod(env, itemObj, midGetItis) : NULL;
                                    if ((*env)->ExceptionCheck(env))
                                    {
                                        (*env)->ExceptionDescribe(env);
                                        (*env)->ExceptionClear(env);
                                        itisObj = NULL;
                                    }
                                    if (itisObj)
                                    {
                                        long code = get_long_from_java_number_like(env, itisObj);
                                        struct SpeedLimit__Member *node =
                                            (struct SpeedLimit__Member *)calloc(1, sizeof(*node));
                                        if (node)
                                        {
                                            node->item.present = SpeedLimit__Member__item_PR_itis;
                                            // cast to the generated typedef if present; otherwise long is fine
                                            node->item.choice.itis = (ITIS_ITIScodes_t)code;
                                            if (ASN_SEQUENCE_ADD(&tdf->content.choice.speedLimit.list, node) != 0)
                                            {
                                                fprintf(stderr, "ASN_SEQUENCE_ADD(SpeedLimit itis) failed\n");
                                                free(node);
                                            }
                                        }
                                        (*env)->DeleteLocalRef(env, itisObj);
                                    }
                                }
                                else if (strcmp(ename, "TEXT") == 0)
                                {
                                    // GenericSignageItem.getText(): ITISTextPhrase (Java)
                                    jmethodID midGetText = (*env)->GetMethodID(
                                        env, itemCls, "getText", "()Lgov/usdot/cv/timencoder/ITISTextPhrase;");
                                    jobject textObj = midGetText ? (*env)->CallObjectMethod(env, itemObj, midGetText) : NULL;
                                    if ((*env)->ExceptionCheck(env))
                                    {
                                        (*env)->ExceptionDescribe(env);
                                        (*env)->ExceptionClear(env);
                                        textObj = NULL;
                                    }
                                    if (textObj)
                                    {
                                        struct SpeedLimit__Member *node =
                                            (struct SpeedLimit__Member *)calloc(1, sizeof(*node));
                                        if (node)
                                        {
                                            node->item.present = SpeedLimit__Member__item_PR_text; // IA5String(1..63)
                                            if (!fill_octet_from_java_string_like(env, &node->item.choice.text, textObj, 63))
                                            {
                                                free(node);
                                            }
                                            else if (ASN_SEQUENCE_ADD(&tdf->content.choice.genericSign.list, node) != 0)
                                            {
                                                fprintf(stderr, "ASN_SEQUENCE_ADD(Speed Limit text) failed\n");
                                                free(node);
                                            }
                                        }
                                        (*env)->DeleteLocalRef(env, textObj);
                                    }
                                }

                                if (jname && ename)
                                    (*env)->ReleaseStringUTFChars(env, jname, ename);
                                if (jname)
                                    (*env)->DeleteLocalRef(env, jname);
                                (*env)->DeleteLocalRef(env, itemCls);
                                (*env)->DeleteLocalRef(env, itemObj);
                            }
                            (*env)->DeleteLocalRef(env, itCls);
                            (*env)->DeleteLocalRef(env, iterator);
                        }
                        else
                        {
                            fprintf(stderr, "Speed Limit : iterator() returned null\n");
                        }

                        (*env)->DeleteLocalRef(env, listCls);
                        (*env)->DeleteLocalRef(env, slCls);
                        (*env)->DeleteLocalRef(env, itemsList);
                        (*env)->DeleteLocalRef(env, slObj);
                        break;
                    }

                    case 4:
                    {
                        tdf->content.present = TravelerDataFrame__content_PR_exitService;

                        // Content.getSpeedLimit(): SpeedLimit
                        jmethodID midGetSL = (*env)->GetMethodID(
                            env, contentCls, "getExitService", "()Lgov/usdot/cv/timencoder/ExitService;");
                        jobject slObj = midGetSL ? (*env)->CallObjectMethod(env, content, midGetSL) : NULL;
                        if ((*env)->ExceptionCheck(env))
                        {
                            (*env)->ExceptionDescribe(env);
                            (*env)->ExceptionClear(env);
                            slObj = NULL;
                        }
                        if (!slObj)
                        {
                            printf("ExitService is null\n");
                            break;
                        }

                        jclass slCls = (*env)->GetObjectClass(env, slObj);
                        // SpeedLimit.getItems(): List<RegulatorySpeedLimit> (or similar)
                        jmethodID midGetItems = (*env)->GetMethodID(env, slCls, "getItems", "()Ljava/util/List;");
                        jobject itemsList = midGetItems ? (*env)->CallObjectMethod(env, slObj, midGetItems) : NULL;
                        if ((*env)->ExceptionCheck(env))
                        {
                            (*env)->ExceptionDescribe(env);
                            (*env)->ExceptionClear(env);
                            itemsList = NULL;
                        }
                        if (!itemsList)
                        {
                            printf("SpeedLimit.items is null\n");
                            (*env)->DeleteLocalRef(env, slCls);
                            (*env)->DeleteLocalRef(env, slObj);
                            break;
                        }

                        jclass listCls = (*env)->GetObjectClass(env, itemsList);
                        jmethodID midIterator = (*env)->GetMethodID(env, listCls, "iterator", "()Ljava/util/Iterator;");
                        jobject iterator = (*env)->CallObjectMethod(env, itemsList, midIterator);
                        if ((*env)->ExceptionCheck(env))
                        {
                            (*env)->ExceptionDescribe(env);
                            (*env)->ExceptionClear(env);
                            iterator = NULL;
                        }

                        if (iterator)
                        {
                            jclass itCls = (*env)->GetObjectClass(env, iterator);
                            jmethodID midHasNext = (*env)->GetMethodID(env, itCls, "hasNext", "()Z");
                            jmethodID midNext = (*env)->GetMethodID(env, itCls, "next", "()Ljava/lang/Object;");

                            while ((*env)->CallBooleanMethod(env, iterator, midHasNext))
                            {
                                jobject itemObj = (*env)->CallObjectMethod(env, iterator, midNext);
                                if ((*env)->ExceptionCheck(env))
                                {
                                    (*env)->ExceptionDescribe(env);
                                    (*env)->ExceptionClear(env);
                                    break;
                                }
                                if (!itemObj)
                                    continue;

                                jclass itemCls = (*env)->GetObjectClass(env, itemObj);
                                jmethodID midGetChoice = (*env)->GetMethodID(
                                    env, itemCls, "getChoice",
                                    "()Lgov/usdot/cv/timencoder/ExitService$ExitServiceItem$Choice;");
                                jobject choiceObj = midGetChoice ? (*env)->CallObjectMethod(env, itemObj, midGetChoice) : NULL;

                                if ((*env)->ExceptionCheck(env))
                                {
                                    (*env)->ExceptionDescribe(env);
                                    (*env)->ExceptionClear(env);
                                    choiceObj = NULL;
                                }
                                const char *ename = NULL;
                                jstring jname = NULL;

                                if (choiceObj)
                                {
                                    jclass choiceCls = (*env)->GetObjectClass(env, choiceObj);
                                    jmethodID midName = (*env)->GetMethodID(env, choiceCls, "name", "()Ljava/lang/String;");
                                    if (midName)
                                    {
                                        jname = (jstring)(*env)->CallObjectMethod(env, choiceObj, midName);
                                        if ((*env)->ExceptionCheck(env))
                                        {
                                            (*env)->ExceptionDescribe(env);
                                            (*env)->ExceptionClear(env);
                                            jname = NULL;
                                        }
                                        if (jname)
                                            ename = (*env)->GetStringUTFChars(env, jname, 0);
                                    }
                                    (*env)->DeleteLocalRef(env, choiceCls);
                                    (*env)->DeleteLocalRef(env, choiceObj);
                                }
                                if (!ename)
                                {

                                    (*env)->DeleteLocalRef(env, itemCls);
                                    (*env)->DeleteLocalRef(env, itemObj);
                                    if (jname)
                                        (*env)->DeleteLocalRef(env, jname);

                                    continue;
                                }
                                if (strcmp(ename, "ITIS") == 0)
                                {
                                    // GenericSignageItem.getItis(): ITIScodes
                                    jmethodID midGetItis = (*env)->GetMethodID(
                                        env, itemCls, "getItis", "()Lgov/usdot/cv/timencoder/ITIScodes;");
                                    jobject itisObj = midGetItis ? (*env)->CallObjectMethod(env, itemObj, midGetItis) : NULL;
                                    if ((*env)->ExceptionCheck(env))
                                    {
                                        (*env)->ExceptionDescribe(env);
                                        (*env)->ExceptionClear(env);
                                        itisObj = NULL;
                                    }
                                    if (itisObj)
                                    {
                                        long code = get_long_from_java_number_like(env, itisObj);
                                        struct ExitService__Member *node =
                                            (struct ExitService__Member *)calloc(1, sizeof(*node));
                                        if (node)
                                        {
                                            node->item.present = ExitService__Member__item_PR_itis;
                                            // cast to the generated typedef if present; otherwise long is fine
                                            node->item.choice.itis = (ITIS_ITIScodes_t)code;
                                            if (ASN_SEQUENCE_ADD(&tdf->content.choice.speedLimit.list, node) != 0)
                                            {
                                                fprintf(stderr, "ASN_SEQUENCE_ADD(Exit Service itis) failed\n");
                                                free(node);
                                            }
                                        }
                                        (*env)->DeleteLocalRef(env, itisObj);
                                    }
                                }
                                else if (strcmp(ename, "TEXT") == 0)
                                {
                                    // GenericSignageItem.getText(): ITISTextPhrase (Java)
                                    jmethodID midGetText = (*env)->GetMethodID(
                                        env, itemCls, "getText", "()Lgov/usdot/cv/timencoder/ITISTextPhrase;");
                                    jobject textObj = midGetText ? (*env)->CallObjectMethod(env, itemObj, midGetText) : NULL;
                                    if ((*env)->ExceptionCheck(env))
                                    {
                                        (*env)->ExceptionDescribe(env);
                                        (*env)->ExceptionClear(env);
                                        textObj = NULL;
                                    }
                                    if (textObj)
                                    {
                                        struct ExitService__Member *node =
                                            (struct ExitService__Member *)calloc(1, sizeof(*node));
                                        if (node)
                                        {
                                            node->item.present = ExitService__Member__item_PR_text; // IA5String(1..63)
                                            if (!fill_octet_from_java_string_like(env, &node->item.choice.text, textObj, 63))
                                            {
                                                free(node);
                                            }
                                            else if (ASN_SEQUENCE_ADD(&tdf->content.choice.genericSign.list, node) != 0)
                                            {
                                                fprintf(stderr, "ASN_SEQUENCE_ADD(EXIT SERVICE text) failed\n");
                                                free(node);
                                            }
                                        }
                                        (*env)->DeleteLocalRef(env, textObj);
                                    }
                                }

                                if (jname && ename)
                                    (*env)->ReleaseStringUTFChars(env, jname, ename);
                                if (jname)
                                    (*env)->DeleteLocalRef(env, jname);
                                (*env)->DeleteLocalRef(env, itemCls);
                                (*env)->DeleteLocalRef(env, itemObj);
                            }
                            (*env)->DeleteLocalRef(env, itCls);
                            (*env)->DeleteLocalRef(env, iterator);
                        }
                        else
                        {
                            fprintf(stderr, "Speed Limit : iterator() returned null\n");
                        }

                        (*env)->DeleteLocalRef(env, listCls);
                        (*env)->DeleteLocalRef(env, slCls);
                        (*env)->DeleteLocalRef(env, itemsList);
                        (*env)->DeleteLocalRef(env, slObj);
                        break;
                    }

                    default:
                        tdf->content.present = TravelerDataFrame__content_PR_NOTHING;
                        break;
                    }

                        if (nameStr)
                        {
                            const char *nameUtf = (*env)->GetStringUTFChars(env, nameStr, NULL);
                            if (nameUtf)
                                (*env)->ReleaseStringUTFChars(env, nameStr, nameUtf);
                            (*env)->DeleteLocalRef(env, nameStr);
                        }
                        else
                        {
                            printf("Content Choice = <string null> (ordinal=%d)\n", (int)ordinal);
                        }

                        (*env)->DeleteLocalRef(env, enumCls);
                        (*env)->DeleteLocalRef(env, contentChoiceObj);
                    }
                }
            }
            else
            {
                printf("Content Choice = <null>\n");
            }
            (*env)->DeleteLocalRef(env, contentCls);
        }

        // ---- contentNew (Part III: FrictionInformation) ----
        mid_getContentNew = (*env)->GetMethodID(env, frameCls, "getContentNew", "()Lgov/usdot/cv/timencoder/TravelerDataFrameNewPartIIIContent;");
        jobject contentNew = mid_getContentNew ? (*env)->CallObjectMethod(env, frame, mid_getContentNew) : NULL;
        if (contentNew)
        {

            jclass contentNewCls = (*env)->GetObjectClass(env, contentNew);
            jmethodID midGetFriction = (*env)->GetMethodID(
                env, contentNewCls, "getFrictionInformation",
                "()Lgov/usdot/cv/timencoder/FrictionInformation;");
            jobject frictionObj = midGetFriction ? (*env)->CallObjectMethod(env, contentNew, midGetFriction) : NULL;
            if (frictionObj)
            {
                jclass frictionCls = (*env)->GetObjectClass(env, frictionObj);
                if (!tdf->contentNew)
                {
                    tdf->contentNew = (TravelerDataFrameNewPartIIIContent_t *)calloc(1, sizeof(*tdf->contentNew));
                    if (!tdf->contentNew)
                    {
                        fprintf(stderr, "OOM: contentNew\n");
                    }
                }
                //tdf->contentNew->present = TravelerDataFrameNewPartIIIContent_PR_frictionInfo;

                FrictionInformation_t *fip = (FrictionInformation_t *)calloc(1, sizeof(FrictionInformation_t));

                // roadSurfaceDescription
                jmethodID midGetDesc = (*env)->GetMethodID(
                    env, frictionCls, "getRoadSurfaceDescription",
                    "()Lgov/usdot/cv/timencoder/DescriptionOfRoadSurface;");
                jobject descObj = midGetDesc ? (*env)->CallObjectMethod(env, frictionObj, midGetDesc) : NULL;
                if (descObj)
                {
                    jclass descCls = (*env)->GetObjectClass(env, descObj);
                    jmethodID midGetChoice = (*env)->GetMethodID(
                        env, descCls, "getChoice",
                        "()Lgov/usdot/cv/timencoder/DescriptionOfRoadSurface$Choice;");
                    jobject choiceObj = midGetChoice ? (*env)->CallObjectMethod(env, descObj, midGetChoice) : NULL;
                    if (choiceObj)
                    {
                        jclass choiceCls = (*env)->GetObjectClass(env, choiceObj);
                        jmethodID midChoiceName = (*env)->GetMethodID(env, choiceCls, "name", "()Ljava/lang/String;");
                        jmethodID midChoiceOrdinal = (*env)->GetMethodID(env, choiceCls, "ordinal", "()I");
                        jstring choiceNameJ = (jstring)(*env)->CallObjectMethod(env, choiceObj, midChoiceName);
                        jint choiceOrdinal = (*env)->CallIntMethod(env, choiceObj, midChoiceOrdinal);
                        if (choiceNameJ)
                        {
                            const char *choiceName = (*env)->GetStringUTFChars(env, choiceNameJ, 0);
                            if (choiceName)
                                (*env)->ReleaseStringUTFChars(env, choiceNameJ, choiceName);
                            (*env)->DeleteLocalRef(env, choiceNameJ);
                        }
                        else
                        {
                            printf("Content New: RoadSurfaceDescription.Choice = <null> (ordinal=%d)\n", (int)choiceOrdinal);
                        }

                        if (choiceOrdinal == 0)
                        {
                            jmethodID midGetPC = (*env)->GetMethodID(
                                env, descCls, "getPortlandCement",
                                "()Lgov/usdot/cv/timencoder/PortlandCement;");
                            jobject pcObj = midGetPC ? (*env)->CallObjectMethod(env, descObj, midGetPC) : NULL;
                            fip->roadSurfaceDescription.present = DescriptionOfRoadSurface_PR_portlandCement;
                            if (pcObj)
                            {
                                jclass pcCls = (*env)->GetObjectClass(env, pcObj);
                                jmethodID midGetType = (*env)->GetMethodID(
                                    env, pcCls, "getType",
                                    "()Lgov/usdot/cv/timencoder/PortlandCementType;");
                                jobject typeObj = midGetType ? (*env)->CallObjectMethod(env, pcObj, midGetType) : NULL;
                                if (typeObj)
                                {
                                    jclass typeCls = (*env)->GetObjectClass(env, typeObj);
                                    jmethodID midIntVal = (*env)->GetMethodID(env, typeCls, "intValue", "()I");
                                    jint enumVal = midIntVal ? (*env)->CallIntMethod(env, typeObj, midIntVal) : -1;

                                    if (enumVal >= 0)
                                    {

                                        // Assign to ASN.1 field
                                        fip->roadSurfaceDescription.choice.portlandCement.type = enumVal;
                                    }
                                    (*env)->DeleteLocalRef(env, typeCls);
                                    (*env)->DeleteLocalRef(env, typeObj);
                                }
                                (*env)->DeleteLocalRef(env, pcCls);
                                (*env)->DeleteLocalRef(env, pcObj);
                            }
                        }

                        else if (choiceOrdinal == 1)
                        {
                            jmethodID midGetAS = (*env)->GetMethodID(
                                env, descCls, "getAsphaltOrTar",
                                "()Lgov/usdot/cv/timencoder/AsphaltOrTar;");
                            jobject asObj = midGetAS ? (*env)->CallObjectMethod(env, descObj, midGetAS) : NULL;
                            fip->roadSurfaceDescription.present = DescriptionOfRoadSurface_PR_asphaltOrTar;
                            if (asObj)
                            {
                                jclass asCls = (*env)->GetObjectClass(env, asObj);
                                jmethodID midGetType = (*env)->GetMethodID(
                                    env, asCls, "getType",
                                    "()Lgov/usdot/cv/timencoder/AsphaltOrTarType;");
                                jobject typeObj = midGetType ? (*env)->CallObjectMethod(env, asObj, midGetType) : NULL;
                                if (typeObj)
                                {
                                    jclass typeCls = (*env)->GetObjectClass(env, typeObj);
                                    jmethodID midIntVal = (*env)->GetMethodID(env, typeCls, "intValue", "()I");
                                    jint enumVal = midIntVal ? (*env)->CallIntMethod(env, typeObj, midIntVal) : -1;

                                    if (enumVal >= 0)
                                    {

                                        // Assign to ASN.1 field
                                        fip->roadSurfaceDescription.choice.asphaltOrTar.type = enumVal;
                                    }
                                    (*env)->DeleteLocalRef(env, typeCls);
                                    (*env)->DeleteLocalRef(env, typeObj);
                                }
                                (*env)->DeleteLocalRef(env, asCls);
                                (*env)->DeleteLocalRef(env, asObj);
                            }
                        }
                        else if (choiceOrdinal == 2)
                        {

                            jmethodID midGetGravel = (*env)->GetMethodID(
                                env, descCls, "getGravel",
                                "()Lgov/usdot/cv/timencoder/Gravel;");
                            jobject gravelObj = midGetGravel ? (*env)->CallObjectMethod(env, gravelObj, midGetGravel) : NULL;
                            fip->roadSurfaceDescription.present = DescriptionOfRoadSurface_PR_gravel;
                            if (gravelObj)
                            {
                                jclass gravelCls = (*env)->GetObjectClass(env, gravelObj);
                                jmethodID midGetType = (*env)->GetMethodID(
                                    env, gravelCls, "getType",
                                    "()Lgov/usdot/cv/timencoder/GravelType;");
                                jobject typeObj = midGetType ? (*env)->CallObjectMethod(env, gravelObj, midGetType) : NULL;
                                if (typeObj)
                                {
                                    jclass typeCls = (*env)->GetObjectClass(env, typeObj);
                                    jmethodID midIntVal = (*env)->GetMethodID(env, typeCls, "intValue", "()I");
                                    jint enumVal = midIntVal ? (*env)->CallIntMethod(env, typeObj, midIntVal) : -1;

                                    if (enumVal >= 0)
                                    {

                                        // Assign to ASN.1 field
                                        fip->roadSurfaceDescription.choice.gravel.type = enumVal;
                                    }
                                    (*env)->DeleteLocalRef(env, typeCls);
                                    (*env)->DeleteLocalRef(env, typeObj);
                                }
                                (*env)->DeleteLocalRef(env, gravelCls);
                                (*env)->DeleteLocalRef(env, gravelObj);
                            }
                        }
                        else if (choiceOrdinal == 3)
                        {

                            jmethodID midGetGrass = (*env)->GetMethodID(
                                env, descCls, "getGrass",
                                "()Lgov/usdot/cv/timencoder/Grass;");
                            jobject grassObj = midGetGrass ? (*env)->CallObjectMethod(env, descObj, midGetGrass) : NULL;
                            fip->roadSurfaceDescription.present = DescriptionOfRoadSurface_PR_grass;
                            if (grassObj)
                            {
                                jclass grassCls = (*env)->GetObjectClass(env, grassObj);
                                jmethodID midGetType = (*env)->GetMethodID(
                                    env, grassCls, "getType",
                                    "()Lgov/usdot/cv/timencoder/GrassType;");
                                jobject typeObj = midGetType ? (*env)->CallObjectMethod(env, grassObj, midGetType) : NULL;
                                if (typeObj)
                                {
                                    jclass typeCls = (*env)->GetObjectClass(env, typeObj);
                                    jmethodID midIntVal = (*env)->GetMethodID(env, typeCls, "intValue", "()I");
                                    jint enumVal = midIntVal ? (*env)->CallIntMethod(env, typeObj, midIntVal) : -1;

                                    if (enumVal >= 0)
                                    {
                                        // Assign to ASN.1 field
                                        fip->roadSurfaceDescription.choice.grass.type = enumVal;
                                    }
                                    (*env)->DeleteLocalRef(env, typeCls);
                                    (*env)->DeleteLocalRef(env, typeObj);
                                }
                                (*env)->DeleteLocalRef(env, grassCls);
                                (*env)->DeleteLocalRef(env, grassObj);
                            }
                        }

                        else if (choiceOrdinal == 4)
                        {
                            jmethodID midgetCinders = (*env)->GetMethodID(
                                env, descCls, "getCinders",
                                "()Lgov/usdot/cv/timencoder/Cinders;");
                            jobject cinderObj = midgetCinders ? (*env)->CallObjectMethod(env, descObj, midgetCinders) : NULL;
                            fip->roadSurfaceDescription.present = DescriptionOfRoadSurface_PR_cinders;
                            if (cinderObj)
                            {
                                jclass cinderCls = (*env)->GetObjectClass(env, cinderObj);
                                jmethodID midGetType = (*env)->GetMethodID(
                                    env, cinderCls, "getType",
                                    "()Lgov/usdot/cv/timencoder/CindersType;");
                                jobject typeObj = midGetType ? (*env)->CallObjectMethod(env, cinderObj, midGetType) : NULL;
                                if (typeObj)
                                {
                                    jclass typeCls = (*env)->GetObjectClass(env, typeObj);
                                    jmethodID midIntVal = (*env)->GetMethodID(env, typeCls, "intValue", "()I");
                                    jint enumVal = midIntVal ? (*env)->CallIntMethod(env, typeObj, midIntVal) : -1;

                                    if (enumVal >= 0)
                                    {

                                        // Assign to ASN.1 field
                                        fip->roadSurfaceDescription.choice.cinders.type = enumVal;
                                    }
                                    (*env)->DeleteLocalRef(env, typeCls);
                                    (*env)->DeleteLocalRef(env, typeObj);
                                }
                                (*env)->DeleteLocalRef(env, cinderCls);
                                (*env)->DeleteLocalRef(env, cinderObj);
                            }
                        }
                        else if (choiceOrdinal == 5)
                        {
                            jmethodID midgetRock = (*env)->GetMethodID(
                                env, descCls, "getRock",
                                "()Lgov/usdot/cv/timencoder/Rock;");
                            jobject rockObj = midgetRock ? (*env)->CallObjectMethod(env, descObj, midgetRock) : NULL;
                            fip->roadSurfaceDescription.present = DescriptionOfRoadSurface_PR_rock;
                            if (rockObj)
                            {
                                jclass rockCls = (*env)->GetObjectClass(env, rockObj);
                                jmethodID midGetType = (*env)->GetMethodID(
                                    env, rockCls, "getType",
                                    "()Lgov/usdot/cv/timencoder/RockTyoe;");
                                jobject typeObj = midGetType ? (*env)->CallObjectMethod(env, rockObj, midGetType) : NULL;
                                if (typeObj)
                                {
                                    jclass typeCls = (*env)->GetObjectClass(env, typeObj);
                                    jmethodID midIntVal = (*env)->GetMethodID(env, typeCls, "intValue", "()I");
                                    jint enumVal = midIntVal ? (*env)->CallIntMethod(env, typeObj, midIntVal) : -1;

                                    if (enumVal >= 0)
                                    {

                                        // Assign to ASN.1 field
                                        fip->roadSurfaceDescription.choice.rock.type = enumVal;
                                    }
                                    (*env)->DeleteLocalRef(env, typeCls);
                                    (*env)->DeleteLocalRef(env, typeObj);
                                }
                                (*env)->DeleteLocalRef(env, rockCls);
                                (*env)->DeleteLocalRef(env, rockObj);
                            }
                        }
                        else if (choiceOrdinal == 6)
                        {
                            jmethodID midgetIce = (*env)->GetMethodID(
                                env, descCls, "getIce",
                                "()Lgov/usdot/cv/timencoder/Ice;");
                            jobject iceObj = midgetIce ? (*env)->CallObjectMethod(env, descObj, midgetIce) : NULL;
                            fip->roadSurfaceDescription.present = DescriptionOfRoadSurface_PR_ice;
                            if (iceObj)
                            {
                                jclass iceCls = (*env)->GetObjectClass(env, iceObj);
                                jmethodID midGetType = (*env)->GetMethodID(
                                    env, iceCls, "getType",
                                    "()Lgov/usdot/cv/timencoder/IceType;");
                                jobject typeObj = midGetType ? (*env)->CallObjectMethod(env, iceObj, midGetType) : NULL;
                                if (typeObj)
                                {
                                    jclass typeCls = (*env)->GetObjectClass(env, typeObj);
                                    jmethodID midIntVal = (*env)->GetMethodID(env, typeCls, "intValue", "()I");
                                    jint enumVal = midIntVal ? (*env)->CallIntMethod(env, typeObj, midIntVal) : -1;

                                    if (enumVal >= 0)
                                    {

                                        // Assign to ASN.1 field
                                        fip->roadSurfaceDescription.choice.ice.type = enumVal;
                                    }
                                    (*env)->DeleteLocalRef(env, typeCls);
                                    (*env)->DeleteLocalRef(env, typeObj);
                                }
                                (*env)->DeleteLocalRef(env, iceCls);
                                (*env)->DeleteLocalRef(env, iceObj);
                            }
                        }
                        else if (choiceOrdinal == 7)
                        {
                            jmethodID midgetSnow = (*env)->GetMethodID(
                                env, descCls, "getSnow",
                                "()Lgov/usdot/cv/timencoder/Snow;");
                            jobject snowObj = midgetSnow ? (*env)->CallObjectMethod(env, descObj, midgetSnow) : NULL;
                            fip->roadSurfaceDescription.present = DescriptionOfRoadSurface_PR_snow;
                            if (snowObj)
                            {
                                jclass snowCls = (*env)->GetObjectClass(env, snowObj);
                                jmethodID midGetType = (*env)->GetMethodID(
                                    env, snowCls, "getType",
                                    "()Lgov/usdot/cv/timencoder/SnowType;");
                                jobject typeObj = midGetType ? (*env)->CallObjectMethod(env, snowObj, midGetType) : NULL;
                                if (typeObj)
                                {
                                    // Call SnowType.intValue()
                                    jclass typeCls = (*env)->GetObjectClass(env, typeObj);
                                    jmethodID midIntVal = (*env)->GetMethodID(env, typeCls, "intValue", "()I");
                                    jint enumVal = midIntVal ? (*env)->CallIntMethod(env, typeObj, midIntVal) : -1;

                                    if (enumVal >= 0)
                                    {
                                        // Assign to ASN.1 field
                                        fip->roadSurfaceDescription.choice.snow.type = enumVal;
                                    }

                                    (*env)->DeleteLocalRef(env, typeCls);
                                    (*env)->DeleteLocalRef(env, typeObj);
                                }
                                (*env)->DeleteLocalRef(env, snowCls);
                                (*env)->DeleteLocalRef(env, snowObj);
                            }
                        }

                        else
                        {
                            printf("Content New: RoadSurfaceDescription: unhandled choice ordinal %d\n", (int)choiceOrdinal);
                        }

                        (*env)->DeleteLocalRef(env, choiceCls);
                        (*env)->DeleteLocalRef(env, choiceObj);
                    }

                    else
                    {
                        printf("Content New: Choice is null\n");
                    }
                    (*env)->DeleteLocalRef(env, descCls);
                    (*env)->DeleteLocalRef(env, descObj);
                }
                else
                {
                    printf("PartIII: roadSurfaceDescription is null\n");
                }

                // Optional: dryOrWet
                jmethodID midGetDry = (*env)->GetMethodID(
                    env, frictionCls, "getDryOrWet",
                    "()Lgov/usdot/cv/timencoder/RoadSurfaceCondition;");
                jobject dryObj = midGetDry ? (*env)->CallObjectMethod(env, frictionObj, midGetDry) : NULL;
                if (dryObj)
                {
                    jclass dryCls = (*env)->GetObjectClass(env, dryObj);
                    jmethodID midIntVal = (*env)->GetMethodID(env, dryCls, "intValue", "()I");
                    jint intVal = (midIntVal) ? (*env)->CallIntMethod(env, dryObj, midIntVal) : -1;

                    if ((*env)->ExceptionCheck(env))
                    {
                        (*env)->ExceptionDescribe(env);
                        (*env)->ExceptionClear(env);
                        intVal = -1;
                    }

                    // Only allocate if value is 0 (DRY) or 1 (WET)
                    if (intVal == 0 || intVal == 1)
                    {
                        fip->dryOrWet = (RoadSurfaceCondition_t *)calloc(1, sizeof(*fip->dryOrWet));
                        if (fip->dryOrWet)
                        {
                            *fip->dryOrWet = (long)intVal;
                        }
                        else
                        {
                            fprintf(stderr, "OOM: dryOrWet\n");
                        }
                    }
                    else
                    {
                        printf("Content New: dryOrWet skipped (invalid value %d)\n", intVal);
                    }
                }
                else
                {

                    fip->dryOrWet = NULL;
                }

                // Optional: roadRoughness
                jmethodID midGetRough = (*env)->GetMethodID(
                    env, frictionCls, "getRoadRoughness",
                    "()Lgov/usdot/cv/timencoder/RoadRoughness;");
                jobject roughObj = midGetRough ? (*env)->CallObjectMethod(env, frictionObj, midGetRough) : NULL;
                if (roughObj)
                {
                    jclass roughCls = (*env)->GetObjectClass(env, roughObj);

                    // Allocate RoadRoughness_t
                    fip->roadRoughness = (RoadRoughness_t *)calloc(1, sizeof(RoadRoughness_t));
                    if (!fip->roadRoughness)
                    {
                        fprintf(stderr, "calloc failed for RoadRoughness\n");
                    }
                    else
                    {
                        // meanVerticalVariation
                        jmethodID midGetMeanVert = (*env)->GetMethodID(env, roughCls, "getMeanVerticalVariation", "()Lgov/usdot/cv/timencoder/CommonMeanVariation;");
                        jobject meanVertObj = midGetMeanVert ? (*env)->CallObjectMethod(env, roughObj, midGetMeanVert) : NULL;
                        if (meanVertObj)
                        {
                            long meanVertVal = get_long_from_java_number_like(env, meanVertObj);
                            fip->roadRoughness->meanVerticalVariation = meanVertVal;
                            (*env)->DeleteLocalRef(env, meanVertObj);
                        }

                        // verticalVariationStdDev
                        jmethodID midGetVertStdDev = (*env)->GetMethodID(env, roughCls, "getVerticalVariationStdDev", "()Lgov/usdot/cv/timencoder/VariationStdDev;");
                        jobject vertStdDevObj = midGetVertStdDev ? (*env)->CallObjectMethod(env, roughObj, midGetVertStdDev) : NULL;
                        if (vertStdDevObj)
                        {
                            long vertStdDevVal = get_long_from_java_number_like(env, vertStdDevObj);
                            fip->roadRoughness->verticalVariationStdDev = (VariationStdDev_t *)calloc(1, sizeof(VariationStdDev_t));
                            if (fip->roadRoughness->verticalVariationStdDev)
                            {
                                *fip->roadRoughness->verticalVariationStdDev = vertStdDevVal;
                            }
                            (*env)->DeleteLocalRef(env, vertStdDevObj);
                        }
                        else
                        {
                            fip->roadRoughness->verticalVariationStdDev = NULL;
                        }

                        // meanHorizontalVariation
                        jmethodID midGetMeanHoriz = (*env)->GetMethodID(env, roughCls, "getMeanHorizontalVariation", "()Lgov/usdot/cv/timencoder/CommonMeanVariation;");
                        jobject meanHorizObj = midGetMeanHoriz ? (*env)->CallObjectMethod(env, roughObj, midGetMeanHoriz) : NULL;
                        if (meanHorizObj)
                        {
                            long meanHorizVal = get_long_from_java_number_like(env, meanHorizObj);
                            fip->roadRoughness->meanHorizontalVariation = (CommonMeanVariation_t *)calloc(1, sizeof(CommonMeanVariation_t));
                            if (fip->roadRoughness->meanHorizontalVariation)
                            {
                                *fip->roadRoughness->meanHorizontalVariation = meanHorizVal;
                            }
                            (*env)->DeleteLocalRef(env, meanHorizObj);
                        }
                        else
                        {
                            fip->roadRoughness->meanHorizontalVariation = NULL;
                        }

                        // horizontalVariationStdDev
                        jmethodID midGetHorizStdDev = (*env)->GetMethodID(env, roughCls, "getHorizontalVariationStdDev", "()Lgov/usdot/cv/timencoder/VariationStdDev;");
                        jobject horizStdDevObj = midGetHorizStdDev ? (*env)->CallObjectMethod(env, roughObj, midGetHorizStdDev) : NULL;
                        if (horizStdDevObj)
                        {
                            long horizStdDevVal = get_long_from_java_number_like(env, horizStdDevObj);
                            fip->roadRoughness->horizontalVariationStdDev = (VariationStdDev_t *)calloc(1, sizeof(VariationStdDev_t));
                            if (fip->roadRoughness->horizontalVariationStdDev)
                            {
                                *fip->roadRoughness->horizontalVariationStdDev = horizStdDevVal;
                            }
                            (*env)->DeleteLocalRef(env, horizStdDevObj);
                        }
                        else
                        {
                            fip->roadRoughness->horizontalVariationStdDev = NULL;
                        }
                    }

                    (*env)->DeleteLocalRef(env, roughCls);
                    (*env)->DeleteLocalRef(env, roughObj);
                }
                else
                {
                    printf("Content New: roadRoughness = <absent>\n");
                }

                TravelerDataFrameNewPartIIIContentItem_t *item = (TravelerDataFrameNewPartIIIContentItem_t *)calloc(1, sizeof(TravelerDataFrameNewPartIIIContentItem_t));
                if (item) {
                    item->present = TravelerDataFrameNewPartIIIContentItem_PR_frictionInformation;
                    item->choice.frictionInformation = *fip;
                    ASN_SEQUENCE_ADD(&tdf->contentNew->list, item);
                } else {
                    fprintf(stderr, "calloc failed for TravelerDataFrameNewPartIIIContentItem\n");
                }
                
                (*env)->DeleteLocalRef(env, contentNewCls);
        }
        else
        {
            printf("contentNew is null\n");
        }
        ASN_SEQUENCE_ADD(&tim.dataFrames, tdf);

        if ((*env)->ExceptionCheck(env))
        {
            (*env)->ExceptionDescribe(env);
            (*env)->ExceptionClear(env);
        }

        // Cleaning up objects
        if (frameType)
            (*env)->DeleteLocalRef(env, frameType);
        if (msgId)
            (*env)->DeleteLocalRef(env, msgId);
        if (startTime)
            (*env)->DeleteLocalRef(env, startTime);
        if (durationTime)
            (*env)->DeleteLocalRef(env, durationTime);
        if (priority)
            (*env)->DeleteLocalRef(env, priority);
        if (regionsListObj)
            (*env)->DeleteLocalRef(env, regionsListObj);
        if (content)
            (*env)->DeleteLocalRef(env, content);
        if (contentNew)
            (*env)->DeleteLocalRef(env, contentNew);
        if (doNotUse1)
            (*env)->DeleteLocalRef(env, doNotUse1);
        if (doNotUse2)
            (*env)->DeleteLocalRef(env, doNotUse2);
        if (doNotUse3)
            (*env)->DeleteLocalRef(env, doNotUse3);
        if (doNotUse4)
            (*env)->DeleteLocalRef(env, doNotUse4);
        (*env)->DeleteLocalRef(env, frame);
    }

    // Cleanup outer locals
    (*env)->DeleteLocalRef(env, listCls);
    (*env)->DeleteLocalRef(env, listObj);
    (*env)->DeleteLocalRef(env, tdfCls);
    (*env)->DeleteLocalRef(env, tdfListObj);
    (*env)->DeleteLocalRef(env, timClass);
    if (frameCls)
        (*env)->DeleteLocalRef(env, frameCls);

    uint8_t buffer[10240]; // initlizing buffer to store encoded message
    asn_enc_rval_t ec;
    MessageFrame_t *message = (MessageFrame_t *)calloc(1, sizeof(*message));
    if (!message)
    {
        fprintf(stderr, "message object initilization Failed\n");
    }

    message->messageId = 31; // TIM
    message->value.present = MessageFrame__value_PR_TravelerInformation;
    message->value.choice.TravelerInformation = tim;
    asn_fprint(stdout, &asn_DEF_MessageFrame, message);

    ec = uper_encode_to_buffer(
        &asn_DEF_MessageFrame,
        NULL,
        message,
        buffer,
        sizeof(buffer));

    if (ec.encoded != -1)
    {
        printf("UPER encoded %zd bits\n", ec.encoded);
        size_t nbytes = (ec.encoded + 7) / 8;

        // Print hex output
        size_t hex_len = nbytes * 3; // "AA " per byte
        char *hex = (char *)malloc(hex_len + 1);
        if (hex)
        {
            char *p = hex;
            for (size_t i = 0; i < nbytes; ++i)
                p += sprintf(p, (i + 1 == nbytes) ? "%02X" : "%02X ", ((uint8_t *)buffer)[i]);
            *p = '\0';
            printf("UPER HEX: %s\n", hex);
            free(hex);
        }

        // Decoding back to check message object Content
        MessageFrame_t *decoded = NULL;
        asn_dec_rval_t dr = uper_decode_complete(
            /*opt_codec_ctx=*/NULL,
            &asn_DEF_MessageFrame,
            (void **)&decoded,
            buffer,
            nbytes);

        if (dr.code == RC_OK && decoded)
        {
            printf("UPER decode OK, consumed %zu bits (~%zu bytes)\n",
                   dr.consumed, (dr.consumed + 7) / 8);

            // Printing the decoded structure, should match the original
            asn_fprint(stdout, &asn_DEF_MessageFrame, decoded);
        }
        else
        {
            printf("UPER decode FAILED (code=%d), consumed %zu bits at about byte %zu\n",
                   dr.code, dr.consumed, dr.consumed / 8);
        }

        // Returning actual byte array to Java
        jbyteArray result = (*env)->NewByteArray(env, nbytes);
        if (result != NULL)
        {
            (*env)->SetByteArrayRegion(env, result, 0, nbytes, (jbyte *)buffer);
        }

        // free decoded struct
        ASN_STRUCT_FREE(asn_DEF_MessageFrame, decoded);

        return result;
    }
    else
    {
        printf("UPER encode failed\n");
        return NULL;
    }
}
