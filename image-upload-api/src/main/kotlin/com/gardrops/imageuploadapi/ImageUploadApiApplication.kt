package com.gardrops.imageuploadapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.gardrops.imageuploadapi.application.service.SessionService
import com.gardrops.imageuploadapi.application.usecase.*
import com.gardrops.imageuploadapi.domain.port.out.ImageProcessingPort
import com.gardrops.imageuploadapi.domain.port.out.SessionRepository
import com.gardrops.imageuploadapi.infrastructure.outbound.persistence.RedisSessionRepository
import com.gardrops.imageuploadapi.infrastructure.outbound.processing.ImageProcessingClient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import java.time.Duration

@SpringBootApplication
@EnableConfigurationProperties(UploadProps::class, ProcessingClientProps::class)
class ImageUploadApiApplication {

    @Bean fun sessionRepository(redis: StringRedisTemplate, om: ObjectMapper): SessionRepository =
        RedisSessionRepository(redis, om)

    @Bean fun imageProcessingPort(props: ProcessingClientProps): ImageProcessingPort =
        ImageProcessingClient(props.baseUrl)

    @Bean fun sessionService(repo: SessionRepository, port: ImageProcessingPort, props: UploadProps): SessionService =
        SessionService(repo, port, Duration.ofHours(props.ttlHours), props.maxImages, props.baseDir)

    // Use case beans (interface exposure – DIP)
    @Bean fun createSession(s: SessionService): CreateSession = s
    @Bean fun addImageToSession(s: SessionService): AddImageToSession = s
    @Bean fun listSessionImages(s: SessionService): ListSessionImages = s
    @Bean fun deleteImageFromSession(s: SessionService): DeleteImageFromSession = s
}

fun main(args: Array<String>) {
    runApplication<ImageUploadApiApplication>(*args)
}

@ConfigurationProperties(prefix = "gardrops.upload")
data class UploadProps(
    val ttlHours: Long = 1,
    val maxImages: Int = 10,
    val baseDir: String = "/tmp/uploads"
)

@ConfigurationProperties(prefix = "gardrops.processing")
data class ProcessingClientProps(
    val baseUrl: String = "http://localhost:8081"
)
