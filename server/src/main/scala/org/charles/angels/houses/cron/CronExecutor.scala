package org.charles.angels.houses.cron

import cats.~>
import cats.effect.kernel.Resource

trait Make[F[_], A] {
  def make(input: A): Resource[F, CronAction ~> F]
}

object CronExecutor {
  final case class CronExecutorPartiallyApplied[F[_]]() {
    def apply[A](input: A)(using M: Make[F, A]) = M.make(input)
  }
  def apply[F[_]] = CronExecutorPartiallyApplied[F]()
}
