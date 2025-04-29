package ch.baunex.user.mapper

import ch.baunex.user.dto.EmployeeDTO
import ch.baunex.user.dto.EmployeeCreateDTO
import ch.baunex.user.model.EmployeeModel
import ch.baunex.user.model.PersonModel
import ch.baunex.user.model.PersonDetails
import ch.baunex.user.model.Role

fun EmployeeModel.toEmployeeDTO(): EmployeeDTO = EmployeeDTO(
    id = this.id!!,
    firstName = this.person.firstName,
    lastName = this.person.lastName,
    email = this.email,
    role = this.role.name,
    ahvNumber = this.ahvNumber,
    bankIban = this.bankIban,
    hourlyRate = this.hourlyRate,
    street     = this.person.details.street,
    city       = this.person.details.city,
    zipCode    = this.person.details.zipCode,
    country    = this.person.details.country,
    phone      = this.person.details.phone,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

fun EmployeeCreateDTO.toEmployeeModel(): EmployeeModel {
    val personModel = PersonModel().apply {
        firstName = this@toEmployeeModel.firstName
        lastName  = this@toEmployeeModel.lastName
        email     = this@toEmployeeModel.email
        details   = PersonDetails(
            street  = this@toEmployeeModel.street,
            city    = this@toEmployeeModel.city,
            zipCode = this@toEmployeeModel.zipCode,
            country = this@toEmployeeModel.country,
            phone   = this@toEmployeeModel.phone
        )
    }
    return EmployeeModel().apply {
        this.person       = personModel
        email        = this@toEmployeeModel.email
        passwordHash = ""  // Passwort-Hash im Service setzen
        role         = Role.valueOf(this@toEmployeeModel.role)
        ahvNumber    = this@toEmployeeModel.ahvNumber
        bankIban     = this@toEmployeeModel.bankIban
        hourlyRate   = this@toEmployeeModel.hourlyRate
    }
}

fun EmployeeCreateDTO.applyTo(employee: EmployeeModel): EmployeeModel {
    employee.person.apply {
        firstName = this@applyTo.firstName
        lastName  = this@applyTo.lastName
        email     = this@applyTo.email
        details.street  = this@applyTo.street
        details.city    = this@applyTo.city
        details.zipCode = this@applyTo.zipCode
        details.country = this@applyTo.country
        details.phone   = this@applyTo.phone
    }
    employee.apply {
        email      = this@applyTo.email
        role       = Role.valueOf(this@applyTo.role)
        ahvNumber  = this@applyTo.ahvNumber
        bankIban   = this@applyTo.bankIban
        hourlyRate = this@applyTo.hourlyRate
    }
    return employee
}