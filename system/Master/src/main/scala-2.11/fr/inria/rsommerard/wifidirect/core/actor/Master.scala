package fr.inria.rsommerard.wifidirect.core.actor

import akka.actor.{Actor, ActorRef}
import fr.inria.rsommerard.wifidirect.core.message._

class Master(val nbNodes: Int) extends Actor {

  var nodes: Set[ActorRef] = Set()
  var nbReadyNodes: Int = 0
  var locations: Map[ActorRef, Location] = Map()

  var tickValue: Int = -1

  override def receive: Receive = initializing

  private def initializing: Receive = {
    case Hello => hello()
    case UI => processUI()
    case u: Any => dealWithUnknown("initializing", u.getClass.getSimpleName)
  }

  private def waiting: Receive = {
    case Ready => ready()
    case UI => processUI()
    case u: Any => dealWithUnknown("waiting", u.getClass.getSimpleName)
  }

  private def moving: Receive = {
    case l: Location => processLocation(l)
    case UI => processUI()
    case u: Any => dealWithUnknown("moving", u.getClass.getSimpleName)
  }

  private def processing: Receive = {
    case Tick => sendTick()
    case UI => processUI()
    case u: Any => dealWithUnknown("processing", u.getClass.getSimpleName)
  }

  private def processUI(): Unit = {
    sender ! LocationList(locations)
  }

  private def processLocation(location: Location): Unit = {
    println(s"Received Location from ${sender.path.address.host.get}")
    locations += (sender -> location)
    if (locations.size == nbNodes) {
      println(s"Sending ${locations.size} locations")
      context.become(processing)
      broadcast(LocationList(locations))
    }
  }

  private def hello(): Unit = {
    println(s"Received Hello from ${sender.path.address.host.get}")
    nodes += sender
    sender ! Hello
    if (nodes.size == nbNodes) context.become(waiting)
  }

  private def ready(): Unit = {
    println(s"Received Ready from ${sender.path.address.host.get}")
    nbReadyNodes += 1
    if (nbReadyNodes == nbNodes) sendTick()
  }

  private def sendTick(): Unit = {
    tickValue += 1
    context.become(moving)
    nodes.foreach(n => n ! Tick(tickValue))
  }

  private def broadcast(message: Message): Unit = nodes.foreach(n => n ! message)

  private def dealWithUnknown(state: String, name: String): Unit = println(s"State $state => Received unknown message ($name)")
}