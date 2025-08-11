package com.gardrops.imageprocessingapi.infrastructure.inbound.events

import com.gardrops.proto.image.ImageDeleted
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@EnableKafka
@Component
class ImageDeletedListener(
    @Value("\${STORAGE_BASEPATH:/data/images}") private val storageBase: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["\${app.topics.imageDeleted:image-deleted}"],
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun onMessage(value: ByteArray) {
        val evt = ImageDeleted.parseFrom(value)
        val rel = toSafeRelative(evt.destinationRelativePath)
        val target = Paths.get(storageBase).resolve(rel).normalize()

        try {
            val deleted = Files.deleteIfExists(target) // idempotent
            log.info(
                "ImageDeleted consumed: session={}, image={}, relPath={}, target={}, deleted={}",
                evt.sessionId, evt.imageId, rel, target, deleted
            )
        } catch (e: Exception) {
            log.error("Failed to delete file for relPath='{}' (target={})", rel, target, e)
            throw e // retry
        }
    }

    private fun toSafeRelative(input: String): String {
        var p: Path = Paths.get(input.trim()).normalize()
        for (prefix in listOf("/tmp/uploads", "/uploads", "/data/uploads")) {
            val kp = Paths.get(prefix).normalize()
            if (p.isAbsolute && p.startsWith(kp)) {
                p = kp.relativize(p).normalize()
                break
            }
        }
        if (p.isAbsolute) p = p.subpath(0, p.nameCount).normalize()

        val s = p.toString()
        require(s.isNotBlank() && !Paths.get(s).isAbsolute && !s.startsWith("..")) {
            "Bad relative path"
        }
        return s
    }
}
