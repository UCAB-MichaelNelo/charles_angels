package org.charles.angels.people.application

import cats.syntax.all.*
import org.charles.angels.people.application.queries.QueryLanguage
import org.charles.angels.people.application.events.EventLanguage
import org.charles.angels.people.application.models.PersonalInformationModel
import java.io.File
import org.charles.angels.people.domain.PersonalInformation
import org.charles.angels.people.application.errors.ApplicationError
import org.charles.angels.people.application.errors.given
import cats.data.ValidatedNec
import cats.data.EitherT
import cats.Inject
import org.charles.angels.people.domain.Wear
import org.charles.angels.people.domain.Child
import org.charles.angels.people.domain.ChildInformation
import cats.data.State
import cats.data.StateT
import org.charles.angels.people.domain.events.ChildEvent
import cats.data.Chain
import monocle.Optional
import java.util.UUID
import org.charles.angels.people.domain.BoyAttire
import org.charles.angels.people.domain.errors.DressError

object ApplicationDSL
    extends QueryLanguage[ApplicationAction]
    with EventLanguage[ApplicationAction] {

  def of[E, A](result: ValidatedNec[E, A])(using
      injector: Inject[E, ApplicationError]
  ): ApplicationLanguage[A] =
    EitherT.fromEither(result.bimap(_.map(injector.inj), identity).toEither)

  private def updateInformation(
      id: UUID,
      inf: PersonalInformationModel,
      op: PersonalInformation => State[Child, ChildEvent]
  ) = for
    child <- getChild(id)
    vInf <- of(
      PersonalInformation(
        inf.ci,
        inf.name,
        inf.lastname,
        inf.birthdate
      )
    )
    (nChild, evt) = op(vInf).run(child).value
    _ <- publish(evt)
  yield nChild

  private def updateInformation(
      id: UUID,
      inf: Option[PersonalInformation],
      op: Option[PersonalInformation] => State[Child, ChildEvent]
  ) = for
    child <- getChild(id)
    (nChild, evt) = op(inf).run(child).value
    _ <- publish(evt)
  yield nChild

  def updatePersonalInformationOfChild(
      id: UUID,
      inf: PersonalInformationModel
  ) = updateInformation(id, inf, Child.setChildInformation)

  def updatePersonalInformationOfChildsMother(
      id: UUID,
      ci: Option[Int]
  ) = for
    inf <- ci traverse findExistantPersonalInformationOrFail
    res <- updateInformation(id, inf, Child.setMotherInformation)
  yield res
  def updatePersonalInformationOfChildsFather(
      id: UUID,
      ci: Option[Int]
  ) = for
    inf <- ci traverse findExistantPersonalInformationOrFail
    res <- updateInformation(id, inf, Child.setFatherInformation)
  yield res

  def updatePersonalInformationOfChildsNonParentRepresentative(
      id: UUID,
      ci: Option[Int]
  ) = for
    inf <- ci traverse findExistantPersonalInformationOrFail
    res <- updateInformation(id, inf, Child.setNonParentInformation)
  yield res
  
  def addRelatedBeneficiary(id: UUID, benId: UUID) = for
    child <- getChild(id)
    _ <- getChild(benId)
    (nChild, evt) = Child.addRelatedBeneficiary(benId).run(child).value
    _ <- publish(evt)
  yield nChild

  def removedRelatedBeneficiary(id: UUID, benId: UUID) = for
    child <- getChild(id)
    (nChild, evt) = Child.removeRelatedBeneficiary(benId).run(child).value
    _ <- publish(evt)
  yield nChild

  def deleteChild(id: UUID) = for
    child <- getChild(id)
    evt = child.delete
    _ <- publish(evt)
  yield child

  def updateChildPhoto(id: UUID, photo: File) = for
    child <- getChild(id)
    (nchild, evt) = Child.setPhoto(photo).run(child).value
    _ <- publish(evt)
  yield evt

  def updateChildAttire(id: UUID, wear: ValidatedNec[DressError, Wear]) = for
    wear <- of(wear)
    child <- getChild(id)
    modifications = wear match {
      case Wear.BoyWear(attire) => for
          shortEvt <- Child.setShortOrTrousersSize(attire.shortOrTrousersSize)
          tShirtEvt <- Child.setTShirtOrShirtSize(attire.tshirtOrshirtSize)
          sweaterEvt <- Child.setSweaterSize(attire.sweaterSize)
          footWearEvt <- Child.setFootwearSize(attire.footwearSize)
        yield Vector(shortEvt, tShirtEvt, sweaterEvt, footWearEvt)
      case Wear.GirlWear(attire) =>for
          shortEvt <- Child.setShortOrTrousersSize(attire.shortOrTrousersSize)
          tShirtEvt <- Child.setTShirtOrShirtSize(attire.tshirtOrshirtSize)
          dressEvt <- Child.setDressSize(attire.dressSize)
          footWearEvt <- Child.setFootwearSize(attire.footwearSize)
        yield Vector(shortEvt, tShirtEvt, dressEvt, footWearEvt)
    }
    (nchild, evts) = modifications.run(child).value
    _ <- evts.traverse(publish)
  yield nchild

  def create(
      houseId: UUID,
      inf: PersonalInformationModel,
      mother: Option[PersonalInformationModel],
      father: Option[PersonalInformationModel],
      nonPerson: Option[PersonalInformationModel],
      relBen: Vector[UUID],
      wear: Wear,
      photo: File
  ): ApplicationLanguage[Child] = for
    information <- of(
      PersonalInformation(
        inf.ci,
        inf.name,
        inf.lastname,
        inf.birthdate
      )
    )
    existingMotherInf <- mother.flatTraverse(inf => findExistantPersonalInformation(inf.ci))
    providedMotherInf <- of(
      mother
        .traverse(inf =>
          PersonalInformation(
            inf.ci,
            inf.name,
            inf.lastname,
            inf.birthdate
          )
        )
    )
    motherInf = existingMotherInf <+> providedMotherInf
    existingFatherInf <- father.flatTraverse(inf => findExistantPersonalInformation(inf.ci))
    providedFatherInf <- of(
      father
        .traverse(inf =>
          PersonalInformation(
            inf.ci,
            inf.name,
            inf.lastname,
            inf.birthdate
          )
        )
    )
    fatherInf = existingFatherInf <+> providedFatherInf
    existingNonPersonInf <- nonPerson.flatTraverse(inf => findExistantPersonalInformation(inf.ci))
    providedNonPersonInf <- of(
      nonPerson
        .traverse(inf =>
          PersonalInformation(
            inf.ci,
            inf.name,
            inf.lastname,
            inf.birthdate
          )
        )
    )
    nonPersonInf = existingNonPersonInf <+> providedNonPersonInf
    relatedBeneficiaries <- relBen.traverse(getChild)
    childInf = ChildInformation(
      information,
      fatherInf,
      motherInf,
      nonPersonInf,
      relatedBeneficiaries.map(_.getID),
      photo
    )
    (child, event) = wear match {
      case Wear.BoyWear(attire)  => Child.Boy(houseId, childInf, attire)
      case Wear.GirlWear(attire) => Child.Girl(houseId, childInf, attire)
    }
    () <- publish(event)
  yield child
}
