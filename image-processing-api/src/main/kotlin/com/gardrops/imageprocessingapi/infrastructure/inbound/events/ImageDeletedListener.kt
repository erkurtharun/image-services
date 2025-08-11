package com.gardrops.imageprocessingapi.infrastructure.inbound.events

import com.gardrops.imageprocessingapi.domain.service.DeleteProcessedImageService
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
    @Value("\${STORAGE_BASEPATH:/data/images}") private val storageBase: String,
    private val deleteProcessedImageService: DeleteProcessedImageService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["\${app.topics.imageDeleted:image-deleted}"],
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun onMessage(value: ByteArray) {
        val evt = ImageDeleted.parseFrom(value)
        deleteProcessedImageService.delete(evt.destinationRelativePath)
        log.info("ImageDeleted consumed: session={}, image={}, relPath={}",
            evt.sessionId, evt.imageId, evt.destinationRelativePath)
    }
}
