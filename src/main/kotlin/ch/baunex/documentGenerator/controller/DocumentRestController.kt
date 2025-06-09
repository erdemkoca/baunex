package ch.baunex.documentGenerator.controller

import ch.baunex.documentGenerator.dto.DocumentDTO
import ch.baunex.documentGenerator.dto.DocumentResponseDTO
import ch.baunex.documentGenerator.facade.DocumentFacade
import ch.baunex.documentGenerator.model.DocumentModel
import ch.baunex.documentGenerator.service.DocumentService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.NotFoundException
import java.io.ByteArrayInputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Path("/api/document")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
class DocumentRestController {

    @Inject
    lateinit var documentFacade: DocumentFacade

    @GET
    fun getAllDocuments(): List<DocumentResponseDTO> = documentFacade.getAll()

    @GET
    @Path("/{id}")
    fun getDocument(@PathParam("id") id: Long): DocumentResponseDTO = documentFacade.getDocument(id)

    @POST
    fun createDocument(dto: DocumentDTO): DocumentResponseDTO = documentFacade.createDocument(dto)

    @PUT
    @Path("/{id}")
    fun updateDocument(@PathParam("id") id: Long, dto: DocumentDTO): DocumentResponseDTO =
        documentFacade.updateDocument(id, dto)

    @DELETE
    @Path("/{id}")
    fun deleteDocument(@PathParam("id") id: Long) = documentFacade.deleteDocument(id)

    @GET
    @Path("/{id}/pdf")
    @Produces("application/pdf")
    fun downloadPdf(@PathParam("id") id: Long): Response {
        try {
            // Versuche zuerst, ein existierendes Dokument zu finden
            val doc = try {
                documentFacade.service.getDocumentById(id)
            } catch (e: NotFoundException) {
                // Wenn kein Dokument existiert, erstelle ein neues aus der Rechnung
                documentFacade.service.createInvoiceDocument(id)
            }

            // Generiere die PDF
            val pdfBytes = documentFacade.service.generatePdfBytes(doc)

            // Erstelle den Dateinamen
            val filename = "rechnung_${doc.invoiceNumber ?: id}_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))}.pdf"

            // Erstelle die Response
            return Response.ok(ByteArrayInputStream(pdfBytes))
                .header("Content-Disposition", "attachment; filename=\"$filename\"")
                .build()
        } catch (e: Exception) {
            e.printStackTrace() // Für Debugging-Zwecke
            throw WebApplicationException("Fehler beim Generieren der PDF: ${e.message}", Response.Status.INTERNAL_SERVER_ERROR)
        }
    }
}
