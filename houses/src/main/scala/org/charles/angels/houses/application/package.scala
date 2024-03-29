package org.charles.angels.houses

import cats.data.EitherT
import cats.free.Free
import cats.data.NonEmptyChain
import org.charles.angels.houses.application.errors.ApplicationError
import cats.data.EitherK
import org.charles.angels.houses.application.events.DomainEventAction
import org.charles.angels.houses.application.services.ServiceAction
import org.charles.angels.houses.application.queries.QueryAction

package object application:
  private type ApplicationAction1[A] =
    EitherK[DomainEventAction, ServiceAction, A]
  type ApplicationAction[A] = EitherK[QueryAction, ApplicationAction1, A]
  type Language[F[_], A] =
    EitherT[Free[F, _], NonEmptyChain[ApplicationError], A]
  type ApplicationLanguage[A] = Language[ApplicationAction, A]
