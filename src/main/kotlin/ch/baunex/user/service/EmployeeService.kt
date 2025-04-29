// --- EmployeeService.kt ---
package ch.baunex.user.service

import ch.baunex.user.dto.EmployeeCreateDTO
import ch.baunex.user.mapper.toEmployeeModel
import ch.baunex.user.mapper.applyTo
import ch.baunex.user.model.EmployeeModel
import ch.baunex.user.repository.EmployeeRepository
import ch.baunex.security.utils.PasswordUtil
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class EmployeeService @Inject constructor(
    private val employeeRepository: EmployeeRepository
) {

    @Transactional
    fun createEmployee(createDTO: EmployeeCreateDTO): EmployeeModel {
        val employee = createDTO.toEmployeeModel()
        employee.passwordHash = PasswordUtil.hashPassword(createDTO.password)
        employeeRepository.persist(employee)
        return employee
    }

    @Transactional
    fun updateEmployee(id: Long, updateDTO: EmployeeCreateDTO): EmployeeModel {
        val employee = employeeRepository.findById(id)
            ?: throw IllegalArgumentException("Employee mit ID $id nicht gefunden")
        updateDTO.applyTo(employee)
        return employee
    }

    fun findEmployeeById(id: Long): EmployeeModel? =
        employeeRepository.findById(id)

    fun listAllEmployees(): List<EmployeeModel> =
        employeeRepository.listAll()

    @Transactional
    fun deleteEmployee(id: Long) {
        employeeRepository.findById(id)?.let {
            employeeRepository.delete(it)
        }
    }
}
