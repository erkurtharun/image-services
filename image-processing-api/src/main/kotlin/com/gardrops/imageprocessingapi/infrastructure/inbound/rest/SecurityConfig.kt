package com.gardrops.imageprocessingapi.infrastructure.inbound.rest

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered

@Configuration
class SecurityConfig {

    @Bean
    @ConditionalOnProperty(
        name = ["security.localhost-only.enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun localhostOnlyFilterRegistration(
        @Value("\${security.localhost-only.allow-private:true}") allowPrivate: Boolean,
        @Value("\${security.localhost-only.allowed-cidrs:}") cidrsCsv: String
    ): FilterRegistrationBean<LocalhostOnlyFilter> =
        FilterRegistrationBean(
            LocalhostOnlyFilter(
                allowPrivate = allowPrivate,
                extraCidrs = cidrsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            )
        ).apply {
            order = Ordered.HIGHEST_PRECEDENCE
            addUrlPatterns("/*")
        }
}
