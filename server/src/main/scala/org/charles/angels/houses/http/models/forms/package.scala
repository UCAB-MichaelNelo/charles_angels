package org.charles.angels.houses.http.models
import cats.syntax.all.*
import org.http4s.DecodeResult
import org.http4s.MalformedMessageBodyFailure
import org.http4s.DecodeFailure
import org.http4s.multipart.Part
import cats.Monad
import fs2.RaiseThrowable
import cats.effect.kernel.Concurrent
import cats.data.EitherT
import cats.Applicative
import scala.reflect.ClassTag

package object forms {
  def missingField(fieldName: String): DecodeFailure =
    MalformedMessageBodyFailure(
      f"'$fieldName' field not provided"
    )

  def invalidIntField(fieldName: String): DecodeFailure =
    MalformedMessageBodyFailure(
      f"'$fieldName' is not a valid int"
    )

  extension [F[_]: Monad: RaiseThrowable: Concurrent](parts: Vector[Part[F]])
    def field[A: PartField[F, _]](name: String) =
      PartField[F, A].extract(parts, name)

  extension [F[_]: Monad: RaiseThrowable: Concurrent, A: PartField[
    F,
    _
  ]: ClassTag](field: PartField[F, A])
    def option = new PartField[F, Option[A]] {
      def extract(parts: Vector[Part[F]], fieldName: String) =
        field
          .extract(
            parts
              .filter(part =>
                (part.name == fieldName.some) || (part.name
                  .filter(_.contains(fieldName))
                  .isDefined)
              ),
            fieldName
          )
          .map(_.some)
          .handleErrorWith {
            case _: MalformedMessageBodyFailure => None.pure
            case e                              => e.raiseError
          }
    }
}
