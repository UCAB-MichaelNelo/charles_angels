package org.charles.angels.houses.http.models.forms

import org.http4s.EntityDecoder
import cats.syntax.all.*
import cats.effect.kernel.Concurrent
import cats.Parallel
import cats.data.EitherT
import org.http4s.DecodeResult
import fs2.Stream

final case class HouseForm[F[_]](
    fs: Stream[F, Byte],
    name: String,
    rif: Int,
    phones: Vector[String],
    address: String,
    maxShares: Int,
    currentShares: Int,
    minimumAge: Int,
    maximumAge: Int,
    currentGirlsHelped: Int,
    currentBoysHelped: Int
)

given [F[_]: Concurrent: Parallel]: EntityDecoder[F, HouseForm[F]] =
  EntityDecoder.multipart
    .map { m =>
      (
        m.parts.field[Stream[F, Byte]]("image"),
        m.parts.field[String]("house_name"),
        m.parts.field[Int]("rif"),
        m.parts.field[Vector[String]]("phones[]"),
        m.parts.field[String]("address"),
        m.parts.field[Int]("maxShares"),
        m.parts.field[Int]("currentShares"),
        m.parts.field[Int]("minimumAge"),
        m.parts.field[Int]("maximumAge"),
        m.parts.field[Int]("currentGirlsHelped"),
        m.parts.field[Int]("currentBoysHelped")
      ).parMapN(HouseForm.apply)
    }
    .flatMapR(result => DecodeResult(result.value))
