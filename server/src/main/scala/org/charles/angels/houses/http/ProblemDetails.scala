package org.charles.angels.houses.http

import cats.syntax.all.*
import io.circe.syntax.*
import cats.data.NonEmptyChain
import org.charles.angels.houses.application.errors.ApplicationError as HouseApplicationError
import org.charles.angels.people.application.errors.ApplicationError as ChildApplicationError
import org.charles.angels.houses.domain.errors.HouseError
import cats.data.Chain
import org.charles.angels.houses.domain.errors.ContactError
import io.circe.Encoder
import io.circe.JsonObject
import cats.Eval
import org.charles.angels.people.domain.errors.DressError
import org.charles.angels.people.domain.errors.PersonalInformationError

enum ProblemDetails { details =>
  case Single(
      `type`: String,
      status: Int,
      title: String,
      detail: String
  )
  case Multiple(problems: NonEmptyChain[ProblemDetails])

  def statusCode: Option[Int] = details match {
    case Single(_, status, _, _) => status.some
    case Multiple(_)             => None
  }
}

given Encoder[ProblemDetails] with
  def apply(details: ProblemDetails) = details match {
    case ProblemDetails.Single(t, status, title, detail) =>
      JsonObject(
        "type" -> t.asJson,
        "status" -> status.asJson,
        "title" -> title.asJson,
        "detail" -> detail.asJson
      ).asJson
    case ProblemDetails.Multiple(nec) if nec.size == 1 =>
      apply(nec.head)
    case ProblemDetails.Multiple(nec) =>
      JsonObject(
        "type" -> "multiple".asJson,
        "detail" -> nec
          .traverse(detail => Eval.later { apply(detail) })
          .value
          .asJson
      ).asJson
  }

trait Detailer[E] {
  def detail(e: E): ProblemDetails
}

extension [A](a: A) {
  def details(using D: Detailer[A]) = D.detail(a)
}

given Detailer[HouseApplicationError] with
  private val houseHandler: HouseError => ProblemDetails = {
    case HouseError.ScheduleEndTimeIsBeforeStartTime =>
      ProblemDetails.Single(
        "ScheduleEndTimeIsBeforeStartTime",
        422,
        "La hora de fin es anterior a la hora de inicio",
        "Establezca una hora de fin que no ocurra antes de la hora de inicio"
      )
    case HouseError.ScheduleStartTimeIsAfterEndTime =>
      ProblemDetails.Single(
        "ScheduleStartTimeIsAfterEndTime",
        422,
        "La hora de inicio es posterior a la hora de fin",
        "Establezca una hora de inicio que no ocurra despues de la hora de fin"
      )
    case HouseError.ScheduleStartTimeAndEndTimeAreEqual =>
      ProblemDetails.Single(
        "ScheduleStartTimeAndEndTimeAreEqual",
        422,
        "La hora de inicio y fin son iguales",
        "Establezca horas diferentes para la hora de inicio y la hora de fin"
      )
    case HouseError.EmptyImage =>
      ProblemDetails.Single(
        "EmptyImage",
        422,
        "La imagen esta vacia",
        "La imagen dada para la casa esta vacia"
      )
    case HouseError.ImageTooLarge(size) =>
      ProblemDetails.Single(
        "ImageTooLarge",
        422,
        "La imagen es muy grande",
        f"La imagen supera el tamaño de 200MB, actual tamaño $size"
      )
    case HouseError.EmptyName =>
      ProblemDetails.Single(
        "EmptyName",
        422,
        "El nombre de la casa esta vacio",
        "El nombre de la casa esta vacio"
      )
    case HouseError.NameTooLong(name) =>
      ProblemDetails.Single(
        "NameTooLong",
        422,
        "El nombre de la casa es muy grande",
        s"""El nombre de la casa es muy largo, actual: $name"""
      )
    case HouseError.InvalidRif(rif) =>
      ProblemDetails.Single(
        "InvalidRIF",
        422,
        "El RIF es invalido",
        s"El rif proveido no tiene 9 caracteres, rif: $rif"
      )
    case HouseError.NoPhonesProvided =>
      ProblemDetails.Single(
        "NoPhonesProvided",
        422,
        s"Telefonos vacios",
        s"No se proveyo ningun telefono asociado la casa"
      )
    case HouseError.InvalidPhone(phone) =>
      ProblemDetails.Single(
        "InvalidPhone",
        422,
        s"Telefono Invalido",
        s"""El telefono proveido "$phone" no cumple el formato adecuado (xxx-xxx-xxxx)"""
      )
    case HouseError.EmptyPhone(idx) =>
      ProblemDetails.Single(
        "EmptyPhone",
        422,
        "Un telefono esta vacio",
        s"El telefono proveido #${idx + 1} esta vacio"
      )
    case HouseError.EmptyAddress =>
      ProblemDetails.Single(
        "EmptyAddress",
        422,
        s"La direccion de la casa esta vacia",
        "La direccion de la casa esta vacia"
      )
    case HouseError.AddressTooLoong(addr) =>
      ProblemDetails.Single(
        "AddressTooLong",
        422,
        "La direccion de la casa es muy larga",
        s"""La direccion "$addr" supera la cantidad de caracteres permitidos"""
      )
    case HouseError.MaxSharesIsZero =>
      ProblemDetails.Single(
        "MaxSharesIsZero",
        422,
        s"La cantidad maxima de cupos no puede ser 0",
        s"La cantidad maxima de cupos no puede ser 0"
      )
    case HouseError.MaximumAgeIsZero =>
      ProblemDetails.Single(
        "MaxSharesIsZero",
        422,
        s"La edad maxima para el beneficio no puede ser 0",
        s"La edad maxima para el beneficio no puede ser 0"
      )
    case HouseError.MinimumAgeIsGreaterThanMaximumAge(min, max) =>
      ProblemDetails.Single(
        "MinimumAgeIsGreaterThanMaximumAge",
        422,
        s"La edad minima es superior a la maxima",
        s"La edad minima $min supera la edad maxima $max"
      )
  }
  private val contactHandler: ContactError => ProblemDetails = {
    case ContactError.InvalidCI =>
      ProblemDetails.Single(
        "InvalidContactCI",
        422,
        "La cedula es invalida",
        "La cedula excede los 8 digitos"
      )
    case ContactError.EmptyName =>
      ProblemDetails.Single(
        "EmptyContactName",
        422,
        "El nombre esta vacio",
        "El nombre no puede estar vacio"
      )
    case ContactError.NameTooLong =>
      ProblemDetails.Single(
        "ContactNameTooLong",
        422,
        "El nombre es muy largo",
        "El nombre supera los 50 caracteres"
      )
    case ContactError.EmptyLastname =>
      ProblemDetails.Single(
        "EmptyContactLastname",
        422,
        "El apellido esta vacio",
        "El apellido no puede estar vacio"
      )
    case ContactError.LastnameTooLong =>
      ProblemDetails.Single(
        "ContactLastnameTooLong ",
        422,
        "El apellido es muy largo",
        "El apellido supera los 75 caracteres"
      )
    case ContactError.EmptyPhone =>
      ProblemDetails.Single(
        "EmptyContactPhone",
        422,
        "El telefono esta vacio",
        "El telefono no puede estar vacio"
      )
    case ContactError.InvalidPhone =>
      ProblemDetails.Single(
        "InvalidContactPhone",
        422,
        "El telefono es invalido",
        "El telefono no cuenta con el formato adecuado (xxx-xxx-xxxx)"
      )
  }
  private val handler: HouseApplicationError => ProblemDetails = { 
    case HouseApplicationError.HouseDomainError(err) =>
      houseHandler(err)
    case HouseApplicationError.HouseNotFoundError(id) =>
      ProblemDetails.Single(
        "HouseNotFound",
        404,
        "Casa no encontrada",
        s"No se encontro una casa bajo el id $id"
      )
    case HouseApplicationError.ContactDomainError(cde) => contactHandler(cde)
    case HouseApplicationError.ContactNotFoundError(ci) =>
      ProblemDetails.Single(
        "ContactNotFound",
        404,
        "Contacto no encontrado",
        s"No se encontro un contacto bajo el CI: $ci"
      )
    case HouseApplicationError.ScheduleNotFoundError(id) =>
      ProblemDetails.Single(
        "ScheduleNotFound",
        404,
        "Horario no encontrado",
        s"""No se encontro un horario bajo el ID: "$id""""
      )
    case HouseApplicationError.ExistingRif(rif) =>
      ProblemDetails.Single(
        "AlreadyRegisteredRif",
        409,
        "El rif ingresado ya existe",
        s"""Ya existe una casa bajo el rif: $rif"""
      )
  }
  def detail(e: HouseApplicationError) = handler(e)

given Detailer[ChildApplicationError] with
  private val dressHandler: DressError => ProblemDetails = {
    case DressError.InvalidSize =>
      ProblemDetails.Single(
        "InvalidSize",
        422,
        "Medida introducida invalida",
        "La medida no puede ser menor que 0"
      )
  }
  private val piHandler: PersonalInformationError => ProblemDetails = {
    case PersonalInformationError.EmptyName =>
      ProblemDetails.Single(
        "PersonalInformationNameEmpty",
        422,
        "Nombre de la informacion personal vacia",
        "El nombre en la informacion personal no puede estar vacia"
      )
    case PersonalInformationError.NameTooLong =>
      ProblemDetails.Single(
        "PersonalInformationNameTooLong",
        422,
        "Nombre de la informacion personal muy largo",
        "El nombre en la informacion personal no puede superar los 50 caracteres"
      )
    case PersonalInformationError.EmptyLastname =>
      ProblemDetails.Single(
        "PersonalInformationLastnameTooLong",
        422,
        "Apellido de la informacion personal vacio",
        "El apellido en la informacion personal no puede estar vacio"
      )
    case PersonalInformationError.LastnameTooLong =>
      ProblemDetails.Single(
        "PersonalInformationLastnameTooLong",
        422,
        "Apellido de la informacion personal muy largo",
        "El apellido en la informacion personal no puede superar los 75 caracteres"
      )
    case PersonalInformationError.InvalidBirthdate =>
      ProblemDetails.Single(
        "InvalidPersonalInformationBirthdate",
        422,
        "Fecha de nacimiento de la informacion personal invalida",
        "La fecha de nacimiento no puede ser posterior a la fecha actual"
      )
    case PersonalInformationError.InvalidCI =>
      ProblemDetails.Single(
        "PersonalInformationCIInvalid",
        422,
        "Cedula de Identidad de la informacion personal vacia",
        "La Cedula de Identidad en la informacion personal no puede superar los 8 digitos o ser inferior a 1"
      )
  }
  private val handler: ChildApplicationError => ProblemDetails = {
    case ChildApplicationError.DomainDressError(dde) => dressHandler(dde)
    case ChildApplicationError.DomainPersonalInformationError(dpie) =>
      piHandler(dpie)
    case ChildApplicationError.ChildNotFound(id) =>
      ProblemDetails.Single(
        "ChildNotFound",
        404,
        "Niño no encontrado",
        s"""No se encontró ningun niño bajo el ID: "$id""""
      )
    case ChildApplicationError.PersonalInformationNotFound(ci) => 
      ProblemDetails.Single(
        "PersonNotFound",
        404,
        "Persona no encontrada",
        s"""No se encontró ninguna persona bajo el CI: "$ci""""
      )
  }
  def detail(err: ChildApplicationError) = handler(err)
