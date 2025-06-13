package ch.baunex.web

import ch.baunex.user.dto.CustomerContactDTO
import ch.baunex.user.dto.CustomerDTO
import ch.baunex.user.facade.CustomerContactFacade
import ch.baunex.user.facade.CustomerFacade
import ch.baunex.web.forms.CustomerForm
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime

@Path("/customers")
@Produces(MediaType.TEXT_HTML)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
class WebCustomerController {

    @Inject
    lateinit var customerFacade: CustomerFacade

    @Inject
    lateinit var customerContactFacade: CustomerContactFacade

    private fun getCurrentDate() = LocalDate.now()

    @GET
    fun list(): Response {
        val customers: List<CustomerDTO> = customerFacade.listAll()
        val template = WebController.Templates
            .customers(customers, getCurrentDate(),"customers")
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/new")
    fun newCustomer(): Response {
        val now = LocalDateTime.now()
        val empty = CustomerDTO(
            id               = 0L,
            firstName        = "",
            lastName         = "",
            email            = null,
            street           = null,
            city             = null,
            zipCode          = null,
            country          = null,
            phone            = null,
            customerNumber   = 0,
            formattedCustomerNumber = "",
            companyName      = null,
            paymentTerms     = null,
            creditLimit      = null,
            industry         = null,
            discountRate     = null,
            preferredLanguage= null,
            marketingConsent = false,
            taxId            = null,
            createdAt        = now,
            updatedAt        = now,
            contacts         = emptyList()
        )
        val template = WebController.Templates
            .customerDetail(empty, emptyList(), getCurrentDate(), "customers")
        return Response.ok(template.render()).build()
    }

    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    fun saveCustomer(@BeanParam form: CustomerForm): Response {
        val id = form.id
        if (id == null || id == 0L) {
            customerFacade.create(form.toCreateDTO())
        } else {
            customerFacade.update(id, form.toCreateDTO())
        }
        return Response.seeOther(URI("/customers")).build()
    }

    @GET
    @Path("/{id}")
    fun view(@PathParam("id") id: Long): Response {
        val customer = customerFacade.findById(id)
        val contacts: List<CustomerContactDTO> = customerContactFacade.listByCustomer(id)
        val template = WebController.Templates
            .customerDetail(customer, contacts, getCurrentDate(), "customers")
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/{id}/delete")
    @Transactional
    fun deleteCustomer(@PathParam("id") id: Long): Response {
        customerFacade.delete(id)
        return Response.seeOther(URI("/customers")).build()
    }
}
