package fr.inria.rsommerard.wifidirect.core.actor

import akka.actor.{Actor, ActorRef}
import fr.inria.rsommerard.wifidirect.core.message._

class ServiceDiscovery extends Actor {

  var locations: Map[ActorRef, Location] = Map()
  var ipNodes: Map[ActorRef, String] = Map()
  var discoverables: Map[ActorRef, Boolean] = Map()
  var services: Map[ActorRef, Service] = Map()

  val master = context.actorSelection("akka.tcp://MasterSystem@10.32.0.42:2552/user/master")

  override def preStart() {
    master ! Hello("ServiceDiscovery")
  }

  override def receive: Receive = {
    case h: Hello => hello(h)
    case i: IP => ip(i)
    case UI => ui()
    case c: Connect => connect(c)
    case d: Disconnect => disconnect(d)
    case l: Location => location(l)
    case d: Discoverable => discoverable(d)
    case s: Service => service(s)
    case Neighbors => neighbors()
    case u: Any => dealWithUnknown("receive", u.getClass.getSimpleName)
  }

  private def service(s: Service): Unit = {
    //println(s"Received Service from ${sender.path.address.host.get}")

    services += (sender -> s)

    sender ! Services(services.values.toList)
  }

  private def connect(c: Connect): Unit = {
    println(s"Received Connect(${c.weaveIpFrom}, ${c.weaveIpTo}, ${c.groupOwnerIp}) from ${sender.path.address.host.get}")

    val sel = ipNodes.filter(e => e._2 == c.weaveIpTo)

    if (sel.isEmpty) {
      println(s"No device found")
      return
    }

    sel.head._1 ! c
  }

  private def disconnect(d: Disconnect): Unit = {
    println(s"Received Disconnect(${d.weaveIp}) from ${sender.path.address.host.get}")

    val sel = ipNodes.filter(e => e._2 == d.weaveIp)

    if (sel.isEmpty) {
      println(s"No device found")
      return
    }

    sel.head._1 ! d
  }

  private def ip(i: IP): Unit = {
    //println(s"Received IP(${i.value}) from ${sender.path.address.host.get}")

    ipNodes += (sender -> i.value)
  }

  private def discoverable(d: Discoverable): Unit = {
    //println(s"Received Discoverable(${d.value}) from ${sender.path.address.host.get}")

    discoverables += (sender -> d.value)
    //println(s"discoverables: $discoverables")
  }

  private def ui(): Unit = {
    sender ! Locations(locations.values.toList)
  }

  private def neighbors(): Unit = {
    //println(s"Received Location from ${sender.path.address.host.get}")

    if (!discoverables(sender)) {
      println(s"${sender.path.address.host.get} is not discoverable")
      return
    }

    //println(s"locations: $locations")

    val loc: Option[Location] = locations.get(sender)

    if (loc.isEmpty) {
      println(s"No Location found for ${sender.path.address.host.get}")
      return
    }

    val selection: Map[ActorRef, Location] =
      locations.filter(l => discoverables.getOrElse(l._1, false) && areInRange(loc.get, l._2))

    var nghbrs: List[Neighbor] = List()
    for (a <- selection.keys) {
      val ip: String = ipNodes(a)

      nghbrs = Neighbor(ip) :: nghbrs
    }

    sender ! Neighbors(nghbrs)
  }


  private def location(loc: Location): Unit = {
    //println(s"Received Location from ${sender.path.address.host.get}")

    locations += (sender -> loc)
    //println(s"locations: $locations")
  }

  private def hello(h: Hello): Unit = {
    println(s"Received Hello(${h.name}) from ${sender.path.address.host.get}")
  }

  private def areInRange(l1: Location, l2: Location): Boolean = {
    val R: Double = 6371000 // m
    val dLat = (l2.lat - l1.lat).toRadians
    val dLon = (l2.lon - l1.lon).toRadians
    val lat1 = l1.lat.toRadians
    val lat2 = l2.lat.toRadians
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    val distance = R * c

    // current range 200m, it seems that WiFi-direct has a range up to 250m
    distance <= 200
  }

  private def dealWithUnknown(state: String, name: String): Unit = println(s"State $state => Received unknown message ($name)")
}
