package ch.baunex.user.service

import ch.baunex.user.dto.CustomerContactCreateDTO
import ch.baunex.user.dto.CustomerContactUpdateDTO
import ch.baunex.user.mapper.applyTo
import ch.baunex.user.mapper.toModel
import ch.baunex.user.model.CustomerContact
import ch.baunex.user.model.CustomerModel
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
    fun createContact(customer: CustomerModel, dto: CustomerContactCreateDTO): CustomerContact {
        val contact = dto.toModel(customer)
        contactRepo.persist(contact)
        return contact
    }

    /**
     * Gibt den Kontakt mit der angegebenen ID zurück oder null.
     */
    fun findById(id: Long): CustomerContact? =
        contactRepo.findById(id)

    /**
     * Listet alle Contacts zu einem bestimmten Kunden auf.
     */
    fun getContactsForCustomer(customerId: Long): List<CustomerContact> =
        contactRepo.listByCustomerId(customerId)

    /**
     * Updated einen vorhandenen Kontakt; liefert das geänderte Entity oder null, wenn nicht gefunden.
     */
    @Transactional
    fun updateContact(id: Long, dto: CustomerContactUpdateDTO): CustomerContact {
        val contact = contactRepo.findById(id) ?: throw IllegalArgumentException("Contact not found")
        dto.applyTo(contact)
        contactRepo.persist(contact)
        return contact
    }

    /**
     * Löscht den Kontakt; liefert true, wenn tatsächlich gelöscht wurde.
     */
    @Transactional
    fun deleteContact(id: Long) {
        val contact = contactRepo.findById(id) ?: return
        contactRepo.delete(contact)
    }
}
