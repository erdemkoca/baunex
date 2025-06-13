package ch.baunex.config

import ch.baunex.company.dto.CompanyDTO
import ch.baunex.company.facade.CompanyFacade
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class SampleCompanyLoader {

    @Inject
    lateinit var companyFacade: CompanyFacade

    @Transactional
    fun load() {
        if (companyFacade.getCompany() != null) return

        val company = CompanyDTO(
            name = "Baunex AG",
            street = "Musterstrasse 123",
            city = "Zürich",
            zipCode = "8000",
            country = "Schweiz",
            phone = "+41 44 123 45 67",
            email = "info@baunex.ch",
            website = "www.baunex.ch",
            iban = "CH93 0076 7000 E528 5290 7",
            bic = "BCVLCH2LXXX",
            bankName = "Banque Cantonale Vaudoise",
            vatNumber = "CHE-123.456.789 MWST",
            taxNumber = "123.456.789",
            logo = "/images/logo/baunex_logo.png",
            defaultInvoiceFooter = """
                Vielen Dank für Ihr Vertrauen.
                
                **Bankverbindung**  
                Banque Cantonale Vaudoise  
                IBAN: CH93 0076 7000 E528 5290 7  
                BIC: BCVLCH2LXXX  
                
                Bei Fragen stehen wir Ihnen gerne zur Verfügung.
            """.trimIndent(),
            defaultInvoiceTerms = """
                Allgemeine Geschäftsbedingungen
                
                1. Zahlungsbedingungen: 30 Tage netto
                2. Alle Preise inklusive 8.1% MWST
                3. Garantie: 2 Jahre auf Material und Arbeit
                4. Verspätete Zahlungen werden mit 5% Verzugszinsen belastet
            """.trimIndent()
        )

        companyFacade.createCompany(company)
    }
} 