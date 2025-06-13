// File: PersonRepository.kt
package ch.baunex.user.repository

import ch.baunex.user.model.PersonModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class PersonRepository : PanacheRepository<PersonModel> {

    fun findByName(firstName: String, lastName: String): List<PersonModel> =
        list("firstName = ?1 and lastName = ?2", firstName, lastName)

    fun searchByLastNameContaining(substring: String): List<PersonModel> =
        list("lower(lastName) like ?1", "%${'$'}{substring.toLowerCase()}%")

    fun findByEmail(email: String): PersonModel? =
        find("email", email).firstResult()

    fun deleteOrphaned(): Long =
        delete("id not in (select person.id from EmployeeModel)"
                + " and id not in (select person.id from CustomerModel)")

}