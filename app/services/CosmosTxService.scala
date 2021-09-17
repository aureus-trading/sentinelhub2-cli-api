package services

import com.google.common.io.BaseEncoding.base64
import models.{BroadCastRequest, BroadcastedTransactionResult, SignedTransactionResult, TransactionBroadcast}
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import scala.sys.process._
import java.io.{File, PrintWriter}
import java.util.UUID.randomUUID
import java.util.{Calendar, UUID}
import javax.inject.Inject
import scala.reflect.io

trait ICosmosTxService{
  def generateAndSignTx(fromAddress: String, toAddress: String, amount: String, assetId: String): SignedTransactionResult
  def broadCastTx(jsonBody: BroadCastRequest): BroadcastedTransactionResult
  def verifyApiKeys(key: String): Boolean
}

class CosmosTxService @Inject() (configuration: Configuration)  extends ICosmosTxService {
  private val apiExeName = configuration.get[String]("cosmos-app")
  private val chainId = configuration.get[String]("chain-id")
  private val keyringBackend = configuration.get[String]("keyring-backend")
  private val keyringDir = configuration.get[String]("keyring-dir")
  private val gasPrice = configuration.get[String]("gas-prices")
  private val gas = configuration.get[String]("gas")
  private val password = configuration.get[String]("password")
  private val logger: Logger = Logger(this.getClass())
  private val nodeRpcUrl = configuration.get[String]("node-url")
  private val apiKeys = configuration.get[String]("apiKey")

  def checkForTheField(index: String, searchText : List[String]): String = {
    var result = ""
    for (item <- searchText) {
      if (item.contains(index)) {
        val findIndex = item.trim.indexOf(":")
        if (findIndex != -1) {
          result = item.substring(findIndex + 1, item.size).trim
        }
      }
    }
    result
  }

  override def generateAndSignTx(fromAddress: String, toAddress: String, amount: String, assetId: String): SignedTransactionResult = {
    val fileToSign = UUID.randomUUID() + (Calendar.getInstance().toInstant.toString + ".json").toLowerCase()
    val commandGenerateTransactions = s"""echo '${password}' | ${apiExeName} tx bank send ${fromAddress} ${toAddress} ${amount}${assetId} --chain-id ${chainId} --keyring-backend ${keyringBackend} --keyring-dir ${keyringDir} --gas-prices ${gasPrice} --gas ${gas} --yes --generate-only --node "${nodeRpcUrl}""""
    var commandSignTransaction = ""
    var generateJsonTransaction = ""
    try {
      val seqScriptGenTX = Seq("/bin/sh", "-c", commandGenerateTransactions)
      generateJsonTransaction = seqScriptGenTX.!!.trim
      val jsonWriter = new PrintWriter(new File(fileToSign))
      jsonWriter.write(generateJsonTransaction)
      jsonWriter.close()
      logger.info(s"Generating Unsigned TX from ${fromAddress} to ${toAddress} amount ${amount} denom ${assetId}")
      commandSignTransaction = s"""echo '${password}' | ${apiExeName} tx sign  ${fileToSign} --chain-id ${chainId} --keyring-backend ${keyringBackend} --keyring-dir ${keyringDir} --from ${fromAddress} --gas-prices ${gasPrice} --gas ${gas} --node "${nodeRpcUrl}""""
      val seqScriptSignTX = Seq("/bin/sh", "-c", commandSignTransaction)
      val signedTransaction = seqScriptSignTX.!!.trim
      val file = io.File(fileToSign)
      file.delete()
      val encodedBytes = base64.encode(signedTransaction.getBytes())
      SignedTransactionResult(false, "", encodedBytes)
    } catch {
      case exception: Exception => {
        val file = io.File(fileToSign)
        file.delete()
        logger.error(exception.getMessage + " " + commandSignTransaction)
        SignedTransactionResult(true, exception.getMessage + " " + commandSignTransaction + " " + generateJsonTransaction, "")
      }
    }
  }

  override def broadCastTx(jsonBody: BroadCastRequest): BroadcastedTransactionResult = {
    val newId = randomUUID()
    val fileName = newId + "_" + Calendar.getInstance().toInstant.toString
    var splitResult = ""
    try {
      val decodedBytes = base64.decode(jsonBody.tx)
      val decodedBytesToString = new String(decodedBytes)
      val jsonWriter = new PrintWriter(new File(fileName))
      jsonWriter.write(decodedBytesToString)
      jsonWriter.close()
      logger.info(s"Broadcasting signed tx signedTX ${decodedBytes}")
      val commandBroadCastTransaction = s"""${apiExeName} tx broadcast  ${fileName} --broadcast-mode sync  --keyring-backend ${keyringBackend} --keyring-dir ${keyringDir} --gas-prices ${gasPrice} --gas ${gas} --node "${nodeRpcUrl}""""

      val resultBroadCastTX =  commandBroadCastTransaction.!!.trim
      val trimmedBroadcastTX = resultBroadCastTX.replace("\"", "")
      var splitResult = trimmedBroadcastTX.split("\n").toList
      if(splitResult.size <= 1){
        splitResult = trimmedBroadcastTX.split(",").toList
      }
      val txHash = checkForTheField("txhash", splitResult).replace("/", "").replace("\"", "")
      val rawData = checkForTheField("raw_data", splitResult).replace("/","\"")
      val code = checkForTheField("raw_data", splitResult).replace("/","\"")
      val txResult : TransactionBroadcast = new TransactionBroadcast(code.trim, rawData.trim,txHash.trim)
      val file = io.File(fileName)
      file.delete()
      BroadcastedTransactionResult(false, "", txResult)
    } catch {
      case exception: Exception =>
        val file = io.File(fileName)
        file.delete()
        logger.error(exception + splitResult)
        BroadcastedTransactionResult(true, exception.getMessage,TransactionBroadcast("","",""))
    }
  }

  override  def verifyApiKeys(key: String): Boolean = {
     if(key == apiKeys){
       true
     }
    else{
       false
     }
  }
}
