/**
  * Created by rahulkamboj on 22/03/17.
  */


import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import entities.ErrorResponse
import model.ApiKeyManager
import spray.json._


trait RestApiOld extends RequestResponseProtocols{

  implicit val system  : ActorSystem
  implicit val material : Materializer


//  val route = get {
//    pathPrefix("hotels") {
//      pathEndOrSingleSlash {
//
//        val currentRequestTimeStamp = System.currentTimeMillis()  // Calculating the timestamp of the current request
//
//        headerValueByName("Authorization") { apiKey =>
//
//          ApiKeyManager.checkIfApiKeyIsValid(apiKey) match {
//            case true => {
//
//              // Business Logic for checking whether the request is allowed, or API Key is blocked or blacklisted.
//              respondWithHeader(RawHeader("X-RateLimit-Remaining", "0")) {
//
//                ApiKeyManager.checkIfRequestIsAllowed(apiKey, currentRequestTimeStamp) match {
//                  case (true, rateLimitResetTime) => {
//                    respondWithHeader(RawHeader("X-RateLimit-Reset", rateLimitResetTime.toString)) {
//                      val errorResponse = ErrorResponse(200, "Valid Case. No error.")
//                      complete(200, HttpEntity(errorResponse.toJson.toString).withContentType(MediaTypes.`application/json`))
//                    }
//                  }
//
//                  case (false, rateLimitResetTime) => {
//                    respondWithHeader(RawHeader("X-RateLimit-Reset", rateLimitResetTime.toString)) {
//                      val errorResponse = ErrorResponse(429, "Too many requests. Rate limit exceeded. API is rate limited (only 1 request per 10 seconds allowed).")
//                      complete(429, HttpEntity(errorResponse.toJson.toString).withContentType(MediaTypes.`application/json`))
//                    }
//                  }
//
//                }
//              }
//
//            }
//
//            case false => {
//              val errorResponse = ErrorResponse(401, "Unauthorized. API KEY not valid.")
//              complete(401, HttpEntity(errorResponse.toJson.toString).withContentType(MediaTypes.`application/json`))
//            }
//          }
//        }
//      }
//    }
//  }

  val route = get {
    pathPrefix("hotels") {
      pathEndOrSingleSlash {

        val currentRequestTimeStamp = System.currentTimeMillis()  // Calculating the timestamp of the current request

        headerValueByName("Authorization") { apiKey =>

          ApiKeyManager.checkIfApiKeyIsValid(apiKey) match {
            case true => {

              // Business Logic for checking whether the request is allowed, or API Key is blocked or blacklisted.

                ApiKeyManager.checkIfRequestIsAllowed(apiKey, currentRequestTimeStamp) match {
                  case (true, nextWindowTimeStamp, rateLimitRemaining) => {

                    val rateLimitResetTime = nextWindowTimeStamp - currentRequestTimeStamp

                    respondWithHeaders(RawHeader("X-RateLimit-Reset", rateLimitResetTime.toString), RawHeader("X-RateLimit-Remaining", rateLimitRemaining.toString)) {
                      val errorResponse = ErrorResponse(200, "Valid Case. No error.")
                      complete(200, HttpEntity(errorResponse.toJson.toString).withContentType(MediaTypes.`application/json`))
                    }
                  }

                  case (false, nextWindowTimeStamp, rateLimitRemaining) => {

                    val rateLimitResetTime = nextWindowTimeStamp - currentRequestTimeStamp

                    respondWithHeaders(RawHeader("X-RateLimit-Reset", rateLimitResetTime.toString), RawHeader("X-RateLimit-Remaining", rateLimitRemaining.toString)) {
                      val errorResponse = ErrorResponse(429, "Too many requests. Rate limit exceeded. API is rate limited (only 1 request per 10 seconds allowed).")
                      complete(429, errorResponse.toJson.toString)
                    }
                  }

                }
              }

            case false => {
              val errorResponse = ErrorResponse(401, "Unauthorized. API KEY not valid.")
              complete(
                HttpResponse(
                  status = 401,
                  entity = HttpEntity(errorResponse.toJson.toString).withContentType(MediaTypes.`application/json`)
                )
              )
            }
          }
        }
      }
    }
  }

}



