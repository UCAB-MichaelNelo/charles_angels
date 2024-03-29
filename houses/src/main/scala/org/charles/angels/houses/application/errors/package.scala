package org.charles.angels.houses.application

import cats.Inject
import org.charles.angels.houses.domain.errors.HouseError
import org.charles.angels.houses.domain.errors.ContactError

package object errors:
  given Inject[HouseError, ApplicationError] =
    new Inject:
      def inj = (error: HouseError) => ApplicationError.HouseDomainError(error)
      def prj = (err: ApplicationError) =>
        err match
          case ApplicationError.HouseDomainError(err) => Some(err)
          case _                                      => None

  given Inject[ContactError, ApplicationError] =
    new Inject:
      def inj = (error: ContactError) =>
        ApplicationError.ContactDomainError(error)
      def prj = (err: ApplicationError) =>
        err match
          case ApplicationError.ContactDomainError(err) => Some(err)
          case _                                        => None