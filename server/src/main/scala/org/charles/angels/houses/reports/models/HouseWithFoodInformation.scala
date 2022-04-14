package org.charles.angels.houses.reports.models

import org.charles.angels.houses.domain.House
import java.util.UUID

final case class FoodInformation(
  foodAmountNeeded: Int,
  allergies: Map[UUID, Vector[String]]
)

final case class HouseWithFoodInformation(
  houseId: UUID,
  foodInformation: FoodInformation
)
