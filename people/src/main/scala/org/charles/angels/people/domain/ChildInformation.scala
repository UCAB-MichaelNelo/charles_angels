package org.charles.angels.people.domain

import cats.syntax.all.*
import java.io.File
import java.util.UUID

final case class ChildInformation(
    information: PersonalInformation,
    father: Option[PersonalInformation],
    mother: Option[PersonalInformation],
    nonParent: Option[PersonalInformation],
    relatedBeneficiaries: Vector[UUID],
    photo: File
) {
  def setInformation(personalInformation: PersonalInformation) =
    copy(information = personalInformation)
  def setFatherInformation(fatherInf: Option[PersonalInformation]) =
    copy(father = fatherInf)
  def setMotherInformation(motherInf: Option[PersonalInformation]) =
    copy(mother = motherInf)
  def setNonParentRepresentativeInformation(repInf: Option[PersonalInformation]) =
    copy(nonParent = repInf)
  def setRelatedBeneficiaries(relBen: Vector[UUID]) =
    copy(relatedBeneficiaries = relBen)
  def setPhoto(img: File) = copy(photo = img)
}
