import java.io.{File, PrintWriter}

object Main extends App {

  val dataFilePath = "/home/romain/Lab/wifidirect/misc/TraceParser/data/Random.txt"
  val destinationFile = "/home/romain/Lab/wifidirect/misc/TraceParser/data/RandomScenarii.scala"

  val brutLines = scala.io.Source.fromFile(dataFilePath).mkString

  val splittedLines: List[String] = brutLines.split('\n').filterNot(l => l.isEmpty).toList
  val head: String = splittedLines.head

  val lines: List[String] = splittedLines.filterNot(l => l == head)
  val id: Set[String] = lines.map(l => l.split(' ')(0)).toSet

  var content: String = ""

  content += "package fr.inria.rsommerard.wifidirect.core\n\n"
  content += "import fr.inria.rsommerard.wifidirect.core.message.{Location, Scenario}\n\n"
  content += "object RandomScenarii {\n\n"
  content += "  val getDefaultScenarii: List[Scenario] = {"

  var ndx = 1
  for (i <- id) {
    content += s"\n    val scnr$ndx = Scenario(List(\n"
    val sel = lines.filter(l => l.split(' ')(0) == i)
    sel.foreach(s => {
      if (s == sel.last) content += s"      Location(${s.split(' ')(3)}, ${s.split(' ')(4)})))\n"
      else content += s"      Location(${s.split(' ')(3)}, ${s.split(' ')(4)}),\n"
    })
    ndx += 1
  }

  val lastIndex = ndx - 1

  content += "\n    List("

  ndx = 1
  id.foreach(i => {
    if (ndx == lastIndex) content += s"scnr$ndx)\n"
    else content += s"scnr$ndx, "
    ndx += 1
  })

  content += "  }\n}\n"

  val printWriter = new PrintWriter(new File(destinationFile))
  try {
    printWriter.write(content)
  } finally {
    printWriter.close()
  }
}
