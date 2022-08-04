package org.charles.angels.houses

import cats.data.EitherK
import org.charles.angels.houses.reports.data.DataReportAction
import org.charles.angels.houses.reports.template.TemplateReportAction

package object reports {
  type ReportAction[A] = EitherK[DataReportAction, TemplateReportAction, A]
}