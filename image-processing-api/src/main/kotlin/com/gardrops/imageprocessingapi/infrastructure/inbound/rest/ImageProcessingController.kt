package com.gardrops.imageprocessingapi.infrastructure.inbound.rest

import com.gardrops.imageprocessingapi.domain.port.`in`.ProcessImage
import com.gardrops.imageprocessingapi.infrastructure.inbound.rest.dto.ProcessImageRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse

@Validated
@RestController
@RequestMapping("/images")
class ImageProcessingController(
    private val processImage: ProcessImage
) {
    @Operation(summary = "Resize & compress image to JPEG and save")
    @ApiResponse(responseCode = "204", description = "Processed")
    @PostMapping("/process", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun process(@ModelAttribute @Valid req: ProcessImageRequest) {
        require(req.destinationFilePath.isNotBlank()) { "destinationFilePath must not be blank" }
        req.image.inputStream.use { stream ->
            processImage.processAndStore(stream, req.destinationFilePath)
        }
    }
}

data class ProcessImageRequest(
    val image: org.springframework.web.multipart.MultipartFile,
    @field:NotBlank val destinationFilePath: String
)
