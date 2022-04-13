package org.charles.angels.houses

import cats.data.EitherT
import cats.free.Free
import cats.data.EitherK
import cats.data.NonEmptyChain
import org.charles.angels.houses.application.ApplicationAction as HousesApplicationAction
import org.charles.angels.people.application.ApplicationAction as PeopleApplicationAction
import org.charles.angels.houses.application.errors.ApplicationError as HousesApplicationError
import org.charles.angels.people.application.errors.ApplicationError as PeopleApplicationError
import org.charles.angels.houses.logging.LoggingAction
import org.charles.angels.houses.db.DatabaseAction
import org.charles.angels.houses.filesystem.FilesystemAction
import org.charles.angels.houses.errors.ServerError
import org.charles.angels.houses.cron.CronAction
import org.charles.angels.houses.notifications.NotificationAction

package object compiler:
  type ApplicationAction[A] =
    EitherK[HousesApplicationAction, PeopleApplicationAction, A]
  type ApplicationLanguage[A] =
    EitherT[Free[ApplicationAction, _], ServerError, A]

  private type ServerAction0[A] = EitherK[LoggingAction, DatabaseAction, A]
  private type ServerAction1[A] = EitherK[FilesystemAction, ServerAction0, A]
  private type ServerAction2[A] = EitherK[NotificationAction, ServerAction1, A]
  type ServerAction[A] = EitherK[CronAction, ServerAction2, A]
  type CompilerLanguage[F[_], A] = EitherT[Free[F, _], Throwable, A]
  type ServerLanguage[A] = EitherT[Free[ServerAction, _], Throwable, A]
