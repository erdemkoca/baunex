package ch.baunex.timetracking.controller

import ch.baunex.timetracking.dto.WeeklyApprovalDTO
import ch.baunex.timetracking.dto.VacationApprovalDTO
import ch.baunex.timetracking.service.ApprovalService
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.logging.Logger

@Path("/api/approval")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ApprovalController {

    @Inject
    lateinit var approvalService: ApprovalService
    
    private val log = Logger.getLogger(ApprovalController::class.java)

    /**
     * Approve a single time entry
     */
    @POST
    @Path("/timeentry/{entryId}/approve")
    fun approveTimeEntry(
        @PathParam("entryId") entryId: Long,
        @QueryParam("approverId") approverId: Long
    ): Response {
        log.info("Approving time entry $entryId by approver $approverId")
        return try {
            val success = approvalService.approveTimeEntry(entryId, approverId)
            if (success) {
                log.info("Successfully approved time entry $entryId")
                Response.ok().entity(mapOf("message" to "Time entry approved successfully")).build()
            } else {
                log.warn("Failed to approve time entry $entryId - entry or approver not found")
                Response.status(Response.Status.NOT_FOUND).entity(mapOf("error" to "Time entry or approver not found")).build()
            }
        } catch (e: Exception) {
            log.error("Failed to approve time entry $entryId", e)
            throw e
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
        log.info("Approving weekly entries for employee $employeeId, year $year, week $week by approver $approverId")
        return try {
            val success = approvalService.approveWeeklyEntries(employeeId, year, week, approverId)
            if (success) {
                log.info("Successfully approved weekly entries for employee $employeeId, year $year, week $week")
                Response.ok().entity(mapOf("message" to "Weekly entries approved successfully")).build()
            } else {
                log.warn("Failed to approve weekly entries for employee $employeeId, year $year, week $week - employee, approver, or entries not found")
                Response.status(Response.Status.NOT_FOUND).entity(mapOf("error" to "Employee, approver, or entries not found")).build()
            }
        } catch (e: Exception) {
            log.error("Failed to approve weekly entries for employee $employeeId, year $year, week $week", e)
            throw e
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
        log.info("Fetching weekly approval summary for employee $employeeId, year $year, week $week")
        return try {
            val summary = approvalService.getWeeklyApprovalSummary(employeeId, year, week)
            if (summary != null) {
                log.info("Successfully fetched weekly approval summary for employee $employeeId, year $year, week $week")
                Response.ok(summary).build()
            } else {
                log.warn("Weekly approval summary not found for employee $employeeId, year $year, week $week")
                Response.status(Response.Status.NOT_FOUND).entity(mapOf("error" to "Employee not found")).build()
            }
        } catch (e: Exception) {
            log.error("Failed to fetch weekly approval summary for employee $employeeId, year $year, week $week", e)
            throw e
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
        log.info("Approving vacation request $vacationId by approver $approverId")
        return try {
            val success = approvalService.approveVacation(vacationId, approverId)
            if (success) {
                log.info("Successfully approved vacation request $vacationId")
                Response.ok().entity(mapOf("message" to "Vacation request approved successfully")).build()
            } else {
                log.warn("Failed to approve vacation request $vacationId - request or approver not found")
                Response.status(Response.Status.NOT_FOUND).entity(mapOf("error" to "Vacation request or approver not found")).build()
            }
        } catch (e: Exception) {
            log.error("Failed to approve vacation request $vacationId", e)
            throw e
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
        log.info("Rejecting vacation request $vacationId by approver $approverId, reason: $reason")
        return try {
            val success = approvalService.rejectVacation(vacationId, approverId, reason)
            if (success) {
                log.info("Successfully rejected vacation request $vacationId")
                Response.ok().entity(mapOf("message" to "Vacation request rejected successfully")).build()
            } else {
                log.warn("Failed to reject vacation request $vacationId - request or approver not found")
                Response.status(Response.Status.NOT_FOUND).entity(mapOf("error" to "Vacation request or approver not found")).build()
            }
        } catch (e: Exception) {
            log.error("Failed to reject vacation request $vacationId", e)
            throw e
        }
    }

    /**
     * Get all pending vacation requests
     */
    @GET
    @Path("/vacation/pending")
    fun getPendingVacationRequests(): Response {
        log.info("Fetching pending vacation requests")
        return try {
            val requests = approvalService.getPendingVacationRequests()
            log.info("Successfully fetched ${requests.size} pending vacation requests")
            Response.ok(requests).build()
        } catch (e: Exception) {
            log.error("Failed to fetch pending vacation requests", e)
            throw e
        }
    }
} 