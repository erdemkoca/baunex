package ch.baunex.documentGenerator.controller

import ch.baunex.documentGenerator.dto.DocumentResponseDTO
import ch.baunex.documentGenerator.dto.GenericDocumentDTO
import ch.baunex.documentGenerator.facade.GenericDocumentFacade
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType

@Path("/api/documents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
class GenericDocumentController @Inject constructor(
    private val facade: GenericDocumentFacade
) {

    @GET
    fun list(): List<DocumentResponseDTO> =
        facade.listAll()

    @GET
    @Path("/{id}")
    fun get(@PathParam("id") id: Long): DocumentResponseDTO =
        facade.getById(id)

    @POST
    fun create(dto: GenericDocumentDTO): DocumentResponseDTO =
        facade.create(dto)

    @PUT
    @Path("/{id}")
    fun update(@PathParam("id") id: Long, dto: GenericDocumentDTO): DocumentResponseDTO =
        facade.update(id, dto)

    @DELETE
    @Path("/{id}")
    fun delete(@PathParam("id") id: Long) {
        facade.delete(id)
    }
}
