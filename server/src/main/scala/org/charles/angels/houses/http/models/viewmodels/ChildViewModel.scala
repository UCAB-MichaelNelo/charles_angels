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
    houseId: Option[UUID],
    id: UUID,
    sex: String,
    information: PersonalInformation,
    mother: Option[PersonalInformation],
    father: Option[PersonalInformation],
    nonParent: Option[PersonalInformation],
    relBen: Vector[UUID],
    attire: AttireViewModel
)

object ChildViewModel {
  def apply(child: Child, houseId: Option[UUID] = None) = child match {
    case Child.Boy(id, ci, attire) =>
      new ChildViewModel(
        houseId,
        id,
        "M",
        ci.information,
        ci.mother,
        ci.father,
        ci.nonParent,
        ci.relatedBeneficiaries,
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
        houseId,
        id,
        "F",
        ci.information,
        ci.mother,
        ci.father,
        ci.nonParent,
        ci.relatedBeneficiaries,
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
