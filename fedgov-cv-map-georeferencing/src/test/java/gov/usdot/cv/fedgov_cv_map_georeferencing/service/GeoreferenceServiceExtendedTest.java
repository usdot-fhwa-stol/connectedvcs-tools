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
package gov.usdot.cv.fedgov_cv_map_georeferencing.service;

import gov.usdot.cv.fedgov_cv_map_georeferencing.config.GeoreferenceProperties;
import gov.usdot.cv.fedgov_cv_map_georeferencing.dto.GCP;
import gov.usdot.cv.fedgov_cv_map_georeferencing.gdal.GdalException;
import gov.usdot.cv.fedgov_cv_map_georeferencing.gdal.GdalFacade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Extended unit tests for GeoreferenceService.
 *
 * Targets branches NOT covered by the original GeoreferenceServiceTest:
 *   - parseSizeToBytes: KB, GB, bare-bytes, invalid, null/empty
 *   - validateImageFormat: empty set (accept-all), null/empty contentType
 *   - process(): max-GCP exceeded, unsupported format, null filename,
 *                size-exceeds-limit, GCP with null geo coords, GCP with blank pointId
 *   - createMockResponse: verifies processedImageBytes is empty byte[]
 *   - extractActualExtentFromGdalInfo: cornerCoordinates path, null gdalinfo_json,
 *                                      malformed JSON fallback, exception path
 *   - webMercatorXToLon / webMercatorYToLat: known coordinate conversion values
 *   - cleanGdalJson / removeCoordinateSystemDirectly: exercised via extractActualExtent
 *   - Constructor: validateGdalUtilities throwing GdalException (warn, no rethrow)
 */
@ExtendWith(MockitoExtension.class)
class GeoreferenceServiceExtendedTest {

    // -------------------------------------------------------------------------
    // Shared mocks / fixtures
    // -------------------------------------------------------------------------

    @Mock
    private GdalFacade gdalFacade;

    @Mock
    private GeoreferenceProperties georeferenceProperties;

    @Mock
    private GeoreferenceProperties.Gcp gcpProperties;

    @Mock
    private GeoreferenceProperties.Image imageProperties;

    private GeoreferenceService service;

    /** Minimum valid GCP list (6 points matching default minCount). */
    private List<GCP> validGcps;

    @BeforeEach
    void setUp() throws GdalException {
        lenient().when(georeferenceProperties.getGcp()).thenReturn(gcpProperties);
        lenient().when(georeferenceProperties.getImage()).thenReturn(imageProperties);
        lenient().when(gcpProperties.getMinCount()).thenReturn(6);
        lenient().when(gcpProperties.getMaxCount()).thenReturn(10);
        lenient().when(imageProperties.getSupportedFormats())
                 .thenReturn(Arrays.asList("image/jpeg", "image/png", "image/tiff"));
        lenient().when(imageProperties.getSupportedFormatsAsSet())
                 .thenReturn(new HashSet<>(Arrays.asList("image/jpeg", "image/png", "image/tiff")));
        lenient().when(imageProperties.getMaxSize()).thenReturn("50MB");

        lenient().doNothing().when(gdalFacade).validateGdalUtilities();
        lenient().when(gdalFacade.getGdalVersion()).thenReturn("GDAL 3.4.1 (mocked)");
        lenient().when(gdalFacade.isGdalAvailable()).thenReturn(false);

        service = new GeoreferenceService(gdalFacade, georeferenceProperties);

        validGcps = Arrays.asList(
            new GCP("P1", 100, 200, -77.10, 38.90),
            new GCP("P2", 200, 300, -77.11, 38.91),
            new GCP("P3", 300, 400, -77.12, 38.92),
            new GCP("P4", 400, 500, -77.13, 38.93),
            new GCP("P5", 500, 600, -77.14, 38.94),
            new GCP("P6", 600, 700, -77.15, 38.95)
        );
    }

    // =========================================================================
    // 1.  Constructor – validateGdalUtilities throws GdalException
    //     Expected: service still constructs (exception is only logged as WARN)
    // =========================================================================

    @Test
    void constructor_WhenValidateGdalThrows_ServiceStillInitialises() throws GdalException {
        doThrow(new GdalException("GDAL unavailable")).when(gdalFacade).validateGdalUtilities();

        // Should NOT propagate the exception
        assertDoesNotThrow(() -> new GeoreferenceService(gdalFacade, georeferenceProperties));
    }

    // =========================================================================
    // 2.  process() – input-validation branches not covered by original tests
    // =========================================================================

    @Test
    void process_NullImageFilename_ThrowsIllegalArgument() {
        // getOriginalFilename() returns null
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> service.process(mockFile, validGcps));
    }

    @Test
    void process_BlankImageFilename_ThrowsIllegalArgument() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("   ");

        assertThrows(IllegalArgumentException.class,
                () -> service.process(mockFile, validGcps));
    }

    @Test
    void process_NullContentType_ThrowsIllegalArgument() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("image.jpg");
        when(mockFile.getContentType()).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> service.process(mockFile, validGcps));
    }

    @Test
    void process_BlankContentType_ThrowsIllegalArgument() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("image.jpg");
        when(mockFile.getContentType()).thenReturn("  ");

        assertThrows(IllegalArgumentException.class,
                () -> service.process(mockFile, validGcps));
    }

    @Test
    void process_UnsupportedContentType_ThrowsIllegalArgument() {
        MockMultipartFile f = new MockMultipartFile(
                "image", "photo.bmp", "image/bmp", "data".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.process(f, validGcps));

        assertTrue(ex.getMessage().contains("Unsupported image format"));
        assertTrue(ex.getMessage().contains("image/bmp"));
    }

    @Test
    void process_ImageExceedsMaxSize_ThrowsIllegalArgument() {
        // Max is "50MB"; supply a file that claims to be 60 MB (60 * 1024 * 1024 bytes)
        long oversizeBytes = 60L * 1024 * 1024;
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("big.jpg");
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getSize()).thenReturn(oversizeBytes);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.process(mockFile, validGcps));

        assertTrue(ex.getMessage().contains("maximum allowed size"));
    }

    @Test
    void process_TooManyGcps_ThrowsIllegalArgument() {
        // maxCount is 10; provide 11
        List<GCP> tooMany = Arrays.asList(
            new GCP("P1",  10, 20, -77.10, 38.90),
            new GCP("P2",  20, 30, -77.11, 38.91),
            new GCP("P3",  30, 40, -77.12, 38.92),
            new GCP("P4",  40, 50, -77.13, 38.93),
            new GCP("P5",  50, 60, -77.14, 38.94),
            new GCP("P6",  60, 70, -77.15, 38.95),
            new GCP("P7",  70, 80, -77.16, 38.96),
            new GCP("P8",  80, 90, -77.17, 38.97),
            new GCP("P9",  90, 100, -77.18, 38.98),
            new GCP("P10", 100, 110, -77.19, 38.99),
            new GCP("P11", 110, 120, -77.20, 39.00)
        );

        MockMultipartFile f = new MockMultipartFile(
                "image", "map.jpg", "image/jpeg", "data".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.process(f, tooMany));

        assertTrue(ex.getMessage().contains("No more than 10 ground control points"));
        assertTrue(ex.getMessage().contains("Provided: 11"));
    }

    @Test
    void process_GcpWithEmptyPointId_ThrowsIllegalArgument() {
        List<GCP> gcpsWithEmptyId = Arrays.asList(
            new GCP("  ", 100, 200, -77.10, 38.90),  // blank pointId
            new GCP("P2",  200, 300, -77.11, 38.91),
            new GCP("P3",  300, 400, -77.12, 38.92),
            new GCP("P4",  400, 500, -77.13, 38.93),
            new GCP("P5",  500, 600, -77.14, 38.94),
            new GCP("P6",  600, 700, -77.15, 38.95)
        );

        MockMultipartFile f = new MockMultipartFile(
                "image", "map.jpg", "image/jpeg", "data".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.process(f, gcpsWithEmptyId));

        assertEquals("All ground control points must have valid point IDs", ex.getMessage());
    }

    @Test
    void process_GcpWithNullGeoCoordinates_ThrowsIllegalArgument() {
        List<GCP> gcpsWithNullGeo = Arrays.asList(
            new GCP("P1", 100, 200, null, 38.90),  // null longitude
            new GCP("P2", 200, 300, -77.11, 38.91),
            new GCP("P3", 300, 400, -77.12, 38.92),
            new GCP("P4", 400, 500, -77.13, 38.93),
            new GCP("P5", 500, 600, -77.14, 38.94),
            new GCP("P6", 600, 700, -77.15, 38.95)
        );

        MockMultipartFile f = new MockMultipartFile(
                "image", "map.jpg", "image/jpeg", "data".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.process(f, gcpsWithNullGeo));

        assertEquals("All ground control points must have valid geographic coordinates", ex.getMessage());
    }

    @Test
    void process_GcpWithNullLatitude_ThrowsIllegalArgument() {
        List<GCP> gcpsWithNullLat = Arrays.asList(
            new GCP("P1", 100, 200, -77.10, null),  // null latitude
            new GCP("P2", 200, 300, -77.11, 38.91),
            new GCP("P3", 300, 400, -77.12, 38.92),
            new GCP("P4", 400, 500, -77.13, 38.93),
            new GCP("P5", 500, 600, -77.14, 38.94),
            new GCP("P6", 600, 700, -77.15, 38.95)
        );

        MockMultipartFile f = new MockMultipartFile(
                "image", "map.jpg", "image/jpeg", "data".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.process(f, gcpsWithNullLat));

        assertEquals("All ground control points must have valid geographic coordinates", ex.getMessage());
    }

    // =========================================================================
    // 3.  validateImageFormat – empty supported-formats set means accept all
    // =========================================================================

    @Test
    void process_WithEmptySupportedFormats_AcceptsAnyContentType() throws Exception {
        // When no formats are configured, the service must accept all content types
        when(imageProperties.getSupportedFormatsAsSet()).thenReturn(Collections.emptySet());

        MockMultipartFile f = new MockMultipartFile(
                "image", "weird.xyz", "image/weird-format", "data".getBytes());

        // Should not throw on format validation; falls through to mock response
        Map<String, Object> result = service.process(f, validGcps);

        assertEquals("mock_processed", result.get("status"));
    }

    // =========================================================================
    // 4.  createMockResponse – verify the empty byte[] and all expected keys
    // =========================================================================

    @Test
    void process_GdalNotAvailable_MockResponseHasEmptyProcessedImageBytes() throws Exception {
        when(gdalFacade.isGdalAvailable()).thenReturn(false);

        MockMultipartFile f = new MockMultipartFile(
                "image", "map.png", "image/png", "content".getBytes());

        Map<String, Object> result = service.process(f, validGcps);

        assertEquals("mock_processed", result.get("status"));
        assertNotNull(result.get("processedImageBytes"));
        byte[] bytes = (byte[]) result.get("processedImageBytes");
        assertEquals(0, bytes.length, "Mock response must return an empty byte array");
        assertTrue(result.get("message").toString().contains("GDAL not available"));
    }

    // =========================================================================
    // 5.  parseSizeToBytes – private method tested via process() size validation
    //     We trigger each unit-suffix branch by setting maxSize and providing an
    //     oversized file relative to each limit.
    // =========================================================================

    @Test
    void process_MaxSizeInKB_ExceedingFileSizeThrows() {
        when(imageProperties.getMaxSize()).thenReturn("10KB");

        long oversizeBytes = 11L * 1024; // 11 KB
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("map.png");
        when(mockFile.getContentType()).thenReturn("image/png");
        when(mockFile.getSize()).thenReturn(oversizeBytes);

        assertThrows(IllegalArgumentException.class,
                () -> service.process(mockFile, validGcps));
    }

    @Test
    void process_MaxSizeInGB_ExceedingFileSizeThrows() {
        when(imageProperties.getMaxSize()).thenReturn("1GB");

        long oversizeBytes = 2L * 1024 * 1024 * 1024; // 2 GB
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("map.tiff");
        when(mockFile.getContentType()).thenReturn("image/tiff");
        when(mockFile.getSize()).thenReturn(oversizeBytes);

        assertThrows(IllegalArgumentException.class,
                () -> service.process(mockFile, validGcps));
    }

    @Test
    void process_MaxSizeInBareBytes_ExceedingFileSizeThrows() {
        when(imageProperties.getMaxSize()).thenReturn("100B");

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("map.png");
        when(mockFile.getContentType()).thenReturn("image/png");
        when(mockFile.getSize()).thenReturn(200L);  // 200 bytes > 100

        assertThrows(IllegalArgumentException.class,
                () -> service.process(mockFile, validGcps));
    }

    @Test
    void process_MaxSizeInKB_WithinLimit_DoesNotThrow() throws Exception {
        when(imageProperties.getMaxSize()).thenReturn("10KB");

        byte[] smallContent = new byte[5 * 1024]; // 5 KB
        MockMultipartFile f = new MockMultipartFile(
                "image", "map.png", "image/png", smallContent);

        // Should pass size validation and reach mock response
        assertDoesNotThrow(() -> service.process(f, validGcps));
    }

    // =========================================================================
    // 6.  parseSizeToBytes invalid format — exercised via reflection because
    //     there's no public entry point that surfaces a bad format string with
    //     a controlled assertion; we use reflection to test the private method.
    // =========================================================================

    @Test
    void parseSizeToBytes_InvalidFormat_ThrowsIllegalArgument() throws Exception {
        Method m = GeoreferenceService.class
                .getDeclaredMethod("parseSizeToBytes", String.class);
        m.setAccessible(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> {
                    try {
                        m.invoke(service, "NOT_A_SIZE");
                    } catch (java.lang.reflect.InvocationTargetException ite) {
                        throw ite.getCause();
                    }
                });

        assertTrue(ex.getMessage().contains("Invalid size format"));
    }

    @Test
    void parseSizeToBytes_NullInput_ThrowsIllegalArgument() throws Exception {
        Method m = GeoreferenceService.class
                .getDeclaredMethod("parseSizeToBytes", String.class);
        m.setAccessible(true);

        assertThrows(Throwable.class, () -> {
            try {
                m.invoke(service, (String) null);
            } catch (java.lang.reflect.InvocationTargetException ite) {
                throw ite.getCause();
            }
        });
    }

    // =========================================================================
    // 7.  webMercatorXToLon / webMercatorYToLat – via reflection
    //     These helpers have known reference values we can verify.
    // =========================================================================

    @Test
    void webMercatorXToLon_AtOrigin_ReturnsZero() throws Exception {
        Method m = GeoreferenceService.class
                .getDeclaredMethod("webMercatorXToLon", double.class);
        m.setAccessible(true);

        double result = (double) m.invoke(service, 0.0);
        assertEquals(0.0, result, 1e-10);
    }

    @Test
    void webMercatorXToLon_AtMaxExtent_Returns180() throws Exception {
        Method m = GeoreferenceService.class
                .getDeclaredMethod("webMercatorXToLon", double.class);
        m.setAccessible(true);

        // MAX_EXTENT = 20037508.34 → should map to exactly 180°
        double result = (double) m.invoke(service, 20037508.34);
        assertEquals(180.0, result, 1e-6);
    }

    @Test
    void webMercatorYToLat_AtOrigin_ReturnsZero() throws Exception {
        Method m = GeoreferenceService.class
                .getDeclaredMethod("webMercatorYToLat", double.class);
        m.setAccessible(true);

        double result = (double) m.invoke(service, 0.0);
        assertEquals(0.0, result, 1e-10);
    }

    @Test
    void webMercatorYToLat_PositiveY_ReturnsPositiveLatitude() throws Exception {
        Method m = GeoreferenceService.class
                .getDeclaredMethod("webMercatorYToLat", double.class);
        m.setAccessible(true);

        double result = (double) m.invoke(service, 5000000.0);
        assertTrue(result > 0.0, "Positive Y should map to a positive latitude");
        assertTrue(result < 90.0, "Latitude must be less than 90°");
    }

    // =========================================================================
    // 8.  extractActualExtentFromGdalInfo – cornerCoordinates path
    //     (GDAL not available → GCP-fallback, but we also test GDAL mode paths)
    // =========================================================================

    @Test
    void process_GdalWithCornerCoordinatesJson_ExtractsWgs84ExtentFromConversion()
            throws Exception {
        // Use GDAL path but return cornerCoordinates (Web Mercator) instead of wgs84Extent
        when(gdalFacade.isGdalAvailable()).thenReturn(true);
        doNothing().when(gdalFacade).createVrtWithGcps(any(), any(), any(), any());
        doNothing().when(gdalFacade).warpImage(any(), any(), any(), any(), any());

        doAnswer(inv -> {
            Path out = inv.getArgument(1);
            Files.createDirectories(out.getParent());
            Files.write(out, "png".getBytes());
            return null;
        }).when(gdalFacade).translateImage(any(), any(), eq("PNG"), eq("-scale"));

        // Provide cornerCoordinates JSON (Web Mercator values near Washington DC)
        String cornerJson = "{" +
            "\"cornerCoordinates\":{" +
            "\"upperLeft\":[-8590000.0,4720000.0]," +
            "\"lowerRight\":[-8580000.0,4710000.0]}" +
            "}";

        Map<String, Object> mockInfo = new HashMap<>();
        mockInfo.put("gdalinfo_json", cornerJson);
        when(gdalFacade.getComprehensiveImageInfo(any())).thenReturn(mockInfo);

        MockMultipartFile f = new MockMultipartFile(
                "image", "map.png", "image/png", "data".getBytes());

        Map<String, Object> result = service.process(f, validGcps);

        assertEquals("processed_gdal_facade", result.get("status"));

        @SuppressWarnings("unchecked")
        Map<String, Double> extent = (Map<String, Double>) result.get("extent");
        assertNotNull(extent);
        // Converted West Mercator X ≈ -77° longitude (rough assertion)
        assertTrue(extent.get("minLongitude") < 0,
                "Longitude west of prime meridian should be negative");
        assertTrue(extent.get("minLatitude") < extent.get("maxLatitude"),
                "minLatitude must be less than maxLatitude");
    }

    @Test
    void process_GdalWithNullGdalinfoJson_FallsBackToGcpExtent() throws Exception {
        when(gdalFacade.isGdalAvailable()).thenReturn(true);
        doNothing().when(gdalFacade).createVrtWithGcps(any(), any(), any(), any());
        doNothing().when(gdalFacade).warpImage(any(), any(), any(), any(), any());

        doAnswer(inv -> {
            Path out = inv.getArgument(1);
            Files.createDirectories(out.getParent());
            Files.write(out, "png".getBytes());
            return null;
        }).when(gdalFacade).translateImage(any(), any(), eq("PNG"), eq("-scale"));

        // No gdalinfo_json key – triggers GCP-based fallback
        when(gdalFacade.getComprehensiveImageInfo(any())).thenReturn(new HashMap<>());

        MockMultipartFile f = new MockMultipartFile(
                "image", "map.png", "image/png", "data".getBytes());

        Map<String, Object> result = service.process(f, validGcps);

        assertEquals("processed_gdal_facade", result.get("status"));

        @SuppressWarnings("unchecked")
        Map<String, Double> extent = (Map<String, Double>) result.get("extent");
        // Fallback must still yield a valid bounding box derived from GCPs
        assertEquals(-77.10, extent.get("maxLongitude"), 1e-6);
        assertEquals(-77.15, extent.get("minLongitude"), 1e-6);
    }

    @Test
    void process_GdalWithMalformedJson_FallsBackToGcpExtent() throws Exception {
        when(gdalFacade.isGdalAvailable()).thenReturn(true);
        doNothing().when(gdalFacade).createVrtWithGcps(any(), any(), any(), any());
        doNothing().when(gdalFacade).warpImage(any(), any(), any(), any(), any());

        doAnswer(inv -> {
            Path out = inv.getArgument(1);
            Files.createDirectories(out.getParent());
            Files.write(out, "png".getBytes());
            return null;
        }).when(gdalFacade).translateImage(any(), any(), eq("PNG"), eq("-scale"));

        // Intentionally malformed JSON
        Map<String, Object> mockInfo = new HashMap<>();
        mockInfo.put("gdalinfo_json", "{this is not valid json @@@@}");
        when(gdalFacade.getComprehensiveImageInfo(any())).thenReturn(mockInfo);

        MockMultipartFile f = new MockMultipartFile(
                "image", "map.png", "image/png", "data".getBytes());

        // Should NOT throw – falls back to GCP extent
        Map<String, Object> result = service.process(f, validGcps);

        assertEquals("processed_gdal_facade", result.get("status"));
        assertNotNull(result.get("extent"));
    }

    @Test
    void process_GdalWithCoordinateSystemInJson_StripsItAndParsesRest() throws Exception {
        // Tests that cleanGdalJson / removeCoordinateSystemDirectly executes
        when(gdalFacade.isGdalAvailable()).thenReturn(true);
        doNothing().when(gdalFacade).createVrtWithGcps(any(), any(), any(), any());
        doNothing().when(gdalFacade).warpImage(any(), any(), any(), any(), any());

        doAnswer(inv -> {
            Path out = inv.getArgument(1);
            Files.createDirectories(out.getParent());
            Files.write(out, "png".getBytes());
            return null;
        }).when(gdalFacade).translateImage(any(), any(), eq("PNG"), eq("-scale"));

        // JSON with a coordinateSystem block followed by wgs84Extent
        String jsonWithCRS = "{" +
            "\"coordinateSystem\":{\"wkt\":\"PROJCRS[\\\"WGS 84 / Pseudo-Mercator\\\"]\"}," +
            "\"wgs84Extent\":{\"coordinates\":[[[-77.15,38.95],[-77.15,38.90],[-77.10,38.90],[-77.10,38.95],[-77.15,38.95]]]}" +
            "}";

        Map<String, Object> mockInfo = new HashMap<>();
        mockInfo.put("gdalinfo_json", jsonWithCRS);
        when(gdalFacade.getComprehensiveImageInfo(any())).thenReturn(mockInfo);

        MockMultipartFile f = new MockMultipartFile(
                "image", "map.png", "image/png", "data".getBytes());

        Map<String, Object> result = service.process(f, validGcps);

        @SuppressWarnings("unchecked")
        Map<String, Double> extent = (Map<String, Double>) result.get("extent");
        assertNotNull(extent);
        assertEquals(-77.15, extent.get("minLongitude"), 1e-6);
        assertEquals(-77.10, extent.get("maxLongitude"), 1e-6);
        assertEquals(38.90,  extent.get("minLatitude"), 1e-6);
        assertEquals(38.95,  extent.get("maxLatitude"), 1e-6);
    }

    // =========================================================================
    // 9.  process() – GDAL processing fields: processingTimestamp, projection keys
    // =========================================================================

    @Test
    void process_WithGdal_ResultContainsProjectionAndTimestampFields() throws Exception {
        when(gdalFacade.isGdalAvailable()).thenReturn(true);
        doNothing().when(gdalFacade).createVrtWithGcps(any(), any(), any(), any());
        doNothing().when(gdalFacade).warpImage(any(), any(), any(), any(), any());

        doAnswer(inv -> {
            Path out = inv.getArgument(1);
            Files.createDirectories(out.getParent());
            Files.write(out, "png".getBytes());
            return null;
        }).when(gdalFacade).translateImage(any(), any(), eq("PNG"), eq("-scale"));

        Map<String, Object> mockInfo = new HashMap<>();
        mockInfo.put("gdalinfo_json",
            "{\"wgs84Extent\":{\"coordinates\":[[[-77.15,38.95],[-77.15,38.90]," +
            "[-77.10,38.90],[-77.10,38.95],[-77.15,38.95]]]}}");
        when(gdalFacade.getComprehensiveImageInfo(any())).thenReturn(mockInfo);

        MockMultipartFile f = new MockMultipartFile(
                "image", "map.png", "image/png", "data".getBytes());

        Map<String, Object> result = service.process(f, validGcps);

        assertTrue(result.containsKey("processingTimestamp"),
                "GDAL path must include processingTimestamp");
        assertEquals("EPSG:4326", result.get("extentProjection"));
        assertEquals("EPSG:3857", result.get("coordinateSystem"));
        assertNotNull(result.get("processedImageSize"));
    }

    // =========================================================================
    // 10. process() – exactly at boundary values for GCP count
    // =========================================================================

    @Test
    void process_ExactlyMinGcpCount_Succeeds() throws Exception {
        // 6 GCPs == minCount – must pass
        Map<String, Object> result = service.process(
            new MockMultipartFile("image", "map.png", "image/png", "data".getBytes()),
            validGcps   // exactly 6
        );
        assertNotNull(result);
    }

    @Test
    void process_ExactlyMaxGcpCount_Succeeds() throws Exception {
        // 10 GCPs == maxCount – must pass
        List<GCP> tenGcps = Arrays.asList(
            new GCP("P1",  10,  20, -77.10, 38.90),
            new GCP("P2",  20,  30, -77.11, 38.91),
            new GCP("P3",  30,  40, -77.12, 38.92),
            new GCP("P4",  40,  50, -77.13, 38.93),
            new GCP("P5",  50,  60, -77.14, 38.94),
            new GCP("P6",  60,  70, -77.15, 38.95),
            new GCP("P7",  70,  80, -77.16, 38.96),
            new GCP("P8",  80,  90, -77.17, 38.97),
            new GCP("P9",  90, 100, -77.18, 38.98),
            new GCP("P10",100, 110, -77.19, 38.99)
        );

        Map<String, Object> result = service.process(
            new MockMultipartFile("image", "map.png", "image/png", "data".getBytes()),
            tenGcps
        );
        assertEquals(10, result.get("gcpCount"));
    }

    // =========================================================================
    // 11. process() – image file without extension defaults to .tif in GDAL path
    // =========================================================================

    @Test
    void process_FilenameWithoutExtension_DoesNotThrow() throws Exception {
        when(gdalFacade.isGdalAvailable()).thenReturn(true);
        doNothing().when(gdalFacade).createVrtWithGcps(any(), any(), any(), any());
        doNothing().when(gdalFacade).warpImage(any(), any(), any(), any(), any());

        doAnswer(inv -> {
            Path out = inv.getArgument(1);
            Files.createDirectories(out.getParent());
            Files.write(out, "png".getBytes());
            return null;
        }).when(gdalFacade).translateImage(any(), any(), eq("PNG"), eq("-scale"));

        Map<String, Object> mockInfo = new HashMap<>();
        mockInfo.put("gdalinfo_json",
            "{\"wgs84Extent\":{\"coordinates\":[[[-77.15,38.95],[-77.15,38.90]," +
            "[-77.10,38.90],[-77.10,38.95],[-77.15,38.95]]]}}");
        when(gdalFacade.getComprehensiveImageInfo(any())).thenReturn(mockInfo);

        // No dot in filename → code defaults to ".tif"
        MockMultipartFile f = new MockMultipartFile(
                "image", "noextension", "image/png", "data".getBytes());

        assertDoesNotThrow(() -> service.process(f, validGcps));
    }
}