package org.charles.angels.houses.http

import cats.syntax.all.*
import cats.effect.kernel.Async
import cats.effect.kernel.Concurrent
import com.comcast.ip4s.ipv4
import com.comcast.ip4s.port
import org.http4s.server.Router
import cats.Parallel
import cats.effect.kernel.Async
import org.charles.angels.houses.shared.Executor
import org.http4s.ember.server.EmberServerBuilder
import scala.concurrent.duration.Duration
import cats.effect.kernel.Resource
import cats.effect.std.Console
import org.http4s.server.middleware.CORS

class HttpServer[F[_]: Async: Console: Concurrent: Parallel](using
    Executor[F]
) {
  def server =
    EmberServerBuilder
      .default[F]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"3500")
      .withHttpWebSocketApp(_ =>
        (
          CORS(Router[F](
            "/houses" -> HouseRoutes[F].service,
            "/contacts" -> ContactRoutes[F].service,
            "/schedules" -> ScheduleRoutes[F].service,
            "/children" -> ChildRoutes[F].service,
            "/notifications" -> NotificationRoutes[F].service
          )).orNotFound
        )
      )
      .withIdleTimeout(Duration.Inf)
      .build >> Resource.eval(Async[F].never)
}
