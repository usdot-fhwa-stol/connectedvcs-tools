/*
 * Copyright (C) 2026 LEIDOS.
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

#define _GNU_SOURCE
#include <ctype.h>
#include <stdio.h>
#include <sys/types.h>
#include "gov_usdot_cv_asn1decoder_TIMDecoder.h"
#include "MessageFrame.h"
#include "DSRCmsgID.h"
#include "TravelerInformation.h"
#include <stdint.h>
#include <string.h>
#include <stdlib.h>

/*
 * Identifies, among a type's own top-level fields (from the schema's element
 * list, in declaration order), the last one with real printed content and the
 * first one that's empty or missing. This is a text-level heuristic run over
 * the same asn_fprint() dump already shown to the user, rather than reaching
 * into raw struct memory -- so it works uniformly for any field kind (list,
 * nested SEQUENCE, scalar) without needing to know each one's C layout.
 *
 * It cannot see past the first level (it won't say *which* element of a list
 * or *which* nested field failed), and "empty" is a heuristic, not certainty --
 * a field can legitimately encode as empty. But combined with an overall
 * decode failure, the first empty/missing field after some real content is a
 * reasonable place to point at.
 */
static void find_last_and_likely_failed_field(
    const asn_TYPE_descriptor_t *asn_def,
    const char *dump,
    const char **lastReachedOut,
    const char **likelyFailedOut)
{
	*lastReachedOut = NULL;
	*likelyFailedOut = NULL;
	if (!dump || !asn_def->elements || asn_def->elements_count == 0)
		return;

	const char *searchFrom = dump;
	for (unsigned i = 0; i < asn_def->elements_count; i++)
	{
		const char *name = asn_def->elements[i].name;
		if (!name)
			continue;

		char needle[128];
		snprintf(needle, sizeof(needle), "\n    %s:", name);
		const char *pos = strstr(searchFrom, needle);
		if (!pos)
		{
			if (!*likelyFailedOut)
				*likelyFailedOut = name;
			continue;
		}

		const char *afterColon = pos + strlen(needle);
		searchFrom = afterColon;
		while (*afterColon == ' ')
			afterColon++;

		int hasContent;
		if (strncmp(afterColon, "<absent>", 8) == 0)
		{
			hasContent = 0;
		}
		else
		{
			const char *newline = strchr(afterColon, '\n');
			const char *brace = strchr(afterColon, '{');
			if (brace && (!newline || brace < newline + 1))
			{
				const char *p = brace + 1;
				int depth = 1;
				hasContent = 0;
				while (*p && depth > 0)
				{
					if (*p == '{')
						depth++;
					else if (*p == '}')
						depth--;
					else if (!isspace((unsigned char)*p) && depth >= 1)
						hasContent = 1;
					p++;
				}
				searchFrom = p;
			}
			else
			{
				hasContent = (*afterColon != '\n' && *afterColon != '\0');
			}
		}

		if (hasContent)
		{
			*lastReachedOut = name;
			/* Decode continued past any prior gap -- that gap was legitimate
			 * absence, not a failure point. */
			*likelyFailedOut = NULL;
		}
		else
		{
			/* Printed but empty is stronger evidence of a stalled decode than
			 * a field that was never printed at all (which is often just
			 * legitimate absence), so it always takes priority. */
			*likelyFailedOut = name;
		}
	}
}

/*
 * Builds a failure message for a decode attempt. asn_dec_rval_t has no offset
 * info to draw on here, so the message stays generic beyond the type name and
 * the field-level guess above -- the real value is the dump of whatever fields
 * decoded successfully before the failure. uper_decode() never frees/clears
 * the target struct on failure (only its own scratch buffers), so partially-
 * decoded fields are still valid to print here. The caller retains ownership
 * of msg and is responsible for freeing it.
 */
static char *build_failure_result(const asn_dec_rval_t *rval, const asn_TYPE_descriptor_t *asn_def, void *msg)
{
	const char *typeName = (asn_def && asn_def->name) ? asn_def->name : "message";

	char partialBuffer[65536];
	partialBuffer[0] = '\0';
	if (msg)
	{
		FILE *partialStream = fmemopen(partialBuffer, sizeof(partialBuffer), "w");
		if (partialStream)
		{
			asn_fprint(partialStream, asn_def, msg);
			fclose(partialStream);
		}
	}

	const char *lastReached = NULL;
	const char *likelyFailed = NULL;
	if (partialBuffer[0] != '\0')
		find_last_and_likely_failed_field(asn_def, partialBuffer, &lastReached, &likelyFailed);

	char detail[160] = "";
	if (lastReached && likelyFailed)
		snprintf(detail, sizeof(detail), " -- successfully decoded through '%s'; likely failed at or in '%s'", lastReached, likelyFailed);
	else if (likelyFailed)
		snprintf(detail, sizeof(detail), " -- likely failed at or in '%s'", likelyFailed);
	else if (lastReached)
		snprintf(detail, sizeof(detail), " -- successfully decoded through '%s'", lastReached);

	char errBuf[256];
	if (rval->code == RC_WMORE)
		snprintf(errBuf, sizeof(errBuf), "Decoding failed for %s: message is truncated or incomplete%s", typeName, detail);
	else
		snprintf(errBuf, sizeof(errBuf), "Decoding failed for %s%s", typeName, detail);

	if (partialBuffer[0] == '\0')
		return strdup(errBuf);

	char *combined = NULL;
	if (asprintf(&combined,
	             "%s\n\n--- Decoded so far, before the failure (fields after this point may be incomplete or default-valued) ---\n%s",
	             errBuf, partialBuffer) < 0 || !combined)
		return strdup(errBuf);

	return combined;
}

/*
 * Decodes an ASN.1 message using the provided ASN descriptor and UPER encoding.
 * On success:
 *   - decodedStrOut is set to human-readable ASN printout of the decoded message
 *   - msgTypeStrOut is set to the provided messageType
 * Returns JNI_TRUE on successful decode, otherwise JNI_FALSE.
 */
static jboolean decode_message_only(
    const asn_TYPE_descriptor_t *asn_def,
    const void *buf,
    int len,
    const char *messageType,
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
				*msgTypeStrOut = messageType;
				ASN_STRUCT_FREE(*asn_def, msg);
				return JNI_TRUE;
			}
			*decodedStrOut = "Failed to allocate decoded output string";
			*msgTypeStrOut = messageType;
		}
		else
		{
			*decodedStrOut = "Failed to allocate memory for output";
		}

		ASN_STRUCT_FREE(*asn_def, msg);
		return JNI_FALSE;
	}

	*decodedStrOut = build_failure_result(&rval, asn_def, msg);

	if (msg)
	{
		ASN_STRUCT_FREE(*asn_def, msg);
	}
	return JNI_FALSE;
}

/*
 * MessageFrame.value is an ASN.1 open type: asn1c decodes it into a temporary
 * that is discarded entirely if decoding fails partway (the same
 * discard-on-failure behavior as CHOICE), so a failed MessageFrame decode
 * never leaves partial payload content behind. To get a real partial dump,
 * we unwrap the envelope ourselves: decode messageId, skip the open type's
 * own length determinant (X.691 length determinant encoding), and hand the
 * remaining bytes directly to decode_message_only() for TravelerInformation,
 * which -- being a plain SEQUENCE decode -- does leave partial fields in
 * place on failure.
 *
 * MessageFrame's own SEQUENCE preamble is a single extensibility bit (it has
 * no OPTIONAL fields ahead of messageId), and DSRCmsgID is a fixed-width
 * 15-bit constrained INTEGER regardless of value -- both verified empirically
 * against the generated code -- so the envelope header is always exactly 16
 * bits (2 bytes), independent of message type or content.
 */
static jboolean decode_tim_message_frame(const void *buf_in, int len, const char **msgTypeStrOut, const char **decodedStrOut)
{
	const unsigned char *buf = (const unsigned char *)buf_in;
	*msgTypeStrOut = "UnknownMessageType";

	if (len >= 3)
	{
		DSRCmsgID_t *msgId = NULL;
		asn_dec_rval_t idRval = uper_decode(0, &asn_DEF_DSRCmsgID, (void **)&msgId, buf, len, 1, 0);

		if (idRval.code == RC_OK && msgId != NULL)
		{
			long id = *msgId;
			free(msgId);

			if (id == DSRCmsgID_travelerInformation)
			{
				/* Envelope header is always exactly 2 bytes: 1 extension bit + 15-bit DSRCmsgID. */
				int hdrByte = 2;
				unsigned char b0 = buf[hdrByte];
				int lenOctets = -1;
				if ((b0 & 0x80) == 0)
				{
					lenOctets = 1;
				}
				else if ((b0 & 0xC0) == 0x80 && len > hdrByte + 1)
				{
					lenOctets = 2;
				}
				/* else: fragmented (>16383 byte) open type payload -- not handled, falls through below */

				if (lenOctets > 0)
				{
					int payloadOff = hdrByte + lenOctets;
					if (payloadOff <= len)
					{
						return decode_message_only(&asn_DEF_TravelerInformation, buf + payloadOff,
						                            len - payloadOff, "TravelerInformationMessage",
						                            msgTypeStrOut, decodedStrOut);
					}
				}
			}
		}
		else if (msgId)
		{
			free(msgId);
		}
	}

	/* Not a recognizable TIM envelope, or unwrap failed -- fall back to
	 * decoding MessageFrame directly as before. */
	MessageFrame_t *message = NULL;
	asn_dec_rval_t rval = uper_decode(0, &asn_DEF_MessageFrame, (void **)&message, buf, len, 0, 0);

	if (rval.code == RC_OK)
	{
		switch (message->messageId)
		{
		case 31:
			*msgTypeStrOut = "TravelerInformationMessage";
			break;
		default:
			*msgTypeStrOut = "UnknownMessageType";
			break;
		}

		char outputBuffer[65536];
		FILE *stream = fmemopen(outputBuffer, sizeof(outputBuffer), "w");
		if (stream)
		{
			asn_fprint(stream, &asn_DEF_MessageFrame, message);
			fclose(stream);
			*decodedStrOut = strdup(outputBuffer);
			return JNI_TRUE;
		}
		*decodedStrOut = "Failed to allocate memory for output";
		return JNI_FALSE;
	}

	*decodedStrOut = build_failure_result(&rval, &asn_DEF_MessageFrame, message);
	return JNI_FALSE;
}

JNIEXPORT jstring JNICALL Java_gov_usdot_cv_asn1decoder_TIMDecoder_decodeTimMsg(JNIEnv *env, jobject obj, jbyteArray encoded_msg)
{

	const char *decodedStr = "";
	const char *msgTypeStr = "";
	jboolean success = JNI_FALSE;

	int len = (*env) -> GetArrayLength(env, encoded_msg); /* Number of bytes in encoded_message */
	jbyte *inCArray = (*env) -> GetByteArrayElements(env, encoded_msg, 0); /* Get Java byte array content */
	char buf[len]; /* Buffer for decoder function */
	for(int i = 0; i < len; i++) {
		buf[i] = inCArray[i];
	} /* Copy into buffer */

	success = decode_tim_message_frame(buf, len, &msgTypeStr, &decodedStr);

	//getting the class of the DecodedResult
	jclass resultClass = (*env)->FindClass(env, "gov/usdot/cv/libasn1decoder/DecodedResult");

	// getting the id of the DecodedResult() constructor
	jmethodID ctor = (*env)->GetMethodID(env, resultClass, "<init>", "()V");
	if (ctor == NULL) return NULL;
	// creating an object of DecodedResult class to return
	jobject resultObj = (*env)->NewObject(env, resultClass, ctor);
	if (resultObj == NULL) return NULL;

	//Retrieving the field of DecodedResult class
	jfieldID decodedField = (*env)->GetFieldID(env, resultClass, "decodedMessage", "Ljava/lang/String;");
	jfieldID typeField = (*env)->GetFieldID(env, resultClass, "messageType", "Ljava/lang/String;");
	jfieldID successField = (*env)->GetFieldID(env, resultClass, "success", "Z");

	// Converting the 'decodedStr' to a Java UTF string (jstring)
	jstring jDecodedStr = (*env)->NewStringUTF(env, decodedStr);
	//Converting the message type to a Java UTF string (jstring)
	jstring jMsgTypeStr = (*env)->NewStringUTF(env, msgTypeStr);

	// Set the corresponding fields in the DecodedResult Java object
	(*env)->SetObjectField(env, resultObj, decodedField, jDecodedStr);
	(*env)->SetObjectField(env, resultObj, typeField, jMsgTypeStr);
	(*env)->SetBooleanField(env, resultObj, successField, success);

	return resultObj;
}
