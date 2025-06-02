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
}
