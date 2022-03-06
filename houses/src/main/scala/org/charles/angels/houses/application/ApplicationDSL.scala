package org.charles.angels.houses.application

import cats.syntax.all.*
import cats.data.ValidatedNec
import org.charles.angels.houses.application.standard.StandardLanguage
import org.charles.angels.houses.application.events.DomainEventLanguage
import org.charles.angels.houses.application.queries.QueryLanguage
import org.charles.angels.houses.application.services.ServiceLanguage
import org.charles.angels.houses.application.models.HouseModel
import org.charles.angels.houses.domain.Contact
import org.charles.angels.houses.application.errors.given
import org.charles.angels.houses.domain.Schedule
import org.charles.angels.houses.domain.House
import java.util.UUID
import org.charles.angels.houses.domain.events.HouseEvent

object ApplicationDSL:
  val STDL = StandardLanguage[ApplicationAction]
  val DEL = DomainEventLanguage[ApplicationAction]
  val QL = QueryLanguage[ApplicationAction]
  val SVCL = ServiceLanguage[ApplicationAction]

  import STDL.*, DEL.*, QL.*, SVCL.*

  def createHouse(house: HouseModel) = for
    (contact, contactCreated) <- of(
      Contact(
        house.contact.ci,
        house.contact.name,
        house.contact.lastname,
        house.contact.phone
      )
    )
    (schedule, scheduleCreated) <- of(
      Schedule(
        house.schedule.monday,
        house.schedule.tuesday,
        house.schedule.wednesday,
        house.schedule.thursday,
        house.schedule.friday
      )
    )
    file <- alloc(house.img, house.name)
    result <- of(
      House(
        file,
        house.name,
        house.rif,
        house.phones,
        house.address,
        contact.ci,
        schedule.id,
        house.maxShares,
        house.currentShares,
        house.minimumAge,
        house.maximumAge,
        house.currentGirlsHelped,
        house.currentBoysHelped
      )
    ).attempt
    (house, houseCreated) <- result match
      case Left(e)  => dealloc(file) >> e.raiseError
      case Right(t) => t.pure[ApplicationLanguage]
    () <- notifyContactEvent(contactCreated) >>
      notifyScheduleEvent(scheduleCreated) >>
      notifyHouseEvent(houseCreated)
  yield house

  private def withHouse[A](
      id: UUID
  )(
      update: House => ApplicationLanguage[(House, HouseEvent)]
  ) = for
    house <- getHouse(id)
    (nHouse, houseEvent) <- update(house)
    () <- notifyHouseEvent(houseEvent)
  yield house

  def setNameToHouse(id: UUID, name: String) = withHouse(id) { house =>
    for vname <- of(House.Name(name))
    yield house.setName(vname)
  }

  def setImageToHouse(id: UUID, contents: Array[Byte]) = withHouse(id) {
    house =>
      for
        img <- alloc(contents, house.name)
        vimg <- of(House.Image(img))
      yield house.setImage(vimg)
  }

  def setRIFToHouse(id: UUID, rif: Int) = withHouse(id) { house =>
    for vrif <- of(House.RIF(rif))
    yield house.setRIF(vrif)
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

  def setCurrentSharesOfHouse(id: UUID, currentShares: Int) = withHouse(id) {
    _.setCurrentShares(currentShares).pure
  }

  def setMinimumAgeOfHouse(id: UUID, minimumAge: Int) = withHouse(id) { house =>
    for (vmin, _) <- of(House.AgeLimits(minimumAge, house.maximumAge))
    yield house.setMinimumAge(vmin)
  }

  def setMaximumAgeOfHouse(id: UUID, maximumAge: Int) = withHouse(id) { house =>
    for (_, vmax) <- of(House.AgeLimits(house.minimumAge, maximumAge))
    yield house.setMaximumAge(vmax)
  }

  def setCurrentBoysHelped(id: UUID, currentBoysHelped: Int) = withHouse(id) {
    _.setCurrentBoysHelped(currentBoysHelped).pure
  }

  def setCurrentGirlsHelped(id: UUID, currentGirlsHelped: Int) = withHouse(id) {
    _.setCurrentGirlsHelped(currentGirlsHelped).pure
  }
