package ch.baunex.web

import ch.baunex.company.dto.CompanyDTO
import ch.baunex.company.facade.CompanyFacade
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.LocalDate
import jakarta.ws.rs.Produces

@Path("/settings/company")
@Produces(MediaType.TEXT_HTML)
class WebCompanyController {

    @Inject
    lateinit var companyFacade: CompanyFacade

    @GET
    fun viewCompany(): Response {
        // hole den DTO (oder ein leeres DTO, falls noch keins angelegt)
        val dto = companyFacade.getCompany() ?: CompanyDTO()
        // serialisiere ihn in JSON
        val companyJson = kotlinx.serialization.json.Json
            .encodeToString(CompanyDTO.serializer(), dto)

        val tpl = WebController.Templates.companySettings(
            companyJson = companyJson,
            activeMenu    = "settings",
            currentDate   = LocalDate.now()
        )
        return Response.ok(tpl.render()).build()
    }

    // ↓ alle POST/@FormParam-Methoden hier löschen!
}
