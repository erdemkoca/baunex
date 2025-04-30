package ch.baunex.web

import ch.baunex.user.dto.CustomerDTO
import ch.baunex.user.facade.CustomerFacade
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

    private fun getCurrentDate() = LocalDate.now()

    /** Kunden-Liste anzeigen */
    @GET
    fun list(): Response {
        val customers: List<CustomerDTO> = customerFacade.listAll()
        val template = WebController.Templates
            .customers(customers, getCurrentDate(),"customers")
        return Response.ok(template.render()).build()
    }

    /** Leeres Formular für neuen Kunden */
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
            customerNumber   = "",
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
            .customerDetail(empty, contacts = emptyList(), getCurrentDate(), "customers")
        return Response.ok(template.render()).build()
    }

    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    fun saveCustomer(@BeanParam form: CustomerForm): Response {
        val id = form.id
        if (id == null) {
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
        val template = WebController.Templates
            .customerDetail(customer, contacts = emptyList(), getCurrentDate(), "customers") // contacts will be loaded by sub-controller/tab
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
