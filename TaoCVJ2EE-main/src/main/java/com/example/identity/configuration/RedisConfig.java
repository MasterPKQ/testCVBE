package com.example.identity.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

        @Bean
        public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
                // Tạo ObjectMapper với cấu hình đơn giản hơn
                ObjectMapper objectMapper = JsonMapper.builder()
                                .addModule(new JavaTimeModule())
                                .build();

                // Cấu hình serialization
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

                // Tạo PolymorphicTypeValidator chỉ cho phép package cụ thể của project
                PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                                .allowIfBaseType(Object.class)
                                .allowIfSubType("com.example.identity")
                                .allowIfSubType("java.util")
                                .allowIfSubType("java.time")
                                .build();

                // Kích hoạt default typing với cấu hình an toàn hơn
                objectMapper.activateDefaultTyping(
                                ptv,
                                ObjectMapper.DefaultTyping.NON_FINAL,
                                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY);

                // Sử dụng GenericJackson2JsonRedisSerializer với ObjectMapper đã config
                GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

                // Cấu hình cache mặc định
                RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(30)) // Cache timeout 30 phút
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(
                                                RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                                .disableCachingNullValues(); // Không cache giá trị null

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(cacheConfig)
                                .build();
        }
}
