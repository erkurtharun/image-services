package com.gardrops.shared.ratelimit

import com.gardrops.shared.exception.RateLimitException
import com.gardrops.shared.redis.RedisKeys
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Aspect
@Component
class RateLimiterAspect(
    private val redis: StringRedisTemplate,
    private val request: HttpServletRequest,
    @Value("\${ratelimit.namespace:gardrops}") private val ns: String = "gardrops"
) {
    @Before("@annotation(rl)")
    fun check(@Suppress("UNUSED_PARAMETER") jp: JoinPoint, rl: RateLimited) {
        val ip = request.remoteAddr ?: "unknown"
        val method = request.method
        val path = normalizePath(request.requestURI)
        val key = RedisKeys.rateKey(ns, ip, method, path)

        val ops = redis.opsForValue()
        val current = ops.increment(key) ?: 1
        if (current == 1L) redis.expire(key, Duration.ofMinutes(1))
        if (current > rl.limitPerMinute) throw RateLimitException("Too many requests")
    }

    private fun normalizePath(path: String): String =
        path.replace(Regex("""\b[0-9a-fA-F-]{36}\b"""), "{uuid}")
            .replace(Regex("""\b\d+\b"""), "{id}")
}
