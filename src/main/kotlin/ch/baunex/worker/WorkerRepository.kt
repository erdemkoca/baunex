package ch.baunex.worker

import ch.baunex.worker.dto.WorkerRequest
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class WorkerRepository: PanacheRepository<WorkerRequest> 