package org.charles.angels.houses.application.errors

import org.charles.angels.houses.domain.errors.HouseError
import org.charles.angels.houses.domain.errors.ContactError
import java.util.UUID

enum ApplicationError:
  case HouseDomainError(error: HouseError)
  case ContactDomainError(error: ContactError)
  case HouseNotFoundError(id: UUID)
  case ContactNotFoundError(id: Int)
  case ScheduleNotFoundError(id: UUID)
  case ExistingRif(rif: Int)
