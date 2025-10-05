package org.tech.product_service.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * SpringCachingConfig configuration class to enable caching in the application.
 * This class is annotated with @EnableCaching to activate Spring's annotation-driven cache management capability.
 * @see org.springframework.cache.annotation.EnableCaching
 * @author Josip Begic
 */
@Configuration
@EnableCaching
public class SpringCachingConfig {
}
