//package ch.baunex.security.exception
//
//import jakarta.ws.rs.ext.ExceptionMapper
//import jakarta.ws.rs.ext.Provider
//import kotlinx.serialization.MissingFieldException
//import jakarta.ws.rs.core.Response
//import kotlinx.serialization.Serializable
//
//@Provider
//class MissingFieldExceptionMapper : ExceptionMapper<MissingFieldException> {
//    override fun toResponse(exception: MissingFieldException): Response {
//        return Response.status(Response.Status.BAD_REQUEST)
//            .entity(MessageResponse("Email and password are required"))
//            .build()
//    }
//}
//
//@Serializable
//data class MessageResponse(val s: String)
