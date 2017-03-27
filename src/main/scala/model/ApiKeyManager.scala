package model

/**
  * Created by rahulkamboj on 26/03/17.
  */

import entities.{APIKey, APIKeyRequestRecord}

import scala.collection.mutable



object ApiKeyManager {


  val requestsFromApiKeyRecords = mutable.HashMap[String, APIKeyRequestRecord]()
  val apiKeysStore = mutable.HashMap[String, APIKey]()

  val defaultRequestWindowTime = 30
  val defaultRateLimitPerWindowTime = 10


  def checkIfApiKeyIsValid(apiKey: String) : Boolean = {

    apiKeysStore.get(apiKey) match {
      case  Some(_) =>
        true

      case None =>
        // Check the Database if it exists there.

        // Assuming it exists in the Database. For now, setting the default rate limit per window for each API Key.
        apiKeysStore += (apiKey -> APIKey(apiKey, defaultRateLimitPerWindowTime))

        println(apiKeysStore)

        true
    }

  }


  def checkIfRequestIsAllowed(apiKey: String, requestTimeStamp: Long): (Boolean, Long, Int) = {

    requestsFromApiKeyRecords.get(apiKey) match {
      case Some(apiKeyRecordFound) =>

        val numberOfRequestsAllowedPerWindow = apiKeysStore(apiKey).requestsAllowedPerWindow
        val differenceBetweenTimeStamps = requestTimeStamp - apiKeyRecordFound.nextWindowTimeStamp

        println("Time Delta: " + differenceBetweenTimeStamps)

        if(differenceBetweenTimeStamps > 0){
            // Valid request

            val newRecord = APIKeyRequestRecord(
              isSuspended = false,
              requestsReceivedInCurrentWindow = 1,    // Reset this value to 1.
              nextWindowTimeStamp = requestTimeStamp + (defaultRequestWindowTime*1000L)  // Add 30 seconds to the timestamp of the current request.
            )

            requestsFromApiKeyRecords.update(apiKey, newRecord)

            println("Case 1: " + requestsFromApiKeyRecords)

            (true, newRecord.nextWindowTimeStamp, numberOfRequestsAllowedPerWindow - 1)

          }
        else{

          // Requests received is in the current window. Check for isSuspended and Rate-Limit.

          if(!apiKeyRecordFound.isSuspended) {

            if (apiKeyRecordFound.requestsReceivedInCurrentWindow < numberOfRequestsAllowedPerWindow) {

              // Request is within the allowed Rate Limit per Window Time

              val newRecord = APIKeyRequestRecord(
                isSuspended = false,
                requestsReceivedInCurrentWindow = apiKeyRecordFound.requestsReceivedInCurrentWindow + 1,
                nextWindowTimeStamp = apiKeyRecordFound.nextWindowTimeStamp // No change in the next window timestamp.
              )

              requestsFromApiKeyRecords.update(apiKey, newRecord)

              println("Case 2: " + requestsFromApiKeyRecords)

              (true, newRecord.nextWindowTimeStamp, numberOfRequestsAllowedPerWindow - newRecord.requestsReceivedInCurrentWindow)
            }
            else {

              // Suspend the API Key

              val newRecord = APIKeyRequestRecord(
                isSuspended = true,
                requestsReceivedInCurrentWindow = apiKeyRecordFound.requestsReceivedInCurrentWindow + 1,
                nextWindowTimeStamp = requestTimeStamp + (5 * 60 * 1000L) // Add 5 minutes to the timestamp of the current request.
              )

              requestsFromApiKeyRecords.update(apiKey, newRecord)

              println("Case 3: " + requestsFromApiKeyRecords)

              (false, newRecord.nextWindowTimeStamp, numberOfRequestsAllowedPerWindow - newRecord.requestsReceivedInCurrentWindow)

            }
          }
          else{
            // API Key is already suspended and the request has come before the next allowed Window.

            val newRecord = APIKeyRequestRecord(
              isSuspended = true,
              requestsReceivedInCurrentWindow = apiKeyRecordFound.requestsReceivedInCurrentWindow + 1,
              nextWindowTimeStamp = apiKeyRecordFound.nextWindowTimeStamp     // nextWindowTimeStamp remains unchanged.
            )

            requestsFromApiKeyRecords.update(apiKey, newRecord)

            println("Case 4: " + requestsFromApiKeyRecords)


            (false, apiKeyRecordFound.nextWindowTimeStamp, numberOfRequestsAllowedPerWindow - apiKeyRecordFound.requestsReceivedInCurrentWindow)
          }

        }

      case None =>
        // API Key does not exists in `apiKeyRequestRecords`. So a fresh entry has to be made into the Map.

        val record = APIKeyRequestRecord(
          isSuspended = false,
          requestsReceivedInCurrentWindow = 1,
          nextWindowTimeStamp = requestTimeStamp + (defaultRequestWindowTime*1000L)   // Add 30 seconds to the timestamp of the current request.
        )

        requestsFromApiKeyRecords += (apiKey -> record)

        println("Case 5: " + requestsFromApiKeyRecords)

        (true, record.nextWindowTimeStamp,  apiKeysStore(apiKey).requestsAllowedPerWindow - 1)
    }

  }


}