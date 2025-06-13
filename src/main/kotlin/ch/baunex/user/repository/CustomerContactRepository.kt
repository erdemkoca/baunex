package ch.baunex.user.repository

import ch.baunex.user.model.CustomerContact
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class CustomerContactRepository : PanacheRepository<CustomerContact> {

    /**
     * Alle Kontakte für einen gegebenen Kunden.
     */
    fun listByCustomerId(customerId: Long): List<CustomerContact> =
        list("customer.id = ?1", customerId)

    /**
     * Findet den primären Kontakt (sofern definiert) für einen Kunden.
     */
    fun findPrimaryByCustomerId(customerId: Long): CustomerContact? =
        find("customer.id = ?1 and isPrimary = true", customerId).firstResult()
}
