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
#include <stdint.h>
#include <jni.h> // Required for JNIEXPORT and JNIEnv
#include "gov_usdot_cv_timencoder_Encoder.h"
#include "MessageFrame.h"

JNIEXPORT jbyteArray JNICALL Java_gov_usdot_cv_timencoder_Encoder_encodeTIM(
    JNIEnv *env,
    jobject cls,
    jobjectArray inputMsg
) {
   printf("\n*** encodeTIM method of asn1c_timencoder is successfully called ***\n");

    // Example data: 0xDE 0xAD 0xBE 0xEF
    jbyte buffer[] = { (jbyte)0xDE, (jbyte)0xAD, (jbyte)0xBE, (jbyte)0xEF };

    jbyteArray result = (*env)->NewByteArray(env, 4);
    (*env)->SetByteArrayRegion(env, result, 0, 4, buffer);
    return result;
}
