package com.gardrops.imageuploadapi.infrastructure.outbound.events

import com.gardrops.proto.image.ImageDeleted
import com.google.protobuf.Timestamp
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class ImageDeletedPublisher(
    private val kafka: KafkaTemplate<String, ByteArray>,
    @Value("\${app.topics.imageDeleted:image-deleted}") private val topic: String
) {
    fun publish(sessionId: UUID, imageId: UUID, relativePath: String) {
        val now = Instant.now()
        val payload = ImageDeleted.newBuilder()
            .setSessionId(sessionId.toString())
            .setImageId(imageId.toString())
            .setDestinationRelativePath(relativePath)
            .setOccurredAt(Timestamp.newBuilder()
                .setSeconds(now.epochSecond).setNanos(now.nano).build())
            .setSource("image-upload-api")
            .build()
            .toByteArray()

        kafka.send(topic, imageId.toString(), payload)
    }
}
