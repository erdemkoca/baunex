package ch.baunex.billing.facade

import ch.baunex.billing.dto.BillingDTO
import ch.baunex.billing.service.BillingService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class BillingFacade {

    @Inject
    lateinit var billingService: BillingService

    fun getBillingForProject(projectId: Long): BillingDTO {
        return billingService.calculateBilling(projectId)
    }
}
