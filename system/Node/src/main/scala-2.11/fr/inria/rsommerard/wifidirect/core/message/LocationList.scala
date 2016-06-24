package fr.inria.rsommerard.wifidirect.core.message

import akka.actor.ActorRef

case class LocationList(locations: Map[ActorRef, Location]) extends Message
