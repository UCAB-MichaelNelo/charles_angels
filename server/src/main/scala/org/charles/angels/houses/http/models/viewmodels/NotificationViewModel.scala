package org.charles.angels.houses.http.models.viewmodels

import org.charles.angels.houses.notifications.Notification
import io.circe.JsonObject
import io.circe.syntax.*
import io.circe.Json

final case class NotificationViewModel(`type`: String, body: Json)

object NotificationViewModel {
  def apply(notification: Notification) = notification match {
    case Notification.SixMonthsBeforeMaxAge(id, house) =>
      new NotificationViewModel(
        "sixMonthsUntilMaxAge",
        JsonObject(
          "childId" -> id.asJson,
          "houseId" -> house.id.asJson
        ).asJson
      )
    case Notification.NotificationsStopped =>
      new NotificationViewModel(
        "notificationsStopped",
        Json.Null
      )
  }
}
