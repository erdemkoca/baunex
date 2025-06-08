package ch.baunex.documentGenerator.controller

import ch.baunex.documentGenerator.dto.DocumentDTO
import ch.baunex.documentGenerator.dto.DocumentResponseDTO
import ch.baunex.documentGenerator.facade.DocumentFacade
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.logging.Logger

@Path("/api/document")
@ApplicationScoped
class DocumentRestController {

    @Inject
    lateinit var documentFacade: DocumentFacade

    private val logger = Logger.getLogger(DocumentRestController::class.java)

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun get(@PathParam("id") id: Long): DocumentResponseDTO =
        documentFacade.getById(id)

    @POST
    @Path("/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun newDocument(dto: DocumentDTO): DocumentResponseDTO {
        logger.info("Received new document request: $dto")
        return documentFacade.createDocument(dto)
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateDocument(@PathParam("id") id: Long, dto: DocumentDTO): DocumentResponseDTO {
        logger.info("Updating document $id with $dto")
        return documentFacade.updateDocument(id, dto)
    }

    @DELETE
    @Path("/{id}")
    fun deleteDocument(@PathParam("id") id: Long): Response {
        logger.info("Deleting document $id")
        documentFacade.deleteDocument(id)
        return Response.noContent().build()
    }

    @GET
    @Path("/{id}/pdf")
    @Produces("application/pdf")
    fun downloadPdf(@PathParam("id") id: Long): Response {
        logger.info("Generating PDF for document $id")
        try {
            // Versuche zuerst, das Dokument zu finden
            val doc = documentFacade.service.getDocumentById(id)
            val pdfBytes = documentFacade.service.generatePdfBytes(doc)
            return Response.ok(pdfBytes)
                .header("Content-Disposition", "attachment; filename=document-$id.pdf")
                .build()
        } catch (e: NotFoundException) {
            // Wenn das Dokument nicht existiert, erstelle es aus der Rechnung
            logger.info("Document not found, creating from invoice $id")
            val doc = documentFacade.service.createInvoiceDocument(id)
            val pdfBytes = documentFacade.service.generatePdfBytes(doc)
            return Response.ok(pdfBytes)
                .header("Content-Disposition", "attachment; filename=document-$id.pdf")
                .build()
        }
    }
}
