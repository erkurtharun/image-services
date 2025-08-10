package com.gardrops.imageuploadapi.domain.model

import java.time.Instant
import java.util.*

data class ImageRef(val imageId: UUID, val filename: String, val path: String)

data class UploadSession(
    val sessionId: UUID,
    val createdAt: Instant,
    val expiresAt: Instant,
    val images: List<ImageRef> = emptyList()
) {
    fun isExpired(now: Instant = Instant.now()) = now.isAfter(expiresAt)
    fun canAddMore(max: Int) = images.size < max
}
