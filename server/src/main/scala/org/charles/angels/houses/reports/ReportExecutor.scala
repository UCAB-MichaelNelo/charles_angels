package org.charles.angels.houses.reports

import cats.~>
import cats.syntax.all.*
import cats.effect.implicits.*
import cats.effect.kernel.Resource
import org.charles.angels.houses.reports.data.Make as DataMake
import org.charles.angels.houses.reports.template.Make as TemplateMake

object ReportExecutor {
  final class ReportExecutorPartiallyApplied[F[_]] {
    def apply[A, B](dataMaker: A, templateMaker: B)(using M: DataMake[F, A])(using MM: TemplateMake[F, B]): Resource[F, ReportAction ~> F] = 
      for
        de <- M.make(dataMaker)
        te <- MM.make(templateMaker)
      yield de or te
  }
  def apply[F[_]] = ReportExecutorPartiallyApplied[F]
}
