package ch.baunex.documentGenerator.repository

import ch.baunex.documentGenerator.model.DocumentEntryModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class DocumentEntryRepository : PanacheRepository<DocumentEntryModel>
