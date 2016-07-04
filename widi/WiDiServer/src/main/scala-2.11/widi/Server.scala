package widi

import scala.io.Source

object Server {

  def start(): Unit = {
    val register = new Register()

    val wiDiOne = new Emulator("emulator-5554", "10.0.2.2", 54412, false, register)
    val wiDiTwo = new Emulator("emulator-5556", "10.0.2.2", 54421, false, register)

    register.addEmulator(wiDiOne)
    register.addEmulator(wiDiTwo)

    wiDiOne.start()
    wiDiTwo.start()

    for (ln <- Source.stdin.getLines()) {
      if (ln.toLowerCase == "q") {
        System.exit(0)
      }
    }
  }
}
