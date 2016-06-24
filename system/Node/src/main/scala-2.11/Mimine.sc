case class Location(lat: Double, lon: Double) {
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

val loc1: Location = Location(50.605611, 3.138421) // 4 cantons
val loc2: Location = Location(50.605747, 3.141081)
val loc3: Location = Location(50.606755, 3.1446)
val loc4: Location = Location(50.605475, 3.147261)
val loc5: Location = Location(50.605039, 3.149493) // inria

loc1.distance(loc2)
loc1.distance(loc3)
loc1.distance(loc4)
loc1.distance(loc5)

loc2.distance(loc3)
loc2.distance(loc4)
loc2.distance(loc5)

loc3.distance(loc4)
loc3.distance(loc5)

loc4.distance(loc5)