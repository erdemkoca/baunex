package ch.baunex.company.controller

import ch.baunex.company.dto.CompanyDTO
import ch.baunex.company.facade.CompanyFacade
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.net.URI

@Path("/api/company")
@ApplicationScoped
class CompanyRestController {

    @Inject
    lateinit var facade: CompanyFacade

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getCompany(): CompanyDTO =
        facade.getCompany()
            ?: throw NotFoundException("Company-Einstellungen nicht vorhanden")

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun saveCompany(dto: CompanyDTO): Response {
        val result: CompanyDTO = if (dto.id == null) {
            facade.createCompany(dto)
        } else {
            facade.updateCompany(dto.id, dto)
        }
        // Location-Header beim Anlegen
        val location = URI.create("/api/company")
        return Response.ok(result).location(location).build()
    }
}
