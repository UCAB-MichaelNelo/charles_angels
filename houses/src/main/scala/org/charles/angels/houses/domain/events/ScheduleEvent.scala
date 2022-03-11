package org.charles.angels.houses.domain.events

import org.charles.angels.houses.domain.DaySchedule
import scala.concurrent.duration.FiniteDuration
import java.time.LocalTime
import java.util.UUID

enum ScheduleEvent:
  case ScheduleCreated(
      id: UUID,
      monday: DaySchedule,
      tuesday: DaySchedule,
      wednesday: DaySchedule,
      thursday: DaySchedule,
      friday: DaySchedule
  )
  case BlockAdded(
      id: UUID,
      day: Int,
      startTime: LocalTime,
      duration: FiniteDuration
  )
  case BlockRemoved(id: UUID, day: Int, blockKey: Int)
  case StartHourUpdatedOnBlock(
      id: UUID,
      day: Int,
      blockKey: Int,
      newStartHour: Int
  )
  case StartMinuteUpdatedOnBlock(
      id: UUID,
      day: Int,
      blockKey: Int,
      newStartMinute: Int
  )
  case DurationHoursUpdatedOnBlock(
      id: UUID,
      day: Int,
      blockKey: Int,
      newDurationHours: Int
  )
  case DurationMinutesUpdatedOnBlock(
      id: UUID,
      day: Int,
      blockKey: Int,
      newDurationMinutes: Int
  )
  case ScheduleDeleted(id: UUID)
