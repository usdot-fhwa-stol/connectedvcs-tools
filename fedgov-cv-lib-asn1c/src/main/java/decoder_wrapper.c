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
	// TODO temporary
	const char *resultStr;

	asn_dec_rval_t rval; /* Decoder return value */
	MessageFrame_t *message = 0; /* Type to decode */
	
	int len = (*env) -> GetArrayLength(env, encoded_msg); /* Number of bytes in encoded_bsm */
	jbyte *inCArray = (*env) -> GetByteArrayElements(env, encoded_msg, 0); /* Get Java byte array content */
	char buf[len]; /* Buffer for decoder function */
	for(int i = 0; i < len; i++) {
		buf[i] = inCArray[i];
	} /* Copy into buffer */

	rval = uper_decode(0, &asn_DEF_MessageFrame, (void **) &message, buf, len, 0, 0);

	if(rval.code == RC_OK) {

		asn_fprint(stdout, &asn_DEF_MessageFrame, message);

		char outputBuffer[4096]; // Ensure it's large enough
		FILE *stream = fmemopen(outputBuffer, sizeof(outputBuffer), "w");
		if (stream) {
			asn_fprint(stream, &asn_DEF_MessageFrame, message);
			fclose(stream);
			resultStr = strdup(outputBuffer); // Dumping the outputBuffer
		} else {
			resultStr = "Failed to allocate memory for output";
		}

	}
	else if(rval.code == RC_WMORE) {
		printf("More Data Needed");
		
	}
	else
	{
		printf("Decoding Failed");

	}
    return (*env)->NewStringUTF(env, resultStr1);
}
