package ch.baunex.invoice.service

import ch.baunex.invoice.dto.InvoiceDraftDTO
import ch.baunex.invoice.dto.InvoiceDTO
import ch.baunex.invoice.mapper.InvoiceDraftMapper
import ch.baunex.invoice.mapper.InvoiceMapper
import ch.baunex.invoice.model.InvoiceDraftModel
import ch.baunex.invoice.model.InvoiceModel
import ch.baunex.invoice.model.InvoiceStatus
import ch.baunex.invoice.repository.InvoiceDraftRepository
import ch.baunex.invoice.repository.InvoiceRepository
import ch.baunex.user.dto.CustomerDTO
import ch.baunex.user.service.CustomerService
import ch.baunex.project.dto.ProjectDTO
import ch.baunex.project.model.ProjectModel
import ch.baunex.project.repository.ProjectRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class InvoiceDraftService {

    @Inject
    lateinit var invoiceDraftRepository: InvoiceDraftRepository

    @Inject
    lateinit var invoiceRepository: InvoiceRepository

    @Inject
    lateinit var invoiceDraftMapper: InvoiceDraftMapper

    @Inject
    lateinit var invoiceMapper: InvoiceMapper

    @Inject
    lateinit var customerService: CustomerService

    @Inject
    lateinit var projectRepository: ProjectRepository

    @Transactional
    fun create(dto: InvoiceDraftDTO): InvoiceDraftModel {
        val model = invoiceDraftMapper.toModel(dto)
        invoiceDraftRepository.persist(model)
        return model
    }

    @Transactional
    fun update(id: Long, dto: InvoiceDraftDTO): InvoiceDraftModel {
        val existing = invoiceDraftRepository.findById(id) ?: throw IllegalArgumentException("Invoice draft not found")
        val updated = invoiceDraftMapper.toModel(dto)
        
        // Update all fields
        existing.invoiceNumber = updated.invoiceNumber
        existing.invoiceDate = updated.invoiceDate
        existing.dueDate = updated.dueDate
        existing.status = updated.status
        existing.customerId = updated.customerId
        existing.projectId = updated.projectId
        existing.totalAmount = updated.totalAmount
        existing.vatAmount = updated.vatAmount
        existing.grandTotal = updated.grandTotal
        existing.notes = updated.notes
        existing.items = updated.items

        invoiceDraftRepository.persist(existing)
        return existing
    }

    fun getById(id: Long): InvoiceDraftModel {
        return invoiceDraftRepository.findById(id) ?: throw IllegalArgumentException("Invoice draft not found")
    }

    fun getAll(): List<InvoiceDraftModel> {
        return invoiceDraftRepository.listAll()
    }

    fun getByProjectId(projectId: Long): List<InvoiceDraftModel> {
        return invoiceDraftRepository.findByProjectId(projectId)
    }

    fun getByCustomerId(customerId: Long): List<InvoiceDraftModel> {
        return invoiceDraftRepository.findByCustomerId(customerId)
    }

    @Transactional
    fun createInvoiceFromDraft(draftId: Long): InvoiceDTO {
        val draft = invoiceDraftRepository.findById(draftId)
            ?: throw IllegalArgumentException("Rechnungsentwurf nicht gefunden")

        if (draft.status != InvoiceStatus.DRAFT) {
            throw IllegalStateException("Nur Rechnungsentwürfe können in Rechnungen umgewandelt werden")
        }

        // Erstelle neue Rechnung aus dem Entwurf
        val invoice = InvoiceModel().apply {
            invoiceNumber = draft.invoiceNumber
            invoiceDate = draft.invoiceDate
            dueDate = draft.dueDate
            customerId = draft.customerId
            projectId = draft.projectId
            notes = draft.notes
            status = InvoiceStatus.CREATED
            totalAmount = draft.totalAmount
            vatAmount = draft.vatAmount
            grandTotal = draft.grandTotal
        }

        // Kopiere alle Positionen
        draft.items.forEach { draftItem ->
            val invoiceItem = InvoiceModel.InvoiceItemModel().apply {
                this.invoice = invoice
                type = draftItem.type
                description = draftItem.description
                quantity = draftItem.quantity
                unitPrice = draftItem.unitPrice
                vatRate = draftItem.vatRate
                totalAmount = draftItem.totalAmount
                vatAmount = draftItem.vatAmount
                grandTotal = draftItem.grandTotal
                itemOrder = draftItem.itemOrder
                timeEntryId = draftItem.timeEntryId
                catalogItemId = draftItem.catalogItemId
            }
            invoice.items.add(invoiceItem)
        }

        // Speichere die neue Rechnung
        invoiceRepository.persist(invoice)

        // Aktualisiere den Status des Entwurfs
        draft.status = InvoiceStatus.CONVERTED
        invoiceDraftRepository.persist(draft)

        return invoiceMapper.toDTO(invoice)
    }

    @Transactional
    fun delete(id: Long) {
        invoiceDraftRepository.deleteById(id)
    }

    fun getAllCustomers(): List<CustomerDTO> {
        return customerService.getAll()
    }

    fun getAllProjects(): List<ProjectModel> {
        return projectRepository.listAll()
    }
} 