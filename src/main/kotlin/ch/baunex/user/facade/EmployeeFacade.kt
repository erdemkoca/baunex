// --- EmployeeFacade.kt ---
package ch.baunex.user.facade

import ch.baunex.user.dto.EmployeeCreateDTO
import ch.baunex.user.dto.EmployeeDTO
import ch.baunex.user.mapper.toEmployeeDTO
import ch.baunex.user.model.Role
import ch.baunex.user.service.EmployeeService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.NotFoundException

@ApplicationScoped
class EmployeeFacade @Inject constructor(
    private val employeeService: EmployeeService
) {

    fun findById(id: Long): EmployeeDTO =
        employeeService.findEmployeeById(id)
            ?.toEmployeeDTO()
            ?: throw NotFoundException("Employee mit ID $id nicht gefunden")

    fun listAll(): List<EmployeeDTO> =
        employeeService.listAllEmployees().map { it.toEmployeeDTO() }

    @Transactional
    fun create(createDTO: EmployeeCreateDTO): EmployeeDTO {
        val saved = employeeService.createEmployee(createDTO)
        return saved.toEmployeeDTO()
    }

    @Transactional
    fun update(id: Long, updateDTO: EmployeeCreateDTO): EmployeeDTO {
        val updated = employeeService.updateEmployee(id, updateDTO)
        return updated.toEmployeeDTO()
    }

    @Transactional
    fun delete(id: Long) {
        employeeService.deleteEmployee(id)
    }

    fun findByRole(role: Role): EmployeeDTO{
        return employeeService.findByRole(role).toEmployeeDTO()
    }
}
