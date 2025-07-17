package ch.baunex.timetracking.exception

import java.time.LocalDate
import java.time.LocalTime

/**
 * Base exception for all time tracking related errors
 */
sealed class TimeTrackingException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Validation exceptions for time entry data
 */
sealed class TimeEntryValidationException(
    message: String,
    val field: String? = null,
    val value: Any? = null
) : TimeTrackingException(message)

/**
 * Employee not found or invalid
 */
class EmployeeNotFoundException(
    employeeId: Long,
    cause: Throwable? = null
) : TimeEntryValidationException(
    "Mitarbeiter mit ID $employeeId wurde nicht gefunden",
    "employeeId",
    employeeId
) {
    constructor(message: String, cause: Throwable? = null) : this(-1L, cause)
}

/**
 * Project not found or invalid
 */
class ProjectNotFoundException(
    projectId: Long,
    cause: Throwable? = null
) : TimeEntryValidationException(
    "Projekt mit ID $projectId wurde nicht gefunden",
    "projectId",
    projectId
) {
    constructor(message: String, cause: Throwable? = null) : this(-1L, cause)
}

/**
 * Invalid date (future date, weekend, holiday, etc.)
 */
class InvalidDateException(
    date: LocalDate,
    reason: String,
    cause: Throwable? = null
) : TimeEntryValidationException(
    "Ungültiges Datum $date: $reason",
    "date",
    date
)

/**
 * Invalid time range (start after end, negative duration, etc.)
 */
class InvalidTimeRangeException(
    startTime: LocalTime,
    endTime: LocalTime,
    reason: String,
    cause: Throwable? = null
) : TimeEntryValidationException(
    "Ungültiger Zeitbereich $startTime - $endTime: $reason",
    "timeRange",
    "$startTime-$endTime"
)

/**
 * Invalid hours worked (negative, too high, etc.)
 */
class InvalidHoursException(
    hours: Double,
    reason: String,
    cause: Throwable? = null
) : TimeEntryValidationException(
    "Ungültige Arbeitsstunden $hours: $reason",
    "hoursWorked",
    hours
)

/**
 * Invalid break configuration
 */
class InvalidBreakException(
    breakInfo: String,
    reason: String,
    cause: Throwable? = null
) : TimeEntryValidationException(
    "Ungültige Pause: $reason",
    "breaks",
    breakInfo
)

/**
 * Missing required field
 */
class MissingRequiredFieldException(
    fieldName: String,
    cause: Throwable? = null
) : TimeEntryValidationException(
    "Pflichtfeld '$fieldName' ist erforderlich",
    fieldName,
    null
)

/**
 * Time entry not found
 */
class TimeEntryNotFoundException(
    entryId: Long,
    cause: Throwable? = null
) : TimeTrackingException(
    "Zeiteintrag mit ID $entryId wurde nicht gefunden",
    cause
)

/**
 * Duplicate time entry (overlapping times)
 */
class DuplicateTimeEntryException(
    employeeId: Long,
    date: LocalDate,
    startTime: LocalTime,
    endTime: LocalTime,
    cause: Throwable? = null
) : TimeTrackingException(
    "Überlappender Zeiteintrag für Mitarbeiter $employeeId am $date von $startTime bis $endTime",
    cause
)

/**
 * Approval related exceptions
 */
class ApprovalException(
    message: String,
    cause: Throwable? = null
) : TimeTrackingException(message, cause)

/**
 * Business rule violations
 */
class BusinessRuleViolationException(
    rule: String,
    details: String,
    cause: Throwable? = null
) : TimeTrackingException(
    "Geschäftsregel verletzt: $rule - $details",
    cause
) 