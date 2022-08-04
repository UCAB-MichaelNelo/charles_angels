package org.charles.angels.houses.reports.template

import org.charles.angels.houses.reports.models.FoodAmountNeeded as FoodAmountNeededModel
import org.charles.angels.houses.reports.models.HouseWithFoodAmountNeeded
import org.charles.angels.houses.reports.models.WearInformation
import org.charles.angels.houses.reports.models.ChildWithSixMonthsOfRemainingTime
import org.charles.angels.houses.reports.models.HouseWithChildrenWithSixMonthsOfRemainingTime
import org.charles.angels.houses.reports.models.HouseWithWearInformation
import cats.InjectK
import cats.data.EitherT
import cats.free.Free
import fs2.Stream
import cats.effect.kernel.Async
import org.charles.angels.houses.reports.models.HouseWithChildrenAndFamily

enum RenderableReport {
    case FoodAmountNeeded(info: Either[FoodAmountNeededModel, Vector[HouseWithFoodAmountNeeded]])
    case WearAmountNeeded(info: Either[Vector[WearInformation], Vector[HouseWithWearInformation]])
    case ChildrenInSixMonthRange(info: Either[Vector[ChildWithSixMonthsOfRemainingTime], Vector[HouseWithChildrenWithSixMonthsOfRemainingTime]])
    case ChildrenWithFamily(info: Vector[HouseWithChildrenAndFamily])
}

enum TemplateReportAction[A] {
    case Render(report: RenderableReport) extends TemplateReportAction[Either[Throwable, Report]]
}

trait Report {
  def name: String
  def stream[F[_] : Async]: F[Stream[F, Byte]]
}

trait TemplateReportLanguage[F[_]](using InjectK[TemplateReportAction, F]) {
  def render(report: RenderableReport) = EitherT(
    Free.liftInject(TemplateReportAction.Render(report))
  )
}