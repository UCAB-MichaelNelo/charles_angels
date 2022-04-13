package org.charles.angels.houses.notifications

import cats.effect.kernel.Resource
import cats.~>
import _root_.fs2.Stream as FStream

trait Make[F[_], A] {
  def make(
      input: A
  ): Resource[F, (FStream[F, Notification], NotificationAction ~> F)]
}

object NotificationExecutor {
  final case class NotificationExecutorMakePartiallyApplied[F[_]]() {
    def apply[A](input: A)(using M: Make[F, A]) = M.make(input)
  }
  def apply[F[_]] = NotificationExecutorMakePartiallyApplied[F]()
}
