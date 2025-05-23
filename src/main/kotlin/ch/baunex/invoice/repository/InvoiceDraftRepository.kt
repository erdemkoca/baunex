package ch.baunex.invoice.repository

import ch.baunex.invoice.model.InvoiceDraftModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class InvoiceDraftRepository : PanacheRepository<InvoiceDraftModel> {
    fun findByProjectId(projectId: Long): List<InvoiceDraftModel> {
        return find("projectId", projectId).list()
    }

    fun findByCustomerId(customerId: Long): List<InvoiceDraftModel> {
        return find("customerId", customerId).list()
    }

    fun findByStatus(status: String): List<InvoiceDraftModel> {
        return find("status", status).list()
    }
} 