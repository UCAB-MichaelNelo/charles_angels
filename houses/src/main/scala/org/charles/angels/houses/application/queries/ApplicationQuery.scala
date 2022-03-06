package org.charles.angels.houses.application.queries

import cats.syntax.all.*
import java.util.UUID
import org.charles.angels.houses.domain.House
import org.charles.angels.houses.domain.Schedule
import org.charles.angels.houses.domain.Contact
import org.charles.angels.houses.application.Language
import cats.InjectK
import cats.data.EitherT
import cats.free.Free
import org.charles.angels.houses.application.errors.ApplicationError

enum QueryAction[A]:
  case GetHouse(id: UUID) extends QueryAction[Option[House]]
  case GetContact(id: UUID) extends QueryAction[Option[Contact]]
  case GetSchedule(id: UUID) extends QueryAction[Option[Schedule]]

trait QueryAlgebra[F[_]]:
  def getHouse(id: UUID): Language[F, House]
  def getContact(id: UUID): Language[F, Contact]
  def getSchedule(id: UUID): Language[F, Schedule]

class QueryLanguage[F[_]](using InjectK[QueryAction, F])
    extends QueryAlgebra[F]:
  def getHouse(id: UUID) = EitherT(
    Free
      .liftInject(QueryAction.GetHouse(id))
      .map(_.toRight(ApplicationError.HouseNotFoundError(id).pure))
  )
  def getContact(id: UUID) = EitherT(
    Free
      .liftInject(QueryAction.GetContact(id))
      .map(_.toRight(ApplicationError.ContactNotFoundError(id).pure))
  )
  def getSchedule(id: UUID) = EitherT(
    Free
      .liftInject(QueryAction.GetSchedule(id))
      .map(_.toRight(ApplicationError.ScheduleNotFoundError(id).pure))
  )
