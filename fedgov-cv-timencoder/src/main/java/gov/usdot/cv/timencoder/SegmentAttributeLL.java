
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

package gov.usdot.cv.timencoder;

public class SegmentAttributeLL {
    private long value;

    public static final long reserved = 0;
	public static final long doNotBlock = 1;
	public static final long whiteLine = 2;
	public static final long mergingLaneLeft = 3;
	public static final long mergingLaneRight = 4;
	public static final long curbOnLeft = 5;
	public static final long curbOnRight = 6;
	public static final long loadingzoneOnLeft = 7;
	public static final long loadingzoneOnRight = 8;
	public static final long turnOutPointOnLeft = 9;
	public static final long turnOutPointOnRight = 10;
	public static final long adjacentParkingOnLeft = 11;
	public static final long adjacentParkingOnRight = 12;
	public static final long adjacentBikeLaneOnLeft = 13;
	public static final long adjacentBikeLaneOnRight = 14;
	public static final long sharedBikeLane = 15;
	public static final long bikeBoxInFront = 16;
	public static final long transitStopOnLeft = 17;
	public static final long transitStopOnRight = 18;
	public static final long transitStopInLane = 19;
	public static final long sharedWithTrackedVehicle = 20;
	public static final long safeIsland = 21;
	public static final long lowCurbsPresent = 22;
	public static final long rumbleStripPresent = 23;
	public static final long audibleSignalingPresent = 24;
	public static final long adaptiveTimingPresent = 25;
	public static final long rfSignalRequestPresent = 26;
	public static final long partialCurbIntrusion = 27;
	public static final long taperToLeft = 28;
	public static final long taperToRight = 29;
	public static final long taperToCenterLine = 30;
	public static final long parallelParking = 31;
	public static final long headInParking = 32;
	public static final long freeParking = 33;
	public static final long timeRestrictionsOnParking = 34;
	public static final long costToPark = 35;
	public static final long midBlockCurbPresent = 36;
	public static final long unEvenPavementPresent = 37;

    public SegmentAttributeLL(long value) {
        this.value = value;
    }    

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "SegmentAttributeLL{" +
                "value=" + value +
                '}';
    }
}
