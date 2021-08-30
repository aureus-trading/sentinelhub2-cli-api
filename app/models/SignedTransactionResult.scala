package models
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{Format, JsPath, Json, Writes}

case class SignedTransactionResult (hasError: Boolean, error: String, signedTransaction: String)

object SignedTransactionResult{
  implicit val format: Format[SignedTransactionResult] = Json.format

  implicit val TWRModelWrite: Writes[SignedTransactionResult] = (
    (JsPath \ "hasError").write[Boolean] and
      (JsPath \ "error").write[String] and
      (JsPath \ "signedTransaction").write[String]
    )(unlift(SignedTransactionResult.unapply))
}