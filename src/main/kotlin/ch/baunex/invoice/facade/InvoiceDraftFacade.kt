package ch.baunex.invoice.facade

import ch.baunex.invoice.dto.InvoiceDraftDTO
import ch.baunex.user.dto.CustomerDTO
import ch.baunex.invoice.mapper.InvoiceDraftMapper
import ch.baunex.invoice.service.InvoiceDraftService
import ch.baunex.project.dto.ProjectListDTO
import ch.baunex.project.mapper.ProjectMapper
import ch.baunex.user.facade.CustomerFacade
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class InvoiceDraftFacade @Inject constructor(
    private val service: InvoiceDraftService,
    private val customerFacade: CustomerFacade,
    private val projectMapper: ProjectMapper
) {

    @Inject
    lateinit var mapper: InvoiceDraftMapper

    fun getAllDrafts(): List<InvoiceDraftDTO> {
        return service.getAll().map { mapper.toDTO(it) }
    }

    fun getDraftById(id: Long): InvoiceDraftDTO {
        return mapper.toDTO(service.getById(id))
    }

    fun createDraft(dto: InvoiceDraftDTO): InvoiceDraftDTO {
        return mapper.toDTO(service.create(dto))
    }

    fun updateDraft(id: Long, dto: InvoiceDraftDTO): InvoiceDraftDTO {
        return mapper.toDTO(service.update(id, dto))
    }

    fun getDraftsByProject(projectId: Long): List<InvoiceDraftDTO> {
        return service.getByProjectId(projectId).map { mapper.toDTO(it) }
    }

    fun getDraftsByCustomer(customerId: Long): List<InvoiceDraftDTO> {
        return service.getByCustomerId(customerId).map { mapper.toDTO(it) }
    }

    fun deleteDraft(id: Long) {
        service.delete(id)
    }

    fun createInvoiceFromDraft(id: Long): InvoiceDraftDTO {
        val invoice = service.createInvoiceFromDraft(id)
        return mapper.toDTO(service.getById(id))
    }

    fun getAllCustomers(): List<CustomerDTO> {
        return customerFacade.getAllCustomers()
    }

    fun getAllProjects(): List<ProjectListDTO> {
        return service.getAllProjects()
            .map { projectMapper.toListDTO(it) }
    }
} 