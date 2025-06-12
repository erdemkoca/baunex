package ch.baunex.notes.mapper

import ch.baunex.notes.dto.AttachmentForUI
import ch.baunex.notes.dto.MediaAttachmentDto
import ch.baunex.notes.model.MediaAttachmentModel
import java.net.URL

fun MediaAttachmentModel.toDto(): MediaAttachmentDto {
    return MediaAttachmentDto(
        id = this.id!!,
        url = this.url,
        type = this.type,
        caption = this.caption
    )
}

fun MediaAttachmentDto.toAttachmentForUI(): AttachmentForUI {
    val filename = URL(this.url).path.substringAfterLast("/")
    val contentType = when {
        filename.endsWith(".png")  -> "image/png"
        filename.endsWith(".jpe")  -> "image/jpeg"
        filename.endsWith(".jpg")  -> "image/jpeg"
        filename.endsWith(".gif")  -> "image/gif"
        else                        -> "application/octet-stream"
    }
    return AttachmentForUI(
        id          = this.id,
        url         = this.url,
        caption     = this.caption,
        filename    = filename,
        contentType = contentType
    )
}