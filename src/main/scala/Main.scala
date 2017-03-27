/**
  * Created by rahulkamboj on 26/03/17.
  */


import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import scala.concurrent.ExecutionContext.Implicits.global
import spray.json._
import entities.ErrorResponse
import model.{ApiKeyManager, HotelSearch}
import akka.http.scaladsl.model.headers.RawHeader



trait RestApi extends AnomalyHandler {

  implicit val system  : ActorSystem
  implicit val material : Materializer



  val route = get {
    pathPrefix("hotels") {
      pathEndOrSingleSlash {

        val currentRequestTimeStamp = System.currentTimeMillis()  // Calculating the timestamp of the current request

        headerValueByName("Authorization") { apiKey =>

          ApiKeyManager.checkIfApiKeyIsValid(apiKey) match {
            case true => {

              // Business Logic for checking whether the request is allowed, or API Key is blocked/suspended or blacklisted.

                ApiKeyManager.checkIfRequestIsAllowed(apiKey, currentRequestTimeStamp) match {
                  case (true, nextWindowTimeStamp, rateLimitRemaining) => {

                    val rateLimitResetTime = nextWindowTimeStamp - currentRequestTimeStamp

                    respondWithHeaders(RawHeader("X-RateLimit-Reset", rateLimitResetTime.toString), RawHeader("X-RateLimit-Remaining", rateLimitRemaining.toString)) {

                      parameters('cityId.as[Int], 'sortBy.as[String].?, 'order.as[String].?) { (cityId, sortBy, order) =>

                        complete {
                          HotelSearch.findHotels(cityId).map { hotels =>

                          val sortedListOfHotels =
                            sortBy match {
                              case Some(sortField) =>
                                sortField.toLowerCase.trim match {
                                  case "stars" =>
                                    hotels.sortBy(_.stars)
                                  case "price" =>
                                    hotels.sortBy(_.price)
                                  case _ =>
                                    hotels
                                }

                              case None =>
                                hotels.sortBy(_.hotelId)    // Hotels are by default sorted by hotelId. So this step is not required.
                            }

                            val orderedListOfHotels =
                              if(order.isDefined && order.get.toLowerCase.trim == "desc")
                                sortedListOfHotels.reverse
                              else
                              sortedListOfHotels


                            HttpResponse(
                              status = 200,
                              entity = HttpEntity(orderedListOfHotels.toJson.toString).withContentType(MediaTypes.`application/json`))
                          }.recover{
                            case exception: Exception =>
                              println("An exception has occurred while computing this request.")
                              exception.printStackTrace()

                              val errorHttpEntity = HttpEntity(ErrorResponse(responseCode = 500, errorMessage = "An exception has occurred while serving this request.").toJson.toString()).withContentType(MediaTypes.`application/json`)

                              HttpResponse(status = InternalServerError, entity = errorHttpEntity)
                          }
                        }
                      }
                    }
                  }

                  case (false, nextWindowTimeStamp, rateLimitRemaining) => {

                    val rateLimitResetTime = nextWindowTimeStamp - currentRequestTimeStamp

                    respondWithHeaders(RawHeader("X-RateLimit-Reset", rateLimitResetTime.toString), RawHeader("X-RateLimit-Remaining", rateLimitRemaining.toString)) {
                      val errorResponse = ErrorResponse(429, "Too many requests. Rate limit exceeded. API is rate limited (only 1 request per 10 seconds allowed).")
                      complete(429, HttpEntity(errorResponse.toJson.toString).withContentType(MediaTypes.`application/json`))
                    }
                  }

                }
              }

            case false => {
              val errorResponse = ErrorResponse(401, "Unauthorized. API KEY not valid.")
              complete(401, HttpEntity(errorResponse.toJson.toString).withContentType(MediaTypes.`application/json`))
            }
          }
        }
      }
    }
  }

}


object Main extends App with RestApi{


  override implicit val system = ActorSystem("ApiRateLimiting")
  override implicit val material = ActorMaterializer()


  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")


}