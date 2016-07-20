case class Toto(b: Int)

case class Tata(c: Int) {
  var a: String = _
}

var t: Tata = Tata(4)
t.a