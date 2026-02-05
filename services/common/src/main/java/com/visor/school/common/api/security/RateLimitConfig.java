package com.visor.school.common.api.security;

import com.visor.school.common.api.Constants;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Rate limiting configuration.
 * Note: In production, use Redis or distributed rate limiting solution.
 */
@Configuration
public class RateLimitConfig {

    /**
     * Default rate limit bucket: 100 requests per minute
     */
    @Bean
    public Bucket defaultRateLimitBucket() {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(Constants.RATE_LIMIT_DEFAULT)
                .refillIntervally(Constants.RATE_LIMIT_DEFAULT, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }

    /**
     * Strict rate limit bucket: 20 requests per minute (for sensitive operations)
     */
    @Bean
    public Bucket strictRateLimitBucket() {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(Constants.RATE_LIMIT_STRICT)
                .refillIntervally(Constants.RATE_LIMIT_STRICT, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }
}
