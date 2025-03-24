package ch.baunex.worker

import ch.baunex.worker.dto.WorkerRequest
import ch.baunex.worker.dto.WorkerResponse
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/worker")
class WorkerController {

    @Inject lateinit var workerHandler: WorkerHandler

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    fun addWorker(dto: WorkerRequest): Response {
        workerHandler.saveWorker(dto)
        return Response.ok().build()
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getAllWorkers(): Response {
        return Response.ok(WorkerResponse(workerHandler.getAllWorkers())).build()
    }
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getWorkerById(@PathParam("id") id: Long): Response {
        val worker = workerHandler.getWorkerById(id) ?: return Response.status(Response.Status.NOT_FOUND).build()
        return Response.ok(worker).build()
    }
    
    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    fun updateWorker(@PathParam("id") id: Long, dto: WorkerRequest): Response {
        val updated = workerHandler.updateWorker(id, dto)
        return if (updated) Response.ok().build() else Response.status(Response.Status.NOT_FOUND).build()
    }
    
    @DELETE
    @Path("/{id}")
    fun deleteWorker(@PathParam("id") id: Long): Response {
        workerHandler.deleteWorker(id)
        return Response.ok().build()
    }
} 