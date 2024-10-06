resolvers += Resolver.sonatypeRepo("staging")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.4")

addSbtPlugin("com.github.alonsodomin" % "sbt-spark" % "0.6.0")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.13")

addSbtPlugin("com.github.sbt" % "sbt-release" % "1.1.0")

addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.9.3")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.2")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.13")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.3")
