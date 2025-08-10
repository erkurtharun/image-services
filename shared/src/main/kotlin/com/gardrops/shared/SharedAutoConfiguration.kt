package com.gardrops.shared

import com.gardrops.shared.logging.CorrelationIdFilter
import com.gardrops.shared.logging.LoggingProperties
import com.gardrops.shared.logging.MdcTaskDecorator
import jakarta.servlet.Filter
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.core.task.TaskDecorator

@AutoConfiguration
@EnableConfigurationProperties(LoggingProperties::class)
class SharedAutoConfiguration {

    @Bean fun mdcTaskDecorator(): TaskDecorator = MdcTaskDecorator()

    @Bean
    fun correlationIdFilterRegistration(): FilterRegistrationBean<Filter> {
        val reg = FilterRegistrationBean<Filter>()
        reg.filter = CorrelationIdFilter()
        reg.order = Ordered.HIGHEST_PRECEDENCE
        reg.addUrlPatterns("/*")
        return reg
    }
}
