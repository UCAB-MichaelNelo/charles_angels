package org.charles.angels.houses.compiler

import cats.syntax.all.*
import org.charles.angels.houses.logging.LoggingLanguage
import org.charles.angels.houses.db.DatabaseAction
import org.charles.angels.houses.db.DatabaseLanguage
import org.charles.angels.houses.compiler.ServerAction
import org.charles.angels.houses.filesystem.FilesystemLanguage
import java.util.UUID
import java.io.File
import java.time.LocalTime
import scala.concurrent.duration.FiniteDuration
import org.charles.angels.houses.domain.ScheduleBlock
import cats.data.Chain
import org.charles.angels.people.domain.ChildInformation
import org.charles.angels.people.domain.Wear
import org.charles.angels.people.domain.PersonalInformation
import org.charles.angels.houses.db.ChildModel
import org.charles.angels.houses.cron.CronLanguage
import org.charles.angels.houses.notifications.NotificationLanguage
import org.charles.angels.houses.notifications.NotificationAction
import org.charles.angels.houses.notifications.Notification
import cats.data.OptionT
import org.charles.angels.houses.reports.ReportLanguage

object CompilerDSL
    extends FilesystemLanguage[ServerAction]
    with LoggingLanguage[ServerAction]
    with DatabaseLanguage[ServerAction]
    with CronLanguage[ServerAction]
    with NotificationLanguage[ServerAction]
    with ReportLanguage[ServerAction]
{
  // Services related DSL
  def allocateFile(contents: Array[Byte], name: String) = info(
    f"Construyendo archivo temporal de imagen de CASA $name"
  ) >> createFile(contents, name)
  def deallocateFile(file: File) = warn(
    f"Destruyendo archivo temporal de nombre ${file.getAbsolutePath}"
  ) >> deleteFile(file)
  // House related DSL
  def findHouse(id: UUID) = for
    findResult <- info(f"Buscando entidad de CASA por ID: $id") >> getHouse(id)
    () <- findResult match {
      case Some(house) => info(f"Encontrado CASA con nombre: ${house.name}")
      case None        => warn(f"No se encontro CASA con ID: $id")
    }
  yield findResult
  def registerHouse(
      id: UUID,
      img: File,
      name: String,
      rif: Int,
      phones: Vector[String],
      address: String,
      maxShares: Int,
      currentShares: Int,
      minimumAge: Int,
      maximumAge: Int,
      currentGirlsHelped: Int,
      currentBoysHelped: Int,
      contactCI: Int,
      scheduleId: UUID
  ) =
    info(
      f"""Registrando nueva CASA bajo:
        ID: $id, Tamaño de la imagen: ${img.length}, Nombre: $name, RIF: $rif,
        Teléfonos: ${phones.show}, Dirección: $address, Cupos máximos: $maxShares,
        Cupos ocupados: $currentShares, Edad mínima de Beneficio: $minimumAge, Edad máxima de Beneficio: $maximumAge,
        Cantidad actual de niños ayudados: $currentBoysHelped, Cantidad actual de chicas ayudadas $currentGirlsHelped,
        Cedula de Identidad del contacto de la CASA: $contactCI, ID del Horario: $scheduleId"""
    ) >>
    storeHouse(
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
      scheduleId
    ) >>
    createNewBeneficiaryCountEntry(id) >>
    createNewFoodAmountEntry(id) >>
    createNewNeededWearEntry(id)

  def updateImageOfHouse(id: UUID, img: File) =
    info(
      f"Actualizando Imagen de la CASA con ID: $id, Longitud de la imagen: ${img.length}"
    ) >> updateImage(id, img)
  def updateNameOfHouse(id: UUID, name: String) = info(
    f"Actualizando nombre de la CASA con ID: $id, Nombre: $name"
  ) >> updateName(id, name)
  def updateRIFOfHouse(id: UUID, rif: Int) = info(
    f"Actualizando RIF de la CASA con ID: $id, RIF: $rif"
  ) >> updateRIF(id, rif)
  def addPhoneToHouse(id: UUID, phone: String) = info(
    f"Agregando Telefono a la CASA con ID: $id, Telefono: $phone"
  ) >> addPhone(id, phone)
  def removePhoneOfHouse(id: UUID, key: Int) = info(
    f"Removiendo Telefono de la CASA con ID: $id, Indice: $key"
  ) >> removePhone(id, key)
  def updatePhoneOfHouse(id: UUID, key: Int, phone: String) = info(
    f"Actualizando Telefono de la CASA con ID: $id, Indice: $key, Telefono: $phone"
  ) >> updatePhone(id, key, phone)
  def updateMaxSharesOfHouse(id: UUID, maxShares: Int) = info(
    f"Actualizando Cupos Maximos de la con ID: $id, Cupos Maximos: $maxShares"
  ) >> updateMaxShares(id, maxShares)
  def updateCurrentSharesOfHouse(id: UUID, currentShares: Int) = info(
    f"Actualizando Cupos Actuales de la CASA con ID: $id, Cupos Actuales: $currentShares"
  ) >> updateCurrentShares(id, currentShares)
  def updateMinimumAgeOfHouse(id: UUID, minimumAge: Int) = info(
    f"Actualizando Edad minima para beneficio de la CASA con ID: $id, Edad Minima: $minimumAge"
  ) >> updateMinimumAge(id, minimumAge)
  def updateMaximumAgeOfHouse(id: UUID, maximumAge: Int) = info(
    f"Actualizando Edad maxima para beneficio de la CASA con ID: $id, Edad Maxima: $maximumAge"
  ) >> updateMaximumAge(id, maximumAge)
  def updateCurrentGirlsHelpedOfHouse(id: UUID, currentGirlsHelped: Int) =
    info(
      f"Actualizando Cantidad Actual de Chicas Ayudadas de CASA con ID: $id, Cantidad Actual de Chicas Ayudadas: $currentGirlsHelped"
    ) >> updateCurrentGirlsHelperd(id, currentGirlsHelped)
  def updateCurrentBoysHelpedOfHouse(id: UUID, currentBoysHelped: Int) =
    info(
      f"Actualizando Cantidad Actual de Chicos Ayudados de CASA con ID: $id, Cantidad Actual de Chicos Ayudados: $currentBoysHelped"
    ) >> updateCurrentBoysHelped(id, currentBoysHelped)
  def eliminateHouse(id: UUID) =
    info(f"Eliminando CASA con ID: $id") >> removeHouse(id)
  // Contact related DSL
  def findContact(ci: Int) = for
    findResult <- info(f"Buscando CONTACTO con Cedula: $ci") >> getContact(ci)
    _ <- findResult match
      case Some(contact) =>
        info(f"Encontrado CONTACTO con Nombre ${contact.name}")
      case None => warn(f"No se encontro Contacto con CI: $ci")
  yield findResult
  def registerContact(
      ci: Int,
      name: String,
      lastname: String,
      phone: Option[String]
  ) = info(
    f"""Registrando CONTACTO con: CI: $ci,
        Nombre: $name, Apellido: $lastname, Telephono: ${phone.show}"""
  ) >> storeContact(ci, name, lastname, phone)
  def changeCIOfContact(ci: Int, newCI: Int) = info(
    f"Actualizando CI del CONTACTO con CI: $ci, CI: $newCI"
  ) >> changeCI(ci, newCI)
  def changeNameOfContact(ci: Int, name: String) = info(
    f"Actualizando Nombre del CONTACTO con CI: $ci, Nombre: $name"
  ) >> changeName(ci, name)
  def changeLastnameOfContact(ci: Int, lastname: String) = info(
    f"Actualizando Apellido del CONTACTO con CI: $ci, Apellido: $lastname"
  ) >> changeLastname(ci, lastname)
  def changePhoneOfContact(ci: Int, phone: String) = info(
    f"Actualizando Telefono del CONTACTO con CI: $ci, Telefono: $phone"
  ) >> changePhone(ci, phone)
  def eliminateContact(ci: Int) =
    info(f"Eliminando CONTACTO con CI: $ci") >> deleteContact(ci)
  // Schedule related DSL
  def findSchedule(id: UUID) = for
    result <- info(f"Buscando HORARIO con ID: $id") >> getSchedule(id)
    _ <- result match
      case Some(s) => info(f"Encontrado HORARIO con ID: $id, $s")
      case None    => warn(f"No se pudo encontrar HORARIO con ID $id")
  yield result
  def registerSchedule(
      id: UUID,
      monday: Chain[ScheduleBlock],
      tuesday: Chain[ScheduleBlock],
      wednesday: Chain[ScheduleBlock],
      thursday: Chain[ScheduleBlock],
      friday: Chain[ScheduleBlock]
  ) = info(f"Registrando HORARIO con ID: $id") >> storeSchedule(
    id,
    monday,
    tuesday,
    wednesday,
    thursday,
    friday
  )
  def addBlockToSchedule(
      id: UUID,
      day: Int,
      key: Long,
      startTime: LocalTime,
      duration: FiniteDuration
  ) = info(
    f"Agregando Bloque de Horario a HORARIO con ID: $id, en el $day dia, a la hora: $startTime, duracion: $duration"
  ) >> addBlock(id, day, key, startTime, duration)
  def removeBlockOfSchedule(
      id: UUID,
      day: Int,
      key: Int
  ) = info(
    f"Removiendo Bloque de Horario de HORARIO con ID: $id, en el $day dia, en el indice: $key"
  ) >> removeBlock(id, day, key)
  def updateStartHourOnBlockOfSchedule(
      id: UUID,
      day: Int,
      key: Int,
      newStartHour: Int
  ) = info(
    f"Actualizando Hora de Inicio de Bloque de Horario de HORARIO con ID: $id, en el $day dia, con indice $key. Hora de Inicio: $newStartHour"
  ) >> updateStartHourOnBlock(id, day, key, newStartHour)
  def updateStartMinuteOnBlockOfSchedule(
      id: UUID,
      day: Int,
      key: Int,
      newStartMinute: Int
  ) = info(
    f"Actualizando Minuto de Inicio de Bloque de Horario de HORARIO con ID: $id, en el $day dia, con indice $key. Minuto de Inicio: $newStartMinute"
  ) >> updateStartMinuteOnBlock(id, day, key, newStartMinute)
  def updateDurationHoursOnBlockOfSchedule(
      id: UUID,
      day: Int,
      key: Int,
      newDurationHours: Int
  ) = info(
    f"Actualizando Horas de Duracion de Bloque de Horario de HORARIO con ID: $id, en el $day dia, con indice $key. Horas de Duracion: $newDurationHours"
  ) >> updateDurationHoursOnBlock(id, day, key, newDurationHours)
  def updateDurationMinutesOnBlockOfSchedule(
      id: UUID,
      day: Int,
      key: Int,
      newDurationMinutes: Int
  ) = info(
    f"Actualizando Minutos de Duracion de Bloque de Horario de HORARIO con ID: $id, en el $day dia, con indice $key. Minutos de Duracion: $newDurationMinutes"
  ) >> updateDurationMinutesOnBlock(id, day, key, newDurationMinutes)
  def eliminateSchedule(id: UUID) =
    info(f"Eliminando HORARIO con ID: $id") >> deleteSchedule(id)

  // People related DSL
  private def sexChar(wear: Wear) = wear match {
    case Wear.BoyWear(_)  => "O"
    case Wear.GirlWear(_) => "A"
  }
  private def string(info: PersonalInformation) =
    f"""CI: ${info.ci}, NOMBRE: ${info.name},
        LASTNAME: ${info.lastname},
        EDAD: ${info.age}
    """
  def saveChild(model: ChildModel) = {
    val char = sexChar(model.wear)
    val ci = model.information
    def unwrapInformation(
        option: Option[PersonalInformation],
        ifMissing: String
    ) = option.map(string) getOrElse ifMissing

    for {
      () <- info(f"""Registrando NIÑ$char bajo: ${string(ci.information)}
                    REPRESENTANTE: ${unwrapInformation(ci.nonParent, "FALTANATE")},
                    MADRE: ${unwrapInformation(ci.mother, "FALLECIDO")},
                    PADRE: ${unwrapInformation(ci.father, "FALLECIDO")},
                    NOMBRE DE ARCHIVO DE FOTO: ${ci.photo.getAbsolutePath}
                    CANTIDAD DE BENEFICIARIOS RELACIONADOS: ${ci.relatedBeneficiaries.size}""")
      houseOption <- findHouse(model.houseId)

      _ <- houseOption match {
        case Some(house) =>
          registerChild(model, house) >>
            incrementBeneficiaryCount(house.id) >>
            incrementFoodAmountNeededInHouse(house.id, model.id, Vector.empty) >>
            addWearNeededInBeneficiaryHouse(house.id, model.wear) >>
            info(f"Agedando notificación de edad máxima") >>
            schedule(model.dateSixMonthsBefore(house.maximumAge)) {
              info(f"Notificando que el chico ${model.information.information.name} está a 6 meses de cumplir la edad máxima")
                >> notify(Notification.SixMonthsBeforeMaxAge(model.id, house))
            }
        case None => ().pure[ServerLanguage]
      }

    } yield ()
  }
  def eliminateChild(id: UUID) = {
    OptionT.some[ServerLanguage] { info(f""""Eliminando NIÑO con ID: $id y desvinculando sus beneficiarions relacionados de la CASA a la que pertenece""") }
      >> {
        for {
          child <- OptionT { getChild(id) }
          houseId <- OptionT { getHouseHousingChild(id) }
          _ <- OptionT.some { decrementBeneficiaryCount(houseId) }
          _ <- OptionT.some { decrementFoodAmountNeededInHouse(houseId, id, Vector.empty) }
          _ <- OptionT.some { removeWearNeededInBeneficiaryHouse(houseId, child.wear) }
        } yield ()
      }
      >> OptionT.some { removeChild(id) }
  }.value.void
  def findChild(id: UUID) =
    info(
      f""""Buscando NIÑO con ID: $id"""
    ) >> getChild(id)
  def updatePersonalInformation(
      ci: Int,
      info: PersonalInformation,
      isOfChild: Boolean
  ) = for
    () <- this.info(f""""actualizando information personal bajo ci $ci
           con informacion: ${string(info)}""")
    () <- updateInformation(ci, info)
    () <-
      if (isOfChild) (for
        child <- OptionT { getChildByCI(ci) }
        houseId <- OptionT { getHouseHousingChild(child.getID) }
        house <- OptionT { getHouse(houseId) }
        _ <- OptionT.some {
          schedule(child.dateSixMonthsBefore(house.maximumAge)) {
            notify(Notification.SixMonthsBeforeMaxAge(child.getID, house))
          }
        }
      yield ()).value.void
      else
        ().pure[ServerLanguage]
  yield ()
  def savePersonalInformation(info: PersonalInformation) =
    this.info(
      f""""Registrando Information Personal con: ${string(info)}"""
    ) >> saveInformation(info)
  def deletePersonalInformation(ci: Int) =
    info(
      f""""Eliminando Information Personal bajo CI: $ci"""
    ) >> deleteInformation(ci)
  def updateChildAttire(id: UUID, wear: Wear) =
    val char = sexChar(wear)
    info(f""""Actualizando vestimenta de NIÑ$char con ID $id""") >>
      updateAttire(id, wear) >> {
        for {
          houseId <- OptionT[ServerLanguage, UUID] { getHouseHousingChild(id) }
          _ <- OptionT.some { removeWearNeededInBeneficiaryHouse(houseId, wear) }
          _ <- OptionT.some { addWearNeededInBeneficiaryHouse(houseId, wear) }
        } yield ()
      }
      .value
      .void
  def updateChildPhoto(id: UUID, filename: String) =
    info(f""""Actualizando foto de NIÑO con ID $id""") >>
      updatePhoto(id, filename)

}
