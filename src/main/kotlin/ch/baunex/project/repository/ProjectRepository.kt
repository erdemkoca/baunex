package ch.baunex.project.repository


import ch.baunex.project.model.ProjectModel

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ProjectRepository : PanacheRepository<ProjectModel>