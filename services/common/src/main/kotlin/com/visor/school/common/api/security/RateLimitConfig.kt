package com.visor.school.common.api.security

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

/**
 * Rate limiting configuration
 * Note: In production, use Redis or distributed rate limiting solution
 */
@Configuration
class RateLimitConfig {

    /**
     * Default rate limit bucket: 100 requests per minute
     */
    @Bean
    fun defaultRateLimitBucket(): Bucket {
        val bandwidth = Bandwidth.builder()
            .capacity(100)
            .refillIntervally(100, Duration.ofMinutes(1))
            .build()
        return Bucket.builder()
            .addLimit(bandwidth)
            .build()
    }

    /**
     * Strict rate limit bucket: 20 requests per minute (for sensitive operations)
     */
    @Bean
    fun strictRateLimitBucket(): Bucket {
        val bandwidth = Bandwidth.builder()
            .capacity(20)
            .refillIntervally(20, Duration.ofMinutes(1))
            .build()
        return Bucket.builder()
            .addLimit(bandwidth)
            .build()
    }
}

