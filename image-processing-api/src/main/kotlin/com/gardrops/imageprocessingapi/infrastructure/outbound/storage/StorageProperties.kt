package com.gardrops.imageprocessingapi.infrastructure.outbound.storage

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "storage")
data class StorageProperties(
    val basePath: String = "./data/images"
)
