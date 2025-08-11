package com.gardrops.imageuploadapi.application.service

import com.gardrops.imageuploadapi.application.usecase.*
import com.gardrops.imageuploadapi.domain.model.ImageRef
import com.gardrops.imageuploadapi.domain.model.UploadSession
import com.gardrops.imageuploadapi.domain.port.out.ImageProcessingPort
import com.gardrops.imageuploadapi.domain.port.out.SessionRepository
import com.gardrops.shared.exception.NotFoundException
import com.gardrops.shared.exception.ValidationException
import com.gardrops.imageuploadapi.infrastructure.outbound.events.ImageDeletedPublisher
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.io.path.Path

class SessionService(
    private val sessions: SessionRepository,
    private val processor: ImageProcessingPort,
    private val imageDeletedPublisher: ImageDeletedPublisher,
    private val ttl: Duration = Duration.ofHours(1),
    private val maxImages: Int = 10,
    private val baseDir: String = "/tmp/uploads"
) : CreateSession, AddImageToSession, ListSessionImages, DeleteImageFromSession {

    override fun create(): UUID {
        val now = Instant.now()
        val sess = UploadSession(UUID.randomUUID(), now, now.plus(ttl))
        sessions.save(sess, ttl)
        return sess.sessionId
    }

    override fun add(sessionId: UUID, filename: String, input: java.io.InputStream): UUID {
        val s = sessions.find(sessionId) ?: throw NotFoundException("Session not found")
        if (s.isExpired()) throw ValidationException("Session expired")
        if (!s.canAddMore(maxImages)) throw ValidationException("Max $maxImages images")

        val imageId = UUID.randomUUID()
        val destination = Path(baseDir).resolve("$sessionId").resolve("$imageId.jpg").toString()

        // Delegate to processing service (port)
        processor.processAndStore(input, destination)

        val updated = s.copy(images = s.images + ImageRef(imageId, filename, destination))
        sessions.save(updated, Duration.between(Instant.now(), s.expiresAt).coerceAtLeast(Duration.ofSeconds(1)))
        return imageId
    }

    override fun list(sessionId: UUID): List<UUID> {
        val s = sessions.find(sessionId) ?: throw NotFoundException("Session not found")
        return s.images.map { it.imageId }
    }

    override fun delete(sessionId: UUID, imageId: UUID) {
        val s = sessions.find(sessionId) ?: throw NotFoundException("Session not found")
        val img = s.images.find { it.imageId == imageId } ?: throw NotFoundException("Image not in session")

        // remove from session
        val updated = s.copy(images = s.images.filterNot { it.imageId == imageId })
        sessions.save(updated, Duration.between(Instant.now(), s.expiresAt).coerceAtLeast(Duration.ofSeconds(1)))

        // send delete event
        imageDeletedPublisher.publish(sessionId, imageId, img.path)
    }
}
