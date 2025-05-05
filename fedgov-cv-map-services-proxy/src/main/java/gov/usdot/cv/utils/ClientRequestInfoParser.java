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
package gov.usdot.cv.utils;

import javax.servlet.http.HttpServletRequest;

public class ClientRequestInfoParser {
    private HttpServletRequest request;


    public ClientRequestInfoParser(HttpServletRequest request) {
        this.request = request;
    }

    public String getClientIPAddress(){
        String ipAddress = this.request.getHeader("X-Forwarded-For");
        if (ipAddress == null) {
            ipAddress = this.request.getRemoteAddr();
        }
        return ipAddress;
    }
}
