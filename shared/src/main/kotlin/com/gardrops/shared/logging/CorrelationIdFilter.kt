package com.gardrops.shared.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

class CorrelationIdFilter : OncePerRequestFilter() {
    private val headerName = "X-Correlation-Id"

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val cid = request.getHeader(headerName)?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
        MDC.put("correlationId", cid)
        response.setHeader(headerName, cid)
        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove("correlationId")
        }
    }
}
