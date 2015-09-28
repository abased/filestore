// Comment to get more information during initialization
logLevel := Level.Info

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.6")

// Support for packaging: RPM, zip, etc
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.8.0-M1")
