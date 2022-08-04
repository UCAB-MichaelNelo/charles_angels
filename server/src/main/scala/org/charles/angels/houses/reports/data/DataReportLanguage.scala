package org.charles.angels.houses.reports.data

import org.charles.angels.houses.domain.House
import java.util.UUID
import org.charles.angels.people.domain.ChildInformation
import org.charles.angels.people.domain.Wear
import org.charles.angels.houses.reports.models.*
import cats.InjectK
import cats.data.EitherT
import cats.free.Free
import cats.effect.kernel.Async

enum ReportScope {
  case Houses
  case General
}

enum DataReportAction[A] {
  case GetFoodAmountNeeded(scope: ReportScope)
      extends DataReportAction[Either[Throwable, Either[FoodAmountNeeded, Vector[
        HouseWithFoodAmountNeeded
      ]]]]
  case GetChildrenWithSixMonthsOfRemainingTime(scope: ReportScope)
  extends DataReportAction[Either[Throwable, Either[Vector[ChildWithSixMonthsOfRemainingTime], Vector[
        HouseWithChildrenWithSixMonthsOfRemainingTime
      ]]]]
  case GetWearNeededCount(scope: ReportScope)
      extends DataReportAction[Either[Throwable, Either[Vector[WearInformation], Vector[
        HouseWithWearInformation
      ]]]]
  case GetChildrenWithFamily
      extends DataReportAction[Either[Throwable, Vector[HouseWithChildrenAndFamily]]]
}

trait DataReportLanguage[F[_]](using InjectK[DataReportAction, F]) {
  def getFoodAmountNeeded(scope: ReportScope) = EitherT(
    Free.liftInject(DataReportAction.GetFoodAmountNeeded(scope))
  )
  def getChildrenWithSixMonthsOfRemainingTime(scope: ReportScope) = EitherT(
    Free.liftInject(DataReportAction.GetChildrenWithSixMonthsOfRemainingTime(scope))
  )
  def getWearNeededCount(scope: ReportScope) = EitherT(
    Free.liftInject(DataReportAction.GetWearNeededCount(scope))
  )
  def getChildrenWithFamily = EitherT(
    Free.liftInject(DataReportAction.GetChildrenWithFamily)
  )
}
