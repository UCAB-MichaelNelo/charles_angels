package org.charles.angels.houses.shared

import cats.~>
import cats.syntax.all.*
import org.charles.angels.houses.compiler.ServerAction
import org.charles.angels.houses.compiler.ServerLanguage
import org.charles.angels.houses.compiler.ApplicationAction
import org.charles.angels.houses.compiler.ApplicationLanguage
import cats.Monad
import cats.data.EitherT
import cats.InjectK
import cats.Inject
import org.charles.angels.houses.errors.ServerError
import cats.free.Free
import fs2.Stream
import org.charles.angels.houses.notifications.Notification
import cats.MonadError

abstract class Executor[F[_]: [F[_]] =>> MonadError[F, Throwable]]
    extends (ApplicationLanguage ~> F) { self =>

  def compiler: ApplicationAction ~> ServerLanguage
  def interpreter: ServerAction ~> F
  def stream: Stream[F, Notification]

  val cachedCompiler = compiler

  val executor =
    new (ServerLanguage ~> F):
      def apply[A](serverLanguage: ServerLanguage[A]) =
        serverLanguage.value
          .foldMap(interpreter)
          .rethrow

  def apply[A](lang: ApplicationLanguage[A]) =
    lang.value
      .compile(cachedCompiler)
      .foldMap(executor)
      .rethrow

  def compile[E, G[_], A](lang: EitherT[Free[G, _], E, A])(using
    E: Inject[E, ServerError],
    I: InjectK[G, ApplicationAction]
  ): ServerLanguage[A] = lang.leftMap(E.inj).value.inject.foldMap(cachedCompiler).rethrow

  def execute[G[_], E, A](lang: EitherT[Free[G, _], E, A])(using
      E: Inject[E, ServerError],
      I: InjectK[G, ApplicationAction]
  ) = apply(EitherT(lang.leftMap(E.inj).value.inject))

  def execute[A](lang: ServerLanguage[A]) =
    executor(lang)

  extension [A](lang: ServerLanguage[A]) def run = self.execute(lang)
  extension [E, G[_], A](lang: EitherT[Free[G, _], E, A])
    def run(using
        E: Inject[E, ServerError],
        I: InjectK[G, ApplicationAction]
    ) = self.execute(lang)
  extension [E, G[_], A](lang: EitherT[Free[G, _], E, A])
    def lift(using
        E: Inject[E, ServerError],
        I: InjectK[G, ApplicationAction]
    ) = self.compile(lang)
}

object Executor {
  def apply[F[_]: Monad](using e: Executor[F]) =
    e
}
