// File: PersonService.kt
package ch.baunex.user.service

import ch.baunex.user.model.PersonModel
import ch.baunex.user.model.PersonDetails
import ch.baunex.user.repository.PersonRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class PersonService {

    @Inject
    lateinit var personRepository: PersonRepository
    /**
     * Findet eine Person anhand der ID
     */
    fun findPersonById(id: Long): PersonModel? =
        personRepository.findById(id)

    /**
     * Listet alle Personen auf
     */
    fun listAllPersons(): List<PersonModel> =
        personRepository.listAll()

    /**
     * Erstellt eine neue Person mit den gegebenen Stammdaten
     */
    @Transactional
    fun createPerson(
        firstName: String,
        lastName: String,
        street: String? = null,
        city: String? = null,
        zipCode: String? = null,
        country: String? = null,
        phone: String? = null,
        email: String? = null
    ): PersonModel {
        val person = PersonModel().apply {
            this.firstName = firstName
            this.lastName = lastName
            this.email = email
            this.details = PersonDetails(
                street = street,
                city = city,
                zipCode = zipCode,
                country = country,
                phone = phone
            )
        }
        person.persist()
        return person
    }

    /**
     * Aktualisiert bestehende Person-Felder (nur nicht-null Parameter)
     */
    @Transactional
    fun updatePerson(
        id: Long,
        firstName: String? = null,
        lastName: String? = null,
        street: String? = null,
        city: String? = null,
        zipCode: String? = null,
        country: String? = null,
        phone: String? = null,
        email: String? = null
    ): PersonModel {
        val person = personRepository.findById(id)
            ?: throw IllegalArgumentException("Person mit ID \$id nicht gefunden")

        firstName?.let { person.firstName = it }
        lastName?.let { person.lastName = it }
        email?.let { person.email = it }

        // Embeddable Felder schrittweise aktualisieren
        street?.let { person.details.street = it }
        city?.let { person.details.city = it }
        zipCode?.let { person.details.zipCode = it }
        country?.let { person.details.country = it }
        phone?.let { person.details.phone = it }

        //person.flush()
        return person
    }

    /**
     * Löscht eine Person
     */
    @Transactional
    fun deletePerson(id: Long) {
        personRepository.findById(id)?.delete()
    }
}