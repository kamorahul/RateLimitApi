package entities

/**
  * Created by rahulkamboj on 26/03/17.
  */
case class APIKeyRequestRecord(isSuspended: Boolean,
                               nextWindowTimeStamp: Long,
                               requestsReceivedInCurrentWindow: Int = 0)
