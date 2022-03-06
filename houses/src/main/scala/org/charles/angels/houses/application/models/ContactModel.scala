package org.charles.angels.houses.application.models

final case class ContactModel(
    ci: Int,
    name: String,
    lastname: String,
    phone: Option[String]
)
