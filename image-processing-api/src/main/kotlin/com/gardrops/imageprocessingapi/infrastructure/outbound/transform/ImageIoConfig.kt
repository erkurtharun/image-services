package com.gardrops.imageprocessingapi.infrastructure.outbound.transform

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "processing")
data class ImageIoConfig(
    val maxWidth: Int = 720,
    val maxHeight: Int = 1280,
    val jpegQuality: Double = 0.9
)
