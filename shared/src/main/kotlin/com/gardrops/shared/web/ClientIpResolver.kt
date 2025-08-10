package com.gardrops.shared.web

import jakarta.servlet.http.HttpServletRequest

class ClientIpResolver(
    private val trustedProxyHops: Int = 1
) {
    fun resolve(request: HttpServletRequest): String {
        val xff = request.getHeader("X-Forwarded-For")?.takeIf { it.isNotBlank() }
        if (xff != null && trustedProxyHops > 0) {
            val parts = xff.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (parts.isNotEmpty()) {
                val idx = (parts.size - 1 - (trustedProxyHops - 1)).coerceAtLeast(0)
                return parts[idx]
            }
        }
        return request.getHeader("X-Real-IP")?.takeIf { it.isNotBlank() } ?: request.remoteAddr
    }
}
