package ch.baunex.web

import ch.baunex.billing.dto.BillingDTO
import ch.baunex.billing.facade.BillingFacade
import ch.baunex.catalog.dto.CatalogItemDTO
import ch.baunex.company.dto.CompanyDTO
import ch.baunex.company.facade.CompanyFacade
import ch.baunex.invoice.dto.InvoiceDTO
import ch.baunex.invoice.dto.InvoiceDraftDTO
import ch.baunex.invoice.facade.InvoiceFacade
import ch.baunex.invoice.model.InvoiceStatus
import ch.baunex.project.dto.ProjectDetailDTO
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.web.WebController.Templates
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.LocalDate

@Path("/invoice")
@ApplicationScoped
class WebInvoiceController {

    @Inject
    lateinit var invoiceFacade: InvoiceFacade

    @Inject
    lateinit var projectFacade: ProjectFacade

    @Inject
    lateinit var companyFacade: CompanyFacade

    @Inject
    lateinit var billingFacade: BillingFacade

    private val mapper = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
    }

    private fun getCurrentDate(): LocalDate = LocalDate.now()

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun list(): Response {
        val invoices = invoiceFacade.getAll()
        val projects = projectFacade.getAllProjects()
        val currentDate = LocalDate.now()
        val activeMenu = "invoice"
        val template = Templates.invoiceList(invoices, projects, currentDate, activeMenu)
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    fun new(@QueryParam("projectId") projectId: Long): Response {
        val selectedProject = projectId.let { projectFacade.getProjectWithDetails(it) }
        if (selectedProject?.customer == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Ungültiges Projekt").build()
        }
        val currentDate = getCurrentDate()
        val dueDate = currentDate.plusDays(30)
        val company = companyFacade.getCompany()
        val customer = selectedProject.customer
        val billing = billingFacade.getBillingForProject(selectedProject.id)

        val emptyInvoice = InvoiceDTO(
            id = null,
            invoiceNumber = "",
            invoiceDate = currentDate,
            dueDate = dueDate,
            customerId = customer.id,
            customerName = "${customer.firstName} ${customer.lastName}",
            customerAddress = "${customer.street}, ${customer.zipCode} ${customer.city}",
            projectId = selectedProject.id,
            projectName = selectedProject.name,
            projectDescription = selectedProject.description ?: "",
            invoiceStatus = InvoiceStatus.DRAFT,
            items = emptyList(),
            totalAmount = 0.0,
            vatAmount = 0.0,
            grandTotal = 0.0,
            vatRate = company?.defaultVatRate ?: 0.0,
            notes = emptyList()
        )

        val invoiceJson = mapper.writeValueAsString(emptyInvoice)
        val companyJson = mapper.writeValueAsString(company)
        val billingJson = mapper.writeValueAsString(billing)
        val template = Templates.invoiceDetail(invoiceJson, currentDate, "invoice", companyJson, billingJson)
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    fun show(@PathParam("id") id: Long): Response {
        val invoice = invoiceFacade.getById(id)
        val currentDate = LocalDate.now()
        val activeMenu = "invoice"
        val billing = billingFacade.getBillingForProject(invoice.projectId)
        
        val invoiceJson = mapper.writeValueAsString(invoice)
        val companyJson = mapper.writeValueAsString(companyFacade.getCompany())
        val billingJson = mapper.writeValueAsString(billing)
        val template = Templates.invoiceDetail(invoiceJson, currentDate, activeMenu, companyJson, billingJson)
        return Response.ok(template.render()).build()
    }

    @POST
    @Path("/{id}/mark-as-paid")
    @Produces(MediaType.APPLICATION_JSON)
    fun markAsPaid(@PathParam("id") id: Long) {
        invoiceFacade.markAsPaid(id)
    }

    @POST
    @Path("/{id}/cancel")
    @Produces(MediaType.APPLICATION_JSON)
    fun cancel(@PathParam("id") id: Long) {
        invoiceFacade.cancel(id)
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createInvoice(dto: InvoiceDraftDTO): Response {
        val result = invoiceFacade.createInvoice(dto)
        return Response.ok(result).build()
    }
}
