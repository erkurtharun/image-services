package com.gardrops.imageuploadapi.infrastructure.outbound.processing

import com.gardrops.imageuploadapi.domain.port.out.ImageProcessingPort
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.io.InputStream

class ImageProcessingClient(
    baseUrl: String
) : ImageProcessingPort {

    private val client = WebClient.builder()
        .baseUrl(baseUrl)
        .build()

    override fun processAndStore(image: InputStream, destinationPath: String) {
        val body = MultipartBodyBuilder().apply {
            part("image", image.readAllBytes()).filename("upload.jpg").contentType(MediaType.IMAGE_JPEG)
            part("destinationFilePath", destinationPath)
        }.build()

        client.post().uri("/images/process")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .retrieve()
            .bodyToMono(Void::class.java)
            .block()
    }
}
