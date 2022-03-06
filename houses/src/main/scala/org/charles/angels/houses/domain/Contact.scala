package org.charles.angels.houses.domain

import cats.syntax.all.*
import cats.data.ValidatedNec
import org.charles.angels.houses.domain.errors.ContactError
import org.charles.angels.houses.domain.events.ContactEvent
import monocle.macros.GenLens
import java.util.UUID

final case class Contact private (
    ci: Int,
    name: String,
    lastname: String,
    phone: Option[String]
) { contact =>

  def setCI(newCi: Int) =
    (Contact.ci.set(newCi)(contact), ContactEvent.CIChanged(ci, newCi))

  def setName(newName: String) =
    (Contact.name.set(newName)(contact), ContactEvent.NameChanged(ci, newName))

  def setLastname(newLastname: String) =
    (
      Contact.lastname.set(newLastname)(contact),
      ContactEvent.LastnameChanged(ci, newLastname)
    )

  def setPhone(phone: String) =
    (
      Contact.phone.set(phone.some)(contact),
      ContactEvent.PhoneChanged(ci, phone)
    )
}

object Contact:
  private def ci = GenLens[Contact](_.ci)
  private def name = GenLens[Contact](_.name)
  private def lastname = GenLens[Contact](_.lastname)
  private def phone = GenLens[Contact](_.phone)

  object CI:
    def apply(ci: Int) =
      (if (ci > 0 || ci.toString.length > 9) ci.validNec
       else ContactError.InvalidCI.invalidNec)

  object Name:
    private val MAX_SIZE = 50
    def apply(name: String) =
      (if (!name.isBlank) name.validNec
       else ContactError.EmptyName.invalidNec) *>
        (if (name.length > MAX_SIZE) name.validNec
         else ContactError.NameTooLong.invalidNec)

  object Lastname:
    private val MAX_SIZE = 75
    def apply(lastname: String) =
      (if (!lastname.isBlank) lastname.validNec
       else ContactError.EmptyLastname.invalidNec) *>
        (if (lastname.length > MAX_SIZE) lastname.validNec
         else ContactError.LastnameTooLong.invalidNec)

  object Phone:
    private val FORMAT = "[0-9]{3}-[0-9]{3}-[0-9]{4}".r
    def apply(
        phone: String
    ) =
      (if (!phone.isBlank) phone.validNec
       else ContactError.EmptyPhone.invalidNec) *>
        (if (FORMAT.matches(phone)) phone.validNec
         else ContactError.InvalidPhone.invalidNec)

  def apply(
      ci: Int,
      name: String,
      lastname: String,
      phone: Option[String]
  ) = (
    CI(ci),
    Name(name),
    Lastname(lastname),
    phone.traverse(Phone(_))
  )
    .mapN(new Contact(_, _, _, _))
    .map(c =>
      (c, ContactEvent.ContactCreated(c.ci, c.name, c.lastname, c.phone))
    )
