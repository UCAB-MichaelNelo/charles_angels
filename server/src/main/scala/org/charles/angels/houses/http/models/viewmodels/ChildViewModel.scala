package org.charles.angels.houses.http.models.viewmodels

import cats.implicits.*
import org.charles.angels.people.domain.Child
import java.util.UUID
import org.charles.angels.people.domain.PersonalInformation

final case class AttireViewModel(
    shortOrTrousersSize: Int,
    tshirtOrshirtSize: Int,
    sweaterSize: Option[Int],
    dressSize: Option[Int],
    footwearSize: Int
)

final case class ChildViewModel(
    id: UUID,
    sex: String,
    information: PersonalInformation,
    mother: Option[PersonalInformation],
    father: Option[PersonalInformation],
    nonParent: Option[PersonalInformation],
    relBen: List[PersonalInformation],
    attire: AttireViewModel
)

object ChildViewModel {
  def apply(child: Child) = child match {
    case Child.Boy(id, ci, attire) =>
      new ChildViewModel(
        id,
        "M",
        ci.information,
        ci.mother,
        ci.father,
        ci.nonParent,
        ci.relatedBeneficiaries.values.toList,
        AttireViewModel(
          attire.shortOrTrousersSize,
          attire.tshirtOrshirtSize,
          attire.sweaterSize.some,
          None,
          attire.footwearSize
        )
      )
    case Child.Girl(id, ci, attire) =>
      new ChildViewModel(
        id,
        "F",
        ci.information,
        ci.mother,
        ci.father,
        ci.nonParent,
        ci.relatedBeneficiaries.values.toList,
        AttireViewModel(
          attire.shortOrTrousersSize,
          attire.tshirtOrshirtSize,
          None,
          attire.dressSize.some,
          attire.footwearSize
        )
      )
  }
}
