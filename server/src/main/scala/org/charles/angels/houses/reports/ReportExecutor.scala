package org.charles.angels.houses.reports

import cats.~>
import cats.effect.kernel.Resource

trait Make[F[_], A] {
  def make(input: A): Resource[F, ReportAction ~> F]
}

object ReportExecutor {
  final class ReportExecutorPartiallyApplied[F[_]] {
    def apply[A](input: A)(using M: Make[F, A]) = M.make(input)
  }
  def apply[F[_]] = ReportExecutorPartiallyApplied[F]
}
