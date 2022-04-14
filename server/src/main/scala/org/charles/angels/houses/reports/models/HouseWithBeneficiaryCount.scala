package org.charles.angels.houses.reports.models

import org.charles.angels.houses.domain.House
import java.util.UUID

final case class BeneficiaryCount(
  beneficiaryCount: Int
)

final case class HouseWithBeneficiaryCount(
  houseId: UUID,
  beneficiaryCount: BeneficiaryCount
)
