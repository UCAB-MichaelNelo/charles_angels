package org.charles.angels.houses.auth

import cats.~>
import cats.effect.kernel.Resource

trait Make[F[_], A] {
  def make(input: A): Resource[F, AuthAction ~> F]
}

object AuthExecutor {
  final case class AuthExecutorPartiallyApplied[F[_]]() {
    def apply[A](input: A)(using M: Make[F, A]) = M.make(input)
  }
  def apply[F[_]] = AuthExecutorPartiallyApplied[F]()
}
