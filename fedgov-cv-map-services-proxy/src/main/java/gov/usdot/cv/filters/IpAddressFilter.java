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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import gov.usdot.cv.utils.ClientRequestInfoParser;

@Component
public class IpAddressFilter implements Filter{
    private static final Logger logger = LogManager.getLogger(IpAddressFilter.class);
    @Override
    public void init(FilterConfig filterConfig){
        logger.info("IpAddressFilter init");
    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
                HttpServletRequest req = (HttpServletRequest)request;
        String ipAddress = new ClientRequestInfoParser(req).getClientIPAddress();
        logger.info("Client IP {}", ipAddress);
        chain.doFilter(request, response);
    }
    @Override
    public void destroy() {
        logger.info("IpAddressFilter destroy");
    }
}
