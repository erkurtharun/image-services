package com.gardrops.imageuploadapi.infrastructure.inbound.rest.dto

import java.util.*

data class CreateSessionResponse(val sessionId: UUID)
data class UploadImageResponse(val imageId: UUID)
data class SessionImagesResponse(val imageIds: List<UUID>)
