package org.charles.angels.houses.reports.data.sql

import cats.~>
import cats.syntax.all.*
import cats.effect.implicits.*
import doobie.implicits.*
import doobie.syntax.all.*
import doobie.postgres.{*, given}
import doobie.postgres.implicits.{*, given}
import cats.effect.kernel.Async
import doobie.util.transactor.Transactor
import org.charles.angels.houses.reports.data.DataReportAction
import org.charles.angels.houses.reports.data.ReportScope
import org.charles.angels.houses.reports.models.*
import cats.Id
import java.util.UUID
import org.charles.angels.houses.reports.data.Make
import cats.effect.kernel.Resource
import scala.concurrent.ExecutionContext
import doobie.hikari.HikariTransactor
import java.util.concurrent.Executors

class SqlExecutor[F[_]: Async](xa: Transactor[F]) extends (DataReportAction ~> F)  {
    type HouseWearSizeCount = Map[UUID, Vector[HouseWearSizeEntry]]
    private def clothesBySizeAmount = 
        sql"""
            SELECT 
                short_or_trousers_info[1], short_or_trousers_info[2],
                tshirt_or_shirt_info[1], tshirt_or_shirt_info[2],
                sweater_info[1], sweater_info[2],
                dress_info[1], dress_info[2],
                footwear_info[1], footwear_info[2]
            FROM crosstab('
                SELECT raw_clothing.ind, raw_clothing.dress_type, ARRAY[raw_clothing.size, raw_clothing.amount] FROM (
                    SELECT ROW_NUMBER() OVER (ORDER BY 1) as ind, ''short_or_trousers'' as dress_type, a.short_or_trousers_size as size, COUNT(*) as amount
                    FROM children c
                    INNER JOIN attires a ON a.id_child = c.id
                    GROUP BY a.short_or_trousers_size

                    UNION

                    SELECT ROW_NUMBER() OVER (ORDER BY 1) as ind, ''tshirt_or_shirt'' as dress_type, a.tshirt_or_shirt_size as size, COUNT(*) as amount
                    FROM children c
                    INNER JOIN attires a ON a.id_child = c.id
                    GROUP BY a.tshirt_or_shirt_size

                    UNION

                    SELECT ROW_NUMBER() OVER (ORDER BY 1) as ind, ''sweater'' as dress_type, a.sweater_size as size, COUNT(*) as amount
                    FROM children c
                    INNER JOIN attires a ON a.id_child = c.id
                    WHERE a.sweater_size IS NOT NULL
                    GROUP BY a.sweater_size

                    UNION

                    SELECT ROW_NUMBER() OVER (ORDER BY 1) as ind, ''dress'' as dress_type, a.dress_size as size, COUNT(*) as amount
                    FROM children c
                    INNER JOIN attires a ON a.id_child = c.id
                    WHERE a.dress_size IS NOT NULL
                    GROUP BY a.dress_size

                    UNION

                    SELECT ROW_NUMBER() OVER (ORDER BY 1) as ind, ''footwear'' as dress_type, a.footwear_size as size, COUNT(*) as amount
                    FROM children c
                    INNER JOIN attires a ON a.id_child = c.id
                    GROUP BY a.footwear_size

                ) raw_clothing
                ORDER BY raw_clothing.ind
            ', '
                SELECT *
                FROM (VALUES
                    (''short_or_trousers''),
                    (''tshirt_or_shirt''),
                    (''sweater''),
                    (''dress''),
                    (''footwear'')
                ) t(dress_type)
            ') AS (
                index INT, 
                short_or_trousers_info bigint[],
                tshirt_or_shirt_info bigint[],
                sweater_info bigint[],
                dress_info bigint[],
                footwear_info bigint[]
            ) ORDER BY (CASE COALESCE(short_or_trousers_info[1], 0)
                            WHEN 0 THEN 0
                            ELSE 1
                        END) + 
                        (CASE COALESCE(tshirt_or_shirt_info[1], 0)
                            WHEN 0 THEN 0
                            ELSE 1
                        END) + 
                        (CASE COALESCE(sweater_info[1], 0)
                            WHEN 0 THEN 0
                            ELSE 1
                        END) + 
                        (CASE COALESCE(dress_info[1], 0)
                            WHEN 0 THEN 0
                            ELSE 1
                        END) + 
                        (CASE COALESCE(footwear_info[1], 0)
                            WHEN 0 THEN 0
                            ELSE 1
                        END) DESC
        """
        .query[WearInformation]
        .to[Vector]
    private def clothesbySizeAndHouseAmount = 
        sql"""
            SELECT 
                key[2]::UUID,
                key[3]::text,
                short_or_trousers_info[1], short_or_trousers_info[2],
                tshirt_or_shirt_info[1], tshirt_or_shirt_info[2],
                sweater_info[1], sweater_info[2],
                dress_info[1], dress_info[2],
                footwear_info[1], footwear_info[2]
            FROM crosstab('
                SELECT ARRAY[clothing.ind::text, h.id::text, h.name::text] AS row_name, clothing.dress_type as cat, ARRAY[clothing.size, clothing.amount]
                FROM houses h
                INNER JOIN (SELECT * FROM (
                    SELECT ROW_NUMBER() OVER (PARTITION BY ch.house_id ORDER BY ch.house_id) as ind, ''short_or_trousers'' as dress_type, a.short_or_trousers_size as size, COUNT(*) as amount, ch.house_id as house_id
                    FROM children c
                    INNER JOIN attires a ON a.id_child = c.id
                    INNER JOIN children_houses ch ON ch.child_id = c.id
                    WHERE ch.child_id = a.id_child
                    GROUP BY ch.house_id, a.short_or_trousers_size

                    UNION

                    SELECT ROW_NUMBER() OVER (PARTITION BY ch.house_id ORDER BY ch.house_id) as ind, ''tshirt_or_shirt'' as dress_type, a.tshirt_or_shirt_size as size, COUNT(*) as amount, ch.house_id as house_id
                    FROM children c
                    INNER JOIN attires a ON a.id_child = c.id
                    INNER JOIN children_houses ch ON ch.child_id = c.id
                    WHERE ch.child_id = a.id_child
                    GROUP BY ch.house_id, a.tshirt_or_shirt_size

                    UNION

                    SELECT ROW_NUMBER() OVER (PARTITION BY ch.house_id ORDER BY ch.house_id) as ind, ''sweater'' as dress_type, a.sweater_size as size, COUNT(*) as amount, ch.house_id as house_id
                    FROM children c
                    INNER JOIN attires a ON a.id_child = c.id
                    INNER JOIN children_houses ch ON ch.child_id = c.id
                    WHERE ch.child_id = a.id_child AND a.sweater_size IS NOT NULL
                    GROUP BY ch.house_id, a.sweater_size

                    UNION

                    SELECT ROW_NUMBER() OVER (PARTITION BY ch.house_id ORDER BY ch.house_id) as ind, ''dress'' as dress_type, a.dress_size as size, COUNT(*) as amount, ch.house_id as house_id
                    FROM children c
                    INNER JOIN attires a ON a.id_child = c.id
                    INNER JOIN children_houses ch ON ch.child_id = c.id
                    WHERE ch.child_id = a.id_child AND a.dress_size IS NOT NULL
                    GROUP BY ch.house_id, a.dress_size

                    UNION

                    SELECT ROW_NUMBER() OVER (PARTITION BY ch.house_id ORDER BY ch.house_id) as ind, ''footwear'' as dress_type, a.footwear_size as size, COUNT(*) as amount, ch.house_id as house_id
                    FROM children c
                    INNER JOIN attires a ON a.id_child = c.id
                    INNER JOIN children_houses ch ON ch.child_id = c.id
                    WHERE ch.child_id = a.id_child
                    GROUP BY ch.house_id, a.footwear_size
                ) raw_clothing) clothing ON clothing.house_id = h.id
                ORDER BY h.id
            ', '
                SELECT *
                FROM (VALUES
                    (''short_or_trousers''),
                    (''tshirt_or_shirt''),
                    (''sweater''),
                    (''dress''),
                    (''footwear'')
                ) t(dress_type)
            ') AS (
                key TEXT[],
                short_or_trousers_info bigint[],
                tshirt_or_shirt_info bigint[],
                sweater_info bigint[],
                dress_info bigint[],
                footwear_info bigint[]
            ) ORDER BY key[2], (CASE COALESCE(short_or_trousers_info[1], 0)
                            WHEN 0 THEN 0
                            ELSE 1
                        END) + 
                        (CASE COALESCE(tshirt_or_shirt_info[1], 0)
                            WHEN 0 THEN 0
                            ELSE 1
                        END) + 
                        (CASE COALESCE(sweater_info[1], 0)
                            WHEN 0 THEN 0
                            ELSE 1
                        END) + 
                        (CASE COALESCE(dress_info[1], 0)
                            WHEN 0 THEN 0
                            ELSE 1
                        END) + 
                        (CASE COALESCE(footwear_info[1], 0)
                            WHEN 0 THEN 0
                            ELSE 1
                        END) DESC
        """
        .query[(UUID, String, WearInformation)]
        .to[Vector]
    def apply[A](fa: DataReportAction[A]): F[A] = fa match {
        case DataReportAction.GetChildrenWithFamily => 
            sql"""SELECT c.id, CONCAT(pi.name, ' ', pi.lastname), pi.ci, ARRAY(
                    SELECT CONCAT(fpi.name, ' ', fpi.lastname, '::', fpi.ci::text, ':', f.id::text)
                    FROM children f
                    INNER JOIN children_houses fh ON fh.child_id = f.id
                    INNER JOIN personal_information fpi ON f.id = fpi.id_children
                    WHERE EXISTS (SELECT 1 FROM related_beneficiaries WHERE (child_id = c.id AND related_id = f.id) OR (child_id = f.id AND related_id = c.id))
                        AND fh.house_id = ch.house_id
                ), h.id, h.name
                FROM children c
                INNER JOIN children_houses ch ON ch.child_id = c.id
                INNER JOIN personal_information pi ON c.id = pi.id_children
                INNER JOIN houses h ON ch.house_id = h.id
                WHERE EXISTS (SELECT 1 FROM related_beneficiaries WHERE child_id = c.id OR related_id = c.id)"""
            .query[(ChildInformation, Vector[String], UUID, String)]
            .to[Vector]
            .map(vector => 
                vector
                .groupBy { case (_, _, id, name) => id -> name }
                .map {
                    case ((id, name), vector) => HouseWithChildrenAndFamily(
                        id,
                        name,
                        vector.map {
                            case (ci, fi, _, _) => 
                                ChildWithFamily(
                                    ci,
                                    fi.map { s => 
                                        val ind = s.lastIndexOf(":")
                                        val (fullnameAndCi, id) = s.splitAt(ind)
                                        val ciInd = fullnameAndCi.indexOf("::")
                                        val (fullname, ci) = fullnameAndCi.splitAt(ciInd)

                                        ChildInformation(UUID.fromString(id.slice(1, id.length)), fullname, ci.slice(2, ci.length).toInt)
                                    }
                                )
                        }
                    )
                }
                .toVector
            )
            .transact(xa)
            .attempt     
        case DataReportAction.GetWearNeededCount(ReportScope.General) => 
            clothesBySizeAmount
            .map(_.asLeft[Vector[HouseWithWearInformation]])
            .transact(xa)
            .attempt
        case DataReportAction.GetWearNeededCount(ReportScope.Houses) => 
            clothesbySizeAndHouseAmount
            .map(vector => 
                vector
                .groupBy{ case (id, name, _) => (id, name) }
                .map { case ((id, name), information) => id -> HouseWithWearInformation(
                    houseName = name,
                    houseId = id,
                    wearInformation = information.map(_._3)
                )}
                .values
                .toVector
                .asRight[Vector[WearInformation]]
            )
            .transact(xa)
            .attempt
        case DataReportAction.GetChildrenWithSixMonthsOfRemainingTime(ReportScope.General) =>
            sql"""
                SELECT c.id, CONCAT(pi.name, ' ', pi.lastname), pi.ci, pi.birthdate, h.maximum_age
                FROM children c
                INNER JOIN children_houses ch ON ch.child_id = c.id
                INNER JOIN houses h ON ch.house_id = h.id
                INNER JOIN personal_information pi ON c.id = pi.id_children
                WHERE ((pi.birthdate + interval '1 year' * (1 + h.maximum_age)) - now()) <= interval '6 month'
            """
            .query[ChildWithSixMonthsOfRemainingTime]
            .to[Vector]
            .map(_.asLeft[Vector[HouseWithChildrenWithSixMonthsOfRemainingTime]])
            .transact(xa)
            .attempt
        case DataReportAction.GetChildrenWithSixMonthsOfRemainingTime(ReportScope.Houses) =>
            sql"""
                SELECT c.id, CONCAT(pi.name, ' ', pi.lastname), pi.ci, pi.birthdate, h.maximum_age, h.id, h.name
                FROM children c
                INNER JOIN children_houses ch ON ch.child_id = c.id
                INNER JOIN houses h ON ch.house_id = h.id
                INNER JOIN personal_information pi ON c.id = pi.id_children
                WHERE ((pi.birthdate + interval '1 year' * (1 + h.maximum_age)) - now()) <= interval '6 month'
            """
            .query[(ChildWithSixMonthsOfRemainingTime, UUID, String)]
            .to[List]
            .map(_
                .groupBy { case(_, id, name) => id -> name }
                .map {
                    case ((id, name), childrenIds) => HouseWithChildrenWithSixMonthsOfRemainingTime(id, name, childrenIds.map(_._1).toVector)
                }
                .toVector
                .asRight[Vector[ChildWithSixMonthsOfRemainingTime]]
            )
            .transact(xa)
            .attempt
        case DataReportAction.GetFoodAmountNeeded(ReportScope.Houses) => 
            sql"""
                SELECT h.id, h.name, COUNT(c.*) FROM houses h
                LEFT JOIN children_houses ch ON ch.house_id = h.id
                LEFT JOIN children c ON ch.child_id = c.id
                GROUP BY h.id
            """
            .query[HouseWithFoodAmountNeeded]
            .to[Vector]
            .map(_.asRight[FoodAmountNeeded])
            .transact(xa)
            .attempt
        case DataReportAction.GetFoodAmountNeeded(ReportScope.General) => 
            sql"""
                SELECT COUNT(c.*) FROM children c
                INNER JOIN children_houses ch ON ch.child_id = c.id
            """
            .query[FoodAmountNeeded]
            .unique
            .map(_.asLeft[Vector[HouseWithFoodAmountNeeded]])
            .transact(xa)
            .attempt
    }
}

final case class Sql(username: String, password: String);

given [F[_]: Async]: Make[F, Sql] with
  def make(sql: Sql) = for
    tp <- Resource.eval { Async[F].delay(ExecutionContext.fromExecutorService(Executors.newWorkStealingPool(8))) }
    transactor <- HikariTransactor.newHikariTransactor[F](
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5432/charles-angels",
      sql.username,
      sql.password,
      tp
    )
  yield SqlExecutor(transactor)

final case class ExistingSqlTransactor[F[_]](transactor: Transactor[F]);

given [F[_]: Async]: Make[F, ExistingSqlTransactor[F]] with
  def make(sql: ExistingSqlTransactor[F]) = Resource.pure(SqlExecutor(sql.transactor))