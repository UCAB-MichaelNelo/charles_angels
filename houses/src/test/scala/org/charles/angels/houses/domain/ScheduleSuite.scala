package org.charles.angels.houses.domain

import munit.ScalaCheckSuite
import cats.syntax.all.*
import org.scalacheck.Prop.*
import org.scalacheck.*
import org.charles.angels.houses.domain.ScheduleGenerator
import org.charles.angels.houses.domain.RawScheduleBlock
import org.charles.angels.houses.domain.ScheduleBlock
import cats.data.Chain
import org.scalacheck.Shrink
import scala.concurrent.duration.FiniteDuration

class ScheduleSuite extends ScalaCheckSuite with ScheduleGenerator {
  private def startMinutes(rb: RawScheduleBlock) =
    rb.startHour * 60 + rb.startMinute
  private def minutes(rb: RawScheduleBlock) =
    rb.durationHours * 60 + rb.durationMinutes
  property("ScheduleBlock is validated") {
    forAll(validRawBlockGenerator) { (rawBlock: RawScheduleBlock) =>
      val result = ScheduleBlock(rawBlock)
      (minutes(rawBlock) + startMinutes(rawBlock) < 1440 && minutes(
        rawBlock
      ) > 0 && startMinutes(rawBlock) > 0) ==> (
        ("ScheduleBlock creation is succesfull" |: result.isValid) &&
          ("Duration matches in minutes" |: minutes(rawBlock).validNec == result
            .map(
              _.lasts.toMinutes
            )) &&
          ("LocalTime matches in hours" |: rawBlock.startHour.validNec == result
            .map(_.starts.getHour)) &&
          ("LocalTime matches in minutes" |: rawBlock.startMinute.validNec == result
            .map(_.starts.getMinute))
      )
    }
  }
  property("ScheduleBlock array is validated for collisions") {
    forAll(validBlockGenerator, validBlockGenerator) {
      (b1: ScheduleBlock, b2: ScheduleBlock) =>
        (b1.intersectsWith(b2) || b2.intersectsWith(b1)) ==> (
          ("Array of Intersecting block is invalid" |: ScheduleBlock(
            Chain(b1, b2)
          ).isInvalid)
        )
    }
  }
}
