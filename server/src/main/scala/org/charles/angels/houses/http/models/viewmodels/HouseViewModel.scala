package org.charles.angels.houses.http.models.viewmodels

import java.util.UUID
import org.charles.angels.houses.domain.House
import java.time.LocalTime

case class HouseViewModel(
    id: UUID,
    name: String,
    rif: Int,
    phones: Vector[String],
    address: String,
    contactCI: Int,
    maxShares: Int,
    currentShares: Int,
    minimumAge: Int,
    maximumAge: Int,
    currentGirlsHelped: Int,
    currentBoysHelped: Int,
    scheduleStartTime: LocalTime,
    scheduleEndTime: LocalTime
)
object HouseViewModel {
  def apply(house: House) = new HouseViewModel(
    house.id,
    house.name,
    house.rif,
    house.phones,
    house.address,
    house.contactCI,
    house.maxShares,
    house.currentShares,
    house.minimumAge,
    house.maximumAge,
    house.currentGirlsHelped,
    house.currentBoysHelped,
    house.scheduleStartTime,
    house.scheduleEndTime
  )
}
