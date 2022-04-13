package org.charles.angels.people

import cats.data.EitherT
import cats.data.NonEmptyChain
import cats.free.Free
import org.charles.angels.people.application.errors.ApplicationError
import cats.data.EitherK
import org.charles.angels.people.application.events.EventAction
import org.charles.angels.people.application.queries.QueryAction

package object application {
  type ApplicationAction[A] = EitherK[QueryAction, EventAction, A]
  type Language[F[_], A] =
    EitherT[Free[F, _], NonEmptyChain[ApplicationError], A]
  type ApplicationLanguage[A] = Language[ApplicationAction, A]
}
