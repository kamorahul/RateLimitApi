package model

/**
  * Created by rahulkamboj on 27/03/17.
  */

import reactivemongo.api.MongoDriver
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.indexes.{Index, IndexType}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.typesafe.config.ConfigFactory
import akka.actor._
import akka.stream.ActorMaterializer

object MongoConnection{

  implicit val system = ActorSystem("MongoSystem")
  implicit val material = ActorMaterializer()


  /*
   * ---------------------
   *      CONFIGS
   * --------------------
   */

  private val mongoConfig = ConfigFactory.load().getConfig("mongo")

  private val host = mongoConfig.getString("host")
  private val port = mongoConfig.getInt("port")
  private val databaseName = mongoConfig.getString("database")


  /*
   * ---------------------------------------
   *      DRIVER, NODES and CONNECTION
   * ---------------------------------------
   */

  private val databaseDriver = new MongoDriver()
  private val mongoNodes = List(host+":"+port)

  private val mongoConnection = databaseDriver.connection(nodes = mongoNodes)


  /*
   * -----------------------------------------
   *      DefaultDb (and MongoCollection ??)
   * -----------------------------------------
   */

  val defaultDb = mongoConnection.database(databaseName)


}
