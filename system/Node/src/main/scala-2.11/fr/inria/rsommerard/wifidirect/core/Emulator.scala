package fr.inria.rsommerard.wifidirect.core

import java.io.PrintStream

import org.apache.commons.net.telnet.TelnetClient

import scala.sys.process.Process

object Emulator {

  val ADB = "/android-sdk-linux/platform-tools/adb"

  def setGPSLocation(lon: Double, lat: Double): Unit = {
    println("[Emulator] Change GPS location to " + lon + " " + lat)

    val tn = new TelnetClient
    tn.connect("localhost", 5554)

    val out = new PrintStream(tn.getOutputStream)
    out.println(s"geo fix $lon $lat")
    out.flush()
    out.close()
    tn.disconnect()
  }

  def isApplicationStarted(packageName: String): Boolean = {
    if (Process(s"$ADB devices").! != 0)
      return false

    val isEmulatorStarted: Boolean = Process(s"$ADB devices").!!.trim.contains("emulator-5554")

    if (Process(s"$ADB -s emulator-5554 shell ps").! != 0)
      return false

    isEmulatorStarted && Process(s"$ADB -s emulator-5554 shell ps").!!.trim.contains(packageName)
  }
}
