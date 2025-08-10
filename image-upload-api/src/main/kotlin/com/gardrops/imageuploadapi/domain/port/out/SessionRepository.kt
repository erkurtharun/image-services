package com.gardrops.imageuploadapi.domain.port.out

import com.gardrops.imageuploadapi.domain.model.UploadSession
import java.time.Duration
import java.util.*

interface SessionRepository {
    fun save(session: UploadSession, ttl: Duration)
    fun find(sessionId: UUID): UploadSession?
    fun delete(sessionId: UUID)
}
