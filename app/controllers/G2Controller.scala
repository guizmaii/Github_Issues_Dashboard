package controllers

import play.api.mvc._
import traits.SyncRedisClient

object G2Controller extends Controller with SyncRedisClient {

  def getAll = Action {
    NotImplemented
  }

}
