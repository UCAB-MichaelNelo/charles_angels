package org.charles.angels.houses.domain.events

import java.io.File
import java.time.LocalTime
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
                     scheduleStartTime: LocalTime,
                     scheduleEndTime: LocalTime
                   )
  case ImageUpdated(id: UUID, newImg: File)
  case NameUpdated(id: UUID, newName: String)
  case AddressUpdated(id: UUID, newAddress: String)
  case RIFUpdated(id: UUID, newRif: Int)
  case PhoneAdded(id: UUID, newPhone: String)
  case PhoneRemoved(id: UUID, key: Int)
  case PhoneUpdated(id: UUID, key: Int, newPhone: String)
  case MaxSharesUpdated(id: UUID, newMaxShares: Int)
  case CurrentSharesUpdated(id: UUID, newCurrentShares: Int)
  case MinimumAgeUpdated(id: UUID, newMinimumAge: Int)
  case MaximumAgeUpdated(id: UUID, newMaximumAge: Int)
  case CurrentGirlsHelpedUpdated(id: UUID, newCurrentGirlsHelped: Int)
  case CurrentBoysHelpedUpdated(id: UUID, newCurrentBoysHelped: Int)
  case ScheduleStartTimeUpdated(id: UUID, startTime: LocalTime)
  case ScheduleEndTimeUpdated(id: UUID, endTime: LocalTime)
  case HouseContactCIUpdated(id: UUID, ci: Int)
  case HouseDeleted(id: UUID)