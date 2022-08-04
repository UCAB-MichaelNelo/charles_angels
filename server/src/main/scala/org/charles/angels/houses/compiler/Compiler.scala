package org.charles.angels.houses.compiler

import cats.~>
import cats.syntax.all.*
import org.charles.angels.houses.compiler.ApplicationAction
import org.charles.angels.houses.application.ApplicationAction as HousesApplicationAction
import org.charles.angels.people.application.ApplicationAction as PeopleApplicationAction
import org.charles.angels.houses.compiler.ServerLanguage
import org.charles.angels.houses.application.services.ServiceAction
import org.charles.angels.houses.application.queries.QueryAction
import org.charles.angels.houses.application.events.DomainEventAction
import org.charles.angels.houses.domain.events.HouseEvent
import org.charles.angels.houses.domain.events.ContactEvent
import org.charles.angels.people.application.queries.QueryAction as PeopleQueryAction
import org.charles.angels.people.application.events.EventAction as PeopleEventAction
import org.charles.angels.people.domain.events.ChildEvent
import org.charles.angels.houses.db.ChildModel

object Compiler extends (ApplicationAction ~> ServerLanguage):
  // Houses compiler
  private object HouseServiceCompiler extends (ServiceAction ~> ServerLanguage):
    def apply[A](serviceAction: ServiceAction[A]) = serviceAction match
      case ServiceAction.AllocFile(contents, name) =>
        CompilerDSL.allocateFile(contents, name)
      case ServiceAction.DeallocFile(file) => CompilerDSL.deallocateFile(file)

  private object HouseQueryCompiler extends (QueryAction ~> ServerLanguage):
    def apply[A](queryAction: QueryAction[A]) = queryAction match
      case QueryAction.GetAllContacts     => CompilerDSL.getAllContacts
      case QueryAction.DoesRIFExists(rif) => CompilerDSL.doesRifExist(rif)
      case QueryAction.GetAllHouses       => CompilerDSL.getAllHouses
      case QueryAction.GetHouse(id)       => CompilerDSL.findHouse(id)
      case QueryAction.GetContact(id)     => CompilerDSL.findContact(id)

  private object HouseEventCompiler
      extends (DomainEventAction ~> ServerLanguage):
    def apply[A](domainEventAction: DomainEventAction[A]) =
      domainEventAction match
        case DomainEventAction.NotifyHouseEvent(
              HouseEvent.HouseCreated(
                id,
                img,
                name,
                rif,
                phones,
                address,
                maxShares,
                currentShares,
                minimumAge,
                maximumAge,
                currentGirlsHelped,
                currentBoysHelped,
                contactCI,
                scheduleStartTime,
                scheduleEndTime
              )
            ) =>
          CompilerDSL.registerHouse(
            id,
            img,
            name,
            rif,
            phones,
            address,
            maxShares,
            currentShares,
            minimumAge,
            maximumAge,
            currentGirlsHelped,
            currentBoysHelped,
            contactCI,
            scheduleStartTime,
            scheduleEndTime
          )
        case DomainEventAction.NotifyHouseEvent(
              HouseEvent.ImageUpdated(id, file)
            ) =>
          CompilerDSL.updateImageOfHouse(id, file)
        case DomainEventAction.NotifyHouseEvent(
              HouseEvent.NameUpdated(id, name)
            ) =>
          CompilerDSL.updateNameOfHouse(id, name)
        case DomainEventAction.NotifyHouseEvent(
              HouseEvent.RIFUpdated(id, rif)
            ) =>
          CompilerDSL.updateRIFOfHouse(id, rif)
        case DomainEventAction.NotifyHouseEvent(
              HouseEvent.AddressUpdated(id, address)
            ) =>
            CompilerDSL.updateAddressOfHouse(id, address)
        case DomainEventAction.NotifyHouseEvent(
              HouseEvent.PhoneAdded(id, phone)
            ) =>
          CompilerDSL.addPhoneToHouse(id, phone)
        case DomainEventAction.NotifyHouseEvent(
              HouseEvent.PhoneRemoved(id, key)
            ) =>
          CompilerDSL.removePhoneOfHouse(id, key)
        case DomainEventAction.NotifyHouseEvent(
              HouseEvent.PhoneUpdated(id, key, phone)
            ) =>
          CompilerDSL.updatePhoneOfHouse(id, key, phone)
        case DomainEventAction.NotifyHouseEvent(
              HouseEvent.MaxSharesUpdated(id, maxShare)
            ) =>
          CompilerDSL.updateMaxSharesOfHouse(id, maxShare)
        case DomainEventAction.NotifyHouseEvent(
              HouseEvent.CurrentSharesUpdated(id, currentShares)
            ) =>
          CompilerDSL.updateCurrentSharesOfHouse(id, currentShares)
        case DomainEventAction.NotifyHouseEvent(
              HouseEvent.MinimumAgeUpdated(id, minimumAge)
            ) =>
          CompilerDSL.updateMinimumAgeOfHouse(id, minimumAge)
        case DomainEventAction.NotifyHouseEvent(
              HouseEvent.MaximumAgeUpdated(id, maximumAge)
            ) =>
          CompilerDSL.updateMaximumAgeOfHouse(id, maximumAge)
        case DomainEventAction.NotifyHouseEvent(
              HouseEvent.CurrentGirlsHelpedUpdated(id, currentGirlsHelped)
            ) =>
          CompilerDSL.updateCurrentGirlsHelpedOfHouse(id, currentGirlsHelped)
        case DomainEventAction.NotifyHouseEvent(
              HouseEvent.CurrentBoysHelpedUpdated(id, currentBoysHelped)
            ) =>
          CompilerDSL.updateCurrentBoysHelpedOfHouse(id, currentBoysHelped)
        case DomainEventAction.NotifyHouseEvent(
            HouseEvent.ScheduleStartTimeUpdated(id, startTime)
          ) => 
            CompilerDSL.updateStartScheduleTime(id, startTime)
        case DomainEventAction.NotifyHouseEvent(
            HouseEvent.ScheduleEndTimeUpdated(id, endTime)
          ) =>
            CompilerDSL.updateScheduleEndTime(id, endTime)
        case DomainEventAction.NotifyHouseEvent(
              HouseEvent.HouseDeleted(id)
            ) =>
          CompilerDSL.eliminateHouse(id).void
        case DomainEventAction.NotifyHouseEvent(
            HouseEvent.HouseContactCIUpdated(id, ci)
          ) => CompilerDSL.updateContactCIOfHouse(id, ci)
        case DomainEventAction.NotifyContactEvent(
              ContactEvent.ContactCreated(
                ci,
                name,
                lastname,
                phone
              )
            ) =>
          CompilerDSL.registerContact(ci, name, lastname, phone)
        case DomainEventAction.NotifyContactEvent(
              ContactEvent.CIChanged(ci, newCi)
            ) =>
          CompilerDSL.changeCIOfContact(ci, newCi)
        case DomainEventAction.NotifyContactEvent(
              ContactEvent.NameChanged(ci, name)
            ) =>
          CompilerDSL.changeNameOfContact(ci, name)
        case DomainEventAction.NotifyContactEvent(
              ContactEvent.LastnameChanged(ci, lastname)
            ) =>
          CompilerDSL.changeLastnameOfContact(ci, lastname)
        case DomainEventAction.NotifyContactEvent(
              ContactEvent.PhoneChanged(ci, phone)
            ) =>
          CompilerDSL.changePhoneOfContact(ci, phone)
        case DomainEventAction.NotifyContactEvent(
              ContactEvent.ContactDeleted(ci)
            ) =>
          CompilerDSL.eliminateContact(ci).void

  // People compiler
  object PeopleQueryCompiler extends (PeopleQueryAction ~> ServerLanguage) {
    def apply[A](action: PeopleQueryAction[A]) = action match {
      case PeopleQueryAction.GetChild(id) => CompilerDSL.findChild(id)
      case PeopleQueryAction.GetChildrenOfHouse(houseId) => CompilerDSL.getAllChildrenOfHouse(houseId)
      case PeopleQueryAction.FindExistantPersonalInformation(ci) => CompilerDSL.getPersonalInformation(ci)
      case PeopleQueryAction.GetAllExistantPersonalInformation => CompilerDSL.getAllPersonalInformation
      case PeopleQueryAction.DoesChildCiExist(ci) => CompilerDSL.doesChidlCiExist(ci)
      case PeopleQueryAction.GetChildrenPersonalInformation => CompilerDSL.getChildrenPersonalInformation
      case PeopleQueryAction.GetChildrenWithoutHousing => CompilerDSL.getChildrenWithoutHousing
    }
  }

  object PeopleEventCompiler extends (PeopleEventAction ~> ServerLanguage) {
    def apply[A](action: PeopleEventAction[A]) = action match {
      case PeopleEventAction.Publish(
            ChildEvent.ChildCreated(houseId, cInf, wear, id)
          ) =>
        CompilerDSL.saveChild(ChildModel(id, houseId, cInf, wear))
      case PeopleEventAction.Publish(
            ChildEvent.PersonalInformationUpdated(ci, pi, isOfChild)
          ) =>
        CompilerDSL.updatePersonalInformation(ci, pi, isOfChild)
      case PeopleEventAction.Publish(
            ChildEvent.FatherInformationUpdated(id, ci)
          ) =>
        CompilerDSL.setFatherCIToChild(id, ci)
      case PeopleEventAction.Publish(
            ChildEvent.MotherInformationUpdated(id, ci)
          ) =>
        CompilerDSL.setMotherCIToChild(id, ci)
      case PeopleEventAction.Publish(
            ChildEvent.NonParentInformationUpdated(id, ci)
          ) =>
        CompilerDSL.setTutorCIToChild(id, ci)
      case PeopleEventAction.Publish(
            ChildEvent.RelatedBeneficiaryAdded(id, bid)
          ) =>
        CompilerDSL.info(f"Agregando beneficiario relacionado") >> 
        CompilerDSL.addRelatedBeneficiary(id, bid).onError {
          case e => CompilerDSL.error(f"Error agregando beneficiario relacionado: $e")
        }
      case PeopleEventAction.Publish(
            ChildEvent.RelatedBeneficiaryRemoved(id, bid)
          ) =>
        CompilerDSL.removeRelatedBeneficiary(id, bid)
      case PeopleEventAction.Publish(
            ChildEvent.PhotoUpdated(ci, img)
          ) =>
        CompilerDSL.updateChildPhoto(ci, img.getAbsolutePath)
      case PeopleEventAction.Publish(
            ChildEvent.AttireUpdated(ci, wear)
          ) =>
        CompilerDSL.updateChildAttire(ci, wear)
      case PeopleEventAction.Publish(
            ChildEvent.BoyDeleted(id)
          ) =>
        CompilerDSL.eliminateChild(id)
      case PeopleEventAction.Publish(
            ChildEvent.GirlDeleted(id)
          ) =>
        CompilerDSL.eliminateChild(id)
    }
  }
  private val houseCompiler: (HousesApplicationAction ~> ServerLanguage) =
    HouseQueryCompiler.or(HouseEventCompiler.or(HouseServiceCompiler))
  private val peopleCompiler: (PeopleApplicationAction ~> ServerLanguage) =
    PeopleQueryCompiler.or(PeopleEventCompiler)

  private val compiler = houseCompiler or peopleCompiler

  def apply[A](action: ApplicationAction[A]) =
    compiler(action)
