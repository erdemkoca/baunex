package ch.baunex.web

import ch.baunex.project.dto.ProjectDTO
import ch.baunex.project.facade.ProjectFacade
import ch.baunex.project.model.ProjectStatus
import ch.baunex.web.WebController.Templates
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.LocalDate

@Path("/projects")
class WebProjectController {

    @Inject
    lateinit var projectFacade: ProjectFacade

    private fun getCurrentDate() = LocalDate.now()

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun list(): Response {
        val projects = projectFacade.getAllProjects()
        val template = Templates.projects(projects, getCurrentDate(), "projects")
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/new")
    @Produces(MediaType.TEXT_HTML)
    fun newProject(): Response {
        val emptyProject = ProjectDTO(
            id = null,
            name = "",
            client = "",
            budget = 0,
            contact = "",
            startDate = null,
            endDate = null,
            description = "",
            status = ProjectStatus.PLANNED,
            street = "",
            city = ""
        )

        val template = Templates.projectDetail(
            project = emptyProject,
            activeMenu = "projects",
            currentDate = getCurrentDate()
        )

        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    fun view(@PathParam("id") id: Long): Response {
        val project = projectFacade.getProjectWithDetails(id) ?: return Response.status(404).build()
        val template = Templates.projectDetail(project, "projects", getCurrentDate())
        return Response.ok(template.render()).build()
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    fun edit(@PathParam("id") id: Long): Response {
        val project = projectFacade.getProjectById(id) ?: return Response.status(404).build()
        val template = Templates.projectDetail(project, "projects", getCurrentDate())
        return Response.ok(template.render()).build()
    }

    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun saveProject(
        @FormParam("id") id: Long?,
        @FormParam("name") name: String,
        @FormParam("client") client: String,
        @FormParam("budget") budget: Int,
        @FormParam("contact") contact: String?,
        @FormParam("startDate") startDate: LocalDate?,
        @FormParam("endDate") endDate: LocalDate?,
        @FormParam("description") description: String?,
        @FormParam("status") status: ProjectStatus?,
        @FormParam("street") street: String?,
        @FormParam("city") city: String?
    ): Response {
        val dto = ProjectDTO(
            id = id,
            name = name,
            client = client,
            budget = budget,
            contact = contact,
            startDate = startDate,
            endDate = endDate,
            description = description,
            status = status ?: ProjectStatus.PLANNED,
            street = street,
            city = city
        )

        if (id == null || id == 0L) {
            projectFacade.createProject(dto)
        } else {
            projectFacade.updateProject(id, dto)
        }

        return Response.seeOther(java.net.URI("/projects")).build()
    }

    @GET
    @Path("/{id}/delete")
    fun delete(@PathParam("id") id: Long): Response {
        projectFacade.deleteProject(id)
        return Response.seeOther(java.net.URI("/projects")).build()
    }
}
