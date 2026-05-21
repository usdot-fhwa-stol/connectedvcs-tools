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

 public class Node_LL_32B {
     private OffsetLL_B16 lon;
     private OffsetLL_B16 lat;
 
 
     public Node_LL_32B(OffsetLL_B16 lon, OffsetLL_B16 lat) {
         this.lon = lon;
         this.lat = lat;
     }    
 
     public OffsetLL_B16 getLon() {
         return lon;
     }

     public OffsetLL_B16 getLat() {
         return lat;
     }
 
     public void setLon(OffsetLL_B16 lon) {
         this.lon = lon;
     }

     public void setLat(OffsetLL_B16 lat) {
         this.lat = lat;
     }
 
     @Override
     public String toString() {
         return "Node_LL_32B{" +
                 "lon=" + lon +
                 ", lat=" + lat +
                 '}';
     }
 }