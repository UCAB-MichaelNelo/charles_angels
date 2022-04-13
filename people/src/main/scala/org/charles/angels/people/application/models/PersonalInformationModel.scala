package org.charles.angels.people.application.models

import java.time.LocalDate
import org.charles.angels.people.domain.PersonalInformation

final case class PersonalInformationModel(
    ci: Int,
    name: String,
    lastname: String,
    birthdate: LocalDate
) {
  def toPersonalInformation =
    PersonalInformation.unsafe(ci, name, lastname, birthdate)
}
