package com.gardrops.shared.logging

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("logging.gardrops")
data class LoggingProperties(
    val service: String = "",
    val environment: String = "local"
)
