package fr.inria.rsommerard.wifidirect.core.actor

import akka.actor.Actor
import fr.inria.rsommerard.wifidirect.core.Emulator
import fr.inria.rsommerard.wifidirect.core.message.{Hello, Tick}

import scala.util.Random

class Node extends Actor {

  val master = context.actorSelection("akka.tcp://MasterSystem@10.32.0.42:2552/user/master")

  override def preStart() {
    master ! Hello
  }

  override def receive: Receive = {
    case Hello =>
      println(s"Received Hello from master")

    case Tick =>
      println(s"Received Tick from ${sender.path.address.host.get}")
      val rand = new Random()
      val lon = rand.nextInt(50).toDouble + rand.nextDouble()
      val lat = rand.nextInt(50).toDouble + rand.nextDouble()
      Emulator.setGPSLocation(lat, lon)

    case _ =>
      println("Received unknown message")
  }
}
