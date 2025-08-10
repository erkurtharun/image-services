package com.gardrops.imageprocessingapi.application

import com.gardrops.imageprocessingapi.domain.model.ProcessSpec
import com.gardrops.imageprocessingapi.infrastructure.outbound.transform.ImageIoConfig
import com.gardrops.imageprocessingapi.infrastructure.outbound.storage.StorageProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(value = [ImageIoConfig::class, StorageProperties::class])
class ProcessingConfig(
    private val cfg: ImageIoConfig
) {
    @Bean
    fun processSpec(): ProcessSpec =
        ProcessSpec(cfg.maxWidth, cfg.maxHeight, cfg.jpegQuality.toFloat())
}
