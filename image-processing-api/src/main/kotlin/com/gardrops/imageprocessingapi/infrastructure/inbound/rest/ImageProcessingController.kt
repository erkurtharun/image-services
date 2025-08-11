package com.gardrops.imageprocessingapi.infrastructure.inbound.rest

import com.gardrops.imageprocessingapi.domain.port.`in`.ProcessImage
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.constraints.NotBlank
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Paths

@RestController
@RequestMapping("/images")
class ImageProcessingController(
    private val processImage: ProcessImage
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Operation(summary = "Resize & compress image to JPEG and save")
    @ApiResponse(responseCode = "204", description = "Processed")
    @PostMapping("/process", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun process(
        @RequestPart("image") image: MultipartFile,
        @RequestPart("destinationFilePath") @NotBlank destinationFilePath: String
    ) {
        try {
            val rel = Paths.get(destinationFilePath).normalize()
            if (rel.isAbsolute || rel.toString().startsWith("..")) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "destinationFilePath must be a safe relative path"
                )
            }
            image.inputStream.use { stream ->
                processImage.processAndStore(stream, rel.toString())
            }

        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: IllegalArgumentException) {
            log.warn("Bad request to /images/process: {}", e.message)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message ?: "Bad request", e)
        } catch (e: Exception) {
            log.error("Processing failed for destination='{}'", destinationFilePath, e)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Processing failed", e)
        }
    }
}
