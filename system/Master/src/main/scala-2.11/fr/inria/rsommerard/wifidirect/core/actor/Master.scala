package fr.inria.rsommerard.wifidirect.core.actor

import akka.actor.{Actor, ActorRef}
import fr.inria.rsommerard.wifidirect.core.message.{Hello, Ready, Tick}

class Master(val nbNodes: Int) extends Actor {

  var nodes: Set[ActorRef] = Set()
  var nbReadyNodes: Int = 0

  override def receive: Receive = initializing

  private def initializing: Receive = {
    case Hello => hello()
    case _ => unknown("initializing")
  }

  private def waiting: Receive = {
    case Ready => ready()
    case _ => unknown("waiting")
  }

  private def processing: Receive = {
    case Tick => tick()
    case _ => unknown("processing")
  }

  private def hello(): Unit = {
    println(s"Received Hello from ${sender.path.address.host.get}")
    nodes += sender
    sender ! Hello
    if (nodes.size == nbNodes) {
      context.become(waiting)
    }
  }

  private def ready(): Unit = {
    println(s"Received Ready from ${sender.path.address.host.get}")
    nbReadyNodes += 1
    if (nbReadyNodes == nbNodes) {
      tick()
      context.become(processing)
    }
  }

  private def tick(): Unit = nodes.foreach(n => n ! Tick)

  private def unknown(state: String): Unit = println(s"State $state => Received unknown message")
}