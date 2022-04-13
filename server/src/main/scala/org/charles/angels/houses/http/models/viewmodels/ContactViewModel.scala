package org.charles.angels.houses.http.models.viewmodels

import org.charles.angels.houses.domain.Contact

final case class ContactViewModel(
    ci: Int,
    phone: Option[String],
    name: String,
    lastname: String
)

object ContactViewModel:
  def apply(contact: Contact) =
    new ContactViewModel(
      contact.ci,
      contact.phone,
      contact.name,
      contact.lastname
    )
