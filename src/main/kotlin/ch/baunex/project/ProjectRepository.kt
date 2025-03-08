package ch.baunex.project


import ch.baunex.project.dto.ProjectRequest

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ProjectRepository: PanacheRepository<ProjectRequest>