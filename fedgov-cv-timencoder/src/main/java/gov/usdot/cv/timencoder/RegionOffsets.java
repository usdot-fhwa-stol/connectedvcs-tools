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

 public class RegionOffsets {
     private OffsetLL_B16 xOffset;
     private OffsetLL_B16 yOffset;
     private OffsetLL_B16 zOffset;
 
 
     public RegionOffsets(OffsetLL_B16 xOffset, OffsetLL_B16 yOffset, OffsetLL_B16 zOffset) {
         this.xOffset = xOffset;
         this.yOffset = yOffset;
         this.zOffset = zOffset;
     }    
 
     public OffsetLL_B16 getXOffset() {
         return xOffset;
     }

     public OffsetLL_B16 getYOffset() {
         return yOffset;
     }

     public OffsetLL_B16 getZOffset() {
         return zOffset;
     }

    public void setXOffset(OffsetLL_B16 xOffset) {
        this.xOffset = xOffset;
    }

    public void setYOffset(OffsetLL_B16 yOffset) {
        this.yOffset = yOffset;
    }

    public void setZOffset(OffsetLL_B16 zOffset) {
        this.zOffset = zOffset;
    }  
 
     @Override
     public String toString() {
         return "RegionOffsets{" +
                 "xOffset=" + xOffset +
                 ", yOffset=" + yOffset +
                 ", zOffset=" + zOffset +
                 '}';
     }
 }