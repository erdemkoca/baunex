package ch.baunex.upload.spi.impl

import ch.baunex.upload.spi.Local
import ch.baunex.upload.spi.StorageService
import jakarta.enterprise.context.ApplicationScoped
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@ApplicationScoped
@Local
class LocalDiskStorage : StorageService {
    private val uploadsDir: Path = Paths.get("uploads").also {
        if (!Files.exists(it)) Files.createDirectories(it)
    }

    override fun save(stream: InputStream, originalFilename: String): String {
        val ext = originalFilename.substringAfterLast('.', "")
            .let { if (it.isNotBlank()) ".$it" else "" }
        val stored = "attachment_${UUID.randomUUID()}$ext"
        val target = uploadsDir.resolve(stored)
        Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING)
        return "/api/upload/files/$stored"
    }

    override fun delete(fileUrl: String) {
        val key = fileUrl.substringAfterLast('/')
        val f = uploadsDir.resolve(key)
        Files.deleteIfExists(f)
    }
}
