package ch.baunex.web

import ch.baunex.company.dto.CompanyDTO
import ch.baunex.company.facade.CompanyFacade
import ch.baunex.web.WebController.Templates
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.net.URI
import java.time.LocalDate

@Path("/settings/company")
@Produces(MediaType.TEXT_HTML)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
class WebCompanyController {

    @Inject
    lateinit var companyFacade: CompanyFacade

    @GET
    fun viewCompany(): Response {
        val company = companyFacade.getCompany()
        val tpl = Templates.companySettings(
            company = company,
            activeMenu = "settings",
            currentDate = LocalDate.now()
        )
        return Response.ok(tpl.render()).build()
    }

    @POST
    @Path("/save")
    @Transactional
    fun save(
        @FormParam("id") id: Long?,
        @FormParam("name") name: String,
        @FormParam("street") street: String,
        @FormParam("city") city: String,
        @FormParam("zipCode") zipCode: String,
        @FormParam("country") country: String,
        @FormParam("phone") phone: String?,
        @FormParam("email") email: String?,
        @FormParam("website") website: String?,
        @FormParam("iban") iban: String?,
        @FormParam("bic") bic: String?,
        @FormParam("bankName") bankName: String?,
        @FormParam("vatNumber") vatNumber: String?,
        @FormParam("taxNumber") taxNumber: String?,
        @FormParam("logo") logo: String?,
        @FormParam("defaultInvoiceFooter") defaultInvoiceFooter: String?,
        @FormParam("defaultInvoiceTerms") defaultInvoiceTerms: String?,
        @FormParam("defaultVatRate") defaultVatRate: Double
    ): Response {
        val dto = CompanyDTO(
            id = id,
            name = name,
            street = street,
            city = city,
            zipCode = zipCode,
            country = country,
            phone = phone,
            email = email,
            website = website,
            iban = iban,
            bic = bic,
            bankName = bankName,
            vatNumber = vatNumber,
            taxNumber = taxNumber,
            logo = logo,
            defaultInvoiceFooter = defaultInvoiceFooter,
            defaultInvoiceTerms = defaultInvoiceTerms,
            defaultVatRate = defaultVatRate
        )

        if (id == null) {
            companyFacade.createCompany(dto)
        } else {
            companyFacade.updateCompany(id, dto)
        }

        return Response.seeOther(URI("/settings/company")).build()
    }
} 