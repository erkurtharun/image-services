package com.gardrops.imageuploadapi.infrastructure.inbound.rest

import com.gardrops.imageuploadapi.application.usecase.*
import com.gardrops.imageuploadapi.infrastructure.inbound.rest.dto.CreateSessionResponse
import com.gardrops.imageuploadapi.infrastructure.inbound.rest.dto.SessionImagesResponse
import com.gardrops.imageuploadapi.infrastructure.inbound.rest.dto.UploadImageResponse
import com.gardrops.shared.ratelimit.RateLimited
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream
import java.util.*

@RestController
@RequestMapping("/sessions")
class SessionController(
    private val createSession: CreateSession,
    private val addImageToSession: AddImageToSession,
    private val listSessionImages: ListSessionImages,
    private val deleteImageFromSession: DeleteImageFromSession
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RateLimited(limitPerMinute = 10)
    fun create(): CreateSessionResponse = CreateSessionResponse(createSession.create())

    @PostMapping("/{sessionId}/images", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @RateLimited(limitPerMinute = 10)
    fun upload(
        @PathVariable sessionId: UUID,
        @RequestPart("image") image: MultipartFile
    ): UploadImageResponse =
        BufferedInputStream(image.inputStream).use {
            UploadImageResponse(addImageToSession.add(sessionId, image.originalFilename ?: "image.jpg", it))
        }

    @GetMapping("/{sessionId}/images")
    @RateLimited(limitPerMinute = 10)
    fun list(@PathVariable sessionId: UUID) =
        SessionImagesResponse(listSessionImages.list(sessionId))

    @DeleteMapping("/{sessionId}/images/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RateLimited(limitPerMinute = 10)
    fun delete(@PathVariable sessionId: UUID, @PathVariable imageId: UUID) =
        deleteImageFromSession.delete(sessionId, imageId)
}
