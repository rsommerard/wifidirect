package fr.inria.rsommerard.wifidirect.core.actor

import akka.actor.{Actor, ActorRef}
import fr.inria.rsommerard.wifidirect.core.Emulator
import fr.inria.rsommerard.wifidirect.core.message._

import scala.util.Random

class Node extends Actor {

  val testingLocation: List[Location] = List(Location(50.605611, 3.138421), Location(50.605747, 3.141081),
    Location(50.606755, 3.1446), Location(50.605475, 3.147261), Location(50.605039, 3.149493))

  val master = context.actorSelection("akka.tcp://MasterSystem@10.32.0.42:2552/user/master")
  var ownLocation: Location = _
  var neighbors: Map[ActorRef, Location] = _
  var tickValue: Int = -1

  override def preStart() {
    master ! Hello
  }

  override def receive: Receive = initializing

  private def initializing: Receive = {
    case Hello => hello()
    case Ready => sendReady()
    case u: Any => dealWithUnknown("initializing", u.getClass.getSimpleName)
  }

  private def processing: Receive = {
    case tick: Tick => {
      tickValue = tick.value
      updateLocation()
    }
    case ll: LocationList => processNeighbors(ll)
    case u: Any => dealWithUnknown("processing", u.getClass.getSimpleName)
  }

  private def updateLocation(): Unit = {
    println(s"Received Tick from ${sender.path.address.host.get}")
    val rand = new Random()
    ownLocation = testingLocation(rand.nextInt(testingLocation.size))
    Emulator.setGPSLocation(ownLocation.lat, ownLocation.lon)
    master ! ownLocation
  }

  private def processNeighbors(locationList: LocationList): Unit = {
    neighbors = Map()
    neighbors = locationList.locations.filter(l => l._1 != self && isInRange(l._2))
    print(s"${neighbors.size} neighbors: $neighbors")
  }

  private def sendReady(): Unit = {
    context.become(processing)
    master ! Ready
  }

  private def hello(): Unit = println(s"Received Hello from master")

  private def dealWithUnknown(state: String, name: String): Unit = println(s"State $state => Received unknown message ($name)")

  private def isInRange(location: Location): Boolean = {
    // current range 200m, it seems that WiFi-direct has a range up to 250m
    if (ownLocation.distance(location) <= 200) true
    else false
  }
}
