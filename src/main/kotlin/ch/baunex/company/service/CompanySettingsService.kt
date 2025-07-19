package ch.baunex.company.service

import ch.baunex.company.model.CompanySettingsModel
import ch.baunex.company.repository.CompanySettingsRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.jboss.logging.Logger

@ApplicationScoped
class CompanySettingsService @Inject constructor(
    private val companySettingsRepository: CompanySettingsRepository
) {
    private val log = Logger.getLogger(CompanySettingsService::class.java)

    @Transactional
    fun getCompanySettings(): CompanySettingsModel {
        log.debug("Getting company settings")
        return companySettingsRepository.findOrCreateDefault()
    }

    fun getPlannedWeeklyHours(): Double {
        log.debug("Getting planned weekly hours")
        return getCompanySettings().plannedWeeklyHours
    }

    fun getDefaultWorkdaysPerWeek(): Int {
        log.debug("Getting default workdays per week")
        return getCompanySettings().defaultWorkdaysPerWeek
    }

    @Transactional
    fun updatePlannedWeeklyHours(hours: Double): CompanySettingsModel {
        log.info("Updating planned weekly hours to: $hours")
        val settings = getCompanySettings()
        settings.plannedWeeklyHours = hours
        return settings
    }

    @Transactional
    fun updateDefaultWorkdaysPerWeek(days: Int): CompanySettingsModel {
        log.info("Updating default workdays per week to: $days")
        val settings = getCompanySettings()
        settings.defaultWorkdaysPerWeek = days
        return settings
    }
} 