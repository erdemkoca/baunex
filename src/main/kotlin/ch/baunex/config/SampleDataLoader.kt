package ch.baunex.config

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
    @Inject lateinit var timeEntryLoader: SampleTimeEntryLoader
    @Inject lateinit var projectCatalogLoader: SampleProjectCatalogItemLoader

    @Transactional
    fun load(@Observes event: StartupEvent) {
        projectLoader.load()
        catalogLoader.load()
        userLoader.load()
        timeEntryLoader.load()
        projectCatalogLoader.load()
    }
}

