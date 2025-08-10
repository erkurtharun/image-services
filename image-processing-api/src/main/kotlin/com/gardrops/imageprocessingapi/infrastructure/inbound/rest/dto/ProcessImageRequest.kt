package com.gardrops.imageprocessingapi.infrastructure.inbound.rest.dto

import org.springframework.web.multipart.MultipartFile

data class ProcessImageRequest(
    val destinationFilePath: String,
    val image: MultipartFile
)
