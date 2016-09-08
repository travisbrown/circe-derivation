package io.circe.examples.derivation.raw

import io.circe.{ Decoder, Encoder }
import macrocompat.bundle
import scala.reflect.macros.whitebox

@bundle
class DerivationMacros(val c: whitebox.Context) {
  import c.universe._

  def materializeDecoderImpl[T: c.WeakTypeTag]: c.Expr[Decoder[T]] = {
    val tpe = weakTypeOf[T]

    val primaryConstructor = tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    }

    primaryConstructor match {
      case Some(constructor) =>
        val fieldNames: List[Name] = constructor.paramLists.flatten.map(_.name)
        val decodedNames: List[String] = fieldNames.map(_.decodedName.toString)
        val fieldTypes: List[Type] = constructor.paramLists.flatten.map { field =>
          tpe.decl(field.name).typeSignature
        }
        val fieldCount = fieldNames.size
        val functionParameters = fieldNames.zip(fieldTypes).map {
          case (fieldName, fieldType) =>
            val termName = TermName(fieldName.toString)
            q"$termName: $fieldType"
        }
        val parameters = fieldNames.map { fieldName =>
          val termName = TermName(fieldName.toString)
          q"$termName"
        }

        val methodName = TermName(s"forProduct$fieldCount")

        if (fieldCount > 0) c.Expr[Decoder[T]](
          q"""
            _root_.io.circe.Decoder.$methodName[..$fieldTypes, $tpe](..$decodedNames)(
              (..$functionParameters) => new $tpe(..$parameters)
            )
          """
        ) else c.Expr[Decoder[T]](q"_root_.io.circe.Decoder.const(new $tpe())")
      case None => c.abort(c.enclosingPosition, s"Could not identify primary constructor for $tpe")
    }
  }

  def materializeEncoderImpl[T: c.WeakTypeTag]: c.Expr[Encoder[T]] = {
    val tpe = weakTypeOf[T]

    val primaryConstructor = tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    }

    primaryConstructor match {
      case Some(constructor) =>
        val fieldNames: List[Name] = constructor.paramLists.flatten.map(_.name)
        val decodedNames: List[String] = fieldNames.map(_.decodedName.toString)
        val fieldTypes: List[Type] = constructor.paramLists.flatten.map { field =>
          tpe.decl(field.name).typeSignature
        }
        val fieldCount = fieldNames.size
        val invocations = fieldNames.map { fieldName => 
          val termName = TermName(fieldName.toString)
          q"toEncode.$termName"
        }

        val methodName = TermName(s"forProduct$fieldCount")

        if (fieldCount > 0) c.Expr[Encoder[T]](
          q"""
            _root_.io.circe.Encoder.$methodName[..$fieldTypes, $tpe](..$decodedNames)(
              toEncode => (..$invocations)
            )
          """
        ) else c.Expr[Encoder[T]](
          q"""
            _root_.io.circe.Encoder.instance[$tpe](_ => _root_.io.circe.Json.obj())
          """
        )
      case None => c.abort(c.enclosingPosition, s"Could not identify primary constructor for $tpe")
    }
  }
}
