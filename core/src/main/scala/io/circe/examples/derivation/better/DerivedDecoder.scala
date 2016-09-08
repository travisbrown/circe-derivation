package io.circe.examples.derivation.better

import cats.data.Xor
import export.exports
import io.circe.{ Decoder, DecodingFailure, HCursor }
import shapeless.{ ::, :+:, CNil, Coproduct, HList, HNil, Inl, Inr, LabelledGeneric, Lazy, Witness }
import shapeless.labelled.{ FieldType, field }

abstract class DerivedDecoder[A] extends Decoder[A]

@exports
final object DerivedDecoder extends LowPriorityDerivedDecoders {
  implicit val decodeHNil: DerivedDecoder[HNil] = new DerivedDecoder[HNil] {
    def apply(c: HCursor): Decoder.Result[HNil] = Xor.right(HNil)
  }

  implicit def decodeHCons[K <: Symbol, H, T <: HList](implicit
    key: Witness.Aux[K],
    decodeH: Lazy[Decoder[H]],
    decodeT: Lazy[DerivedDecoder[T]]
  ): DerivedDecoder[FieldType[K, H] :: T] = new DerivedDecoder[FieldType[K, H] :: T] {
    def apply(c: HCursor): Decoder.Result[FieldType[K, H] :: T] = for {
      h <- c.get(key.value.name)(decodeH.value)
      t <- decodeT.value(c)
    } yield field[K](h) :: t
  }

  implicit val decodeCNil: DerivedDecoder[CNil] = new DerivedDecoder[CNil] {
    def apply(c: HCursor): Decoder.Result[CNil] = Xor.left(DecodingFailure("CNil", c.history))
  }

  implicit def decodeCoproduct[K <: Symbol, L, R <: Coproduct](implicit
    key: Witness.Aux[K],
    decodeL: Lazy[Decoder[L]],
    decodeR: Lazy[DerivedDecoder[R]]
  ): DerivedDecoder[FieldType[K, L] :+: R] = new DerivedDecoder[FieldType[K, L] :+: R] {
    def apply(c: HCursor): Decoder.Result[FieldType[K, L] :+: R] =
      c.downField(key.value.name).focus match {
        case Some(value) => value.as(decodeL.value).map(l => Inl(field(l)))
        case None => decodeR.value(c).map(Inr(_))
      }
  }

  implicit def decodeCaseClass[A, R <: HList](implicit
    gen: LabelledGeneric.Aux[A, R],
    decodeR: Lazy[DerivedDecoder[R]]
  ): DerivedDecoder[A] = new DerivedDecoder[A] {
    def apply(c: HCursor): Decoder.Result[A] = decodeR.value(c).map(gen.from)
  }

  implicit def decodeAdt[A, R <: Coproduct](implicit
    gen: LabelledGeneric.Aux[A, R],
    decodeR: Lazy[DerivedDecoder[R]]
  ): DerivedDecoder[A] = new DerivedDecoder[A] {
    def apply(c: HCursor): Decoder.Result[A] = decodeR.value(c).map(gen.from)
  }
}

trait LowPriorityDerivedDecoders {
  implicit def decodeHConsDerived[K <: Symbol, H, T <: HList](implicit
    key: Witness.Aux[K],
    decodeH: Lazy[DerivedDecoder[H]],
    decodeT: Lazy[DerivedDecoder[T]]
  ): DerivedDecoder[FieldType[K, H] :: T] = new DerivedDecoder[FieldType[K, H] :: T] {
    def apply(c: HCursor): Decoder.Result[FieldType[K, H] :: T] = for {
      h <- c.get(key.value.name)(decodeH.value)
      t <- decodeT.value(c)
    } yield field[K](h) :: t
  }

  implicit def decodeCoproductDerived[K <: Symbol, L, R <: Coproduct](implicit
    key: Witness.Aux[K],
    decodeL: Lazy[DerivedDecoder[L]],
    decodeR: Lazy[DerivedDecoder[R]]
  ): DerivedDecoder[FieldType[K, L] :+: R] = new DerivedDecoder[FieldType[K, L] :+: R] {
    def apply(c: HCursor): Decoder.Result[FieldType[K, L] :+: R] =
      c.downField(key.value.name).focus match {
        case Some(value) => value.as(decodeL.value).map(l => Inl(field(l)))
        case None => decodeR.value(c).map(Inr(_))
      }
  }
}
