package org.charles.angels.houses.domain.events

enum ContactEvent:
  case ContactCreated(
      ci: Int,
      name: String,
      lastname: String,
      phone: Option[String]
  )
  case CIChanged(ci: Int, newCi: Int)
  case NameChanged(ci: Int, name: String)
  case LastnameChanged(ci: Int, lastname: String)
  case PhoneChanged(ci: Int, phone: String)
