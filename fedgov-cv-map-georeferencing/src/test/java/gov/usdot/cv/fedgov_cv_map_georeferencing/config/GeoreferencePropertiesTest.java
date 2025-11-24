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
package gov.usdot.cv.fedgov_cv_map_georeferencing.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GeoreferencePropertiesTest {

    @Autowired
    private GeoreferenceProperties properties;

    @Test
    void testPropertiesLoadedFromYaml() {
        // Assert that properties are loaded from application.yaml
        assertNotNull(properties);
        assertNotNull(properties.getImage());
        
        // Verify supported formats from YAML
        var supportedFormats = properties.getImage().getSupportedFormats();
        assertNotNull(supportedFormats);
        assertFalse(supportedFormats.isEmpty());
        assertTrue(supportedFormats.contains("image/png"));
        assertTrue(supportedFormats.contains("image/jpeg"));
        assertTrue(supportedFormats.contains("image/jpg"));
        
        // Verify max size from YAML
        assertEquals("50MB", properties.getImage().getMaxSize());
    }
    
    @Test
    void testSupportedFormatsAsSet() {
        Set<String> formatSet = properties.getImage().getSupportedFormatsAsSet();
        assertNotNull(formatSet);
        assertFalse(formatSet.isEmpty());
        assertTrue(formatSet.contains("image/png"));
        assertTrue(formatSet.contains("image/jpeg"));
        assertTrue(formatSet.contains("image/jpg"));
        assertEquals(3, formatSet.size());
    }
}