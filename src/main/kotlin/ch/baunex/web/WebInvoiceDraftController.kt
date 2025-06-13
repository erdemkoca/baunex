package ch.baunex.web

import ch.baunex.invoice.dto.InvoiceDraftDTO
import ch.baunex.invoice.dto.InvoiceEntryDTO
import ch.baunex.invoice.service.InvoiceDraftService
import ch.baunex.invoice.facade.InvoiceDraftFacade
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.company.facade.CompanyFacade
import ch.baunex.billing.facade.BillingFacade
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestPath
import java.time.LocalDate
import java.net.URI

@Path("/invoice-drafts")
@ApplicationScoped
class WebInvoiceDraftController {

    @Inject
    lateinit var invoiceDraftService: InvoiceDraftService

    @Inject
    lateinit var invoiceDraftFacade: InvoiceDraftFacade

    @Inject
    lateinit var projectFacade: ProjectFacade

    @Inject
    lateinit var companyFacade: CompanyFacade

    @Inject
    lateinit var billingFacade: BillingFacade

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun list(): Response {
        val drafts = invoiceDraftFacade.getAll()
        val projects = projectFacade.getAllProjects()
        val currentDate = LocalDate.now()
        val activeMenu = "invoice-drafts"
        val template = WebController.Templates.invoiceDraftList(drafts, projects, currentDate, activeMenu)
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    fun show(@PathParam("id") id: Long): Response {
        val draft = invoiceDraftFacade.getById(id)
        val projects = projectFacade.getAllProjects()
        val currentDate = LocalDate.now()
        val activeMenu = "invoice-drafts"
        val template = WebController.Templates.invoiceDraftList(drafts = listOf(draft),projects, currentDate = currentDate, activeMenu = activeMenu)
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    fun newForm(@QueryParam("projectId") projectId: Long?): Response {
        val projects = projectFacade.getAllProjects()
        val customers = invoiceDraftFacade.getAllCustomers()
        val currentDate = LocalDate.now()
        val dueDate = currentDate.plusDays(30)
        val company = companyFacade.getCompany()

        val project = projectId?.let { projectFacade.getProjectWithDetails(it) }
        val billing = projectId?.let { billingFacade.getBillingForProject(it) }

        val mapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        val template = WebController.Templates.invoiceDraftForm(
            draft = null,
            customers = customers,
            projects = projects,
            currentDate = currentDate,
            dueDate = dueDate,
            activeMenu = "invoice-drafts",
            selectedProject = project,
            company = company,
            billing = billing
        )
            .data("projectJson", mapper.writeValueAsString(project))
            .data("companyJson", mapper.writeValueAsString(company))
            .data("billingJson", mapper.writeValueAsString(billing))

        return Response.ok(template.render()).build()
    }


    @POST
    @Path("/new")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun create(
        @FormParam("projectId") projectId: Long,
        @FormParam("serviceStartDate") serviceStartDate: String?,
        @FormParam("serviceEndDate") serviceEndDate: String?,
        @FormParam("notes") notes: String?
    ): Response {
        val project = projectFacade.getProjectWithDetails(projectId)
            ?: return Response.status(404).build()

        // Collect all entries: time entries, their catalog items, and project catalog items
        val entries = mutableListOf<InvoiceEntryDTO>()

        // Add time entries
        project.timeEntries.forEach { entry ->
            // Add the time entry itself
            entries.add(InvoiceEntryDTO(
                description = entry.notes,
                type = "VA",
                quantity = entry.hoursWorked,
                price = entry.hourlyRate,
                total = entry.hoursWorked * entry.hourlyRate
            ))

            // Add catalog items from this time entry
            entry.catalogItems.forEach { catalogItem ->
                entries.add(InvoiceEntryDTO(
                    description = catalogItem.itemName,
                    type = "IC",
                    quantity = catalogItem.quantity.toDouble(),
                    price = catalogItem.unitPrice,
                    total = catalogItem.totalPrice
                ))
            }
        }

        // Add project catalog items
        project.catalogItems.forEach { item ->
            entries.add(InvoiceEntryDTO(
                description = item.itemName,
                type = "IC",
                quantity = item.quantity.toDouble(),
                price = item.unitPrice,
                total = item.totalPrice
            ))
        }

        val draft = InvoiceDraftDTO(
            projectId = projectId,
            customerId = project.customerId,
            customerName = project.customerName,
            customerAddress = project.customer.street + ", " + project.customer.city,
            serviceStartDate = serviceStartDate?.let { LocalDate.parse(it) },
            serviceEndDate = serviceEndDate?.let { LocalDate.parse(it) },
            notes = notes,
            status = "DRAFT",
            entries = entries
        )

        val createdDraft = invoiceDraftFacade.create(draft)
        return Response.seeOther(URI.create("/invoice-drafts/${createdDraft.id}")).build()
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    fun editForm(@RestPath id: Long): Response {
        val draft = invoiceDraftFacade.getById(id)
        val customers = invoiceDraftFacade.getAllCustomers()
        val projects = projectFacade.getAllProjects()
        val currentDate = LocalDate.now()
        val dueDate = draft.dueDate ?: currentDate.plusDays(30)
        val activeMenu = "invoice-drafts"
        val template = WebController.Templates.invoiceDraftForm(
            draft = draft,
            customers = customers,
            projects = projects,
            currentDate = currentDate,
            dueDate = dueDate,
            activeMenu = activeMenu
        )
        return Response.ok(template.render()).build()
    }

    @POST
    @Path("/{id}/create-invoice")
    @Produces(MediaType.TEXT_HTML)
    fun createInvoice(@RestPath id: Long): Response {
        try {
            val draft = invoiceDraftService.getById(id)
            if (draft.status != "DRAFT") {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Nur Rechnungsentwürfe können in Rechnungen umgewandelt werden.")
                    .build()
            }

            val invoice = invoiceDraftService.createInvoiceFromDraft(id)
            return Response.seeOther(java.net.URI.create("/invoices/${invoice.id}")).build()
        } catch (e: Exception) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Fehler beim Erstellen der Rechnung: ${e.message}")
                .build()
        }
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    fun create(@FormParam("invoiceNumber") invoiceNumber: String,
              @FormParam("invoiceDate") invoiceDate: String,
              @FormParam("dueDate") dueDate: String,
              @FormParam("customerId") customerId: Long,
              @FormParam("projectId") projectId: Long,
              @FormParam("notes") notes: String?): Response {
        val draft = InvoiceDraftDTO(
            invoiceNumber = invoiceNumber,
            invoiceDate = java.time.LocalDate.parse(invoiceDate),
            dueDate = java.time.LocalDate.parse(dueDate),
            customerId = customerId,
            projectId = projectId,
            notes = notes,
            status = "DRAFT"
        )

        val created = invoiceDraftFacade.create(draft)
        return Response.seeOther(java.net.URI.create("/invoice-drafts/${created.id}")).build()
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    fun update(@RestPath id: Long,
              @FormParam("invoiceNumber") invoiceNumber: String,
              @FormParam("invoiceDate") invoiceDate: String,
              @FormParam("dueDate") dueDate: String,
              @FormParam("customerId") customerId: Long,
              @FormParam("projectId") projectId: Long,
              @FormParam("notes") notes: String?): Response {
        val draft = InvoiceDraftDTO(
            id = id,
            invoiceNumber = invoiceNumber,
            invoiceDate = java.time.LocalDate.parse(invoiceDate),
            dueDate = java.time.LocalDate.parse(dueDate),
            customerId = customerId,
            projectId = projectId,
            notes = notes,
            status = "DRAFT"
        )

        invoiceDraftFacade.update(id, draft)
        return Response.seeOther(java.net.URI.create("/invoice-drafts/${id}")).build()
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun delete(@RestPath id: Long): Response {
        invoiceDraftFacade.delete(id)
        return Response.ok().build()
    }
} 