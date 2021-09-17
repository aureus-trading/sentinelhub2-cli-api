package controllers

import models.{BroadCastRequest, BroadcastedTransactionResult, TransactionBroadcast}
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc._
import services.ICosmosTxService

import javax.inject._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class TxController @Inject()(val controllerComponents: ControllerComponents,
                             val cosmosTxService: ICosmosTxService) extends BaseController {


  var test = "{\"height\":\"0\",\"txhash\":\"9C925D480381FACEAF32EE923673A26BB479CAC2129DE50A7F5F9362C0918D0B\",\"codespace\":\"\",\"code\":0,\"data\":\"\",\"raw_log\":\"[]\",\"logs\":[],\"info\":\"\",\"gas_wanted\":\"0\",\"gas_used\":\"0\",\"tx\":null,\"timestamp\":\"\"}\n"

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def signTx(fromAddress: String,
             toAddress: String,
             amount: Long,
             assetId: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>

    val apiKeyCheck = request.headers.get("ApiKey").getOrElse("")
    if (cosmosTxService.verifyApiKeys(apiKeyCheck)) {
      val signedResult = cosmosTxService.generateAndSignTx(fromAddress, toAddress, amount.toString, assetId)
      if (signedResult.hasError) {
        BadRequest(Json.toJson(signedResult))
      } else {
        Ok(Json.toJson(signedResult))
      }
    }
    else {
      Unauthorized
    }
  }

  def broadCastTx(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val apiKeyCheck = request.headers.get("ApiKey").getOrElse("")
    if (cosmosTxService.verifyApiKeys(apiKeyCheck)) {
      var resultBroadCastTx: BroadcastedTransactionResult = null
      if(request.body.asJson.getOrElse(null) != null){
        request.body.asJson.map {
          json => {
            val broadCastRequest = json.validate[BroadCastRequest]
            broadCastRequest match {
              case JsSuccess(value, path) => resultBroadCastTx = cosmosTxService.broadCastTx(value)
              case e: JsError => resultBroadCastTx = BroadcastedTransactionResult(true, "Missing tx in body", TransactionBroadcast("", "", ""))
            }
          }
        }
        if(!resultBroadCastTx.hasError) {
          Ok(Json.toJson(resultBroadCastTx))
        } else {
          BadRequest(Json.toJson(resultBroadCastTx))
        }
      }else{
        BadRequest(Json.toJson(BroadcastedTransactionResult(true, "Missing tx in body", TransactionBroadcast("", "", ""))))
      }
    } else {
      Unauthorized
    }
  }


}
