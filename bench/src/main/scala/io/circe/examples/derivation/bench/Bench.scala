package io.circe.examples.derivation.bench

import java.io.{ File, FileWriter, PrintWriter }
import scala.concurrent.duration.Duration

abstract class Bench(name: String) {
  def ccResults: List[Duration]
  def adtResults: List[Duration]
}

object Bench {
  def main(args: Array[String]): Unit = {
    val writer = new PrintWriter(new FileWriter(new File(args(0))))

    writer.println("time,deriver,size")

    Raw.ccResults.zipWithIndex.foreach {
      case (result, i) =>
        writer.println(s"${ result.toMillis }, raw macros, ${ i % math.min(Raw.size + 1, 23) }")
    }

    Simple.ccResults.zipWithIndex.foreach {
      case (result, i) => writer.println(s"${ result.toMillis }, simple Shapeless, ${ i % (Simple.size + 1) }")
    }

    /*Dryer.ccResults.zipWithIndex.foreach {
      case (result, i) => writer.println(s"${ result.toMillis }, D, ${ i % (Dryer.size + 1) }")
    }*/

    Better.ccResults.zipWithIndex.foreach {
      case (result, i) => writer.println(s"${ result.toMillis }, better Shapeless, ${ i % (Better.size + 1) }")
    }

    Generic.ccResults.zipWithIndex.foreach {
      case (result, i) => writer.println(s"${ result.toMillis }, circe-generic, ${ i % (Generic.size + 1) }")
    }

    writer.close()

    val adtWriter = new PrintWriter(new FileWriter(new File(args(1))))

    adtWriter.println("time,deriver,size")

    Simple.adtResults.zipWithIndex.foreach {
      case (result, i) => adtWriter.println(s"${ result.toMillis }, simple Shapeless, ${ i % Simple.size + 1}")
    }

    /*Dryer.adtResults.zipWithIndex.foreach {
      case (result, i) => adtWriter.println(s"${ result.toMillis }, D, ${ i % Dryer.size + 1 }")
    }*/

    Better.adtResults.zipWithIndex.foreach {
      case (result, i) => adtWriter.println(s"${ result.toMillis }, better Shapeless, ${ i % Better.size + 1 }")
    }

    Generic.adtResults.zipWithIndex.foreach {
      case (result, i) => adtWriter.println(s"${ result.toMillis }, circe-generic, ${ i % Generic.size + 1 }")
    }

    adtWriter.close()


    val deepWriter = new PrintWriter(new FileWriter(new File(args(2))))

    deepWriter.println("time,deriver,depth")

    Raw.deepResults.zipWithIndex.foreach {
      case (result, i) => deepWriter.println(s"${ result.toMillis }, raw macros, ${ i % (Raw.size + 1) }")
    }

    Simple.deepResults.zipWithIndex.foreach {
      case (result, i) => deepWriter.println(s"${ result.toMillis }, simple Shapeless, ${ i % (Simple.size + 1) }")
    }

    /*Dryer.deepResults.zipWithIndex.foreach {
      case (result, i) => deepWriter.println(s"${ result.toMillis }, D, ${ i % Dryer.size + 1 }")
    }*/

    Better.deepResults.zipWithIndex.foreach {
      case (result, i) => deepWriter.println(s"${ result.toMillis }, better Shapeless, ${ i % (Better.size + 1) }")
    }

    Generic.deepResults.zipWithIndex.foreach {
      case (result, i) => deepWriter.println(s"${ result.toMillis }, circe-generic, ${ i % (Generic.size + 1) }")
    }

    deepWriter.close()
  }
  
}
