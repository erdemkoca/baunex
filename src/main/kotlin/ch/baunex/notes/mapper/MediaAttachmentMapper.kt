package ch.baunex.notes.mapper

import ch.baunex.notes.dto.AttachmentForUI
import ch.baunex.notes.dto.MediaAttachmentDto
import ch.baunex.notes.model.MediaAttachmentModel
import ch.baunex.notes.model.MediaType
import java.net.URLConnection

fun MediaAttachmentModel.toDto(): MediaAttachmentDto {
    val filename = this.url.substringAfterLast('/')
    val type = when {
        filename.endsWith(".png") || filename.endsWith(".jpg") || 
        filename.endsWith(".jpeg") || filename.endsWith(".gif") -> MediaType.IMAGE
        filename.endsWith(".pdf") -> MediaType.PDF
        filename.endsWith(".mp4") || filename.endsWith(".avi") || 
        filename.endsWith(".mov") -> MediaType.VIDEO
        else -> MediaType.IMAGE // Default to IMAGE if unknown
    }
    // Guess content‐type from the filename
    val contentType = URLConnection
        .guessContentTypeFromName(filename)
        ?: "application/octet-stream"

    return MediaAttachmentDto(
        id = this.id!!,
        url = this.url,
        type = type,
        caption = this.caption,
        contentType = contentType,
        filename = filename
    )
}

fun List<MediaAttachmentModel>.toDtoList(): List<MediaAttachmentDto> =
    this.map { it.toDto() }

fun MediaAttachmentDto.toAttachmentForUI(): AttachmentForUI {
    val filename = this.url.substringAfterLast('/')
    val contentType = when {
        filename.endsWith(".png")  -> "image/png"
        filename.endsWith(".jpe")  -> "image/jpeg"
        filename.endsWith(".jpg")  -> "image/jpeg"
        filename.endsWith(".gif")  -> "image/gif"
        filename.endsWith(".pdf")  -> "application/pdf"
        filename.endsWith(".mp4")  -> "video/mp4"
        filename.endsWith(".avi")  -> "video/x-msvideo"
        filename.endsWith(".mov")  -> "video/quicktime"
        else                       -> "application/octet-stream"
    }
    return AttachmentForUI(
        id          = this.id,
        url         = this.url,
        caption     = this.caption,
        filename    = filename,
        contentType = contentType
    )
}