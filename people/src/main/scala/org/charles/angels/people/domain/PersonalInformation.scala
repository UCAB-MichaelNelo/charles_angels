package org.charles.angels.people.domain

import cats.syntax.all.*
import org.charles.angels.people.domain.errors.PersonalInformationError
import cats.data.State
import java.time.LocalDate
import java.time.temporal.ChronoUnit.YEARS

final case class PersonalInformation(
    ci: Int,
    name: String,
    lastname: String,
    birthdate: LocalDate
) {
  def setCI(newCi: Int) =
    copy(ci = newCi)
  def setName(newName: String) =
    copy(name = newName)
  def setLastname(newLastname: String) =
    copy(lastname = newLastname)
  def setBirthdate(newDate: LocalDate) =
    copy(birthdate = newDate)
  def age = YEARS.between(birthdate, LocalDate.now)
}

object PersonalInformation {
  object CI:
    def apply(ci: Int) =
      (if (ci > 0 || ci.toString.length > 9) ci.validNec
       else PersonalInformationError.InvalidCI.invalidNec)

  object Name:
    private val MAX_SIZE = 50
    def apply(name: String) =
      (if (!name.isBlank) name.validNec
       else PersonalInformationError.EmptyName.invalidNec) *>
        (if (name.length < MAX_SIZE) name.validNec
         else PersonalInformationError.NameTooLong.invalidNec)

  object Lastname:
    private val MAX_SIZE = 75
    def apply(lastname: String) =
      (if (!lastname.isBlank) lastname.validNec
       else PersonalInformationError.EmptyLastname.invalidNec) *>
        (if (lastname.length < MAX_SIZE) lastname.validNec
         else PersonalInformationError.LastnameTooLong.invalidNec)

  object Birthdate:
    def apply(date: LocalDate) =
      (if (date.isAfter(LocalDate.now))
         PersonalInformationError.InvalidBirthdate.invalidNec
       else date.validNec)

  def apply(
      ci: Int,
      name: String,
      lastname: String,
      date: LocalDate
  ) =
    (
      CI(ci),
      Name(name),
      Lastname(lastname),
      Birthdate(date)
    )
      .mapN(
        new PersonalInformation(_, _, _, _)
      )

  def unsafe(
      ci: Int,
      name: String,
      lastname: String,
      birthdate: LocalDate
  ) = new PersonalInformation(ci, name, lastname, birthdate)
}
