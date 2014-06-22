package controllers

import play.api.mvc._
import traits.SyncRedisClient

object G3Controller extends Controller with SyncRedisClient {

  def getAll = Action {
    NotImplemented
  }

}
