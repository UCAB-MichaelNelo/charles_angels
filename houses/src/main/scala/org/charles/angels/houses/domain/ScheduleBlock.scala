package org.charles.angels.houses.domain

import cats.syntax.all.*
import cats.data.ValidatedNec
import cats.data.Chain
import java.time.LocalTime
import scala.concurrent.duration.FiniteDuration
import org.charles.angels.houses.domain.errors.ScheduleError
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoField
import monocle.Lens

final case class RawScheduleBlock(
    startHour: Int,
    startMinute: Int,
    durationHours: Int,
    durationMinutes: Int
)

private type RawDaySchedule = Chain[RawScheduleBlock]

final case class ScheduleBlock private[domain] (
    starts: LocalTime,
    lasts: FiniteDuration
) {
  private def has(time: LocalTime) =
    time.isAfter(starts) && time.isBefore(ends)
  def ends = starts.plus(lasts.length, lasts.unit.toChronoUnit)
  def intersectsWith(block: ScheduleBlock) =
    has(block.starts) || block.has(starts)
}

type DaySchedule = Chain[ScheduleBlock]

object ScheduleBlock:
  object Hour:
    def apply(hour: Int): ValidatedNec[ScheduleError, Int] =
      (if (hour <= 24) hour.validNec
       else ScheduleError.InvalidHour(hour).invalidNec)

  object Minute:
    def apply(minute: Int): ValidatedNec[ScheduleError, Int] =
      (if (minute <= 60) minute.validNec
       else ScheduleError.InvalidMinute(minute).invalidNec)

  private[domain] def startHour =
    Lens[ScheduleBlock, Int](_.starts.getHour)(h =>
      b => b.copy(starts = b.starts.withHour(h))
    )
  private[domain] def startMinute =
    Lens[ScheduleBlock, Int](_.starts.getMinute)(m =>
      b => b.copy(starts = b.starts.withMinute(m))
    )
  private[domain] def durationHours =
    Lens[ScheduleBlock, Long](_.lasts.toHours % 24)(h =>
      b =>
        b.copy(lasts =
          FiniteDuration(h, "h") + FiniteDuration(b.lasts.toMinutes % 60, "m")
        )
    )
  private[domain] def durationMinutes =
    Lens[ScheduleBlock, Long](_.lasts.toMinutes % 60)(m =>
      b =>
        b.copy(lasts =
          FiniteDuration(b.lasts.toHours % 24, "h") + FiniteDuration(m, "m")
        )
    )

  private[domain] def validateBlock(record: ScheduleBlock) = if (
    record.ends.isBefore(record.starts)
  )
    ScheduleError
      .StartTimeAndDurationOutlastsADay(
        record.starts,
        record.lasts
      )
      .invalidNec
  else
    record.validNec

  def apply(day: Chain[ScheduleBlock]) = (
    for
      b1 <- day
      b2 <- day
    yield
      if (b1.intersectsWith(b2)) ().validNec
      else ScheduleError.IntersectingBlocks(b1, b2).invalidNec
  ).sequence *> day.validNec

  def apply(rawSchedule: RawScheduleBlock) = {
    val duration = (
      Hour(rawSchedule.durationHours),
      Minute(rawSchedule.durationMinutes)
    ).mapN {
      FiniteDuration(_, "h") + FiniteDuration(_, "m")
    }
    val startTime = (
      Hour(rawSchedule.startHour),
      Minute(rawSchedule.startMinute)
    ).mapN {
      LocalTime.of(_, _)
    }

    (duration product startTime).andThen {
      case (duration, startTime) => {
        (if (duration < FiniteDuration(24, "h"))
           duration.validNec[ScheduleError]
         else
           ScheduleError
             .DurationGreaterThanADay(duration)
             .invalidNec[ScheduleBlock]) *>
          (if (duration.length > 0) duration.validNec[ScheduleError]
           else
             ScheduleError.NoDurationProvided
               .invalidNec[ScheduleBlock]) *>
          validateBlock(new ScheduleBlock(startTime, duration))
      }
    }
  }
