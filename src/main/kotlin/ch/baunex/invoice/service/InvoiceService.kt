package ch.baunex.invoice.service

import ch.baunex.catalog.repository.ProjectCatalogItemRepository
import ch.baunex.invoice.dto.InvoiceDraftDTO
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

    fun getAll(): List<InvoiceModel> {
        return repository.listAll()
    }

    fun getById(id: Long): InvoiceModel {
        return repository.findById(id) ?: throw IllegalArgumentException("Invoice not found")
    }

    @Transactional
    fun createInvoice(dto: InvoiceDraftDTO): InvoiceModel {
        val newInvoice = InvoiceModel().apply {
            invoiceNumber = dto.invoiceNumber
                ?.takeIf { it.isNotBlank() }
                ?: generateInvoiceNumber()
            invoiceDate = LocalDate.parse(dto.invoiceDate)
            dueDate = LocalDate.parse(dto.dueDate)
            customerId = dto.customerId
            projectId = dto.projectId
            invoiceStatus = InvoiceStatus.ISSUED

            // 1. Lokale Referenz auf dieses InvoiceModel
            val invoiceEntity = this

            // 2. Notizen richtig mappen: Für jede NoteDto eine neue NoteModel anlegen
            this.notes = dto.notes.map { noteDto ->
                NoteModel().apply {
                    content = noteDto.content
                    title = noteDto.title
                    category = noteDto.category
                    tags = noteDto.tags
                    createdAt = noteDto.createdAt
                    createdBy = employeeService.findEmployeeById(noteDto.createdById)!!
                    invoice = invoiceEntity   // ← hier verknüpfe ich tatsächlich mit newInvoice
                }
            }.toMutableList()
        }

        // Artikel/Positionen anlegen
        val items = dto.items.map { itemDto ->
            InvoiceItemModel().apply {
                description = itemDto.description
                type = itemDto.type
                quantity = itemDto.quantity
                price = itemDto.price
                total = itemDto.quantity * itemDto.price
                timeEntryId = itemDto.timeEntryId
                projectCatalogItemId = itemDto.projectCatalogItemId
                invoice = newInvoice  // ← Verknüpfung mit derselben Invoice
            }
        }

        newInvoice.items = items.toMutableList()
        newInvoice.totalNetto = items.sumOf { it.total }
        newInvoice.vatAmount = newInvoice.totalNetto * dto.vatRate / 100
        newInvoice.totalBrutto = newInvoice.totalNetto + newInvoice.vatAmount

        newInvoice.persist()

        val timeEntryIds = newInvoice.items.mapNotNull { it.timeEntryId }
        val projectCatalogItemIds = newInvoice.items.mapNotNull { it.projectCatalogItemId }
        markOriginalEntriesAsInvoiced(newInvoice.id, timeEntryIds, projectCatalogItemIds)

        return newInvoice
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
        val lastInvoice = repository
            .listAll()
            .mapNotNull { it.invoiceNumber?.toIntOrNull() }
            .maxOrNull() ?: 1000

        return (lastInvoice + 1).toString().padStart(5, '0') // z. B. "01001"
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


} 