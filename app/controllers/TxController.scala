package controllers

import com.google.common.io.BaseEncoding.base64
import models.{BroadcastedTransactionResult, SignedTransactionResult, TransactionBroadcast}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc._
import play.api.Logger

import java.io.{File, PrintWriter}
import java.util.{Calendar, UUID}
import java.util.UUID.randomUUID
import javax.inject._
import scala.reflect.io
import scala.sys.process._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class TxController @Inject()(val controllerComponents: ControllerComponents,
                             val configuration: Configuration) extends BaseController {

  private val apiExeName = configuration.get[String]("cosmos-app")
  private val chainId = configuration.get[String]("chain-id")
  private val keyringBackend = configuration.get[String]("keyring-backend")
  private val keyringDir = configuration.get[String]("keyring-dir")
  private val gasPrice = configuration.get[String]("gas-prices")
  private val gas = configuration.get[String]("gas")
  private val password = configuration.get[String]("password")
  private val logger: Logger = Logger(this.getClass())
  private val nodeRpcUrl = configuration.get[String]("node-url")
  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def signTx(fromaddress: String,
             toaddress: String,
             amount: Long,
             assetId: String) = Action { implicit request: Request[AnyContent] =>

    val fileToSign = UUID.randomUUID() +  (Calendar.getInstance().toInstant.toString + ".json").toLowerCase()
    val commandGenerateTransactions = s"""echo '${password}' | ${apiExeName} tx bank send ${fromaddress} ${toaddress} ${amount}${assetId} --chain-id ${chainId} --keyring-backend ${keyringBackend} --keyring-dir ${keyringDir} --gas-prices ${gasPrice} --gas ${gas} --yes --generate-only --node "${nodeRpcUrl}""""
    try {
      val seqScriptGenTX= Seq("/bin/sh","-c", commandGenerateTransactions)
      val generateJsonTransaction = seqScriptGenTX.!!.trim
      val jsonWriter = new PrintWriter(new File(fileToSign))
      jsonWriter.write(generateJsonTransaction)
      jsonWriter.close()
      logger.info(s"Generating Unsigned TX from ${fromaddress} to ${toaddress} amount ${amount} denom ${assetId}")
      val commandSignTransaction = s"""echo '${password}' | ${apiExeName} tx sign  ${fileToSign} --chain-id ${chainId} --keyring-backend ${keyringBackend} --keyring-dir ${keyringDir} --from ${fromaddress} --gas-prices ${gasPrice} --gas ${gas} --node "${nodeRpcUrl}""""
      val seqScriptSignTX= Seq("/bin/sh","-c", commandSignTransaction)
      val signedTransaction = seqScriptSignTX.!!.trim
      val file = io.File(fileToSign)
      file.delete()
      val encodedBytes = base64.encode(signedTransaction.getBytes())
      Ok(Json.toJson(SignedTransactionResult(false, "", encodedBytes)))
    } catch {
      case exception: Exception => {
        val file = io.File(fileToSign)
        file.delete()
        logger.error(exception.getMessage)
        BadRequest(Json.toJson(SignedTransactionResult(true, "error has occured", "")))
      }
    }
  }

  def broadCastTx() = Action { implicit request: Request[AnyContent] =>
    val newId = randomUUID()
    val jsonBody: Option[String] = request.body.asText
    val fileName = newId + "_" + Calendar.getInstance().toInstant.toString
    try {
      val decodedBytes = base64.decode(jsonBody.getOrElse(""))
      val decodedBytesToString = new String(decodedBytes)
      val jsonWriter = new PrintWriter(new File(fileName))
      jsonWriter.write(decodedBytesToString)
      jsonWriter.close()
      val commandBroadCastTransaction = s"""${apiExeName} tx broadcast  ${fileName} --broadcast-mode sync  --keyring-backend ${keyringBackend} --keyring-dir ${keyringDir} --gas-prices ${gasPrice} --gas ${gas} --node "${nodeRpcUrl}""""
      val signedTransaction = commandBroadCastTransaction.!!
      val splitResult = signedTransaction.split("\n").toList
      val txResult : TransactionBroadcast = new TransactionBroadcast(splitResult(0).split(":")(1).trim,
        splitResult(8).split(":")(1).trim,
        splitResult(9).split(":")(1).trim,
        splitResult(11).split(":")(1).trim)
      val file = io.File(fileName)
      file.delete()
      Ok(Json.toJson(BroadcastedTransactionResult(false, "", txResult)))
    } catch {
      case exception: Exception => {
        val file = io.File(fileName)
        file.delete()
        BadRequest(Json.toJson(SignedTransactionResult(true, exception.getMessage, "")))
      }
    }
  }
}
