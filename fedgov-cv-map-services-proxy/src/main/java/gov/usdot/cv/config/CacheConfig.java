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

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

import gov.usdot.cv.gnis.services.GnisLookupService;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("mapTilesCache");
        cacheManager.setCaffeine(Caffeine.newBuilder().recordStats());
        // GNIS codes rarely change, so lookups and searches are kept for a day to avoid repeat outside calls
        cacheManager.registerCustomCache(GnisLookupService.LOOKUP_CACHE,
            Caffeine.newBuilder().maximumSize(10000).expireAfterWrite(1, TimeUnit.DAYS).recordStats().build());
        cacheManager.registerCustomCache(GnisLookupService.SEARCH_CACHE,
            Caffeine.newBuilder().maximumSize(5000).expireAfterWrite(1, TimeUnit.DAYS).recordStats().build());
        return cacheManager;
    }
}
