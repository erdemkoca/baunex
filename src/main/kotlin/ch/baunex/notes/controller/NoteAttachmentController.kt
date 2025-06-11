package ch.baunex.notes.controller

import ch.baunex.notes.facade.NoteAttachmentFacade
import ch.baunex.notes.facade.NoteFacade
import ch.baunex.notes.service.NoteAttachmentService
import ch.baunex.timetracking.facade.TimeTrackingFacade
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestForm
import java.io.InputStream
import org.jboss.resteasy.reactive.multipart.FileUpload


@Path("/api/timetracking/note-attachment")
@ApplicationScoped
class NoteAttachmentController {

    @Inject lateinit var noteAttachmentFacade: NoteAttachmentFacade

    @POST
    @Path("/{noteId}/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    fun upload(
        @PathParam("noteId") noteId: Long,
        @RestForm fileStream: InputStream,
        @RestForm("file") fileDetails: FileUpload?
    ): Response {
        if (fileDetails == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "No file uploaded"))
                .build()
        }
        val dto = noteAttachmentFacade.uploadAttachment(
            noteId, fileStream, fileDetails.fileName()
        )
        return Response.ok(dto).build()
    }

    @DELETE
    @Path("/{attachmentId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun delete(@PathParam("attachmentId") id: Long): Response {
        return if (noteAttachmentFacade.deleteAttachment(id)) {
            Response.ok().build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
    }
}