package ch.baunex.config

import ch.baunex.config.SampleCustomerAndContactsLoader
import ch.baunex.config.sample.SampleProjectCatalogItemLoader
import ch.baunex.config.sample.SampleProjectLoader
import io.quarkus.arc.profile.IfBuildProfile
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import jakarta.transaction.Transactional


@IfBuildProfile("dev")
@ApplicationScoped
class SampleDataLoader {
    @Inject lateinit var projectLoader: SampleProjectLoader
    @Inject lateinit var catalogLoader: SampleCatalogLoader
    @Inject lateinit var employeeLoader: SampleEmployeeLoader
    @Inject lateinit var timeEntryLoader: SampleTimeEntryLoader
    @Inject lateinit var projectCatalogLoader: SampleProjectCatalogItemLoader
    @Inject lateinit var customerAndContactsLoader: SampleCustomerAndContactsLoader
    @Inject lateinit var companyLoader: SampleCompanyLoader

    @Transactional
    fun load(@Observes event: StartupEvent) {
        companyLoader.load()
        catalogLoader.load()
        employeeLoader.load()
        customerAndContactsLoader.load()
        projectLoader.load()
        projectCatalogLoader.load()
        timeEntryLoader.load()
    }
}

