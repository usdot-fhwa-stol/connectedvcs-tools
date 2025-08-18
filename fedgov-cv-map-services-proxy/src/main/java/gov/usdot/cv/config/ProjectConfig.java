package gov.usdot.cv.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ProjectConfig  {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(c -> {
            c.requireCsrfProtectionMatcher(request -> 
                request.getRequestURI().contains("/azuremap/api/proxy/tileset/")
            );
        });

        http.authorizeHttpRequests(
            c -> c.anyRequest().permitAll()
        );

        return http.build();
    }
}
