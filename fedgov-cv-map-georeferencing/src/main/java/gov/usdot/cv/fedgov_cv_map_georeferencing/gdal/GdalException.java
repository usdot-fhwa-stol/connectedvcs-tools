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
package gov.usdot.cv.fedgov_cv_map_georeferencing.gdal;

/**
 * Exception thrown when GDAL operations fail.
 * 
 * This exception encapsulates all errors that can occur during GDAL command execution,
 * including:
 * - GDAL utilities not being available
 * - Command execution failures
 * - File I/O errors
 * - Invalid parameters or configurations
 * 
 * The exception includes detailed error messages and maintains the cause chain
 * for proper debugging and error reporting.
 */
public class GdalException extends Exception {
    
    /**
     * Constructs a new GdalException with the specified detail message.
     * 
     * @param message the detail message
     */
    public GdalException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new GdalException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public GdalException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new GdalException with the specified cause.
     * 
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public GdalException(Throwable cause) {
        super(cause);
    }
}