package fr.inria.rsommerard.wifidirect.core.actor

import java.util.Calendar

import akka.actor.{Actor, ActorRef}
import fr.inria.rsommerard.wifidirect.core.BerlinMODScenarii
import fr.inria.rsommerard.wifidirect.core.message._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class Master(val nbNodes: Int) extends Actor {

  var nodes: Set[ActorRef] = Set()
  val scenarii: List[Scenario] = BerlinMODScenarii.getDefaultScenarii
  var tickValue: Int = -1
  val serviceDiscovery = context.actorSelection("akka.tcp://ServiceDiscoverySystem@10.32.0.43:2552/user/servicediscovery")

  override def receive: Receive = initialize(0)

  def process(): Receive = {
    case Tick => tick()
    case u: Any => dealWithUnknown("process", u.getClass.getSimpleName)
  }

  def initialize(nbReadyNodes: Int): Receive = {
    case h: Hello => hello(h)
    case Ready => ready(nbReadyNodes)
    case u: Any => dealWithUnknown("initialize", u.getClass.getSimpleName)
  }

  private def hello(h: Hello): Unit = {
    println("#+#+#+#+#")
    println(s"#+#+#+#+# Received Hello(${h.name}) from ${sender.path.address.host.get}")
    println("#+#+#+#+#")

    sender ! Hello("Master")

    if (h.name == "Node") {
      sender ! scenarii(nodes.size)
      nodes += sender
    }
  }

  private def ready(nbReadyNodes: Int): Unit = {
    println("#+#+#+#+#")
    println(s"#+#+#+#+# Received Ready from ${sender.path.address.host.get}")
    println("#+#+#+#+#")

    val nbReady = nbReadyNodes + 1
    if (nbReady == nbNodes) {
      context.become(process())
      context.system.scheduler.schedule(0 seconds, 1 minute, self, Tick)
    } else {
      context.become(initialize(nbReady))
    }
  }

  private def tick(): Unit = {
    tickValue += 1

    println("#+#+#+#+#")
    println(s"#+#+#+#+# Tick: $tickValue [${Calendar.getInstance().getTime}]")
    println("#+#+#+#+#")

    nodes.foreach(n => n ! Tick(tickValue))
    serviceDiscovery ! Tick(tickValue)
  }

  private def dealWithUnknown(state: String, name: String): Unit = {
    println("#+#+#+#+#")
    println(s"#+#+#+#+# State $state => Received unknown message ($name)")
    println("#+#+#+#+#")
  }
}
