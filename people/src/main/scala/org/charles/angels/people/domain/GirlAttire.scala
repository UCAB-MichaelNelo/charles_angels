package org.charles.angels.people.domain

import cats.syntax.all.*
import org.charles.angels.people.domain.errors.DressError

final case class GirlAttire(
    shortOrTrousersSize: Int,
    tshirtOrshirtSize: Int,
    dressSize: Int,
    footwearSize: Int
) {
  def setShortsOrTrousersSize(size: Int) = copy(shortOrTrousersSize = size)
  def setTshirtOrShirtSize(size: Int) = copy(tshirtOrshirtSize = size)
  def setDressSize(size: Int) = copy(dressSize = size)
  def setFootwearSize(size: Int) = copy(footwearSize = size)
}

object GirlAttire {
  object Size:
    def apply(size: Int) =
      if (size <= 0) DressError.InvalidSize.invalidNec else size.validNec

  def apply(
      shortOrTrousersSize: Int,
      tshirtOrshirtSize: Int,
      dressSize: Int,
      footwearSize: Int
  ) = (
    Size(shortOrTrousersSize),
    Size(tshirtOrshirtSize),
    Size(dressSize),
    Size(footwearSize)
  ).mapN(new GirlAttire(_, _, _, _))

  def unsafe(
      shortOrTrousersSize: Int,
      tshirtOrshirtSize: Int,
      dressSize: Int,
      footwearSize: Int
  ) = new GirlAttire(
    shortOrTrousersSize,
    tshirtOrshirtSize,
    dressSize,
    footwearSize
  )
}
