/*
 * Copyright (C) 2023 LEIDOS.
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

#include <jni.h>
/* Header for class gov_usdot_cv_lib_asn1c_MapMessage*/

#ifndef _Included_gov_usdot_cv_lib_asn1c_MapMessage
#define _Included_gov_usdot_cv_lib_asn1c_MapMessage
#ifdef __cplusplus
extern "C" {
#endif
#undef gov_usdot_cv_lib_asn1c_MapMessage_MAX_NODE_LIST_SIZE
#define gov_usdot_cv_lib_asn1c_MapMessage_MAX_NODE_LIST_SIZE 63L
#undef gov_usdot_cv_lib_asn1c_MapMessage_INTERSECTION_DATA_SIZE
#define gov_usdot_cv_lib_asn1c_MapMessage_INTERSECTION_DATA_SIZE 9L
#undef gov_usdot_cv_lib_asn1c_MapMessage_MAX_LANE_LIST_SIZE
#define gov_usdot_cv_lib_asn1c_MapMessage_MAX_LANE_LIST_SIZE 255L
#undef gov_usdot_cv_lib_asn1c_MapMessage_NODE_OFFSETS_DATA_SIZE
#define gov_usdot_cv_lib_asn1c_MapMessage_NODE_OFFSETS_DATA_SIZE 189L
#undef gov_usdot_cv_lib_asn1c_MapMessage_CONNECTION_DATA_SIZE
#define gov_usdot_cv_lib_asn1c_MapMessage_CONNECTION_DATA_SIZE 32L
/*
 * Class:     gov_usdot_cv_lib_asn1c_MapMessage
 * Method:    decodeMap
 */
JNIEXPORT jint JNICALL Java_gov_usdot_cv_mapencoder_Encoder_decodeMap
  (JNIEnv *, jobject, jbyteArray, jobject, jintArray, jintArray, jintArray, jintArray, jintArray, jintArray, jobjectArray, jobjectArray, jobject, jobject);

/*
 * Class:     gov_usdot_cv_lib_asn1c_MapMessage
 * Method:    encodeMAP
 */
JNIEXPORT jbyteArray JNICALL Java_gov_usdot_cv_mapencoder_Encoder_encodeMap
  (JNIEnv *, jobject, jint, jlong, jlong, jlong, jobjectArray);
#ifdef __cplusplus
}
#endif
#endif