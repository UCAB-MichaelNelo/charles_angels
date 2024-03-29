package org.charles.angels.houses.domain

import cats.syntax.all.*
import cats.data.ValidatedNec
import java.io.File
import java.util.UUID
import java.time.LocalTime
import errors.HouseError
import events.HouseEvent
import java.time.Period
import java.time.LocalDate

final case class House private (
    id: UUID,
    img: File,
    name: String,
    rif: Int,
    phones: Vector[String],
    address: String,
    maxShares: Int,
    currentShares: Int,
    minimumAge: Int,
    maximumAge: Int,
    currentGirlsHelped: Int,
    currentBoysHelped: Int,
    contactCI: Int,
    scheduleStartTime: LocalTime,
    scheduleEndTime: LocalTime
) { house =>

  // House Information

  def fileExtension = 
    img.getAbsolutePath.split("\\.").last

  def setImage(file: File) =
    (copy(img = file), HouseEvent.ImageUpdated(id, file))

  def setName(name: String) =
    (copy(name = name), HouseEvent.NameUpdated(id, name))

  def setRIF(rif: Int) =
    (copy(rif = rif), HouseEvent.RIFUpdated(id, rif))

  def setAddress(address: String) =
    copy(address = address) -> HouseEvent.AddressUpdated(id, address)

  def addPhone(phone: String) =
    (
      copy(phones = (phones :+ phone).distinct),
      HouseEvent.PhoneAdded(id, phone)
    )

  def removePhone(key: Int) =
    (
      copy(phones =
        phones.zipWithIndex.filterNot((_, idx) => idx == key).map(_._1)
      ),
      HouseEvent.PhoneRemoved(id, key)
    )
  def updatePhone(key: Int, phone: String) = (
    copy(phones =
      phones
        .get(key)
        .map(_ => phones.updated(key, phone).distinct)
        .fold(phones)(identity)
    ),
    HouseEvent.PhoneUpdated(id, key, phone)
  )
  def setMaxShares(newMaxShares: Int) = (
    copy(maxShares = newMaxShares),
    HouseEvent.MaxSharesUpdated(id, newMaxShares)
  )

  def setCurrentShares(newCurrentShares: Int) = (
    copy(currentShares = newCurrentShares),
    HouseEvent.CurrentSharesUpdated(id, newCurrentShares)
  )

  def setMinimumAge(newMinimumAge: Int) = (
    copy(minimumAge = newMinimumAge),
    HouseEvent.MinimumAgeUpdated(id, newMinimumAge)
  )

  def setMaximumAge(newMaximumAge: Int) = (
    copy(maximumAge = newMaximumAge),
    HouseEvent.MaximumAgeUpdated(id, newMaximumAge)
  )

  def setCurrentBoysHelped(newCurrentBoysHelped: Int) = (
    copy(currentBoysHelped = newCurrentBoysHelped),
    HouseEvent.CurrentBoysHelpedUpdated(id, newCurrentBoysHelped)
  )

  def setCurrentGirlsHelped(newCurrentGirlsHelped: Int) = (
    copy(currentGirlsHelped = newCurrentGirlsHelped),
    HouseEvent.CurrentGirlsHelpedUpdated(id, newCurrentGirlsHelped)
  )

  def setScheduleStartTime(startTime: LocalTime) =
    House.Schedule(startTime, house.scheduleEndTime)
      .map { (startTime, _) => 
        copy(scheduleStartTime = startTime) -> 
        HouseEvent.ScheduleStartTimeUpdated(id, startTime) 
      }

  def setScheduleEndTime(endTime: LocalTime) =
    House.Schedule(house.scheduleStartTime, endTime)
      .map { (_, endTime) => 
        copy(scheduleEndTime = endTime) -> 
        HouseEvent.ScheduleEndTimeUpdated(id, endTime) 
      }

  def validateAgeBounds(birthdate: LocalDate) =
    val period = Period.between(birthdate, LocalDate.now)
    val age = period.getYears

    age >= minimumAge && age <= maximumAge

  def canHaveNewBeneficiaries =
    currentShares < maxShares

  // Contact

  def setContact(ci: Int) = copy(contactCI = ci) -> HouseEvent.HouseContactCIUpdated(id, ci)

  // Schedule

  def delete = HouseEvent.HouseDeleted(id)
}

object House:
  object Image:
    private val MAX_SIZE = 20 * 1024 * 1024
    def apply(file: File) =
      (if (file.length > 0) file.validNec
       else HouseError.EmptyImage.invalidNec) *>
        (if (file.length <= MAX_SIZE) file.validNec
         else HouseError.ImageTooLarge(file.length).invalidNec)

  object Name:
    private val MAX_SIZE = 50
    def apply(name: String) =
      (if (!name.isBlank) name.validNec
       else HouseError.EmptyImage.invalidNec) *>
        (if (name.length <= MAX_SIZE) name.validNec
         else HouseError.NameTooLong(name).invalidNec)

  object RIF:
    private val MAX_SIZE = 9
    def apply(rif: Int): ValidatedNec[HouseError, Int] =
      (if (rif.toString.length == 9) rif.validNec
       else HouseError.InvalidRif(rif).invalidNec)

  object Phones:
    private val FORMAT = "[0-9]{3}-[0-9]{3}-[0-9]{4}".r
    def validatePhone(
        phone: String,
        index: Int
    ): ValidatedNec[HouseError, String] =
      (if (!phone.isBlank) phone.validNec[HouseError]
       else HouseError.EmptyPhone(index).invalidNec[String]) *>
        (if (FORMAT.matches(phone)) phone.validNec[HouseError]
         else HouseError.InvalidPhone(phone).invalidNec[String])

    def apply(phones: Vector[String]) =
      (if (phones.isEmpty) HouseError.NoPhonesProvided.invalidNec
       else phones.validNec) *>
        phones.mapWithIndex(validatePhone).sequence *>
        phones.validNec

  object Address:
    private val MAX_SIZE = 255
    def apply(address: String) =
      (if (!address.isBlank) address.validNec
       else HouseError.EmptyAddress.invalidNec) *>
        (if (address.length <= MAX_SIZE) address.validNec
         else HouseError.AddressTooLoong(address).invalidNec)

  object MaxShares:
    def apply(maxShares: Int) =
      (if (maxShares > 0) maxShares.validNec
       else HouseError.MaxSharesIsZero.invalidNec)

  object AgeLimits:
    def apply(minAge: Int, maxAge: Int) =
      (if (maxAge > 0) maxAge.validNec
       else HouseError.MaximumAgeIsZero.invalidNec) *>
        (if (minAge < maxAge) (minAge, maxAge).validNec
         else
           HouseError
             .MinimumAgeIsGreaterThanMaximumAge(minAge, maxAge)
             .invalidNec)

  object Schedule:
    def apply(startTime: LocalTime, endTime: LocalTime): ValidatedNec[HouseError, (LocalTime, LocalTime)] =
      (if (startTime.isAfter(endTime)) HouseError.ScheduleStartTimeIsAfterEndTime.invalidNec
      else startTime.validNec) *>
        (if (endTime.isBefore(startTime)) HouseError.ScheduleEndTimeIsBeforeStartTime.invalidNec
        else endTime.validNec) *>
          (if (startTime.compareTo(endTime) != 0) (startTime, endTime).validNec
          else HouseError.ScheduleStartTimeAndEndTimeAreEqual.invalidNec)

  def apply(
      img: File,
      name: String,
      rif: Int,
      phones: Vector[String],
      address: String,
      contactCI: Int,
      maxShares: Int,
      currentShares: Int,
      minimumAge: Int,
      maximumAge: Int,
      currentGirlsHelped: Int,
      currentBoysHelped: Int,
      scheduleStartTime: LocalTime,
      scheduleEndTime: LocalTime
  ) = (
    Image(img),
    Name(name),
    RIF(rif),
    Phones(phones),
    Address(address),
    MaxShares(maxShares),
    AgeLimits(minimumAge, maximumAge),
    Schedule(scheduleStartTime, scheduleEndTime)
  ).mapN { (vimg, vname, vrif, vphones, vaddress, vmaxShares, vAgeLimit, vScheduleTime) =>
    val (vminAge, vmaxAge) = vAgeLimit
    val (vScheduleStartTime, vScheduleEndTime) = vScheduleTime
    new House(
      UUID.randomUUID,
      vimg,
      vname,
      vrif,
      vphones.distinct,
      vaddress,
      vmaxShares,
      currentShares,
      vminAge,
      vmaxAge,
      currentGirlsHelped,
      currentBoysHelped,
      contactCI,
      vScheduleStartTime,
      vScheduleEndTime
    )
  }.map(h =>
    (
      h,
      HouseEvent.HouseCreated(
        h.id,
        h.img,
        h.name,
        h.rif,
        h.phones,
        h.address,
        h.maxShares,
        h.currentShares,
        h.minimumAge,
        h.maximumAge,
        h.currentGirlsHelped,
        h.currentBoysHelped,
        h.contactCI,
        h.scheduleStartTime,
        h.scheduleEndTime
      )
    )
  )

  def unsafe(
      id: UUID,
      img: File,
      name: String,
      rif: Int,
      phones: Vector[String],
      address: String,
      contactCI: Int,
      maxShares: Int,
      currentShares: Int,
      minimumAge: Int,
      maximumAge: Int,
      currentGirlsHelped: Int,
      currentBoysHelped: Int,
      scheduleStartTime: LocalTime,
      scheduleEndTime: LocalTime
  ) = new House(
    id,
    img,
    name,
    rif,
    phones.distinct,
    address,
    maxShares,
    currentShares,
    minimumAge,
    maximumAge,
    currentGirlsHelped,
    currentBoysHelped,
    contactCI,
    scheduleStartTime,
    scheduleEndTime
  )
