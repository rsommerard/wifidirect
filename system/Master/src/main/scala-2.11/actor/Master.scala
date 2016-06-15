package actor

import akka.actor.Actor
import message.Hello

class Master extends Actor {

  override def receive: Receive = {
    case Hello =>
      println("Received Hello")

    case _ =>
      println("Received unknown message")
  }
}