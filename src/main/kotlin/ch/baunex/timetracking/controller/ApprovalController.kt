package ch.baunex.timetracking.controller

import ch.baunex.timetracking.dto.WeeklyApprovalDTO
import ch.baunex.timetracking.dto.VacationApprovalDTO
import ch.baunex.timetracking.service.ApprovalService
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/api/approval")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ApprovalController {

    @Inject
    lateinit var approvalService: ApprovalService

    /**
     * Approve a single time entry
     */
    @POST
    @Path("/timeentry/{entryId}/approve")
    fun approveTimeEntry(
        @PathParam("entryId") entryId: Long,
        @QueryParam("approverId") approverId: Long
    ): Response {
        val success = approvalService.approveTimeEntry(entryId, approverId)
        return if (success) {
            Response.ok().entity(mapOf("message" to "Time entry approved successfully")).build()
        } else {
            Response.status(Response.Status.NOT_FOUND).entity(mapOf("error" to "Time entry or approver not found")).build()
        }
    }

    /**
     * Approve all time entries for an employee in a specific week
     */
    @POST
    @Path("/weekly/approve")
    fun approveWeeklyEntries(
        @QueryParam("employeeId") employeeId: Long,
        @QueryParam("year") year: Int,
        @QueryParam("week") week: Int,
        @QueryParam("approverId") approverId: Long
    ): Response {
        val success = approvalService.approveWeeklyEntries(employeeId, year, week, approverId)
        return if (success) {
            Response.ok().entity(mapOf("message" to "Weekly entries approved successfully")).build()
        } else {
            Response.status(Response.Status.NOT_FOUND).entity(mapOf("error" to "Employee, approver, or entries not found")).build()
        }
    }

    /**
     * Get weekly approval summary
     */
    @GET
    @Path("/weekly/summary")
    fun getWeeklyApprovalSummary(
        @QueryParam("employeeId") employeeId: Long,
        @QueryParam("year") year: Int,
        @QueryParam("week") week: Int
    ): Response {
        val summary = approvalService.getWeeklyApprovalSummary(employeeId, year, week)
        return if (summary != null) {
            Response.ok(summary).build()
        } else {
            Response.status(Response.Status.NOT_FOUND).entity(mapOf("error" to "Employee not found")).build()
        }
    }

    /**
     * Approve a vacation request
     */
    @POST
    @Path("/vacation/{vacationId}/approve")
    fun approveVacation(
        @PathParam("vacationId") vacationId: Long,
        @QueryParam("approverId") approverId: Long
    ): Response {
        val success = approvalService.approveVacation(vacationId, approverId)
        return if (success) {
            Response.ok().entity(mapOf("message" to "Vacation request approved successfully")).build()
        } else {
            Response.status(Response.Status.NOT_FOUND).entity(mapOf("error" to "Vacation request or approver not found")).build()
        }
    }

    /**
     * Reject a vacation request
     */
    @POST
    @Path("/vacation/{vacationId}/reject")
    fun rejectVacation(
        @PathParam("vacationId") vacationId: Long,
        @QueryParam("approverId") approverId: Long,
        @QueryParam("reason") reason: String?
    ): Response {
        val success = approvalService.rejectVacation(vacationId, approverId, reason)
        return if (success) {
            Response.ok().entity(mapOf("message" to "Vacation request rejected successfully")).build()
        } else {
            Response.status(Response.Status.NOT_FOUND).entity(mapOf("error" to "Vacation request or approver not found")).build()
        }
    }

    /**
     * Get all pending vacation requests
     */
    @GET
    @Path("/vacation/pending")
    fun getPendingVacationRequests(): Response {
        val requests = approvalService.getPendingVacationRequests()
        return Response.ok(requests).build()
    }
} 