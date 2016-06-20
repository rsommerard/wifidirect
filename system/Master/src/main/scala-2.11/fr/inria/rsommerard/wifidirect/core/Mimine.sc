trait Momo

case class Toto(a: Int) extends Momo
case class Bobo(b: Int)

val toto = Toto(4)
toto

val bobo = Bobo(42)
bobo

toto.isInstanceOf[Momo]
bobo.isInstanceOf[Momo]
