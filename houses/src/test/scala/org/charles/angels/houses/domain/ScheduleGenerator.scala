package org.charles.angels.houses.domain

import org.scalacheck.Gen
import org.charles.angels.houses.domain.ScheduleBlock
import java.time.LocalTime
import scala.concurrent.duration.FiniteDuration
import org.scalacheck.Arbitrary
import org.scalacheck.Shrink

trait ScheduleGenerator {
  val validHourGenerator: Gen[Int] =
    for hour: Int <- Gen.oneOf(0 to 23)
    yield hour

  val validMinuteGenerator: Gen[Int] =
    for minute: Int <- Gen.oneOf(0 to 59)
    yield minute

  val validRawBlockGenerator = for
    startingHour <- validHourGenerator
    startingMinute <- validMinuteGenerator
    durationHours <- validHourGenerator
    durationMinutes <- validMinuteGenerator
  yield RawScheduleBlock(
    startingHour,
    startingMinute,
    durationHours,
    durationMinutes
  )

  val validBlockGenerator = for
    startingHour <- validHourGenerator
    startingMinute <- validMinuteGenerator
    durationHours <- validHourGenerator
    durationMinutes <- validMinuteGenerator
    block = ScheduleBlock(
      LocalTime.of(startingHour, durationHours),
      FiniteDuration(durationHours * 60 + durationMinutes, "m")
    )
  yield block
}
