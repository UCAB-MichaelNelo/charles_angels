package org.charles.angels.people.domain

import cats.syntax.all.*
import java.io.File
import java.util.UUID

final case class ChildInformation(
    information: PersonalInformation,
    father: Option[PersonalInformation],
    mother: Option[PersonalInformation],
    nonParent: Option[PersonalInformation],
    relatedBeneficiaries: Map[Int, PersonalInformation],
    photo: File
) {
  def setInformation(personalInformation: PersonalInformation) =
    copy(information = personalInformation)
  def setFatherInformation(fatherInf: PersonalInformation) =
    copy(father = fatherInf.some)
  def setMotherInformation(motherInf: PersonalInformation) =
    copy(mother = motherInf.some)
  def setNonParentRepresentativeInformation(repInf: PersonalInformation) =
    copy(nonParent = repInf.some)
  def setRelatedBeneficiaries(relBen: Map[Int, PersonalInformation]) =
    copy(relatedBeneficiaries = relBen)
  def setPhoto(img: File) = copy(photo = img)
}
