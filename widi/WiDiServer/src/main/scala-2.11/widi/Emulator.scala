package widi

import java.io.{ObjectInputStream, ObjectOutputStream}
import java.net._

import play.api.libs.json.{JsValue, Json}

import scala.sys.process.Process
import scala.util.Try

class Emulator(val name: String, val deviceAddress: String, val serverPort: Int, var isDiscoverable: Boolean, val register: Register) {
  val serverSocket: ServerSocket = new ServerSocket(serverPort)

  val device: Device = Device(name, deviceAddress)

  val adbPath = {
    if (Try(Process("/android-sdk-linux/platform-tools/adb version").!).isSuccess)
      "/android-sdk-linux/platform-tools/adb"
    else
      "/home/romain/Android/Sdk/platform-tools/adb"
  }

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
          implicit val oOStream: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
          implicit val oIStream: ObjectInputStream = new ObjectInputStream(socket.getInputStream)
          val message: String  = receive()

          println(s"$message from ${socket.getInetAddress.getHostName}:${socket.getPort}")

          message match {
            case Protocol.HELLO => hello()
            case Protocol.CARTON => carton()
            case Protocol.DISCOVER_PEERS => discoverPeers()
            case Protocol.STOP_DISCOVERY => stopDiscovery()
            case Protocol.CANCEL_CONNECT => cancelConnect()
            case Protocol.CONNECT => connect()
            case Protocol.REQUEST_PEERS => requestPeers()
            case Protocol.DISCOVER_SERVICES => discoverServices()
            case _ => unknown()
          }

          socket.close()
        }
      }
    }).start()
  }

  def send(message: String)(implicit oOStream: ObjectOutputStream): Unit = {
    oOStream.writeObject(message)
    oOStream.flush()
  }

  def receive()(implicit oIStream: ObjectInputStream): String = {
    oIStream.readObject().toString
  }

  def hello()(implicit oOStream: ObjectOutputStream): Unit = {
    println(s"> ${Protocol.HELLO}")
    send(Protocol.HELLO)
    sendStateChangedIntent()
    sendConnectIntent(false)
    sendThisDeviceChangedIntent()
  }

  def carton(): Unit = {
    println(s"> ${Protocol.CARTON}")
  }

  def discoverPeers(): Unit = {
    println(s"> ${Protocol.DISCOVER_PEERS}")
    isDiscoverable = true
    if (register.emulators.count(e => e.isDiscoverable) > 1) {
      register.emulators.foreach(e => e.sendPeersChangedIntent())
    }
  }

  def stopDiscovery(): Unit = {
    println(s"> ${Protocol.STOP_DISCOVERY}")
    isDiscoverable = false
    if (register.emulators.count(e => e.isDiscoverable) > 1) {
      register.emulators.foreach(e => e.sendPeersChangedIntent())
    }
  }

  def cancelConnect()(implicit oOStream: ObjectOutputStream): Unit = {
    println(s"> ${Protocol.CANCEL_CONNECT}")

    if (wifiP2pConfig == null) {
      send(Protocol.CARTON)
      return
    }

    send(Protocol.ACK)

    sendConnectIntent(false)
    val connectedDevice = register.emulators.filter(e => e.device.deviceAddress == wifiP2pConfig.deviceAddress && e != this)
    connectedDevice.head.sendConnectIntent(false)

    wifiP2pConfig = null
    connectedDevice.head.wifiP2pConfig = null
  }

  def connect()(implicit oIStream: ObjectInputStream, oOStream: ObjectOutputStream): Unit = {
    println(s"> ${Protocol.CONNECT}")
    val str = receive()
    println(str)

    val jsonWifiP2pConfig = Json.parse(str)
    implicit val wpsInfoFormat = Json.format[WpsInfo]
    implicit val wifiP2pConfigFormat = Json.format[WifiP2pConfig]

    wifiP2pConfig = Json.fromJson[WifiP2pConfig](jsonWifiP2pConfig).get

    val deviceToConnect = register.emulators.filter(e => e.device.deviceAddress == wifiP2pConfig.deviceAddress && e != this)

    if (deviceToConnect.isEmpty && deviceToConnect.head.wifiP2pConfig != null &&
      wifiP2pConfig.deviceAddress == deviceToConnect.head.device.deviceAddress) {
      return
    }

    if (deviceToConnect.isEmpty || deviceToConnect.head.wifiP2pConfig != null) {
      send(Protocol.CARTON)
      return
    }

    deviceToConnect.head.wifiP2pConfig = WifiP2pConfig(this.device.deviceAddress, wifiP2pConfig.wps,
      wifiP2pConfig.netId, 0)

    send(Protocol.ACK)

    if (wifiP2pConfig.groupOwnerIntent < 7) {
      sendConnectIntent(true, true, deviceAddress)
      deviceToConnect.head.sendConnectIntent(true, false, deviceAddress)
      return
    }

    sendConnectIntent(true, false, deviceToConnect.head.deviceAddress)
    deviceToConnect.head.sendConnectIntent(true, true, deviceToConnect.head.deviceAddress)
  }

  def requestPeers()(implicit oOStream: ObjectOutputStream): Unit = {
    println(s"> ${Protocol.REQUEST_PEERS}")
    implicit val deviceFormat = Json.format[Device]
    val json = Json.toJson(register.emulators.filterNot(e => e.device.eq(device)).map(e => e.device))
    println(json)
    send(json.toString())
  }

  def discoverServices()(implicit oIStream: ObjectInputStream, oOStream: ObjectOutputStream): Unit = {
    println(s"> ${Protocol.DISCOVER_SERVICES}")
    isDiscoverable = true

    // Receive DnsSdServiceResponse from emulator
    var str = receive()
    println(str)
    val jsonDnsSdServiceResponse = Json.parse(str)
    implicit val deviceFormat = Json.format[Device]
    implicit val dnsSdServiceResponseFormat = Json.format[DnsSdServiceResponse]
    dnsSdServiceResponse = Json.fromJson[DnsSdServiceResponse](jsonDnsSdServiceResponse).get
    dnsSdServiceResponse.srcDevice = device

    send(Protocol.ACK)

    // Receive DnsSdTxtRecord from emulator
    str = receive()
    println(str)
    val jsonDnsSdTxtRecord = Json.parse(str)
    implicit val dnsSdTxtRecordFormat = Json.format[DnsSdTxtRecord]
    dnsSdTxtRecord = Json.fromJson[DnsSdTxtRecord](jsonDnsSdTxtRecord).get
    dnsSdTxtRecord.srcDevice = device

    send(Protocol.ACK)

    if (register.emulators.count(e => e.isDiscoverable) > 1) {
      register.emulators.filter(e => e.isDiscoverable).foreach(e => e.sendPeersChangedIntent())
    }

    // Send DnsSdServiceResponse to emulator
    val dnsSdServiceResponses: List[DnsSdServiceResponse] = register.emulators.filter(e => e.isDiscoverable).filterNot(e => e.dnsSdServiceResponse == dnsSdServiceResponse).map(e => e.dnsSdServiceResponse)
    val jsonDnsSdServiceResponses: JsValue = Json.toJson(dnsSdServiceResponses)
    println(jsonDnsSdServiceResponses.toString())
    send(jsonDnsSdServiceResponses.toString())

    var ack = receive()
    if (Protocol.ACK != ack) {
      return
    }

    // Send DnsSdTxtRecord to emulator
    val dnsSdTxtRecords: List[DnsSdTxtRecord] = register.emulators.filter(e => e.isDiscoverable).filterNot(e => e.dnsSdTxtRecord.eq(dnsSdTxtRecord)).map(e => e.dnsSdTxtRecord)
    val jsonDnsSdTxtRecords = Json.toJson(dnsSdTxtRecords)
    println(jsonDnsSdTxtRecords.toString())
    send(jsonDnsSdTxtRecords.toString())

    ack = receive()
  }

  def unknown(): Unit = {
    println("> unknown")
  }
}
