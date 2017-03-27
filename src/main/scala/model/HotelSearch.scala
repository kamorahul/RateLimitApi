package model

/**
  * Created by rahulkamboj on 26/03/17.
  */

import entities.Hotel
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object HotelSearch {

  val hotelsCollection: Future[BSONCollection] = MongoConnection.defaultDb.map(_.collection("Hotels"))
//  def findHotels(cityId: Int) : List[Hotel] = {
  def findHotels(cityId: Int) : Future[List[Hotel]] = {
    hotelsCollection.flatMap(_.find(BSONDocument("cityId" -> cityId))
                          .sort(BSONDocument("hotelId" -> 1))
                          .cursor[BSONDocument].collect[List]())
                    .map(_.map(document =>
                      Hotel(
                        hotelId = document.getAs[Int]("hotelId").get,
                        hotelName = document.getAs[String]("hotelName").get,
                        hotelAddress = document.getAs[String]("hotelAddress").get,
                        rooms = document.getAs[Int]("rooms").get,
                        price = document.getAs[Int]("price").get,
                        stars = document.getAs[Int]("stars").get,
                        hotelType = document.getAs[String]("hotelType").get
                      )))

  }

  def findHotels(cityName: String) : List[Hotel] = {

    List[Hotel]()
  }

}
