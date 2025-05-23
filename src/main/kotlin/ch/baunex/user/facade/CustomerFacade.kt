package ch.baunex.user.facade

import ch.baunex.user.dto.CustomerCreateDTO
import ch.baunex.user.dto.CustomerDTO
import ch.baunex.user.mapper.CustomerMapper
import ch.baunex.user.service.CustomerService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.NotFoundException

@ApplicationScoped
class CustomerFacade @Inject constructor(
    private val customerService: CustomerService,
    private val mapper: CustomerMapper
) {

    fun findById(id: Long): CustomerDTO =
        customerService.findCustomerModelById(id)
            ?.let { mapper.toDTO(it) }
            ?: throw NotFoundException("Customer mit ID $id nicht gefunden")

    fun listAll(): List<CustomerDTO> = customerService.getAll()

    fun getAllCustomers(): List<CustomerDTO> = listAll()

    fun getById(id: Long): CustomerDTO = customerService.getById(id)

    @Transactional
    fun create(dto: CustomerCreateDTO): CustomerDTO = customerService.createCustomer(dto)

    @Transactional
    fun update(id: Long, dto: CustomerCreateDTO): CustomerDTO = customerService.updateCustomer(id, dto)

    @Transactional
    fun delete(id: Long) = customerService.delete(id)
}
