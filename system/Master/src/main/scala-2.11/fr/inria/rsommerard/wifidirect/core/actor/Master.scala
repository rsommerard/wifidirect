package fr.inria.rsommerard.wifidirect.core.actor

import akka.actor.{Actor, ActorRef}
import fr.inria.rsommerard.wifidirect.core.message._

class Master(val nbNodes: Int) extends Actor {

  var nodes: Set[ActorRef] = Set()
  var nbReadyNodes: Int = 0

  var tickValue: Int = -1

  override def receive: Receive = {
    case h: Hello => hello(h)
    case Ready => ready()
    case Tick => tick()
    case u: Any => dealWithUnknown("receive", u.getClass.getSimpleName)
  }

  private def hello(h: Hello): Unit = {
    println(s"Received Hello(${h.msg}) from ${sender.path.address.host.get}")
    sender ! Hello("Master")

    if (h.msg == "Node") {
      nodes += sender
    }
  }

  private def ready(): Unit = {
    println(s"Received Ready from ${sender.path.address.host.get}")
    nbReadyNodes += 1
    if (nbReadyNodes == nbNodes) tick()
  }

  private def tick(): Unit = {
    tickValue += 1
    broadcast(Tick(tickValue))
  }

  private def broadcast(message: Message): Unit = nodes.foreach(n => n ! message)

  private def dealWithUnknown(state: String, name: String): Unit = println(s"State $state => Received unknown message ($name)")
}