package ch.baunex.timetracking.exception

import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.jboss.logging.Logger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Global exception handler for time tracking exceptions
 * Maps exceptions to appropriate HTTP status codes and JSON error responses
 */
@Provider
class TimeTrackingExceptionHandler : ExceptionMapper<TimeTrackingException> {
    
    private val log = Logger.getLogger(TimeTrackingExceptionHandler::class.java)
    
    override fun toResponse(exception: TimeTrackingException): Response {
        val errorResponse = createErrorResponse(exception)
        val status = determineHttpStatus(exception)
        
        // Log the error with appropriate level
        logError(exception, status)
        
        return Response.status(status)
            .entity(errorResponse)
            .type(MediaType.APPLICATION_JSON)
            .build()
    }
    
    private fun createErrorResponse(exception: TimeTrackingException): Map<String, Any> {
        val baseResponse = mutableMapOf<String, Any>()
        baseResponse["timestamp"] = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        baseResponse["error"] = exception.message ?: "Unbekannter Fehler"
        baseResponse["type"] = exception.javaClass.simpleName
        
        // Add field-specific information for validation exceptions
        if (exception is TimeEntryValidationException) {
            exception.field?.let { baseResponse["field"] = it }
            exception.value?.let { baseResponse["value"] = it }
        }
        
        return baseResponse
    }
    
    private fun determineHttpStatus(exception: TimeTrackingException): Response.Status {
        return when (exception) {
            is MissingRequiredFieldException -> Response.Status.BAD_REQUEST
            is EmployeeNotFoundException -> Response.Status.NOT_FOUND
            is ProjectNotFoundException -> Response.Status.NOT_FOUND
            is TimeEntryNotFoundException -> Response.Status.NOT_FOUND
            is InvalidDateException -> Response.Status.BAD_REQUEST
            is InvalidTimeRangeException -> Response.Status.BAD_REQUEST
            is InvalidHoursException -> Response.Status.BAD_REQUEST
            is InvalidBreakException -> Response.Status.BAD_REQUEST
            is DuplicateTimeEntryException -> Response.Status.CONFLICT
            is ApprovalException -> Response.Status.BAD_REQUEST
            is BusinessRuleViolationException -> Response.Status.BAD_REQUEST
            else -> Response.Status.INTERNAL_SERVER_ERROR
        }
    }
    
    private fun logError(exception: TimeTrackingException, status: Response.Status) {
        val logMessage = buildString {
            append("TimeTracking API Error: ${exception.message}")
            if (exception is TimeEntryValidationException) {
                exception.field?.let { append(" (Field: $it)") }
                exception.value?.let { append(" (Value: $it)") }
            }
            append(" [HTTP ${status.statusCode}]")
        }
        
        when {
            status.statusCode in 400..499 -> log.warn(logMessage, exception)
            status.statusCode >= 500 -> log.error(logMessage, exception)
            else -> log.info(logMessage)
        }
    }
}

/**
 * Handler for general validation exceptions (IllegalArgumentException, etc.)
 */
@Provider
class GeneralValidationExceptionHandler : ExceptionMapper<IllegalArgumentException> {
    
    private val log = Logger.getLogger(GeneralValidationExceptionHandler::class.java)
    
    override fun toResponse(exception: IllegalArgumentException): Response {
        val errorResponse = mutableMapOf<String, Any>()
        errorResponse["timestamp"] = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        errorResponse["error"] = exception.message ?: "Validierungsfehler"
        errorResponse["type"] = "ValidationError"
        
        log.warn("Validation error: ${exception.message}", exception)
        
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(errorResponse)
            .type(MediaType.APPLICATION_JSON)
            .build()
    }
}

/**
 * Handler for general runtime exceptions
 */
@Provider
class GeneralRuntimeExceptionHandler : ExceptionMapper<RuntimeException> {
    
    private val log = Logger.getLogger(GeneralRuntimeExceptionHandler::class.java)
    
    override fun toResponse(exception: RuntimeException): Response {
        val errorResponse = mutableMapOf<String, Any>()
        errorResponse["timestamp"] = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        errorResponse["error"] = "Ein interner Fehler ist aufgetreten"
        errorResponse["type"] = "InternalError"
        
        log.error("Runtime error in time tracking API", exception)
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(errorResponse)
            .type(MediaType.APPLICATION_JSON)
            .build()
    }
} 