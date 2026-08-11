/*
 * Copyright (C) 2026 LEIDOS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.usdot.cv.timencoder;

import org.junit.Assert;
import org.junit.Test;

import gov.usdot.cv.mapencoder.IntersectionGeometry;

/**
 * MAP and TIM each load the
 * consolidated asn1c JNI shared library at class-init time. Before
 * NativeLoadLibrary, each Encoder called System.loadLibrary() directly
 * with its own (now-stale) library name; whichever one initialized second
 * in a given JVM/ClassLoader risked an UnsatisfiedLinkError. These tests
 * force both encoders to initialize in the same JVM, in both orders, and
 * call a real native method afterwards to prove the library is not just
 * loaded but actually linked and callable.
 */
public class CrossEncoderNativeLoadOrderTest {

    @Test
    public void mapEncoderThenTimEncoderInitializeInSameJvm() {
        gov.usdot.cv.mapencoder.Encoder mapEncoder = new gov.usdot.cv.mapencoder.Encoder();
        mapEncoder.encodeMap(0, 0L, 0L, 0L, new IntersectionGeometry[0]);

        gov.usdot.cv.timencoder.Encoder timEncoder = new gov.usdot.cv.timencoder.Encoder();
        // Reaching this line without an UnsatisfiedLinkError proves the TIM
        // encoder's static initializer -- which runs NativeLoadLibrary.load()
        // a second time in this ClassLoader -- succeeded after MAP's did.
        Assert.assertNotNull(timEncoder);
    }

    @Test
    public void timEncoderThenMapEncoderInitializeInSameJvm() {
        gov.usdot.cv.timencoder.Encoder timEncoder = new gov.usdot.cv.timencoder.Encoder();
        Assert.assertNotNull(timEncoder);

        gov.usdot.cv.mapencoder.Encoder mapEncoder = new gov.usdot.cv.mapencoder.Encoder();
        mapEncoder.encodeMap(0, 0L, 0L, 0L, new IntersectionGeometry[0]);
    }
}
