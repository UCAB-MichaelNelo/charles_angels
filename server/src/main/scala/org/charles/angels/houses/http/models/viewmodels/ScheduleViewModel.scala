package org.charles.angels.houses.http.models.viewmodels

import io.circe.Encoder
import io.circe.generic.semiauto
import org.charles.angels.houses.domain.ScheduleBlock
import org.charles.angels.houses.domain.Schedule

final case class ScheduleBlockViewModel(
    starts: String,
    lasts: Long
)

object ScheduleBlockViewModel:
  def apply(b: ScheduleBlock) =
    new ScheduleBlockViewModel(b.starts.toString, b.lasts.toMinutes)

final case class ScheduleViewModel(
    monday: Array[ScheduleBlockViewModel],
    tuesday: Array[ScheduleBlockViewModel],
    wednesday: Array[ScheduleBlockViewModel],
    thursday: Array[ScheduleBlockViewModel],
    friday: Array[ScheduleBlockViewModel]
)

object ScheduleViewModel:
  def apply(s: Schedule) =
    new ScheduleViewModel(
      s.monday.map(ScheduleBlockViewModel.apply).toVector.toArray,
      s.tuesday.map(ScheduleBlockViewModel.apply).toVector.toArray,
      s.wednesday.map(ScheduleBlockViewModel.apply).toVector.toArray,
      s.thursday.map(ScheduleBlockViewModel.apply).toVector.toArray,
      s.friday.map(ScheduleBlockViewModel.apply).toVector.toArray
    )

given Encoder[ScheduleBlockViewModel] = semiauto.deriveEncoder
