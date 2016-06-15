
lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(name := "WiFiDirect-Master",
    version := "1.0",
    scalaVersion := "2.11.8",
    libraryDependencies := Seq(
      "org.scala-lang" % "scala-library" % "2.11.8",
      "com.typesafe.akka" %% "akka-actor" % "2.4.2",
      "com.typesafe.akka" %% "akka-remote" % "2.4.2"
    )
  )