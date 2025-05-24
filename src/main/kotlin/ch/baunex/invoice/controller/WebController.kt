package ch.baunex.invoice.controller

import ch.baunex.invoice.dto.InvoiceDraftDTO
import ch.baunex.invoice.dto.ProjectDTO
import ch.baunex.invoice.facade.InvoiceDraftFacade
import ch.baunex.project.facade.ProjectFacade
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import java.time.LocalDate

@Path("/")
class WebController {

    @Inject
    lateinit var invoiceDraftForm: Template

    @Inject
    lateinit var invoiceDraftFacade: InvoiceDraftFacade

    @Inject
    lateinit var projectFacade: ProjectFacade

    @GET
    @Path("/invoice-drafts/new")
    @Produces(MediaType.TEXT_HTML)
    fun newInvoiceDraftForm(): TemplateInstance {
        val currentDate = LocalDate.now()
        return invoiceDraftForm
            .data("draft", null)
            .data("projects", projectFacade.getAllProjects())
            .data("currentDate", currentDate)
    }

    @GET
    @Path("/invoice-drafts/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    fun editInvoiceDraftForm(@PathParam("id") id: Long): TemplateInstance {
        val draft = invoiceDraftFacade.getById(id)
        val currentDate = LocalDate.now()
        return invoiceDraftForm
            .data("draft", draft)
            .data("projects", projectFacade.getAllProjects())
            .data("currentDate", currentDate)
    }

    @POST
    @Path("/invoice-drafts/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    fun updateInvoiceDraft(
        @PathParam("id") id: Long,
        @FormParam("projectId") projectId: Long,
        @FormParam("serviceStartDate") serviceStartDate: String?,
        @FormParam("serviceEndDate") serviceEndDate: String?,
        @FormParam("notes") notes: String?
    ): String {
        val draft = invoiceDraftFacade.getById(id)
        val updatedDraft = draft.copy(
            projectId = projectId,
            serviceStartDate = serviceStartDate?.let { LocalDate.parse(it) },
            serviceEndDate = serviceEndDate?.let { LocalDate.parse(it) },
            notes = notes
        )
        invoiceDraftFacade.update(id, updatedDraft)
        return "redirect:/invoice-drafts"
    }

    @POST
    @Path("/invoice-drafts")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    fun createInvoiceDraft(
        @FormParam("projectId") projectId: Long,
        @FormParam("serviceStartDate") serviceStartDate: String?,
        @FormParam("serviceEndDate") serviceEndDate: String?,
        @FormParam("notes") notes: String?
    ): String {
        val currentDate = LocalDate.now()
        val newDraft = InvoiceDraftDTO(
            projectId = projectId,
            invoiceDate = currentDate,
            dueDate = currentDate.plusDays(30),
            serviceStartDate = serviceStartDate?.let { LocalDate.parse(it) },
            serviceEndDate = serviceEndDate?.let { LocalDate.parse(it) },
            notes = notes,
            status = "DRAFT"
        )
        invoiceDraftFacade.create(newDraft)
        return "redirect:/invoice-drafts"
    }
} 