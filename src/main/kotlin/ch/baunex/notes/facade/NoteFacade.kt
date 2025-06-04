package ch.baunex.notes.facade

import ch.baunex.notes.dto.MediaAttachmentDto
import ch.baunex.notes.dto.NoteCreateDto
import ch.baunex.notes.dto.NoteDto
import ch.baunex.notes.service.NoteService
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class NoteFacade(private val noteService: NoteService) {

    fun getNotesByProject(projectId: Long): List<NoteDto> =
        noteService.listNotesByProject(projectId)

    fun getNotesByTimeEntry(timeEntryId: Long): List<NoteDto> =
        noteService.listNotesByTimeEntry(timeEntryId)

    fun getNotesByDocument(documentId: Long): List<NoteDto> =
        noteService.listNotesByDocument(documentId)

    fun getNote(noteId: Long): NoteDto =
        noteService.getNoteById(noteId)

    fun createNote(createDto: NoteCreateDto, userId: Long): NoteDto =
        noteService.createNote(createDto, userId)

    fun updateNote(noteId: Long, updateDto: NoteCreateDto, userId: Long): NoteDto =
        noteService.updateNote(noteId, updateDto, userId)

    fun deleteNote(noteId: Long, userId: Long) =
        noteService.deleteNote(noteId, userId)

    fun addAttachment(noteId: Long, url: String, type: String, caption: String?): MediaAttachmentDto =
        noteService.addAttachment(noteId, url, type, caption)

    fun getAttachments(noteId: Long): List<MediaAttachmentDto> =
        noteService.listAttachments(noteId)
}
