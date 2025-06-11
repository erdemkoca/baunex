package ch.baunex.notes.facade

import ch.baunex.notes.dto.MediaAttachmentDto
import ch.baunex.notes.dto.NoteCreateDto
import ch.baunex.notes.dto.NoteDto
import ch.baunex.notes.service.NoteService
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class NoteFacade(private val noteService: NoteService) {

    fun getNote(noteId: Long): NoteDto =
        noteService.getNoteById(noteId)
}
