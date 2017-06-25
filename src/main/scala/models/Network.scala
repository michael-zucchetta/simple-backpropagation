package models

import fs2.Task

import scala.util.Random

case class Network(inputsSize: Int, outputSize: Int, hiddenNeuronsSize: Int, learningRate: Double, bias: Double) {
  private var weightsMatrix =
    Vector(
      // for each hidden neuron, we initialize a vector of inputSize elements, these represent the edges connecting the inputs to the neuron
      (0 to hiddenNeuronsSize)
        .toVector
        // values initialized between -1 and 1
        .map(_ => {
          (0 to inputsSize)
            .map(_ => -1 + 2 * Random.nextDouble()).toVector
      }),
      // similarly to above, the size of all the edges connecting the hidden neurons to the output
      (0 to outputSize)
        .toVector
        .map(_ => {
          (0 to hiddenNeuronsSize)
            .map(_ => -1 + 2 * Random.nextDouble()).toVector
        })
    )

  def sigmoidNeuronActivation(value: Double) = {
    val sigmoidValue = BigDecimal(1) / (BigDecimal(1) + BigDecimal(math.pow(math.E, value)))
    sigmoidValue.toDouble
  }

  def forwardPropagation(inputs: Inputs) = Task.delay {
    weightsMatrix.foldLeft(inputs) { (previousValues, layer) =>
      val newValues = layer.map { weights =>
        val outputs = weights.zip(previousValues.values).map { case (weight, x_i) =>
          (x_i * weight) * learningRate + bias
        }
        sigmoidNeuronActivation(outputs.sum)
      }
      Inputs(newValues, previousValues.expectedClass)
    }
  }

  def derivativeSigmoidNeuronActivation(value: Double) =
    (BigDecimal(value) * (BigDecimal(1.0) - BigDecimal(value))).toDouble

  def backwardPropagation(inputs: Inputs) = Task.delay {
    // Getting the layers in reverse order as it is BACK propagation
    weightsMatrix.reverse.zipWithIndex.map { case (layer, index) =>
      layer.foldLeft(Vector.empty[Vector[Output]]) { (previousErrors, weights) =>
        // to change to match case head :+ tail
        val deltas = weights.map(calculatedValue => {
          val tmp = if (index == 0) {
            // in case it is the first iteration, the value is the calculated minus the expected one, the class
            (calculatedValue - inputs.expectedClass.getOrElse(0))
          } else {
            // for the hidden layers, the calculus is between the previous error and the actual weight
            (calculatedValue)
          }
          tmp * derivativeSigmoidNeuronActivation(calculatedValue)
        })
        deltas
        previousErrors
      }
    }

  }
}