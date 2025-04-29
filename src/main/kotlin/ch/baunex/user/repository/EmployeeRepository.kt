package ch.baunex.user.repository

import ch.baunex.user.model.EmployeeModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class EmployeeRepository : PanacheRepository<EmployeeModel> {

    fun findByAhv(ahvNumber: String): EmployeeModel? =
        find("ahvNumber", ahvNumber).firstResult()

    // PanacheRepository bringt euch kostenlos:
    //   findById(id), listAll(), persist(entity), delete(entity), etc.
}
