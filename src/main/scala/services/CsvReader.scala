package services

import fs2.Task
import models.Inputs

import scala.io.Source
import scala.util.Try

object CsvReader {

  def readCsvInputs(filePath: String): Task[Vector[Inputs]] =
    Task.delay(Source.fromResource(filePath).getLines()).map { lines =>
      lines.map{ line =>
        line.split(",").toVector match {
          case s :+ tail =>
            Inputs(
              values = s.map(_.toDouble),
              expectedClass = Try(tail.toDouble).toOption
            )
        }
      }.toVector
    }
}
