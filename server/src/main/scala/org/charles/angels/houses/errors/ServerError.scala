package org.charles.angels.houses.errors

import cats.syntax.all.*
import cats.data.NonEmptyChain
import org.charles.angels.houses.application.errors.ApplicationError as HousesApplicationError
import org.charles.angels.people.application.errors.ApplicationError as PeopleApplicationError
import cats.Inject
import org.http4s.MessageFailure

enum ServerError extends Throwable:
  case HousesError(errors: NonEmptyChain[HousesApplicationError])
  case PeopleError(errors: NonEmptyChain[PeopleApplicationError])
  case DatabaseError(error: Throwable)
  case FilesystemError(error: Throwable)
  case ParseError(error: MessageFailure)
  case NotificationStreamClosed

given necHousesSE: Inject[NonEmptyChain[HousesApplicationError], ServerError]
  with
  def inj = (errs: NonEmptyChain[HousesApplicationError]) =>
    ServerError.HousesError(errs)
  def prj = (err: ServerError) =>
    err match {
      case ServerError.HousesError(errs) => errs.some
      case _                             => None
    }

given necPeopleSE: Inject[NonEmptyChain[PeopleApplicationError], ServerError]
  with
  def inj = (errs: NonEmptyChain[PeopleApplicationError]) =>
    ServerError.PeopleError(errs)
  def prj = (err: ServerError) =>
    err match {
      case ServerError.PeopleError(errs) => errs.some
      case _                             => None
    }
