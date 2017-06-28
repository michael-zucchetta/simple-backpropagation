import fs2.Task
import services.CsvReader
import models._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main {
  def main(args: Array[String]): Unit = {
    val learningRate = 0.5
    val bias = 1.0
    val forcedWeights = Vector(
      Vector(
        // edges  connecting the hidden neuron from input
        Vector(0.8, 0.2),
        // second hidden neuron
        Vector(0.4, 0.9),
        // edges going from input to third neuron
        Vector(0.3, 0.5)
      ),
      Vector(
        Vector(0.3, 0.5, 0.9)
      )
    )
    val inputValues = for {
      inputValues <- CsvReader.readCsvInputs("test.txt")
      inputSize = inputValues.headOption.map(_.values.size).getOrElse(0)
      network = Network(inputSize, 2, 3, learningRate, bias, Some(forcedWeights))
      forwardResults <- network.forwardPropagation(inputValues.head)
      backwardResults <- network.backwardPropagation(forwardResults)
    } yield {
//      println(s"${network} ${result.map(_.mkString(",")).mkString("\n")}")
//      println(s"${forwardResults} vs ${backwardResults}")
      println(s"${forwardResults}")

      forwardResults
    }
    val result = Await.result(inputValues.unsafeRunAsyncFuture(), Duration.Inf)
  }
}
