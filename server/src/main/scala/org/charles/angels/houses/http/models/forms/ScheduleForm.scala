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