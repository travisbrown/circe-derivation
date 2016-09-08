package io.circe.examples.derivation.simple

import io.circe.{ Decoder, Encoder, JsonObject, ObjectEncoder }
import shapeless.{ ::, HList, HNil, Witness }
import shapeless.labelled.{ FieldType, field }

trait HListCodecs {
  implicit val decodeHNil: Decoder[HNil] = Decoder.const(HNil)

  implicit def decodeHCons[K <: Symbol, H, T <: HList](implicit
    key: Witness.Aux[K],
    decodeH: Decoder[H],
    decodeT: Decoder[T]
  ): Decoder[FieldType[K, H] :: T] = Decoder.instance { c =>
    for {
      h <- c.get[H](key.value.name)
      t <- decodeT(c)
    } yield field[K](h) :: t
  }

  implicit val encodeHNil: ObjectEncoder[HNil] = ObjectEncoder.instance(_ => JsonObject.empty)

  implicit def encodeHCons[K <: Symbol, H, T <: HList](implicit
    key: Witness.Aux[K],
    encodeH: Encoder[H],
    encodeT: ObjectEncoder[T]
  ): ObjectEncoder[FieldType[K, H] :: T] = ObjectEncoder.instance {
    case h :: t => (key.value.name -> encodeH(h)) +: encodeT.encodeObject(t)
  }
}
