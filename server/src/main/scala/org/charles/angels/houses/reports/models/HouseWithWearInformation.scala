package org.charles.angels.houses.reports.models

import org.charles.angels.houses.domain.House
import java.util.UUID

final case class WearInformation(
  shortOrTrousersNeededAmountBySize: Map[Int, Int],
  tshirtOrShirtNeededAmountBySize: Map[Int, Int],
  footwearNeededAmountBySize: Map[Int, Int],
  sweaterNeededAmountBySize: Map[Int, Int],
  dressNeededAmountBySize: Map[Int, Int],
)

final case class HouseWithWearInformation(
  houseId: UUID,
  wearInformation: WearInformation
)
