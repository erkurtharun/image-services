package com.gardrops.imageprocessingapi.domain.model

data class ProcessSpec(
    val maxWidth: Int = 720,
    val maxHeight: Int = 1280,
    val jpegQuality: Float = 0.9f
)
