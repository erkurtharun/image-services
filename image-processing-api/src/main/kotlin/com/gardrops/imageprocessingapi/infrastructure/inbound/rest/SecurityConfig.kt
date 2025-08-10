package com.gardrops.imageprocessingapi.infrastructure.inbound.rest

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered

@Configuration
class SecurityConfig {
    @Bean
    fun localhostOnlyFilterRegistration(): FilterRegistrationBean<LocalhostOnlyFilter> =
        FilterRegistrationBean(LocalhostOnlyFilter()).apply {
            order = Ordered.HIGHEST_PRECEDENCE
            addUrlPatterns("/*")
        }
}
