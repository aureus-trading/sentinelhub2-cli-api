package models

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{Format, JsPath, Json, Reads, Writes}

case class BroadCastRequest(tx: String)

object BroadCastRequest{
  implicit val format: Format[BroadCastRequest] = Json.format
}