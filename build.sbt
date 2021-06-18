enablePlugins(SparkPlugin)

name := "spark-google-spreadsheets"

organization := "io.github.kostjas.spark-google-spreadsheets"

homepage := Some(url("https://github.com/kostjas/spark-google-spreadsheets"))

organizationHomepage := Some(url("https://github.com/kostjas"))

description := "Google Spreadsheets datasource for SparkSQL and DataFrames."

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

scalaVersion := "2.12.13"

version := "0.10.0-SNAPSHOT"

sparkVersion := "3.1.1"

sparkComponents := Seq("core", "sql")

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.30" % "provided",
  "org.scalatest" %% "scalatest" % "3.2.9" % "test",
  ("com.google.api-client" % "google-api-client" % "1.31.5").
    exclude("com.google.guava", "guava-jdk5")
    .exclude("com.google.guava", "guava:30.1.1-android"),
  "com.google.apis" % "google-api-services-sheets" % "v4-rev20210322-1.31.0"
)

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library" % scalaVersion.value % "compile",
  "javax.servlet" % "javax.servlet-api" % "4.0.1" % "compile"
)

resolvers ++= Seq(Resolver.mavenLocal, Resolver.sonatypeRepo("staging"))

/**
 * release settings
 */
publishMavenStyle := true

pgpKeyRing := Some(file("~/.gnupg/pubring.kbx"))

releasePublishArtifactsAction := PgpKeys.publishSigned.value

publishArtifact in Test := false

// Remove all additional repository other than Maven Central from POM
pomIncludeRepository := { _ => false }

credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials")

// For all Sonatype accounts created on or after February 2021
sonatypeCredentialHost := "s01.oss.sonatype.org"

sonatypeProfileName := "io.github.kostjas"

publishTo := sonatypePublishToBundle.value

scmInfo := Some(
  ScmInfo(
    url("https://github.com/kostjas/spark-google-spreadsheets"),
    "git@github.com:kostjas/spark-google-spreadsheets.git"
  )
)

developers := List(
  Developer(
    id    = "kostjas",
    name  = "Kostya Spitsyn",
    email = "konstantin.spitsyn@gmail.com",
    url   = url("https://github.com/kostjas/")
  )
)

// Skip tests during assembly
test in assembly := {}

releaseCrossBuild := false

import ReleaseTransformations._

// Add publishing to spark packages as another step.
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges
)

releaseVcsSign := true