package io.circe.examples.derivation.dryer

import cats.data.Xor
import io.circe.{ Decoder, DecodingFailure }
import shapeless.{ :+:, ::, CNil, Coproduct, HList, Inl, Inr, HNil }
import shapeless.{ LabelledTypeClass, LabelledTypeClassCompanion }

object decoders extends LabelledTypeClassCompanion[Decoder] {
  val typeClass: LabelledTypeClass[Decoder] = new LabelledTypeClass[Decoder] {
    def emptyProduct: Decoder[HNil] = Decoder.const(HNil)

    def product[H, T <: HList](name: String,
      decodeH: Decoder[H],
      decodeT: Decoder[T]
    ): Decoder[H :: T] = Decoder.instance { c =>
      for {
        h <- c.get(name)(decodeH)
        t <- decodeT(c)
      } yield h :: t
    }

    def emptyCoproduct: Decoder[CNil] = 
      Decoder.instance(c => Xor.left(DecodingFailure("CNil", c.history)))

    def coproduct[L, R <: Coproduct](
      name: String,
      decodeL: => Decoder[L],
      decodeR: => Decoder[R]
    ): Decoder[L :+: R] = Decoder.instance { c =>
      c.downField(name).focus match {
        case Some(value) => decodeL.decodeJson(value).map(Inl(_))
        case None => decodeR(c).map(Inr(_))
      }
    }

    def project[F, G](instance: => Decoder[G], to: F => G, from: G => F): Decoder[F] =
      instance.map(from)
  }
}

