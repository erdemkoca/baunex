package ch.baunex.upload.service

import jakarta.enterprise.context.ApplicationScoped
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import org.jboss.resteasy.reactive.multipart.FileUpload
import java.nio.file.StandardCopyOption

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

    fun saveFile(fileStream: InputStream, originalFilename: String): String {
        // 1) Upload-Verzeichnis sicherstellen
        val uploadsDir: Path = Paths.get("uploads")
        if (!Files.exists(uploadsDir)) {
            Files.createDirectories(uploadsDir)
        }

        // 2) Dateiendung extrahieren
        val ext = originalFilename
            .substringAfterLast('.', "")
            .let { if (it.isNotBlank()) ".$it" else "" }

        // 3) Zufälligen Dateinamen generieren
        val storedFilename = "attachment_${UUID.randomUUID()}$ext"
        val targetPath = uploadsDir.resolve(storedFilename)

        // 4) File-Stream in das Ziel kopieren
        Files.copy(fileStream, targetPath, StandardCopyOption.REPLACE_EXISTING)

        // 5) Pfad zurückgeben (je nach Setup ggf. mit Kontext-Prefix)
        return "/uploads/$storedFilename"
    }
} 