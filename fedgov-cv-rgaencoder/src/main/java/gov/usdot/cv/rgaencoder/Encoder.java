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
 
 import java.util.List;
 import org.apache.commons.codec.binary.Hex;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;

 import gov.usdot.cv.mapencoder.ByteArrayObject;
 
 public class Encoder {
     private static final Logger logger = LogManager.getLogger(Encoder.class);
 
     public Encoder() {
        //Will be developed at a later time
     }
 
     // Load libasn1c.so external C library
     static {
         try {
             System.loadLibrary("asn1c_rga");
         } catch (Exception e) {
             logger.error("Exception trapped while trying to load the asn1c library", e);
         }
     }
 
     /**
      * This is the declaration for native method. It will take data from RGA message
      * object and return an byte array with encoded information. Because of the
      * efficiency of JNI method call, it takes fields directly instead of a
      * single RGA object.
      * 
      * @return encoded RGA message
      */

     public native byte[] encodeRGA(BaseLayer baseLayer, List<GeometryContainer> geometryContainers);
 
     //
     public ByteArrayObject encode(RGAData message) {
         logger.debug("Encoding the RGA message " + message.toString() + "!");
 
         byte[] encodeMsg = encodeRGA(message.getBaseLayer(), message.getGeometryContainers());
 
         if (encodeMsg == null) {
             // cannot encode RGA message
             logger.error("Cannot encode!");
             return new ByteArrayObject("RGA", null);
         }

         String hexString = Hex.encodeHexString(encodeMsg);
         logger.debug("Encoded hex string of the RGA message: " + hexString);
 
         return new ByteArrayObject("RGA", encodeMsg);
     }

 }
 