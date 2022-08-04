package org.charles.angels.houses.http

import cats.implicits.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityDecoder.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import cats.effect.kernel.Async
import cats.Parallel
import cats.effect.Concurrent
import org.charles.angels.houses.shared.Executor
import org.http4s.HttpRoutes
import org.charles.angels.houses.http.models.forms.LoginForm
import org.charles.angels.houses.compiler.CompilerDSL
import org.http4s.Status
import org.http4s.Response
import org.http4s.ResponseCookie
import java.nio.charset.StandardCharsets
import org.http4s.Request
import org.http4s.HttpDate
import scala.concurrent.duration.FiniteDuration


class AuthRoutes[F[_]: Async: Parallel: Concurrent: Executor]
    extends ServerRoutes[F] {

  private def loggedInResponse(req: Request[F]) =
     req.cookies.find(_.name == "authenticated").as(Ok())
  
  def routes = HttpRoutes.of[F] {
    case req @ POST -> Root / "login" => for
        form <- req.as[LoginForm]
        authKey <- CompilerDSL.login(form.username, form.password).run
        response <- Ok()
        epoch <- Async[F].delay { System.currentTimeMillis }
    yield response.addCookie(ResponseCookie("authenticated", authKey, path = "/".some, expires = HttpDate.MaxValue.some))
    case req @ GET -> Root => loggedInResponse(req) getOrElse Response(Status.Unauthorized).pure[F]
    case req @ POST -> Root / "logout" => 
      loggedInResponse(req)
        .nested
        .map(_.addCookie(ResponseCookie("authenticated", "", path = "/".some, expires = HttpDate.Epoch.some, maxAge = 0L.some)))
        .value getOrElse Response(Status.Unauthorized).pure[F]
  }
}