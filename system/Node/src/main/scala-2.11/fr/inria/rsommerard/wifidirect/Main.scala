package fr.inria.rsommerard.wifidirect

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import fr.inria.rsommerard.wifidirect.core.Emulator
import fr.inria.rsommerard.wifidirect.core.actor.Node

object Main extends App {

  val packageName: String = args(0).split('/')(0)

  val system = ActorSystem("NodeSystem", ConfigFactory.load("node"))

  val node = system.actorOf(Props[Node], "node")

  println("Node actor started...")

  while (! Emulator.isApplicationStarted(packageName)) {
    Thread.sleep(3000)
  }
}
