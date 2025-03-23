package ch.baunex.worker.dto

import kotlinx.serialization.Serializable

@Serializable
data class WorkerResponse(
    var workers: List<WorkerRequest>
) 