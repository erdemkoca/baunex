package ch.baunex.config.sample

import ch.baunex.user.dto.CustomerCreateDTO
import ch.baunex.user.facade.CustomerFacade
import ch.baunex.user.model.PersonDetails
import ch.baunex.user.model.PersonModel
import ch.baunex.user.model.CustomerContact
import ch.baunex.user.service.CustomerService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.math.BigDecimal

@ApplicationScoped
class SampleCustomerAndContactsLoader {

    @Inject
    lateinit var customerFacade: CustomerFacade

    @Inject
    lateinit var customerService: CustomerService  // zum Anhängen von CustomerContact

    @Transactional
    fun load() {
        if (customerFacade.listAll().isNotEmpty()) return

        // Hilfsfunktion, um Kontakt-Personen einfach zu erzeugen und zu persistieren
        fun mkPerson(
            fn: String, ln: String, email: String,
            street: String, city: String, zip: String, country: String, phone: String
        ): PersonModel =
            PersonModel().apply {
                firstName = fn
                lastName  = ln
                this.email = email
                details = PersonDetails(street, city, zip, country, phone)
                persist()
            }

        //
        // 1) Elektro Meier AG mit vier Kontakten
        //
        val hansDto = CustomerCreateDTO(
            firstName        = "Hans",
            lastName         = "Meier",
            email            = "h.meier@elektromeier.ch",
            street           = "Weinbergstrasse 12",
            city             = "Zürich",
            zipCode          = "8001",
            country          = "Switzerland",
            phone            = "0441234567",
            customerNumber   = "EM-001",
            companyName      = "Elektro Meier AG",
            paymentTerms     = "30 Tage",
            creditLimit      = 20000.0,
            industry         = "Elektrotechnik",
            discountRate     = 0.0,
            preferredLanguage= "DE",
            marketingConsent = false,
            taxId            = "CHE-000.000.000"
        )
        // Anlage des Kunden
        val savedHans = customerFacade.create(hansDto)
        // Hole das JPA‐Entity, um Contacts anzuhängen
        val hansCustomer = customerService.findCustomerById(savedHans.id)
            ?: throw IllegalStateException("Kunde EM-001 nicht gefunden")

        // 1a) Primary-Kontakt (derselbe wie Hans Meier)
        val primary = mkPerson(
            "Hans", "Meier", "h.meier@elektromeier.ch",
            "Weinbergstrasse 12", "Zürich", "8001", "Switzerland", "0441234567"
        )
        CustomerContact().apply {
            customer      = hansCustomer
            contactPerson = primary
            role          = "Primary"
            isPrimary     = true
        }.persist()

        // 1b) Drei weitere Sekundärkontakte
        val anna = mkPerson(
            "Anna", "Müller", "a.mueller@elektromeier.ch",
            "Bahnhofstrasse 1", "Zürich", "8001", "Switzerland", "0447654321"
        )
        CustomerContact().apply {
            customer      = hansCustomer
            contactPerson = anna
            role          = "Sales"
            isPrimary     = false
        }.persist()

        val beat = mkPerson(
            "Beat", "Beispiel", "b.beispiel@elektromeier.ch",
            "Lindenweg 5", "Zürich", "8002", "Switzerland", "0442345678"
        )
        CustomerContact().apply {
            customer      = hansCustomer
            contactPerson = beat
            role          = "Support"
            isPrimary     = false
        }.persist()

        val sarah = mkPerson(
            "Sarah", "Fischer", "s.fischer@elektromeier.ch",
            "Seestrasse 20", "Zürich", "8003", "Switzerland", "0448765432"
        )
        CustomerContact().apply {
            customer      = hansCustomer
            contactPerson = sarah
            role          = "Billing"
            isPrimary     = false
        }.persist()

        //
        // 2–5) Die übrigen vier Kunden wie gehabt
        //
        fun createCust(
            firstName: String, lastName: String, email: String,
            street: String, city: String, zip: String, country: String, phone: String,
            custNo: String, company: String
        ) {
            val dto = CustomerCreateDTO(
                firstName        = firstName,
                lastName         = lastName,
                email            = email,
                street           = street,
                city             = city,
                zipCode          = zip,
                country          = country,
                phone            = phone,
                customerNumber   = custNo,
                companyName      = company,
                paymentTerms     = "30 Tage",
                creditLimit      = 20000.0,
                industry         = "Branche",
                discountRate     = 0.0,
                preferredLanguage= "DE",
                marketingConsent = false,
                taxId            = "CHE-000.000.000"
            )
            val saved = customerFacade.create(dto)
            val person = mkPerson(
                firstName, lastName, email,
                street, city, zip, country, phone
            )
            CustomerContact().apply {
                customer      = customerService.findCustomerById(saved.id)!!
                contactPerson = person
                role          = "Primary"
                isPrimary     = true
            }.persist()
        }

        createCust(
            firstName = "Stefan", lastName = "Burri", email = "s.burri@emobility.ch",
            street="Steinenvorstadt 99", city="Basel", zip="4051", country="Switzerland", phone="0617654321",
            custNo="EMO-002", company="E-Mobility Solutions GmbH"
        )
        createCust(
            firstName = "Claudia", lastName = "Haller", email = "c.haller@immopro.ch",
            street="Museggstrasse 3", city="Luzern", zip="6004", country="Switzerland", phone="0411234567",
            custNo="IMP-003", company="ImmoPro AG"
        )
        createCust(
            firstName = "Patrick", lastName = "Frei", email = "p.frei@buerotrend.ch",
            street="Bundesgasse 45", city="Bern", zip="3011", country="Switzerland", phone="0317654321",
            custNo="BT-004", company="BüroTrend GmbH"
        )
        createCust(
            firstName = "Martina", lastName = "Keller", email = "m.keller@itsolutions.ch",
            street="Badenerstrasse 101", city="Zürich", zip="8004", country="Switzerland", phone="0448910111",
            custNo="ITS-005", company="IT Solutions AG"
        )
    }
}
