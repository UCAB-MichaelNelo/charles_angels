package org.charles.angels.houses.reports.template

import cats.~>
import cats.effect.kernel.Resource

trait Make[F[_], A] {
    def make(input: A): Resource[F, TemplateReportAction ~> F]
}
