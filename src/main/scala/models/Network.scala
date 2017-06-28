package models

import fs2.Task
import org.log4s.getLogger

import scala.util.Random

case class Network(inputsSize: Int, outputSize: Int, hiddenNeuronsSize: Int, learningRate: Double, bias: Double, forcedWeightsOpt: Option[Vector[Vector[Vector[Double]]]] = None) {
  private[this] val logger = getLogger

  private var weightsMatrix = forcedWeightsOpt.getOrElse {
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
  }


  def sigmoidNeuronActivation(value: Double) = {
    val sigmoidValue = BigDecimal(1) / (BigDecimal(1) + BigDecimal(math.pow(math.E, -value)))
    sigmoidValue.toDouble
  }

  def forwardPropagation(inputs: Inputs) = Task.delay {
    weightsMatrix.foldLeft(Vector(inputs)) { (previousValues, layer) =>
      val prevInputs = previousValues.lastOption.getOrElse(Inputs(Vector.empty[Double], None))
      val newValues = layer.map { weights =>
        val outputs = weights.zip(prevInputs.values).map { case (weight, x_i) =>
          logger.info(s"x_i * weight $x_i $weight learningRate $learningRate $bias ")
          (x_i * weight)// * learningRate + bias
        }
        logger.info(s"outputs $outputs")
        sigmoidNeuronActivation(outputs.sum)
      }
      logger.info(s"Results $newValues")
      previousValues :+ Inputs(newValues, prevInputs.expectedClass)
    }
  }

  def derivativeSigmoidNeuronActivation(value: Double) =
    (BigDecimal(value) * (BigDecimal(1.0) - BigDecimal(value))).toDouble

  /*
  def backwardPropagation(inputs: Inputs) = Task.delay {
    // in case it is the first iteration, the value is the calculated minus the expected one, the class
    val initialOutputs = inputs.values.map { calculatedValue =>
      val error = inputs.expectedClass.getOrElse(0) - calculatedValue
      error * derivativeSigmoidNeuronActivation(calculatedValue)
    }.map(Output)
    logger.info(s"Initial output $initialOutputs")
    // Getting the layers in reverse order as it is BACK propagation
    weightsMatrix.reverse.zipWithIndex.map { case (layer, index) =>
      layer.zip(initialOutputs).map { case (weights, prevOutput) =>
        // to change to match case head :+ tail
        val deltas = weights.map { calculatedValue => {
            logger.info(s"$calculatedValue * ${prevOutput.error} * ${derivativeSigmoidNeuronActivation(calculatedValue)}")
            // for the hidden layers, the calculus is between the previous error and the actual weight
            (calculatedValue * prevOutput.error) * derivativeSigmoidNeuronActivation(calculatedValue)
          }
        }
        logger.info(s"iteration $index: $deltas with weights size ${weights.size}")
        deltas.map(Output)
      }
    }
  }*/

  def backwardPropagation(outputs: Vector[Inputs]) = Task.delay {
    // Getting the layers in reverse order as it is BACK propagation
    val reversedOutputs = outputs.reverse
    val finalOutput = reversedOutputs.head
    val firstDelta = finalOutput.values.map { value =>
      val error = (finalOutput.expectedClass.getOrElse(0) - value)
      error * derivativeSigmoidNeuronActivation(value)
    }
    logger.info(s"First delta is $firstDelta")

    weightsMatrix.reverse.zip(reversedOutputs.drop(1)).map { case (layer, output) =>
      // Considering error as a function of the inputs of all neurons receiving input from neuron j
      // weights * every delta of the output neuron the input neuron gives input to
      layer.zip(firstDelta).map { case (weights, delta) =>
        // to change to match case head :+ tail
        val deltas = weights.zip(output.values).map { case (calculatedValue, outputError) => {
                logger.info(s"${calculatedValue} * $delta  * ${derivativeSigmoidNeuronActivation(outputError)}")
                // for the hidden layers, the calculus is between the previous error and the actual weight
                (calculatedValue * delta ) * derivativeSigmoidNeuronActivation(outputError)
            }
          }
        logger.info(s"iteration: $deltas with weights size ${weights.size}")
        deltas
      }
    }
  }
}