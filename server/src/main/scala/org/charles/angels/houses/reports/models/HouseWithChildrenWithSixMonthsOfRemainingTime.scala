package org.charles.angels.houses.reports.models

import java.util.UUID
import java.time.LocalDate

final case class ChildWithSixMonthsOfRemainingTime(id: UUID, name: String, ci: Int, birthdate: LocalDate, maxAge: Int)

final case class HouseWithChildrenWithSixMonthsOfRemainingTime(
    houseId: UUID,
    houseName: String,
    children: Vector[ChildWithSixMonthsOfRemainingTime]
)
