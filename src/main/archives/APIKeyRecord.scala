/**
  * Created by rahulkamboj on 26/03/17.
  */

case class APIKeyRecord(apiKey: String,
                        lastRequestTimeStamp: Long,
                        isBlocked: Boolean,
                        nextRequestAllowedAfterTimeStamp: Long,
                        rateLimitPerWindow: Int = 5)  // Set the global value of Rate Limit. Fetch this value from the configuration file.
