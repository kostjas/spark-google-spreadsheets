import ReleaseTransformations._

enablePlugins(SparkPlugin)

ThisBuild / name := "spark-google-spreadsheets"
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / organization := "io.github.riskidentdms"
ThisBuild / homepage := Some(url("https://github.com/riskidentdms/spark-google-spreadsheets"))
ThisBuild / organizationHomepage := Some(url("https://github.com/riskidentdms"))
ThisBuild / description := "Google Spreadsheets datasource for SparkSQL and DataFrames."
ThisBuild / licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / scalaVersion := "2.12.15"

sparkVersion := "3.3.0"

sparkComponents := Seq("core", "sql")

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.36" % "provided",
  "org.scalatest" %% "scalatest" % "3.2.13" % "test",
  "com.google.apis" % "google-api-services-sheets" % "v4-rev20220620-2.0.0" excludeAll(
    ExclusionRule("com.google.guava", "guava")
  ),
  "com.google.auth" % "google-auth-library-oauth2-http" % "1.10.0" excludeAll(
    ExclusionRule("com.google.guava", "guava")
  ),
  "org.scala-lang" % "scala-library" % scalaVersion.value % "compile",
  "javax.servlet" % "javax.servlet-api" % "4.0.1" % "compile",
  "com.google.guava" % "guava" % "31.1-jre"
)

ThisBuild / resolvers ++= Seq(Resolver.mavenLocal) ++ Resolver.sonatypeOssRepos("staging")

/**
 * release settings
 */
ThisBuild / publishMavenStyle := true

ThisBuild / pgpKeyRing := Some(file("~/.gnupg/pubring.kbx"))

ThisBuild / releasePublishArtifactsAction := PgpKeys.publishSigned.value

ThisBuild / Test / publishArtifact := false

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }

ThisBuild / credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials")

// For all Sonatype accounts created on or after February 2021
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

ThisBuild / sonatypeProfileName := "io.github.riskidentdms"

ThisBuild / publishTo := sonatypePublishToBundle.value

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/riskidentdms/spark-google-spreadsheets"),
    "git@github.com:riskidentdms/spark-google-spreadsheets.git"
  )
)

developers := List(
  Developer(
    id    = "ri-kostya",
    name  = "Kostya Spitsyn",
    email = "kostyantyn@riskident.com",
    url   = url("https://github.com/ri-kostya/")
  )
)

// Skip tests during assembly
ThisBuild / assembly / test := {}

ThisBuild / releaseCrossBuild := false

// Add publishing to spark packages as another step.
ThisBuild / releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("publishSigned"),
  releaseStepCommand("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

ThisBuild / releaseVcsSign := true