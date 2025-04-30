package ch.baunex.user.facade

import ch.baunex.user.dto.CustomerContactCreateDTO
import ch.baunex.user.dto.CustomerContactDTO
import ch.baunex.user.dto.CustomerContactUpdateDTO
import ch.baunex.user.mapper.toDTO
import ch.baunex.user.mapper.toModel
import ch.baunex.user.mapper.applyTo
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
        val customer = customerService.findCustomerById(customerId)
            ?: throw NotFoundException("Kunde mit ID $customerId nicht gefunden")
        val person = personService.findPersonById(dto.personId)
            ?: throw NotFoundException("Person mit ID ${dto.personId} nicht gefunden")
        val model = dto.toModel(customer, person)
        return service.create(model).toDTO()
    }

    @Transactional
    fun update(id: Long, dto: CustomerContactUpdateDTO): CustomerContactDTO {
        val entity = service.findById(id)
            ?: throw NotFoundException("Kontakt mit ID $id nicht gefunden")
        dto.applyTo(entity)
        return entity.toDTO()
    }

    @Transactional
    fun delete(id: Long) {
        if (!service.delete(id)) {
            throw NotFoundException("Kontakt mit ID $id nicht gefunden")
        }
    }
}
