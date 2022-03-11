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
import org.charles.angels.houses.domain.events.ContactEvent
import org.charles.angels.houses.domain.RawScheduleBlock
import org.charles.angels.houses.domain.ScheduleBlock
import org.charles.angels.houses.domain.events.ScheduleEvent

object ApplicationDSL:
  val STDL = StandardLanguage[ApplicationAction]
  val DEL = DomainEventLanguage[ApplicationAction]
  val QL = QueryLanguage[ApplicationAction]
  val SVCL = ServiceLanguage[ApplicationAction]

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

  private def withSchedule[A](
      id: UUID
  )(update: Schedule => ApplicationLanguage[(Schedule, ScheduleEvent)]) = for
    schedule <- getSchedule(id)
    (nschedule, scheduleEvent) <- update(schedule)
    () <- notifyScheduleEvent(scheduleEvent)
  yield nschedule

  // Use cases relative to House Entity

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

  def setCurrentBoysHelpedOfHouse(id: UUID, currentBoysHelped: Int) =
    withHouse(id) {
      _.setCurrentBoysHelped(currentBoysHelped).pure
    }

  def setCurrentGirlsHelpedOfHouse(id: UUID, currentGirlsHelped: Int) =
    withHouse(id) {
      _.setCurrentGirlsHelped(currentGirlsHelped).pure
    }

  def deleteHouse(id: UUID) = for
    house <- getHouse(id)
    () <- notifyHouseEvent(house.delete)
  yield ()

  // Use cases relative to Contact

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

  def setPhone(ci: Int, phone: String) = withContact(ci) { contact =>
    for vphone <- of(Contact.Phone(phone))
    yield contact.setPhone(vphone)
  }

  def deleteContact(ci: Int) = for
    contact <- getContact(ci)
    () <- notifyContactEvent(contact.delete)
  yield ()
  // Use cases relative to Schedule

  private def addBlock(
      id: UUID,
      rawBlock: RawScheduleBlock
  )(
      addBlockToSchedule: (
          Schedule,
          ScheduleBlock
      ) => ApplicationLanguage[(Schedule, ScheduleEvent)]
  ) =
    withSchedule(id) { schedule =>
      for
        sblock <- of(ScheduleBlock(rawBlock))
        t <- addBlockToSchedule(schedule, sblock)
      yield t
    }

  def addBlockOnMonday(id: UUID, rawBlock: RawScheduleBlock) =
    addBlock(id, rawBlock) { (schedule, block) =>
      of(schedule.addBlockOnMonday(block))
    }

  def addBlockOnTuesday(id: UUID, rawBlock: RawScheduleBlock) =
    addBlock(id, rawBlock) { (schedule, block) =>
      of(schedule.addBlockOnTuesday(block))
    }

  def addBlockOnWednesday(id: UUID, rawBlock: RawScheduleBlock) =
    addBlock(id, rawBlock) { (schedule, block) =>
      of(schedule.addBlockOnWednesday(block))
    }

  def addBlockOnThursday(id: UUID, rawBlock: RawScheduleBlock) =
    addBlock(id, rawBlock) { (schedule, block) =>
      of(schedule.addBlockOnThursday(block))
    }

  def addBlockOnFriday(id: UUID, rawBlock: RawScheduleBlock) =
    addBlock(id, rawBlock) { (schedule, block) =>
      of(schedule.addBlockOnFriday(block))
    }

  private def removeBlock(
      id: UUID,
      key: Int
  )(
      removeBlockFromSchedule: (
          Schedule,
          Int
      ) => ApplicationLanguage[(Schedule, ScheduleEvent)]
  ) =
    withSchedule(id) {
      removeBlockFromSchedule(_, key)
    }

  def removeBlockOnMonday(id: UUID, key: Int) =
    removeBlock(id, key) { (schedule, key) =>
      of(schedule.removeBlockOnMonday(key))
    }

  def removeBlockOnTuesday(id: UUID, key: Int) =
    removeBlock(id, key) { (schedule, key) =>
      of(schedule.removeBlockOnTuesday(key))
    }

  def removeBlockOnWednesday(id: UUID, key: Int) =
    removeBlock(id, key) { (schedule, key) =>
      of(schedule.removeBlockOnWednesday(key))
    }

  def removeBlockOnThursday(id: UUID, key: Int) =
    removeBlock(id, key) { (schedule, key) =>
      of(schedule.removeBlockOnThursday(key))
    }

  def removeBlockOnFriday(id: UUID, key: Int) =
    removeBlock(id, key) { (schedule, key) =>
      of(schedule.removeBlockOnFriday(key))
    }

  def setStartingHourOnMonday(id: UUID, key: Int, startingHour: Int) =
    withSchedule(id) { schedule =>
      for
        vhour <- of(ScheduleBlock.Hour(startingHour))
        t <- of(schedule.setStartingHourOnMonday(key, startingHour))
      yield t
    }

  def setStartingHourOnTuesday(id: UUID, key: Int, startingHour: Int) =
    withSchedule(id) { schedule =>
      for
        vhour <- of(ScheduleBlock.Hour(startingHour))
        t <- of(schedule.setStartingMinuteOnTuesday(key, startingHour))
      yield t
    }

  def setStartingHourOnWednesday(id: UUID, key: Int, startingHour: Int) =
    withSchedule(id) { schedule =>
      for
        vhour <- of(ScheduleBlock.Hour(startingHour))
        t <- of(schedule.setStartingHourOnWednesday(key, startingHour))
      yield t
    }

  def setStartingHourOnThursday(id: UUID, key: Int, startingHour: Int) =
    withSchedule(id) { schedule =>
      for
        vhour <- of(ScheduleBlock.Hour(startingHour))
        t <- of(schedule.setStartingHourOnThursday(key, startingHour))
      yield t
    }

  def setStartingHourOnFriday(id: UUID, key: Int, startingHour: Int) =
    withSchedule(id) { schedule =>
      for
        vhour <- of(ScheduleBlock.Hour(startingHour))
        t <- of(schedule.setStartingHourOnFriday(key, startingHour))
      yield t
    }

  def setStartingMinuteOnMonday(id: UUID, key: Int, startingMinute: Int) =
    withSchedule(id) { schedule =>
      for
        vhour <- of(ScheduleBlock.Minute(startingMinute))
        t <- of(schedule.setStartingMinuteOnMonday(key, startingMinute))
      yield t
    }

  def setStartingMinuteOnTuesday(id: UUID, key: Int, startingMinute: Int) =
    withSchedule(id) { schedule =>
      for
        vhour <- of(ScheduleBlock.Minute(startingMinute))
        t <- of(schedule.setStartingMinuteOnTuesday(key, startingMinute))
      yield t
    }

  def setStartingMinuteOnWednesday(id: UUID, key: Int, startingMinute: Int) =
    withSchedule(id) { schedule =>
      for
        vhour <- of(ScheduleBlock.Minute(startingMinute))
        t <- of(schedule.setStartingMinuteOnWednesday(key, startingMinute))
      yield t
    }

  def setStartingMinuteOnThursday(id: UUID, key: Int, startingMinute: Int) =
    withSchedule(id) { schedule =>
      for
        vhour <- of(ScheduleBlock.Minute(startingMinute))
        t <- of(schedule.setStartingMinuteOnThursday(key, startingMinute))
      yield t
    }

  def setStartingMinuteOnFriday(id: UUID, key: Int, startingMinute: Int) =
    withSchedule(id) { schedule =>
      for
        vhour <- of(ScheduleBlock.Minute(startingMinute))
        t <- of(schedule.setStartingMinuteOnFriday(key, startingMinute))
      yield t
    }

  def setDurationHoursOnMonday(id: UUID, key: Int, durationHours: Int) =
    withSchedule(id) { schedule =>
      for
        vhour <- of(ScheduleBlock.Hour(durationHours))
        t <- of(schedule.setDurationHoursOnMonday(key, durationHours))
      yield t
    }

  def setDurationHoursOnTuesday(id: UUID, key: Int, durationHours: Int) =
    withSchedule(id) { schedule =>
      for
        vhour <- of(ScheduleBlock.Hour(durationHours))
        t <- of(schedule.setDurationHoursOnTuesday(key, durationHours))
      yield t
    }

  def setDurationHoursOnWednesday(id: UUID, key: Int, durationHours: Int) =
    withSchedule(id) { schedule =>
      for
        vhour <- of(ScheduleBlock.Hour(durationHours))
        t <- of(schedule.setDurationHoursOnWednesday(key, durationHours))
      yield t
    }

  def setDurationHoursOnThursday(id: UUID, key: Int, durationHours: Int) =
    withSchedule(id) { schedule =>
      for
        vhour <- of(ScheduleBlock.Hour(durationHours))
        t <- of(schedule.setDurationHoursOnThursday(key, durationHours))
      yield t
    }

  def setDurationHoursOnFriday(id: UUID, key: Int, durationHours: Int) =
    withSchedule(id) { schedule =>
      for
        vhour <- of(ScheduleBlock.Hour(durationHours))
        t <- of(schedule.setDurationHoursOnFriday(key, durationHours))
      yield t
    }

  def setDurationMinuteOnMonday(id: UUID, key: Int, durationMinutes: Int) =
    withSchedule(id) { schedule =>
      for
        vhour <- of(ScheduleBlock.Minute(durationMinutes))
        t <- of(schedule.setDurationHoursOnMonday(key, durationMinutes))
      yield t
    }

  def setDurationMinuteOnTuesday(id: UUID, key: Int, durationMinutes: Int) =
    withSchedule(id) { schedule =>
      for
        vhour <- of(ScheduleBlock.Minute(durationMinutes))
        t <- of(schedule.setDurationHoursOnTuesday(key, durationMinutes))
      yield t
    }

  def setDurationMinuteOnWednesday(id: UUID, key: Int, durationMinutes: Int) =
    withSchedule(id) { schedule =>
      for
        vhour <- of(ScheduleBlock.Minute(durationMinutes))
        t <- of(schedule.setDurationHoursOnWednesday(key, durationMinutes))
      yield t
    }

  def setDurationMinuteOnThursday(id: UUID, key: Int, durationMinutes: Int) =
    withSchedule(id) { schedule =>
      for
        vhour <- of(ScheduleBlock.Minute(durationMinutes))
        t <- of(schedule.setDurationHoursOnThursday(key, durationMinutes))
      yield t
    }

  def setDurationMinuteOnFriday(id: UUID, key: Int, durationMinutes: Int) =
    withSchedule(id) { schedule =>
      for
        vhour <- of(ScheduleBlock.Minute(durationMinutes))
        t <- of(schedule.setDurationHoursOnFriday(key, durationMinutes))
      yield t
    }

  def deleteSchedule(id: UUID) = for
    schedule <- getSchedule(id)
    () <- notifyScheduleEvent(schedule.delete)
  yield ()
