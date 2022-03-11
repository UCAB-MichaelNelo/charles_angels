package org.charles.angels.houses.server

import cats.syntax.all.*
import cats.effect.IOApp
import cats.effect.IO
import cats.effect.ExitCode
import cats.effect.std.Console

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    Console[IO].println("Running main") >>
      ExitCode.Success.pure
}
