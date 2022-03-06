package org.charles.angels.houses.domain.errors

import scala.concurrent.duration.FiniteDuration
import java.time.LocalTime
import org.charles.angels.houses.domain.ScheduleBlock

enum ScheduleError:
  case InvalidHour(hour: Int)
  case InvalidMinute(minute: Int)
  case StartTimeAndDurationOutlastsADay(
      start: LocalTime,
      duration: FiniteDuration
  )
  case DurationGreaterThanADay(duration: FiniteDuration)
  case NoDurationProvided
  case IntersectingBlocks(block1: ScheduleBlock, block2: ScheduleBlock)
  case NonExistentBlock(day: Int, position: Int)
