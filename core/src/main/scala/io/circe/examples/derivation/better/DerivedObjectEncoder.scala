package io.circe.examples.derivation.better

import export.exports
import io.circe.{ Encoder, JsonObject, ObjectEncoder }
import shapeless._, shapeless.labelled.FieldType

abstract class DerivedObjectEncoder[A] extends ObjectEncoder[A]

@exports
final object DerivedObjectEncoder extends LowPriorityDerivedObjectEncoders {
  implicit val encodeHNil: DerivedObjectEncoder[HNil] = new DerivedObjectEncoder[HNil] {
    def encodeObject(a: HNil): JsonObject = JsonObject.empty
  }

  implicit def encodeHCons[K <: Symbol, H, T <: HList](implicit
    key: Witness.Aux[K],
    encodeH: Lazy[Encoder[H]],
    encodeT: Lazy[DerivedObjectEncoder[T]]
  ): DerivedObjectEncoder[FieldType[K, H] :: T] = new DerivedObjectEncoder[FieldType[K, H] :: T] {
    def encodeObject(a: FieldType[K, H] :: T): JsonObject = a match {
      case h :: t => (key.value.name -> encodeH.value(h)) +: encodeT.value.encodeObject(t)
    }
  }

  implicit val encodeCNil: DerivedObjectEncoder[CNil] = new DerivedObjectEncoder[CNil] {
    def encodeObject(a: CNil): JsonObject =
      sys.error("No JSON representation of CNil (this shouldn't happen)")
  }

  implicit def encodeCoproduct[K <: Symbol, L, R <: Coproduct](implicit
    key: Witness.Aux[K],
    encodeL: Lazy[Encoder[L]],
    encodeR: Lazy[DerivedObjectEncoder[R]]
  ): DerivedObjectEncoder[FieldType[K, L] :+: R] = new DerivedObjectEncoder[FieldType[K, L] :+: R] {
    def encodeObject(a: FieldType[K, L] :+: R): JsonObject = a match {
      case Inl(l) => JsonObject.singleton(key.value.name, encodeL.value(l))
      case Inr(r) => encodeR.value.encodeObject(r)
    }
  }

  implicit def encodeCaseClass[A, R <: HList](implicit
    gen: LabelledGeneric.Aux[A, R],
    encodeR: Lazy[DerivedObjectEncoder[R]]
  ): DerivedObjectEncoder[A] = new DerivedObjectEncoder[A] {
    def encodeObject(a: A): JsonObject = encodeR.value.encodeObject(gen.to(a))
  }

  implicit def encodeAdt[A, R <: Coproduct](implicit
    gen: LabelledGeneric.Aux[A, R],
    encodeR: Lazy[DerivedObjectEncoder[R]]
  ): DerivedObjectEncoder[A] = new DerivedObjectEncoder[A] {
    def encodeObject(a: A): JsonObject = encodeR.value.encodeObject(gen.to(a))
  }
}

private[circe] trait LowPriorityDerivedObjectEncoders {
  implicit def encodeHConsDerived[K <: Symbol, H, T <: HList](implicit
    key: Witness.Aux[K],
    encodeH: Lazy[DerivedObjectEncoder[H]],
    encodeT: Lazy[DerivedObjectEncoder[T]]
  ): DerivedObjectEncoder[FieldType[K, H] :: T] = new DerivedObjectEncoder[FieldType[K, H] :: T] {
    def encodeObject(a: FieldType[K, H] :: T): JsonObject = a match {
      case h :: t => (key.value.name -> encodeH.value(h)) +: encodeT.value.encodeObject(t)
    }
  }

  implicit def encodeCoproductDerived[K <: Symbol, L, R <: Coproduct](implicit
    key: Witness.Aux[K],
    encodeL: Lazy[DerivedObjectEncoder[L]],
    encodeR: Lazy[DerivedObjectEncoder[R]]
  ): DerivedObjectEncoder[FieldType[K, L] :+: R] = new DerivedObjectEncoder[FieldType[K, L] :+: R] {
    def encodeObject(a: FieldType[K, L] :+: R): JsonObject = a match {
      case Inl(l) => JsonObject.singleton(key.value.name, encodeL.value(l))
      case Inr(r) => encodeR.value.encodeObject(r)
    }
  }
}
