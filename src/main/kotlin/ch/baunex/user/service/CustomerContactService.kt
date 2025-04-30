package ch.baunex.user.service

import ch.baunex.user.dto.CustomerContactUpdateDTO
import ch.baunex.user.mapper.applyTo
import ch.baunex.user.model.CustomerContact
import ch.baunex.user.repository.CustomerContactRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class CustomerContactService {

    @Inject
    lateinit var contactRepo: CustomerContactRepository

    /**
     * Legt einen neuen Kontakt an.
     */
    @Transactional
    fun create(entity: CustomerContact): CustomerContact {
        contactRepo.persist(entity)
        return entity
    }

    /**
     * Gibt den Kontakt mit der angegebenen ID zurück oder null.
     */
    fun findById(id: Long): CustomerContact? =
        contactRepo.findById(id)

    /**
     * Listet alle Contacts zu einem bestimmten Kunden auf.
     */
    fun listByCustomer(customerId: Long): List<CustomerContact> =
        contactRepo.find("customer.id", customerId).list()

    /**
     * Updated einen vorhandenen Kontakt; liefert das geänderte Entity oder null, wenn nicht gefunden.
     */
    @Transactional
    fun update(id: Long, dto: CustomerContactUpdateDTO): CustomerContact? {
        val contact = contactRepo.findById(id) ?: return null
        dto.applyTo(contact)
        return contact
    }

    /**
     * Löscht den Kontakt; liefert true, wenn tatsächlich gelöscht wurde.
     */
    @Transactional
    fun delete(id: Long): Boolean {
        val contact = contactRepo.findById(id) ?: return false
        contactRepo.delete(contact)
        return true
    }
}
