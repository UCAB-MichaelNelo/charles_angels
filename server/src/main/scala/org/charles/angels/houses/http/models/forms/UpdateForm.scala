package org.charles.angels.houses.http.models.forms

import cats.syntax.all.*
import io.circe.Decoder

final case class UpdateHouseNameForm(name: String)
final case class UpdateHouseRIFForm(rif: Int)
final case class AddPhoneToHouseForm(phone: String)
final case class RemovePhoneFromHouseForm(index: Int)
final case class UpdatePhoneOfHouseForm(index: Int, phone: String)
final case class UpdateMaxSharesOfHouseForm(maxShares: Int)
final case class UpdateCurrentSharesOfHouseForm(currentShares: Int)
final case class UpdateMinimumAgeOfHouseForm(minimumAge: Int)
final case class UpdateMaximumAgeOfHouseForm(maximumAge: Int)
final case class UpdateCurrentGirlsHelpedOfHouseForm(currentGirlsHelped: Int)
final case class UpdateCurrentBoysHelpedOfHouseForm(currentBoysHelped: Int)

final case class UpdateNameOfContactForm(name: String)
final case class UpdateLastnameOfContactForm(lastname: String)
final case class UpdatePhoneOfContactForm(phone: String)
final case class UpdateCIOfContactForm(ci: Int)

enum Day:
  case Monday
  case Tuesday
  case Wednesday
  case Thursday
  case Friday

given Decoder[Day] = Decoder[Int].emap(n =>
  n match {
    case 1 => Day.Monday.asRight
    case 2 => Day.Tuesday.asRight
    case 3 => Day.Wednesday.asRight
    case 4 => Day.Thursday.asRight
    case 5 => Day.Friday.asRight
    case e =>
      s"Day out of range [1-5], got $n".asLeft
  }
)

final case class AddBlockToScheduleForm(
    day: Day,
    startHour: Int,
    startMinute: Int,
    durationHours: Int,
    durationMinutes: Int
)

final case class RemoveBlockFromScheduleForm(
    day: Day,
    block: Int
)

final case class UpdateStartHourOnScheduleBlockForm(
    day: Day,
    key: Int,
    startHour: Int
)

final case class UpdateStartMinuteOnScheduleBlockForm(
    day: Day,
    key: Int,
    startMinute: Int
)

final case class UpdateDurationHourOnScheduleBlockForm(
    day: Day,
    key: Int,
    durationHour: Int
)

final case class UpdateDurationMinuteOnScheduleBlockForm(
    day: Day,
    key: Int,
    durationMinute: Int
)
