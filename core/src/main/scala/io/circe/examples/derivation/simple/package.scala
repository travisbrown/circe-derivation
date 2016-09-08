package io.circe.examples.derivation

import io.circe.{ Decoder, ObjectEncoder }
import shapeless.LabelledGeneric

package object simple extends HListCodecs with CoproductCodecs {
  implicit def decodeGeneric[A, R](implicit
    gen: LabelledGeneric.Aux[A, R],
    decodeR: Decoder[R]
  ): Decoder[A] = decodeR.map(gen.from)

  implicit def encodeGeneric[A, R](implicit
    gen: LabelledGeneric.Aux[A, R],
    encodeR: ObjectEncoder[R]
  ): ObjectEncoder[A] = ObjectEncoder.instance(a => encodeR.encodeObject(gen.to(a)))
}
