package controllers

import play.api.mvc._
import traits.SyncRedisable

object G3Graphs extends Controller with SyncRedisable {

  def getAll = Action {
    NotImplemented
  }

}
