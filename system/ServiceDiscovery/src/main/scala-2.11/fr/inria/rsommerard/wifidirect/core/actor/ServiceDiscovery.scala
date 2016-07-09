package fr.inria.rsommerard.wifidirect.core.actor

import akka.actor.{Actor, ActorRef}
import fr.inria.rsommerard.wifidirect.core.message.{Hello, Location, LocationList, UI}

class ServiceDiscovery extends Actor {

  var locations: Map[ActorRef, Location] = Map()

  val master = context.actorSelection("akka.tcp://MasterSystem@10.32.0.42:2552/user/master")

  override def preStart() {
    master ! Hello("ServiceDiscovery")
  }

  override def receive: Receive = {
    case h: Hello => hello(h)
    case UI => processUI()
    case l: Location => processLocation(l)
    case u: Any => dealWithUnknown("receive", u.getClass.getSimpleName)
  }

  private def processUI(): Unit = {
    sender ! LocationList(locations)
  }

  private def processLocation(location: Location): Unit = {
    println(s"Received Location from ${sender.path.address.host.get}")
    locations += (sender -> location)
    val neighbors: Map[ActorRef, Location] = locations.filter(l => areInRange(location, l._2))
    val locationList = new LocationList(neighbors)
    neighbors.keys.foreach(a => a ! locationList)
  }

  private def hello(h: Hello): Unit = {
    println(s"Received Hello(${h.msg}) from ${sender.path.address.host.get}")
  }

  private def areInRange(l1: Location, l2: Location): Boolean = {
    // current range 200m, it seems that WiFi-direct has a range up to 250m
    if (l1.distance(l2) <= 200) true
    else false
  }

  private def dealWithUnknown(state: String, name: String): Unit = println(s"State $state => Received unknown message ($name)")
}
