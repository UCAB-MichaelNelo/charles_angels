package org.charles.angels.houses.http.models.forms

import cats.syntax.all.*
import org.http4s.EntityDecoder
import cats.effect.kernel.Concurrent
import cats.Parallel
import org.http4s.DecodeResult
import org.http4s.multipart.Part

final case class RawScheduleForm(
    startHour: Int,
    startMinute: Int,
    durationHours: Int,
    durationMinutes: Int
)

final case class ScheduleForm(
    monday: Vector[RawScheduleForm],
    tuesday: Vector[RawScheduleForm],
    wednesday: Vector[RawScheduleForm],
    thursday: Vector[RawScheduleForm],
    friday: Vector[RawScheduleForm]
)

given [F[_]: Concurrent: Parallel]: EntityDecoder[F, ScheduleForm] =
  EntityDecoder.multipart
    .map { m =>
      def rawScheduleOfDay(day: String) = (
        m.parts.field[Vector[Int]](f"$day[][startHour]"),
        m.parts.field[Vector[Int]](f"$day[][startMinute]"),
        m.parts.field[Vector[Int]](f"$day[][durationHours]"),
        m.parts.field[Vector[Int]](f"$day[][durationMinutes]")
      ).mapN((sH, sM, dH, dM) =>
        (sH, sM, dH, dM).parMapN(RawScheduleForm.apply)
      )

      (
        rawScheduleOfDay("monday"),
        rawScheduleOfDay("tuesday"),
        rawScheduleOfDay("wednesday"),
        rawScheduleOfDay("thursday"),
        rawScheduleOfDay("friday")
      ).parMapN(ScheduleForm.apply)
    }
    .flatMapR(result => DecodeResult(result.value))
