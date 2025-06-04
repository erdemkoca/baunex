package ch.baunex.notes.mapper

import ch.baunex.notes.dto.MediaAttachmentDto
import ch.baunex.notes.dto.NoteDto
import ch.baunex.notes.model.MediaAttachmentModel
import ch.baunex.notes.model.NoteModel

fun NoteModel.toDto(): NoteDto {
    return NoteDto(
        id = this.id!!,
        projectId = this.project?.id,
        timeEntryId = this.timeEntry?.id,
        documentId = this.document?.id,
        createdById = this.createdBy.id!!,
        createdByName = "${this.createdBy.person.firstName} ${this.createdBy.person.lastName}",
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        title = this.title,
        content = this.content,
        category = this.category,
        tags = this.tags,
        attachments = this.attachments.map { it.toDto() }
    )
}

fun MediaAttachmentModel.toDto(): MediaAttachmentDto {
    return MediaAttachmentDto(
        id = this.id!!,
        url = this.url,
        type = this.type,
        caption = this.caption
    )
}
