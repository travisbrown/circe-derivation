package io.circe.examples.derivation.simple

import cats.data.Xor
import io.circe.{ Decoder, DecodingFailure, Encoder, JsonObject, ObjectEncoder }
import shapeless.{ :+:, CNil, Coproduct, Inl, Inr, Witness }
import shapeless.labelled.{ FieldType, field }

trait CoproductCodecs {
  implicit val decodeCNil: Decoder[CNil] =
    Decoder.instance(c => Xor.left(DecodingFailure("CNil", c.history)))

  implicit def decodeCoproduct[K <: Symbol, L, R <: Coproduct](implicit
    key: Witness.Aux[K],
    decodeL: Decoder[L],
    decodeR: Decoder[R]
  ): Decoder[FieldType[K, L] :+: R] = Decoder.instance { c =>
    c.downField(key.value.name).focus match {
      case Some(value) => value.as[L].map(l => Inl(field(l)))
      case None => decodeR(c).map(Inr(_))
    }
  }

  implicit val encodeCNil: ObjectEncoder[CNil] =
    ObjectEncoder.instance(_ => sys.error("No JSON representation of CNil (this shouldn't happen)"))

  implicit def encodeCoproduct[K <: Symbol, L, R <: Coproduct](implicit
    key: Witness.Aux[K],
    encodeL: Encoder[L],
    encodeR: ObjectEncoder[R]
  ): ObjectEncoder[FieldType[K, L] :+: R] = ObjectEncoder.instance {
    case Inl(l) => JsonObject.singleton(key.value.name, encodeL(l))
    case Inr(r) => encodeR.encodeObject(r)
  }
}
