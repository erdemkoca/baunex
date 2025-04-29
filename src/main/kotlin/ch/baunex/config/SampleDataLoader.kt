package ch.baunex.config

import ch.baunex.config.sample.SampleCustomerAndContactsLoader
import ch.baunex.config.sample.SampleProjectCatalogItemLoader
import ch.baunex.config.sample.SampleProjectLoader
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import jakarta.transaction.Transactional


@ApplicationScoped
class SampleDataLoader {
    @Inject lateinit var projectLoader: SampleProjectLoader
    @Inject lateinit var catalogLoader: SampleCatalogLoader
    @Inject lateinit var userLoader: SampleUserLoader
    @Inject lateinit var employeeLoader: SampleEmployeeLoader
    @Inject lateinit var timeEntryLoader: SampleTimeEntryLoader
    @Inject lateinit var projectCatalogLoader: SampleProjectCatalogItemLoader
    @Inject lateinit var customerAndContactsLoader: SampleCustomerAndContactsLoader

    @Transactional
    fun load(@Observes event: StartupEvent) {
        catalogLoader.load()
        userLoader.load()
        employeeLoader.load()
        projectCatalogLoader.load()
        customerAndContactsLoader.load()
        projectLoader.load()
        timeEntryLoader.load()
    }
}

