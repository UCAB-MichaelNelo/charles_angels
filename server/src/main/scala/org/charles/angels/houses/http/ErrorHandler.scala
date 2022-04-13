package org.charles.angels.houses.http

import org.charles.angels.houses.http.given
import org.charles.angels.houses.http.*
import cats.syntax.all.*
import org.charles.angels.houses.compiler.ServerLanguage
import org.charles.angels.houses.errors.ServerError
import org.http4s.Response
import org.http4s.circe.*
import org.http4s.circe.CirceEntityDecoder.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import cats.effect.kernel.Async
import org.http4s.Status
import org.http4s.dsl.Http4sDsl
import org.charles.angels.houses.compiler.CompilerDSL
import cats.data.NonEmptyChain
import org.charles.angels.houses.application.errors.ApplicationError as HouseApplicationError
import org.http4s.dsl.impl.Responses
import fs2.Stream
import org.http4s.dsl.impl.Responses.UnprocessableEntityOps
import org.charles.angels.houses.shared.Executor
import cats.Monad
import cats.InjectK
import cats.Inject
import org.http4s.HttpVersion
import org.http4s.Headers
import org.http4s.MessageFailure

trait ErrorHandler[F[_]] {
  def handle(serverError: ServerError): F[Response[F]]
  def unhandled(e: Throwable): F[Response[F]]
}

object ErrorHandler {
  def apply[F[_]](using E: ErrorHandler[F]) = E
}

private given ErrorHandler[ServerLanguage] with
  private val dsl = Http4sDsl[ServerLanguage]

  import dsl.*

  private def problemDetailsHanlder[A: Detailer](
      domainPrefix: String,
      nec: NonEmptyChain[A]
  ) =
    for
      _ <- CompilerDSL.info(
        f"ERRORES DE DOMINIO ($domainPrefix): ${ProblemDetails.Multiple(nec.map(_.details)).asJson}"
      )
      response <-
        (if (nec.size == 1) {
           val details = nec.head.details
           (for
             code <- details.statusCode
             status <- Status.fromInt(code).toOption
           yield Response(
             status,
             headers = Headers("Content-Type" -> "application/json"),
             body = Stream.emits(details.asJson.spaces2.getBytes)
           ).pure[ServerLanguage]) getOrElse BadRequest(
             details.asJson
           )
         } else
           BadRequest(
             ProblemDetails.Multiple(nec.map(_.details)).asJson
           )
        )
    yield response

  private val handler
      : ServerError => ServerLanguage[Response[ServerLanguage]] = {
    case ServerError.DatabaseError(e) =>
      CompilerDSL.error(
        s"ERROR DE BASE DE DATOS: ${e.getMessage}"
      ) >> InternalServerError()
    case ServerError.FilesystemError(e) =>
      CompilerDSL.error(
        s"ERROR DE SISTEMA DE ARCHIVOS: ${e.getMessage}"
      ) >> InternalServerError()
    case ServerError.NotificationStreamClosed =>
      CompilerDSL.error(
        s"ERROR DE NOTIFICACIONES: LA CORRIENTE ESTA CERRADA"
      ) >> InternalServerError()
    case ServerError.ParseError(err) =>
      CompilerDSL.warn(f"ERROR DE SERIALIZACION: ${err.message}") >>
        err.toHttpResponse(HttpVersion.`HTTP/1.1`).pure
    case ServerError.HousesError(nec) => problemDetailsHanlder("CASA", nec)
    case ServerError.PeopleError(nec) => problemDetailsHanlder("NIÑOS", nec)

  }
  def handle(serverError: ServerError) = handler(serverError)
  def unhandled(e: Throwable) = e match {
    case e: MessageFailure =>
      CompilerDSL.error(
        s"ERROR DE CODIFICAION: ${e.message}"
      ) >> e.toHttpResponse(HttpVersion.`HTTP/1.1`).pure[ServerLanguage]
    case e =>
      CompilerDSL.error(s"ERROR DESCONOCIDO: $e") >> InternalServerError()
  }

given [F[_]: Monad](using E: Executor[F]): ErrorHandler[F] with
  def handle(e: ServerError) =
    ErrorHandler[ServerLanguage].handle(e).map(_.mapK(E.executor)).run
  def unhandled(e: Throwable) =
    ErrorHandler[ServerLanguage].unhandled(e).map(_.mapK(E.executor)).run
