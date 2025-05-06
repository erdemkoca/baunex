package ch.baunex.user.facade

import ch.baunex.user.dto.CustomerContactCreateDTO
import ch.baunex.user.dto.CustomerContactDTO
import ch.baunex.user.dto.CustomerContactUpdateDTO
import ch.baunex.user.mapper.toDTO
import ch.baunex.user.mapper.toModel
import ch.baunex.user.mapper.applyTo
import ch.baunex.user.model.CustomerContact
import ch.baunex.user.model.PersonDetails
import ch.baunex.user.model.PersonModel
import ch.baunex.user.service.CustomerContactService
import ch.baunex.user.service.CustomerService
import ch.baunex.user.service.PersonService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.NotFoundException

@ApplicationScoped
class CustomerContactFacade {

    @Inject
    lateinit var service: CustomerContactService

    @Inject
    lateinit var customerService: CustomerService

    @Inject
    lateinit var personService: PersonService

    fun findById(id: Long): CustomerContactDTO =
        service.findById(id)
            ?.toDTO()
            ?: throw NotFoundException("Kontakt mit ID $id nicht gefunden")

    fun listByCustomer(customerId: Long): List<CustomerContactDTO> =
        service.listByCustomer(customerId)
            .map { it.toDTO() }

    @Transactional
    fun create(customerId: Long, dto: CustomerContactCreateDTO): CustomerContactDTO {
        val cust = customerService.findCustomerById(customerId)!!
        // create a new PersonModel from dto
        val person = PersonModel().apply {
            firstName = dto.firstName
            lastName  = dto.lastName
            email     = dto.email
            details   = PersonDetails(dto.street, dto.city, dto.zipCode, dto.country, dto.phone)
            persist()
        }
        val contact = CustomerContact().apply {
            customer      = cust
            contactPerson = person
            role          = dto.role
            isPrimary     = dto.isPrimary
            persist()
        }
        return contact.toDTO()
    }


    @Transactional
    fun update(id: Long, dto: CustomerContactUpdateDTO): CustomerContactDTO {
        val contact = service.findById(id)!!
        val person  = personService.findPersonById(dto.personId)!!
        // merge into person:
        person.apply {
            firstName = dto.firstName; lastName = dto.lastName
            email     = dto.email
            details   = PersonDetails(dto.street, dto.city, dto.zipCode, dto.country, dto.phone)
            persist()
        }
        contact.apply {
            role      = dto.role
            isPrimary = dto.isPrimary
            persist()
        }
        return contact.toDTO()
    }


    @Transactional
    fun delete(id: Long) {
        if (!service.delete(id)) {
            throw NotFoundException("Kontakt mit ID $id nicht gefunden")
        }
    }
}
