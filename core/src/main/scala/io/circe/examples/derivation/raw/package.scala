package io.circe.examples.derivation

import io.circe.{ Decoder, Encoder }
import scala.language.experimental.macros

package object raw {
  implicit def deriveDecoder[A]: Decoder[A] = macro DerivationMacros.materializeDecoderImpl[A]
  implicit def deriveEncoder[A]: Encoder[A] = macro DerivationMacros.materializeEncoderImpl[A]
}
