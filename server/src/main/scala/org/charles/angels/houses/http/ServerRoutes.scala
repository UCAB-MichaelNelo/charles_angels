package org.charles.angels.houses.http

import cats.syntax.all.*
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes as Http4sRoutes
import org.charles.angels.houses.errors.ServerError
import cats.Monad
import cats.data.OptionT
import cats.MonadError
import org.http4s.AuthedRoutes

abstract class ServerRoutes[
    F[_]: ErrorHandler: [F[_]] =>> MonadError[F[_], Throwable]
] extends Http4sDsl[F] {
  def routes: Http4sRoutes[F]
  def service = Http4sRoutes[F] { req =>
    OptionT(routes(req).value.handleErrorWith {
      case e: ServerError => ErrorHandler[F].handle(e).map(_.some)
      case e              => ErrorHandler[F].unhandled(e).map(_.some)
    })
  }
  def authenticatedService = AuthedRoutes[Unit, F] { ctxReq =>
    OptionT(routes(ctxReq.req).value.handleErrorWith {
      case e: ServerError => ErrorHandler[F].handle(e).map(_.some)
      case e              => ErrorHandler[F].unhandled(e).map(_.some)
    })
  }
}
