package org.charles.angels.houses.domain

import cats.syntax.all.*
import cats.data.ValidatedNec
import java.io.File
import org.charles.angels.houses.domain.errors.HouseError
import java.util.UUID
import org.charles.angels.houses.domain.events.HouseEvent

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
    scheduleId: UUID
) { house =>

  // House Information

  def setImage(file: File) =
    (copy(img = file), HouseEvent.ImageUpdated(id, file))

  def setName(name: String) =
    (copy(name = name), HouseEvent.NameUpdated(id, name))

  def setRIF(rif: Int) =
    (copy(rif = rif), HouseEvent.RIFUpdated(id, rif))

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
    copy(phones = phones.updated(key, phone).distinct),
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
    HouseEvent.MaximumAgeUpdateed(id, newMaximumAge)
  )

  def setCurrentBoysHelped(newCurrentBoysHelped: Int) = (
    copy(currentBoysHelped = newCurrentBoysHelped),
    HouseEvent.CurrentBoysHelpedUpdated(id, newCurrentBoysHelped)
  )

  def setCurrentGirlsHelped(newCurrentGirlsHelped: Int) = (
    copy(currentGirlsHelped = newCurrentGirlsHelped),
    HouseEvent.CurrentGirlsHelpedUpdated(id, newCurrentGirlsHelped)
  )

  // Contact

  def setContact(ci: Int) = copy(contactCI = ci)

  // Schedule

  def setSchedule(id: UUID) = copy(scheduleId = id)

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
      (if (!phones.isEmpty) HouseError.NoPhonesProvided.invalidNec
       else phones.validNec) *>
        phones.mapWithIndex(validatePhone).sequence *>
        phones.validNec

  object Address:
    private val MAX_SIZE = 255
    def apply(address: String) =
      (if (!address.isBlank) address.validNec
       else HouseError.EmptyAddress.invalidNec) *>
        (if (address.length <= 255) address.validNec
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

  def apply(
      img: File,
      name: String,
      rif: Int,
      phones: Vector[String],
      address: String,
      contactCI: Int,
      scheduleId: UUID,
      maxShares: Int,
      currentShares: Int,
      minimumAge: Int,
      maximumAge: Int,
      currentGirlsHelped: Int,
      currentBoysHelped: Int
  ) = (
    Image(img),
    Name(name),
    RIF(rif),
    Phones(phones),
    Address(address),
    MaxShares(maxShares),
    AgeLimits(minimumAge, maximumAge)
  ).mapN { (vimg, vname, vrif, vphones, vaddress, vmaxShares, vAgeLimit) =>
    val (vminAge, vmaxAge) = vAgeLimit
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
      scheduleId
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
        h.scheduleId
      )
    )
  )
