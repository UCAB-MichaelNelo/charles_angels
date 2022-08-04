package org.charles.angels.houses.reports.models

import org.charles.angels.houses.domain.House
import java.util.UUID

final case class FoodAmountNeeded(
  foodAmountNeeded: Int
)

final case class HouseWithFoodAmountNeeded(
  houseId: UUID,
  houseName: String,
  foodAmountNeeded: FoodAmountNeeded
)
