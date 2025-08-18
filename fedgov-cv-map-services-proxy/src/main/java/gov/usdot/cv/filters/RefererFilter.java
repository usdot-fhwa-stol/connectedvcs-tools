
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
package gov.usdot.cv.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;



public class RefererFilter implements Filter {
    private static final Logger logger = LogManager.getLogger(RefererFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String referer = httpRequest.getHeader("Referer");
            
            if (referer == null || referer.isEmpty()) {
                logger.warn("Blocked request without Referer header from IP: {}", httpRequest.getRemoteAddr());
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "Referer header required");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
