package org.charles.angels.houses.http

import cats.implicits.*
import cats.syntax.all.*
import cats.effect.kernel.Async
import cats.effect.kernel.Concurrent
import com.comcast.ip4s.ipv4
import com.comcast.ip4s.port
import com.comcast.ip4s.Port
import org.http4s.server.Router
import cats.Parallel
import cats.effect.kernel.Async
import org.charles.angels.houses.shared.Executor
import org.http4s.ember.server.EmberServerBuilder
import scala.concurrent.duration.Duration
import cats.effect.kernel.Resource
import cats.effect.std.Console
import org.http4s.server.middleware.CORS
import org.http4s.Request
import cats.data.Kleisli
import cats.data.OptionT
import org.charles.angels.houses.compiler.CompilerDSL
import org.http4s.Response
import org.http4s.Status
import fs2.Stream
import fs2.Chunk
import org.http4s.AuthedRoutes
import org.http4s.server.AuthMiddleware
import org.charles.angels.houses.errors.ServerError
import org.http4s.server.middleware.GZip

class HttpServer[F[_]: Async: Console: Concurrent: Parallel](using
    Executor[F]
) {
  private def authAdmin: Kleisli[F, Request[F], Either[Throwable, Unit]] = Kleisli { request =>
    for
      cookieOpt <- request.cookies.find(_.name == "authenticated").map(_.content).pure[F]
      either <- (
        cookieOpt.map(CompilerDSL.validateToken(_).run) getOrElse Async[F].raiseError(ServerError.UnauthorizedAccess)
      ).attempt
    yield either
  }

  private def errorResponse: AuthedRoutes[Throwable, F] = Kleisli { req => OptionT.liftF{
    req.context match {
      case e: ServerError => ErrorHandler[F].handle(e)
      case e => ErrorHandler[F].unhandled(e)
    }
  }}

  private val middleware: AuthMiddleware[F, Unit] = AuthMiddleware(authAdmin, errorResponse)

  def server(httpPort: Int) = for
    port <- Resource.eval { Async[F].fromEither(Port.fromInt(httpPort).toRight(Exception(s"Provided PORT $httpPort is invalid"))) }
    server <- EmberServerBuilder
      .default[F]
      .withHost(ipv4"0.0.0.0")
      .withPort(port)
      .withHttpWebSocketApp(_ =>
        (
          CORS(Router[F](
            "/api" -> Router[F](
              "/houses" -> middleware(HouseRoutes[F].authenticatedService),
              "/contacts" -> middleware(ContactRoutes[F].authenticatedService),
              "/children" -> middleware(ChildRoutes[F].authenticatedService),
              "/notifications" -> middleware(NotificationRoutes[F].authenticatedService),
              "/reports" -> GZip(middleware(ReportRoutes[F].authenticatedService)),
              "/auth" -> AuthRoutes[F].service
            ),
            "/" -> GZip(StaticFilesRoutes[F].service)
          )).orNotFound
        )
      )
      .withIdleTimeout(Duration.Inf)
      .build >> Resource.eval(Async[F].never)
  yield server
}
