package org.charles.angels.houses.reports.mongodb

import collection.convert.ImplicitConversions.*
import cats.syntax.all.*
import cats.~>
import cats.effect.kernel.Async
import mongo4cats.collection.MongoCollection
import mongo4cats.circe.*
import io.circe.generic.auto.*
import io.circe.generic.semiauto
import mongo4cats.bson.ObjectId
import java.util.UUID
import org.charles.angels.houses.reports.models.BeneficiaryCount
import org.charles.angels.houses.reports.ReportAction
import org.charles.angels.houses.reports.models.WearInformation
import org.charles.angels.houses.reports.models.FoodInformation
import mongo4cats.collection.operations.Filter
import mongo4cats.collection.operations.Update
import org.charles.angels.people.domain.Wear
import com.mongodb.client.model.Updates
import com.mongodb.client.model.Filters
import org.charles.angels.houses.reports.models.HouseWithBeneficiaryCount
import mongo4cats.collection.operations.Aggregate
import mongo4cats.collection.operations.Accumulator
import org.charles.angels.houses.reports.models.HouseWithFoodInformation
import com.mongodb.client.model.MergeOptions
import org.charles.angels.houses.reports.models.HouseWithWearInformation
import org.charles.angels.houses.reports.ReportScope
import org.charles.angels.houses.reports.Make
import mongo4cats.client.MongoClient
import cats.effect.kernel.Resource
import mongo4cats.client.ServerAddress
import io.circe.Encoder
import io.circe.Codec

final case class MongoDB(
  host: String,
  port: Int
)

class MongoDbExecutor[F[_]: Async](
  beneficiaryCountCollection: MongoCollection[F, MongoHouseBeneficiaryInformation],
  wearInformationCollection: MongoCollection[F, MongoHouseWearInformation],
  foodInformationCollection: MongoCollection[F, MongoHouseFoodInformation],
) extends (ReportAction ~> F){
  def apply[A](action: ReportAction[A]) = action match {
    case ReportAction.CreateNewBeneficiaryCountEntry(houseId) => beneficiaryCountCollection.insertOne(MongoHouseBeneficiaryInformation(ObjectId(), houseId, BeneficiaryCount(0))).void.attempt
    case ReportAction.CreateNewFoodAmountEntry(houseId) => foodInformationCollection.insertOne(MongoHouseFoodInformation(ObjectId(), houseId, FoodInformation(0, Map.empty))).void.attempt
    case ReportAction.CreateNewNeededWearEntry(houseId) => wearInformationCollection.insertOne(MongoHouseWearInformation(ObjectId(), houseId, WearInformation(Map.empty, Map.empty, Map.empty, Map.empty, Map.empty))).void.attempt

    case ReportAction.IncrementBeneficiaryCount(houseId) => beneficiaryCountCollection.updateOne(Filter.eq("houseId", houseId), Update.inc("beneficiaryInformation.beneficiaryCount", 1)) >> beneficiaryCountCollection.find(Filter.eq("houseId", houseId)).first.map(_.toRight(Exception(s"No se pudo encontrar entrada de cantidad de beneficiarios para CASA con ID $houseId")).map(_.beneficiaryInformation.beneficiaryCount)).rethrow.attempt
    case ReportAction.AddWearNeededInHouse(houseId, wear) =>
      val filter = Filters.eq("houseId", houseId)
      (wear match {
        case Wear.BoyWear(boyAttire) =>
          wearInformationCollection.updateOne(
            filter,
            Updates.combine(
              Updates.inc(s"wearInformation.shortOrTrousersNeededBySize.${boyAttire.shortOrTrousersSize}", 1),
              Updates.inc(s"wearInformation.tshirtOrShirtNeededAmountBySize.${boyAttire.tshirtOrshirtSize}", 1),
              Updates.inc(s"wearInformation.footwearNeededAmountBySize.${boyAttire.footwearSize}", 1),
              Updates.inc(s"wearInformation.sweaterNeededAmountBySize.${boyAttire.sweaterSize}", 1),
            )
          )
        case Wear.GirlWear(girlAttire) =>
          wearInformationCollection.updateOne(
            filter,
            Updates.combine(
              Updates.inc(s"wearInformation.shortOrTrousersNeededBySize.${girlAttire.shortOrTrousersSize}", 1),
              Updates.inc(s"wearInformation.tshirtOrShirtNeededAmountBySize.${girlAttire.tshirtOrshirtSize}", 1),
              Updates.inc(s"wearInformation.footwearNeededAmountBySize.${girlAttire.footwearSize}", 1),
              Updates.inc(s"wearInformation.dressNeededAmountBySize.${girlAttire.dressSize}", 1),
            )
          )
      }).void.attempt
    case ReportAction.IncrementFoodAmountNeededInHouse(houseId, childId, allergies) => foodInformationCollection.updateOne(Filters.eq("houseId", houseId), Updates.combine(Updates.inc("foodInformation.foodAmountNeeded", 1), Updates.addEachToSet[String](s"foodInformation.allergies.$childId", allergies.toList))).void.attempt
    case ReportAction.DecrementBeneficiaryCount(houseId) =>  beneficiaryCountCollection.updateOne(Filter.eq("houseId", houseId), Update.inc("beneficiaryInformation.beneficiaryCount", -1)) >> beneficiaryCountCollection.find(Filter.eq("houseId", houseId)).first.map(_.toRight(Exception(s"No se pudo encontrar entrada de cantidad de beneficiarios para CASA con ID $houseId")).map(_.beneficiaryInformation.beneficiaryCount)).rethrow.attempt
    case ReportAction.RemoveWearNeededInHouse(houseId, wear) =>
      val filter = Filters.eq("houseId", houseId)
      (wear match {
        case Wear.BoyWear(boyAttire) =>
          wearInformationCollection.updateOne(
            filter,
            Updates.combine(
              Updates.inc(s"wearInformation.shortOrTrousersNeededBySize.${boyAttire.shortOrTrousersSize}", -1),
              Updates.inc(s"wearInformation.tshirtOrShirtNeededAmountBySize.${boyAttire.tshirtOrshirtSize}", -1),
              Updates.inc(s"wearInformation.footwearNeededAmountBySize.${boyAttire.footwearSize}", -1),
              Updates.inc(s"wearInformation.sweaterNeededAmountBySize.${boyAttire.sweaterSize}", -1),
            )
          )
        case Wear.GirlWear(girlAttire) =>
          wearInformationCollection.updateOne(
            filter,
            Updates.combine(
              Updates.inc(s"wearInformation.shortOrTrousersNeededBySize.${girlAttire.shortOrTrousersSize}", -1),
              Updates.inc(s"wearInformation.tshirtOrShirtNeededAmountBySize.${girlAttire.tshirtOrshirtSize}", -1),
              Updates.inc(s"wearInformation.footwearNeededAmountBySize.${girlAttire.footwearSize}", -1),
              Updates.inc(s"wearInformation.dressNeededAmountBySize.${girlAttire.dressSize}", -1),
            )
          )
      }).void.attempt
    case ReportAction.DecrementFoodAmountNeededInHouse(houseId, childId, allergies) => foodInformationCollection.updateOne(Filters.eq("houseId", houseId), Updates.combine(Updates.inc("foodInformation.foodAmountNeeded", -1), Updates.pullAll[String](s"foodInformation.allergies.$childId", allergies.toList))).void.attempt
    case ReportAction.GetBeneficiaryCount(scope) => scope match {
      case ReportScope.Houses => beneficiaryCountCollection.find.all.map(_.toVector).nested.map(mongo => HouseWithBeneficiaryCount(mongo.houseId, mongo.beneficiaryInformation)).value.map(_.asRight[BeneficiaryCount]).attempt
      case ReportScope.General =>
        val accumulator = Accumulator.sum("beneficiaryCount", "$beneficiaryInformation.beneficiaryCount").first("_id", null)
        beneficiaryCountCollection.aggregate[BeneficiaryCount](Aggregate.group("beneficiaryInformation", accumulator)).first.map(_ getOrElse BeneficiaryCount(0)).map(_.asLeft[Vector[HouseWithBeneficiaryCount]]).attempt
    }
    case ReportAction.GetFoodAmountNeeded(houseId) => houseId match {
      case ReportScope.Houses => foodInformationCollection.find.all.map(_.toVector).nested.map(mongo => HouseWithFoodInformation(mongo.houseId, mongo.foodInformation)).value.map(_.asRight[FoodInformation]).attempt
      case ReportScope.General => {
        val accumulator = Accumulator.sum("foodAmountNeeded", "$foodInformation.foodAmountNeeded").first("_id", null)
        val aggregate = Aggregate.group("foodInformaiton", accumulator).merge("$foodInformation.allergies", MergeOptions())
        foodInformationCollection.aggregate[FoodInformation](aggregate).first.map(_ getOrElse FoodInformation(0, Map.empty)).map(_.asLeft[Vector[HouseWithFoodInformation]]).attempt

      }
    }
    case ReportAction.GetWearNeededCount(houseId) => houseId match {
      case ReportScope.Houses => wearInformationCollection.find.all.map(_.toVector).nested.map(mongo => HouseWithWearInformation(mongo.houseId, mongo.wearInformation)).value.map(_.asRight[WearInformation]).attempt
      case ReportScope.General => {
        val aggregate = Aggregate
          .merge("$wearInformation.shortOrTrousersNeededAmountBySize", MergeOptions())
          .merge("$wearInformation.tshirtOrShirtNeededAmountBySize", MergeOptions())
          .merge("$wearInformation.footwearNeededAmountBySize", MergeOptions())
          .merge("$wearInformation.sweaterNeededAmountBySize", MergeOptions())
          .merge("$wearInformation.dressNeededAmountBySize", MergeOptions())

          wearInformationCollection.aggregate[WearInformation](aggregate).first.map(_ getOrElse WearInformation(Map.empty, Map.empty, Map.empty, Map.empty, Map.empty)).map(_.asLeft[Vector[HouseWithWearInformation]]).attempt

      }
    }
  }
}

private final case class MongoHouseBeneficiaryInformation(
  _id: ObjectId,
  houseId: UUID,
  beneficiaryInformation: BeneficiaryCount
)
private final case class MongoHouseWearInformation(
  _id: ObjectId,
  houseId: UUID,
  wearInformation: WearInformation
)
private final case class MongoHouseFoodInformation(
  _id: ObjectId,
  houseId: UUID,
  foodInformation: FoodInformation
)

given [F[_]: Async]: Make[F, MongoDB] with
  def make(conf: MongoDB) =
    for {
      client <- MongoClient.fromServerAddress(ServerAddress(conf.host, conf.port))
      db <- Resource.eval { client.getDatabase("charles-angels-reports-database") }
      beneficiaryCountCollection <- Resource.eval { db.getCollectionWithCodec[MongoHouseBeneficiaryInformation]("housesBeneficiaryCount") }
      foodInformationCollection <- Resource.eval { db.getCollectionWithCodec[MongoHouseFoodInformation]("housesFoodInformation") }
      wearInformationCollection <- Resource.eval { db.getCollectionWithCodec[MongoHouseWearInformation]("housesWearInformation") }
    } yield MongoDbExecutor(beneficiaryCountCollection, wearInformationCollection, foodInformationCollection)

