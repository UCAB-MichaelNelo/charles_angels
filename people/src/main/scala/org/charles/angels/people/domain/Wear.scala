package org.charles.angels.people.domain

enum Wear { wear =>
  case BoyWear(wear: BoyAttire)
  case GirlWear(wear: GirlAttire)

  def setShortOrTrousersSize(size: Int) = wear match {
    case BoyWear(wear)  => BoyWear(wear.setShortsOrTrousersSize(size))
    case GirlWear(wear) => GirlWear(wear.setShortsOrTrousersSize(size))
  }
  def setTShirtOrShirtSize(size: Int) = wear match {
    case BoyWear(wear)  => BoyWear(wear.setTshirtOrShirtSize(size))
    case GirlWear(wear) => GirlWear(wear.setTshirtOrShirtSize(size))
  }
  def setSweaterSize(size: Int) = wear match {
    case BoyWear(wear)  => BoyWear(wear.setSweaterSize(size))
    case GirlWear(wear) => BoyWear(BoyAttire.unsafe(wear.shortOrTrousersSize, wear.tshirtOrshirtSize, size, wear.footwearSize))
  }
  def setDressSize(size: Int) = wear match {
    case BoyWear(wear)  => GirlWear(GirlAttire.unsafe(wear.shortOrTrousersSize, wear.tshirtOrshirtSize, size, wear.footwearSize))
    case GirlWear(wear) => GirlWear(wear.setDressSize(size))
  }
  def setFootwearSize(size: Int) = wear match {
    case BoyWear(wear)  => BoyWear(wear.setFootwearSize(size))
    case GirlWear(wear) => GirlWear(wear.setFootwearSize(size))
  }
}
