package ch.baunex.user.facade

import ch.baunex.user.dto.CustomerCreateDTO
import ch.baunex.user.dto.CustomerDTO
import ch.baunex.user.service.CustomerService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.NotFoundException
import ch.baunex.user.mapper.toCustomerDTO
import ch.baunex.user.mapper.toCustomerModel

@ApplicationScoped
class CustomerFacade {

    @Inject
    lateinit var customerService: CustomerService

    fun findById(id: Long): CustomerDTO =
        customerService.findCustomerById(id)
            ?.toCustomerDTO()
            ?: throw NotFoundException("Customer mit ID $id nicht gefunden")

    fun listAll(): List<CustomerDTO> =
        customerService.listAllCustomers()
            .map { it.toCustomerDTO() }

    @Transactional
    fun create(dto: CustomerCreateDTO): CustomerDTO {
        val saved = customerService.createCustomer(dto.toCustomerModel())
        return saved.toCustomerDTO()
    }

    @Transactional
    fun update(id: Long, dto: CustomerCreateDTO): CustomerDTO {
        val updated = customerService.updateCustomer(id, dto)
        return updated.toCustomerDTO()
    }

    @Transactional
    fun delete(id: Long) {
        customerService.deleteCustomer(id)
    }
}
