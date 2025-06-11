package ch.baunex.web

import ch.baunex.billing.facade.BillingFacade
import ch.baunex.company.facade.CompanyFacade
import ch.baunex.invoice.dto.InvoiceDTO
import ch.baunex.invoice.facade.InvoiceFacade
import ch.baunex.invoice.model.InvoiceStatus
import ch.baunex.project.facade.ProjectFacade
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.jboss.logging.Logger
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import ch.baunex.serialization.SerializationUtils.json


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

    private val logger = Logger.getLogger(WebInvoiceController::class.java)

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun list(): Response {
        val invoices = invoiceFacade.getAll()
        val projects = projectFacade.getAllProjects()
        val currentDate = LocalDate.now()
        val activeMenu = "invoice"
        val template = WebController.Templates.invoiceList(
            invoicesJson = json.encodeToString(invoices),
            projectsJson = json.encodeToString(projects),
            currentDate = currentDate,
            activeMenu = activeMenu
        )
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    fun new(@QueryParam("projectId") projectId: Long, @QueryParam("status") status: String?): Response {
        try {
            logger.info("Starting to create new invoice for project ID: $projectId with status: $status")
            
            val project = projectFacade.getProjectWithDetails(projectId)
            logger.info("Project found: ${project?.name}")
            
            if (project == null) {
                logger.error("Project not found with id: $projectId")
                throw IllegalArgumentException("Project not found with id: $projectId")
            }
            
            val company = companyFacade.getCompany()
            logger.info("Company found: ${company?.name}")
            
            if (company == null) {
                logger.error("Company information not found")
                throw IllegalStateException("Company information not found")
            }
            
            val billing = billingFacade.getBillingForProject(projectId)
            logger.info("Billing found for project: ${billing != null}")
            
            if (billing == null) {
                logger.error("Billing information not found for project: $projectId")
                throw IllegalStateException("Billing information not found for project: $projectId")
            }
            
            val currentDate = LocalDate.now()
            val activeMenu = "invoice"
            
            // Create a new empty invoice with the specified status
            val newInvoice = InvoiceDTO(
                id = null,
                invoiceNumber = invoiceFacade.generateInvoiceNumber(),
                invoiceDate = currentDate,
                dueDate = currentDate.plusDays(30),
                customerId = project.customerId,
                customerName = project.customerName,
                customerAddress = "${project.customer.street ?: ""}, ${project.customer.city ?: ""}",
                projectId = project.id,
                projectName = project.name,
                projectDescription = project.description,
                invoiceStatus = if (status == "DRAFT") InvoiceStatus.DRAFT else InvoiceStatus.ISSUED,
                items = emptyList(),
                totalAmount = 0.0,
                vatAmount = 0.0,
                grandTotal = 0.0,
                vatRate = company.defaultVatRate
            )
            
            val template = WebController.Templates.invoiceDetail(
                invoiceJson = json.encodeToString(newInvoice),
                currentDate = currentDate,
                activeMenu = activeMenu,
                companyJson = json.encodeToString(company),
                billingJson = json.encodeToString(billing)
            )
            
            return Response.ok(template.render()).build()
        } catch (e: Exception) {
            logger.error("Error creating new invoice", e)
            throw e
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    fun show(@PathParam("id") id: Long): Response {
        val invoice = invoiceFacade.getById(id)
        val company = companyFacade.getCompany()
        val billing = billingFacade.getBillingForProject(id)
        val currentDate = LocalDate.now()
        val activeMenu = "invoice"

        val template = WebController.Templates.invoiceDetail(
            invoiceJson = json.encodeToString(invoice),
            currentDate = currentDate,
            activeMenu = activeMenu,
            companyJson = json.encodeToString(company),
            billingJson = json.encodeToString(billing)
        )
        return Response.ok(template.render()).build()
    }

    @POST
    @Path("/{id}/publish")
    fun publishInvoice(@PathParam("id") id: Long): Response {
        return try {
            invoiceFacade.publishInvoice(id)
            Response.ok().build()
        } catch (e: Exception) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Fehler beim Veröffentlichen der Rechnung: ${e.message}")
                .build()
        }
    }

    // catch-all for other routes like /invoice/edit, etc.
    @GET
    @Path("/{path:^(?!\\d+$|new$).*}")
    @Produces(MediaType.TEXT_HTML)
    fun shellCatchAll(): Response =
        Response.ok(WebController.Templates.invoiceShell().render()).build()
}

