/**
  * Created by rahulkamboj on 26/03/17.
  */



import scala.collection.mutable

object Model {

//  val blockedKeysRecords =  mutable.HashMap[String, Boolean]()
//  val blockedTimeRecords =  mutable.HashMap[String, String]()
//  val requestTimeRecords =  mutable.HashMap[String, String]()

  val apiKeyRequestRecords = mutable.HashMap[String, APIKeyRecord]()
  val defaultRequestWindowTime = 10
  val defaultRateLimitPerWindowTime = 5

  def checkIfApiKeyIsValid(apiKey: String) : Boolean = {
    true
  }

  def checkIfRequestIsAllowed(apiKey: String, requestTimeStamp: Long): (Boolean, Long) = {

    apiKeyRequestRecords.get(apiKey) match {
      case Some(apiKeyRecordFound) =>

        if(apiKeyRecordFound.isBlocked){

          val differenceBetweenTimeStamps = requestTimeStamp - apiKeyRecordFound.nextRequestAllowedAfterTimeStamp

          if(differenceBetweenTimeStamps < 0){
            // Valid request. Request came from a blocked API Key after the Blocked Window Time.

            val newRecord = APIKeyRecord(
              apiKey = apiKey,
              isBlocked = false,  // Unblocking the API Key
              lastRequestTimeStamp = requestTimeStamp,
              nextRequestAllowedAfterTimeStamp = requestTimeStamp + (10*1000L)  // Resetting this value by adding 10 seconds to the timestamp of the current request.
            )

            apiKeyRequestRecords.update(apiKey, newRecord)

            println(apiKeyRequestRecords)

            (true, newRecord.nextRequestAllowedAfterTimeStamp)
          }
          else {

            (false, 0)
          }

        }
        else {

          val differenceBetweenTwoConsecutiveRequests = requestTimeStamp - apiKeyRecordFound.nextRequestAllowedAfterTimeStamp

          if(differenceBetweenTwoConsecutiveRequests > 0){
            // Valid request

            val newRecord = APIKeyRecord(
              apiKey = apiKey,
              isBlocked = false,
              lastRequestTimeStamp = requestTimeStamp,
              nextRequestAllowedAfterTimeStamp = requestTimeStamp + (10*1000L)  // Add 10 seconds to the timestamp of the current request.
            )

            apiKeyRequestRecords.update(apiKey, newRecord)

            println(apiKeyRequestRecords)

            (true, newRecord.nextRequestAllowedAfterTimeStamp)

          }
          else{
            // Suspend the API Key

            val newRecord = APIKeyRecord(
              apiKey = apiKey,
              isBlocked = true,    // Suspending the API Key
              lastRequestTimeStamp = requestTimeStamp,
              nextRequestAllowedAfterTimeStamp = requestTimeStamp + (5*60*1000L)  // Add 5 minutes to the timestamp of the current request.
            )

            apiKeyRequestRecords.update(apiKey, newRecord)

            println(apiKeyRequestRecords)

            (false, newRecord.nextRequestAllowedAfterTimeStamp)
          }
        }

      case None =>
        // API Key does not exists in `apiKeyRequestRecords`. So a fresh entry has to be made into the Map.

        val record = APIKeyRecord(
          apiKey = apiKey,
          isBlocked = false,
          lastRequestTimeStamp = requestTimeStamp,
          nextRequestAllowedAfterTimeStamp = requestTimeStamp + (10*1000L)  // Add 10 seconds to the timestamp of the current request.
        )

        apiKeyRequestRecords.+=((apiKey, record))

        println(apiKeyRequestRecords)

        (true, record.nextRequestAllowedAfterTimeStamp)
    }

  }


}
