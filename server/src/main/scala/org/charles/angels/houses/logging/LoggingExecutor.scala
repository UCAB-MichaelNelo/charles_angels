package org.charles.angels.houses.logging

import cats.~>
import cats.effect.kernel.Resource

type LoggingExecutor[F[_]] = LoggingAction ~> F

trait Make[F[_], A]:
  def make(input: A): Resource[F, LoggingExecutor[F]]

object LoggingExecutor:
  final case class LoggingExecutorMakePartiallyApplied[F[_]]() {
    def apply[A](input: A)(using M: Make[F, A]) = M.make(input)
  }
  def apply[F[_]] = LoggingExecutorMakePartiallyApplied[F]()
