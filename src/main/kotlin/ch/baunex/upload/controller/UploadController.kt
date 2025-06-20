package ch.baunex.upload.controller

import ch.baunex.upload.dto.UploadResponse
import ch.baunex.upload.service.UploadService
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestForm
import java.nio.file.Files
import java.nio.file.Paths

@Path("/api/upload")
class UploadController {

    @Inject
    lateinit var uploadService: UploadService

    @GET
    @Path("/files/{filename}")
    fun serveFile(@PathParam("filename") filename: String): Response {
        val file = Paths.get("uploads", filename)
        if (!Files.exists(file)) {
            return Response.status(Response.Status.NOT_FOUND).build()
        }

        val contentType = when {
            filename.endsWith(".jpg") || filename.endsWith(".jpeg") -> "image/jpeg"
            filename.endsWith(".png") -> "image/png"
            filename.endsWith(".gif") -> "image/gif"
            else -> "application/octet-stream"
        }

        return Response.ok(Files.readAllBytes(file))
            .header("Content-Type", contentType)
            .header("Content-Disposition", "inline; filename=\"$filename\"")
            .build()
    }

    @POST
    @Path("/logo")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    fun uploadLogo(@RestForm file: java.io.InputStream, @RestForm("file") fileDetails: org.jboss.resteasy.reactive.multipart.FileUpload?): Response {
        if (fileDetails == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(mapOf("error" to "No file uploaded")).build()
        }

        try {
            val url = uploadService.saveLogo(file, fileDetails)
            return Response.ok(UploadResponse(url)).build()
        } catch (e: Exception) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to "Failed to upload file: ${e.message}"))
                .build()
        }
    }
}