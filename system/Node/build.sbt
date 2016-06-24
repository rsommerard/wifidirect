
lazy val root = (project in file(".")).
  enablePlugins(JavaAppPackaging).
  settings(
    name := "WiFiDirect-Node",
    version := "1.0",
    scalaVersion := "2.11.8",
    libraryDependencies := Seq(
      "org.scala-lang" % "scala-reflect" % "2.11.8",
      "org.scala-lang" % "scala-library" % "2.11.8",
      "com.typesafe.akka" %% "akka-actor" % "2.4.2",
      "com.typesafe.akka" %% "akka-remote" % "2.4.2",
      "com.typesafe.play" %% "play-json" % "2.4.6",
      "commons-net" % "commons-net" % "2.0"
    )
  )
