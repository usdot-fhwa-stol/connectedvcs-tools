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
package gov.usdot.cv.fedgov_cv_map_georeferencing.frontend;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class GeoreferencerJsTest {

    private static String jsContent;
    private static String cssContent;

    @BeforeAll
    static void loadResources() throws IOException {
        jsContent = readClasspathResource("static/georeferencer/georeferencer.js");
        cssContent = readClasspathResource("static/georeferencer/georeferencer.css");
    }

    private static String readClasspathResource(String path) throws IOException {
        try (InputStream is = GeoreferencerJsTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(is, "Resource not found on classpath: " + path);
            byte[] bytes = new byte[is.available()];
            int totalRead = 0;
            while (totalRead < bytes.length) {
                int read = is.read(bytes, totalRead, bytes.length - totalRead);
                if (read == -1) break;
                totalRead += read;
            }
            return new String(bytes, 0, totalRead, StandardCharsets.UTF_8);
        }
    }

    // --- JS file presence and exports ---

    @Test
    void jsFileExistsAndIsNotEmpty() {
        assertFalse(jsContent.isEmpty(), "georeferencer.js should not be empty");
    }

    @Test
    void cssFileExistsAndIsNotEmpty() {
        assertFalse(cssContent.isEmpty(), "georeferencer.css should not be empty");
    }

    @Test
    void jsExportsInitGeoreferencer() {
        assertTrue(jsContent.contains("export function initGeoreferencer"),
                "JS must export initGeoreferencer function");
    }


    // --- DEFAULTS config keys ---

    @Test
    void jsDefaultsContainsRequiredKeys() {
        List<String> requiredKeys = Arrays.asList(
                "georefEndpoint", "csrfTokenUrl", "minGcps", "maxGcps",
                "initPollInterval", "initMaxAttempts",
                "zoomMin", "zoomMax", "zoomStep",
                "coordPrecision", "xhrTimeout"
        );
        for (String key : requiredKeys) {
            assertTrue(jsContent.contains(key),
                    "DEFAULTS must contain key: " + key);
        }
    }

    // --- Security checks ---

    @Test
    void noWindowGlobalPollution() {
        assertFalse(jsContent.contains("window._georef"),
                "Must not use window._georef* globals — use module-scope variables");
    }

    @Test
    void noInnerHtmlWithInterpolation() {
        Pattern xssPattern = Pattern.compile("innerHTML\\s*=\\s*`[^`]*\\$\\{");
        assertFalse(xssPattern.matcher(jsContent).find(),
                "Must not use innerHTML with template literal interpolation (XSS risk)");
    }

    // --- CSS expected selectors ---

    @Test
    void cssContainsExpectedSelectors() {
        List<String> selectors = Arrays.asList(
                ".georef-modal",
                ".georef-overlay-panel",
                ".georef-image-container",
                ".georef-image-marker",
                ".georef-status",
                ".georef-gcp-table",
                ".georef-map-add-mode"
        );
        for (String selector : selectors) {
            assertTrue(cssContent.contains(selector),
                    "CSS must contain selector: " + selector);
        }
    }

    // --- Module structure checks ---

    @Test
    void jsUsesModuleScopePendingState() {
        assertTrue(jsContent.contains("let pendingLonLat"),
                "Pending lon/lat must be module-scope");
        assertTrue(jsContent.contains("let pendingFeature"),
                "Pending feature must be module-scope");
    }

    @Test
    void jsUsesEventDelegationForDrag() {
        assertFalse(
                Pattern.compile("function setupImageMarkerDrag\\(marker,\\s*gcp\\)")
                        .matcher(jsContent).find(),
                "setupImageMarkerDrag must not take gcp param (use event delegation)");
    }

    @Test
    void jsCoordinateValidationPresent() {
        assertTrue(jsContent.contains("numValue < -180") || jsContent.contains("numValue > 180"),
                "Must validate longitude bounds in handleCellEdit");
        assertTrue(jsContent.contains("numValue < -90") || jsContent.contains("numValue > 90"),
                "Must validate latitude bounds in handleCellEdit");
    }

    @Test
    void jsScopedEnforceFocus() {
        assertTrue(jsContent.contains("georef_modal"),
                "enforceFocus override must be scoped to georef_modal");
        assertFalse(
                Pattern.compile("enforceFocus\\s*=\\s*function\\s*\\(\\)\\s*\\{\\s*\\}")
                        .matcher(jsContent).find(),
                "enforceFocus must not be unconditionally emptied");
    }

    // --- A1: response is validated before building the overlay ---

    @Test
    void jsValidatesGeoreferenceResultBeforeOverlay() {
        assertTrue(jsContent.contains("extentValid"),
                "startGeoreferencing must validate the extent before building the layer");
        assertTrue(jsContent.contains("did not return a usable result"),
                "Must surface an error when the response lacks a usable URL/extent");
        // The guard must run before the overlay extent/layer are constructed.
        int guardIdx = jsContent.indexOf("did not return a usable result");
        int transformIdx = jsContent.indexOf("transformExtent");
        assertTrue(guardIdx != -1 && transformIdx != -1 && guardIdx < transformIdx,
                "Result validation must precede extent transformation");
    }

    // --- A5: maxGcps default is not overwritten with undefined ---

    @Test
    void jsGuardsMaxCountConfig() {
        assertTrue(
                Pattern.compile("typeof config\\.gcp\\.maxCount === 'number'")
                        .matcher(jsContent).find(),
                "fetchBackendConfig must guard maxCount before overwriting the default");
    }

    // --- A6: POST has a client-side timeout ---

    @Test
    void jsPostHasAbortTimeout() {
        assertTrue(jsContent.contains("new AbortController()"),
                "POST must use an AbortController for timeout");
        assertTrue(jsContent.contains("controller.signal"),
                "fetch must pass the abort signal");
        assertTrue(jsContent.contains("AbortError"),
                "Abort/timeout must be handled with a friendly message");
    }

    // --- A3: enforceFocus patch installs exactly once ---

    @Test
    void jsEnforceFocusPatchedOnce() {
        assertTrue(jsContent.contains("enforceFocusPatched"),
                "enforceFocus patch must be guarded by a one-time install flag");
        assertTrue(jsContent.contains("function patchBootstrapEnforceFocus"),
                "enforceFocus patch must live in a named, guarded function");
    }

    // --- A2: markers anchored to an image stage element ---

    @Test
    void jsAndCssUseImageStage() {
        assertTrue(jsContent.contains("georef-image-stage"),
                "Markers must be appended to a .georef-image-stage wrapper");
        assertTrue(cssContent.contains(".georef-image-stage"),
                "CSS must define the .georef-image-stage positioning context");
    }
}
