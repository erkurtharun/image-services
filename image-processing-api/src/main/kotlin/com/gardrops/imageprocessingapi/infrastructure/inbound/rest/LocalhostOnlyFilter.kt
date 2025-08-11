package com.gardrops.imageprocessingapi.infrastructure.inbound.rest

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.filter.OncePerRequestFilter
import java.net.Inet4Address
import java.net.InetAddress

class LocalhostOnlyFilter(
    private val allowPrivate: Boolean = true,
    private val extraCidrs: List<String> = emptyList()
) : OncePerRequestFilter() {

    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        val remoteIp = req.getHeader("X-Forwarded-For")
            ?.split(",")?.firstOrNull()?.trim()
            ?: req.remoteAddr

        val allowed = isAllowed(remoteIp)
        if (!allowed) {
            res.status = HttpStatus.FORBIDDEN.value()
            res.contentType = "text/plain;charset=UTF-8"
            res.writer.use { it.write("Forbidden: ImageProcessingApi allows only local/private network") }
            return
        }

        chain.doFilter(req, res)
    }

    private fun isAllowed(ipStr: String?): Boolean {
        if (ipStr.isNullOrBlank()) return false
        val ip = try { InetAddress.getByName(ipStr) } catch (_: Exception) { return false }

        // 1) loopback: 127.0.0.1 / ::1
        if (ip.isLoopbackAddress) return true

        // 2) docker/internal/private ağlar: 10/8, 172.16/12, 192.168/16 vb.
        if (allowPrivate && ip.isSiteLocalAddress) return true

        // 3) ayrıca izin verilen CIDR'ler
        if (ip is Inet4Address && extraCidrs.any { inCidr(ip, it) }) return true

        return false
    }

    // Basit IPv4 CIDR kontrolü (IPv6 için gerekirse ayrıca eklenebilir)
    private fun inCidr(ip: Inet4Address, cidr: String): Boolean {
        val parts = cidr.trim().split("/")
        if (parts.size != 2) return false
        val base = (InetAddress.getByName(parts[0]) as? Inet4Address) ?: return false
        val prefix = parts[1].toIntOrNull() ?: return false
        if (prefix !in 0..32) return false

        fun toLong(a: Inet4Address): Long {
            val b = a.address
            return (((b[0].toInt() and 0xFF) shl 24) or
                    ((b[1].toInt() and 0xFF) shl 16) or
                    ((b[2].toInt() and 0xFF) shl 8) or
                    (b[3].toInt() and 0xFF)).toLong() and 0xFFFFFFFFL
        }

        val mask = if (prefix == 0) 0L else (0xFFFFFFFFL shl (32 - prefix)) and 0xFFFFFFFFL
        val ipL = toLong(ip)
        val baseL = toLong(base)
        return (ipL and mask) == (baseL and mask)
    }
}
