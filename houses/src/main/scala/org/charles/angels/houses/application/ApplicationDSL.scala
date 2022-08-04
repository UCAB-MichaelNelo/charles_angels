package org.charles.angels.houses.application

import cats.syntax.all.*
import cats.data.ValidatedNec
import cats.data.NonEmptyChain
import org.charles.angels.houses.application.standard.StandardLanguage
import org.charles.angels.houses.application.events.DomainEventLanguage
import org.charles.angels.houses.application.queries.QueryLanguage
import org.charles.angels.houses.application.services.ServiceLanguage
import org.charles.angels.houses.application.models.HouseModel
import org.charles.angels.houses.domain.Contact
import org.charles.angels.houses.application.errors.given
import org.charles.angels.houses.domain.House
import java.util.UUID
import org.charles.angels.houses.domain.events.HouseEvent
import org.charles.angels.houses.domain.events.ContactEvent
import org.charles.angels.houses.application.errors.ApplicationError
import java.io.File
import java.time.LocalTime
import java.time.LocalDate

object ApplicationDSL:
  private val STDL = StandardLanguage[ApplicationAction]
  private val DEL = DomainEventLanguage[ApplicationAction]
  private val QL = QueryLanguage[ApplicationAction]
  private val SVCL = ServiceLanguage[ApplicationAction]

  import STDL.*, DEL.*, QL.*, SVCL.*

  private def withHouse[A](
      id: UUID
  )(
      update: House => ApplicationLanguage[(House, HouseEvent)]
  ) = for
    house <- getHouse(id)
    (nHouse, houseEvent) <- update(house)
    () <- notifyHouseEvent(houseEvent)
  yield nHouse

  private def withContact[A](
      ci: Int
  )(update: Contact => ApplicationLanguage[(Contact, ContactEvent)]) = for
    contact <- getContact(ci)
    (ncontact, contactEvent) <- update(contact)
    () <- notifyContactEvent(contactEvent)
  yield ncontact

  // Use cases relative to House Entity

  def findHouse(id: UUID) = getHouse(id)

  def getAllHouses = QL.getAllHouses

  def assertRifDoesNotExist(rif: Int): ApplicationLanguage[Unit] = QL.doesRifExist(rif).leftWiden[NonEmptyChain[ApplicationError]]

  def getAllContacts = QL.getContacts

  def createHouse(house: HouseModel) = for
    _ <- assertRifDoesNotExist(house.rif)
    (contact, contactCreated) <- getContact(house.contact.ci)
      .map(_ -> None)
      .handleErrorWith { _ =>
        for (contact, evt) <- of(
            Contact(
              house.contact.ci,
              house.contact.name,
              house.contact.lastname,
              house.contact.phone
            )
          )
        yield (contact, Some(evt))
      }
    file = File(house.filename)
    result <- of(
      House(
        file,
        house.name,
        house.rif,
        house.phones,
        house.address,
        contact.ci,
        house.maxShares,
        house.currentShares,
        house.minimumAge,
        house.maximumAge,
        house.currentGirlsHelped,
        house.currentBoysHelped,
        house.scheduleStartTime,
        house.scheduleEndTime
      )
    ).attempt
    (house, houseCreated) <- result match
      case Left(e)  => dealloc(file) >> e.raiseError
      case Right(t) => t.pure[ApplicationLanguage]
    () <- (contactCreated match {
      case Some(event) => notifyContactEvent(event)
      case None        => ().pure[ApplicationLanguage]
    }) >>
      notifyHouseEvent(houseCreated)
  yield house

  def setNameToHouse(id: UUID, name: String) = withHouse(id) { house =>
    for vname <- of(House.Name(name))
    yield house.setName(vname)
  }

  def setImageToHouse(id: UUID, ext: String, contents: Array[Byte]) = withHouse(id) {
    house =>
      for
        img <- alloc(contents, f"${house.rif}.$ext")
        vimg <- of(House.Image(img)).onError { _ => dealloc(img) }
      yield house.setImage(vimg)
  }

  def setImageToHouse(id: UUID, file: File) = withHouse(id) { house =>
    of(House.Image(file).map(house.setImage))
  }

  def setRIFToHouse(id: UUID, rif: Int) = withHouse(id) { house =>
    for vrif <- of(House.RIF(rif))
    yield house.setRIF(vrif)
  }

  def setAddressToHouse(id: UUID, address: String) = withHouse(id) { house => 
    of(House.Address(address).map(house.setAddress(_)))
  }

  def addPhoneToHouse(id: UUID, phone: String) = withHouse(id) { house =>
    for vphone <- of(House.Phones.validatePhone(phone, house.phones.length))
    yield house.addPhone(phone)
  }

  def removePhoneOfHouse(id: UUID, key: Int) = withHouse(id) {
    _.removePhone(key).pure
  }

  def updatePhoneOfHouse(id: UUID, key: Int, phone: String) = withHouse(id) {
    house =>
      for vphone <- of(House.Phones.validatePhone(phone, key))
      yield house.updatePhone(key, phone)
  }

  def setMaxSharesOfHouse(id: UUID, maxShares: Int) = withHouse(id) { house =>
    for vmaxShares <- of(House.MaxShares(maxShares))
    yield house.setMaxShares(vmaxShares)
  }

  def setMinimumAgeOfHouse(id: UUID, minimumAge: Int) = withHouse(id) { house =>
    for (vmin, _) <- of(House.AgeLimits(minimumAge, house.maximumAge))
    yield house.setMinimumAge(vmin)
  }

  def setMaximumAgeOfHouse(id: UUID, maximumAge: Int) = withHouse(id) { house =>
    for (_, vmax) <- of(House.AgeLimits(house.minimumAge, maximumAge))
    yield house.setMaximumAge(vmax)
  }

  def incrementCurrentSharesOfHouse(id: UUID) = withHouse(id) { house =>
    house.setCurrentShares(house.currentShares + 1).pure
  }

  def decrementCurrentSharesOfHouse(id: UUID) = withHouse(id) { house =>
    house.setCurrentShares(house.currentShares - 1).pure
  }

  def incrementCurrentBoysHelpedOfHouse(id: UUID) =
    withHouse(id) { house =>
      house.setCurrentBoysHelped(house.currentBoysHelped + 1).pure
    }

  def decrementCurrentBoysHelpedOfHouse(id: UUID) =
    withHouse(id) { house =>
      house.setCurrentBoysHelped(house.currentBoysHelped - 1).pure
    }

  def incrementCurrentGirlsHelpedOfHouse(id: UUID) =
    withHouse(id) { house =>
      house.setCurrentGirlsHelped(house.currentGirlsHelped + 1).pure
    }
  def decrementCurrentGirlsHelpedOfHouse(id: UUID) =
    withHouse(id) { house =>
      house.setCurrentGirlsHelped(house.currentGirlsHelped - 1).pure
    }

  def setScheduleStartTimeOfHouse(id: UUID, startTime: LocalTime) = 
    withHouse(id) { house =>
      of(house.setScheduleStartTime(startTime))
    }

  def setScheduleEndTimeOfHouse(id: UUID, endTime: LocalTime) =
    withHouse(id) { house =>
      of(house.setScheduleEndTime(endTime))
    }

  def setContactCIOfHouse(id: UUID, ci: Int) = for
    contact <- findContact(ci)
    _ <- withHouse(id) { house =>
      of(Contact.CI(contact.ci).map(house.setContact))
    }
  yield ()

  def deleteHouse(id: UUID) = for
    house <- getHouse(id)
    () <- notifyHouseEvent(house.delete)
  yield ()

  def validateCanAddWithBirthdate(id: UUID, birthdate: LocalDate) = for
    house <- getHouse(id)
  yield house.validateAgeBounds(birthdate) && house.canHaveNewBeneficiaries

  def getHousesThatCanAddWithBirthdate(birthdate: LocalDate) = for
    houses <- getAllHouses
  yield houses.filter(house => house.validateAgeBounds(birthdate) && house.canHaveNewBeneficiaries)

  // Use cases relative to Contact

  def findContact(ci: Int) = getContact(ci)

  def setCIOfContact(ci: Int, newCI: Int) = withContact(ci) { contact =>
    for vnewCI <- of(Contact.CI(newCI))
    yield contact.setCI(vnewCI)
  }

  def setNameOfContact(ci: Int, name: String) = withContact(ci) { contact =>
    for vname <- of(Contact.Name(name))
    yield contact.setName(vname)
  }

  def setLastnameOfContact(ci: Int, lastname: String) = withContact(ci) {
    contact =>
      for vlname <- of(Contact.Lastname(lastname))
      yield contact.setLastname(vlname)
  }

  def setPhoneOfContact(ci: Int, phone: String) = withContact(ci) { contact =>
    for vphone <- of(Contact.Phone(phone))
    yield contact.setPhone(vphone)
  }

  def deleteContact(ci: Int) = for
    contact <- getContact(ci)
    () <- notifyContactEvent(contact.delete)
  yield ()
