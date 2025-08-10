package com.gardrops.shared.redis

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Duration

@Configuration
class RedisConfig {

    @Bean
    fun redisConnectionFactory(
        @Value("\${spring.data.redis.host:localhost}") host: String,
        @Value("\${spring.data.redis.port:6379}") port: Int,
        @Value("\${spring.data.redis.database:0}") database: Int,
        @Value("\${spring.data.redis.password:}") password: String,
        @Value("\${spring.data.redis.timeout:2s}") timeout: Duration
    ): LettuceConnectionFactory {
        val standalone = RedisStandaloneConfiguration(host, port).apply {
            this.database = database
            if (password.isNotBlank()) this.password = RedisPassword.of(password)
        }

        val clientCfg = LettuceClientConfiguration.builder()
            .commandTimeout(timeout)
            .shutdownTimeout(Duration.ofMillis(0))
            .build()

        return LettuceConnectionFactory(standalone, clientCfg)
    }

    @Bean
    fun stringRedisTemplate(cf: LettuceConnectionFactory) = StringRedisTemplate(cf)
}
