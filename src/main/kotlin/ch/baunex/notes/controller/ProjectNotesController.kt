package ch.baunex.notes.controller

import ch.baunex.notes.dto.NoteCreateDto
import ch.baunex.notes.dto.NoteDto
import ch.baunex.notes.facade.NoteAttachmentFacade
import ch.baunex.notes.facade.NoteFacade
import ch.baunex.project.dto.ProjectNotesViewDTO
import ch.baunex.project.facade.ProjectFacade
import kotlinx.serialization.encodeToString
import ch.baunex.serialization.SerializationUtils.json
import ch.baunex.web.WebController.Templates
import org.jboss.resteasy.reactive.multipart.FileUpload
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestForm
import java.io.InputStream

@Path("/projects/{projectId}/notes")
@ApplicationScoped
class ProjectNotesController {

    @Inject
    lateinit var projectFacade: ProjectFacade

    @Inject
    lateinit var noteFacade: NoteFacade

    @Inject
    lateinit var noteAttachmentFacade: NoteAttachmentFacade

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun viewNotes(@PathParam("projectId") projectId: Long): Response {
        val projectNotes = noteFacade.getProjectNotesView(projectId)
        val tpl = Templates.projectNotes(
            projectNotesJson = json.encodeToString(projectNotes),
            activeMenu = "projects",
            activeSubMenu = "notes",
            projectId = projectId
        )
        return Response.ok(tpl.render()).build()
    }

    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    fun getNotesJson(@PathParam("projectId") projectId: Long): ProjectNotesViewDTO =
        noteFacade.getProjectNotesView(projectId)

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun addNoteJson(
        @PathParam("projectId") projectId: Long,
        createDto: NoteCreateDto
    ): NoteDto {
        // now return just the newly created note
        return noteFacade.addNoteToProject(createDto, createDto.createdById)
    }

    @POST
    @Path("/{noteId}/attachments")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    fun uploadNoteAttachment(
        @PathParam("projectId") projectId: Long,
        @PathParam("noteId")    noteId:    Long,
        @RestForm("file")       fileStream: InputStream,
        @RestForm("file")       fileMeta:   FileUpload?
    ): Response {
        // Resteasy Reactive FileUploada has a `size` property
        if (fileMeta == null || fileMeta.size() == 0L) {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "No file uploaded"))
                .build()
        }

        val dto = noteAttachmentFacade.uploadAttachment(
            noteId,
            fileStream,
            fileMeta.fileName()
        )
        return Response.ok(dto).build()
    }

    @DELETE
    @Path("/{noteId}/attachments/{attachmentId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun deleteAttachment(
        @PathParam("projectId")   projectId:    Long,
        @PathParam("noteId")      noteId:       Long,
        @PathParam("attachmentId") attachmentId: Long
    ): Response {
        return if (noteAttachmentFacade.deleteAttachment(attachmentId)) {
            Response.noContent().build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
    }
}
