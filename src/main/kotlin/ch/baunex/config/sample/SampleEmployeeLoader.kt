package ch.baunex.config.sample

import ch.baunex.user.dto.EmployeeCreateDTO
import ch.baunex.user.facade.EmployeeFacade
import io.quarkus.arc.profile.IfBuildProfile
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.LocalDate

/**
 * Sample employee loader - DEV ONLY
 * This class can be safely removed before production release.
 */
@IfBuildProfile("dev")
@ApplicationScoped
class SampleEmployeeLoader {

    @Inject
    lateinit var employeeFacade: EmployeeFacade
    
    @Inject
    lateinit var employeeService: ch.baunex.user.service.EmployeeService

    @Transactional
    fun load() {
        val employeesToCreate = listOf(
            EmployeeCreateDTO(
                firstName = "Admin",
                lastName = "Baunex",
                street = "Hauptstrasse 1",
                city = "Zürich",
                zipCode = "8000",
                country = "Switzerland",
                phone = "0761112233",
                email = "admin@baunex.ch",
                password = "admin123",
                role = "ADMIN",
                ahvNumber = "756.1234.5678.97",
                bankIban = "CH9300762011623852957",
                hourlyRate = 200.0,
                startDate = LocalDate.now().minusWeeks(12)
            ),
            EmployeeCreateDTO(
                firstName = "Max",
                lastName = "Muster",
                street = "Bahnweg 45",
                city = "Bern",
                zipCode = "3000",
                country = "Switzerland",
                phone = "0789998877",
                email = "pm.meyer@baunex.ch",
                password = "project123",
                role = "PROJECT_MANAGER",
                ahvNumber = "756.2345.6789.01",
                bankIban = "CH5604835012345678009",
                hourlyRate = 180.0,
                startDate = LocalDate.now().minusWeeks(12)
            ),
            EmployeeCreateDTO(
                firstName = "Hans",
                lastName = "Elektriker",
                street = "Werkstrasse 9",
                city = "Basel",
                zipCode = "4051",
                country = "Switzerland",
                phone = "0794455667",
                email = "elektro.hans@baunex.ch",
                password = "elektro456",
                role = "ELECTRICIAN",
                ahvNumber = "756.3456.7890.12",
                bankIban = "CH4401234123412341234",
                hourlyRate = 160.0,
                startDate = LocalDate.now().minusWeeks(12)
            ),
            EmployeeCreateDTO(
                firstName = "Leni",
                lastName = "Lernig",
                street = "Lehrweg 3",
                city = "Luzern",
                zipCode = "6000",
                country = "Switzerland",
                phone = "0775566778",
                email = "lerni@baunex.ch",
                password = "lerni789",
                role = "EMPLOYEE",
                ahvNumber = "756.4567.8901.23",
                bankIban = "CH5800791123000889012",
                hourlyRate = 150.0,
                startDate = LocalDate.now().minusWeeks(12)
            )
        )

        employeesToCreate.forEach { dto ->
            try {
                employeeService.createEmployee(dto)
            } catch (_: Exception) {
                // Eintrag vermutlich schon vorhanden oder anderes Problem – ignorieren
            }
        }
    }
} 