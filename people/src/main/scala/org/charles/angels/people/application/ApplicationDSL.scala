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

  def updatePersonalInformationOfChild(
      id: UUID,
      inf: PersonalInformationModel
  ) = updateInformation(id, inf, Child.setChildInformation)

  def updatePersonalInformationOfChildsMother(
      id: UUID,
      inf: PersonalInformationModel
  ) = updateInformation(id, inf, Child.setMotherInformation)

  def updatePersonalInformationOfChildsFather(
      id: UUID,
      inf: PersonalInformationModel
  ) = updateInformation(id, inf, Child.setFatherInformation)

  def updatePersonalInformationOfChildsNonParentRepresentative(
      id: UUID,
      inf: PersonalInformationModel
  ) = updateInformation(id, inf, Child.setNonParentInformation)

  def addRelatedBeneficiary(id: UUID, inf: PersonalInformationModel) =
    updateInformation(id, inf, Child.addRelatedBeneficiary)

  def removedRelatedBeneficiary(id: UUID, benKey: Int) = for
    child <- getChild(id)
    (evt, nChild) = Child.removeRelatedBeneficiary(benKey).run(child).value
  yield nChild

  def updateRelatedBeneficiary(
      id: UUID,
      benKey: Int,
      pInf: PersonalInformationModel
  ) =
    updateInformation(id, pInf, Child.updateRelatedBeneficiary(benKey, _))

  def deleteChild(id: UUID) = for
    child <- getChild(id)
    evt = child.delete
    _ <- publish(evt)
  yield ()

  def create(
      houseId: UUID,
      inf: PersonalInformationModel,
      mother: Option[PersonalInformationModel],
      father: Option[PersonalInformationModel],
      nonPerson: Option[PersonalInformationModel],
      relBen: List[PersonalInformationModel],
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
    motherInf <- of(
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
    fatherInf <- of(
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
    nonPersonInf <- of(
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
    relatedBeneficiaries <- of(
      relBen
        .traverse(inf =>
          PersonalInformation(
            inf.ci,
            inf.name,
            inf.lastname,
            inf.birthdate
          )
        )
    )
    childInf = ChildInformation(
      information,
      fatherInf,
      motherInf,
      nonPersonInf,
      relatedBeneficiaries.map(i => (i.ci, i)).toMap,
      photo
    )
    (child, event) = wear match {
      case Wear.BoyWear(attire)  => Child.Boy(houseId, childInf, attire)
      case Wear.GirlWear(attire) => Child.Girl(houseId, childInf, attire)
    }
    () <- publish(event)
  yield child
}
