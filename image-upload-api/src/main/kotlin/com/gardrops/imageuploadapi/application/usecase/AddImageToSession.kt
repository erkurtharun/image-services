package com.gardrops.imageuploadapi.application.usecase

import java.io.InputStream
import java.util.*

interface AddImageToSession {
    fun add(sessionId: UUID, filename: String, input: InputStream): UUID
}
