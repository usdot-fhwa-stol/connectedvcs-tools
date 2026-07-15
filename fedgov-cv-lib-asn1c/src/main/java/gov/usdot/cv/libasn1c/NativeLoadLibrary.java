/*
 * Copyright (C) 2026 LEIDOS.
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
package gov.usdot.cv.libasn1c;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Single point of entry for loading the consolidated asn1c JNI shared
 * library used by every MAP/TIM/RGA encoder and decoder.
 * <p>
 * The library is extracted from the classpath (it is bundled inside this
 * jar) to a uniquely named temp file and loaded via {@link System#load},
 * rather than {@link System#loadLibrary}. This matters because when the
 * MAP and TIM tools are deployed as separate webapps sharing one JVM
 * (each with its own isolated ClassLoader), the JVM tracks loaded native
 * libraries by canonical file path across all ClassLoaders: a second
 * ClassLoader that tries to load the exact same .so path fails with
 * "already loaded in another classloader", even though it is the first
 * time that ClassLoader has attempted the load. Loading a private,
 * uniquely-named copy per ClassLoader sidesteps that collision. The
 * static guard here only prevents redundant re-loading within a single
 * ClassLoader; it is not a substitute for the unique-path trick.
 */
public final class NativeLoadLibrary {

    private static final Logger logger = LogManager.getLogger(NativeLoadLibrary.class);
    private static final String LIBRARY_RESOURCE_NAME = "libasn1c_jni.so";

    private static boolean loaded = false;

    private NativeLoadLibrary() {
    }

    /**
     * Loads the asn1c JNI shared library if it has not already been
     * loaded by this ClassLoader. Safe to call from multiple classes'
     * static initializers.
     */
    public static synchronized void load() {
        if (loaded) {
            return;
        }
        File tempLib = extractToTempFile();
        System.load(tempLib.getAbsolutePath());
        loaded = true;
        logger.info("Loaded native library {} from {}", LIBRARY_RESOURCE_NAME, tempLib.getAbsolutePath());
    }

    private static File extractToTempFile() {
        try (InputStream in = NativeLoadLibrary.class.getResourceAsStream("/" + LIBRARY_RESOURCE_NAME)) {
            if (in == null) {
                throw new IllegalStateException(
                        "Unable to locate " + LIBRARY_RESOURCE_NAME + " on the classpath");
            }
            File tempLib = File.createTempFile("libasn1c_jni", ".so");
            tempLib.deleteOnExit();
            try (OutputStream out = Files.newOutputStream(tempLib.toPath())) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempLib;
        } catch (IOException e) {
            throw new UnsatisfiedLinkError(
                    "Failed to extract " + LIBRARY_RESOURCE_NAME + " to a temp file: " + e.getMessage());
        }
    }
}
