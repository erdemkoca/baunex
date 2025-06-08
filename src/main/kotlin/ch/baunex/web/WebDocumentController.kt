//package ch.baunex.web
//
//import ch.baunex.documentGenerator.dto.DocumentDTO
//import ch.baunex.documentGenerator.facade.DocumentFacade
//import ch.baunex.documentGenerator.pdf.PdfRenderer
//import io.quarkus.qute.TemplateInstance
//import jakarta.inject.Inject
//import jakarta.ws.rs.*
//import jakarta.ws.rs.core.MediaType
//import jakarta.ws.rs.core.Response
//
//@Path("/api/documents")
//@Produces(MediaType.APPLICATION_JSON)
//@Consumes(MediaType.APPLICATION_JSON)
//class WebDocumentController {
//
//    @Inject
//    lateinit var documentFacade: DocumentFacade
//
//    @GET
//    @Path("/{id}/pdf")
//    fun generatePdf(@PathParam("id") id: Long): Response {
//        val pdfBytes = documentFacade.generatePdf(id)          // ← ByteArray from facade
//        return Response.ok(pdfBytes)
//            .header("Content-Disposition", "inline; filename=\"document-$id.pdf\"")
//            .build()
//    }
//
//    @POST
//    fun createDocument(dto: DocumentDTO): Response {
//        val created = documentFacade.createDocument(dto)
//        return Response.ok(mapOf("id" to created.id)).build()
//    }
//
//    @GET
//    @Path("/{id}")
//    fun getDocument(@PathParam("id") id: Long): Response {
//        val doc = documentFacade.getById(id)
//        return Response.ok(doc).build()
//    }
//
//    @PUT
//    @Path("/{id}")
//    fun updateDocument(@PathParam("id") id: Long, dto: DocumentDTO): Response {
//        val updated = documentFacade.updateDocument(id, dto)
//        return Response.ok(mapOf("id" to updated.id)).build()
//    }
//
//    @DELETE
//    @Path("/{id}")
//    fun deleteDocument(@PathParam("id") id: Long): Response {
//        documentFacade.deleteDocument(id)
//        return Response.noContent().build()
//    }
//}
