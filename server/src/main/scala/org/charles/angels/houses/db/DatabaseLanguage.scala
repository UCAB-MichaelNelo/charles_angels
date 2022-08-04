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

  def getAllChildrenOfHouse(houseId: UUID) = for
    ids <- getChildrenOfHouse(houseId)
    children <- ids.traverse(getChild)
  yield children.flatMap(_.toList).toVector

  def getChildrenWithoutHousing = for
    children <- getAllChildren
    childrenWithoutHousing <- children.flatTraverse { inf =>
      getHouseHousingChild(inf.getID).map(_.tupleRight(inf).as(Vector.empty).getOrElse(Vector(inf))) 
    }
  yield childrenWithoutHousing 

  def registerChild(model: ChildModel, house: House) = for
    id <- storeChild(model.information, model.wear, model.id)
    () <- bindChildToHouse(house.id, model.id)
  yield id

  def removeChild(id: UUID) = for
    childResult <- getChild(id)
    houseIdResult <- getHouseHousingChild(id)
    () <- childResult product houseIdResult match {
      case None => ().pure[CompilerLanguage[F, _]]
      case Some((child, houseId)) => unbindChildFromHouse(id)
    }
    () <- deleteChild(id)
  yield ()

  def removeHouse(id: UUID) = for
    ids <- getChildrenOfHouse(id)
    () <- ids
      .traverse(unbindChildFromHouse)
      .void
    house <- deleteHouse(id)
  yield house
}
