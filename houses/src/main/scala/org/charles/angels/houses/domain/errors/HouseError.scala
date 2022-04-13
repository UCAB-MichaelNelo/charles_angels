package org.charles.angels.houses.domain.errors

import java.io.File
import cats.Show

enum HouseError:
  case EmptyImage extends HouseError
  case ImageTooLarge(size: Long) extends HouseError
  case EmptyName extends HouseError
  case NameTooLong(name: String) extends HouseError
  case InvalidRif(rif: Int) extends HouseError
  case NoPhonesProvided extends HouseError
  case InvalidPhone(phone: String) extends HouseError
  case EmptyPhone(index: Int) extends HouseError
  case EmptyAddress extends HouseError
  case AddressTooLoong(address: String) extends HouseError
  case MaxSharesIsZero extends HouseError
  case MaximumAgeIsZero extends HouseError
  case MinimumAgeIsGreaterThanMaximumAge(minimumAge: Int, maximumAge: Int)
      extends HouseError
