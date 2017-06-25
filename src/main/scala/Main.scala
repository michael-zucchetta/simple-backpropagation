import fs2.Task
import services.CsvReader
import models._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.Source

object Main {
  def main(args: Array[String]): Unit = {
    val learningRate = 0.5
    val bias = 1.0
    val inputValues = for {
      inputValues <- CsvReader.readCsvInputs("test.txt")
      inputSize = inputValues.headOption.map(_.values.size).getOrElse(0)
      network = Network(inputSize, 2, 5, learningRate, bias)
      forwardResults <- network.forwardPropagation(inputValues.head)

    } yield {
//      println(s"${network} ${result.map(_.mkString(",")).mkString("\n")}")
      println(s"${forwardResults}")
      forwardResults
    }
    val result = Await.result(inputValues.unsafeRunAsyncFuture(), Duration.Inf)
  }
}
