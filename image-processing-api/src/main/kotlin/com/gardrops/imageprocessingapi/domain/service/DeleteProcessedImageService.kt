package com.gardrops.imageprocessingapi.domain.service

import com.gardrops.imageprocessingapi.domain.port.`in`.DeleteImage
import com.gardrops.imageprocessingapi.infrastructure.outbound.storage.SafeFileSystemImageStorage
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.nio.file.Paths

@Service
class DeleteProcessedImageService(
    private val store: SafeFileSystemImageStorage
) : DeleteImage {

    override fun delete(path: String) {
        require(path.isNotBlank()) { "Path must not be blank" }
        val safePath = toSafeRelative(path)
        store.delete(safePath)
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