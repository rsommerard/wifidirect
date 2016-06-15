package actor

import akka.actor.Actor
import message.Hello

class Node() extends Actor {

  val master = context.actorSelection("akka.tcp://MasterSystem@10.32.0.42:2552/user/master")

  override def receive: Receive = {
    case Hello =>
      println("Hello")
      master ! Hello

    case _ =>
      println("Received unknown message")
  }
}
