package ch.baunex.user.facade

import ch.baunex.user.dto.CustomerContactCreateDTO
import ch.baunex.user.dto.CustomerContactDTO
import ch.baunex.user.dto.CustomerContactUpdateDTO
import ch.baunex.user.dto.CustomerDTO
import ch.baunex.user.mapper.CustomerMapper
import ch.baunex.user.model.CustomerContact
import ch.baunex.user.model.CustomerModel
import ch.baunex.user.service.CustomerContactService
import ch.baunex.user.service.CustomerService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class CustomerContactFacade @Inject constructor(
    private val customerContactService: CustomerContactService,
    private val customerService: CustomerService,
    private val mapper: CustomerMapper
) {
    fun listByCustomer(customerId: Long): List<CustomerContactDTO> =
        customerContactService.getContactsForCustomer(customerId)
            .map { mapper.toContactDTO(it) }

    fun findById(id: Long): CustomerContactDTO {
        val contact = customerContactService.findById(id)
            ?: throw IllegalArgumentException("Contact not found")
        return mapper.toContactDTO(contact)
    }

    @Transactional
    fun create(customerId: Long, dto: CustomerContactCreateDTO): CustomerContactDTO {
        val customer = customerService.findCustomerModelById(customerId)
            ?: throw IllegalArgumentException("Customer mit ID $customerId nicht gefunden")
        val contact = customerContactService.createContact(customer, dto)
        return mapper.toContactDTO(contact)
    }

    @Transactional
    fun update(id: Long, dto: CustomerContactUpdateDTO): CustomerContactDTO {
        val contact = customerContactService.updateContact(id, dto)
        return mapper.toContactDTO(contact)
    }

    @Transactional
    fun delete(id: Long) = customerContactService.deleteContact(id)
}
