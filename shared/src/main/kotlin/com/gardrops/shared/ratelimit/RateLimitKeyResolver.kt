package com.gardrops.shared.ratelimit

import jakarta.servlet.http.HttpServletRequest

fun interface RateLimitKeyResolver {
    fun resolveKey(request: HttpServletRequest, limitPerMinute: Int): String
}
