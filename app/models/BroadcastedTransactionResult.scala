package models

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{Format, JsPath, Json, Reads, Writes}

case class BroadcastedTransactionResult(hasError: Boolean, error: String, tx: TransactionBroadcast)

case class TransactionBroadcast(code: String, raw_logs: String, timestamp: String, txhash: String)

object BroadcastedTransactionResult {

  implicit val format: Format[BroadcastedTransactionResult] = Json.format

  implicit val TWRModelWrite: Writes[BroadcastedTransactionResult] = (
    (JsPath \ "hasError").write[Boolean] and
      (JsPath \ "error").write[String] and
      (JsPath \ "tx").write[TransactionBroadcast]
    ) (unlift(BroadcastedTransactionResult.unapply))
}


object TransactionBroadcast {

  implicit val format: Format[TransactionBroadcast] = Json.format

  implicit val TWRModelWrite: Writes[TransactionBroadcast] = (
    (JsPath \ "code").write[String] and
      (JsPath \ "txhash").write[String] and
      (JsPath \ "raw_logs").write[String] and
      (JsPath \ "timestamp").write[String]) (unlift(TransactionBroadcast.unapply))

  implicit val rdCHR : Reads[TransactionBroadcast] = (
    (JsPath \ "code").read[String] and
      (JsPath \ "txhash").read[String] and
      (JsPath \ "raw_logs").read[String] and
      (JsPath \ "timestamp").read[String])(TransactionBroadcast.apply _)
}

