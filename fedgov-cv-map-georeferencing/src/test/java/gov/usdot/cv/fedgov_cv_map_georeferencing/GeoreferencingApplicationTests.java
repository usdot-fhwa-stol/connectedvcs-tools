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
package gov.usdot.cv.fedgov_cv_map_georeferencing;

import gov.usdot.cv.fedgov_cv_map_georeferencing.controller.GeoreferenceController;
import gov.usdot.cv.fedgov_cv_map_georeferencing.service.GeoreferenceService;
import gov.usdot.cv.fedgov_cv_map_georeferencing.exception.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GeoreferencingApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void contextLoads() {
		// Verify the application context loads successfully
		assertNotNull(applicationContext);
	}

	@Test
	void testMainApplicationClass_Exists() {
		// Verify the main application class can be loaded
		assertDoesNotThrow(() -> {
			GeoreferencingApplication.class.getDeclaredConstructor().newInstance();
		});
	}

	@Test
	void testRequiredBeansAreLoaded() {
		// Verify that essential beans are loaded in the application context
		assertNotNull(applicationContext.getBean(GeoreferenceController.class));
		assertNotNull(applicationContext.getBean(GeoreferenceService.class));
		assertNotNull(applicationContext.getBean(ApiExceptionHandler.class));
	}

	@Test
	void testGeoreferenceControllerBean_IsCorrectlyWired() {
		// Verify the controller bean is properly configured
		GeoreferenceController controller = applicationContext.getBean(GeoreferenceController.class);
		assertNotNull(controller);
	}

	@Test
	void testGeoreferenceServiceBean_IsCorrectlyWired() {
		// Verify the service bean is properly configured
		GeoreferenceService service = applicationContext.getBean(GeoreferenceService.class);
		assertNotNull(service);
	}

	@Test
	void testApiExceptionHandlerBean_IsCorrectlyWired() {
		// Verify the exception handler bean is properly configured
		ApiExceptionHandler exceptionHandler = applicationContext.getBean(ApiExceptionHandler.class);
		assertNotNull(exceptionHandler);
	}

	@Test
	void testApplicationConfiguration_HasCorrectBeanCount() {
		// Verify that we have the expected number of our custom beans
		String[] controllerBeans = applicationContext.getBeanNamesForType(GeoreferenceController.class);
		String[] serviceBeans = applicationContext.getBeanNamesForType(GeoreferenceService.class);
		String[] exceptionHandlerBeans = applicationContext.getBeanNamesForType(ApiExceptionHandler.class);
		
		assertEquals(1, controllerBeans.length, "Should have exactly one GeoreferenceController bean");
		assertEquals(1, serviceBeans.length, "Should have exactly one GeoreferenceService bean");
		assertEquals(1, exceptionHandlerBeans.length, "Should have exactly one ApiExceptionHandler bean");
	}

}
