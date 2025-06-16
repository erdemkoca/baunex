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
import jakarta.ws.rs.core.GenericEntity
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
    ): Response {
        // Wir übergeben das ganze DTO und die projectId in einer Methode
        val created: NoteDto = noteFacade.createNoteForProject(projectId, createDto)

        // Liefern das frisch erstellte NoteDto zurück
        return Response.ok(created).build()
    }

    @POST
    @Path("/{noteId}/attachment")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    fun uploadNoteAttachment(
        @PathParam("noteId") noteId: Long,
        @RestForm("file") fileStream: InputStream,
        @RestForm("file") fileDetails: FileUpload?
    ): Response {
        if (fileDetails == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "No file uploaded"))
                .build()
        }
        return try {
            val dto = noteAttachmentFacade.uploadAttachment(
                noteId,
                fileStream,
                fileDetails.fileName()
            )
            Response.ok(dto).build()
        } catch (e: Exception) {
            Response.serverError()
                .entity(mapOf("error" to e.message))
                .build()
        }
    }
}
