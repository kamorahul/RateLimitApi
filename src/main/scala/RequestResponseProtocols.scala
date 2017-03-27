/**
  * Created by rahulkamboj on 26/03/17.
  */

import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import entities.{ErrorResponse, Hotel}


trait RequestResponseProtocols extends DefaultJsonProtocol with SprayJsonSupport {


  implicit val errorResponseProtocol = jsonFormat2(ErrorResponse.apply)
  implicit val hotelResponseProtocol = jsonFormat7(Hotel.apply)

}
