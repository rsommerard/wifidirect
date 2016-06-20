
lazy val root = (project in file(".")).
  enablePlugins(JavaAppPackaging).
  settings(
    name := "WiFiDirect-UI",
    version := "1.0",
    scalaVersion := "2.11.8",
    libraryDependencies := Seq(
      "io.spray" %% "spray-routing" % "1.3.3",
      "io.spray" %% "spray-can" % "1.3.3",
      "org.json4s" %% "json4s-jackson" % "3.3.0",
      "com.typesafe.akka" %% "akka-actor" % "2.4.2",
      "com.typesafe.akka" %% "akka-remote" % "2.4.2"
    )
  )
