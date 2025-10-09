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

public enum NodeAttributeLL {

    reserved(0),
    stopLine(1),
    roundedCapStyleA(2),
    roundedCapStyleB(3),
    mergePoint(4),
    divergePoint(5),
    downstreamStopLine(6),
    downstreamStartNode(7),
    closedToTraffic(8),
    safeIsland(9),
    curbPresentAtStepOff(10),
    hydrantPresent(11);

    private final int value;

    NodeAttributeLL(int value) {
        this.value = value;
    }    

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "NodeAttributeLL{" +
                "value=" + value +
                '}';
    }
}
