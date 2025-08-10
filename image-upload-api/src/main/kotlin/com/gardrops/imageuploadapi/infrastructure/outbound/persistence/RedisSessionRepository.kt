package com.gardrops.imageuploadapi.infrastructure.outbound.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import com.gardrops.imageuploadapi.domain.model.UploadSession
import com.gardrops.imageuploadapi.domain.port.out.SessionRepository
import com.gardrops.shared.redis.RedisKeys
import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Duration
import java.util.*

class RedisSessionRepository(
    private val redis: StringRedisTemplate,
    private val om: ObjectMapper
) : SessionRepository {

    override fun save(session: UploadSession, ttl: Duration) {
        val key = RedisKeys.sessionKey(session.sessionId.toString())
        redis.opsForValue().set(key, om.writeValueAsString(session), ttl)
    }

    override fun find(sessionId: UUID): UploadSession? {
        val key = RedisKeys.sessionKey(sessionId.toString())
        val json = redis.opsForValue().get(key) ?: return null
        return om.readValue(json, UploadSession::class.java)
    }

    override fun delete(sessionId: UUID) {
        redis.delete(RedisKeys.sessionKey(sessionId.toString()))
    }
}
