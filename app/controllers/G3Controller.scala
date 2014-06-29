package controllers

import play.api.mvc._
import spray.json._

// Ici, nous sommes obligé de "violer" le model de données de nvd3 pour que
// le graph affiche de que l'on veut.

// Ainsi, ici le "x" correspond à un nom de dépôts. "scala" par exemple
// et le "y" correspond à la vélocité du contrbuteur pour ce projet.
case class G3Repo(x: String, y: Int)

// Ici, "key" est le nom du contributeur et "values" est la liste de projets qui seront affichés avec ses vélocités
// sur chaque projet. Voir "data/g3DataExample.json" pour un example.
case class G3Json(key: String, values: List[G3Repo])

object G3JsonProtocol extends DefaultJsonProtocol {
  implicit val g3RepoFormat = jsonFormat2(G3Repo)
  implicit val g3Format = jsonFormat2(G3Json)
}

object G3Controller extends Controller {

  def getAll = Action {
    NotImplemented
  }

}
