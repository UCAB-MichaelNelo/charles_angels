package org.charles.angels.houses.db.people.sql

import cats.~>
import cats.syntax.all.*
import cats.effect.implicits.*
import doobie.implicits.*
import doobie.syntax.all.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.transactor.Transactor
import doobie.ConnectionIO
import org.charles.angels.houses.db.people.DatabaseAction
import java.util.UUID
import org.charles.angels.people.domain.PersonalInformation
import cats.effect.kernel.Async
import org.charles.angels.people.domain.Child
import org.charles.angels.people.domain.ChildInformation
import java.io.File
import org.charles.angels.people.domain.Wear
import org.charles.angels.people.domain.BoyAttire
import org.charles.angels.people.domain.GirlAttire
import doobie.util.update.Update
import org.charles.angels.houses.db.Make
import cats.effect.kernel.Resource
import doobie.hikari.HikariTransactor
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import java.time.LocalDate
import org.charles.angels.people.application.models.PersonalInformationOfChild

class SqlExecutor[F[_]: Async](xa: Transactor[F])
    extends (DatabaseAction ~> F) {

  private def getSexChar = (_: Wear) match {
    case Wear.BoyWear(_)  => "M"
    case Wear.GirlWear(_) => "F"
  }

  private def insertOptionalPersonalInformation(
      inf: Option[PersonalInformation],
      idChildren: Option[UUID]
  ) = {
    inf match {
      case Some(fi) =>
        sql"""INSERT INTO "personal_information" (ci, name, lastname, birthdate, id_children) VALUES (${fi.ci}, ${fi.name}, ${fi.lastname}, ${fi.birthdate}, $idChildren) ON CONFLICT DO NOTHING""".update.run.void
      case None => ().pure[ConnectionIO]
    }
  }
  def apply[A](action: DatabaseAction[A]) = action match {
    case DatabaseAction.SetTutorCIToChild(id, ci) =>
      sql"""UPDATE "children" SET non_parent_ci = $ci WHERE id = $id"""
      .update
      .run
      .transact(xa)
      .void
      .attempt
    case DatabaseAction.SetFatherCIToChild(id, ci) =>
      sql"""UPDATE "children" SET father_ci = $ci WHERE id = $id"""
      .update
      .run
      .transact(xa)
      .void
      .attempt
    case DatabaseAction.SetMotherCIToChild(id, ci) =>
      sql"""UPDATE "children" SET mother_ci = $ci WHERE id = $id"""
      .update
      .run
      .transact(xa)
      .void
      .attempt
    case DatabaseAction.RemoveRelatedBeneficiary(id, bid) =>
      sql"""DELETE FROM "related_beneficiaries" WHERE child_id = $id AND related_id = $bid"""
      .update
      .run
      .transact(xa)
      .void
      .attempt
    case DatabaseAction.AddRelatedBeneficiary(id, bid) =>
      sql"""INSERT INTO "related_beneficiaries" (child_id, related_id) VALUES ($id, $bid)"""
      .update
      .run
      .transact(xa)
      .void
      .attempt
    case DatabaseAction.DoesChildCiExist(ci) =>
      sql"""SELECT ci FROM "personal_information" WHERE ci = $ci"""
      .query[Int]
      .option
      .transact(xa)
      .attempt
    case DatabaseAction.GetChildrenPersonalInformation =>
      sql"""SELECT * FROM "personal_information" WHERE id_children IS NOT NULL"""
      .query[PersonalInformationOfChild]
      .to[Vector]
      .transact(xa)
      .attempt
    case DatabaseAction.GetAllPersonalInformation =>
      sql"""SELECT * FROM "personal_information" WHERE id_children IS NULL"""
      .query[SqlExecutor.PersonalInformationModel]
      .to[Vector]
      .nested
      .map(_.toPersonalInformation)
      .value
      .transact(xa)
      .attempt
    case DatabaseAction.GetPersonalInformation(ci) => 
      sql"""SELECT * FROM "personal_information" WHERE ci = $ci AND id_children IS NULL"""
      .query[SqlExecutor.PersonalInformationModel]
      .option
      .nested
      .map(_.toPersonalInformation)
      .value
      .transact(xa)
      .attempt
    case DatabaseAction.GetChildByCI(ci) =>
      (
        for
          result <-
            sql"""SELECT c.id, c.sex, c.photo, pi.*, mpi.*, fpi.*, nppi.*, a.*
                  FROM "children" c
                  INNER JOIN "personal_information" pi ON c.id = pi.id_children
                  LEFT JOIN "personal_information" mpi ON c.mother_ci = pi.ci
                  LEFT JOIN "personal_information" fpi ON c.father_ci = pi.ci
                  LEFT JOIN "personal_information" nppi ON c.non_parent_ci = pi.ci
                  INNER JOIN "attires" a ON c.id = a.id_child
                  WHERE pi.ci = $ci
              """
              .query[SqlExecutor.ChildModel]
              .option
          related <- result match {
            case Some(childModel) =>
              sql"""SELECT rb.related_id
                  FROM "related_beneficiaries" rb
                  WHERE rb.child_id = ${childModel.id}"""
                .query[UUID]
                .to[List]
            case None => List.empty.pure[ConnectionIO]
          }
        yield result.flatMap(_.toChild(related.toVector))
      ).transact(xa).attempt
    case DatabaseAction.GetAllChildren =>
      (
        for
          childModels <-
            sql"""SELECT c.id, c.sex, c.photo, pi.*, mpi.*, fpi.*, nppi.*, a.*
                  FROM "children" c
                  INNER JOIN "personal_information" pi ON c.id = pi.id_children
                  LEFT JOIN "personal_information" mpi ON c.mother_ci = mpi.ci
                  LEFT JOIN "personal_information" fpi ON c.father_ci = fpi.ci
                  LEFT JOIN "personal_information" nppi ON c.non_parent_ci = nppi.ci
                  INNER JOIN "attires" a ON c.id = a.id_child
              """
              .query[SqlExecutor.ChildModel]
              .to[Vector]
          result <- childModels.toList.traverse { child =>
            sql"""SELECT rb.related_id
                  FROM "related_beneficiaries" rb
                  WHERE rb.child_id = ${child.id}
               """
              .query[UUID]
              .to[List]
              .tupleLeft(child)
          }
        yield result.toVector.flatMap((tup) => tup._1.toChild(tup._2.toVector).map(Vector(_)).getOrElse(Vector.empty[Child]))
      ).transact(xa).attempt
    case DatabaseAction.GetChild(id) =>
      (
        for
          result <-
            sql"""SELECT c.id, c.sex, c.photo, pi.*, mpi.*, fpi.*, nppi.*, a.*
                  FROM "children" c
                  INNER JOIN "personal_information" pi ON c.id = pi.id_children
                  LEFT JOIN "personal_information" mpi ON c.mother_ci = mpi.ci
                  LEFT JOIN "personal_information" fpi ON c.father_ci = fpi.ci
                  LEFT JOIN "personal_information" nppi ON c.non_parent_ci = nppi.ci
                  INNER JOIN "attires" a ON c.id = a.id_child
                  WHERE c.id = $id
              """
              .query[SqlExecutor.ChildModel]
              .option
          related <-
            sql"""SELECT rb.related_id
                  FROM "related_beneficiaries" rb
                  WHERE rb.child_id = $id
               """
              .query[UUID]
              .to[List]
        yield result.flatMap(_.toChild(related.toVector))
      ).transact(xa).attempt
    case DatabaseAction.StoreChild(info, wear, id) =>
      var sexChar = getSexChar(wear)
      val attire = SqlExecutor.AttireModel(wear, id)
      val relatedBen = info.relatedBeneficiaries
      (insertOptionalPersonalInformation(info.father, None) >>
        insertOptionalPersonalInformation(info.mother, None) >>
        insertOptionalPersonalInformation(info.nonParent, None) >>
        sql"""INSERT INTO "children" (id, sex, photo, father_ci, mother_ci, non_parent_ci) VALUES (
          $id,
          $sexChar,
          ${info.photo.getAbsolutePath},
          ${info.father.map(_.ci)},
          ${info.mother.map(_.ci)},
          ${info.nonParent.map(_.ci)})
        """.update.run >>
        insertOptionalPersonalInformation(info.information.some, id.some) >>
        Update[(UUID, UUID)](
          """INSERT INTO "related_beneficiaries" (child_id, related_id) VALUES (?, ?)"""
        ).updateMany(relatedBen.map(p => (id, p))) >>
        sql"""INSERT INTO "attires" (short_or_trousers_size, tshirt_or_shirt_size, sweater_size, dress_size, footwear_size, id_child) VALUES (${attire.shortOrTrousersSize}, ${attire.tshirtOrshirtSize}, ${attire.sweaterSize}, ${attire.dressSize}, ${attire.footwearSize}, $id)""".update.run).void
        .transact(xa)
        .attempt
    case DatabaseAction.UpdateInformation(ci, info) =>
      sql"""UPDATE "personal_information" SET ci = ${info.ci}, name = ${info.name}, lastname = ${info.lastname}, birthdate = ${info.birthdate} WHERE ci = $ci""".update.run.void
        .transact(xa)
        .attempt
    case DatabaseAction.DeleteInformation(ci) =>
      sql"""DELETE FROM "personal_information" WHERE ci = $ci""".update.run.void
        .transact(xa)
        .attempt
    case DatabaseAction.SaveInformation(info) =>
      insertOptionalPersonalInformation(info.some, None).void
        .transact(xa)
        .attempt
    case DatabaseAction.UpdateAttire(id, wear) =>
      val sexChar = getSexChar(wear)
      val attire = SqlExecutor.AttireModel(wear, id)
      ((sql"""UPDATE "attires" SET short_or_trousers_size = ${attire.shortOrTrousersSize}, tshirt_or_shirt_size = ${attire.tshirtOrshirtSize}, sweater_size = ${attire.sweaterSize}, dress_size = ${attire.dressSize}, footwear_size = ${attire.footwearSize} WHERE id_child = $id""".update.run) >>
        (sql"""UPDATE "children" SET sex = $sexChar WHERE id = $id""").update.run)
        .void
        .transact(xa)
        .attempt
    case DatabaseAction.UpdatePhoto(id, filename) =>
      sql"""UPDATE "children" SET photo = $filename WHERE id = $id""".update.run.void
        .transact(xa)
        .attempt
    case DatabaseAction.DeleteChild(id) =>
      (sql"""DELETE FROM "personal_information" pi USING "children" c WHERE pi.id_children = $id AND c.id = $id""".update.run >>
        sql"""DELETE FROM "related_beneficiaries" WHERE child_id = $id OR related_id = $id""".update.run >>
        sql"""DELETE FROM "attires" WHERE id_child = $id""".update.run >>
        sql"""DELETE FROM "children" WHERE id = $id""".update.run).void
        .transact(xa)
        .attempt
  }
}

object SqlExecutor {
  final case class AttireModel(
      shortOrTrousersSize: Int,
      tshirtOrshirtSize: Int,
      sweaterSize: Option[Int],
      dressSize: Option[Int],
      footwearSize: Int,
      idChild: UUID
  )
  object AttireModel {
    def apply(wear: Wear, idChild: UUID) = wear match {
      case Wear.BoyWear(attire) =>
        new AttireModel(
          attire.shortOrTrousersSize,
          attire.tshirtOrshirtSize,
          attire.sweaterSize.some,
          None,
          attire.footwearSize,
          idChild
        )
      case Wear.GirlWear(attire) =>
        new AttireModel(
          attire.shortOrTrousersSize,
          attire.tshirtOrshirtSize,
          None,
          attire.dressSize.some,
          attire.footwearSize,
          idChild
        )
    }
  }
  final case class PersonalInformationModel(
      ci: Int,
      name: String,
      lastname: String,
      birthdate: LocalDate,
      id_children: Option[UUID]
  ) {
    def toPersonalInformation =
      PersonalInformation.unsafe(ci, name, lastname, birthdate)
  }
  final case class ChildModel(
      id: UUID,
      sex: String,
      photo: String,
      pi: PersonalInformationModel,
      mpi: Option[PersonalInformationModel],
      fpi: Option[PersonalInformationModel],
      nppi: Option[PersonalInformationModel],
      attire: AttireModel
  ) {
    def toChild(
        relatedBeneficiaries: Vector[UUID]
    ): Option[Child] = {
      val ci = ChildInformation(
        pi.toPersonalInformation,
        fpi.map(_.toPersonalInformation),
        mpi.map(_.toPersonalInformation),
        nppi.map(_.toPersonalInformation),
        relatedBeneficiaries,
        File(photo)
      )
      (sex, attire.sweaterSize, attire.dressSize) match {
        case ("M", Some(sweaterSize), None) =>
          Child.Boy
            .unsafe(
              id,
              ci,
              BoyAttire.unsafe(
                attire.shortOrTrousersSize,
                attire.tshirtOrshirtSize,
                sweaterSize,
                attire.footwearSize
              )
            )
            .some
        case ("F", None, Some(dressSize)) =>
          Child.Girl
            .unsafe(
              id,
              ci,
              GirlAttire.unsafe(
                attire.shortOrTrousersSize,
                attire.tshirtOrshirtSize,
                dressSize,
                attire.footwearSize
              )
            )
            .some
        case _ => None
      }
    }
  }
}

final case class Sql(username: String, password: String);

given [F[_]: Async]: Make[F, DatabaseAction, Sql] with
  def make(sql: Sql) = for
    tp <- Resource.eval {
      Async[F].delay(
        ExecutionContext.fromExecutorService(Executors.newWorkStealingPool(8))
      )
    }
    transactor <- HikariTransactor.newHikariTransactor[F](
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5432/charles-angels",
      sql.username,
      sql.password,
      tp
    )
  yield SqlExecutor(transactor)
