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
    printf("msgCnt = %d\n", j_msgCnt);

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

    jint n = (*env)->CallIntMethod(env, listObj, midSize);
    if ((*env)->ExceptionCheck(env))
    {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
    }

    // ---- Cache TravelerDataFrame getters (using first element to get class) ----
    jclass frameCls = NULL;
    jmethodID mid_getDoNotUse1 = NULL, mid_getDoNotUse2 = NULL, mid_getDoNotUse3 = NULL, mid_getDoNotUse4 = NULL;
    jmethodID mid_getFrameType = NULL, mid_getMsgId = NULL, mid_getStartTime = NULL, mid_getDurationTime = NULL;
    jmethodID mid_getPriority = NULL, mid_getRegions = NULL, mid_getContent = NULL, mid_getContentNew = NULL;
    // If frame exist initilize the methods to get the method IDs
    if (n > 0)
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
    for (jint i = 0; i < n; i++)
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

        printf("\n--- Frame %d ---\n", i);

        // doNotUse1
        mid_getDoNotUse1 = (*env)->GetMethodID(env, frameCls, "getDoNotUse1", "()Lgov/usdot/cv/timencoder/SSPindex;");
        jobject doNotUse1 = mid_getDoNotUse1 ? (*env)->CallObjectMethod(env, frame, mid_getDoNotUse1) : NULL;
        if (doNotUse1)
        {
            jclass doNotUse1Cls = (*env)->GetObjectClass(env, doNotUse1);
            jmethodID midGetIndex = (*env)->GetMethodID(env, doNotUse1Cls, "get", "()I");
            jint index = (*env)->CallIntMethod(env, doNotUse1, midGetIndex);
            tdf->doNotUse1 = (long)index;
            printf("doNotUse1 SSP Index = %d\n", index);
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
            printf("Traveler Info Type = %d\n", val);
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

                    rs->position.lat = (Common_Latitude_t)llrint(lat * 1e7);
                    rs->position.Long = (Common_Latitude_t)llrint(lon * 1e7);

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

                (*env)->DeleteLocalRef(env, msgIdCls);
            }
            else
            {
                tdf->msgId.present = TravelerDataFrame__msgId_PR_NOTHING;
            }
        }

        // startTime (MinuteOfTheYear)
        mid_getStartTime = (*env)->GetMethodID(env, frameCls, "getStartTime", "()Lgov/usdot/cv/timencoder/MinuteOfTheYear;");
        jobject startTime = mid_getStartTime ? (*env)->CallObjectMethod(env, frame, mid_getStartTime) : NULL;
        if (startTime)
        {

            jclass startTimeCls = (*env)->GetObjectClass(env, startTime);
            jmethodID midGetMinute = (*env)->GetMethodID(env, startTimeCls, "getValue", "()I");
            jint start = (*env)->CallIntMethod(env, startTime, midGetMinute);
            tdf->startTime = (long)start;
            printf("Start Time (minute of year) = %d\n", start);
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
            printf("Duration (minutes) = %d\n", dur);
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
            printf("Priority = %d\n", pri);
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
            printf("doNotUse2 SSP Index = %d\n", index2);
            (*env)->DeleteLocalRef(env, doNotUse2Cls);
        }

        // regions (GeographicalPath[])
        mid_getRegions = (*env)->GetMethodID(env, frameCls, "getRegions", "()[Lgov/usdot/cv/timencoder/GeographicalPath;");
        jobject regionsArr = mid_getRegions ? (*env)->CallObjectMethod(env, frame, mid_getRegions) : NULL;
        if (regionsArr)
        {

            jsize arrSize = (*env)->GetArrayLength(env, (jarray)regionsArr);
            printf("Regions array size = %d\n", (int)arrSize);

            for (jsize j = 0; j < arrSize; j++)
            {

                // TODO: All the fields of GeographicalPath need to be filled here.As it's optional field is left blank for this story
                GeographicalPath_t *gp = (GeographicalPath_t *)calloc(1, sizeof(*gp));
                if (!gp)
                {
                    fprintf(stderr, "calloc GeographicalPath failed\n");
                }
                else if (ASN_SEQUENCE_ADD(&tdf->regions.list, gp) != 0)
                {
                    fprintf(stderr, "ASN_SEQUENCE_ADD(GeographicalPath) failed at %d\n", (int)j);
                    free(gp);
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
            printf("doNotUse3 SSP Index = %d\n", index3);
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
            printf("doNotUse4 SSP Index = %d\n", index4);
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

                                printf("WorkZone item index %d\n", idx);

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
                                        //   printf("  WZ[%-3d] ITIS = %ld\n", (int)k, code);

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
                            printf("Content Choice = %s (ordinal=%d)\n", nameUtf ? nameUtf : "<null>", (int)ordinal);
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
                tdf->contentNew->present = TravelerDataFrameNewPartIIIContent_PR_frictionInfo;

                FrictionInformation_t *fip = &tdf->contentNew->choice.frictionInfo;

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
                            printf("Content New: RoadSurfaceDescription.Choice = %s (ordinal=%d)\n",
                                   choiceName ? choiceName : "<null>", (int)choiceOrdinal);
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
                                        printf("Content New: Snow.type intValue=%d\n", enumVal);
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
                            printf("Content New: dryOrWet = %d\n", intVal);
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
                    // TODO: get Road Roughness information
                }
                else
                {
                    printf("Content New: roadRoughness = <absent>\n");
                }

                (*env)->DeleteLocalRef(env, frictionCls);
                (*env)->DeleteLocalRef(env, frictionObj);
            }
            else
            {
                printf("Content New: FrictionInformation is null\n");
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
        if (regionsArr)
            (*env)->DeleteLocalRef(env, regionsArr);
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
