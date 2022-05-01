package org.charles.angels.houses.application.models

final case class HouseModel(
    filename: String,
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
    contact: ContactModel,
    schedule: ScheduleModel
)
