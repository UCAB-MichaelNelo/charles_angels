val scala3Version = "3.1.1"
val Http4sVersion = "0.23.10"
val CirceVersion = "0.14.1"

ThisBuild / scalaVersion := scala3Version

lazy val root = project
  .in(file("."))
  .settings(
    name := "Charles Angels' Houses",
    version := "0.1.0"
  )
  .aggregate(houses, server)

lazy val houses = project
  .in(file("houses"))
  .settings(
    name := "Charles Angels' Houses Management",
    scalacOptions += "-source:future",
    scalacOptions += "-Ykind-projector:underscores",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-kernel" % "2.7.0",
      "org.typelevel" %% "cats-core" % "2.7.0",
      "org.typelevel" %% "cats-free" % "2.7.0",
      "dev.optics" %% "monocle-core" % "3.1.0",
      "dev.optics" %% "monocle-macro" % "3.1.0",
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.scalameta" %% "munit-scalacheck" % "0.7.29" % Test
    )
  )
lazy val server = project
  .in(file("server"))
  .settings(
    name := "Charles Angels' Houses Server",
    scalaVersion := scala3Version,
    scalacOptions += "-source:future",
    scalacOptions += "-Ykind-projector:underscores",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-ember-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "org.typelevel" %% "cats-kernel" % "2.7.0",
      "org.typelevel" %% "cats-core" % "2.7.0",
      "org.typelevel" %% "cats-free" % "2.7.0",
      "co.fs2" %% "fs2-core" % "3.2.5",
      "co.fs2" %% "fs2-io" % "3.2.5",
      "org.typelevel" %% "cats-effect" % "3.3.7",
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.scalameta" %% "munit-scalacheck" % "0.7.29" % Test
    )
  )
  .dependsOn(houses)
