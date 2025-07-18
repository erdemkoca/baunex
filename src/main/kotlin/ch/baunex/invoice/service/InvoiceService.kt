package ch.baunex.invoice.service

import ch.baunex.catalog.repository.ProjectCatalogItemRepository
import ch.baunex.invoice.dto.InvoiceDTO
import ch.baunex.invoice.dto.InvoiceNewDraftDTO
import ch.baunex.invoice.mapper.InvoiceMapper
import ch.baunex.invoice.model.InvoiceItemModel
import ch.baunex.invoice.model.InvoiceModel
import ch.baunex.invoice.model.InvoiceStatus
import ch.baunex.invoice.repository.InvoiceRepository
import ch.baunex.notes.model.NoteModel
import ch.baunex.timetracking.repository.TimeEntryRepository
import ch.baunex.user.service.EmployeeService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.LocalDate

@ApplicationScoped
class InvoiceService {

    @Inject
    lateinit var repository: InvoiceRepository

    @Inject
    lateinit var timeEntryRepository: TimeEntryRepository

    @Inject
    lateinit var projectCatalogItemRepository: ProjectCatalogItemRepository

    @Inject
    lateinit var employeeService: EmployeeService

    @Inject
    lateinit var mapper: InvoiceMapper

    fun getAll(): List<InvoiceModel> {
        return repository.find("FROM InvoiceModel i").list<InvoiceModel>()
    }

    fun getById(id: Long): InvoiceModel {
        return repository.findById(id) ?: throw IllegalArgumentException("Invoice not found")
    }

    @Transactional
    fun createNewInvoice(dto: InvoiceNewDraftDTO): InvoiceModel {
        val newInvoice = mapper.toModelFromNewDraft(dto)
        newInvoice.persist()

        val timeEntryIds = newInvoice.items.mapNotNull { it.timeEntryId }
        val projectCatalogItemIds = newInvoice.items.mapNotNull { it.projectCatalogItemId }
        markOriginalEntriesAsInvoiced(newInvoice.id, timeEntryIds, projectCatalogItemIds)

        return newInvoice
    }

    @Transactional
    fun createDraftInvoice(dto: InvoiceDTO): InvoiceModel {
        if (dto.id == null) {
            throw IllegalArgumentException("Cannot create draft invoice without ID. Use createNewInvoice for new invoices.")
        }
        
        val invoice = getById(dto.id)
        mapper.updateModelFromDTO(invoice, dto).apply {
            invoiceStatus = InvoiceStatus.DRAFT
        }
        
        repository.persist(invoice)
        return invoice
    }

    @Transactional
    fun createIssuedInvoice(dto: InvoiceDTO): InvoiceModel {
        if (dto.id == null) {
            throw IllegalArgumentException("Cannot create issued invoice without ID. Use createNewInvoice for new invoices.")
        }
        
        val invoice = getById(dto.id)
        mapper.updateModelFromDTO(invoice, dto).apply {
            invoiceStatus = InvoiceStatus.ISSUED
        }
        
        repository.persist(invoice)
        return invoice
    }

    @Transactional
    fun markAsPaid(id: Long) {
        val invoice = getById(id)
        invoice.invoiceStatus = InvoiceStatus.PAID
        repository.persist(invoice)
    }

    @Transactional
    fun cancel(id: Long) {
        val invoice = getById(id)
        invoice.invoiceStatus = InvoiceStatus.CANCELLED
        repository.persist(invoice)
    }

    fun generateInvoiceNumber(): String {
        val invoices = repository.find("FROM InvoiceModel i").list<InvoiceModel>()
        val lastInvoice = invoices
            .mapNotNull { it.invoiceNumber?.toIntOrNull() }
            .maxOrNull() ?: 1000

        return (lastInvoice + 1).toString().padStart(5, '0')
    }

    @Transactional
    fun markOriginalEntriesAsInvoiced(
        invoiceId: Long,
        timeEntryIds: List<Long>,
        projectCatalogItemIds: List<Long>
    ) {
        val invoice = getById(invoiceId)

        timeEntryRepository
            .list("id in ?1", timeEntryIds)
            .forEach { it.invoice = invoice }

        projectCatalogItemRepository
            .list("id in ?1", projectCatalogItemIds)
            .forEach { it.invoice = invoice }
    }

    @Transactional
    fun updateInvoiceStatus(id: Long, newStatus: InvoiceStatus) {
        val invoice = getById(id)
        invoice.invoiceStatus = newStatus
        repository.persist(invoice)
    }

} 