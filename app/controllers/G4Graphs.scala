package controllers

import play.api.mvc._
import traits.SyncRedisClient

object G4Graphs extends Controller with SyncRedisClient {

  def getAll = Action {
    NotImplemented
  }

}
