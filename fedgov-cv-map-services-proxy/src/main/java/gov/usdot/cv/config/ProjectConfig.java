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
