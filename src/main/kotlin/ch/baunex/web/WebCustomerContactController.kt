package ch.baunex.web

import ch.baunex.user.dto.CustomerContactDTO
import ch.baunex.user.facade.CustomerContactFacade
import ch.baunex.web.forms.CustomerContactForm
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime

@Path("/customers/{customerId}/contacts")
@Produces(MediaType.TEXT_HTML)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
class WebCustomerContactController {

    @Inject
    lateinit var contactFacade: CustomerContactFacade

    private fun getCurrentDate() = LocalDate.now()

    @GET
    fun list(@PathParam("customerId") customerId: Long): Response {
        val contacts = contactFacade.listByCustomer(customerId)
        val tpl = WebController.Templates
            .customerContacts(contacts, customerId, getCurrentDate(), "customers")
        return Response.ok(tpl.render()).build()
    }

    @GET
    @Path("/new")
    fun newContact(@PathParam("customerId") customerId: Long): Response {
        val now = LocalDateTime.now()
        val blank = CustomerContactDTO(
            id         = 0L,
            personId   = 0L,
            firstName  = "",
            lastName   = "",
            email      = null,
            street     = null,
            city       = null,
            zipCode    = null,
            country    = null,
            phone      = null,
            role       = null,
            isPrimary  = false,
            createdAt  = now,
            updatedAt  = now
        )
        val tpl = WebController.Templates
            .customerContactForm(blank, customerId, getCurrentDate(), "customers")
        return Response.ok(tpl.render()).build()
    }

    @GET
    @Path("/{contactId}/edit")
    fun editContact(
        @PathParam("customerId") customerId: Long,
        @PathParam("contactId") contactId: Long
    ): Response {
        val contact = contactFacade.findById(contactId)
        val tpl = WebController.Templates
            .customerContactForm(contact, customerId, getCurrentDate(), "customers")
        return Response.ok(tpl.render()).build()
    }

    @POST
    @Path("/save")
    @Transactional
    fun saveContact(
        @PathParam("customerId") customerId: Long,
        @BeanParam form: CustomerContactForm
    ): Response {
        if (form.id == null) {
            contactFacade.create(customerId, form.toCreateDTO())
        } else {
            contactFacade.update(form.id!!, form.toUpdateDTO())
        }
        // redirect back to the contacts list
        return Response.seeOther(URI("/customers/$customerId/contacts")).build()
    }

    @GET
    @Path("/{contactId}/delete")
    @Transactional
    fun delete(
        @PathParam("customerId") customerId: Long,
        @PathParam("contactId") contactId: Long
    ): Response {
        contactFacade.delete(contactId)
        return Response.seeOther(URI("/customers/$customerId/contacts")).build()
    }
}
