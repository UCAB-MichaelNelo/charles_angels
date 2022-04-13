package org.charles.angels.houses.http.models.forms

import cats.syntax.all.*
import org.http4s.multipart.Part
import cats.Monad
import cats.effect.kernel.Concurrent
import fs2.RaiseThrowable
import fs2.Stream
import org.http4s.DecodeResult
import scala.reflect.ClassTag
import cats.Functor
import java.util.UUID
import org.http4s.InvalidMessageBodyFailure
import java.time.LocalDate
import scala.util.Try
import cats.data.EitherT

final case class FilePart[F[_]](
    name: String,
    stream: Stream[F, Byte]
)

trait PartField[F[_], A] {
  def extract(parts: Vector[Part[F]], fieldName: String): DecodeResult[F, A]
}

object PartField {
  def apply[F[_], A](using pf: PartField[F, A]) = pf
  def field[F[_], A](subField: String)(using pf: PartField[F, A]) =
    new PartField[F, A] {
      def extract(parts: Vector[Part[F]], fieldName: String) =
        pf.extract(parts, f"$fieldName[$subField]")
    }

  given [F[_]: Monad: RaiseThrowable: Concurrent]: PartField[F, String] with
    def extract(parts: Vector[Part[F]], fieldName: String) =
      parts
        .find(_.name == fieldName.some)
        .toOptionT
        .toRight(missingField(fieldName))
        .semiflatMap(_.bodyText.compile.string)

  given [F[_]: Monad: RaiseThrowable: Concurrent]: PartField[F, UUID] with
    def extract(parts: Vector[Part[F]], fieldName: String) =
      parts
        .find(_.name == fieldName.some)
        .toOptionT
        .toRight(missingField(fieldName))
        .semiflatMap(_.bodyText.compile.string)
        .subflatMap(s =>
          Either
            .catchNonFatal(UUID.fromString(s))
            .leftMap(_ => InvalidMessageBodyFailure(s"$s is no valid UUID"))
        )

  given [F[_]: Monad: RaiseThrowable: Concurrent]: PartField[F, Int] with
    def extract(parts: Vector[Part[F]], fieldName: String) =
      PartField[F, String].extract(parts, fieldName).flatMap {
        _.toIntOption.toRight(invalidIntField(fieldName)).toEitherT
      }

  given [F[_]: Monad: RaiseThrowable: Concurrent]: PartField[F, LocalDate] with
    def extract(parts: Vector[Part[F]], fieldName: String) =
      PartField[F, String]
        .extract(parts, fieldName)
        .flatMap { s =>
          Either
            .catchNonFatal(
              LocalDate.parse(s)
            )
            .leftMap(_ => InvalidMessageBodyFailure(s"$s is an invalid Date"))
            .toEitherT
        }

  given [F[_]: Monad: RaiseThrowable: Concurrent]: PartField[F, Stream[F, Byte]]
    with
    def extract(parts: Vector[Part[F]], fieldName: String) =
      parts
        .find(_.name == fieldName.some)
        .toOptionT
        .toRight(missingField(fieldName))
        .map(_.body)

  given [F[_]: Monad: RaiseThrowable: Concurrent]: PartField[F, FilePart[F]]
    with
    def extract(parts: Vector[Part[F]], fieldName: String) =
      parts
        .find(_.name == fieldName.some)
        .toOptionT
        .subflatMap(part => part.filename product part.body.pure)
        .toRight(missingField(fieldName))
        .map { case (name, body) => FilePart(name, body) }

  given [F[_]: Monad: RaiseThrowable: Concurrent, A: PartField[F, _]: ClassTag]
      : PartField[F, Vector[A]] with
    def extract(parts: Vector[Part[F]], fieldName: String) =
      parts
        .filter(part =>
          (part.name == fieldName.some) || (part.name
            .filter(_.contains(fieldName))
            .isDefined)
        )
        .map(part => PartField[F, A].extract(Vector(part), fieldName))
        .sequence

  given [F[_]: Monad: RaiseThrowable: Concurrent, A: PartField[F, _]: ClassTag]
      : PartField[F, Option[A]] = PartField[F, A].option

  given [F[_]: Monad]: Monad[PartField[F, _]] with
    def tailRecM[A, B](
        a: A
    )(f: A => PartField[F, Either[A, B]]): PartField[F, B] =
      new PartField[F, B] {
        def extract(parts: Vector[Part[F]], fieldName: String) =
          f(a).extract(parts, fieldName).flatMap { result =>
            result match
              case Right(b) => DecodeResult.successT(b)
              case Left(fa) =>
                fa.tailRecM[DecodeResult[F, _], B](
                  f(_).extract(parts, fieldName)
                )
          }
      }
    def pure[A](a: A) = new PartField[F, A] {
      def extract(parts: Vector[Part[F]], fieldName: String) = a.pure
    }
    def flatMap[A, B](fa: PartField[F, A])(f: A => PartField[F, B]) =
      new PartField[F, B]:
        def extract(parts: Vector[Part[F]], fieldName: String) = for
          res1 <- fa.extract(parts, fieldName)
          e = f(res1)
          res2 <- e.extract(parts, fieldName)
        yield res2

}
