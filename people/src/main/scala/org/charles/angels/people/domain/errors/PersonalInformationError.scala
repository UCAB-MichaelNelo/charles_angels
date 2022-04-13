package org.charles.angels.people.domain.errors

enum PersonalInformationError {
  case InvalidCI
  case EmptyName
  case EmptyLastname
  case NameTooLong
  case LastnameTooLong
  case InvalidBirthdate
}
