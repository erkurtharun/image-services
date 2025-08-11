package com.gardrops.imageuploadapi.infrastructure.outbound.processing

import com.gardrops.imageuploadapi.domain.port.out.ImageProcessingPort
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.util.UriBuilder
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths

class ImageProcessingClient(
    baseUrl: String
) : ImageProcessingPort {

    private val client: WebClient = WebClient.builder()
        .baseUrl(baseUrl)
        .build()

    override fun processAndStore(image: InputStream, destinationPath: String) {
        val relative = toRelativePath(destinationPath)

        val bytes = image.readAllBytes()
        val body = MultipartBodyBuilder().apply {
            part("image", bytes)
                .filename("upload.jpg")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
            part("destinationFilePath", relative)
        }.build()

        client.post()
            .uri { ub: UriBuilder -> ub.path("/images/process").build() }
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .retrieve()
            .toBodilessEntity()
            .onErrorMap(WebClientResponseException::class.java) { ex ->
                IllegalStateException(
                    "Image processing call failed: ${ex.statusCode} ${ex.responseBodyAsString}",
                    ex
                )
            }
            .block()
    }

    /**
     * Makes sure the path we send to the processing service is a SAFE RELATIVE path.
     * Strategy:
     * 1) If IMAGE_UPLOAD_BASEDIR env/system property is set and the path starts with it, relativize.
     * 2) Else strip common prefixes (/tmp/uploads, /uploads, /data/uploads) if present.
     * 3) Else, if still absolute, drop the root ("/a/b/c" -> "a/b/c"; "C:\x\y" -> "x\y").
     * 4) Reject paths that resolve to empty, absolute, or start with "..".
     */
    private fun toRelativePath(input: String): String {
        val raw = input.trim()
        require(raw.isNotEmpty()) { "destinationPath must not be blank" }

        // Normalize once
        var p: Path = Paths.get(raw).normalize()

        // 1) Try configured base dir (env or system prop)
        val cfgBase = System.getenv("IMAGE_UPLOAD_BASEDIR")
            ?: System.getProperty("image.upload.basedir")
        if (!cfgBase.isNullOrBlank()) {
            val basePath = Paths.get(cfgBase).normalize()
            if (p.startsWith(basePath)) {
                p = basePath.relativize(p).normalize()
            }
        }

        // 2) Try known prefixes
        if (p.isAbsolute) {
            val knownPrefixes = listOf("/tmp/uploads", "/uploads", "/data/uploads")
            for (prefix in knownPrefixes) {
                val kp = Paths.get(prefix).normalize()
                if (p.startsWith(kp)) {
                    p = kp.relativize(p).normalize()
                    break
                }
            }
        }

        // 3) Still absolute? Drop root and keep the names only ("/a/b/c" -> "a/b/c")
        if (p.isAbsolute) {
            if (p.nameCount > 0) {
                p = p.subpath(0, p.nameCount).normalize()
            } else {
                throw IllegalArgumentException("destinationPath resolves to an empty absolute path")
            }
        }

        // 4) Final safety checks
        val relStr = p.toString()
        require(relStr.isNotBlank()) { "destinationPath resolved to blank" }
        require(!Paths.get(relStr).isAbsolute) { "destinationPath must be relative after normalization" }
        require(!relStr.startsWith("..")) { "destinationPath must not escape parent directories" }

        return relStr
    }
}
