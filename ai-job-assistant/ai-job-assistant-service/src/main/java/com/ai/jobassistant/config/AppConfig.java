package com.ai.jobassistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * AppConfig
 * Redis cache manager and WebClient for OpenAI API calls.
 */
@Configuration
public class AppConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
            .codecs(configurer ->
                configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024) // 10MB
            );
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .disableCachingNullValues()
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(new GenericJackson2JsonRedisSerializer())
            );

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // JD analysis cached for 24 hours — same JD won't re-hit OpenAI
        cacheConfigs.put("jd-analysis",
            defaultConfig.entryTtl(Duration.ofHours(24)));

        // Resume scores cached for 12 hours
        cacheConfigs.put("resume-scores",
            defaultConfig.entryTtl(Duration.ofHours(12)));

        // Interview questions cached for 24 hours
        cacheConfigs.put("interview-questions",
            defaultConfig.entryTtl(Duration.ofHours(24)));

        // Application details cached for 30 minutes
        cacheConfigs.put("applications",
            defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
    }
}
