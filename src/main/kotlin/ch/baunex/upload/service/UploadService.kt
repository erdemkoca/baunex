package ch.baunex.upload.service

import jakarta.enterprise.context.ApplicationScoped
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import org.jboss.resteasy.reactive.multipart.FileUpload

@ApplicationScoped
class UploadService {

    fun saveLogo(file: InputStream, fileDetails: FileUpload): String {
        val uploadsDir = Paths.get("uploads")
        if (!Files.exists(uploadsDir)) {
            Files.createDirectories(uploadsDir)
        }

        val ext = fileDetails.fileName().substringAfterLast('.', "")
        val filename = "logo_${UUID.randomUUID()}${if (ext.isNotEmpty()) ".$ext" else ""}"
        val targetPath = uploadsDir.resolve(filename)

        Files.copy(fileDetails.uploadedFile(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING)

        return "/uploads/$filename"
    }
} 