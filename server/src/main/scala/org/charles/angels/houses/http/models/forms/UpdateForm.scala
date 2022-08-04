package org.charles.angels.houses.http.models.forms

import cats.syntax.all.*
import io.circe.Decoder
import java.time.LocalTime
import java.util.UUID

final case class UpdateHouseNameForm(name: String)
final case class UpdateHouseRIFForm(rif: Int)
final case class UpdateHouseAddressForm(address: String)
final case class AddPhoneToHouseForm(phone: String)
final case class RemovePhoneFromHouseForm(index: Int)
final case class UpdatePhoneOfHouseForm(index: Int, phone: String)
final case class UpdateMaxSharesOfHouseForm(maxShares: Int)
final case class UpdateCurrentSharesOfHouseForm(currentShares: Int)
final case class UpdateMinimumAgeOfHouseForm(minimumAge: Int)
final case class UpdateMaximumAgeOfHouseForm(maximumAge: Int)
final case class UpdateCurrentGirlsHelpedOfHouseForm(currentGirlsHelped: Int)
final case class UpdateCurrentBoysHelpedOfHouseForm(currentBoysHelped: Int)
final case class UpdateScheduleStartTime(scheduleStartTime: LocalTime)
final case class UpdateScheduleEndTime(scheduleEndTime: LocalTime)
final case class UpdateContactCIOfHouse(contactCI: Int)

final case class UpdateNameOfContactForm(name: String)
final case class UpdateLastnameOfContactForm(lastname: String)
final case class UpdatePhoneOfContactForm(phone: String)
final case class UpdateCIOfContactForm(ci: Int)

final case class UpdateHouseOfChildForm(id: UUID)