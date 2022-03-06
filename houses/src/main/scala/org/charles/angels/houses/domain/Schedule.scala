package org.charles.angels.houses.domain

import cats.syntax.all.*
import cats.data.ValidatedNec
import cats.data.Chain
import java.time.LocalTime
import scala.concurrent.duration.FiniteDuration
import org.charles.angels.houses.domain.errors.ScheduleError
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoField
import org.charles.angels.houses.domain.events.ScheduleEvent
import monocle.Lens
import monocle.Optional
import monocle.macros.GenLens
import java.util.UUID

final case class Schedule private (
    id: UUID,
    monday: DaySchedule,
    tuesday: DaySchedule,
    wednesday: DaySchedule,
    thursday: DaySchedule,
    friday: DaySchedule
) { schedule =>
  private def addBlockOnDay(
      day: Lens[Schedule, DaySchedule],
      dayInt: Int,
      block: ScheduleBlock
  ) = day
    .andThen(Schedule.lastBlock)
    .set(block)
    .andThen(s =>
      ScheduleBlock(day.get(s)) *> s.validNec.product(
        ScheduleEvent.BlockAdded(id, dayInt, block.starts, block.lasts).validNec
      )
    )
    .apply(schedule)

  def addBlockOnMonday(block: ScheduleBlock) =
    addBlockOnDay(Schedule.monday, 1, block)
  def addBlockOnTuesday(block: ScheduleBlock) =
    addBlockOnDay(Schedule.tuesday, 2, block)
  def addBlockOnWednesday(block: ScheduleBlock) =
    addBlockOnDay(Schedule.wednesday, 3, block)
  def addBlockOnThursday(block: ScheduleBlock) =
    addBlockOnDay(Schedule.thursday, 4, block)
  def addBlockOnFriday(block: ScheduleBlock) =
    addBlockOnDay(Schedule.friday, 5, block)

  private def removeBlockOn(
      day: Lens[Schedule, DaySchedule],
      dayInd: Int,
      index: Int
  ) = day
    .get(schedule)
    .zipWithIndex
    .deleteFirst({ case (_, ind) =>
      ind == index
    })
    .toValidNec(ScheduleError.NonExistentBlock(dayInd, index))
    .unzip
    ._2
    .nested
    .map(_._1)
    .value
    .andThen(ScheduleBlock(_))
    .map(
      day.set(_)(schedule) *: ScheduleEvent
        .BlockRemoved(id, dayInd, index) *: EmptyTuple
    )

  def removeBlockOnMonday(index: Int) = removeBlockOn(Schedule.monday, 1, index)
  def removeBlockOnTuesday(index: Int) =
    removeBlockOn(Schedule.tuesday, 2, index)
  def removeBlockOnWednesday(index: Int) =
    removeBlockOn(Schedule.wednesday, 3, index)
  def removeBlockOnThursday(index: Int) =
    removeBlockOn(Schedule.thursday, 4, index)
  def removeBlockOnFriday(index: Int) = removeBlockOn(Schedule.friday, 5, index)

  private def updateBlock[A](
      focusDay: Lens[Schedule, DaySchedule],
      block: Lens[ScheduleBlock, A],
      evt: ScheduleEvent,
      dayInt: Int,
      value: A,
      blockIndex: Int
  ) = focusDay
    .andThen(Schedule.blockOn(blockIndex))
    .getOption(schedule)
    .toValidNec(ScheduleError.NonExistentBlock(dayInt, blockIndex))
    .andThen(
      block
        .set(value)
        .andThen(ScheduleBlock.validateBlock)
    )
    .map(focusDay.andThen(Schedule.blockOn(blockIndex)).set(_)(schedule))
    .product(evt.validNec)

  def setStartingHourOnMonday(key: Int, startingHour: Int) =
    updateBlock(
      Schedule.monday,
      ScheduleBlock.startHour,
      ScheduleEvent.StartHourUpdatedOnBlock(id, 1, key, startingHour),
      1,
      startingHour,
      key
    )
  def setStartingHourOnTuesday(key: Int, startingHour: Int) =
    updateBlock(
      Schedule.tuesday,
      ScheduleBlock.startHour,
      ScheduleEvent.StartHourUpdatedOnBlock(id, 2, key, startingHour),
      2,
      startingHour,
      key
    )
  def setStartingHourOnWednesday(key: Int, startingHour: Int) =
    updateBlock(
      Schedule.wednesday,
      ScheduleBlock.startHour,
      ScheduleEvent.StartHourUpdatedOnBlock(id, 3, key, startingHour),
      3,
      startingHour,
      key
    )
  def setStartingHourOnThursday(key: Int, startingHour: Int) =
    updateBlock(
      Schedule.thursday,
      ScheduleBlock.startHour,
      ScheduleEvent.StartHourUpdatedOnBlock(id, 4, key, startingHour),
      4,
      startingHour,
      key
    )
  def setStartingHourOnFriday(key: Int, startingHour: Int) =
    updateBlock(
      Schedule.friday,
      ScheduleBlock.startHour,
      ScheduleEvent.StartHourUpdatedOnBlock(id, 5, key, startingHour),
      5,
      startingHour,
      key
    )

  def setStartingMinuteOnMonday(key: Int, startingMinute: Int) =
    updateBlock(
      Schedule.monday,
      ScheduleBlock.startMinute,
      ScheduleEvent.StartMinuteUpdatedOnBlock(id, 1, key, startingMinute),
      1,
      startingMinute,
      key
    )
  def setStartingMinuteOnTuesday(key: Int, startingMinute: Int) =
    updateBlock(
      Schedule.tuesday,
      ScheduleBlock.startMinute,
      ScheduleEvent.StartMinuteUpdatedOnBlock(id, 2, key, startingMinute),
      2,
      startingMinute,
      key
    )
  def setStartingMinuteOnWednesday(key: Int, startingMinute: Int) =
    updateBlock(
      Schedule.wednesday,
      ScheduleBlock.startMinute,
      ScheduleEvent.StartMinuteUpdatedOnBlock(id, 3, key, startingMinute),
      3,
      startingMinute,
      key
    )
  def setStartingMinuteOnThursday(key: Int, startingMinute: Int) =
    updateBlock(
      Schedule.thursday,
      ScheduleBlock.startMinute,
      ScheduleEvent.StartMinuteUpdatedOnBlock(id, 4, key, startingMinute),
      4,
      startingMinute,
      key
    )
  def setStartingMinuteOnFriday(key: Int, startingMinute: Int) =
    updateBlock(
      Schedule.friday,
      ScheduleBlock.startMinute,
      ScheduleEvent.StartMinuteUpdatedOnBlock(id, 5, key, startingMinute),
      5,
      startingMinute,
      key
    )

  def setDurationHoursOnMonday(key: Int, durationHours: Int) =
    updateBlock(
      Schedule.monday,
      ScheduleBlock.durationHours,
      ScheduleEvent.DurationHoursUpdatedOnBlock(id, 1, key, durationHours),
      1,
      durationHours,
      key
    )
  def setDurationHoursOnTuesday(key: Int, durationHours: Int) =
    updateBlock(
      Schedule.tuesday,
      ScheduleBlock.durationHours,
      ScheduleEvent.DurationHoursUpdatedOnBlock(id, 2, key, durationHours),
      2,
      durationHours,
      key
    )
  def setDurationHoursOnWednesday(key: Int, durationHours: Int) =
    updateBlock(
      Schedule.wednesday,
      ScheduleBlock.durationHours,
      ScheduleEvent.DurationHoursUpdatedOnBlock(id, 3, key, durationHours),
      3,
      durationHours,
      key
    )
  def setDurationHoursOnThursday(key: Int, durationHours: Int) =
    updateBlock(
      Schedule.thursday,
      ScheduleBlock.durationHours,
      ScheduleEvent.DurationHoursUpdatedOnBlock(id, 4, key, durationHours),
      4,
      durationHours,
      key
    )
  def setDurationHoursOnFriday(key: Int, durationHours: Int) =
    updateBlock(
      Schedule.friday,
      ScheduleBlock.durationHours,
      ScheduleEvent.DurationHoursUpdatedOnBlock(id, 5, key, durationHours),
      5,
      durationHours,
      key
    )

  def setDurationMinutesOnMonday(key: Int, durationMinutes: Int) =
    updateBlock(
      Schedule.monday,
      ScheduleBlock.durationMinutes,
      ScheduleEvent.DurationHoursUpdatedOnBlock(id, 1, key, durationMinutes),
      1,
      durationMinutes,
      key
    )
  def setDurationMinutesOnTuesday(key: Int, durationMinutes: Int) =
    updateBlock(
      Schedule.tuesday,
      ScheduleBlock.durationMinutes,
      ScheduleEvent.DurationHoursUpdatedOnBlock(id, 2, key, durationMinutes),
      2,
      durationMinutes,
      key
    )
  def setDurationMinutesOnWednesday(key: Int, durationMinutes: Int) =
    updateBlock(
      Schedule.wednesday,
      ScheduleBlock.durationMinutes,
      ScheduleEvent.DurationHoursUpdatedOnBlock(id, 3, key, durationMinutes),
      3,
      durationMinutes,
      key
    )
  def setDurationMinutesOnThursday(key: Int, durationMinutes: Int) =
    updateBlock(
      Schedule.thursday,
      ScheduleBlock.durationMinutes,
      ScheduleEvent.DurationHoursUpdatedOnBlock(id, 4, key, durationMinutes),
      4,
      durationMinutes,
      key
    )
  def setDurationMinutesOnFriday(key: Int, durationMinutes: Int) =
    updateBlock(
      Schedule.friday,
      ScheduleBlock.durationMinutes,
      ScheduleEvent.DurationHoursUpdatedOnBlock(id, 5, key, durationMinutes),
      5,
      durationMinutes,
      key
    )
}

object Schedule:
  private def monday =
    GenLens[Schedule](_.monday)
  private def tuesday =
    GenLens[Schedule](_.tuesday)
  private def wednesday =
    GenLens[Schedule](_.wednesday)
  private def thursday =
    GenLens[Schedule](_.thursday)
  private def friday =
    GenLens[Schedule](_.friday)

  private def lastBlock =
    Optional[DaySchedule, ScheduleBlock](_.lastOption)(block => s => s :+ block)

  private def blockOn(index: Int) =
    Optional[DaySchedule, ScheduleBlock](_.get(index)) { b => s =>
      s.zipWithIndex.flatMap { (b1, i) =>
        if (i == index) Chain(b) else Chain(b1)
      }
    }

  def apply(
      monday: RawDaySchedule,
      tuesday: RawDaySchedule,
      wednesday: RawDaySchedule,
      thursday: RawDaySchedule,
      friday: RawDaySchedule
  ) = (
    monday.traverse(ScheduleBlock(_)).andThen(ScheduleBlock(_)),
    tuesday.traverse(ScheduleBlock(_)).andThen(ScheduleBlock(_)),
    wednesday.traverse(ScheduleBlock(_)).andThen(ScheduleBlock(_)),
    thursday.traverse(ScheduleBlock(_)).andThen(ScheduleBlock(_)),
    friday.traverse(ScheduleBlock(_)).andThen(ScheduleBlock(_))
  )
    .mapN(new Schedule(UUID.randomUUID, _, _, _, _, _))
    .map(s =>
      (
        s,
        ScheduleEvent.ScheduleCreated(
          s.id,
          s.monday,
          s.tuesday,
          s.wednesday,
          s.thursday,
          s.friday
        )
      )
    )
