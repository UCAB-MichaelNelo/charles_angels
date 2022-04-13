package org.charles.angels.houses.db

import cats.~>
import cats.effect.kernel.Resource

trait Make[F[_], G[_], A]:
  def make(in: A): Resource[F, G ~> F]

object DatabaseExecutor {
  final case class DatabaseExecutorMakePartiallyApplied[F[_], G[_]]() {
    def apply[A](input: A)(using M: Make[F, G, A]) = M.make(input)
  }
  def apply[F[_], G[_]] = DatabaseExecutorMakePartiallyApplied[F, G]()
}
