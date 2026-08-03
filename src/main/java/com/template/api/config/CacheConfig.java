package com.template.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * In-process cache (ConcurrentMap by default). Enable Redis later without changing service annotations.
 *
 * <pre>
 * app.cache.enabled=true   # default
 * app.cache.enabled=false  # disable for debugging
 * </pre>
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(prefix = "app.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CacheConfig {
}
