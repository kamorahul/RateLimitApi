/**
  * Created by rahulkamboj on 27/03/17.
  */

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import entities.ErrorResponse
import spray.json._


trait AnomalyHandler extends RequestResponseProtocols {

  implicit val myExceptionHandler =
    ExceptionHandler {

      case exception: Exception =>
        exception.printStackTrace()

        val errorHttpEntity = HttpEntity(ErrorResponse(responseCode = 500, errorMessage = "An exception has occurred while serving this request.").toJson.toString()).withContentType(MediaTypes.`application/json`)

        complete(HttpResponse(status = InternalServerError, entity = errorHttpEntity))

    }


  implicit val myRejectionHandler = RejectionHandler
    .newBuilder()
    .handleNotFound {

      val errorHttpEntity = HttpEntity(ErrorResponse(responseCode = 404, errorMessage = "Resource not found.").toJson.toString).withContentType(MediaTypes.`application/json`)
      complete(HttpResponse(status = NotFound, entity = errorHttpEntity))

    }
    .handle {

      case MethodRejection(_) =>

        val errorHttpEntity = HttpEntity(ErrorResponse(responseCode = 405, errorMessage = "Method not unsupported. Please check the URI path and Http Method supported with it.").toJson.toString).withContentType(MediaTypes.`application/json`)
        complete(HttpResponse(status = MethodNotAllowed, entity = errorHttpEntity))


      case _ =>

        val errorHttpEntity = HttpEntity(ErrorResponse(responseCode = 500, errorMessage = "Request rejected.").toJson.toString).withContentType(MediaTypes.`application/json`)
        complete(HttpResponse(status = InternalServerError, entity = errorHttpEntity))

    }
    .result()

}
