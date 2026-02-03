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
#include "gov_usdot_cv_asn1decoder_Decoder.h"
#include "MessageFrame.h"
#include "TravelerInformation.h" 
#include "BasicSafetyMessage.h"
#include "PersonalSafetyMessage.h"
#include "SPAT.h"
#include "MapData.h"
#include <stdint.h>
#include <string.h>
#include <stdlib.h>

/*Takes ASN Descriptor for message and decodes it into msgTypeStrOut*/
static jboolean decode_message_only(
    const asn_TYPE_descriptor_t *asn_def,
    const void *buf,
    int len,
    const char *successTypeName,
    const char **msgTypeStrOut,
    const char **decodedStrOut)
{
    void *msg = NULL;
    *msgTypeStrOut = "UnknownMessageType";
    *decodedStrOut = "Decoding failed.";

    asn_dec_rval_t rval = uper_decode(0, asn_def, (void **)&msg, buf, len, 0, 0);
   

    if (rval.code == RC_OK && msg != NULL)
    {
        char outputBuffer[65536];
        FILE *stream = fmemopen(outputBuffer, sizeof(outputBuffer), "w");
        if (stream)
        {
            asn_fprint(stream, asn_def, msg);
            fclose(stream);

            char *heapStr = strdup(outputBuffer);
            if (heapStr)
            {
                *decodedStrOut = heapStr; 
                *msgTypeStrOut = successTypeName ? successTypeName : "DecodedMessage";
                ASN_STRUCT_FREE(*asn_def, msg);
                return JNI_TRUE;
            }
            else
            {
                *decodedStrOut = "Failed to allocate decoded output string";
                *msgTypeStrOut = successTypeName ? successTypeName : "DecodedMessage";
            }
        }
        else
        {
            *decodedStrOut = "Failed to allocate memory for output";
            *msgTypeStrOut = successTypeName ? successTypeName : "DecodedMessage";
        }

        ASN_STRUCT_FREE(*asn_def, msg);
        return JNI_FALSE;
    }

    if (msg)
    {
        ASN_STRUCT_FREE(*asn_def, msg);
    }
    return JNI_FALSE;
}

JNIEXPORT jobject JNICALL Java_gov_usdot_cv_asn1decoder_Decoder_decodeMsg(JNIEnv *env, jobject obj, jbyteArray encoded_msg, jstring msg_type)
{
    const char *decodedStr = "";
    const char *msgTypeStr = "";
    const char *type = (*env)->GetStringUTFChars(env, msg_type, NULL);

    jboolean success = JNI_FALSE;

    asn_dec_rval_t rval;
    MessageFrame_t *message = 0;

    int len = (*env)->GetArrayLength(env, encoded_msg);
    jbyte *inCArray = (*env)->GetByteArrayElements(env, encoded_msg, 0);

    char buf[len];
    for (int i = 0; i < len; i++)
    {
        buf[i] = inCArray[i];
    }


    (*env)->ReleaseByteArrayElements(env, encoded_msg, inCArray, JNI_ABORT);

    if (strcmp(type, "MessageFrame") == 0)
    {
        rval = uper_decode(0, &asn_DEF_MessageFrame, (void **)&message, buf, len, 0, 0);

        if (rval.code == RC_OK)
        {

            printf("Message ID: %ld\n", message->messageId);

            char outputBuffer[65536];
            FILE *stream = fmemopen(outputBuffer, sizeof(outputBuffer), "w");
            if (stream)
            {
                asn_fprint(stream, &asn_DEF_MessageFrame, message);
                fclose(stream);
                decodedStr = strdup(outputBuffer);
                success = JNI_TRUE;
            }
            else
            {
                decodedStr = "Failed to allocate memory for output";
            }

            switch (message->messageId)
            {
            case 20:
                msgTypeStr = "BasicSafetyMessage";
                break;
            case 18:
                msgTypeStr = "MapData";
                break;
            case 19:
                msgTypeStr = "SPaT";
                break;
            case 31:
                msgTypeStr = "TravelerInformationMessage";
                break;
            case 32:
                msgTypeStr = "PersonalSafetyMessage";
                break;
            default:
                msgTypeStr = "UnknownMessageType";
                break;
            }
        }
        else if (rval.code == RC_WMORE)
        {
            printf("Additional bytes required for decoding");
        }
        else
        {
            printf("Decoding MessageFrame Failed\n");
        }
    }

    else
    {
        // Decoding for Message Types other than MessageFrame

        const asn_TYPE_descriptor_t *asn_def = NULL;
        const char *messageType = "UnknownMessageType";

       
        if (type && strcmp(type, "BSM") == 0)
        {
            asn_def = &asn_DEF_BasicSafetyMessage;
            messageType = "BasicSafetyMessage";
        }
        else if (type && strcmp(type, "PSM") == 0)
        {
            asn_def = &asn_DEF_PersonalSafetyMessage;
            messageType = "PersonalSafetyMessage";
        }
        else if (type && strcmp(type, "TIM") == 0)
        {
            asn_def = &asn_DEF_TravelerInformation;
            messageType = "TravelerInformationMessage";
        }
        else if (type && (strcmp(type, "SPAT") == 0 || strcmp(type, "SPaT") == 0))
        {
            asn_def = &asn_DEF_SPAT;
            messageType = "SPAT";
        }
        else if (type && strcmp(type, "MAP") == 0)
        {
            asn_def = &asn_DEF_MapData;
            messageType = "MapData";
        }
        else
        {
            
            decodedStr = "Unknown message type requested.";
            msgTypeStr = "UnknownMessageType";
            success = JNI_FALSE;
        }

      
        if (asn_def != NULL)
        {
            const char *localDecoded = NULL;
            const char *localType = NULL;

            success = decode_message_only(
                asn_def,
                buf,
                len,
                messageType,
                &localType,
                &localDecoded);

            decodedStr = localDecoded;
            msgTypeStr = localType;


        }
    }

    // getting the class of the DecodedResult
    jclass resultClass = (*env)->FindClass(env, "gov/usdot/cv/libasn1decoder/DecodedResult");

    // getting the id of the DecodedResult() constructor
    jmethodID ctor = (*env)->GetMethodID(env, resultClass, "<init>", "()V");
    if (ctor == NULL)
        return NULL;

    // creating an object of DecodedResult class to return
    jobject resultObj = (*env)->NewObject(env, resultClass, ctor);
    if (resultObj == NULL)
        return NULL;

    // Retrieving the field of DecodedResult class
    jfieldID decodedField = (*env)->GetFieldID(env, resultClass, "decodedMessage", "Ljava/lang/String;");
    jfieldID typeField = (*env)->GetFieldID(env, resultClass, "messageType", "Ljava/lang/String;");
    jfieldID successField = (*env)->GetFieldID(env, resultClass, "success", "Z");

    // Converting the 'decodedStr' to a Java UTF string (jstring)
    jstring jDecodedStr = (*env)->NewStringUTF(env, decodedStr);
    // Converting the message type to a Java UTF string (jstring)
    jstring jMsgTypeStr = (*env)->NewStringUTF(env, msgTypeStr);

    free((void *)decodedStr);
    free((void *)type);
    
    // Set the corresponding fields in the DecodedResult Java object
    (*env)->SetObjectField(env, resultObj, decodedField, jDecodedStr);
    (*env)->SetObjectField(env, resultObj, typeField, jMsgTypeStr);
    (*env)->SetBooleanField(env, resultObj, successField, success);

    return resultObj;
}
