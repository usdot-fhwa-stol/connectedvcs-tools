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
package gov.usdot.cv.fedgov_cv_map_georeferencing.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ApiExceptionHandlerTest {

    @InjectMocks
    private ApiExceptionHandler apiExceptionHandler;

    @BeforeEach
    void setUp() {
        // Setup is handled by @InjectMocks
    }

    @Test
    void testHandleValidation_ReturnsCorrectErrorResponse() {
        // Arrange
        MethodParameter methodParameter = mock(MethodParameter.class);
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException methodArgumentNotValidException = 
            new MethodArgumentNotValidException(methodParameter, bindingResult);

        // Act
        ResponseEntity<?> response = apiExceptionHandler.handleValidation(methodArgumentNotValidException);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        Object responseBody = response.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, String> errorMap = (Map<String, String>) responseBody;
        assertEquals("validation_failed", errorMap.get("error"));
    }

    @Test
    void testHandleValidation_ResponseStructure_IsCorrect() {
        // Arrange
        MethodParameter methodParameter = mock(MethodParameter.class);
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException methodArgumentNotValidException = 
            new MethodArgumentNotValidException(methodParameter, bindingResult);

        // Act
        ResponseEntity<?> response = apiExceptionHandler.handleValidation(methodArgumentNotValidException);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.containsKey("error"));
        assertEquals(1, responseBody.size());
    }

    @Test
    void testHandleGeneric_RuntimeException_ReturnsCorrectErrorResponse() {
        // Arrange
        RuntimeException runtimeException = new RuntimeException("Something went wrong");

        // Act
        ResponseEntity<?> response = apiExceptionHandler.handleGeneric(runtimeException);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        Object responseBody = response.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, String> errorMap = (Map<String, String>) responseBody;
        assertEquals("Something went wrong", errorMap.get("error"));
    }

    @Test
    void testHandleGeneric_IllegalArgumentException_ReturnsCorrectErrorResponse() {
        // Arrange
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Invalid parameter");

        // Act
        ResponseEntity<?> response = apiExceptionHandler.handleGeneric(illegalArgumentException);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("Invalid parameter", responseBody.get("error"));
    }

    @Test
    void testHandleGeneric_NullPointerException_ReturnsCorrectErrorResponse() {
        // Arrange
        NullPointerException nullPointerException = new NullPointerException("Null value encountered");

        // Act
        ResponseEntity<?> response = apiExceptionHandler.handleGeneric(nullPointerException);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("Null value encountered", responseBody.get("error"));
    }

    @Test
    void testHandleGeneric_ExceptionWithNullMessage_HandlesGracefully() {
        // Arrange
        Exception exceptionWithNullMessage = new Exception((String) null);

        // Act
        ResponseEntity<?> response = apiExceptionHandler.handleGeneric(exceptionWithNullMessage);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertNull(responseBody.get("error")); // Should be null as exception message is null
    }

    @Test
    void testHandleGeneric_ResponseStructure_IsCorrect() {
        // Arrange
        Exception genericException = new Exception("Test error message");

        // Act
        ResponseEntity<?> response = apiExceptionHandler.handleGeneric(genericException);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.containsKey("error"));
        assertEquals(1, responseBody.size());
        assertEquals("Test error message", responseBody.get("error"));
    }

    @Test
    void testHandleGeneric_EmptyMessage_ReturnsEmptyErrorMessage() {
        // Arrange
        Exception emptyMessageException = new Exception("");

        // Act
        ResponseEntity<?> response = apiExceptionHandler.handleGeneric(emptyMessageException);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("", responseBody.get("error"));
    }

    @Test
    void testHandleGeneric_LongErrorMessage_HandlesLongMessages() {
        // Arrange
        String longMessage = "This is a very long error message that should still be handled correctly by the exception handler without any issues or truncation problems";
        Exception longMessageException = new Exception(longMessage);

        // Act
        ResponseEntity<?> response = apiExceptionHandler.handleGeneric(longMessageException);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals(longMessage, responseBody.get("error"));
    }

    @Test
    void testApiExceptionHandler_Construction_CreatesValidInstance() {
        // Arrange & Act
        ApiExceptionHandler handler = new ApiExceptionHandler();

        // Assert
        assertNotNull(handler);
    }

    @Test
    void testBothHandlers_DifferentHttpStatusCodes_ReturnCorrectStatuses() {
        // Arrange
        Exception genericException = new Exception("Generic error");
        MethodParameter methodParameter = mock(MethodParameter.class);
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException validationException = 
            new MethodArgumentNotValidException(methodParameter, bindingResult);

        // Act
        ResponseEntity<?> validationResponse = apiExceptionHandler.handleValidation(validationException);
        ResponseEntity<?> genericResponse = apiExceptionHandler.handleGeneric(genericException);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, validationResponse.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, genericResponse.getStatusCode());
        assertNotEquals(validationResponse.getStatusCode(), genericResponse.getStatusCode());
    }
}