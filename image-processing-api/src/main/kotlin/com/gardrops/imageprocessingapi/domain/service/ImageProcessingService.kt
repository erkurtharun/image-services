package com.gardrops.imageprocessingapi.domain.service

import com.gardrops.imageprocessingapi.domain.port.`in`.ProcessImage
import com.gardrops.imageprocessingapi.domain.port.out.ImageStorage
import com.gardrops.imageprocessingapi.domain.port.out.ImageTransformer
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
class ImageProcessingService(
    private val transformer: ImageTransformer,
    private val storage: ImageStorage
) : ProcessImage {

    override fun processAndStore(input: InputStream, destinationPath: String): String {
        require(destinationPath.isNotBlank()) { "destinationPath must not be blank" }
        input.use { input ->
            val jpeg = transformer.transformToJpeg(input)
            jpeg.use { return storage.save(destinationPath.ensureJpegExtension(), it) }
        }
    }

    private fun String.ensureJpegExtension(): String =
        if (endsWith(".jpg", true) || endsWith(".jpeg", true)) this else "$this.jpg"
}
