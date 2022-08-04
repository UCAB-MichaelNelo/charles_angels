package org.charles.angels.houses.reports

import org.charles.angels.houses.domain.House
import java.util.UUID
import org.charles.angels.people.domain.ChildInformation
import org.charles.angels.people.domain.Wear
import org.charles.angels.houses.reports.models.*
import cats.InjectK
import cats.data.EitherT
import cats.free.Free
import cats.effect.kernel.Async
import org.charles.angels.houses.reports.data.DataReportLanguage
import org.charles.angels.houses.reports.template.TemplateReportLanguage
import org.charles.angels.houses.reports.template.TemplateReportAction
import org.charles.angels.houses.reports.data.DataReportAction


trait ReportLanguage[F[_]](using InjectK[TemplateReportAction, F])(using InjectK[DataReportAction, F])
  extends DataReportLanguage[F]
     with TemplateReportLanguage[F] {
      
    }