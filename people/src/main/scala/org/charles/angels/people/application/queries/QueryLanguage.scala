package org.charles.angels.people.application.queries

import cats.syntax.all.*
import org.charles.angels.people.domain.Child
import org.charles.angels.people.application.Language
import org.charles.angels.people.application.errors.ApplicationError
import cats.InjectK
import cats.data.EitherT
import cats.free.Free
import java.util.UUID
import org.charles.angels.people.domain.PersonalInformation
import org.charles.angels.people.application.models.PersonalInformationOfChild

enum QueryAction[A] {
  case GetChild(id: UUID) extends QueryAction[Option[Child]]
  case GetChildrenWithoutHousing extends QueryAction[Vector[Child]]
  case GetChildrenOfHouse(houseId: UUID) extends QueryAction[Vector[Child]]
  case FindExistantPersonalInformation(ci: Int) extends QueryAction[Option[PersonalInformation]]
  case GetAllExistantPersonalInformation extends QueryAction[Vector[PersonalInformation]]
  case GetChildrenPersonalInformation extends QueryAction[Vector[PersonalInformationOfChild]]
  case DoesChildCiExist(ci: Int) extends QueryAction[Option[Int]]
}

trait QueryLanguage[F[_]](using InjectK[QueryAction, F]) {
  def getChildrenWithoutHousing: Language[F, Vector[Child]] = EitherT.right(
    Free.liftInject(QueryAction.GetChildrenWithoutHousing)
  )
  def getChildrenPersonalInformation: Language[F, Vector[PersonalInformationOfChild]] = EitherT.right(
    Free.liftInject(QueryAction.GetChildrenPersonalInformation)
  )
  def doesChildCiExist(ci: Int): Language[F, Option[Int]] = EitherT.right(
    Free.liftInject(QueryAction.DoesChildCiExist(ci))
  )
  def getAllExistantPersonalInformation: Language[F, Vector[PersonalInformation]] = EitherT.right(
    Free.liftInject(QueryAction.GetAllExistantPersonalInformation)
  )
  def getChild(id: UUID): Language[F, Child] = EitherT(
    Free
      .liftInject(QueryAction.GetChild(id))
      .map(_.toRight(ApplicationError.ChildNotFound(id).pure))
  )
  def getChildrenOfHouse(houseId: UUID): Language[F, Vector[Child]] = EitherT.right(
    Free.liftInject(QueryAction.GetChildrenOfHouse(houseId))
  )
  def findExistantPersonalInformation(ci: Int): Language[F, Option[PersonalInformation]] = EitherT.right(
    Free.liftInject(QueryAction.FindExistantPersonalInformation(ci))
  )
  def findExistantPersonalInformationOrFail(ci: Int): Language[F, PersonalInformation] = EitherT(
    Free
      .liftInject(QueryAction.FindExistantPersonalInformation(ci))
      .map(_.toRight(ApplicationError.PersonalInformationNotFound(ci).pure))
  )
}
