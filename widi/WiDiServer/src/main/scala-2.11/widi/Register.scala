package widi

class Register() {

  var emulators: List[Emulator] = List()

  def addEmulator(emulator: Emulator) = {
    emulators ::= emulator
  }
}
