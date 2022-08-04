package org.charles.angels.houses.reports.models

import org.charles.angels.houses.domain.House
import java.util.UUID

final case class HouseWearSizeEntry(
  id: UUID,
  size: Int,
  amount: Int
)

final case class WearInformation(
  shortOrTrousersSize: Option[Int],
  shortOrTrousersAmount: Option[Int],
  tshirtOrShirtSize: Option[Int],
  tshirtOrShirtAmount: Option[Int],
  sweaterSize: Option[Int],
  sweaterAmount: Option[Int],
  dressSize: Option[Int],
  dressAmount: Option[Int],
  footwearSize: Option[Int],
  footwearAmount: Option[Int]
)

final case class HouseWithWearInformation(
  houseName: String,
  houseId: UUID,
  wearInformation: Vector[WearInformation]
)
