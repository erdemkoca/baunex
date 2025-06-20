package ch.baunex.upload.config

import ch.baunex.upload.spi.Local
import ch.baunex.upload.spi.S3
import ch.baunex.upload.spi.StorageService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Instance
import jakarta.enterprise.inject.Produces
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class StorageProducer {

    @ConfigProperty(name = "storage.type")
    lateinit var type: String

    @Inject @Local
    lateinit var localStorage: StorageService

    // Always inject an Instance, even if there's no @S3 bean
    @Inject
    @S3
    lateinit var s3Storage: Instance<StorageService>

    @Produces @ApplicationScoped
    fun storageService(): StorageService =
        when (type.lowercase()) {
            "s3" -> s3Storage.get()       // .get() will throw if no bean, but only if you ask for it
            else -> localStorage
        }
}
