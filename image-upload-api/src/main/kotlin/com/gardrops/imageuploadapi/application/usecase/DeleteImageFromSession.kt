package com.gardrops.imageuploadapi.application.usecase

import java.util.*

interface DeleteImageFromSession {
    fun delete(sessionId: UUID, imageId: UUID)
}
