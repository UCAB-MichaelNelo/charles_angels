val scala3Version = "3.1.1"
val Http4sVersion = "0.23.10"
val CirceVersion = "0.14.1"
val ScalaJsReactVer = "2.1.0"

ThisBuild / scalaVersion := scala3Version
ThisBuild / fork / run := true

lazy val root = project
  .in(file("."))
  .settings(
    name := "Charles Angels' Houses",
    version := "1.0",
  )
  .aggregate(houses, people, server)

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

lazy val people = project
  .in(file("people"))
  .settings(
    name := "Charles Angels' People Management",
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
  .enablePlugins(JavaAppPackaging)
  .settings(
    scriptClasspath := Seq("*"),
    name := "Charles Angels' Houses Server",
    scalacOptions += "-source:future",
    scalacOptions += "-Ykind-projector:underscores",
    version := "1.0",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-ember-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-parser" % CirceVersion,
      "io.github.vigoo" %% "prox-fs2-3" % "0.7.3",
      "org.typelevel" %% "cats-kernel" % "2.7.0",
      "org.typelevel" %% "cats-core" % "2.7.0",
      "org.typelevel" %% "cats-free" % "2.7.0",
      "co.fs2" %% "fs2-core" % "3.2.5",
      "co.fs2" %% "fs2-io" % "3.2.5",
      "org.typelevel" %% "cats-effect" % "3.3.7",
      "ch.qos.logback" % "logback-classic" % "1.2.10",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC1",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC1",
      "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC1",
      "org.postgresql" % "postgresql" % "42.2.16",
      "org.typelevel" %% "shapeless3-deriving" % "3.0.1",
      "com.lihaoyi" %% "scalatags" % "0.11.1",
      "is.cir" %% "ciris" % "2.3.3",
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.1",
      "io.github.jmcardon" %% "tsec-http4s" % "0.4.0",
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.scalameta" %% "munit-scalacheck" % "0.7.29" % Test
    )
  )
  .dependsOn(houses, people)

// lazy val client = project
// .in(file("client"))
// .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
// .settings(
// name := "Charles Angels' Client",
// scalaVersion := "2.13.8",
// scalaJSUseMainModuleInitializer := true,
// libraryDependencies ++= Seq(
// "com.github.japgolly.scalacss" %%% "core" % "1.0.0",
// "com.github.japgolly.scalacss" %%% "ext-react" % "1.0.0",
// // Optionally include scalajs-react Callback classes
// // (Note: these need to come before "core-bundle-cats_effect")
// "com.github.japgolly.scalajs-react" %%% "callback" % ScalaJsReactVer,
// "com.github.japgolly.scalajs-react" %%% "callback-ext-cats" % ScalaJsReactVer,
// "com.github.japgolly.scalajs-react" %%% "callback-ext-cats_effect" % ScalaJsReactVer,
//
// // Mandatory
// "com.github.japgolly.scalajs-react" %%% "core-bundle-cats_effect" % ScalaJsReactVer,
// "io.kinoplan" %%% "scalajs-react-material-ui-core" % "0.3.1",
// "io.kinoplan" %%% "scalajs-react-material-ui-icons" % "0.3.1",
// "io.kinoplan" %%% "scalajs-react-material-ui-lab" % "0.3.1"
// ),
// Compile / npmDependencies ++= Seq(
// "react" -> "*",
// "react-dom" -> "*",
// "@material-ui/core" -> "*",
// "@material-ui/icons" -> "*",
// "@material-ui/lab" -> "*"
// )
// )
