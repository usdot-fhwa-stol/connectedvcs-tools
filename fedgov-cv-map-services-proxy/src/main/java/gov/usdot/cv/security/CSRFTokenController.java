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
package gov.usdot.cv.security;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/security/api")
public class CSRFTokenController {
    private static Logger logger = Logger.getLogger(CSRFTokenController.class.getName());

    @GetMapping("/csrf-token")
    public ResponseEntity<Map<String, String>> getCsrfToken() {
        Object o = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
            .getRequest().getAttribute("_csrf");
        CsrfToken token = (CsrfToken) o;
        logger.info("Send CSRF token to client: " + token.getToken());
        Map<String, String> response = new HashMap<>();
        response.put("csrfToken", token.getToken());
        return ResponseEntity.ok(response);
    }
}
