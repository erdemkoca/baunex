package ch.baunex.invoice.repository

import ch.baunex.invoice.model.InvoiceModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class InvoiceRepository : PanacheRepository<InvoiceModel> {
    fun findByProjectId(projectId: Long): List<InvoiceModel> {
        return find("projectId", projectId).list()
    }

    fun findByCustomerId(customerId: Long): List<InvoiceModel> {
        return find("customerId", customerId).list()
    }
} 