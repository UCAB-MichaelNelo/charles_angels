package org.charles.angels.houses.http.models.forms

import org.http4s.EntityDecoder
import cats.syntax.all.*
import cats.effect.kernel.Concurrent
import cats.Parallel
import cats.data.EitherT
import org.http4s.DecodeResult
import fs2.Stream
import cats.effect.std.Console
import java.time.LocalTime

final case class HouseForm(
    fileExtension: String,
    name: String,
    rif: Int,
    phones: Vector[String],
    address: String,
    maxShares: Int,
    currentShares: Int,
    minimumAge: Int,
    maximumAge: Int,
    currentGirlsHelped: Int,
    currentBoysHelped: Int,
    scheduleStartTime: LocalTime,
    scheduleEndTime: LocalTime
)