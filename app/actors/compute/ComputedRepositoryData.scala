package actors.compute

import domain.GraphType
import play.api.libs.json.JsValue

case class ComputedRepositoryData(graph: GraphType, repoName: String, repoOwner: String, computedData: List[JsValue])