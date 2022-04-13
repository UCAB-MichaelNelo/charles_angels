package org.charles.angels.houses.http

import cats.implicits.*
import fs2.Stream
import fs2.Pipe
import org.charles.angels.houses.notifications.Notification
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import org.http4s.circe.*
import org.http4s.circe.CirceEntityDecoder.*
import io.circe.generic.semiauto
import io.circe.syntax.*
import io.circe.Encoder
import cats.effect.kernel.Async
import org.charles.angels.houses.shared.Executor
import org.charles.angels.houses.http.models.viewmodels.NotificationViewModel
import io.circe.Json
import org.http4s.Headers

class NotificationRoutes[F[_]: Async: Executor] extends ServerRoutes[F] {

  given Encoder[Notification] =
    Encoder
      .forProduct2[NotificationViewModel, String, Json]("type", "body") {
        viewModel => (viewModel.`type`, viewModel.body)
      }
      .contramap(NotificationViewModel.apply)

  object sse {
    def process[A: Encoder]: Pipe[F, A, String] = stream =>
      stream.map(data => s"data: ${data.asJson.noSpaces} \n\n")
  }

  def routes = HttpRoutes.of[F] { case GET -> Root =>
    Ok(
      Executor[F].stream.through(sse.process),
      ("Content-Type", "text/event-stream"),
      ("Cache-Control", "no-store")
    )
  }
}
