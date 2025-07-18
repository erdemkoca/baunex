package ch.baunex.user.repository

import ch.baunex.user.model.CustomerModel
import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class CustomerRepository : PanacheRepository<CustomerModel> {

    fun findByCustomerNumber(customerNumber: String): CustomerModel? =
        find("customerNumber", customerNumber).firstResult()

    fun findByCompanyName(companyName: String): List<CustomerModel> =
        list("companyName = ?1", companyName)

    fun listByIndustry(industry: String): List<CustomerModel> =
        list("industry = ?1", industry)

    fun listAllCustomers(): List<CustomerModel> =
        find("FROM CustomerModel c").list<CustomerModel>()

}