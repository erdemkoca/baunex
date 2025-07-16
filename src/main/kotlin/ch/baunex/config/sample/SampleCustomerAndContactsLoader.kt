package ch.baunex.config.sample

import ch.baunex.user.dto.CustomerContactCreateDTO
import ch.baunex.user.dto.CustomerCreateDTO
import ch.baunex.user.mapper.CustomerMapper
import ch.baunex.user.model.CustomerContact
import ch.baunex.user.model.CustomerModel
import ch.baunex.user.service.CustomerContactService
import ch.baunex.user.service.CustomerService
import io.quarkus.arc.profile.IfBuildProfile
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.util.concurrent.atomic.AtomicLong

/**
 * Sample customer and contacts loader - DEV ONLY
 * This class can be safely removed before production release.
 */
@IfBuildProfile("dev")
@ApplicationScoped
class SampleCustomerAndContactsLoader @Inject constructor(
    private val customerService: CustomerService,
    private val customerContactService: CustomerContactService,
    private val mapper: CustomerMapper
) {
    private val customerNumberCounter = AtomicLong(1)

    @Transactional
    fun load() {
        // Only load if no customers exist
        if (customerService.getAll().isNotEmpty()) {
            return
        }

        // Create sample customers
        val customer1 = createCustomer(
            "Muster AG",
            "Max",
            "Muster",
            "max.muster@muster.ch",
            "Musterstrasse 1",
            "Musterstadt",
            "8000",
            "CH",
            "+41 44 123 45 67"
        )

        val customer2 = createCustomer(
            "Beispiel GmbH",
            "Anna",
            "Beispiel",
            "anna.beispiel@beispiel.de",
            "Beispielweg 2",
            "Beispielstadt",
            "10115",
            "DE",
            "+49 30 123 45 67"
        )

        val customer3 = createCustomer(
            "Test SA",
            "Jean",
            "Test",
            "jean.test@test.fr",
            "Rue de Test 3",
            "Testville",
            "75001",
            "FR",
            "+33 1 23 45 67 89"
        )

        val customer4 = createCustomer(
            "Prova SRL",
            "Marco",
            "Prova",
            "marco.prova@prova.it",
            "Via Prova 4",
            "Provacitta",
            "20100",
            "IT",
            "+39 02 123 45 67"
        )

        // Create sample contacts
        createContact(
            customer1,
            "Peter",
            "Kontakt",
            "peter.kontakt@muster.ch",
            "Kontaktstrasse 1",
            "Musterstadt",
            "8000",
            "CH",
            "+41 44 234 56 78",
            "Projektleiter",
            true
        )

        createContact(
            customer2,
            "Maria",
            "Kontakt",
            "maria.kontakt@beispiel.de",
            "Kontaktweg 2",
            "Beispielstadt",
            "10115",
            "DE",
            "+49 30 234 56 78",
            "Geschäftsführerin",
            true
        )

        createContact(
            customer3,
            "Pierre",
            "Kontakt",
            "pierre.kontakt@test.fr",
            "Rue de Contact 3",
            "Testville",
            "75001",
            "FR",
            "+33 1 23 45 67 90",
            "Chef de Projet",
            true
        )

        createContact(
            customer4,
            "Paolo",
            "Kontakt",
            "paolo.kontakt@prova.it",
            "Via Contatto 4",
            "Provacitta",
            "20100",
            "IT",
            "+39 02 234 56 78",
            "Direttore",
            true
        )
    }

    private fun createCustomer(
        companyName: String,
        firstName: String,
        lastName: String,
        email: String,
        street: String,
        city: String,
        zipCode: String,
        country: String,
        phone: String
    ): CustomerModel {
        val dto = CustomerCreateDTO(
            firstName = firstName,
            lastName = lastName,
            email = email,
            street = street,
            city = city,
            zipCode = zipCode,
            country = country,
            phone = phone,
            companyName = companyName,
            paymentTerms = "30 Tage",
            creditLimit = 20000.0,
            industry = "Branche",
            discountRate = 0.0,
            preferredLanguage = "DE",
            marketingConsent = false,
            taxId = "CHE-000.000.000"
        )
        val customerDTO = customerService.createCustomer(dto)
        return customerService.findCustomerModelById(customerDTO.id!!)!!
    }

    private fun createContact(
        customer: CustomerModel,
        firstName: String,
        lastName: String,
        email: String,
        street: String,
        city: String,
        zipCode: String,
        country: String,
        phone: String,
        role: String,
        isPrimary: Boolean
    ): CustomerContact {
        val dto = CustomerContactCreateDTO(
            firstName = firstName,
            lastName = lastName,
            email = email,
            street = street,
            city = city,
            zipCode = zipCode,
            country = country,
            phone = phone,
            role = role,
            isPrimary = isPrimary
        )
        return customerContactService.createContact(customer, dto)
    }
} 