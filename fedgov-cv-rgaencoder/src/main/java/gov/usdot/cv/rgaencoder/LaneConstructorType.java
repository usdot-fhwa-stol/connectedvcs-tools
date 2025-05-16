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

package gov.usdot.cv.rgaencoder;

public class LaneConstructorType {
    private int choice;
    private PhysicalXYZNodeInfo physicalXYZNodeInfo;
    private ComputedXYZNodeInfo computedXYZNodeInfo;
    private DuplicateXYZNodeInfo duplicateXYZNodeInfo;

    // Constants to represent the choices
    public static final int PHYSICAL_NODE = 1;
    public static final int COMPUTED_NODE = 2;
    public static final int DUPLICATE_NODE = 3;

    public LaneConstructorType() {
        this.choice = -1;
        this.physicalXYZNodeInfo = null;
        this.computedXYZNodeInfo = null;
        this.duplicateXYZNodeInfo = null;
    }

    public LaneConstructorType(int choice, PhysicalXYZNodeInfo physicalXYZNodeInfo,
            ComputedXYZNodeInfo computedXYZNodeInfo, DuplicateXYZNodeInfo duplicateXYZNodeInfo) {
        this.choice = choice;
        this.physicalXYZNodeInfo = physicalXYZNodeInfo;
        this.computedXYZNodeInfo = computedXYZNodeInfo;
        this.duplicateXYZNodeInfo = duplicateXYZNodeInfo;
    }

    public int getChoice() {
        return choice;
    }

    public void setChoice(int choice) {
        this.choice = choice;
    }

    public PhysicalXYZNodeInfo getPhysicalXYZNodeInfo() {
        return physicalXYZNodeInfo;
    }

    public void setPhysicalXYZNodeInfo(PhysicalXYZNodeInfo physicalXYZNodeInfo) {
        this.physicalXYZNodeInfo = physicalXYZNodeInfo;
    }

    public ComputedXYZNodeInfo getComputedXYZNodeInfo() {
        return computedXYZNodeInfo;
    }

    public void setComputedXYZNodeInfo(ComputedXYZNodeInfo computedXYZNodeInfo) {
        this.computedXYZNodeInfo = computedXYZNodeInfo;
    }

    public DuplicateXYZNodeInfo getDuplicateXYZNodeInfo() {
        return duplicateXYZNodeInfo;
    }

    public void setDuplicateXYZNodeInfo(DuplicateXYZNodeInfo duplicateXYZNodeInfo) {
        this.duplicateXYZNodeInfo = duplicateXYZNodeInfo;
    }

    @Override
    public String toString() {
        return "LaneConstructorType [physicalXYZNodeInfo=" + physicalXYZNodeInfo + ", computedXYZNodeInfo="
                + computedXYZNodeInfo + ", duplicateXYZNodeInfo=" + duplicateXYZNodeInfo + "]";
    }
}
