package ch.baunex.config

import ch.baunex.user.dto.UserDTO
import ch.baunex.user.facade.UserFacade
import ch.baunex.user.model.Role
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class SampleUserLoader {

    @Inject
    lateinit var userFacade: UserFacade

    @Transactional
    fun load() {
        val usersToCreate = listOf(
            UserDTO(
                email = "admin@baunex.ch",
                password = "admin123",
                role = Role.ADMIN,
                phone = "0761112233",
                street = "Hauptstrasse 1",
                city = "Zürich"
            ),
            UserDTO(
                email = "pm.meyer@baunex.ch",
                password = "project123",
                role = Role.PROJECT_MANAGER,
                phone = "0789998877",
                street = "Bahnweg 45",
                city = "Bern"
            ),
            UserDTO(
                email = "elektro.hans@baunex.ch",
                password = "elektro456",
                role = Role.ELECTRICIAN,
                phone = "0794455667",
                street = "Werkstrasse 9",
                city = "Basel"
            ),
            UserDTO(
                email = "lerni@baunex.ch",
                password = "lerni789",
                role = Role.EMPLOYEE,
                phone = "0775566778",
                street = "Lehrweg 3",
                city = "Luzern"
            ),
            UserDTO(
                email = "kunde.muster@firma.ch",
                password = "kunde123",
                role = Role.CLIENT,
                phone = "0769988776",
                street = "Industriestrasse 2",
                city = "Winterthur"
            )
        )

        usersToCreate.forEach {
            if (!userFacade.existsByEmail(it.email)) {
                userFacade.registerUser(it)
            }
        }
    }
}
