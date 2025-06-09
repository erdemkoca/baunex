package ch.baunex.documentGenerator.repository

import ch.baunex.documentGenerator.model.DocumentModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class DocumentRepository : PanacheRepository<DocumentModel> {
    fun findByIdWithEntries(id: Long): DocumentModel? {
        return find("FROM DocumentModel d LEFT JOIN FETCH d.entries WHERE d.id = ?1", id).firstResult()
    }
}
