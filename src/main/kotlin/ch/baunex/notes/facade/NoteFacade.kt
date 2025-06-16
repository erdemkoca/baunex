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

    /** Erzeugt eine neue Projekt-Note und ggf. eine DefectPosition */
//    fun createNoteForProject(projectId: Long, dto: NoteCreateDto): List<NoteDto> {
//        return noteService.createNoteForProject(projectId, dto)
//    }
}
