package org.charles.angels.houses.reports.models

import java.util.UUID

final case class ChildInformation(
    id: UUID,
    name: String,
    ci: Int
)

final case class ChildWithFamily(
    information: ChildInformation,
    family: Vector[ChildInformation]
)

final case class HouseWithChildrenAndFamily(
    houseId: UUID,
    houseName: String,
    children: Vector[ChildWithFamily]
)
