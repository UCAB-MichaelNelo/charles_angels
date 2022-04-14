package org.charles.angels.people.domain

import cats.syntax.all.*
import cats.data.State
import org.charles.angels.people.domain.events.ChildEvent
import monocle.Lens
import monocle.Optional
import cats.data.StateT
import java.io.File
import monocle.Prism
import monocle.Iso
import java.util.UUID
import java.time.temporal.ChronoUnit.YEARS
import java.time.temporal.ChronoUnit.MONTHS
import java.time.temporal.ChronoUnit

enum Child { child =>
  case Boy(id: UUID, information: ChildInformation, wearInformation: BoyAttire)
  case Girl(
      id: UUID,
      information: ChildInformation,
      wearInformation: GirlAttire
  )

  def getID = child match {
    case Child.Boy(id, _, _)  => id
    case Child.Girl(id, _, _) => id
  }

  def getInformation = child match {
    case Child.Boy(_, inf, _)  => inf
    case Child.Girl(_, inf, _) => inf
  }

  def wear = child match {
    case Boy(_, _, wear) => Wear.BoyWear(wear)
    case Girl(_, _, wear) => Wear.GirlWear(wear)
  }

  def delete = child match {
    case Child.Boy(id, _, _)  => ChildEvent.BoyDeleted(id)
    case Child.Girl(id, _, _) => ChildEvent.GirlDeleted(id)
  }

  def dateSixMonthsBefore(years: Int) =
    ChronoUnit.YEARS
      .addTo(getInformation.information.birthdate, years)
      .minusMonths(6)

  private def setInformation(information: ChildInformation) = child match {
    case Child.Boy(id, _, wear)  => new Child.Boy(id, information, wear)
    case Child.Girl(id, _, wear) => new Child.Girl(id, information, wear)
  }
}

object Child {
  object Boy {
    def apply(
        hotelId: UUID,
        information: ChildInformation,
        wearInformation: BoyAttire
    ): (Child, ChildEvent) =
      val id = UUID.randomUUID
      (
        new Child.Boy(id, information, wearInformation),
        ChildEvent.ChildCreated(
          hotelId,
          information,
          Wear.BoyWear(wearInformation),
          id
        )
      )
    def unsafe(
        id: UUID,
        information: ChildInformation,
        wearInformation: BoyAttire
    ) = new Child.Boy(id, information, wearInformation)
  }
  object Girl {
    def apply(
        houseId: UUID,
        information: ChildInformation,
        wearInformation: GirlAttire
    ): (Child, ChildEvent) =
      val id = UUID.randomUUID
      (
        new Child.Girl(id, information, wearInformation),
        ChildEvent.ChildCreated(
          houseId,
          information,
          Wear.GirlWear(wearInformation),
          id
        )
      )
    def unsafe(
        id: UUID,
        information: ChildInformation,
        wearInformation: GirlAttire
    ) = new Child.Girl(id, information, wearInformation)
  }
  // Lenses
  private def information: Lens[Child, PersonalInformation] = Lens(
    (_: Child).getInformation.information
  )((inf: PersonalInformation) =>
    (child: Child) => {
      val ci = child.getInformation
      val newCI = ci.setInformation(inf)

      child.setInformation(newCI)
    }
  )
  private object Representative {
    def father: Optional[Child, PersonalInformation] =
      Optional(
        (_: Child).getInformation.father
      )(pI =>
        c => {
          val ci = c.getInformation
          val newCI = ci.setFatherInformation(pI)

          c.setInformation(newCI)
        }
      )
    def mother: Optional[Child, PersonalInformation] =
      Optional(
        (_: Child).getInformation.mother
      )(pI =>
        c => {
          val ci = c.getInformation
          val newCI = ci.setMotherInformation(pI)

          c.setInformation(newCI)
        }
      )
    def nonParent: Optional[Child, PersonalInformation] =
      Optional(
        (_: Child).getInformation.nonParent
      )(pI =>
        c => {
          val ci = c.getInformation
          val newCI = ci.setNonParentRepresentativeInformation(pI)

          c.setInformation(newCI)
        }
      )

  }
  private def relatedBeneficiaries = Lens[Child, Map[Int, PersonalInformation]](
    (_: Child).getInformation.relatedBeneficiaries
  )(rb =>
    c => {
      val ci = c.getInformation
      val newCI = ci.setRelatedBeneficiaries(rb)

      c.setInformation(newCI)
    }
  )
  private def photo = Lens[Child, File](
    (_: Child).getInformation.photo
  )(p =>
    c => {
      val ci = c.getInformation
      val newCI = ci.setPhoto(p)

      c.setInformation(newCI)
    }
  )
  private def wear = Lens[Child, Wear](c =>
    c match {
      case Child.Boy(_, _, attire)  => Wear.BoyWear(attire)
      case Child.Girl(_, _, attire) => Wear.GirlWear(attire)
    }
  )(w =>
    c =>
      (w, c) match {
        case (Wear.BoyWear(attire), Child.Boy(id, inf, _)) =>
          new Child.Boy(id, inf, attire)
        case (Wear.GirlWear(attire), Child.Girl(id, inf, _)) =>
          new Child.Girl(id, inf, attire)
        case _ => c
      }
  )

  private def updateInformation(update: Child => Child) = for
    ci <- State.inspect[Child, Int](c => information.get(c).ci)
    _ <- State.modify(update)
    pI <- State.inspect(information.get)
  yield ChildEvent.PersonalInformationUpdated(ci, pI, true)
  private def updateInformationWith(
      inf: PersonalInformation,
      lens: Optional[Child, PersonalInformation],
      event: (Int, PersonalInformation) => ChildEvent
  ) = for
    ci <- State.inspect[Child, Option[Int]](c => lens.getOption(c).map(_.ci))
    _ <- State.modify(lens.set(inf))
    pI <- State.inspect[Child, Option[PersonalInformation]](
      information.getOption
    )
  yield event(ci getOrElse inf.ci, inf)
  private def updateAttire(
      update: Child => Child,
      event: (UUID, Wear) => ChildEvent
  ) =
    State
      .modify(update)
      .inspect(child => (child.getID, wear.get(child)))
      .map(event.tupled)

  // Update functions

  def setChildInformation(inf: PersonalInformation) =
    updateInformation(information.set(inf))
  def setFatherInformation(inf: PersonalInformation) =
    updateInformationWith(
      inf,
      Representative.father,
      ChildEvent.FatherInformationUpdated.apply
    )
  def setMotherInformation(inf: PersonalInformation) =
    updateInformationWith(
      inf,
      Representative.mother,
      ChildEvent.MotherInformationUpdated.apply
    )
  def setNonParentInformation(inf: PersonalInformation) =
    updateInformationWith(
      inf,
      Representative.nonParent,
      ChildEvent.NonParentInformationUpdated.apply
    )
  def addRelatedBeneficiary(benInf: PersonalInformation) =
    State
      .modify(relatedBeneficiaries.modify(_.updated(benInf.ci, benInf)))
      .as(ChildEvent.RelatedBeneficiaryAdded(benInf))
  def removeRelatedBeneficiary(ci: Int) =
    State
      .modify(relatedBeneficiaries.modify(_.removed(ci)))
      .as(ChildEvent.RelatedBeneficiaryRemoved(ci))
  def updateRelatedBeneficiary(ci: Int, inf: PersonalInformation) =
    State
      .modify(relatedBeneficiaries.modify(_.updatedWith(ci) { _.as(inf) }))
      .as(ChildEvent.RelatedBeneficiaryUpdated(ci, inf))
  def setPhoto(img: File) =
    State
      .modify(photo.set(img))
      .inspect(_.getID)
      .map(ChildEvent.PhotoUpdated(_, img))
  def setShortOrTrousersSize(size: Int) = updateAttire(
    wear.modify(_.setShortOrTrousersSize(size)),
    ChildEvent.AttireUpdated.apply
  )
  def setTShirtOrShirtSize(size: Int) = updateAttire(
    wear.modify(_.setTShirtOrShirtSize(size)),
    ChildEvent.AttireUpdated.apply
  )
  def setSweaterSize(size: Int) = updateAttire(
    wear.modify(_.setSweaterSize(size)),
    ChildEvent.AttireUpdated.apply
  )
  def setDressSize(size: Int) = updateAttire(
    wear.modify(_.setDressSize(size)),
    ChildEvent.AttireUpdated.apply
  )
  def setFootwearSize(size: Int) = updateAttire(
    wear.modify(_.setFootwearSize(size)),
    ChildEvent.AttireUpdated.apply
  )
}
