package ch.baunex.notes.mapper

import ch.baunex.notes.dto.NoteDto
import ch.baunex.notes.dto.NoteForUI
import ch.baunex.notes.model.NoteModel
import java.time.LocalDate

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

fun NoteModel.toNoteForUI(source: String, entryId: Long? = null, entryDate: LocalDate? = null, entryTitle: String? = null): NoteForUI {
    return NoteForUI(
        id = this.id!!,
        title = this.title,
        content = this.content,
        category = this.category,
        tags = this.tags,
        createdById = this.createdBy.id!!,
        createdAt = this.createdAt,
        attachments = this.attachments.map { 
            val dto = it.toDto()
            dto.copy(url = "/api/upload/files/${dto.url.substringAfterLast('/')}").toAttachmentForUI() 
        },
        source = source,
        entryId = entryId,
        entryDate = entryDate,
        entryTitle = entryTitle
    )
}