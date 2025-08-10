package com.gardrops.imageprocessingapi.infrastructure.inbound.rest

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.filter.OncePerRequestFilter
import java.net.InetAddress

class LocalhostOnlyFilter : OncePerRequestFilter() {
    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        val remote = req.remoteAddr
        val isLoopback = try { InetAddress.getByName(remote).isLoopbackAddress } catch (_: Exception) { false }
        if (!isLoopback) {
            res.status = HttpStatus.FORBIDDEN.value()
            res.writer.write("Forbidden: ImageProcessingApi is local-only")
            return
        }
        chain.doFilter(req, res)
    }
}
