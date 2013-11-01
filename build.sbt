name := "Crawler"

version := "1.0"

scalaVersion := "2.10.3"

libraryDependencies += "org.htmlparser" % "htmlparser" % "2.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3-M1"

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.3-M1"

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.3-M1" % "test"

