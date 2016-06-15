import actor.Node
import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import message.Hello

object Main extends App {

  val system = ActorSystem("NodeSystem", ConfigFactory.load("node"))

  val node = system.actorOf(Props[Node], "node")

  println("Node actor started...")

  node ! Hello
}
