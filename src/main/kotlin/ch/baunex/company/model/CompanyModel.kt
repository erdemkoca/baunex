package ch.baunex.company.model

import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "companies")
class CompanyModel : PanacheEntity() {
    @Column(nullable = false)
    lateinit var name: String

    @Column(nullable = false)
    lateinit var street: String

    @Column(nullable = false)
    lateinit var city: String

    @Column(nullable = false)
    lateinit var zipCode: String

    @Column(nullable = false)
    lateinit var country: String

    @Column
    var phone: String? = null

    @Column
    var email: String? = null

    @Column
    var website: String? = null

    @Column
    var iban: String? = null

    @Column
    var bic: String? = null

    @Column
    var bankName: String? = null

    @Column
    var vatNumber: String? = null

    @Column
    var taxNumber: String? = null

    @Column
    var logo: String? = null  // Base64 encoded image or URL

    @Column(columnDefinition = "TEXT")
    var defaultInvoiceFooter: String? = null

    @Column(columnDefinition = "TEXT")
    var defaultInvoiceTerms: String? = null

    @Column
    var defaultVatRate: Double = 8.1  // Default Swiss VAT rate
} 