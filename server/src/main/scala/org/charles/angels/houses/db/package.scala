package org.charles.angels.houses

import cats.data.EitherK
import org.charles.angels.houses.db.houses.DatabaseAction as HousesDatabaseAction
import org.charles.angels.houses.db.people.DatabaseAction as PeopleDatabaseAction
import org.charles.angels.houses.db.relationships.DatabaseAction as RelationshipsDatabaseAction

package object db {
  private type DatabaseAction0[A] =
    EitherK[HousesDatabaseAction, PeopleDatabaseAction, A]
  type DatabaseAction[A] =
    EitherK[RelationshipsDatabaseAction, DatabaseAction0, A]
}
