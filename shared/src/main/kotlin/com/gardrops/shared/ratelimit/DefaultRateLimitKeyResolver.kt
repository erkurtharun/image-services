// com/gardrops/shared/ratelimit/DefaultRateLimitKeyResolver.kt
package com.gardrops.shared.ratelimit

import com.gardrops.shared.redis.RedisKeys
import com.gardrops.shared.web.ClientIpResolver
import jakarta.servlet.http.HttpServletRequest

class DefaultRateLimitKeyResolver(
    private val ipResolver: ClientIpResolver = ClientIpResolver(),
    private val namespace: String = "gardrops"
) : RateLimitKeyResolver {

    override fun resolveKey(request: HttpServletRequest, limitPerMinute: Int): String {
        val ip = ipResolver.resolve(request)
        val method = request.method
        val normalizedPath = normalizePath(request.requestURI)
        return RedisKeys.rateKey(namespace, ip, method, normalizedPath)
    }

    private fun normalizePath(path: String): String {
        // /sessions/550e8400-e29b-41d4-a716-446655440000/images  -> /sessions/{uuid}/images
        return path.replace(Regex("""\b[0-9a-fA-F-]{36}\b"""), "{uuid}")
            .replace(Regex("""\b\d+\b"""), "{id}")
    }
}
