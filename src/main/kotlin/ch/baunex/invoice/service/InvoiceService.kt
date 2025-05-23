package ch.baunex.invoice.service

import ch.baunex.invoice.model.InvoiceModel
import ch.baunex.invoice.model.InvoiceStatus
import ch.baunex.invoice.repository.InvoiceRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class InvoiceService {

    @Inject
    lateinit var repository: InvoiceRepository

    fun getAll(): List<InvoiceModel> {
        return repository.listAll()
    }

    fun getById(id: Long): InvoiceModel {
        return repository.findById(id) ?: throw IllegalArgumentException("Invoice not found")
    }

    @Transactional
    fun markAsPaid(id: Long) {
        val invoice = getById(id)
        invoice.status = InvoiceStatus.PAID
        repository.persist(invoice)
    }

    @Transactional
    fun cancel(id: Long) {
        val invoice = getById(id)
        invoice.status = InvoiceStatus.CANCELLED
        repository.persist(invoice)
    }
} 