package org.charles.angels.houses.domain.events

import java.io.File
import java.util.UUID

enum HouseEvent:
  case HouseCreated(
      id: UUID,
      img: File,
      name: String,
      rif: Int,
      phones: Vector[String],
      address: String,
      maxShares: Int,
      currentShares: Int,
      minimumAge: Int,
      maximumAge: Int,
      currentGirlsHelped: Int,
      currentBoysHelped: Int,
      contactCI: Int,
      scheduleId: UUID
  )
  case ImageUpdated(id: UUID, newImg: File)
  case NameUpdated(id: UUID, newName: String)
  case RIFUpdated(id: UUID, newRif: Int)
  case PhoneAdded(id: UUID, newPhone: String)
  case PhoneRemoved(id: UUID, key: Int)
  case PhoneUpdated(id: UUID, key: Int, newPhone: String)
  case MaxSharesUpdated(id: UUID, newMaxShares: Int)
  case CurrentSharesUpdated(id: UUID, newCurrentShares: Int)
  case MinimumAgeUpdated(id: UUID, newMinimumAge: Int)
  case MaximumAgeUpdateed(id: UUID, newMaximumAge: Int)
  case CurrentGirlsHelpedUpdated(id: UUID, newCurrentGirlsHelped: Int)
  case CurrentBoysHelpedUpdated(id: UUID, newCurrentBoysHelped: Int)
