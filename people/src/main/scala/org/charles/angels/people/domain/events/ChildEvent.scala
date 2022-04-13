package org.charles.angels.people.domain.events

import org.charles.angels.people.domain.PersonalInformation
import org.charles.angels.people.domain.Wear
import java.io.File
import org.charles.angels.people.domain.ChildInformation
import java.util.UUID

enum ChildEvent {
  case ChildCreated(
      houseId: UUID,
      childInformation: ChildInformation,
      wear: Wear,
      id: UUID
  )
  case PersonalInformationUpdated(
      ci: Int,
      personalInformation: PersonalInformation,
      isOfChild: Boolean = false
  )
  case FatherInformationUpdated(
      ci: Int,
      personalInformation: PersonalInformation
  )
  case MotherInformationUpdated(
      ci: Int,
      personalInformation: PersonalInformation
  )
  case NonParentInformationUpdated(
      ci: Int,
      personalInformation: PersonalInformation
  )
  case RelatedBeneficiaryAdded(information: PersonalInformation)
  case RelatedBeneficiaryRemoved(ci: Int)
  case RelatedBeneficiaryUpdated(ci: Int, information: PersonalInformation)
  case PhotoUpdated(id: UUID, img: File)
  case AttireUpdated(id: UUID, wear: Wear)
  case BoyDeleted(id: UUID)
  case GirlDeleted(id: UUID)
}
