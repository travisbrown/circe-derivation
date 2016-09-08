package io.circe.examples.derivation.dryer

import io.circe.{ Encoder, Json, JsonObject }
import shapeless.{ :+:, ::, CNil, Coproduct, HList, Inl, Inr, HNil }
import shapeless.{ LabelledTypeClass, LabelledTypeClassCompanion }

object encoders extends LabelledTypeClassCompanion[Encoder] {
  val typeClass: LabelledTypeClass[Encoder] = new LabelledTypeClass[Encoder] {
    def emptyProduct: Encoder[HNil] = Encoder.instance(_ => Json.fromJsonObject(JsonObject.empty))

    def product[H, T <: HList](
      name: String,
      encodeH: Encoder[H],
      encodeT: Encoder[T]
    ): Encoder[H :: T] = Encoder.instance {
      case h :: t =>
        val encodedTail = encodeT(t).asObject.getOrElse(
          sys.error("An HList encoder does not return a JSON object")
        )

        Json.fromJsonObject((name -> encodeH(h)) +: encodedTail)
    }

    def emptyCoproduct: Encoder[CNil] = Encoder.instance(_ =>
      sys.error("No JSON representation of CNil (this shouldn't happen)")
    )

    def coproduct[L, R <: Coproduct](
      name: String,
      encodeL: => Encoder[L],
      encodeR: => Encoder[R]
    ): Encoder[L :+: R] = Encoder.instance {
      case Inl(l) => Json.obj(name -> encodeL(l))
      case Inr(r) => encodeR(r)
    }

    def project[F, G](instance: => Encoder[G], to: F => G, from: G => F): Encoder[F] =
      instance.contramap(to)
  }
}

