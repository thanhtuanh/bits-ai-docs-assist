package com.aidocs.aiservice.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.netflix.discovery.EurekaClient;

@TestConfiguration
@Import(EurekaClientAutoConfiguration.class)
public class TestConfig {

    @Bean
    public EurekaClient eurekaClientMock() {
        return Mockito.mock(EurekaClient.class);
    }

    @Bean
    public StringRedisTemplate redisTemplateMock() {
        return Mockito.mock(StringRedisTemplate.class);
    }
}
