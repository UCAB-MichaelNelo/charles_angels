package org.charles.angels.houses.http

import cats.implicits.*
import org.http4s.HttpRoutes
import org.charles.angels.houses.compiler.CompilerDSL
import org.http4s.circe.*
import org.http4s.circe.CirceEntityDecoder.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import cats.effect.kernel.Async
import cats.Parallel
import cats.effect.kernel.Concurrent
import org.charles.angels.houses.shared.Executor
import org.http4s.Request
import org.http4s.StaticFile

class StaticFilesRoutes[F[_]: Async: Parallel: Concurrent: Executor]
    extends ServerRoutes[F] {

  private def static(file: String, request: Request[F]) =
    StaticFile.fromResource("public/" + file, Some(request), true).getOrElseF(NotFound())

  def routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ GET -> "client" /: _ =>
      static(s"dist/index.html", req)
    case req @ GET -> "assets" /: path =>
      static(s"dist/assets/$path", req)
    case req @ GET -> "css" /: path =>
      static(s"css/$path", req)
    case req @ GET -> "js" /: path =>
      static(s"js/$path", req)
  }

}
