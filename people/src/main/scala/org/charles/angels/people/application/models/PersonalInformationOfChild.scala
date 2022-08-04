package org.charles.angels.people.application.models

import org.charles.angels.people.domain.PersonalInformation
import java.util.UUID

final case class PersonalInformationOfChild(
    information: PersonalInformation,
    childId: UUID
)
