package ch.baunex.notes.mapper

import ch.baunex.notes.dto.MediaAttachmentDto
import ch.baunex.notes.model.MediaAttachmentModel

fun MediaAttachmentModel.toDto(): MediaAttachmentDto {
    return MediaAttachmentDto(
        id = this.id!!,
        url = this.url,
        type = this.type,
        caption = this.caption
    )
}