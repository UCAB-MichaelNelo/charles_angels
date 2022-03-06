package org.charles.angels.houses.application.standard

import org.charles.angels.houses.application.Language
import cats.data.ValidatedNec
import cats.Inject
import org.charles.angels.houses.application.errors.ApplicationError
import cats.data.EitherT

trait StandardOperation[F[_]]:
  def of[E, A](result: ValidatedNec[E, A])(using
      inj: Inject[E, ApplicationError]
  ): Language[F, A]

class StandardLanguage[F[_]] extends StandardOperation[F]:
  def of[E, A](result: ValidatedNec[E, A])(using
      injector: Inject[E, ApplicationError]
  ) = EitherT.fromEither(
    result.bimap(_.map(injector.inj), identity).toEither
  )
