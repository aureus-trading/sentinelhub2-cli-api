# SentinelHub2-Cli-Api

##### Description:
This is simple API service, which is written in Play framework (Scala). There are 2 endpoints for
generating transaction on Sentinel Network, which is built on Cosmos SDK platform. First endpoint will generate and sign the transaction and return Base64 Transaction. The second endpoint will receive this Base64 signed transaction and broadcast to any network which is based on Cosmos SDK.

##### Requirements
1. Install Open Java JDK 11 or higher and Scala SBT
2. Install Scala SDK 2.13.3 and higher plus Sbt.
2. Install Cosmos SDK. Please follow instractions on https://hub.cosmos.network/main/getting-started/installation.html.

##### How to run

1. Clone project into your local folder git clone https://github.com/aureus-trading/sentinelhub2-cli-api.
2. Update settings in application.conf based on your requirements and setup.

        cosmos-app
        chain-id
        keyring-backend
        keyring-dir
        ga
        password
        node-url
        apiKey
   a.  cosmos-app = folder of executable sentinelhub-cli app. For example: /home/talda/Projects/Go/cosmos-sdk/build/simd.
   b.  chain-id = specify your chain of your network. For example: my-test-chain.
   c   keyring-dir = specify folder of your key's folder.
   d.  gas = specify gas as per SentinelHub or Cosmos SDK documentation.
   e.  password = specify password of your sentinelhub cli.
   f.  node-url = specify url of your node -> sentinelhub-cli For example: "tcp://192.168.21.129:26657".
   g.  apiKey = api keys for http calls to api service.
   h.  keyring-backend = what type of keyring is set. Default file.


3.  Built the project and run it.
    sbt package
    sbt build

4. Debug
   sbt run

#### For more information how to build and run scala app with Play Framework please follow instruction
    https://www.playframework.com/documentation/2.8.x/Deploying

##### Endpoins

Generate and sign transaction and return as Json response.
##### 1.  <your_api_port>/tx/sign/:fromaddress/:toaddress/:amount/:assetId

###### Request:
FromAddress -> Sender
ToAddress ->  Receiver
Amount -> Amount in Long For example 100000L
AssetId -> assetId of token to be transfer

###### Response:
{hasError: boolean, error: string, signedTransaction: Based64 Tx}
Broadcast Transaction

##### 2.   <your_api_port>/tx/broadcast
###### Request:
Request Body = { "tx": "Base64 Tx"}
###### Response:
{hasError: boolean, error: string, signedTransaction: Based64 Tx}



    


