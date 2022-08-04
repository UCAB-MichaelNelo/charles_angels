package org.charles.angels.people.application.errors

import org.charles.angels.people.domain.errors.DressError
import org.charles.angels.people.domain.PersonalInformation
import cats.Inject
import org.charles.angels.people.domain.errors.PersonalInformationError
import java.util.UUID

enum ApplicationError {
  case DomainDressError(error: DressError)
  case DomainPersonalInformationError(error: PersonalInformationError)
  case ChildNotFound(id: UUID)
  case PersonalInformationNotFound(ci: Int)
}

given Inject[DressError, ApplicationError] with
  def inj = (error: DressError) => ApplicationError.DomainDressError(error)
  def prj = (err: ApplicationError) =>
    err match {
      case ApplicationError.DomainDressError(err) => Some(err)
      case _                                      => None
    }

given Inject[PersonalInformationError, ApplicationError] with
  def inj = (error: PersonalInformationError) =>
    ApplicationError.DomainPersonalInformationError(error)
  def prj = (err: ApplicationError) =>
    err match {
      case ApplicationError.DomainPersonalInformationError(err) => Some(err)
      case _                                                    => None
    }
