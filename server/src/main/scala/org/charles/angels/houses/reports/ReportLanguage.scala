package org.charles.angels.houses.reports

import org.charles.angels.houses.domain.House
import java.util.UUID
import org.charles.angels.people.domain.ChildInformation
import org.charles.angels.people.domain.Wear
import org.charles.angels.houses.reports.models.HouseWithBeneficiaryCount
import org.charles.angels.houses.reports.models.HouseWithFoodInformation
import org.charles.angels.houses.reports.models.HouseWithWearInformation
import org.charles.angels.houses.reports.models.BeneficiaryCount
import org.charles.angels.houses.reports.models.FoodInformation
import org.charles.angels.houses.reports.models.WearInformation
import cats.InjectK
import cats.data.EitherT
import cats.free.Free
import cats.effect.kernel.Async

enum ReportScope {
  case Houses
  case General
}

enum ReportAction[A] {
  case CreateNewBeneficiaryCountEntry(houseId: UUID)
      extends ReportAction[Either[Throwable, Unit]]
  case CreateNewFoodAmountEntry(houseId: UUID)
      extends ReportAction[Either[Throwable, Unit]]
  case CreateNewNeededWearEntry(houseId: UUID)
      extends ReportAction[Either[Throwable, Unit]]

  case IncrementBeneficiaryCount(houseId: UUID)
      extends ReportAction[Either[Throwable, Int]]
  case AddWearNeededInHouse(houseId: UUID, wear: Wear)
      extends ReportAction[Either[Throwable, Unit]]
  case IncrementFoodAmountNeededInHouse(
      houseId: UUID,
      childId: UUID,
      allergies: Vector[String]
  ) extends ReportAction[Either[Throwable, Unit]]

  case DecrementBeneficiaryCount(houseId: UUID)
      extends ReportAction[Either[Throwable, Int]]
  case RemoveWearNeededInHouse(houseId: UUID, wear: Wear)
      extends ReportAction[Either[Throwable, Unit]]
  case DecrementFoodAmountNeededInHouse(
      houseId: UUID,
      childId: UUID,
      allergies: Vector[String]
  ) extends ReportAction[Either[Throwable, Unit]]

  case GetBeneficiaryCount(scope: ReportScope)
      extends ReportAction[Either[Throwable, Either[BeneficiaryCount, Vector[
        HouseWithBeneficiaryCount
      ]]]]
  case GetFoodAmountNeeded(scope: ReportScope)
      extends ReportAction[Either[Throwable, Either[FoodInformation, Vector[
        HouseWithFoodInformation
      ]]]]
  case GetWearNeededCount(scope: ReportScope)
      extends ReportAction[Either[Throwable, Either[WearInformation, Vector[
        HouseWithWearInformation
      ]]]]
}

trait ReportLanguage[F[_]](using InjectK[ReportAction, F]) {
  def createNewBeneficiaryCountEntry(houseId: UUID) = EitherT(
    Free.liftInject(ReportAction.CreateNewBeneficiaryCountEntry(houseId))
  )
  def createNewFoodAmountEntry(houseId: UUID) = EitherT(
    Free.liftInject(ReportAction.CreateNewFoodAmountEntry(houseId))
  )
  def createNewNeededWearEntry(houseId: UUID) = EitherT(
    Free.liftInject(ReportAction.CreateNewNeededWearEntry(houseId))
  )

  def incrementBeneficiaryCount(houseId: UUID) = EitherT(
    Free.liftInject(ReportAction.IncrementBeneficiaryCount(houseId))
  )
  def addWearNeededInBeneficiaryHouse(houseId: UUID, wear: Wear) = EitherT(
    Free.liftInject(ReportAction.AddWearNeededInHouse(houseId, wear))
  )
  def incrementFoodAmountNeededInHouse(
      houseId: UUID,
      childId: UUID,
      allergies: Vector[String]
  ) = EitherT(
    Free.liftInject(
      ReportAction.IncrementFoodAmountNeededInHouse(houseId, childId, allergies)
    )
  )

  def decrementBeneficiaryCount(houseId: UUID) = EitherT(
    Free.liftInject(ReportAction.DecrementBeneficiaryCount(houseId))
  )
  def removeWearNeededInBeneficiaryHouse(houseId: UUID, wear: Wear) = EitherT(
    Free.liftInject(ReportAction.RemoveWearNeededInHouse(houseId, wear))
  )
  def decrementFoodAmountNeededInHouse(
      houseId: UUID,
      childId: UUID,
      allergies: Vector[String]
  ) = EitherT(
    Free.liftInject(
      ReportAction.DecrementFoodAmountNeededInHouse(houseId, childId, allergies)
    )
  )

  def getBeneficiaryCount(scope: ReportScope) = EitherT(
    Free.liftInject(ReportAction.GetBeneficiaryCount(scope))
  )
  def getFoodAmountNeeded(scope: ReportScope) = EitherT(
    Free.liftInject(ReportAction.GetFoodAmountNeeded(scope))
  )
  def getWearNeededCount(scope: ReportScope) = EitherT(
    Free.liftInject(ReportAction.GetWearNeededCount(scope))
  )
}
