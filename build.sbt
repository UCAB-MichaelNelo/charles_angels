val scala3Version = "3.1.1"

ThisBuild / scalaVersion := scala3Version

lazy val root = project
  .in(file("."))
  .settings(
    name := "Charles Angels' Houses",
    version := "0.1.0"
  )
  .aggregate(houses)

lazy val houses = project
  .in(file("houses"))
  .settings(
    name := "Charles Angels' Houses Management",
    scalacOptions += "-source:future",
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
