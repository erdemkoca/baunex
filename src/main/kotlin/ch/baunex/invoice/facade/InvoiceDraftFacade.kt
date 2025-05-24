package ch.baunex.invoice.facade

import ch.baunex.invoice.dto.InvoiceDraftDTO
import ch.baunex.invoice.service.InvoiceDraftService
import ch.baunex.project.dto.ProjectListDTO
import ch.baunex.project.mapper.ProjectMapper
import ch.baunex.user.dto.CustomerDTO
import ch.baunex.user.facade.CustomerFacade
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class InvoiceDraftFacade {

    @Inject
    lateinit var invoiceDraftService: InvoiceDraftService

    @Inject
    lateinit var customerFacade: CustomerFacade

    @Inject
    lateinit var projectMapper: ProjectMapper

    fun getById(id: Long): InvoiceDraftDTO {
        return invoiceDraftService.getById(id).let { invoiceDraftService.invoiceDraftMapper.toDTO(it) }
    }

    fun getAll(): List<InvoiceDraftDTO> {
        return invoiceDraftService.getAll().map { invoiceDraftService.invoiceDraftMapper.toDTO(it) }
    }

    fun getByProjectId(projectId: Long): List<InvoiceDraftDTO> {
        return invoiceDraftService.getByProjectId(projectId).map { invoiceDraftService.invoiceDraftMapper.toDTO(it) }
    }

    fun getByCustomerId(customerId: Long): List<InvoiceDraftDTO> {
        return invoiceDraftService.getByCustomerId(customerId).map { invoiceDraftService.invoiceDraftMapper.toDTO(it) }
    }

    fun create(dto: InvoiceDraftDTO): InvoiceDraftDTO {
        return invoiceDraftService.create(dto).let { invoiceDraftService.invoiceDraftMapper.toDTO(it) }
    }

    fun update(id: Long, dto: InvoiceDraftDTO): InvoiceDraftDTO {
        return invoiceDraftService.update(id, dto).let { invoiceDraftService.invoiceDraftMapper.toDTO(it) }
    }

    fun delete(id: Long) {
        invoiceDraftService.delete(id)
    }

    fun getAllCustomers(): List<CustomerDTO> {
        return customerFacade.listAll()
    }

    fun getAllProjects(): List<ProjectListDTO> {
        return invoiceDraftService.getAllProjects().map(projectMapper::toListDTO)
    }
} 