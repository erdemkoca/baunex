package ch.baunex.user.service

import ch.baunex.user.dto.CustomerCreateDTO
import ch.baunex.user.mapper.applyTo
import ch.baunex.user.model.CustomerModel
import ch.baunex.user.repository.CustomerRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.math.BigDecimal

@ApplicationScoped
class CustomerService {

    @Inject
    lateinit var customerRepo: CustomerRepository

    @Inject
    lateinit var personService: PersonService

    @Transactional
    fun createCustomer(entity: CustomerModel): CustomerModel {
        customerRepo.persist(entity)
        return entity
    }

    fun findCustomerById(id: Long): CustomerModel? =
        customerRepo.findById(id)

    fun listAllCustomers(): List<CustomerModel> =
        customerRepo.listAllCustomers()

    @Transactional
    fun updateCustomer(id: Long, dto: CustomerCreateDTO): CustomerModel {
        val customer = customerRepo.findById(id)
            ?: throw IllegalArgumentException("Customer mit ID $id nicht gefunden")

        dto.applyTo(customer)

        return customer
    }


    @Transactional
    fun deleteCustomer(id: Long) {
        val customer = customerRepo.findById(id) ?: return
        customerRepo.delete(customer)
        personService.deletePerson(id)
    }
}