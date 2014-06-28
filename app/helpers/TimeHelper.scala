package helpers

import org.joda.time.DateTime

object TimeHelper {

  def dateTimeToTimestamp(date: DateTime) = date.toDate.getTime

}
