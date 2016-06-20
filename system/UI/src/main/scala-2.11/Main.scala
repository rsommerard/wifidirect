import akka.actor.ActorSystem
import spray.http.StatusCodes
import spray.routing.SimpleRoutingApp
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import fr.inria.rsommerard.wifidirect.core.message.{Location, LocationList, UI}

import scala.concurrent.duration._
import org.json4s._
import jackson.Serialization.write
import org.json4s.jackson.Serialization

import scala.util.{Failure, Success}

object Main extends App with SimpleRoutingApp {
  implicit val system = ActorSystem("UISystem", ConfigFactory.load("ui"))

  import system.dispatcher
  implicit val timeout = Timeout(3.second)

  val master = system.actorSelection("akka.tcp://MasterSystem@10.32.0.42:2552/user/master")

  lazy val statusRoute = {
    path("status") {
      get {
        complete(StatusCodes.OK)
      }
    }
  }

  lazy val positionsRoute = {
    path("locations") {
      get {
        onComplete(master ? UI) {
          case Success(value) =>
            val locations: Set[Location] = value.asInstanceOf[LocationList].locations.values.toSet
            println(locations)

            implicit val formats = Serialization.formats(NoTypeHints)
            complete(write(locations))

          case Failure(ex) =>
            complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
        }
      }
    }
  }

  startServer(interface = "localhost", port = 8080) {
    statusRoute ~ positionsRoute
  }
}
