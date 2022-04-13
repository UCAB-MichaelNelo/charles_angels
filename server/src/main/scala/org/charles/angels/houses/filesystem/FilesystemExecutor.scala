package org.charles.angels.houses.filesystem

import cats.~>
import cats.effect.kernel.Resource

type FilesystemExecutor[F[_]] = FilesystemAction ~> F

trait Make[F[_], A]:
  def make(input: A): Resource[F, FilesystemExecutor[F]]

object FilesystemExecutor:
  class FilesystemMakePartiallyApplied[F[_]] {
    def apply[A](input: A)(using maker: Make[F, A]) = maker.make(input)
  }
  def apply[F[_]] = FilesystemMakePartiallyApplied[F]
