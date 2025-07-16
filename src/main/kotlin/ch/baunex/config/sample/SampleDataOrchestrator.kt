package ch.baunex.config.sample

import ch.baunex.config.sample.SampleCompanyLoader
import ch.baunex.config.sample.SampleCustomerAndContactsLoader
import ch.baunex.config.sample.SampleCatalogLoader
import ch.baunex.config.sample.SampleEmployeeLoader
import ch.baunex.config.sample.SampleTimeEntryLoader
import io.quarkus.arc.profile.IfBuildProfile
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import jakarta.transaction.Transactional

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

    @Transactional
    fun loadSampleData(@Observes event: StartupEvent) {
        println("🚀 Loading sample data for development environment...")
        
        // Load in dependency order
        companyLoader.load()
        catalogLoader.load()
        employeeLoader.load()
        customerAndContactsLoader.load()
        projectLoader.load()
        projectCatalogLoader.load()
        timeEntryLoader.load()
        
        println("✅ Sample data loading completed.")
    }
} 