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

object CompilerDSL
    extends FilesystemLanguage[ServerAction]
    with LoggingLanguage[ServerAction]
    with DatabaseLanguage[ServerAction]
    with CronLanguage[ServerAction]
    with NotificationLanguage[ServerAction] {
  // Services related DSL
  def allocateFile(contents: Array[Byte], name: String) = info(
    f"Construyendo archivo temporal de imagen de CASA $name"
  ) >> createFile(contents, name)
  def deallocateFile(file: File) = warn(
    f"Destruyendo archivo temporal de nombre ${file.getAbsolutePath}"
  ) >> deleteFile(file)
  // House related DSL
  def findHouse(id: UUID) = for
    findResult <- debug(f"Buscando entidad de CASA por ID: $id") >> getHouse(id)
    () <- findResult match {
      case Some(house) => debug(f"Encontrado CASA con nombre: ${house.name}")
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
  ) = debug(
    f"""Registrando nueva CASA bajo:
        ID: $id, Tamaño de la imagen: ${img.length}, Nombre: $name, RIF: $rif,
        Teléfonos: ${phones.show}, Dirección: $address, Cupos máximos: $maxShares,
        Cupos ocupados: $currentShares, Edad mínima de Beneficio: $minimumAge, Edad máxima de Beneficio: $maximumAge,
        Cantidad actual de niños ayudados: $currentBoysHelped, Cantidad actual de chicas ayudadas $currentGirlsHelped,
        Cedula de Identidad del contacto de la CASA: $contactCI, ID del Horario: $scheduleId"""
  ) >> storeHouse(
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
  )
  def updateImageOfHouse(id: UUID, img: File) = debug(
    f"Actualizando Imagen de la CASA con ID: $id, Longitud de la imagen: ${img.length}"
  ) >> updateImage(id, img)
  def updateNameOfHouse(id: UUID, name: String) = debug(
    f"Actualizando nombre de la CASA con ID: $id, Nombre: $name"
  ) >> updateName(id, name)
  def updateRIFOfHouse(id: UUID, rif: Int) = debug(
    f"Actualizando RIF de la CASA con ID: $id, RIF: $rif"
  ) >> updateRIF(id, rif)
  def addPhoneToHouse(id: UUID, phone: String) = debug(
    f"Agregando Telefono a la CASA con ID: $id, Telefono: $phone"
  ) >> addPhone(id, phone)
  def removePhoneOfHouse(id: UUID, key: Int) = debug(
    f"Removiendo Telefono de la CASA con ID: $id, Indice: $key"
  ) >> removePhone(id, key)
  def updatePhoneOfHouse(id: UUID, key: Int, phone: String) = debug(
    f"Actualizando Telefono de la CASA con ID: $id, Indice: $key, Telefono: $phone"
  ) >> updatePhone(id, key, phone)
  def updateMaxSharesOfHouse(id: UUID, maxShares: Int) = debug(
    f"Actualizando Cupos Maximos de la con ID: $id, Cupos Maximos: $maxShares"
  ) >> updateMaxShares(id, maxShares)
  def updateCurrentSharesOfHouse(id: UUID, currentShares: Int) = debug(
    f"Actualizando Cupos Actuales de la CASA con ID: $id, Cupos Actuales: $currentShares"
  ) >> updateCurrentShares(id, currentShares)
  def updateMinimumAgeOfHouse(id: UUID, minimumAge: Int) = debug(
    f"Actualizando Edad minima para beneficio de la CASA con ID: $id, Edad Minima: $minimumAge"
  ) >> updateMinimumAge(id, minimumAge)
  def updateMaximumAgeOfHouse(id: UUID, maximumAge: Int) = debug(
    f"Actualizando Edad maxima para beneficio de la CASA con ID: $id, Edad Maxima: $maximumAge"
  ) >> updateMaximumAge(id, maximumAge)
  def updateCurrentGirlsHelpedOfHouse(id: UUID, currentGirlsHelped: Int) =
    debug(
      f"Actualizando Cantidad Actual de Chicas Ayudadas de CASA con ID: $id, Cantidad Actual de Chicas Ayudadas: $currentGirlsHelped"
    ) >> updateCurrentGirlsHelperd(id, currentGirlsHelped)
  def updateCurrentBoysHelpedOfHouse(id: UUID, currentBoysHelped: Int) =
    debug(
      f"Actualizando Cantidad Actual de Chicos Ayudados de CASA con ID: $id, Cantidad Actual de Chicos Ayudados: $currentBoysHelped"
    ) >> updateCurrentBoysHelped(id, currentBoysHelped)
  def eliminateHouse(id: UUID) =
    debug(f"Eliminando CASA con ID: $id") >> removeHouse(id)
  // Contact related DSL
  def findContact(ci: Int) = for
    findResult <- debug(f"Buscando CONTACTO con Cedula: $ci") >> getContact(ci)
    _ <- findResult match
      case Some(contact) =>
        debug(f"Encontrado CONTACTO con Nombre ${contact.name}")
      case None => warn(f"No se encontro Contacto con CI: $ci")
  yield findResult
  def registerContact(
      ci: Int,
      name: String,
      lastname: String,
      phone: Option[String]
  ) = debug(
    f"""Registrando CONTACTO con: CI: $ci,
        Nombre: $name, Apellido: $lastname, Telephono: ${phone.show}"""
  ) >> storeContact(ci, name, lastname, phone)
  def changeCIOfContact(ci: Int, newCI: Int) = debug(
    f"Actualizando CI del CONTACTO con CI: $ci, CI: $newCI"
  ) >> changeCI(ci, newCI)
  def changeNameOfContact(ci: Int, name: String) = debug(
    f"Actualizando Nombre del CONTACTO con CI: $ci, Nombre: $name"
  ) >> changeName(ci, name)
  def changeLastnameOfContact(ci: Int, lastname: String) = debug(
    f"Actualizando Apellido del CONTACTO con CI: $ci, Apellido: $lastname"
  ) >> changeLastname(ci, lastname)
  def changePhoneOfContact(ci: Int, phone: String) = debug(
    f"Actualizando Telefono del CONTACTO con CI: $ci, Telefono: $phone"
  ) >> changePhone(ci, phone)
  def eliminateContact(ci: Int) =
    debug(f"Eliminando CONTACTO con CI: $ci") >> deleteContact(ci)
  // Schedule related DSL
  def findSchedule(id: UUID) = for
    result <- debug(f"Buscando HORARIO con ID: $id") >> getSchedule(id)
    _ <- result match
      case Some(s) => debug(f"Encontrado HORARIO con ID: $id, $s")
      case None    => warn(f"No se pudo encontrar HORARIO con ID $id")
  yield result
  def registerSchedule(
      id: UUID,
      monday: Chain[ScheduleBlock],
      tuesday: Chain[ScheduleBlock],
      wednesday: Chain[ScheduleBlock],
      thursday: Chain[ScheduleBlock],
      friday: Chain[ScheduleBlock]
  ) = debug(f"Registrando HORARIO con ID: $id") >> storeSchedule(
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
  ) = debug(
    f"Agregando Bloque de Horario a HORARIO con ID: $id, en el $day dia, a la hora: $startTime, duracion: $duration"
  ) >> addBlock(id, day, key, startTime, duration)
  def removeBlockOfSchedule(
      id: UUID,
      day: Int,
      key: Int
  ) = debug(
    f"Removiendo Bloque de Horario de HORARIO con ID: $id, en el $day dia, en el indice: $key"
  ) >> removeBlock(id, day, key)
  def updateStartHourOnBlockOfSchedule(
      id: UUID,
      day: Int,
      key: Int,
      newStartHour: Int
  ) = debug(
    f"Actualizando Hora de Inicio de Bloque de Horario de HORARIO con ID: $id, en el $day dia, con indice $key. Hora de Inicio: $newStartHour"
  ) >> updateStartHourOnBlock(id, day, key, newStartHour)
  def updateStartMinuteOnBlockOfSchedule(
      id: UUID,
      day: Int,
      key: Int,
      newStartMinute: Int
  ) = debug(
    f"Actualizando Minuto de Inicio de Bloque de Horario de HORARIO con ID: $id, en el $day dia, con indice $key. Minuto de Inicio: $newStartMinute"
  ) >> updateStartMinuteOnBlock(id, day, key, newStartMinute)
  def updateDurationHoursOnBlockOfSchedule(
      id: UUID,
      day: Int,
      key: Int,
      newDurationHours: Int
  ) = debug(
    f"Actualizando Horas de Duracion de Bloque de Horario de HORARIO con ID: $id, en el $day dia, con indice $key. Horas de Duracion: $newDurationHours"
  ) >> updateDurationHoursOnBlock(id, day, key, newDurationHours)
  def updateDurationMinutesOnBlockOfSchedule(
      id: UUID,
      day: Int,
      key: Int,
      newDurationMinutes: Int
  ) = debug(
    f"Actualizando Minutos de Duracion de Bloque de Horario de HORARIO con ID: $id, en el $day dia, con indice $key. Minutos de Duracion: $newDurationMinutes"
  ) >> updateDurationMinutesOnBlock(id, day, key, newDurationMinutes)
  def eliminateSchedule(id: UUID) =
    debug(f"Eliminando HORARIO con ID: $id") >> deleteSchedule(id)

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
  def saveChild(model: ChildModel) =
    val char = sexChar(model.wear)
    val info = model.information
    def unwrapInformation(
        option: Option[PersonalInformation],
        ifMissing: String
    ) = option.map(string) getOrElse ifMissing

    for
      () <- debug(f"""Registrando NIÑ$char bajo: ${string(info.information)}
              REPRESENTANTE: ${unwrapInformation(info.nonParent, "FALTANATE")},
              MADRE: ${unwrapInformation(info.mother, "FALLECIDO")},
              PADRE: ${unwrapInformation(info.father, "FALLECIDO")},
              NOMBRE DE ARCHIVO DE FOTO: ${info.photo.getAbsolutePath}
              CANTIDAD DE BENEFICIARIOS RELACIONADOS: ${info.relatedBeneficiaries.size}
     """)
      houseOption <- findHouse(model.houseId)
      _ <- houseOption match {
        case Some(house) =>
          registerChild(model, house) >>
            this.info(f"Scheduling max age notification") >>
            schedule(model.dateSixMonthsBefore(house.maximumAge)) {
              this.info(
                f"Notifying that child ${model.information.information.name} is 6 months to reach maximum age"
              ) >>
                notify(Notification.SixMonthsBeforeMaxAge(model.id, house))
            }
        case None => ().pure[ServerLanguage]
      }
    yield ()
  def eliminateChild(id: UUID) =
    debug(
      f""""Eliminando NIÑO con ID: $id y desvinculando sus beneficiarions relacionados de la CASA a la que pertenece"""
    ) >> removeChild(id)
  def findChild(id: UUID) =
    debug(
      f""""Buscando NIÑO con ID: $id"""
    ) >> getChild(id)
  def updatePersonalInformation(
      ci: Int,
      info: PersonalInformation,
      isOfChild: Boolean
  ) = for
    () <- debug(f""""actualizando information personal bajo ci $ci
           con informacion: ${string(info)}""")
    () <- updateInformation(ci, info)
    () <-
      if (isOfChild) (for
        child <- OptionT(getChildByCI(ci))
        houseId <- OptionT(getHouseHousingChild(child.getID))
        house <- OptionT(getHouse(houseId))
        _ <- OptionT.liftF(
          schedule(
            child.dateSixMonthsBefore(house.maximumAge)
          ) {
            notify(Notification.SixMonthsBeforeMaxAge(child.getID, house))
          }
        )
      yield ()).value.void
      else
        ().pure[ServerLanguage]
  yield ()
  def savePersonalInformation(info: PersonalInformation) =
    debug(
      f""""Registrando Information Personal con: ${string(info)}"""
    ) >> saveInformation(info)
  def deletePersonalInformation(ci: Int) =
    debug(
      f""""Eliminando Information Personal bajo CI: $ci"""
    ) >> deleteInformation(ci)
  def updateChildAttire(id: UUID, wear: Wear) =
    val char = sexChar(wear)
    debug(f""""Actualizando vestimenta de NIÑ$char con ID $id""") >>
      updateAttire(id, wear)
  def updateChildPhoto(id: UUID, filename: String) =
    debug(f""""Actualizando foto de NIÑO con ID $id""") >>
      updatePhoto(id, filename)

}
