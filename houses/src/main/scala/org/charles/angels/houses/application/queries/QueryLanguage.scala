package org.charles.angels.houses.application.queries

import cats.syntax.all.*
import java.util.UUID
import org.charles.angels.houses.domain.House
import org.charles.angels.houses.domain.Schedule
import org.charles.angels.houses.domain.Contact
import org.charles.angels.houses.application.Language
import cats.InjectK
import cats.data.EitherT
import cats.data.NonEmptyChain
import cats.free.Free
import org.charles.angels.houses.application.errors.ApplicationError

enum QueryAction[A]:
  case GetAllHouses extends QueryAction[Vector[House]]
  case GetHouse(id: UUID) extends QueryAction[Option[House]]
  case GetContact(ci: Int) extends QueryAction[Option[Contact]]
  case GetSchedule(id: UUID) extends QueryAction[Option[Schedule]]
  case GetAllContactCI extends QueryAction[Vector[Int]]
  case DoesRIFExists(rif: Int) extends QueryAction[Option[Int]]

trait QueryAlgebra[F[_]]:
  def getHouse(id: UUID): Language[F, House]
  def getContact(ci: Int): Language[F, Contact]
  def getSchedule(id: UUID): Language[F, Schedule]

class QueryLanguage[F[_]](using InjectK[QueryAction, F])
    extends QueryAlgebra[F]:
  def getAllHouses: Language[F, Vector[House]] =
    EitherT.liftF(Free.liftInject(QueryAction.GetAllHouses))
  def getHouse(id: UUID) = EitherT(
    Free
      .liftInject(QueryAction.GetHouse(id))
      .map(_.toRight(ApplicationError.HouseNotFoundError(id).pure))
  )
  def getContact(id: Int) = EitherT(
    Free
      .liftInject(QueryAction.GetContact(id))
      .map(_.toRight(ApplicationError.ContactNotFoundError(id).pure))
  )
  def getSchedule(id: UUID) = EitherT(
    Free
      .liftInject(QueryAction.GetSchedule(id))
      .map(_.toRight(ApplicationError.ScheduleNotFoundError(id).pure))
  )

  def doesRifExist(rif: Int) = EitherT(
    Free.liftInject(QueryAction.DoesRIFExists(rif))
    .map(_.map(ApplicationError.ExistingRif(_).pure[NonEmptyChain]).toLeft(()))
  )

  def getAllContactCI: Language[F, Vector[Int]] = EitherT.liftF(Free.liftInject(QueryAction.GetAllContactCI))
