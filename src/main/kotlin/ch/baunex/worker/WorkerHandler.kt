package ch.baunex.worker

import ch.baunex.worker.dto.WorkerRequest
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@RequestScoped
class WorkerHandler {

    @Inject lateinit var workerRepo: WorkerRepository

    @Transactional
    fun saveWorker(dto: WorkerRequest) {
        workerRepo.persist(dto)
    }

    fun getAllWorkers(): List<WorkerRequest> {
        return workerRepo.findAll().list()
    }
    
    @Transactional
    fun deleteWorker(id: Long) {
        workerRepo.deleteById(id)
    }
    
    fun getWorkerById(id: Long): WorkerRequest? {
        return workerRepo.findById(id)
    }
    
    @Transactional
    fun updateWorker(id: Long, dto: WorkerRequest): Boolean {
        val existingWorker = workerRepo.findById(id) ?: return false
        existingWorker.firstName = dto.firstName
        existingWorker.lastName = dto.lastName
        existingWorker.email = dto.email
        existingWorker.phone = dto.phone
        existingWorker.position = dto.position
        existingWorker.hourlyRate = dto.hourlyRate
        workerRepo.persist(existingWorker)
        return true
    }
}