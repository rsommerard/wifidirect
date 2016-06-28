package widi

import java.io.{ObjectInputStream, ObjectOutputStream}
import java.net._

import play.api.libs.json.{JsValue, Json}

import scala.sys.process.Process

class Emulator(val name: String, val deviceAddress: String, val serverPort: Int, var isDiscoverable: Boolean, val register: Register) {
  val serverSocket: ServerSocket = new ServerSocket(serverPort)

  val device: Device = Device(name, deviceAddress)

  lazy val adbPath = "/home/romain/Android/Sdk/platform-tools/adb"

  var dnsSdServiceResponse: DnsSdServiceResponse = _
  var dnsSdTxtRecord: DnsSdTxtRecord = _
  var wifiP2pConfig: WifiP2pConfig = null

  def sendStateChangedIntent(): Unit = {
    Process(s"$adbPath -s $name shell am broadcast -a ${Intent.WIFI_P2P_STATE_CHANGED_ACTION} --ei ${Extra.EXTRA_WIFI_STATE} ${Extra.WIFI_P2P_STATE_ENABLED}").run()
  }

  def sendPeersChangedIntent(): Unit = {
    Process(s"$adbPath -s $name shell am broadcast -a ${Intent.WIFI_P2P_PEERS_CHANGED_ACTION}").run()
  }

  def sendConnectionChangedIntent(): Unit = {
    Process(s"$adbPath -s $name shell am broadcast -a ${Intent.WIFI_P2P_CONNECTION_CHANGED_ACTION}").run()
  }

  def sendConnectIntent(isConnect: Boolean, isGroupOwner: Boolean = false, groupOwnerAddress: String = ""): Unit = {
    if (isConnect)
      Process(s"$adbPath -s $name shell am broadcast -a ${Intent.CONNECT} --ez ${Extra.EXTRA_CONNECT_STATE} true --ez ${Extra.EXTRA_GROUP_OWNER} $isGroupOwner --es ${Extra.EXTRA_GROUP_OWNER_ADDRESS} $groupOwnerAddress").run()
    else
      Process(s"$adbPath -s $name shell am broadcast -a ${Intent.CONNECT} --ez ${Extra.EXTRA_CONNECT_STATE} false").run()
  }

  def sendThisDeviceChangedIntent(): Unit = {
    Process(s"$adbPath -s $name shell am broadcast -a ${Intent.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION}").run()
  }

  def start() = {
    new Thread(new Runnable {

      override def run(): Unit = {
        while (true) {
          println(s"$name waiting next incoming command on port $serverPort")
          var socket = serverSocket.accept
          val oOStream: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
          val oIStream: ObjectInputStream = new ObjectInputStream(socket.getInputStream)
          val message: String  = oIStream.readObject().toString

          println(s"$message from ${socket.getInetAddress.getHostName}:${socket.getPort}")

          message match {
            case Protocol.HELLO =>
              println(s"> ${Protocol.HELLO}")
              oOStream.writeObject(Protocol.HELLO)
              oOStream.flush()
              sendStateChangedIntent()
              sendConnectIntent(false)
              sendThisDeviceChangedIntent()

            case Protocol.CARTON =>
              println(s"> ${Protocol.CARTON}")

            case Protocol.DISCOVER_PEERS =>
              println(s"> ${Protocol.DISCOVER_PEERS}")
              isDiscoverable = true
              if (register.emulators.count(e => e.isDiscoverable) > 1) {
                register.emulators.foreach(e => e.sendPeersChangedIntent())
              }

            case Protocol.STOP_DISCOVERY =>
              println(s"> ${Protocol.STOP_DISCOVERY}")
              isDiscoverable = false
              if (register.emulators.count(e => e.isDiscoverable) > 1) {
                register.emulators.foreach(e => e.sendPeersChangedIntent())
              }

            case Protocol.CANCEL_CONNECT =>
              println(s"> ${Protocol.CANCEL_CONNECT}")

              if (wifiP2pConfig != null) {
                oOStream.writeObject(Protocol.ACK)
                oOStream.flush()

                sendConnectIntent(false)
                val connectedDevice = register.emulators.filter(e => e.device.deviceAddress == wifiP2pConfig.deviceAddress)
                connectedDevice.head.sendConnectIntent(false)

                wifiP2pConfig = null
                connectedDevice.head.wifiP2pConfig = null
              } else {
                oOStream.writeObject(Protocol.CARTON)
                oOStream.flush()
              }

            case Protocol.CONNECT =>
              println(s"> ${Protocol.CONNECT}")
              val str = oIStream.readObject().toString
              println(str)

              val jsonWifiP2pConfig = Json.parse(str)
              implicit val wpsInfoFormat = Json.format[WpsInfo]
              implicit val wifiP2pConfigFormat = Json.format[WifiP2pConfig]

              wifiP2pConfig = Json.fromJson[WifiP2pConfig](jsonWifiP2pConfig).get

              val deviceToConnect = register.emulators.filter(e => e.device.deviceAddress == wifiP2pConfig.deviceAddress)

              if (deviceToConnect.nonEmpty || deviceToConnect.head.wifiP2pConfig == null ||
                wifiP2pConfig.deviceAddress != deviceToConnect.head.device.deviceAddress) {

                if (deviceToConnect.isEmpty || deviceToConnect.head.wifiP2pConfig != null) {
                  wifiP2pConfig = null
                  oOStream.writeObject(Protocol.CARTON)
                  oOStream.flush()
                } else {
                  oOStream.writeObject(Protocol.ACK)
                  oOStream.flush()

                  if (wifiP2pConfig.groupOwnerIntent < 7) {
                    sendConnectIntent(true, true, deviceAddress)
                  } else {
                    sendConnectIntent(true, false, deviceToConnect.head.deviceAddress)
                  }

                  if (wifiP2pConfig.groupOwnerIntent < 7) {
                    deviceToConnect.head.sendConnectIntent(true, false, deviceAddress)
                  } else {
                    deviceToConnect.head.sendConnectIntent(true, true, deviceToConnect.head.deviceAddress)
                  }
                }
              }

            case Protocol.REQUEST_PEERS =>
              println(s"> ${Protocol.REQUEST_PEERS}")
              implicit val deviceFormat = Json.format[Device]
              val json = Json.toJson(register.emulators.filterNot(e => e.device.eq(device)).map(e => e.device))
              println(json)
              oOStream.writeObject(json.toString())
              oOStream.flush()

            case Protocol.DISCOVER_SERVICES =>
              println(s"> ${Protocol.DISCOVER_SERVICES}")
              isDiscoverable = true

              // Receive DnsSdServiceResponse from emulator
              var str = oIStream.readObject().toString
              println(str)
              val jsonDnsSdServiceResponse = Json.parse(str)
              implicit val deviceFormat = Json.format[Device]
              implicit val dnsSdServiceResponseFormat = Json.format[DnsSdServiceResponse]
              dnsSdServiceResponse = Json.fromJson[DnsSdServiceResponse](jsonDnsSdServiceResponse).get
              dnsSdServiceResponse.srcDevice = device

              oOStream.writeObject(Protocol.ACK)
              oOStream.flush()

              // Receive DnsSdTxtRecord from emulator
              str = oIStream.readObject().toString
              println(str)
              val jsonDnsSdTxtRecord = Json.parse(str)
              implicit val dnsSdTxtRecordFormat = Json.format[DnsSdTxtRecord]
              dnsSdTxtRecord = Json.fromJson[DnsSdTxtRecord](jsonDnsSdTxtRecord).get
              dnsSdTxtRecord.srcDevice = device

              oOStream.writeObject(Protocol.ACK)
              oOStream.flush()

              if (register.emulators.count(e => e.isDiscoverable) > 1) {
                register.emulators.filter(e => e.isDiscoverable).foreach(e => e.sendPeersChangedIntent())
              }

              // Send DnsSdServiceResponse to emulator
              val dnsSdServiceResponses: List[DnsSdServiceResponse] = register.emulators.filter(e => e.isDiscoverable).filterNot(e => e.dnsSdServiceResponse == dnsSdServiceResponse).map(e => e.dnsSdServiceResponse)
              val jsonDnsSdServiceResponses: JsValue = Json.toJson(dnsSdServiceResponses)
              println(jsonDnsSdServiceResponses.toString())
              oOStream.writeObject(jsonDnsSdServiceResponses.toString())
              oOStream.flush()

              var ack = oIStream.readObject().toString
              if (Protocol.ACK == ack) {
                // Send DnsSdTxtRecord to emulator
                val dnsSdTxtRecords: List[DnsSdTxtRecord] = register.emulators.filter(e => e.isDiscoverable).filterNot(e => e.dnsSdTxtRecord.eq(dnsSdTxtRecord)).map(e => e.dnsSdTxtRecord)
                val jsonDnsSdTxtRecords = Json.toJson(dnsSdTxtRecords)
                println(jsonDnsSdTxtRecords.toString())
                oOStream.writeObject(jsonDnsSdTxtRecords.toString())
                oOStream.flush()

                ack = oIStream.readObject().toString
              }

            case _ =>
              println("> unknown")
          }

          socket.close()
        }
      }
    }).start()
  }
}