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
package gov.usdot.cv.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ProjectConfig {
    @Value("${csrf.protected.uris}")
    private String csrfProtectedURIs;

    /**
     * Configures the security filter chain to apply CSRF protection only to specified URIs.
     * 
     * @param http the HttpSecurity object to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String[] protectedUris = csrfProtectedURIs.split("\\s*,\\s*");
        http.csrf(c -> c.requireCsrfProtectionMatcher(request -> {
            String uri = request.getRequestURI();
            for (String protectedUri : protectedUris) {
                if (uri.contains(protectedUri)) {
                    return true;
                }
            }
            return false;
        }));
        http.authorizeHttpRequests(c -> c.anyRequest().permitAll());

        return http.build();
    }
}
