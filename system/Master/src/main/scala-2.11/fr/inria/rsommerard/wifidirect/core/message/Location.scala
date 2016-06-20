package fr.inria.rsommerard.wifidirect.core.message

case class Location(lat: Double, lon: Double) extends Message {
  def distance(loc: Location): Double = {
    val R: Double = 6371000 // m
    val dLat = (loc.lat - lat).toRadians
    val dLon = (loc.lon - lon).toRadians
    val lat1 = lat.toRadians
    val lat2 = loc.lat.toRadians
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    R * c
  }
}