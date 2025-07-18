package ch.baunex.user.repository

import ch.baunex.user.model.EmployeeModel
import ch.baunex.user.model.Role
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class EmployeeRepository : PanacheRepository<EmployeeModel> {

    fun findByAhv(ahvNumber: String): EmployeeModel? =
        find("ahvNumber", ahvNumber).firstResult()

    fun findByRole(role: Role): EmployeeModel =
        find("role", role).firstResult()
        
    fun listAllEmployees(): List<EmployeeModel> =
        find("FROM EmployeeModel e").list<EmployeeModel>()
        
    fun listAllEmployeesWithoutPerson(): List<EmployeeModel> =
        find("SELECT e FROM EmployeeModel e WHERE e.id IS NOT NULL").list<EmployeeModel>()
}
