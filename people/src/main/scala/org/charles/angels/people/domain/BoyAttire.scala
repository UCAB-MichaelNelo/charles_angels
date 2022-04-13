package org.charles.angels.people.domain

import cats.syntax.all.*
import org.charles.angels.people.domain.errors.DressError

final case class BoyAttire(
    shortOrTrousersSize: Int,
    tshirtOrshirtSize: Int,
    sweaterSize: Int,
    footwearSize: Int
) {
  def setShortsOrTrousersSize(size: Int) = copy(shortOrTrousersSize = size)
  def setTshirtOrShirtSize(size: Int) = copy(tshirtOrshirtSize = size)
  def setSweaterSize(size: Int) = copy(sweaterSize = size)
  def setFootwearSize(size: Int) = copy(footwearSize = size)
}

object BoyAttire {
  object Size:
    def apply(size: Int) =
      if (size <= 0) DressError.InvalidSize.invalidNec else size.validNec

  def apply(
      shortOrTrousersSize: Int,
      tshirtOrshirtSize: Int,
      sweaterSize: Int,
      footwearSize: Int
  ) = (
    Size(shortOrTrousersSize),
    Size(tshirtOrshirtSize),
    Size(sweaterSize),
    Size(footwearSize)
  ).mapN(new BoyAttire(_, _, _, _))

  def unsafe(
      shortOrTrousersSize: Int,
      tshirtOrshirtSize: Int,
      sweaterSize: Int,
      footwearSize: Int
  ) = new BoyAttire(
    shortOrTrousersSize,
    tshirtOrshirtSize,
    sweaterSize,
    footwearSize
  )
}
