package eu.joaocosta.minart.extra

import scala.io.{ BufferedSource, Source }
import scala.util.Try

import eu.joaocosta.minart.graphics._
import eu.joaocosta.minart.graphics.pure._

object Image {

  def invert(surface: RamSurface): RamSurface =
    new RamSurface(
      surface.data.map(_.map { c =>
        Color(255 - c.r, 255 - c.g, 255 - c.b)
      }))

  def flipH(surface: RamSurface): RamSurface =
    new RamSurface(surface.data.map(_.reverse))

  def flipV(surface: RamSurface): RamSurface =
    new RamSurface(surface.data.reverse)

  def transpose(surface: RamSurface): RamSurface =
    new RamSurface(surface.data.transpose.map(_.toArray))

  def loadPpmImage(resource: Resource): Try[RamSurface] = Try {
    println("Loading resource")
    val inputStream = resource.asInputStream()
    val byteIterator: Iterator[Int] = Iterator.continually(inputStream.read()).takeWhile(_ != -1)
    val charIterator: Iterator[Char] = byteIterator.map(_.toChar)
    def nextLine(): String = charIterator.takeWhile(_ != '\n').mkString("")
    val lineIt = Iterator.continually(nextLine())
    val stringIt = lineIt.filterNot(_.startsWith("#")).flatMap(_.split(" "))
    val builder = Array.newBuilder[Int]
    val format = stringIt.next()
    val width = stringIt.next().toInt
    val height = stringIt.next().toInt
    require(stringIt.next() == "255", "Invalid color range")
    format match {
      case "P3" =>
        val intIterator = stringIt.map(_.toInt)
        println("Reading pixels...")
        (0 until (width * height)).foreach { _ =>
          val color = Color(intIterator.next(), intIterator.next(), intIterator.next()).argb
          builder += color
        }
        inputStream.close()
      case "P6" =>
        val intIterator = byteIterator
        println("Reading pixels...")
        (0 until (width * height)).foreach { _ =>
          val color = Color(intIterator.next(), intIterator.next(), intIterator.next()).argb
          builder += color
        }
        inputStream.close()
      case fmt =>
        inputStream.close()
        throw new Exception("Invalid pixel format: " + fmt)
    }
    println("Formatting")
    val pixels = builder.result().sliding(width, width).map(_.map(Color.fromRGB).toArray).toVector
    println("Done")
    new RamSurface(pixels)
  }

  def loadBmpImage(resource: Resource): Try[RamSurface] = Try {
    println("Loading resource")
    val inputStream = resource.asInputStream()
    val byteIterator: Iterator[Int] = Iterator.continually(inputStream.read()).takeWhile(_ != -1)
    val builder = Array.newBuilder[Int]
    def readNumber(bytes: Int) =
      byteIterator.take(bytes).zipWithIndex.map { case (num, idx) => num.toInt << (idx * 8) }.sum
    def discardBytes(bytes: Int) =
      byteIterator.take(bytes).foldLeft(())((_, _) => ())

    val formatString = byteIterator.take(2).map(_.toChar).mkString
    require(formatString == "BM", s"Invalid file format ($formatString): Only windows BMPs are supported")
    val size = readNumber(4)
    readNumber(4) // Reserved
    val offset = readNumber(4)
    val dibHeaderSize = readNumber(4)
    require(dibHeaderSize == 40, s"Unsupported DIB Header with size $dibHeaderSize")
    val width = readNumber(4)
    val height = readNumber(4)
    val colorPlanes = readNumber(2)
    require(colorPlanes == 1, s"Invalid number of color planes ($colorPlanes)")
    val bitsPerPixel = readNumber(2)
    val hasAlpha = bitsPerPixel == 32
    require(bitsPerPixel == 24 || bitsPerPixel == 32, s"Invalid bit depth ($bitsPerPixel): Only 24bit and 32bit BMPs are supported")
    val compressionMethod = readNumber(4)
    require(compressionMethod == 0, "Unsupported compression")
    discardBytes(5 * 4) // Irrelevant header data
    val rowSize = 4 * ((bitsPerPixel * width) + 31) / 32
    val skipBytes = rowSize - (bitsPerPixel * width)
    discardBytes(offset - 14 - 40) // Skip to the offset
    println("Reading pixels...")
    (0 until height).foreach { _ =>
      (0 until width).foreach { _ =>
        val b = byteIterator.next()
        val g = byteIterator.next()
        val r = byteIterator.next()
        if (hasAlpha) byteIterator.next()
        val color = Color(r, g, b).argb
        builder += color
      }
      discardBytes(skipBytes)
    }
    inputStream.close()
    println("Formatting...")
    val pixels = builder.result().sliding(width, width).map(_.map(Color.fromRGB).toArray).toVector.reverse
    println("Done")
    new RamSurface(pixels)
  }
}
