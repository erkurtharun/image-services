package com.gardrops.imageprocessingapi.infrastructure.outbound.storage

import com.gardrops.imageprocessingapi.domain.port.out.ImageStorage
import org.springframework.stereotype.Component
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

@Component
class SafeFileSystemImageStorage(
    private val props: StorageProperties
) : ImageStorage {

    override fun save(destinationPath: String, imageJpeg: InputStream): String {
        val target = resolveUnderBase(destinationPath)
        Files.createDirectories(target.parent)
        // this is an atomic-ish operation: first write to a .part file, then rename it to the final name
        val tmp = target.resolveSibling(target.fileName.toString() + ".part")
        imageJpeg.use {
            Files.newOutputStream(tmp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).use { os ->
                it.copyTo(os)
            }
        }
        Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        return target.toAbsolutePath().toString()
    }

    private fun resolveUnderBase(rel: String): Path {
        val safeRel = rel.replace("\\", "/")
            .replace(Regex("""\.\.+"""), "")
            .replace(Regex("""^/+"""), "")
            .trim()

        val base = Path.of(props.basePath).toAbsolutePath().normalize()
        val target = base.resolve(safeRel).normalize()
        require(target.startsWith(base)) { "Invalid destination path" }
        return target
    }

    override fun delete(path: String): Boolean {
        val target = resolveUnderBase(path)
        return try {
            Files.deleteIfExists(target)
        } catch (e: Exception) {
            false
        }
    }
}
