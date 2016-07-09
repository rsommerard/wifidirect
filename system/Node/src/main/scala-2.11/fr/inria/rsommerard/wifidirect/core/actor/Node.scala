package fr.inria.rsommerard.wifidirect.core.actor

import akka.actor.{Actor, ActorRef}
import fr.inria.rsommerard.wifidirect.core.Emulator
import fr.inria.rsommerard.wifidirect.core.message._

import scala.util.Random

class Node extends Actor {

  val testingLocation: List[Location] = List(Location(50.605611, 3.138421), Location(50.605747, 3.141081),
    Location(50.606755, 3.1446), Location(50.605475, 3.147261), Location(50.605039, 3.149493))

  val master = context.actorSelection("akka.tcp://MasterSystem@10.32.0.42:2552/user/master")
  val serviceDiscovery = context.actorSelection("akka.tcp://ServiceDiscoverySystem@10.32.0.43:2552/user/servicediscovery")
  var ownLocation: Location = _
  var neighbors: Map[ActorRef, Location] = _
  var tickValue: Int = -1

  override def preStart() {
    master ! Hello("Node")
  }

  override def receive: Receive = {
    case h: Hello => hello(h)
    case Ready => sendReady()
    case t: Tick => tick(t)
    case ll: LocationList => processNeighbors(ll)
    case u: Any => dealWithUnknown("receive", u.getClass.getSimpleName)
  }

  private def tick(t: Tick): Unit = {
    tickValue = t.value
    updateLocation()
  }

  private def updateLocation(): Unit = {
    println(s"Received Tick from ${sender.path.address.host.get}")
    val rand = new Random()
    ownLocation = testingLocation(rand.nextInt(testingLocation.size))
    Emulator.setGPSLocation(ownLocation.lat, ownLocation.lon)
    serviceDiscovery ! ownLocation
  }

  private def processNeighbors(locationList: LocationList): Unit = {
    neighbors = locationList.locations.filter(l => l._1 != self)
    print(s"${neighbors.size} neighbors: $neighbors")
  }

  private def sendReady(): Unit = {
    master ! Ready
  }

  private def hello(h: Hello): Unit = println(s"Received Hello(${h.msg}) from ${sender.path.address.host.get}")

  private def dealWithUnknown(state: String, name: String): Unit = println(s"State $state => Received unknown message ($name)")
}
