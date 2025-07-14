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

#include <jni.h>
#include "LaneConstructorType.h" 
#include "NodeXYZOffsetValue.h" 
#include "IndividualWayConnections.h" 
#include "IndividualWayCnxnsManeuvers.h"
/* Header for class gov_usdot_cv_lib_asn1c_RGAMessage*/

#ifndef _Included_gov_usdot_cv_lib_asn1c_RGAMessage
#define _Included_gov_usdot_cv_lib_asn1c_RGAMessage
#ifdef __cplusplus
extern "C" {
#endif

// Constants for Geometry Layer IDs
#define APPROACH_GEOMETRY_LAYER_ID 1
#define MOTOR_VEHICLE_LANE_GEOMETRY_LAYER_ID 2
#define BICYCLE_LANE_GEOMETRY_LAYER_ID 3
#define CROSSWALK_LANE_GEOMETRY_LAYER_ID 4

// Constants for Movements Layer IDs
#define MTR_VEH_LANE_DIRECTION_OF_TRAVEL_LAYER_ID 1
#define MTR_VEH_LANE_CONNECTIONS_LAYER_ID 2
#define MTR_VEH_LANE_CONNECTIONS_MANEUVERS_LAYER_ID 3
#define BIKE_LANE_CONNECTIONS__LAYER_ID 4

// Constants for WayCnxnManeuverControlType
#define SIGNALIZED_CONTROL 1
#define UNSIGNALIZED_CONTROL 2
#define UNCONTROLLED 3

// Constants for NodeSetNode Choice
#define FIRST_NODE 0
#define LAST_NODE 1

// Constants for LaneConstructorType Choice
#define PHYSICAL_NODE 1
#define COMPUTED_NODE 2
#define DUPLICATE_NODE 3

// Constants for NodeXYZOffsetValue Choice
#define OFFSET_B10 1
#define OFFSET_B11 2
#define OFFSET_B12 3
#define OFFSET_B13 4
#define OFFSET_B14 5
#define OFFSET_B16 6

/*
 * Class:     gov_usdot_cv_lib_asn1c_RGAMessage
 * Method:    encodeRGA
 * Encodes an RGA message into a UPER-encoded byte array.
 * 
 * @param JNIEnv* - Pointer to the JNI environment
 * @param jobject - Reference to the calling Java object
 * @param jobject - Java object representing the base layer
 * @param jobject - Java object representing the geometry containers
 * @return jbyteArray - Encoded ASN.1 message as a Java byte array, or NULL if encoding fails
 */
JNIEXPORT jbyteArray JNICALL Java_gov_usdot_cv_rgaencoder_Encoder_encodeRGA
  (JNIEnv *, jobject, jobject, jobject, jobject);

/*
 * Method to populate a LaneConstructorType_t structure from corresponding Java object
 * @param env JNI environment pointer.
 * @param laneConstructorTypeObj Java object containing lane construction type data.
 * @param laneConstructorType Pointer to the C structure
 */
void populateLaneConstructorType(JNIEnv *env, jobject laneConstructorTypeObj, LaneConstructorType_t *laneConstructorType);

/*
 * Method to populate a NodeXYZOffsetValue_t structure from corresponding Java object
 * @param env JNI environment pointer.
 * @param offsetValueObj Java object containing offset value data.
 * @param offsetValue Pointer to the C structure
 */
void populateNodeXYZOffsetValue(JNIEnv *env, jobject offsetValueObj, NodeXYZOffsetValue_t *offsetValue);

/**
 * Method to populate a IndividualWayConnections_t structure from corresponding Java object
 * @param env JNI environment pointer.
 * @param wayConnObj Java object containing IndividualWayConnection value data.
 * @param indWayCnxn Pointer to the C structure
 */
void populateIndividualWayConnection(JNIEnv *env, jobject wayConnObj, IndividualWayConnections_t *indWayCnxn); 

/**
 * Method to populate a IndividualWayCnxnManeuvers_t structure from corresponding Java object
 * @param env JNI environment pointer.
 * @param wayCnxnsManeuversObj Java object containing IndividualWayCnxnManeuvers value data.
 * @param indWayCnxnManeuvers Pointer to the C structure
 */
void populateIndividualWayCnxnsManeuvers(JNIEnv *env, jobject wayCnxnsManeuversObj, IndividualWayCnxnsManeuvers_t *indWayCnxnManeuvers);


#ifdef __cplusplus
}
#endif
#endif