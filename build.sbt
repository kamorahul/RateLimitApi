name := "API_RateLimiting_Demo"

version := "1.0"

scalaVersion := "2.11.8"


val akkaV       = "2.4.11"

val akkaDependencies = Seq(

  "com.typesafe.akka" %% "akka-actor" % akkaV withSources() withJavadoc(),
  "com.typesafe.akka" %% "akka-stream" % akkaV withSources() withJavadoc(),
  "com.typesafe.akka" %% "akka-agent" % akkaV withSources() withJavadoc(),
  "com.typesafe.akka" %% "akka-http-core" % akkaV  withSources() withJavadoc(),
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV withSources() withJavadoc()
)


val databaseDependencies = Seq(
  "org.reactivemongo" %% "reactivemongo" % "0.11.14" withSources() withJavadoc()
)

libraryDependencies ++= akkaDependencies ++ databaseDependencies
libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.12"