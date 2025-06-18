package ch.baunex.controlreport.repository

import ch.baunex.controlreport.model.ControlReportModel
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ControlReportRepository : PanacheRepository<ControlReportModel> {

    fun findByProjectId(projectId: Long): List<ControlReportModel> =
        find("project.id", projectId).list()

    fun countByProjectId(projectId: Long): Long =
        count("project.id", projectId)
}
