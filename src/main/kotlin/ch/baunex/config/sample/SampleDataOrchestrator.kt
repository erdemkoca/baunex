package ch.baunex.config.sample

import io.quarkus.arc.profile.IfBuildProfile
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.annotation.Priority

/**
 * Sample data orchestrator that runs ONLY in dev profile.
 * This class can be safely removed before production release.
 */
@IfBuildProfile("dev")
@ApplicationScoped
class SampleDataOrchestrator {
    @Inject lateinit var companyLoader: SampleCompanyLoader
    @Inject lateinit var catalogLoader: SampleCatalogLoader
    @Inject lateinit var employeeLoader: SampleEmployeeLoader
    @Inject lateinit var customerAndContactsLoader: SampleCustomerAndContactsLoader
    @Inject lateinit var projectLoader: SampleProjectLoader
    @Inject lateinit var projectCatalogLoader: SampleProjectCatalogItemLoader
    @Inject lateinit var timeEntryLoader: SampleTimeEntryLoader
    @Inject lateinit var holidayLoader: SampleHolidayLoader

    @Transactional
    fun loadSampleData(@Observes @Priority(10) event: StartupEvent) {
        println("🚀 Loading sample data for development environment at ${java.time.LocalDateTime.now()}...")

        companyLoader.load()
        catalogLoader.load()
        employeeLoader.load()
        customerAndContactsLoader.load()
        projectLoader.load()
        projectCatalogLoader.load()
        holidayLoader.load()
        timeEntryLoader.load()
        
        println("✅ Sample data loading completed at ${java.time.LocalDateTime.now()}.")
    }
} 