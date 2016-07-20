package fr.inria.rsommerard.wifidirect.core.actor

import akka.actor.{Actor, ActorRef}
import fr.inria.rsommerard.wifidirect.core.message._
import fr.inria.rsommerard.wifidirect.core.widi.Emulator

import scala.util.Random

class Node(val weaveIp: String, val emulator: Emulator) extends Actor {

  val testingLocations: List[Location] = List(Location(50.605611, 3.138421), Location(50.605747, 3.141081),
    Location(50.606755, 3.1446), Location(50.605475, 3.147261), Location(50.605039, 3.149493))

  val master = context.actorSelection("akka.tcp://MasterSystem@10.32.0.42:2552/user/master")
  val serviceDiscovery = context.actorSelection("akka.tcp://ServiceDiscoverySystem@10.32.0.43:2552/user/servicediscovery")

  var neighbors: List[Neighbor] = List()

  override def preStart() {
    master ! Hello("Node")
    serviceDiscovery ! IP(weaveIp)
  }

  override def receive: Receive = {
    case h: Hello => hello(h)
    case Ready => ready()
    case Tick => tick()
    case d: Discoverable => discoverable(d)
    case c: Connect => connect(c)
    case d: Disconnect => disconnect(d)
    case s: Service => service(s)
    case s: Services => services(s)
    case r: Request => request(r)
    case nghbrs: Neighbors => neighbors(nghbrs)
    case u: Any => dealWithUnknown("receive", u.getClass.getSimpleName)
  }

  private def service(s: Service): Unit = {
    //println(s"Received Service: $s")

    serviceDiscovery ! s
  }

  private def services(s: Services): Unit = {
    //println(s"Received Services: $s")

    emulator.updateServices(s.values)
  }

  private def tick(): Unit = {
    //println(s"Received Tick")
    updateLocation()
  }

  private def disconnect(d: Disconnect): Unit = {
    println(s"Received Disconnect: $d")

    if (d.weaveIp != weaveIp) {
      serviceDiscovery ! d
    }
  }

  private def connect(c: Connect): Unit = {
    println(s"Received Connect: $c")

    c.weaveIpTo match {
      case `weaveIp` => emulator.connectExt(c.weaveIpFrom, c.groupOwnerIp)
      case _ => serviceDiscovery ! c
    }
  }

  private def discoverable(d: Discoverable): Unit = {
    //println(s"Received Discoverable: $d")

    serviceDiscovery ! d
  }

  private def request(r: Request): Unit = {
    r.value match {
      case "Neighbors" => serviceDiscovery ! Neighbors
    }
  }

  private def updateLocation(): Unit = {
    val rand = new Random()
    val ownLocation: Location = testingLocations(rand.nextInt(testingLocations.size))
    Emulator.setGPSLocation(ownLocation.lat, ownLocation.lon)
    serviceDiscovery ! ownLocation
  }

  private def neighbors(nghbrs: Neighbors): Unit = {
    neighbors = nghbrs.values.filter(n => n.weaveIp != emulator.weaveIp)
    //print(s"${neighbors.size} neighbors: $neighbors")
    emulator.updateNeighbors(neighbors)
    emulator.sendPeersChangedIntent()
  }

  private def ready(): Unit = {
    master ! Ready
  }

  private def hello(h: Hello): Unit = println(s"Received Hello: $h")

  private def dealWithUnknown(state: String, name: String): Unit = println(s"State $state => Received unknown message ($name)")
}
