package gov.usdot.cv.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureConfig {    
    @Value("${azure.map.tileset.url}")
    private String tilesetUrl;
    
    @Value("${azure.map.api.key}")
    private String apiKey;

    public String getTilesetUrl() {
        return tilesetUrl;
    }

    public String getApiKey() {
        return apiKey;
    }
}