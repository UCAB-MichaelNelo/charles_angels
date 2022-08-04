package org.charles.angels.houses.http

import cats.implicits.*
import org.http4s.HttpRoutes
import org.charles.angels.houses.compiler.CompilerDSL
import org.http4s.circe.*
import org.http4s.circe.CirceEntityDecoder.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import cats.effect.kernel.Async
import cats.Parallel
import cats.effect.kernel.Concurrent
import org.charles.angels.houses.shared.Executor
import org.charles.angels.houses.http.models.forms.ReportRenderQueryParameter
import org.charles.angels.houses.http.models.forms.ReportScopeQueryParameter
import org.charles.angels.houses.reports.template.RenderableReport
import org.http4s.Headers
import org.http4s.MediaType
import org.http4s.headers.`Content-Type`


class ReportRoutes[F[_]: Async: Parallel: Concurrent: Executor]
    extends ServerRoutes[F] {
    
    private def sendReport[T](reportInfo: F[T], renderableReport: T => RenderableReport, inline: Boolean) = for
        info <- reportInfo
        report <- CompilerDSL.render(renderableReport(info)).run
        stream <- report.stream
        response <- Ok(stream, headers =
                `Content-Type`(MediaType.application.pdf),
                "Content-Disposition" -> s" ${if (inline) "inline" else "attachment"}; filename*=UTF-8''${report.name}"
            )
    yield response

    def routes: HttpRoutes[F] = HttpRoutes.of[F] {
        case GET -> "foodInformation" /: _ :? ReportScopeQueryParameter(scope) +& ReportRenderQueryParameter(render) => 
            sendReport(
                CompilerDSL.getFoodAmountNeeded(scope).run,
                RenderableReport.FoodAmountNeeded.apply, 
                render.fold(false)(identity)
            )
        case GET -> "childsInSixMonthsRange" /: _ :? ReportScopeQueryParameter(scope) +& ReportRenderQueryParameter(render) => 
            sendReport(
                CompilerDSL.getChildrenWithSixMonthsOfRemainingTime(scope).run,
                RenderableReport.ChildrenInSixMonthRange.apply, 
                render.fold(false)(identity)
            )
        case GET -> "wearInformation" /: _ :? ReportScopeQueryParameter(scope) +& ReportRenderQueryParameter(render) => 
            sendReport(
                CompilerDSL.getWearNeededCount(scope).run,
                RenderableReport.WearAmountNeeded.apply, 
                render.fold(false)(identity)
            )
        case GET -> "childrenWithFamily" /: _ :? ReportRenderQueryParameter(render) => 
            sendReport(
                CompilerDSL.getChildrenWithFamily.run,
                RenderableReport.ChildrenWithFamily.apply, 
                render.fold(false)(identity)
            )
    }
}
