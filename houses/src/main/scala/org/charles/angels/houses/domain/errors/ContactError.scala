package org.charles.angels.houses.domain.errors

enum ContactError:
  case InvalidCI
  case EmptyName
  case NameTooLong
  case EmptyLastname
  case LastnameTooLong
  case EmptyPhone
  case InvalidPhone
