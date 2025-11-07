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

 public class Node_LL_28B {
     private OffsetLL_B14 lon;
     private OffsetLL_B14 lat;
 
 
     public Node_LL_28B(OffsetLL_B14 lon, OffsetLL_B14 lat) {
         this.lon = lon;
         this.lat = lat;
     }    
 
     public OffsetLL_B14 getLon() {
         return lon;
     }

     public OffsetLL_B14 getLat() {
         return lat;
     }
 
     public void setLon(OffsetLL_B14 lon) {
         this.lon = lon;
     }

     public void setLat(OffsetLL_B14 lat) {
         this.lat = lat;
     }
 
     @Override
     public String toString() {
         return "Node_LL_28B{" +
                 "lon=" + lon +
                 ", lat=" + lat +
                 '}';
     }
 }