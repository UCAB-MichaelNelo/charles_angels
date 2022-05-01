package org.charles.angels.houses.notifications.fs2

import cats.~>
import cats.implicits.*
import fs2.concurrent.Topic
import org.charles.angels.houses.notifications.Notification
import org.charles.angels.houses.notifications.NotificationAction
import cats.Monad
import org.charles.angels.houses.errors.ServerError
import org.charles.angels.houses.notifications.Make
import cats.effect.kernel.Resource
import cats.effect.std.Queue
import cats.effect.kernel.Async
import cats.Functor
import cats.MonadError

class Fs2NotificationExecutor[F[_]: [F[_]] =>> MonadError[F, Throwable]](
    topic: Topic[F, Notification]
) extends (NotificationAction ~> F) {
  def apply[A](action: NotificationAction[A]) = action match {
    case NotificationAction.Notify(notification) => topic.publish1(notification).void.attempt
  }
}

class Fs2;

given [F[_]: Async: Functor]: Make[F, Fs2] with
  def make(token: Fs2) = for
    queue <- Resource.eval { Queue.unbounded[F, Notification] }
    topic <- Resource.make[F, Topic[F, Notification]](Topic[F, Notification]) {
      topic =>
        topic
          .publish1(Notification.NotificationsStopped)
          .map(
            _.leftMap(_ => ServerError.NotificationStreamClosed).void
          )
          .rethrow >> topic.close.void
    }
  yield (topic.subscribe(1), Fs2NotificationExecutor(topic))
