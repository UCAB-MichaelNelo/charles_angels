package org.charles.angels.houses.application.models

import org.charles.angels.houses.domain.RawScheduleBlock
import cats.data.Chain

final case class ScheduleModel(
    monday: Chain[RawScheduleBlock],
    tuesday: Chain[RawScheduleBlock],
    wednesday: Chain[RawScheduleBlock],
    thursday: Chain[RawScheduleBlock],
    friday: Chain[RawScheduleBlock]
)
