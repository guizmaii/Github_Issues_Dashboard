package controllers

import play.api.mvc._
import traits.SyncRedisClient

object G3Graphs extends Controller with SyncRedisClient {

  def getAll = Action {
    NotImplemented
  }

}
