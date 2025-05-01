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
#include <stdint.h>

JNIEXPORT jstring JNICALL Java_gov_usdot_cv_asn1decoder_Decoder_decodeMsg(JNIEnv *env, jobject obj, jbyteArray encoded_msg)
{
	
	const char *decodedStr = "";
	const char *msgTypeStr = "";
	jboolean success = JNI_FALSE;

	asn_dec_rval_t rval; /* Decoder return value */
	MessageFrame_t *message = 0; /* Type to decode */
	
	int len = (*env) -> GetArrayLength(env, encoded_msg); /* Number of bytes in encoded_message */
	jbyte *inCArray = (*env) -> GetByteArrayElements(env, encoded_msg, 0); /* Get Java byte array content */
	char buf[len]; /* Buffer for decoder function */
	for(int i = 0; i < len; i++) {
		buf[i] = inCArray[i];
	} /* Copy into buffer */

	rval = uper_decode(0, &asn_DEF_MessageFrame, (void **) &message, buf, len, 0, 0);

	if(rval.code == RC_OK) {

		// If message decoding is sucessful print the message id
		printf("Message ID: %ld\n", message->messageId);
	
		//copying decoded message to char  outputBuffer

		char outputBuffer[65536]; //Output buffer of size 64 KB
		//Open memory stream outputBuffer to write into outputBuffer
 		FILE *stream = fmemopen(outputBuffer, sizeof(outputBuffer), "w");
		if (stream) {
			/*if stream is sucessfully created by fmemopen*/				
			asn_fprint(stream, &asn_DEF_MessageFrame, message); // Write the ASN.1 encoded message ('message') to the memory stream ('stream').
			fclose(stream); //closing the memory stream
			decodedStr  = strdup(outputBuffer); // Copy the content of 'outputBuffer' to 'resultStr' using 'strdup()'.
			success = JNI_TRUE;   
		} else {
			   // If 'fmemopen()' failed to create the memory stream ('stream' is NULL)
			   decodedStr = "Failed to allocate memory for output";
		}
		switch (message->messageId) {
			case 20:
				msgTypeStr = "BasicSafetyMessage";
				break;
			case 18:
				msgTypeStr = "MapData";
				break;
			case 19:
				msgTypeStr = "SPAT";
				break;
			default:
				msgTypeStr = "UnknownMessageType";
				break;
		}

	}
	else if(rval.code == RC_WMORE) {
		// Checks if decoding requires more data ('RC_WMORE') indicating incomplete parsing needing additional bytes.

		printf("Additional bytes  required for decoding");
		
	}
	else
	{
		//Decoding failed due to invalid data
		printf("Decoding Failed");

	}
	//converting char array to Java String
   // return (*env)->NewStringUTF(env, resultStr);
	//creating Java Object
	jclass resultClass = (*env)->FindClass(env, "gov/usdot/cv/libasn1decoder/DecodedResult");

	jmethodID ctor = (*env)->GetMethodID(env, resultClass, "<init>", "()V");
	if (ctor == NULL) return NULL;

	jobject resultObj = (*env)->NewObject(env, resultClass, ctor);
	if (resultObj == NULL) return NULL;

	jfieldID decodedField = (*env)->GetFieldID(env, resultClass, "decodedMessage", "Ljava/lang/String;");
	jfieldID typeField = (*env)->GetFieldID(env, resultClass, "messageType", "Ljava/lang/String;");
	jfieldID successField = (*env)->GetFieldID(env, resultClass, "success", "Z");

	jstring jDecodedStr = (*env)->NewStringUTF(env, decodedStr);
	jstring jMsgTypeStr = (*env)->NewStringUTF(env, msgTypeStr);

	(*env)->SetObjectField(env, resultObj, decodedField, jDecodedStr);
	(*env)->SetObjectField(env, resultObj, typeField, jMsgTypeStr);
	(*env)->SetBooleanField(env, resultObj, successField, success);

	return resultObj;
}
