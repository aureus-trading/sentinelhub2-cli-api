name := """polarity-cosmo-api"""
organization := "polarity"

ThisBuild / scalaVersion     := "2.13.6"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "polarity"

import com.typesafe.sbt.packager.docker.DockerChmodType
import com.typesafe.sbt.packager.docker.DockerPermissionStrategy
dockerChmodType := DockerChmodType.UserGroupWriteExecute
dockerPermissionStrategy := DockerPermissionStrategy.CopyChown
Docker / maintainer := "m.sivak@polarity.exchange.com" // TODO: set your info here
Docker / packageName := "cosmos-api-cli"
Docker / version := sys.env.getOrElse("BUILD_NUMBER", "0")
Docker / daemonUserUid  := None
Docker / daemonUser := "daemon"
dockerExposedPorts := Seq(9000)
dockerBaseImage := "java-alpine-openjdk8-jre"
dockerUpdateLatest := true
version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
scalaVersion := "2.13.6"
libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "polarity.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "polarity.binders._"
