package helpers

import org.joda.time.DateTime

object TimeHelper {

  // Date de lancement de Github (voir Wikipedia) : 01/04/2008
  // Ce sera notre année 0 en qq sorte ou encore, pour les informaticiens, cela équivaut au 01/01/1970 du temps Posix.
  val githubOpenDate = new DateTime(2008, 4, 1, 0, 0)

  def dateTimeToTimestamp(date: DateTime) = date.toDate.getTime

}
