package org.charles.angels.houses.reports.template.scalatags

import cats.~>
import cats.syntax.all.*
import cats.implicits.*
import cats.effect.implicits.*
import cats.effect.syntax.all.*
import cats.effect.std.Queue
import cats.effect.kernel.Async
import org.charles.angels.houses.reports.template.TemplateReportAction
import org.charles.angels.houses.reports.template.Report
import org.charles.angels.houses.reports.template.RenderableReport
import org.charles.angels.houses.reports.template.Make
import scalatags.Text as TextTags
import scalatags.Text.tags as Html
import scalatags.Text.tags2 as Html2
import scalatags.Text.attrs as Attr
import scalatags.Text.styles as TagStyling
import scalatags.Text.implicits.*
import scalatags.stylesheet.{*, given}
import java.time.LocalDate
import java.time.LocalTime
import cats.effect.kernel.Resource
import fs2.Stream
import fs2.Chunk
import java.io.OutputStream
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import io.github.vigoo.prox.ProxFS2
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.io.PipedOutputStream
import java.io.PipedInputStream
import org.charles.angels.houses.reports.models.FoodAmountNeeded
import java.time.Period

class ScalatagsExecutor[F[_]: Async](wkhtmltopdfPath: String, resourcesBasePath: String) extends (TemplateReportAction ~> F) {
    object Styling extends CascadingStyleSheet {
        initStyleSheet()

        override def customSheetName: Option[String] = "charles-angels".some;

        val caDivider = cls(
            TagStyling.fontWeight := 600, 
            TagStyling.textAlign := "center"
        )

        val caHeadTh = cls(
            TagStyling.width := 100
        )

        val caSubTh = cls(
            TagStyling.width := 50,
            TagStyling.borderRight := "2px solid rgba(224, 224, 224, 1)",
            TagStyling.outline := "none!important"
        )

        val caTable = cls(
            TagStyling.width := "100%",
            td(
                TagStyling.padding := "0.5em",
                TagStyling.textAlign := "center",
                TagStyling.borderRight := "thin solid rgba(224, 224, 224, 1)",
                TagStyling.borderBottom := "thin solid rgba(224, 224, 224, 1)",
            ),
            th(
                TagStyling.padding := "0.5em",
                TagStyling.backgroundColor := "#488aaa",
                TagStyling.fontWeight := "600",
                TagStyling.textAlign := "center",
                TagStyling.color := "white"
            ),
            tr(
                td.firstOfType(
                    TagStyling.borderLeft := "thin solid rgba(224, 224, 224, 1)",
                )
            )
        )
    }

    private def layout(reportName: String, reportScope: String)(body: TextTags.TypedTag[String]*) = 
        Html.html(
            Html.head(
                Html.base(Attr.href := resourcesBasePath),
                Html.meta(Attr.charset := "utf-8"),
                Html.meta(Attr.name := "viewport", Attr.content := "width=device-width, initial-scale=1"),
                Html.link(
                    Attr.rel := "stylesheet", 
                    Attr.href := "/css/mui.min.css",
                    Attr.`type` := "text/css"
                ),
                Html2.style(Styling.styleSheetText),
                Html2.title(s"$reportName ($reportScope)"),
                Html.script(Attr.src := "/js/mui.min.js")
            ),
            Html.body(
                Html.div(Attr.`class` := "mui-container-fluid", TagStyling.padding := "6em")(
                    Html.div(
                        TagStyling.display := "-webkit-box",
                        TagStyling.display := "flex", 
                        TagStyling.css("gap") := "3em",
                        TagStyling.minHeight := "150px"
                    )(
                        Html.img(
                            Attr.src := "/img/charles_angels_logo.jpeg", 
                            TagStyling.width := "135px",
                            TagStyling.flexShrink := "0"
                        ),
                        Html.div(
                            TagStyling.flexGrow := "1",
                            TagStyling.css("-webkit-box-flex") := "1",
                            TagStyling.css("-webkit-flex") := "1",
                            TagStyling.paddingLeft := "3em",
                            TagStyling.flexShrink := "0"
                        )(
                            Html.h6(Attr.`class` := "mui--text-button")(Html.strong("FUNDACION ANGELES DE CHARLES")),
                            Html.h6(Attr.`class` := "mui--text-button")(Html.strong("RIF: "), Html.span(Attr.`class` := "mui--text-body2")("J412469053")),
                            Html.h6(Attr.`class` := "mui--text-button")(Html.strong("DIRECCION: "), Html.span(Attr.`class` := "mui--text-body2")("CALLE MADRID QTA MARENI ZONA LAS MERCEDES CARACAS MIRANDA ZONA POSTAL 1060")),
                            Html.h6(Attr.`class` := "mui--text-button")(Html.strong("FECHA DE EMISION: "), Html.span(Attr.`class` := "mui--text-body2")(LocalDate.now().toString())),
                            Html.h6(Attr.`class` := "mui--text-button")(Html.strong("HORA DE EMISION: "), Html.span(Attr.`class` := "mui--text-body2")(LocalTime.now().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_TIME)))
                        )
                    )
                ),
                Html.div(
                    Attr.`class` := "mui-container-fluid",
                    TagStyling.padding := "6em", 
                    TagStyling.paddingTop := "0", 
                    TagStyling.display := "grid", 
                    TagStyling.css("gap") := "4em"
                )(
                    (Seq(Html.h5(
                        TagStyling.fontWeight := "600", 
                        TagStyling.textDecoration := "underline", 
                        TagStyling.textAlign := "center", 
                        TagStyling.width := "100%",
                        TagStyling.paddingBottom := "6em"
                    )(s"$reportName ($reportScope)")) ++ body)*
                )
            )
        )

    private def renderReport(reportName: String, reportScope: String)(body: TextTags.TypedTag[String]*) = new Report {
        private def inputStream[G[_]: Async] = fs2.io.readOutputStream[G](1024) { 
            (os: OutputStream) => Async[G].delay { layout(reportName, reportScope)(body*).writeBytesTo(os); os.close() } 
        }
        private def output[G[_]: Async](queue: Queue[G, Option[Byte]]): fs2.Pipe[G, Byte, Unit] = (stream) => 
            stream
            .map(_.some)
            .evalMap(queue.offer)
            .drain
        def name = s"$reportName ($reportScope).pdf"
        def stream[G[_]: Async] = {
            val prox = ProxFS2[G]

            import prox.Process
            import prox.ProcessRunner
            import prox.JVMProcessInfo
            import prox.JVMProcessRunner

            val runner = new JVMProcessRunner
            val process = Process(wkhtmltopdfPath, List("--enable-local-file-access", "--title", name, "-", "-"))

            for 
                queue <- Queue.unbounded[G, Option[Byte]]
                stream = fs2.Stream.fromQueueNoneTerminated(queue)
            yield stream.merge(Stream.eval { 
                for
                    _ <- (process < inputStream[G] > output[G](queue)).run()(runner)
                    _ <- queue.offer(None)
                yield 0
            })
        }
    }.pure[F].attempt

    def apply[A](fa: TemplateReportAction[A]): F[A] = fa match {
        case TemplateReportAction.Render(RenderableReport.ChildrenWithFamily(houseChildrenInfo)) =>
            renderReport("REPORTE BENEFICIARIOS CON FAMILIA EN LA MISMA CASA", "ÚNICO")(
                houseChildrenInfo.toSeq.map(houseGroup =>
                    Html.div(
                        Html.div(Styling.caDivider)(houseGroup.houseName),
                        Html.table(Styling.caTable, TagStyling.paddingBottom := "3em", TagStyling.paddingTop := "3em")(
                            Html.thead(
                                Html.tr(
                                    Html.th("Cedula de Identidad"),
                                    Html.th("Nombre de Beneficiario"),
                                    Html.th("Familia de Beneficiario")
                                )
                            ),
                            Html.tbody(
                                houseGroup.children.toSeq.map(group => 
                                    Html.tr(
                                        Html.td(group.information.ci),
                                        Html.td(group.information.name),
                                        Html.td(group.family.map(inf => s"${inf.name} (${inf.ci})").mkString(", "))
                                    )
                                )*
                            )
                        )
                    )
                )*
            )
        case TemplateReportAction.Render(RenderableReport.ChildrenInSixMonthRange(Right(houseChildrenGroups))) => 
            renderReport("REPORTE DE BENEFICIARIOS A 6 MESES O MENOS DE PERDER EL BENEFICIO", "POR CASAS")(
                houseChildrenGroups.toSeq.map(houseGroup =>
                    Html.div(
                        Html.div(Styling.caDivider)(houseGroup.houseName),
                        Html.table(Styling.caTable, TagStyling.paddingBottom := "3em", TagStyling.paddingTop := "3em")(
                            Html.thead(
                                Html.tr(
                                    Html.th("Cedula de identidad"),
                                    Html.th("Nombre Beneficiario"),
                                    Html.th("Edad Maxima"),
                                    Html.th("Fecha de nacimiento"),
                                    Html.th("Tiempo hasta cumpleaños")
                                )
                            ),
                            Html.tbody(
                                houseGroup.children.toSeq.map(group => 
                                    val now = LocalDate.now
                                    val timeRemainingForBirthday = Period.between(now, group.birthdate.plusYears(group.maxAge + 1))
                                    Html.tr(
                                        Html.td(group.ci),
                                        Html.td(group.name),
                                        Html.td(group.maxAge),
                                        Html.td(group.birthdate.toString),
                                        Html.td(s"${timeRemainingForBirthday.getMonths} meses, ${timeRemainingForBirthday.getDays} dias"),
                                    )
                                )*
                            )
                        )
                    )
                )*
            )
        case TemplateReportAction.Render(RenderableReport.ChildrenInSixMonthRange(Left(childrenGroups))) => 
            renderReport("REPORTE DE BENEFICIARIOS A 6 MESES O MENOS DE PERDER EL BENEFICIO", "GENERAL")(
                Html.table(Styling.caTable)(
                    Html.thead(
                        Html.tr(
                            Html.th("Cedula de identidad"),
                            Html.th("Nombre Beneficiario"),
                            Html.th("Edad Maxima"),
                            Html.th("Fecha de nacimiento"),
                            Html.th("Tiempo hasta cumpleaños")
                        )
                    ),
                    Html.tbody(
                        childrenGroups.toSeq.map(group => 
                            val now = LocalDate.now
                            val timeRemainingForBirthday = Period.between(now, group.birthdate.plusYears(group.maxAge + 1))
                            Html.tr(
                                Html.td(group.ci),
                                Html.td(group.name),
                                Html.td(group.maxAge),
                                Html.td(group.birthdate.toString),
                                Html.td(s"${timeRemainingForBirthday.getMonths} meses, ${timeRemainingForBirthday.getDays} dias"),
                            )
                        )*
                    )
                )
            )
        case TemplateReportAction.Render(RenderableReport.WearAmountNeeded(Right(houseWearGroups))) =>
            renderReport("REPORTE DE VESTIMENTA", "POR CASAS")(
                houseWearGroups.toSeq.map(houseGroup => 
                    Html.div(
                        Html.div(Styling.caDivider)(houseGroup.houseName),
                        Html.table(Styling.caTable, TagStyling.paddingBottom := "3em", TagStyling.paddingTop := "3em")(
                            Html.thead(
                                Html.tr(
                                    Html.th(Attr.colspan := 2, Styling.caHeadTh)("Shorts o Pantalones"),
                                    Html.th(Attr.colspan := 2, Styling.caHeadTh)("Camisas o Camisetas"),
                                    Html.th(Attr.colspan := 2, Styling.caHeadTh)("Suéteres"),
                                    Html.th(Attr.colspan := 2, Styling.caHeadTh)("Vestidos"),
                                    Html.th(Attr.colspan := 2, Styling.caHeadTh)("Calzados")
                                ),
                                Html.tr(
                                    Html.th(Styling.caSubTh)("Talla"),
                                    Html.th(Styling.caSubTh)("Cantidad"),
                                    Html.th(Styling.caSubTh)("Talla"),
                                    Html.th(Styling.caSubTh)("Cantidad"),
                                    Html.th(Styling.caSubTh)("Talla"),
                                    Html.th(Styling.caSubTh)("Cantidad"),
                                    Html.th(Styling.caSubTh)("Talla"),
                                    Html.th(Styling.caSubTh)("Cantidad"),
                                    Html.th(Styling.caSubTh)("Talla"),
                                    Html.th(Styling.caSubTh)("Cantidad")
                                )
                            ),
                            Html.tbody(
                                houseGroup.wearInformation.toSeq.map(group => 
                                    Html.tr(
                                        Html.td(group.shortOrTrousersSize.map(_.toString) getOrElse ""),
                                        Html.td(group.shortOrTrousersSize.map(_.toString) >> group.shortOrTrousersAmount.map(_.toString) getOrElse ""),
                                        Html.td(group.tshirtOrShirtSize.map(_.toString) getOrElse ""),
                                        Html.td(group.tshirtOrShirtSize.map(_.toString) >> group.tshirtOrShirtAmount.map(_.toString) getOrElse ""),
                                        Html.td(group.sweaterSize.map(_.toString) getOrElse ""),
                                        Html.td(group.sweaterSize.map(_.toString) >> group.sweaterAmount.map(_.toString) getOrElse ""),
                                        Html.td(group.dressSize.map(_.toString) getOrElse ""),
                                        Html.td(group.dressSize.map(_.toString) >> group.dressAmount.map(_.toString) getOrElse ""),
                                        Html.td(group.footwearSize.map(_.toString) getOrElse ""),
                                        Html.td(group.footwearSize.map(_.toString) >> group.footwearAmount.map(_.toString) getOrElse "")
                                    )
                                )*
                            )
                        )
                    )
                )*
            )

        case TemplateReportAction.Render(RenderableReport.WearAmountNeeded(Left(wearGroups))) =>
            renderReport("REPORTE DE VESTIMENTA", "GENERAL")(
                Html.table(Styling.caTable)(
                    Html.thead(
                        Html.tr(
                            Html.th(Attr.colspan := 2, Styling.caHeadTh)("Shorts o Pantalones"),
                            Html.th(Attr.colspan := 2, Styling.caHeadTh)("Camisas o Camisetas"),
                            Html.th(Attr.colspan := 2, Styling.caHeadTh)("Suéteres"),
                            Html.th(Attr.colspan := 2, Styling.caHeadTh)("Vestidos"),
                            Html.th(Attr.colspan := 2, Styling.caHeadTh)("Calzados")
                        ),
                        Html.tr(
                            Html.th(Styling.caSubTh)("Talla"),
                            Html.th(Styling.caSubTh)("Cantidad"),
                            Html.th(Styling.caSubTh)("Talla"),
                            Html.th(Styling.caSubTh)("Cantidad"),
                            Html.th(Styling.caSubTh)("Talla"),
                            Html.th(Styling.caSubTh)("Cantidad"),
                            Html.th(Styling.caSubTh)("Talla"),
                            Html.th(Styling.caSubTh)("Cantidad"),
                            Html.th(Styling.caSubTh)("Talla"),
                            Html.th(Styling.caSubTh)("Cantidad")
                        )
                    ),
                    Html.tbody(
                        wearGroups.toSeq.map(group => 
                            Html.tr(
                                Html.td(group.shortOrTrousersSize.map(_.toString) getOrElse ""),
                                Html.td(group.shortOrTrousersSize.map(_.toString) >> group.shortOrTrousersAmount.map(_.toString) getOrElse ""),
                                Html.td(group.tshirtOrShirtSize.map(_.toString) getOrElse ""),
                                Html.td(group.tshirtOrShirtSize.map(_.toString) >> group.tshirtOrShirtAmount.map(_.toString) getOrElse ""),
                                Html.td(group.sweaterSize.map(_.toString) getOrElse ""),
                                Html.td(group.sweaterSize.map(_.toString) >> group.sweaterAmount.map(_.toString) getOrElse ""),
                                Html.td(group.dressSize.map(_.toString) getOrElse ""),
                                Html.td(group.dressSize.map(_.toString) >> group.dressAmount.map(_.toString) getOrElse ""),
                                Html.td(group.footwearSize.map(_.toString) getOrElse ""),
                                Html.td(group.footwearSize.map(_.toString) >> group.footwearAmount.map(_.toString) getOrElse "")
                            )
                        )*
                    )
                )
            )
        case TemplateReportAction.Render(RenderableReport.FoodAmountNeeded(Right(houses))) =>
            renderReport("REPORTE DE ALIMENTOS", "POR CASA")(
                Html.table(Styling.caTable)(
                    Html.thead(
                        Html.tr(
                            Html.th("Casa"),
                            Html.th("Cantidad de comida necesaria")
                        )
                    ),
                    Html.tbody(
                        houses.toSeq.map(house => 
                            Html.tr(
                                Html.td(Html.a(Attr.href := s"//houses/${house.houseId}", house.houseName)),
                                Html.td(house.foodAmountNeeded.foodAmountNeeded)
                            )
                        )*
                    )
                )
            ) 
        case TemplateReportAction.Render(RenderableReport.FoodAmountNeeded(Left(foodNeeded))) =>
            renderReport("REPORTE DE ALIMENTOS", "GENERAL")(
                Html.table(Styling.caTable)(
                    Html.thead(
                        Html.tr(
                            Html.th("Casa"),
                            Html.th("Cantidad de comida necesaria")
                        )
                    ),
                    Html.tbody(
                        Html.tr(
                            Html.td("Todas"),
                            Html.td(foodNeeded.foodAmountNeeded)
                        )
                    )
                )
            )
    }
}

case class Scalatags(wkhtmltopdfPath: String, resourcesBasePath: String)

given [F[_]: Async]: Make[F, Scalatags] with
    def make(maker: Scalatags): Resource[F, TemplateReportAction ~> F] = ScalatagsExecutor[F](maker.wkhtmltopdfPath, maker.resourcesBasePath).pure[Resource[F, _]]