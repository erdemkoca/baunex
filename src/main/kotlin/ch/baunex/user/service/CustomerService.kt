package ch.baunex.user.service

import ch.baunex.user.dto.CustomerCreateDTO
import ch.baunex.user.dto.CustomerDTO
import ch.baunex.user.mapper.CustomerMapper
import ch.baunex.user.model.CustomerModel
import ch.baunex.user.repository.CustomerRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class CustomerService {

    @Inject
    lateinit var customerRepo: CustomerRepository

    @Inject
    lateinit var personService: PersonService

    @Inject
    lateinit var mapper: CustomerMapper

    @Transactional
    fun createCustomer(dto: CustomerCreateDTO): CustomerDTO {
        val entity = mapper.toEntity(dto)
        entity.customerNumber = generateNextCustomerNumber()
        customerRepo.persist(entity)
        return mapper.toDTO(entity)
    }

    fun generateNextCustomerNumber(): Int {
        val max = customerRepo.listAllCustomers()
            .map { it.customerNumber }
            .maxOrNull() ?: 1000
        return max + 1
    }


    fun findCustomerById(id: Long): CustomerDTO? =
        customerRepo.findById(id)?.let { mapper.toDTO(it) }

    fun findCustomerModelById(id: Long): CustomerModel? =
        customerRepo.findById(id)

    fun listAllCustomers(): List<CustomerDTO> =
        customerRepo.listAllCustomers().map { mapper.toDTO(it) }

    @Transactional
    fun updateCustomer(id: Long, dto: CustomerCreateDTO): CustomerDTO {
        val customer = customerRepo.findById(id)
            ?: throw IllegalArgumentException("Customer mit ID $id nicht gefunden")

        mapper.applyTo(dto, customer)
        customerRepo.persist(customer)
        return mapper.toDTO(customer)
    }

    @Transactional
    fun deleteCustomer(id: Long) {
        val customer = customerRepo.findById(id) ?: return
        customerRepo.delete(customer)
        personService.deletePerson(id)
    }

    fun getAll(): List<CustomerDTO> {
        return customerRepo.listAll().map { mapper.toDTO(it) }
    }

    fun getById(id: Long): CustomerDTO {
        val customer = customerRepo.findById(id) ?: throw IllegalArgumentException("Kunde nicht gefunden")
        return mapper.toDTO(customer)
    }

    @Transactional
    fun create(customer: CustomerDTO): CustomerDTO {
        val entity = mapper.toEntity(customer)
        customerRepo.persist(entity)
        return mapper.toDTO(entity)
    }

    @Transactional
    fun update(id: Long, customer: CustomerDTO): CustomerDTO {
        val existing = customerRepo.findById(id) ?: throw IllegalArgumentException("Kunde nicht gefunden")
        val updated = mapper.toEntity(customer)
        existing.person.firstName = updated.person.firstName
        existing.person.lastName = updated.person.lastName
        existing.person.email = updated.person.email
        existing.person.details.phone = updated.person.details.phone
        customerRepo.persist(existing)
        return mapper.toDTO(existing)
    }

    @Transactional
    fun delete(id: Long) {
        customerRepo.deleteById(id)
    }
}