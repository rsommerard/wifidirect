package fr.inria.rsommerard.wifidirect

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import fr.inria.rsommerard.wifidirect.core.Emulator
import fr.inria.rsommerard.wifidirect.core.actor.ServiceDiscovery
import fr.inria.rsommerard.wifidirect.core.message.Ready

object Main extends App {

  val system = ActorSystem("ServiceDiscoverySystem", ConfigFactory.load("servicediscovery"))
  val serviceDiscovery = system.actorOf(Props[ServiceDiscovery], "servicediscovery")

  println("ServiceDiscovery actor started...")
}
