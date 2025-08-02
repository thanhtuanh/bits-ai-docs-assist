package com.aidocs.aiservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test")
public class AiServiceTestConfiguration {

    /**
     * Mock RestTemplate für Tests - verhindert echte HTTP Calls
     */
    @Bean
    @Primary
    public RestTemplate testRestTemplate() {
        return mock(RestTemplate.class);
    }

    /**
     * Mock StringRedisTemplate für Tests - verhindert echte Redis Calls
     */
    @Bean
    @Primary  
    public StringRedisTemplate testStringRedisTemplate() {
        return mock(StringRedisTemplate.class);
    }

    /**
     * Mock RedisConnectionFactory für Tests
     */
    @Bean
    @Primary
    public RedisConnectionFactory testRedisConnectionFactory() {
        return mock(LettuceConnectionFactory.class);
    }
}