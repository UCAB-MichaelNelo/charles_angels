package org.charles.angels.houses.db

import cats.syntax.all.*
import cats.InjectK
import org.charles.angels.houses.compiler.CompilerLanguage
import org.charles.angels.houses.db.houses.DatabaseLanguage as HousesDatabaseLanguage
import org.charles.angels.houses.db.people.DatabaseLanguage as PeopleDatabaseLanguage
import org.charles.angels.houses.db.relationships.DatabaseLanguage as RelationshipsDatabaseLanguage
import org.charles.angels.houses.db.houses.DatabaseAction as HousesDatabaseAction
import org.charles.angels.houses.db.people.DatabaseAction as PeopleDatabaseAction
import org.charles.angels.houses.db.relationships.DatabaseAction as RelationshipsDatabaseAction
import org.charles.angels.people.domain.ChildInformation
import org.charles.angels.people.domain.Wear
import java.util.UUID
import cats.free.Yoneda
import org.charles.angels.people.domain.Child
import java.time.temporal.ChronoUnit
import org.charles.angels.houses.domain.House

final case class ChildModel(
    id: UUID,
    houseId: UUID,
    information: ChildInformation,
    wear: Wear
) {
  def dateSixMonthsBefore(years: Int) =
    ChronoUnit.YEARS
      .addTo(information.information.birthdate, years)
      .minusMonths(6)
}

trait DatabaseLanguage[F[_]](using
    InjectK[HousesDatabaseAction, F],
    InjectK[PeopleDatabaseAction, F],
    InjectK[RelationshipsDatabaseAction, F]
) extends HousesDatabaseLanguage[F]
    with PeopleDatabaseLanguage[F]
    with RelationshipsDatabaseLanguage[F] {

  def registerChild(model: ChildModel, house: House) = for
    id <- storeChild(model.information, model.wear, model.id)
    () <- bindChildToHouse(house.id, model.id)
    _ <- model.information.relatedBeneficiaries.keys.toList
      .traverse(bindPersonToHouse(house.id, _))
  yield id

  def removeChild(id: UUID) = for
    childResult <- getChild(id)
    houseIdResult <- getHouseHousingChild(id)
    () <- childResult product houseIdResult match {
      case None => ().pure[CompilerLanguage[F, _]]
      case Some((child, houseId)) =>
        child.getInformation.relatedBeneficiaries.keys.toList
          .traverse(unbindPersonFromHouse(_)) >>
          unbindChildFromHouse(id)
    }
    () <- deleteChild(id)
  yield ()

  def removeHouse(id: UUID) = for
    ids <- getChildrenOfHouse(id)
    () <- ids
      .traverse(unbindChildFromHouse)
      .void
    children <- ids.traverse(getChild(_).map(_.toList)).map(_.flatten)
    personIds = children.flatMap(
      _.getInformation.relatedBeneficiaries.keys.toList
    )
    () <- personIds.traverse(unbindPersonFromHouse(_)).void
    house <- deleteHouse(id)
  yield house
}
