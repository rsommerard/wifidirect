package fr.inria.rsommerard.wifidirect.core.actor

import akka.actor.{Actor, ActorRef}
import fr.inria.rsommerard.wifidirect.core.message._

class Master(val nbNodes: Int) extends Actor {

  var nodes: Set[ActorRef] = Set()

  override def receive: Receive = initialize(0)

  def process: Receive = {
    case Tick => tick()
    case u: Any => dealWithUnknown("process", u.getClass.getSimpleName)
  }

  def initialize(nbReadyNodes: Int): Receive = {
    case h: Hello => hello(h)
    case Ready => ready(nbReadyNodes)
    case u: Any => dealWithUnknown("initialize", u.getClass.getSimpleName)
  }

  private def hello(h: Hello): Unit = {
    println(s"Received Hello(${h.name}) from ${sender.path.address.host.get}")
    sender ! Hello("Master")

    if (h.name == "Node") {
      nodes += sender
    }
  }

  private def ready(nbReadyNodes: Int): Unit = {
    println(s"Received Ready from ${sender.path.address.host.get}")
    val nbReady = nbReadyNodes + 1
    if (nbReady == nbNodes) {
      context.become(process)
      tick()
    } else {
      context.become(initialize(nbReady))
    }
  }

  private def tick(): Unit = nodes.foreach(n => n ! Tick)

  private def dealWithUnknown(state: String, name: String): Unit =
    println(s"State $state => Received unknown message ($name)")
}
